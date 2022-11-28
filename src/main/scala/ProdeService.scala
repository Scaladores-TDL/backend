import org.bson.conversions.Bson
import org.mongodb.scala.model.Filters
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.result.{DeleteResult, InsertOneResult, UpdateResult}

import scala.concurrent.{ExecutionContext, Future}
// the following is equivalent to `implicit val ec = ExecutionContext.global`
import ExecutionContext.Implicits.global

class ProdeService(val prodesCollection: MongoCollection[Prode]) {

  def initProdes = {
    val bufferedSource = io.Source.fromFile("src/main/resources/prode")
    val prodeData = bufferedSource.getLines().next().split(",").map(_.trim)

    val games: List[Game] = bufferedSource.getLines().map(line => {
      println(line)
      val cols = line.split(",").map(_.trim)
      Game(cols(0), cols(1), cols(2).toLong, cols(3).toLong)
    }).toList
    bufferedSource.close

    val request = CreateProdeRequest(prodeData(0).toLong, prodeData(1), prodeData(2).toLong, games)
    this.create(request)
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

  def create(request: CreateProdeRequest): Future[InsertOneResult] = {
    val prode = Prode(request.id, request.user, request.groupId, request.matches, 0, 0, 0)
    prodesCollection.insertOne(prode).toFuture()
  }

  def delete(prodeId: Long): Future[DeleteResult] = {
    prodesCollection.deleteOne(Filters.eq("_id", prodeId)).toFuture()
  }

  def simulateGame(game: Game) = {
    this.find().map(prodes => {
      prodes.foreach(prode => {
        val prodeUpdated = prode.simulateGame(game)
        prodesCollection.replaceOne(Filters.eq("_id", prodeUpdated._id), prodeUpdated).subscribe((updateResult: UpdateResult) => println(updateResult))
      })
    })
  }
}
