package prode

import games.{CompleteGame, Game, GroupStage, Statistics}
import org.bson.conversions.Bson
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters
import org.mongodb.scala.result.{DeleteResult, UpdateResult}

import scala.concurrent.Future
// the following is equivalent to `implicit val ec = ExecutionContext.global`
import scala.concurrent.ExecutionContext.Implicits.global

class ProdeService(val prodesCollection: MongoCollection[Prode]) {

  def initProdes = {
    val bufferedSource = io.Source.fromFile("src/main/resources/prodeTest")
    val prodeData = bufferedSource.getLines().next().split(",").map(_.trim)

    val games: List[GroupStage] = bufferedSource.getLines().map(line => {
      println(line)
      val cols = line.split(",").map(_.trim)
      GroupStage(cols(0), cols(1), cols(2).toLong, cols(3).toLong)
    }).toList
    bufferedSource.close

    //val request = prodeTest.CreateProdeRequest(prodeData(0).toLong, prodeData(1), prodeData(2).toLong, games, List(),)
    //this.create(request)
    Future {}
  }

  def find(filters: Bson = Filters.empty()): Future[Seq[Prode]] = {
    prodesCollection.find(filters).toFuture()
  }

  def findProdeById(prodeId: Long): Future[Option[Prode]] = {
    val filters = Filters.eq("_id", prodeId)
    this.find(filters).map {
      case prode :: Nil => Some(prode)
      case _ =>  None
    }
  }

  def findProdesByGroupId(groupId: Long): Future[Seq[Prode]] = {
    val filters = Filters.eq("groupId", groupId)
    this.find(filters)
  }

  def create(request: CreateProdeRequest) = {

    //See if this user already create a prodeTest in this group
    val filters = Filters.and(Filters.eq("user", request.user), Filters.eq("groupId", request.groupId))
    find(filters).flatMap(prodes => {
      if (prodes.nonEmpty) {
        Future.failed(new Error)
      } else {
        val prode = Prode(request._id, request.user, request.groupId, request.matches, request.octaveFinal, request.finalGame, Statistics(0,0,0))
        prodesCollection.insertOne(prode).toFuture()
      }
    })
  }

  def delete(prodeId: Long): Future[DeleteResult] = {
    prodesCollection.deleteOne(Filters.eq("_id", prodeId)).toFuture()
  }

  def simulate(game: Game) = {
    this.find().map(prodes => {
      prodes.foreach(prode => {
        val prodeUpdated = prode.simulate(game)
        prodesCollection.replaceOne(Filters.eq("_id", prodeUpdated._id), prodeUpdated).subscribe((updateResult: UpdateResult) => println(updateResult))
      })
    })
  }

  def simulateFinal(simulation: CompleteGame) = {
    this.find().map(prodes => {
      prodes.foreach(prode => {
        val prodeUpdated = prode.simulateFinal(simulation)
        prodesCollection.replaceOne(Filters.eq("_id", prodeUpdated._id), prodeUpdated).subscribe((updateResult: UpdateResult) => println(updateResult))
      })
    })
  }
}
