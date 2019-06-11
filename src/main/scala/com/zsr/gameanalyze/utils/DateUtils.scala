package com.zsr.gameanalyze.utils

import java.util.Calendar
import org.apache.commons.lang3.time.FastDateFormat
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._

object DateUtils {
  def isDate(msg: String): Boolean = {
    import java.text.SimpleDateFormat
    //    DateTime.now + a.days
    import org.joda.time.DateTime
    val dateStr = "2018-06-01"
    val pattern = "yyyy-MM-dd"
    val date = new SimpleDateFormat(pattern).parse(dateStr)
    val dateTime = new DateTime(date)
    println(date)
    println(dateTime)
    true
  }

  private val fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
  private val calendar = Calendar.getInstance()

  // 将String类型的日期转换为Long类型
  def apply (time: String): Long = {
    calendar.setTime(fdf.parse(time))
    calendar.getTimeInMillis
  }

  // 改变日期的方法
  def updateCalendar(a: Int): Long = {
    calendar.add(Calendar.DATE, a)
    val time = calendar.getTimeInMillis
    calendar.add(Calendar.DATE, -a)
    time
  }
}
