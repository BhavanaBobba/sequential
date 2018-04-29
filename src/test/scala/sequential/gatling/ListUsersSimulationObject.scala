package sequential.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._


object ListUsersSimulationObject {
  val callEndpoint = exec(
    http(session => "Get list of Details").get("details")
  )
}
