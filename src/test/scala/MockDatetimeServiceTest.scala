
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ololo._
import org.scalatest.{FlatSpec, Matchers}


class MockDatetimeServiceTest extends FlatSpec with Matchers with ScalatestRouteTest with MainService {
  val username = config.getString("auth.user")
  val password = config.getString("auth.pass")
  val staticToken: Token = "token"
  private var datetime = DatetimeResponse("trololo time")

  override def testConfigSource = "akka.loglevel = WARNING"

  override def config = testConfig

  override def getDatetimeService(token: String): Datetime = new Datetime {
    override def getDatetime: DatetimeResponse = datetime
  }

  "Datetime service" should " do authentication for Datetime service" in {
    Post(s"/token?user=%s&pass=%s".format(username, password)) ~> routes ~> check {
      val token = responseAs[TokenResponse].token
      val tokenHeader = HttpHeader.parse("Authorization", token).asInstanceOf[Ok].header
      Get("/datetime").addHeader(tokenHeader) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[DatetimeResponse] shouldBe datetime
      }
      datetime = DatetimeResponse("ololo time")
      Get("/datetime").addHeader(tokenHeader) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[DatetimeResponse] shouldBe datetime
      }
    }

  }

}