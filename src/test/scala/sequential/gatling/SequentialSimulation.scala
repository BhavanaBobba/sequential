package sequential.gatling

import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import akka.http.scaladsl.Http

import scala.concurrent.Await
import scala.concurrent.duration._


class SequentialSimulation extends Simulation  {
  val appUrlBase = sys.env("GATLING_URL")
  val appHttpProtocol = if (sys.env("localDevelopment") == "true") "http://" else "https://"

  // Environment to use (eg. Local, np, prod)
  val toUse = appHttpProtocol.concat(appUrlBase)

  // http configuration for gatling to use
  val httpConf = http.baseURL(toUse)

  // Scenario for gatling to run
  val sequentialScenario = scenario("SequentialSimulation").repeat(3){
    exec(
      ListUsersSimulationObject.callEndpoint
    )
  }

// Setup the scenario for users and to use the http configuration
  setUp(sequentialScenario.inject(atOnceUsers(5))).protocols(httpConf)
    .assertions(global.successfulRequests.percent.greaterThan(95))
}
