package org.apache.spark.example.streaming
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import org.apache.spark.streaming.flume._
import org.apache.spark.util.IntParam


object FlumeEventCount {
  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println(
        "Usage: FlumeEventCount <host> <port>")
      System.exit(1)
    }
    StreamingExamples.setStreamingLogLevels()
    val Array(host, IntParam(port)) = args
    val batchInterval = Milliseconds(2000)
    // Create the context and set the batch size
    val sparkConf = new SparkConf().setAppName("FlumeEventCount").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, batchInterval)
    // Create a flume stream
    val stream = FlumeUtils.createStream(ssc, host, port, StorageLevel.MEMORY_ONLY_SER_2)
    // Print out the count of events received from this server in each batch
    stream.count().map(cnt => "Received " + cnt + " flume events." ).print()
    ssc.start()
    ssc.awaitTermination()
  }
}