package games

case class GroupStage(team1: String, team2: String, result1: Long, result2: Long) extends Game(team1, team2, result1, result2) {
}
