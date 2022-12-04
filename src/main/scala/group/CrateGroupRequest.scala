package group

final case class CrateGroupRequest(_id: Long, name: String) {
  require(name.nonEmpty, "group name must not be empty")
}
