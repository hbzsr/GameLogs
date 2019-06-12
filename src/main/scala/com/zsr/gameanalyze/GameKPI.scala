package com.zsr.gameanalyze


import com.zsr.gameanalyze.utils.{DateUtils, FilterUtils}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.spark._

object GameKPI {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("GameAnalyze")
      .setMaster("local[2]")
      .set("es.nodes", "bdnode1,bdnode2,bdnode3")
      .set("es.port", "9200")
      //自动创建index
      .set("es.index.auto.create", "")
    val sc = new SparkContext(conf)

    val queryTime = args(0)
    val startTime = DateUtils(queryTime)
    val endTime = DateUtils.updateCalendar(+1)
    val startTimeMorrow = DateUtils.updateCalendar(+1)
    val endTimeMorrow = DateUtils.updateCalendar(+2)

    //查询es的条件
    val query =
      s"""
         |{"query":{"match_all":{}}}
       """.stripMargin
    //获取es数据,返回的类型中
    // 第一个参数：String是对应"_id"字段值
    //第二个参数：Map表示的是 _id对应的数据
    val queryRDD: RDD[(String, collection.Map[String, AnyRef])] = sc.esRDD("gamelogs", query)
    println("——————————————————————————————" + queryRDD.count())
    //将_id过滤掉
    // map中的第一个参数：String是字段名称 ， 第二个参数AnyRef是字段值
    val filterRDD: RDD[collection.Map[String, AnyRef]] = queryRDD.map(_._2).filter(line => {
      val current_time = line.getOrElse("current_time", -1).toString
      current_time.substring(0, 1).equals("2")
    })
    println("-->>>>>>>>>-----" + filterRDD.count())
    //切分数据
    val spliteRDD: RDD[Array[String]] = filterRDD.map(line => {
      val et = line.getOrElse("event_type", "-1").toString
      //事件类型
      val time = line.getOrElse("current_time", "-1").toString
      val user = line.getOrElse("user", "").toString
      Array(et, time, user)
    })
    println("*******>>>>>******" + spliteRDD.count())
    // 很多指标的统计都需要发过滤，可以将过滤的逻辑封装为工具类，减少代码冗余
    // SimpleDateFormat是线程不安全的，可以加锁，也可以用另外一个线程安全的对象：FastDateFormat
    // 最好不要在算子内部去new一个对象，会占用很多内存资源
    //    splitedRDD.filter(x => {
    //      val time = x(1)
    //      val sdf = new SimpleDateFormat("yyyy年MM月dd日,E,HH:mm:ss")
    //      val time_long = sdf.parse(time).getTime
    //    })

    /**
      * 日新增玩家（DNU） 当日新增加的玩家帐户数。
      */
    //SimpleDateFormat是线程不安全的，可以加锁，也可以用另外一个线程安全的对象FastDateFormat.
    //在算子中不建议直接new一个对象。

    val DNU = spliteRDD.filter(fields => {
      FilterUtils.filterByTimeAndType(fields, EventType.REGISTER, (startTime, endTime))
    })

    /**
      * 日活跃：DAU
      */
    val DAU: Long = spliteRDD.filter(fields => {
      FilterUtils.filterByTime(fields, startTime, endTime) &&
        FilterUtils.filterByMultiEventType(fields, Array(EventType.REGISTER, EventType.LOGIN))
    }).map(_ (2)).distinct().count()

    /**
      * 次日留存
      */
    // 第一天的新增用户拿出来，需要和第二天的登录的用户进行intersction
    val dayNewUsers = DNU.map(_ (2))
    //第二天登陆的用户
    val day2Users = spliteRDD.filter(fields => {
      FilterUtils.filterByTimeAndType(fields, EventType.LOGIN, (startTimeMorrow, endTimeMorrow))
    }).map(_ (2)).distinct()
    val keep2Day = dayNewUsers.intersection(day2Users)

    println("新增用户" + DNU.count(), "日活跃用户：" + DAU, "次日留存 ： " + keep2Day.count())

    sc.stop

  }

}
