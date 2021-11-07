name := "g-gen"

version := "0.1"

scalaVersion := "2.13.6"



libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.17"
libraryDependencies += "com.typesafe.akka" %% "akka-discovery" % "2.6.17"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.17"
libraryDependencies += "com.lightbend.akka.management" %% "akka-management" % "1.1.1"
libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.6.17"
libraryDependencies += "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "1.1.1"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.6.17"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.30"
libraryDependencies += "org.scala-graph" %% "graph-core" % "1.13.2"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.6.17" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.3.0-SNAP3" % Test








