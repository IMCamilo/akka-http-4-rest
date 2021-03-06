package imcamilo.com.github

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future

/**
  * Created by Camilo Jorquera on 14-01-19
  */
object Server {

    //needed to run a route
    implicit val system = ActorSystem("imcm-system")
    implicit val materializer = ActorMaterializer()

    // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
    implicit val executionContext = system.dispatcher

    var orders: List[Item] = Nil

    //domain model
    final case class Item(name: String, id: Long)
    final case class Order(items: List[Item])

    // formats for unmarshalling and marshalling
    implicit val itemFormat = jsonFormat2(Item)
    implicit val orderFormat = jsonFormat1(Order)

    // (fake) async database query api
    def fetchItem(itemId: Long): Future[Option[Item]] = Future {
      orders.find(o => o.id == itemId)
    }
    def saveOrder(order: Order): Future[Done] = {
      orders = order match {
        case Order(items) => items ::: orders
        case _            => orders
      }
      Future { Done }
    }


  def main(args: Array[String]): Unit = {

    val route: Route =
      get {
        pathPrefix("item"/ LongNumber) { id =>
          // there might be no item for a given id
          val maybeItem: Future[Option[Item]] = fetchItem(id)
          onSuccess(maybeItem) {
            case Some(item) => complete(item)
            case None       => complete(StatusCodes.NotFound)
          }
        }
      } ~
      post {
        path("orders") {
          entity(as[Order]) { order =>
            val saved: Future[Done] = saveOrder(order)
            onComplete(saved) { done =>
              complete("Order Created")
            }
          }
        }
      }


    val bindingFuture = Http().bindAndHandle(route, "localhost", 8765)

    println("Server running at 8765")

  }

}
