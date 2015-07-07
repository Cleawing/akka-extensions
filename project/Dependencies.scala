import sbt._

object Dependencies {
  object Versions {
    val akka = "2.3.11"
    val akkaUpcoming = "2.4-M2"
    val akkaStreams = "1.0-RC4"
    val bouncyCastleProvider = "1.46"
    val json4sJackson = "3.3.0.RC2"
    val scalaTest = "2.2.5"
  }

  lazy val akka = Seq(
    "com.typesafe.akka" %%  "akka-actor"    % Versions.akka,
    "com.typesafe.akka" %%  "akka-remote"   % Versions.akka,
    "com.typesafe.akka" %%  "akka-cluster"  % Versions.akka,
    "com.typesafe.akka" %%  "akka-testkit"  % Versions.akka % "test"
  )

  lazy val akkaUpcoming = Seq(
    "com.typesafe.akka" %%  "akka-actor"    % Versions.akkaUpcoming,
    "com.typesafe.akka" %%  "akka-remote"   % Versions.akkaUpcoming,
    "com.typesafe.akka" %%  "akka-cluster"  % Versions.akkaUpcoming,
    "com.typesafe.akka" %%  "akka-testkit"  % Versions.akkaUpcoming % "test"
  )

  lazy val akkaStreams = Seq(
    "com.typesafe.akka" %%  "akka-stream-experimental"          % Versions.akkaStreams,
    "com.typesafe.akka" %%  "akka-http-core-experimental"       % Versions.akkaStreams,
    "com.typesafe.akka" %%  "akka-http-experimental"            % Versions.akkaStreams
  )

  lazy val bouncyCastleProvider = "org.bouncycastle" % "bcprov-jdk16" % Versions.bouncyCastleProvider

  lazy val json4s = "org.json4s" %% "json4s-jackson" % Versions.json4sJackson

  lazy val scalaTest  = "org.scalatest" %%  "scalatest" % Versions.scalaTest
}
