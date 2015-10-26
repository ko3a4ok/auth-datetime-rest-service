package ololo

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * Created by ko3a4ok on 10/26/15.
 */

trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val errorFormats = jsonFormat1(ErrorResponse)
  implicit val tokenFormats = jsonFormat1(TokenResponse)
  implicit val datetimeFormats = jsonFormat1(DatetimeResponse)
}