import java.text.SimpleDateFormat

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpHeader, HttpMethods}
import akka.http.scaladsl.server.{MethodRejection, MissingHeaderRejection}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ololo._
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by ko3a4ok on 10/27/15.
 */
class ServiceTest extends FlatSpec with Matchers with ScalatestRouteTest with MainService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig

  "Auth Service" should "authenticate a correct user" in {
    Post(s"/token?user=%s&pass=%s".format(config.getString("auth.user"), config.getString("auth.pass"))) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[TokenResponse].token.length should be > 0
    }
  }

  it should " not authenticate a wrong user" in {
    Post(s"/token?user=%s&pass=%s".format("wrong", "wrong")) ~> routes ~> check {
      status shouldBe Unauthorized
      contentType shouldBe `application/json`
      responseAs[ErrorResponse] shouldBe authorizeError
    }
  }

  it should " have only http post method" in {
    Post(s"/token") ~> routes ~> check {
      status shouldBe Unauthorized
    }
    Get(s"/token") ~> routes ~> check {
      rejection shouldBe MethodRejection(HttpMethods.POST)
    }
    Put(s"/token") ~> routes ~> check {
      rejection shouldBe MethodRejection(HttpMethods.POST)
    }
    Delete(s"/token") ~> routes ~> check {
      rejection shouldBe MethodRejection(HttpMethods.POST)
    }
  }

  it should " refresh token" in {
    Post(s"/token?user=%s&pass=%s".format(config.getString("auth.user"), config.getString("auth.pass"))) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      val token = responseAs[TokenResponse].token
      Post(s"/token/refresh?token=$token") ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        val newToken = responseAs[TokenResponse].token
        newToken should not be token
      }
      Post(s"/token/refresh?token=$token") ~> routes ~> check {
        status shouldBe Unauthorized
        contentType shouldBe `application/json`
        responseAs[ErrorResponse] shouldBe authorizeError
      }
    }

  }

  "Datetime service" should " support authorization" in {
    Get("/datetime") ~> routes ~> check {
      rejection shouldBe MissingHeaderRejection("Authorization")
    }
    Post(s"/datetime") ~> routes ~> check {
      rejection shouldBe MethodRejection(HttpMethods.GET)
    }
    Put(s"/datetime") ~> routes ~> check {
      rejection shouldBe MethodRejection(HttpMethods.GET)
    }
    Delete(s"/datetime") ~> routes ~> check {
      rejection shouldBe MethodRejection(HttpMethods.GET)
    }

    Post(s"/token?user=%s&pass=%s".format(config.getString("auth.user"), config.getString("auth.pass"))) ~> routes ~> check {
      val token = responseAs[TokenResponse].token
      val tokenHeader = HttpHeader.parse("Authorization", token).asInstanceOf[Ok].header
      Get("/datetime").addHeader(tokenHeader) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[DatetimeResponse].datetime.length should be > 0
      }
      var newTokenHeader: HttpHeader = null
      Post(s"/token/refresh?token=$token") ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        val newToken = responseAs[TokenResponse].token
        newToken should not be token
        newTokenHeader = HttpHeader.parse("Authorization", newToken).asInstanceOf[Ok].header
      }

      Get("/datetime").addHeader(tokenHeader) ~> routes ~> check {
        status shouldBe Unauthorized
        contentType shouldBe `application/json`
        responseAs[ErrorResponse].error shouldBe "token expired"
      }
      val fakeHeader = HttpHeader.parse("Authorization", "ololo").asInstanceOf[Ok].header
      Get("/datetime").addHeader(fakeHeader) ~> routes ~> check {
        status shouldBe Unauthorized
        contentType shouldBe `application/json`
        responseAs[ErrorResponse] shouldBe authorizeError
      }
    }
  }

  it should "return current date and time" in {
    Post(s"/token?user=%s&pass=%s".format(config.getString("auth.user"), config.getString("auth.pass"))) ~> routes ~> check {
      val token = responseAs[TokenResponse].token
      val tokenHeader = HttpHeader.parse("Authorization", token).asInstanceOf[Ok].header
      Get("/datetime").addHeader(tokenHeader) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        var raw = responseAs[DatetimeResponse].datetime
        raw.length should be > 0
        val date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(raw)
        val diff: Int = (System.currentTimeMillis() - date.getTime).toInt
        diff should equal(1000 +- 1000) // [0; 2] range of seconds
      }
    }

  }




}