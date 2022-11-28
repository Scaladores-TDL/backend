import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.{Filters}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


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

  def getGroupById(id: Long): Future[Seq[Prode]] = {
    val f = prodeCollection.find(Filters.eq("groupId", id)).toFuture()

    f.map(prodes => {
      val sorted = prodes.sortWith((p1, p2) => p1.compare(p2))
      sorted
    })
  }
}