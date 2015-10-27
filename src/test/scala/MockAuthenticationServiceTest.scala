import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ololo._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class MockAuthenticationServiceTest extends FlatSpec with Matchers with ScalatestRouteTest with MainService {
  override lazy val tokenService = new AuthenticationService {
    override def tokenExpired(oldToken: Token): Unit = {
      if (oldToken != staticToken) throw new Exception
    }

    override def getToken(): Future[Token] = Future {
      staticToken
    }(ExecutionContext.global)

    override def validate(token: Token): Boolean = {
      if (token != staticToken) throw new Exception(authorizeError.error)
      true
    }
  }
  val username = config.getString("auth.user")
  val password = config.getString("auth.pass")
  val staticToken: Token = "token"

  override def testConfigSource = "akka.loglevel = WARNING"

  override def config = testConfig

  "Authentication service" should " refresh a token" in {
    Post(s"/token/refresh?token=$staticToken") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      val newToken = responseAs[TokenResponse].token
      newToken shouldBe staticToken
    }
    val wrongToken = staticToken.reverse
    Post(s"/token/refresh?token=$wrongToken") ~> routes ~> check {
      status shouldBe Unauthorized
      contentType shouldBe `application/json`
      responseAs[ErrorResponse] shouldBe authorizeError
    }
  }

  it should " do authentication for Datetime service" in {
    val header = HttpHeader.parse("Authorization", staticToken).asInstanceOf[Ok].header
    Get("/datetime").addHeader(header) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
    }
    val fakeHeader = HttpHeader.parse("Authorization", "ololo").asInstanceOf[Ok].header
    Get("/datetime").addHeader(fakeHeader) ~> routes ~> check {
      status shouldBe Unauthorized
      contentType shouldBe `application/json`
    }
  }

}