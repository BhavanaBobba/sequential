package sequential.data

import spray.json.DefaultJsonProtocol._
import spray.json._

case class Credentials(url: String, user: String, password: String)

object Credentials {
  implicit val format = jsonFormat3(Credentials.apply)
}