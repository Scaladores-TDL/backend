import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import org.mongodb.scala.{MongoClient, MongoCollection}
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.model.Filters
import org.mongodb.scala.result.DeleteResult

import scala.io.StdIn
import scala.util.{Failure, Success}


object Main {
  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  // domain model
  final case class Match(team1: String, team2: String, result: String)
  final case class Prode(id: Long, user: String, matches: List[Match])

  // formats for unmarshalling and marshalling
  implicit val matchFormat = jsonFormat3(Match)
  implicit val prodeFormat = jsonFormat3(Prode)


  def main(args: Array[String]): Unit = {

    val uri: String = "mongodb://localhost:27017"
    val client: MongoClient = MongoClient(uri)
    val codecRegistry = fromRegistries(fromProviders(classOf[Prode], classOf[Match]), MongoClient.DEFAULT_CODEC_REGISTRY)

    val database = client.getDatabase("prodes").withCodecRegistry(codecRegistry)
    val prodesCollection: MongoCollection[Prode] = database.getCollection("prodes")

    // insert a document
    val prode: Prode = Prode(1,"Julian", List(Match("ARG", "BRA", "1")))


    val route = cors() {
      concat(
        get {
          pathPrefix("prode") {
            val f = prodesCollection.find().toFuture()
            onComplete(f) {
              case Success(prodes) => complete(prodes)
              case Failure(e) =>  complete(StatusCodes.InternalServerError)
            }
          }
        },
        get {
          pathPrefix("prode" / LongNumber) {
            prodeId => {
              println(prodeId)
              val f = prodesCollection.find(Filters.eq("id", prodeId)).toFuture()
              onComplete(f) {
                case Success(prode) => complete(prode)
                case Failure(e) =>  complete(StatusCodes.InternalServerError)
              }
            }
          }
        },
        post {
          pathPrefix("prode") {
            entity(as[Prode]) {
              prode => {
                val f = prodesCollection.insertOne(prode).toFuture()
                onComplete(f) {
                  case Success(value) => complete("prode created succesfully")
                  case Failure(e) =>  complete(StatusCodes.InternalServerError)
                }
              }
            }
          }
        },
        delete {
          pathPrefix("prode" / LongNumber) {
            prodeId => {
              val f = prodesCollection.deleteOne(Filters.eq("id", prodeId)).toFuture()
              onComplete(f) {
                case Success(result: DeleteResult) => {
                  println(result)
                  if (result.getDeletedCount == 0) {
                    complete(StatusCodes.NotFound)
                  } else {
                    complete("delete succesfully")

                  }
                }
                case Failure(e) =>  complete(StatusCodes.InternalServerError)
              }
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
