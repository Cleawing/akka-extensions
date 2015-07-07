import org.stormenroute.mecha._
import sbt._
import sbt.Keys._

object Build extends MechaRepoBuild {
  lazy val buildSettings = Defaults.defaultSettings ++
    MechaRepoPlugin.defaultSettings ++ Seq(
    name := "akka-extensions",
    scalaVersion := "2.11.7",
    version := "0.1",
    organization := "com.cleawing",
    libraryDependencies ++= superRepoDependencies("akka-extensions") ++
      Dependencies.akka ++ Dependencies.akkaStreams ++
      Seq(Dependencies.bouncyCastleProvider, Dependencies.json4s, Dependencies.scalaTest)
  )

  def repoName = "akka-extenstions"

  lazy val akkaExtenstions: Project = Project(
    "akka-extentions",
    file("."),
    settings = buildSettings
  ) dependsOnSuperRepo
}
