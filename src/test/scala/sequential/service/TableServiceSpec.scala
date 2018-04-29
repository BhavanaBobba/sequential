package sequential.service

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, RequestEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import sequential.data._
import org.scalatest.{WordSpecLike, Matchers => RouteMatchers}
import org.scalatest.concurrent.ScalaFutures
import spray.json._
import DefaultJsonProtocol._
import akka.http.scaladsl.marshalling.Marshal
import sequential.dao.TableDao

import scala.concurrent.duration._
import scala.concurrent.Await


class TableServiceSpec extends TableService with WordSpecLike
  with RouteMatchers with ScalatestRouteTest with ScalaFutures with SprayJsonSupport {
  override implicit val executor = system.dispatcher
  implicit val log = Logging(system, "ReferenceDataServiceSpec")
  val tableDao = new TableDao

//  "The reference data HTTP service" should {
//
//    "return a 200 OK response for a list of details" in {
//      val expectedOutput = Seq(User(Some(1), "RCB", "Randomized in RCB style", "rcb"),
//        User(Some(2), "CRD", "Randomized in CRD style", "crd"))
//      Get(s"/details") ~> Route.seal(referenceDataRoutes) ~> check {
//        status shouldBe StatusCodes.OK
//        contentType shouldBe ContentTypes.`application/json`
//      }
//    }
//
//    "return a 200 OK response for a post of details" in {
//      val input = User(None, "RCB", "Randomized in RCB style", "rcb")
//      val expectedOutput = User(Some(1), "RCB", "Randomized in RCB style", "rcb")
//      val entity = Await.result(Marshal(input.toJson).to[RequestEntity], 1.second)
//      Post(s"/details", entity) ~> Route.seal(referenceDataRoutes) ~> check {
//        status shouldBe StatusCodes.OK
//        contentType shouldBe ContentTypes.`application/json`
//      }
//    }
//
//    "return a 200 OK response for a put of details" in {
//      val strategyToUpdate = User(Some(1), "RCB", "Randomized in RCB style", "rcb")
//      val entity = Await.result(Marshal(strategyToUpdate.toJson).to[RequestEntity], 1.second)
//      Put(s"/details", entity) ~> Route.seal(referenceDataRoutes) ~> check {
//        status shouldBe StatusCodes.OK
//        contentType shouldBe ContentTypes.`application/json`
//      }
//    }
//  }

}
