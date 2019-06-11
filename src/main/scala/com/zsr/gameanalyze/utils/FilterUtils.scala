package com.zsr.gameanalyze.utils

import org.apache.commons.lang3.time.FastDateFormat

object FilterUtils {
  private val fdf = FastDateFormat.getInstance("yyyy年MM月dd日,E,HH:mm:ss")

  //以时间作为过滤条件
  def filterByTime(list: Array[String], startTime: Long, endTime: Long): Boolean = {
    val time_long = fdf.parse(list(1)).getTime
    time_long >= startTime && time_long < endTime
  }

  //以事件作为过滤条件
  def filterByEventType(list: Array[String], eventType: String): Boolean = {
    eventType.equals(list(0))
  }

  //以时间和类型作为过滤条件
  def filterByTimeAndType(fields: Array[String], eventType: String,
                          time_rang: (Long, Long)): Boolean = {
    val time_long = fdf.parse(fields(1)).getTime
    time_long >= time_rang._1 && time_long < time_rang._2 && fields(0).equals(eventType)
  }

  //以多事件作为过滤条件
  def filterByMultiEventType(fields: Array[String], eventTypes: Array[String]): Boolean = {
    val et = fields(0)
    for (eventType <- eventTypes) {
      if (eventType.equals(et)) {
        return true
      }
    }
    false
  }
}
