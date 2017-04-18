package classification.knn

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.broadcast.Broadcast
import org.apache.hadoop.fs.Path
import org.apache.hadoop.fs.FSDataInputStream
import org.apache.hadoop.conf.Configuration

import java.util.logging.Level
import java.util.logging.Logger
import java.util.Random
import java.util.StringTokenizer
import java.io._
import java.util.Scanner

import scala.collection.mutable.MutableList
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer

import classification.abstr.TraitClassifier
import instanceSelection.lshis.ANDsTable
import utils.partitioner.RandomPartitioner
import classification.seq.knn.KNNSequential

/**
 * Approximate kNN classifier by using the LSH.
 * The classifier uses the locality sensitive hashing technique for finding out
 * approximate nearest neighbours in a fast way.
 *
 * @author Álvar Arnaiz-González
 * @version 2.0
 */
class LSHKNN extends TraitClassifier {

  /**
   * Path for logger's messages.
   */
  private val bundleName = "resources.loggerStrings.stringsLSHKNN";

  /**
   * Classifier's logger.
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  /**
   * Number of nearest neighbours.
   */
  var k = 1

  /**
   * Conjunto de datos almacenado tras la etapa de entrenamiento.
   */
  var trainingData: RDD[LabeledPoint] = null

  /**
   * Number of AND functions.
   */
  var numAND = 10

  /**
   * Number of OR functions.
   */
  var numOR = 4

  /**
   * Buckets' size.
   */
  var width: Double = 1

  /**
   * Random seed.
   */
  var seed: Long = 1

  /**
   * Spark's context.
   */
  private var sc: SparkContext = null

  /**
   * Table with hash functions.
   */
  private var tables: ArrayBuffer[ANDsTable] = null

  /**
   * kNN calculator (sequential version).
   */
  private var knnSequential: KNNSequential = null

  override def train(trainingSet: RDD[LabeledPoint]): Unit = {
    // Assign training data.
    trainingData = trainingSet
    trainingData.persist

    sc = trainingSet.sparkContext

    tables = createANDTables(trainingData.first().features.size, new Random(seed))

    knnSequential = new KNNSequential(k)
  }

  override def classify(testInstances: RDD[(Long, Vector)]): RDD[(Long, Double)] = {
    // ((test_id, test_instance), list_train_insts_bucket)
    var knnJoinORs: RDD[((Long, Vector), List[LabeledPoint])] = null

    testInstances.persist

    // For each OR
    for { i <- 0 until numOR } {
      val andTable = tables(i)

      // Map the training data set to get the bucket of each instance
      val hashTrainRDD = trainingData.map { instance =>
        (andTable.hash(instance), instance)
      }

      // Group by bucket all training instances
      val hashListTrainRDD = hashTrainRDD.groupByKey.mapValues(_.toList).map {
        case (bucket, list) => (bucket, list.distinct)
      }

      // Map the test data set to get the bucket of each instance
      val hashTestRDD = testInstances.map {
        case (indx: Long, instance: Vector) =>
          (andTable.hash(instance), (indx, instance))
      }

      // Join test RDD with the list of training instances in each bucket (by bucket)
      val joinTestTrainGroupedByBucketRDD = hashTestRDD.leftOuterJoin(hashListTrainRDD).map({
        case (bucket, (instTest, listTrain)) => (instTest, listTrain)
      }).map {
        case (test, list) => (test, list.getOrElse(List[LabeledPoint]()))
      }

      // First OR function
      if (i == 0) {
        knnJoinORs = joinTestTrainGroupedByBucketRDD
        knnJoinORs.persist
      } else {
        // Join the kNN instances of each OR function
        knnJoinORs = knnJoinORs.join(joinTestTrainGroupedByBucketRDD).map {
          case (inst, lists) => (inst, lists._1 ::: lists._2)
        }
        knnJoinORs.persist
      }
    }

    // Return the solution RDD: (instance's id, predicted class) 
    knnJoinORs.map(knnSequential.mapClassify)
  }

  override def setParameters(args: Array[String]): Unit = {
    // Check whether or not the number of arguments is even
    if (args.size % 2 != 0) {
      logger.log(Level.SEVERE, "LSHKNNPairNumberParamError",
        this.getClass.getName)
      throw new IllegalArgumentException()
    }

    for { i <- 0 until args.size by 2 } {
      try {
        val identifier = args(i)
        val value = args(i + 1)
        assignValToParam(identifier, value)
      } catch {
        case ex: NumberFormatException =>
          logger.log(Level.SEVERE, "LSHKNNNoNumberError", args(i + 1))
          throw new IllegalArgumentException()
      }
    }

    // Error if variables has not set correctly
    if (k <= 0 || numAND <= 0 || numOR <= 0 || width <= 0) {
      logger.log(Level.SEVERE, "LSHKNNWrongArgsValuesError")
      logger.log(Level.SEVERE, "LSHKNNPossibleArgs")
      throw new IllegalArgumentException()
    }
  }

  protected def assignValToParam(identifier: String, value: String): Unit = {
    identifier match {
      case "-k"   => k = value.toInt
      case "-and" => numAND = value.toInt
      case "-or"  => numOR = value.toInt
      case "-w"   => width = value.toDouble
      case "-s"   => seed = value.toLong
      case somethingElse: Any =>
        logger.log(Level.SEVERE, "LSHKNNWrongArgsError", somethingElse.toString())
        logger.log(Level.SEVERE, "LSHKNNPossibleArgs")
        throw new IllegalArgumentException()
    }
  }

  /**
   * Computes and returns an array of AND tables.
   *
   * @param  dim  Size of the hashing functions.
   * @param  r  Random generator.
   * @return Array with the tables.
   */
  private def createANDTables(dim: Int, r: Random): ArrayBuffer[ANDsTable] = {
    var andTables: ArrayBuffer[ANDsTable] = new ArrayBuffer[ANDsTable]

    for { i <- 0 until numOR } {
      andTables += new ANDsTable(numAND, dim, width, r.nextLong)
    }

    andTables
  }

}
