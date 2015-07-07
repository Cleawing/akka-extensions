package com.cleawing.akka.http

import java.io.{File, FileReader, BufferedReader, FileInputStream}
import java.security.cert.{CertificateFactory, Certificate}
import java.security.{KeyPair, KeyStore, SecureRandom, Security}
import javax.net.ssl.{KeyManagerFactory, X509TrustManager, TrustManagerFactory, SSLContext}

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMReader

trait TLSSupport {
  val keyPath: String
  val certPath: String
  val caPath: Option[String]

  def getSSLContext: SSLContext
}

object TLSSupport {
  def apply(cert_path: String): TLSSupport = {
    val path = new File(cert_path)

    if (!path.exists()) illegal(s"Path '$cert_path' is not exist")
    if (!path.isDirectory) illegal(s"Path '$cert_path' is not a directory")

    val (key, cert) = (pem(path, "key"), pem(path, "cert"))

    val ca = List(key, cert).filterNot(isFile) match {
      case List(_, _) => illegal(s"Path '$cert_path' does not contain any certs")
      case List(missed_path) => illegal(s"Path '$missed_path' is not a file")
      case List() => if (isFile(pem(path, "ca"))) Some(pem(path, "ca")) else None
    }
    apply(key, cert, ca)
  }

  def apply(keyPath: String, certPath: String, caPath: Option[String] = None): TLSSupport = {
    apply(List(Some(keyPath), Some(certPath), caPath))
  }

  private def apply(paths: List[Option[String]]): TLSSupport = {
    paths.collect {
      case Some(f) if !(isFile(f) && isCanRead(f)) =>
        illegal(s"Path '$f' is not readable or not a file")
    }
    paths match {
      case List(Some(keyPath), Some(certPath), caPath) => new TLSSupportImpl(keyPath, certPath, caPath)
    }
  }

  private def pem(path: File, name: String) = s"${path.getAbsolutePath}/$name.pem"
  private def isFile(f: String) = new File(f).isFile
  private def isCanRead(f: String) = new File(f).canRead
  private def illegal(msg: String) = throw new IllegalArgumentException(msg)
}

private case class TLSSupportImpl(keyPath: String, certPath: String, caPath: Option[String]) extends TLSSupport {
  Security.addProvider(new BouncyCastleProvider)

  def getSSLContext : SSLContext = {
    val ctx = SSLContext.getInstance("TLSv1")
    val trust = for {
      ca    <- caPath
      trust <- trustManager(ca)
    } yield trust

    ctx.init(keyManagers, trust.map(Array(_)).orNull, new SecureRandom)
    ctx
  }

  private def certificate(path: String): Certificate = {
    val certStm = new FileInputStream(path)
    try CertificateFactory.getInstance("X.509").generateCertificate(certStm)
    finally certStm.close()
  }

  private def withStore[T](f: KeyStore => T): KeyStore = {
    val store = KeyStore.getInstance(KeyStore.getDefaultType)
    f(store)
    store
  }

  private def keyStore = {
    val key = new PEMReader(new BufferedReader(new FileReader(keyPath))).
      readObject().asInstanceOf[KeyPair].getPrivate

    withStore { store =>
      store.load(null, null)
      store.setKeyEntry("key", key, "".toCharArray, Array(certificate(certPath)))}
  }

  private def trustStore(caPath: String) = withStore { store =>
    store.load(null, null)
    store.setCertificateEntry("cacert", certificate(caPath))
  }

  private def trustManager(caPath: String) = {
    val fact = TrustManagerFactory.getInstance("SunX509", "SunJSSE")
    fact synchronized {
      fact.init(trustStore(caPath))
      fact.getTrustManagers.find(_.isInstanceOf[X509TrustManager])
    }
  }

  private def keyManagers = {
    val algo = Option(Security.getProperty("ssl.KeyManagerFactory.algorithm")).getOrElse("SunX509")
    val kmf = KeyManagerFactory.getInstance(algo)
    kmf.init(keyStore, "".toCharArray)
    kmf.getKeyManagers
  }
}
