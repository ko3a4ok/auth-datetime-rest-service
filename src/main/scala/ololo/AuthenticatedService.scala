package ololo

trait AuthenticatedService {
  implicit val token: Token
  implicit val validator: Validator
  validator.validate(token)
}
