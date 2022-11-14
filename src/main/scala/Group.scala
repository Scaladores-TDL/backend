final case class CrateGroupRequest(id: Long, name: String) {
  require(!name.isEmpty, "group name must not be empty")
}

final case class Group(id: Long, name: String, prodesIds: Seq[Long])
