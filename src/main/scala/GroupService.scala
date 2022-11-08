import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters

import scala.concurrent.Future

class GroupService(val groupCollection: MongoCollection[Group]) {

  def find: Future[Seq[Group]] = {
    groupCollection.find().toFuture()
  }

  def create(request: Group) = {
    groupCollection.insertOne(request).toFuture()
  }

  def delete(groupId: Long) = {
    groupCollection.deleteOne(Filters.eq("id", groupId)).toFuture()
  }
}