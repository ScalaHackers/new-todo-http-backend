name := "todo-backend-akka"

packageArchetype.java_application

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.0.3",
  "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % "2.0.3",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "mysql" % "mysql-connector-java" % "5.1.37",
  "com.typesafe.akka" % "akka-http-testkit-experimental_2.11" % "2.0.3" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)

Revolver.settings
