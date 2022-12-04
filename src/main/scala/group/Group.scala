package group

import prode.Prode

final case class Group(_id: Long, name: String, prodes: Seq[Prode]) {
}
