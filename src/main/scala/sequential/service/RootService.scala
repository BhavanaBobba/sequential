package sequential.service

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import spray.json._
import sequential.{ActorSystemProvider, LogProvider}


trait RootService extends SprayJsonSupport with ActorSystemProvider with LogProvider with ServiceCommons {
  val ping = s"""{"detail": "Service is up.", "name": "$packageName", "version": "$packageVersion"}""".parseJson

  val rootRoutes = pathPrefix("ping") {
    pathEndOrSingleSlash {
      get {
        complete(ping)
      }
    }
  } ~
  pathPrefix(root) {
    pathSingleSlash {
      get {
        complete(ping)
      }
    } ~
    pathEnd(get(redirect(s"/$root/", StatusCodes.TemporaryRedirect)))
  }
}
