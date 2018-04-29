package sequential.data

import spray.json._, DefaultJsonProtocol._

case class OrderRequest(requestType: String, groupedAttribute: String, orderQuantity: Long,
                        orderConfirmed: Long, unconfirmedOrder: Long, records: Seq[Records])
  extends RestRequest

case class Request(seqName: String, records: Seq[Records])
  extends RestRequest

object OrderRequest extends RestRequest {
  implicit val availabilityJsonFormat = jsonFormat(OrderRequest.apply,
    "requestType",
    "groupedAttribute",
    "orderQuantity",
    "orderConfirmed",
    "unconfirmedOrder",
    "records"
  )
}

object Request extends RestRequest {
  implicit val requestJsonFormat = jsonFormat(Request.apply,
    "seqName",
    "records"
  )
}

//do we get multiple acroynms in same request? if so each will have their own data