import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, onComplete}
import org.mongodb.scala.model.Filters
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.{ExecutionContext, Future}
// the following is equivalent to `implicit val ec = ExecutionContext.global`
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class ProdeService(val prodesCollection: MongoCollection[Prode]) {

  def find: Future[Seq[Prode]] = {
    prodesCollection.find().toFuture()
  }

  def create(request: CreateProdeRequest) = {
    val prode = Prode(request.id, request.user, request.groupId, request.matches, 0)
    prodesCollection.insertOne(prode).toFuture()
  }

  def delete(prodeId: Long) = {
    prodesCollection.deleteOne(Filters.eq("id", prodeId)).toFuture()
  }

  def simulateGame(game: Game) = {
    val f = this.find
    f.map(prodes => {
      prodes.foreach(prode => {
        val prodeUpdated = prode.simulateGame(game)
        prodesCollection.replaceOne(Filters.eq("id", prodeUpdated.id), prodeUpdated).subscribe((updateResult: UpdateResult) => println(updateResult))
      })
    })
  }
}
