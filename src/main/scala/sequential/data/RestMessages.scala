package sequential.data

import spray.json._
import sequential.CustomDateTimeFormat
import spray.json.DefaultJsonProtocol._

// Requests
trait RestRequest{
  implicit object CustomLocalDateFormat extends CustomDateTimeFormat

}

case class DetailRequest(id: String) extends RestRequest
case class GetRequest() extends RestRequest

// Responses
// Note that `detail` field can be JSON-encoded or just a string. If it's a plain string, the API will return JSON like:
// {"detail": "Your plain string"}
trait RestResponse
case class AcceptedResponse(detail: String) extends RestResponse
case class OkResponse(detail: String) extends RestResponse
case class BadResponse(detail: String) extends RestResponse
case class NotFoundResponse(detail: String) extends RestResponse
case class ErrorResponse(detail: String) extends RestResponse
case class CreatedResponse(detail: String) extends RestResponse

object OkResponse extends RestRequest{
  implicit val okResponseJsonFormat = jsonFormat1(OkResponse.apply)
}
