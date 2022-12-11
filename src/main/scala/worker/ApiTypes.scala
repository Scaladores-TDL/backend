package worker

object ApiTypes {
  case class ApiMatch(homeTeam: String, awayTeam: String, homeScore: Int, awayScore: Int, homeScorers: List[String],
                      awayScorers: List[String], matchType: String, state: String)
  case class ApiResponse(status: String, data: List[ApiMatch])
}
