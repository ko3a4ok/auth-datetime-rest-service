package ololo

import java.text.SimpleDateFormat

/**
 * Created by ko3a4ok on 10/26/15.
 */
class DatetimeService(val token: Token) extends AuthenticatedService {

  def getDatetime: DatetimeResponse = {
    DatetimeResponse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis))
  }
}
