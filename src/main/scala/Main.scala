import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.concurrent.Future
import scala.io.StdIn


object Main {
  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  //fake databse
  var prodes: List[Prode] = List(Prode(1,"Julian"))

  // domain model
  final case class Prode(id: Long, user: String)

  // formats for unmarshalling and marshalling
  implicit val prodeFormat = jsonFormat2(Prode)

  // (fake) async database query api
  def findAll(): Future[List[Prode]] = Future {
    prodes
  }
  def findProde(prodeId: Long): Future[Option[Prode]] = Future {
    prodes.find(p => p.id == prodeId)
  }
  def createProde(prode: Prode): Future[Done] = {
    prodes = prodes :+ prode
    Future { Done }
  }
  def deleteProde(prodeId: Long): Future[Done] = Future {
    prodes = prodes.filter(p => p.id != prodeId)
    Done
  }

  def main(args: Array[String]): Unit = {

    val uri: String = "mongodb+srv://username:password@cluster0.eyzrrjg.mongodb.net/?retryWrites=true&w=majority"
    System.setProperty("org.mongodb.async.type", "netty")
    //val client: MongoClient = MongoClient(uri)

    val route = cors() {
      concat(
        get {
          pathPrefix("prode") {
            //Return all prodes
            val prodes = findAll()
            complete(prodes)
          }
        },
        get {
          pathPrefix("prode" / LongNumber) {
            prodeId => {
              println(prodeId)
              val prode: Future[Option[Prode]] = findProde(prodeId)

              onSuccess(prode) {
                case Some(prode) => complete(prode)
                case None => complete(StatusCodes.NotFound)
              }
            }
          }
        },
        post {
          pathPrefix("prode") {
            entity(as[Prode]) {
              prode => {
                val saved: Future[Done] = createProde(prode)
                onSuccess(saved) { _ => // we are not interested in the result value `Done` but only in the fact that it was successful
                  complete("prode created")
                }
              }
            }
          }
        },
        delete {
          pathPrefix("prode" / LongNumber) {
            prodeId => {
              complete(deleteProde(prodeId))
            }
          }
        }
      )
    }
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
