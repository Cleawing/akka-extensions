package com.cleawing.akka.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.{HttpsContext, Http}
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.{StatusCode, HttpResponse, HttpRequest}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source, Flow}

import scala.concurrent.{Future, ExecutionContextExecutor}

trait Client {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer
  implicit val logger: LoggingAdapter

  val host: String
  val port: Int
  val tlsOn: Boolean
  val tlsSupport: Option[TLSSupport]
  
  lazy val simpleConnectionFlow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] = (tlsOn, tlsSupport) match {
    case (true, Some(tls)) =>
      Http().outgoingConnectionTls(
        host = host,
        port = port,
        httpsContext = Some(HttpsContext(tls.getSSLContext))
      )
    case _ => Http().outgoingConnection(host, port)
  }

  def singleRequest(request: HttpRequest): Future[HttpResponse] = {
    try {
      Source.single(request).
        via(simpleConnectionFlow).
        runWith(Sink.head)
    } catch { // Workaround. Sometimes instead of StreamTcpException("Connection failed.") we got NoSuchElementException
      case _ : NoSuchElementException => singleRequest(request)
      case t : Throwable => throw t
    }
  }
}

object Client {
  sealed trait Response {
    val status: StatusCode
    val body: String
  }

  case class Success(status: StatusCode, body: String) extends Response
  case class Error(status: StatusCode, body: String) extends Response
  case class Failure(cause: Throwable)
}
