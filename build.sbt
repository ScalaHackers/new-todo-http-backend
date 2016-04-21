name := "new-todo-http-backend"

packageArchetype.java_application

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.3",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.3",
  "io.spray" %%  "spray-json" % "1.3.2",
  "com.typesafe.slick" %% "slick" % "3.1.0",
  "com.typesafe.slick" %% "slick-extensions" % "3.1.0",
  "com.oracle" % "ojdbc7" % "12.1.0.2",
  "mysql" % "mysql-connector-java" % "5.1.37",
  "com.typesafe.akka" %% "akka-http-testkit-experimental" % "2.0.3" % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-api" % "1.7.19",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.19",
  "ch.qos.logback" % "logback-classic" % "1.1.2" % Runtime,
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

Revolver.settings

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Ylog-classpath")

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"