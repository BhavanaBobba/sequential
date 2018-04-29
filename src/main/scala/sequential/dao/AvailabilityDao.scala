package sequential.dao

import sequential.data.PostgresDbDriver.api._
import sequential.data.{DBConnection, _}
import slick.jdbc.{GetResult, PositionedResult}
import slick.lifted.{TableQuery, Tag}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps


class AvailabilityDao extends DBConnection {

  //  lazy val db = getDbConnection(ConfigFactory.load)
  lazy val db = getDockerDbConnection

  implicit object ResultMap extends GetResult[Map[String, Any]] {
    def apply(pr: PositionedResult) = {
      val rs = pr.rs
      val md = rs.getMetaData();
      (1 to rs.getRow).flatMap { j => (1 to pr.numColumns).map { i => md.getColumnName(i) -> rs.getObject(i) } }.toMap
    }
  }

  def getSeqRuleAssignment(requestType: String, groupedAttribute: String) = {
    val sql =
      sql"""select * from sequential.seq_rule_assignment where request_type='#${requestType}' and
            grouped_attribute='#${groupedAttribute}' and is_active=true""".as[Map[String, Any]]
    Await.result(db.run(sql), 2.seconds).map(i => JsObject(i.map(j => ((j._1, if (j._2 != null) j._2.toString.toJson else "".toJson)))))
  }

  def getSeqRule(tableName: String, seqName: String) = {
    val sql =
      sql"""select * from sequential.#${tableName} where seq_name='#${seqName}'""".as[Map[String, Any]]
    val result = Await.result(db.run(sql), 2.seconds)
    result.map(i => JsObject(i.map(j => ((j._1, if (j._2 != null) j._2.toString.toJson else "".toJson)))))
  }

  def getEligibility(request: Request) = {
    val condition = request.records.map(i => s"${i.columnname} = '${i.columnvalue}'").mkString(" AND ")
    val sql = sql"""select * from sequential.#${request.seqName} where #${condition}""".as[Map[String, Any]]
    val result = Await.result(db.run(sql), 2.seconds)
    if(result.nonEmpty){
      OkResponse("Eligible")
    }
    else OkResponse("Not Eligible")
//    result.map(i => JsObject(i.map(j => ((j._1, if (j._2 != null) j._2.toString.toJson else "".toJson)))))
  }

  def getAvailability(request: Request) = {
    val condition = request.records.map(i => s"${i.columnname} = '${i.columnvalue}'").mkString(" AND ")
    val sql = sql"""select * from sequential.#${request.seqName} where #${condition}""".as[Map[String, Any]]
    val result = Await.result(db.run(sql), 2.seconds)
    if(result.nonEmpty){
      val conditionForStock = result.map(i => (i.map(j => s"${j._1} = '${j._2}'")).mkString(" AND ")).mkString(" OR ")
      val stockSql = sql"""select * from sequential.stock where #${conditionForStock}""".as[Map[String, Any]]
      val stockResult = Await.result(db.run(stockSql), 2.seconds)
      var quantity: Long = 0
      stockResult.map(i => i.map(j => if(j._1 == "quantity") quantity = quantity + j._2.toString.toLong))
      OkResponse(s"Available Stock = $quantity")
    }
    else OkResponse("Stock Not Available")
  }

  def orderRequest(orderRequest: OrderRequest) = {
    val ruleAssignment = getSeqRuleAssignment(orderRequest.requestType, orderRequest.groupedAttribute)
    println("*************************************" + ruleAssignment)
    if(ruleAssignment.isEmpty) throw new NoSuchElementException("No assigned rule for request")
    val eligibility = ruleAssignment.head.getFields("eligibility_seq_rule_name")
    val availability = ruleAssignment.head.getFields("availability_seq_rule_name")
    val substitution = ruleAssignment.head.getFields("substitution_seq_rule_name")

    val eligibilityRule = if(eligibility.nonEmpty && eligibility.head != "".toJson){
      val rule = getSeqRule("eligibility_seq_rules", eligibility.head.toString)
      if(rule.isEmpty) throw new NoSuchElementException("No Eligibility Seq Rule for the request")
      getEligibility(Request(rule.head.getFields("seq_name").head.toString, orderRequest.records))
    }
    else OkResponse("Eligibility not requested")

    val availabilityRule = if(availability.nonEmpty && availability.head != "".toJson){
      val rule = getSeqRule("availability_seq_rules", availability.head.toString.replace("\"", ""))
      if(rule.isEmpty) throw new NoSuchElementException("No Availability Seq Rule for the request")
      getAvailability(Request(rule.head.getFields("seq_name").head.toString, orderRequest.records))
    }
    else OkResponse("Availability not requested")

    val substitutionRule = if(substitution.nonEmpty && substitution.head != "".toJson){
      val rule = getSeqRule("substitution_seq_rules", substitution.head.toString)
    }

    availabilityRule
  }


  //  if(result.nonEmpty){
  //    val availabilitySeq = result.head.map(i => if(i._1 == "availability_seq_name" & i._2 != null) getAvailabilityRule(i._2.toString))
  //    val eligibilitySeq = result.head.map(i => if(i._1 == "eligibility_seq_name" & i._2 != null) getEligibilityRule(i._2.toString))
  //    val substitutionSeq = result.head.map(i => if(i._1 == "substitution_seq_name" & i._2 != null) getSubstitutionRule(i._2.toString))
  //    (availabilitySeq, eligibilitySeq, substitutionSeq)
  //  }
  //  else{
  //    throw new Exception("No rules found for grouped attribute" + groupedAttribute)
  //  }

//  def getAvailableStock(request: Availability): Seq[String] = {
//    val sql =
//      sql"""select * from seq_rule_assignment where request_type='#${request.requestType}' and
//            acroynm_name='#${request.acroynmName}' and is_active=true""".as[Map[String, Any]]
//    val result = Await.result(db.run(sql), 2.seconds)
//    println(result)
//    val rule = result.map(i => JsObject(i.map(j => ((j._1, if (j._2 != null) j._2.toString.toJson else "".toJson)))))
//    if (rule.nonEmpty) {
//      rule.map(i => {
//        val eligibilitySeq = i.getFields("eligibility_seq_name")
//          if (eligibilitySeq.nonEmpty) {
//          val sql =
//            sql"""select * from eligibility_seq_rules where
//                 seq_name='#${eligibilitySeq.head}'""".as[Map[String, Any]]
//            val result = Await.result(db.run(sql), 2.seconds).map(i => JsObject(i.map(j => ((j._1, if (j._2 != null)
//              j._2.toString.toJson else "".toJson)))))
//            if (result.nonEmpty) {
////              val eligibilityRule = i.getFields("level")
//              if(i.getFields("level").head == "At Level"){
//                val sql =
//                  sql"""select * from #${eligibilitySeq.head} where #${i.getFields("level").head}=                     """.as[Map[String, Any]]
//                val result = Await.result(db.run(sql), 2.seconds).map(i => JsObject(i.map(j => ((j._1, if (j._2 != null)
//                  j._2.toString.toJson else "".toJson)))))
//              }
//
//            }
//
//        }
//        else {
//        }
//        val availabilitySeq = if (i.getFields("availability_seq_name").nonEmpty) {
//
//        }
//        else {
//
//        }
//        val substitutionSeq = if (i.getFields("substitution_seq_name").nonEmpty) {
//
//        }
//        else {
//
//        }
//      })
//    }
//    else {
//      throw new Exception("No rules found for acroynm" + request.acroynmName)
//    }
//  }

}