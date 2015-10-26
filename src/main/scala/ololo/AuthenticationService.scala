package ololo

import scala.concurrent.Future
import scala.util.Random

/**
 * Created by ko3a4ok on 10/26/15.
 */
object AuthenticationService extends Authentication {

  private val TOKEN_LENGTH = 32

  @volatile
  private var currentToken: Token = _
  private var expiredTokens = new collection.mutable.HashSet[Token]()

  private def generateToken: Token = {
    Random.alphanumeric.take(32).toArray.mkString
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  override def getToken(): Future[Token] = Future[Token] {
      if (currentToken == null) currentToken = generateToken
      currentToken
    }


  override def tokenExpired(oldToken: Token): Unit = {
    if (currentToken != oldToken) throw new Exception
    expiredTokens += oldToken
    currentToken = generateToken
  }

  def validate(token: Token): Boolean = {
    if (token != this.currentToken) {
      throw new Exception(if (expiredTokens.contains(token)) "token expired" else "not authorized")
    }
    true
  }
}
