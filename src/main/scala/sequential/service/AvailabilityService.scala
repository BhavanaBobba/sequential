package sequential.service

import javax.ws.rs.Path

import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import io.swagger.annotations._
import sequential.dao.{AvailabilityDao}
import sequential.data._
import sequential.{ActorSystemProvider, LogProvider}
import spray.json.DefaultJsonProtocol._
import spray.json._

@Path("/")
@Api(value = "/availability", produces = "application/json", consumes = "application/json")
trait AvailabilityService extends ActorSystemProvider with LogProvider with ServiceCommons with SprayJsonSupport
  with Directives {

  val availabilityDao: AvailabilityDao

  @Path("/seq-rule-assignment/{requestType}/attribute/{attributeValue}")
  @ApiOperation(httpMethod = "GET", response = classOf[OkResponse], value = "Provides Seq Rule Assignment")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "requestType", required = true, paramType = "path",
    dataType = "string", value = "Please provide the requestType to get rule"),
    new ApiImplicitParam(name = "attributeValue", required = true, paramType = "path",
      dataType = "string", value = "Please provide the attribute value to get rule")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully gets seq Rule")))
  def getSeqRuleAssignmentPath = pathPrefix("seq-rule-assignment" / Segment / "attribute" / Segment) { (requestType, attributeValue) =>
    get {
      logRequest(s"GET /$root/seq-rule-assignment/$requestType/attribute/$attributeValue", Logging.InfoLevel) {
        complete(availabilityDao.getSeqRuleAssignment(requestType, attributeValue))
      }
    } ~
      pathEnd(get(redirect(s"/$root/seq-rule-assignment/$requestType/attribute/$attributeValue", StatusCodes.TemporaryRedirect)))
  }

  @Path("/seq-rules/{tableName}/seq-name/{seqName}")
  @ApiOperation(httpMethod = "GET", response = classOf[OkResponse], value = "Provides Seq Rules")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to get data"),
    new ApiImplicitParam(name = "seqName", required = true, paramType = "path",
      dataType = "string", value = "Please provide the seq name to get rule")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully gets seq Rule")))
  def getSeqRulePath = pathPrefix("seq-rules" / Segment / "seq-name" / Segment) { (tableName, seqName) =>
    get {
      logRequest(s"GET /$root/seq-rules/$tableName/seq-name/$seqName", Logging.InfoLevel) {
        complete(availabilityDao.getSeqRule(tableName, seqName))
      }
    } ~
      pathEnd(get(redirect(s"/$root/seq-rules/$tableName/seq-name/$seqName", StatusCodes.TemporaryRedirect)))
  }

  @Path("/availability")
  @ApiOperation(httpMethod = "POST", response = classOf[OkResponse], value = "Calculates stock availability")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "request", required = true, paramType = "body",
    dataType = "sequential.data.Request", value = "Please provide the order details to get stock availability")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully calculates stock availability")))
  def getAvailabilityPath = path("availability") {
    post {
      entity(as[Request]) { body =>
        logRequest(s"POST /$root/availability", Logging.InfoLevel) {
          complete(availabilityDao.getAvailability(body))
        }
      }
    }
  }

  @Path("/eligibility")
  @ApiOperation(httpMethod = "POST", response = classOf[OkResponse], value = "Provides stock eligibility")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "request", required = true, paramType = "body",
    dataType = "sequential.data.Request", value = "Please provide the details to get stock eligibility")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully provides stock eligibility")))
  def getEligibilityPath = path("eligibility") {
    post {
      entity(as[Request]) { body =>
        logRequest(s"POST /$root/eligibility", Logging.InfoLevel) {
          complete(availabilityDao.getEligibility(body))
        }
      }
    }
  }

  @Path("/order-request")
  @ApiOperation(httpMethod = "POST", response = classOf[OkResponse], value = "Provides data for the request")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "request", required = true, paramType = "body",
    dataType = "sequential.data.OrderRequest", value = "Please provide the order request details")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully provides data for requested order")))
  def getOrderRequestPath = path("order-request") {
    post {
      entity(as[OrderRequest]) { body =>
        logRequest(s"POST /$root/order-request", Logging.InfoLevel) {
          complete(availabilityDao.orderRequest(body))
        }
      }
    }
  }


  val seqRulesRoutes = {
    getSeqRuleAssignmentPath ~
    getSeqRulePath ~
    getAvailabilityPath ~
    getEligibilityPath ~
      getOrderRequestPath
  }

}
