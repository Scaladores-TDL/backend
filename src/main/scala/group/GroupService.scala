package group

import org.bson.conversions.Bson
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import org.mongodb.scala.model.Filters
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}
import prode.ProdeService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class GroupService(val database: MongoDatabase) {
  val groupCollection: MongoCollection[Group] = database.getCollection("groups")

  def findGroups(filters: Bson = Filters.empty()): Future[Seq[Group]] = {
    groupCollection.find(filters).toFuture()
  }

  def create(request: CrateGroupRequest): Future[InsertOneResult] = {
    val group = Group(request._id, request.name)
    groupCollection.insertOne(group).toFuture()
  }

  def delete(groupId: Long): Future[DeleteResult] = {
    groupCollection.deleteOne(Filters.eq("_id", groupId)).toFuture()
  }

  def findGroupById(id: Long): Future[Option[Group]] = {
    val filters = Filters.eq("_id", id)
    this.findGroups(filters).map {
      case group :: Nil => Some(group)
      case _ =>  None
    }
  }
}