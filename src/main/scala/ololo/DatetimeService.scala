package ololo

import java.text.SimpleDateFormat

class DatetimeService(val token: Token, val validator: Validator) extends AuthenticatedService with Datetime {

  def getDatetime: DatetimeResponse = {
    DatetimeResponse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis))
  }
}
