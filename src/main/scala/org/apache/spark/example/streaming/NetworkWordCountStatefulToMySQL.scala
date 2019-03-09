package org.apache.spark.example.streaming
import java.sql.{PreparedStatement, Connection, DriverManager}
import java.util.concurrent.atomic.AtomicInteger
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.storage.StorageLevel
object NetworkWordCountStatefulToMySQL {
  def main(args: Array[String]): Unit = {
    //定义状态更新函数
    val updateFunc = (values: Seq[Int], state: Option[Int]) => {
      val currentCount = values.foldLeft(0)(_ + _)
      val previousCount = state.getOrElse(0)
      Some(currentCount + previousCount)
    }
    StreamingExamples.setStreamingLogLevels()  //设置log4j日志级别
    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCountStateful")
    val sc = new StreamingContext(conf, Seconds(5))
    sc.checkpoint("file:///usr/local/spark/mycode/streaming/stateful/")    //设置检查点，检查点具有容错机制
    val lines = sc.socketTextStream("localhost", 9999)
    val words = lines.flatMap(_.split(" "))
    val wordDstream = words.map(x => (x, 1))
    val stateDstream = wordDstream.updateStateByKey[Int](updateFunc)
    stateDstream.print()
    //下面是新增的语句，把DStream保存到MySQL数据库中
    stateDstream.foreachRDD(rdd => {
      //内部函数
      def func(records: Iterator[(String,Int)]) {
        var conn: Connection = null
        var stmt: PreparedStatement = null

        try {
        val user = "root"
        val url = "jdbc:mysql://localhost:3306/spark" //地址+数据库
        val password = "123456"

          conn = DriverManager.getConnection(url, user, password)
          records.foreach(p => {
            val sql = "insert into woedcount(word,count) values (?,?)"
            stmt = conn.prepareStatement(sql)
            stmt.setString(1, p._1.trim)
            stmt.setInt(2,p._2.toInt)
            stmt.executeUpdate()
          })
        } catch {
          case e: Exception => e.printStackTrace()
        } finally {
          if (stmt != null) {
            stmt.close()
          }
          if (conn != null) {
            conn.close()
          }
        }
      }

      val repartitionedRDD = rdd.repartition(1)
      repartitionedRDD.foreachPartition(func)
    })




    sc.start()
    sc.awaitTermination()
  }
}
