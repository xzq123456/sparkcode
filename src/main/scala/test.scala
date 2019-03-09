import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.ml.clustering.{GaussianMixture,GaussianMixtureModel}
import org.apache.spark.ml.linalg.Vectors

object test {
  def main(args: Array[String]): Unit = {
        val conf = new SparkConf().setAppName("Wordcount").setMaster("local[2]")
        val sc=new SparkContext(conf)

  }
}
