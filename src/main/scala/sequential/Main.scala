package sequential

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.CorsDirectives._
import ch.megard.akka.http.cors.CorsSettings
import sequential.dao.{AvailabilityDao, TableDao}
import sequential.data.ErrorResponse
import sequential.service._
import sequential.service.{RootService, SwaggerService}
import slick.jdbc.{GetResult, PositionedResult}

import scala.util.{Failure, Success}
import scala.collection.TraversableOnce._
import scala.collection.immutable.{Seq => ISeq}

trait ServiceStack
  extends ActorSystemProvider
     with LogProvider
     with RootService
     with SwaggerService
     with TableService
     with AvailabilityService

trait Server extends ServiceStack {
  implicit val system = ActorSystem("Sequential")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  implicit val log = Logging(system, "Sequential")
  val tableDao = new TableDao
  val availabilityDao = new AvailabilityDao

  implicit def myExceptionHandler: ExceptionHandler = ExceptionHandler {
    case ex: IllegalArgumentException =>
      complete(HttpResponse(StatusCodes.BadRequest, entity = ex.getMessage))
    case ex: NoSuchElementException =>
      complete(HttpResponse(StatusCodes.BadRequest, entity = ex.getMessage))
    case ex: Exception =>
      complete(HttpResponse(StatusCodes.BadRequest, entity = ex.getMessage))
  }


  val corsAllowedMethods = ISeq(HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT, HttpMethods.DELETE,
                                HttpMethods.HEAD, HttpMethods.OPTIONS, HttpMethods.PATCH)
  val corsSettings = CorsSettings.defaultSettings.copy(allowedMethods = corsAllowedMethods)

  val appRoutes: Route = cors(corsSettings) {
    rootRoutes ~
  pathPrefix(root) {
      referenceDataRoutes ~
        seqRulesRoutes ~
      swaggerRoutes
    }
  }
  // HOST should be 0.0.0.0 for Scala apps on CloudFoundry
  // PORT should be the PORT from CloudFoundry otherwise config in build to point to another port when running locally
  // PORT uses `System.getenv` so it does not fail if not in environment for local dev, all other env vars use `sys.env`
  val appHost = "0.0.0.0"
  val appPort = Option(System.getenv("PORT")).getOrElse("8080").toInt

  log.info(s"Starting service bound on http://$appHost:$appPort...")
  log.info(s"Swagger Docs found at http://$appHost:$appPort/sequential/docs")

  Http().bindAndHandle(appRoutes, appHost, appPort).onComplete {
    case Success(sb) => log.info("Server successfully started.")
    case Failure(ex) => log.error("Error starting server", ex.getMessage)
  }
  sys.addShutdownHook(system.terminate())

}

object Main extends Server with App