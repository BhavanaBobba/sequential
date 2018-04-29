
import ReleaseTransformations._

enablePlugins(JavaAppPackaging)
enablePlugins(GatlingPlugin)

name := "sequential"

scalaVersion := "2.11.8"

coverageExcludedPackages := "sequential.gatling.*"
addCommandAlias("testCoverage", "; clean; scalastyle; coverage; test; coverageOff; coverageReport; coverageAggregate")

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,              // : ReleaseStep
  inquireVersions,                        // : ReleaseStep
  runTest,                                // : ReleaseStep
  setReleaseVersion,                      // : ReleaseStep
  commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
  tagRelease,                             // : ReleaseStep
  setNextVersion,                         // : ReleaseStep
  commitNextVersion                      // : ReleaseStep
//  pushChanges                             // : ReleaseStep, also checks that an upstream branch is properly configured
)

scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor"           % "2.4.12",
    "com.typesafe.akka" %% "akka-http-core"       % "10.0.3",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.3",
    "ch.megard"         %% "akka-http-cors"       % "0.1.11",
    "joda-time"         %  "joda-time"            % "2.9.7",
    "com.julianpeeters" %% "case-class-generator" % "0.7.1",
//    "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "0.18",



  // -- Database
    "org.postgresql"        %  "postgresql"             % "9.4.1212",
    "com.typesafe.slick"    %% "slick"                  % "3.1.1" exclude ("org.slf4j", "slf4j-api"),
    "com.github.tminglei"   %% "slick-pg"               % "0.15.0-M3" exclude ("org.slf4j", "slf4j-api"),
    "com.github.tminglei"   %% "slick-pg_spray-json"    % "0.15.0-M3",
    "com.github.tminglei"   %% "slick-pg_joda-time"     % "0.15.0-M3",
    "commons-dbcp"          %  "commons-dbcp"           % "1.4",

    // Docs
    "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.8.1",

    // Tests
    "com.typesafe.akka" %% "akka-http-testkit"  % "10.0.3"  % "test",
    "org.scalatest"     %% "scalatest"          % "2.2.6"   % "test",
    "org.mockito"       %  "mockito-all"        % "1.10.19" % "test",

    //Automation Tests
    "org.jbehave" % "jbehave-scala" % "3.9.5" % "test",
    "org.apache.httpcomponents" % "httpclient" % "4.5.3",
    "com.mohaine" % "mohaine-utils" % "1.0.2",

    // Gatling
    "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.2" % "test",
    "io.gatling"            % "gatling-test-framework"    % "2.2.2" % "test",

    // scalaj-http
    "org.scalaj" % "scalaj-http_2.11" % "2.3.0"
  )
}

