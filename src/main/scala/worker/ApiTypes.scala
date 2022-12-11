package worker

object ApiTypes {
  case class ApiMatch(homeTeam: String, awayTeam: String, homeScore: Int, awayScore: Int)
  case class ApiResponse(status: String, data: List[ApiMatch])
}
