package imcamilo.com.github

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

/**
  * Created by Camilo Jorquera on 14-01-19
  */
object Server {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("imc-system")
    implicit val materializer = ActorMaterializer()

    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`application/json`, "Hi, this a simple get!"))
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8765)

    println("Server running at 8765")

  }

}
