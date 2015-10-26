package ololo

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.Config


/**
 * Created by ko3a4ok on 10/26/15.
 */
trait MainService extends JsonSupport {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer
  def config: Config

  val authorizeError = ErrorResponse("not authorized")
  val tokenService = AuthenticationService
  lazy val user = User(config.getString("auth.user"), config.getString("auth.pass"))
  lazy val loginActor = system.actorOf(Props(new LoginActor(user)))


  def authToken(request: RequestContext): Boolean = {
    val query = request.request.uri.query
    val username = query.get("user")
    val password = query.get("pass")
    if (username.isEmpty || password.isEmpty) return false
    implicit val timeout = Timeout(5 seconds)
    val response = loginActor ? User(username.get, password.get)
    val res = Await.result(response, timeout.duration).asInstanceOf[Boolean]
    res
  }

  implicit val timeout = Timeout(5 seconds)
  val routes =
    (path("token") & post) {
      authorize(authToken(_)) {
        onComplete[Token](tokenService.getToken()) {
          case x => complete(OK -> TokenResponse(x.get))
        } ~ {
            reject
        }
      } ~ {
        complete(Unauthorized -> authorizeError)
      }
    } ~
      (path("token" / "refresh") & post) {
        parameters('token ?) { token =>
          try {
            tokenService.tokenExpired(token.get)
            onComplete(tokenService.getToken()) {
              case x => complete(OK -> TokenResponse(x.get))
            }
          }catch {
            case ex: Exception => complete(Unauthorized -> authorizeError)
          }
        }
    } ~
      (path("datetime") & get) {
        headerValueByName("Authorization") {token => {
          try {
            val datetimeService = new DatetimeService(token)
            complete(datetimeService.getDatetime)
          } catch {
            case ex : Exception => complete(Unauthorized -> ErrorResponse(ex.getMessage))
          }
        }
      }
     }
}
