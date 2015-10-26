package ololo

import scala.concurrent.Future

/**
 * Created by ko3a4ok on 10/26/15.
 */
trait Authentication {
  def getToken():Future[Token] // or throw exception if not authorized
  def tokenExpired(oldToken:Token):Unit // inform the service that the token has expired
}
