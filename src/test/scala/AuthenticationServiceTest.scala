import akka.util.Timeout
import ololo.{AuthenticationService, Token}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._


class AuthenticationServiceTest extends FlatSpec with Matchers {
  implicit val timeout = Timeout(5 seconds)

  import scala.concurrent.ExecutionContext.Implicits.global

  val as = new AuthenticationService
  var currentToken: Token = null

  "Authentication Service" should "generate new token" in {
    currentToken = Await.result(as.getToken, 5 seconds)
    currentToken should not be null
  }

  it should "not generate new token again" in {
    currentToken = Await.result(as.getToken, 5 seconds)
    currentToken should not be null
    for (token <- as.getToken()) {
      currentToken shouldBe token
    }
  }

  it should "expire token" in {
    val oldToken = currentToken
    as.tokenExpired(currentToken)
    currentToken = Await.result(as.getToken, 5 seconds)
    currentToken should not be oldToken
  }

  it should "throw exection if token is invalid" in {
    a[Exception] should be thrownBy {
      as.tokenExpired("ololo")
    }
  }

}
