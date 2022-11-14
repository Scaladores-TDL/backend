import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, onComplete}
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.{Filters, Updates}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future}
import scala.util.{Failure, Success}

class GroupService(val groupCollection: MongoCollection[Group], val prodeCollection: MongoCollection[Prode]) {

  def getGroups: Future[Seq[Group]] = {
    groupCollection.find().toFuture()
  }

  def create(request: CrateGroupRequest) = {
    val group = Group(request.id, request.name, List())
    groupCollection.insertOne(group).toFuture()
  }

  def delete(groupId: Long) = {
    groupCollection.deleteOne(Filters.eq("id", groupId)).toFuture()
  }

  def getGroupById(id: Long): Unit = {
    val f = prodeCollection.find(Filters.eq("groupId", id)).toFuture()

    f.onComplete {
      case Success(prodes: Seq[Prode]) => {
        val sorted = prodes.sortWith(_.points > _.points)
        println(sorted)
        //Devolver el grupo con sus prodes ordenados
      }
    }
  }
}