package sequential.service

import akka.event.Logging
import akka.http.scaladsl.model._
import com.github.swagger.akka.model.Info
import com.github.swagger.akka.{HasActorSystem, SwaggerHttpService}
import io.swagger.models.Scheme
import sequential.{ActorSystemProvider, LogProvider}

import scala.reflect.runtime.universe.typeOf


trait SwaggerJsonService extends SwaggerHttpService with ActorSystemProvider with HasActorSystem with ServiceCommons {
  implicit val actorSystem = system
  override val apiTypes = Seq(
    typeOf[TableService],
    typeOf[AvailabilityService]
  )

  override val host = sys.env("APP_URL_BASE")
  override val scheme = if (sys.env("localDevelopment") == "true") Scheme.HTTP else Scheme.HTTPS
  override val basePath = root
  override val info = Info(title = packageName, version = "v1",
    description = "An API for uploading data and downloading results.")

  override lazy val routes = path("swagger.json") {
    get {
      complete(HttpEntity(MediaTypes.`application/json`, toJsonString(swagger)))
    }
  }
}

trait SwaggerService extends ActorSystemProvider with LogProvider with ServiceCommons with SwaggerJsonService {
  val swaggerRoutes = pathPrefix("docs") {
    logRequest(s"GET /$root/docs", Logging.InfoLevel) {
      routes ~ // SwaggerJsonService.routes "swagger.json"
        pathEnd {
          get(redirect("docs/index.html", StatusCodes.TemporaryRedirect))
        } ~
        pathSingleSlash {
          get(redirect("index.html", StatusCodes.TemporaryRedirect))
        } ~
        getFromResourceDirectory("swagger")
    }
  }
}
