name := "event-store"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "twitter-repo" at "https://maven.twttr.com"
//resolvers += "centular-repo" at "http://nexus.centular.io/repository/centular/"

libraryDependencies ++= {
  val finagleV = "6.40.0"
  Seq(
    "com.twitter" % "finagle-serversets_2.11" % finagleV,
    "com.twitter" % "util-core_2.11" % "6.39.0")
}
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
libraryDependencies += "ch.qos.logback" %  "logback-classic" % "1.1.8"
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0"
libraryDependencies += "io.centular" % "service-libs_2.11" % "0.14"
libraryDependencies += "org.flywaydb" % "flyway-core" % "4.0.3"
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.2"
libraryDependencies += "net.postgis" % "postgis-jdbc" % "2.2.1"
libraryDependencies += "com.typesafe" % "config" % "1.3.0"
libraryDependencies += "com.thoughtworks.xstream" % "xstream" % "1.4.9"
libraryDependencies += "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.4"

excludeDependencies += "org.slf4j" % "slf4j-jdk14"
excludeDependencies += "org.slf4j" % "slf4j-log4j12"
    