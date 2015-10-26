package object ololo {
  /**
   * Created by ko3a4ok on 10/26/15.
   */

  type Token = String
  case class ErrorResponse(error: String)
  case class TokenResponse(token: String)
  case class DatetimeResponse(datetime: String)
  case class User(username: String, password: String)
}
