package ololo

/**
 * Created by ko3a4ok on 10/26/15.
 */
trait AuthenticatedService {
  implicit val token: Token
  AuthenticationService.validate(token)
}
