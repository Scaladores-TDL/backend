package worker

object ApiTypes {
  case class ApiMatch(homeTeam: String, awayTeam: String, homeScore: Int, awayScore: Int, homeScorers: List[String],
                      awayScorers: List[String], matchType: String, state: String) {
    def title = s"$homeTeam vs $awayTeam"
  }

  case class ApiResponse(status: String, data: List[ApiMatch])
}
