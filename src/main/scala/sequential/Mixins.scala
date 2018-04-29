package sequential

import java.util.UUID

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.joda.time.{LocalDate, DateTime => JodaTime}
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import spray.json._
import DefaultJsonProtocol._
import sequential.data.PostgresDbDriver.api._
import org.apache.commons.codec.binary.Base64
import sequential.data.DBConnection

import scala.concurrent._


/**
 * Mix these traits in with any class/object that needs references to the actor
 * system, context, etc.
 */
trait ActorSystemProvider {
  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit def executor: ExecutionContext
}

trait LogProvider {
  implicit def log: LoggingAdapter
}

trait UUIDFormat extends RootJsonFormat[UUID] {
  def write(obj: UUID): JsValue = {
    JsString(obj.toString)
  }

  def read(json: JsValue): UUID = json match {
    case JsString(s) => try {
      UUID.fromString(s)
    }
    catch {
      case t: Throwable => error(s)
    }
    case _ =>
      error(json.toString())
  }

  def error(v: Any): UUID = {
    val example = UUID.randomUUID()
    deserializationError(s"'$v' is not a valid UUID value. UUIDs should be of a format like '$example'")
  }
}

trait CustomDateTimeFormat extends RootJsonFormat[LocalDate] {
  val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  def write(obj: LocalDate): JsValue = {
    JsString(formatter.print(obj))
  }

  def read(json: JsValue): LocalDate = json match {
    case JsString(s) => try {
      LocalDate.parse(s, formatter)
    }
    catch {
      case t: Throwable => error(s)
    }
    case _ =>
      error(json.toString())
  }

  def error(v: Any): LocalDate = {
    val example = formatter.print(0)
    deserializationError(s"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
  }
}