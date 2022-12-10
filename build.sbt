ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "backend"
  )

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.10"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "ch.megard" %% "akka-http-cors" % "1.1.3",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.7.0",
  "org.slf4j" % "slf4j-simple" % "1.6.4",
  "com.github.jwt-scala" %% "jwt-core" % "9.1.2",
  "com.github.jw3" %% "webhooks" % "0.6",
  "io.spray" %%  "spray-json" % "1.3.6",
  "com.lihaoyi" %% "upickle" % "0.9.5"
)

resolvers += "jw3 at bintray" at "https://dl.bintray.com/jw3/maven"