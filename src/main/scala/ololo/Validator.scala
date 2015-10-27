package ololo

trait Validator {
  def validate(token: Token): Boolean
}
