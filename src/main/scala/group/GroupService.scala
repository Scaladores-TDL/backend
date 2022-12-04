package group

import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters
import prode.{ProdeService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class GroupService(val groupCollection: MongoCollection[Group], val prodeService: ProdeService) {

  def getGroups: Future[Seq[Group]] = {
    groupCollection.find().toFuture()
  }

  def create(request: CrateGroupRequest) = {
    val group = Group(request._id, request.name, List())
    groupCollection.insertOne(group).toFuture()
  }

  def delete(groupId: Long) = {
    groupCollection.deleteOne(Filters.eq("_id", groupId)).toFuture()
  }

  def getGroupById(id: Long): Future[Group] = {
    val filters = Filters.eq("_id", id)

    this.groupCollection.find(filters).toFuture()
      .map(groups => groups.head)
      .flatMap(group => {
        prodeService.findProdesByGroupId(group._id).map(prodes => {
          group.copy(prodes = prodes)
        })
      })
  }
}