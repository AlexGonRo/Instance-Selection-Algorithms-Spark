package instanceSelection.lshis

import java.util.Random
import java.util.logging.Level
import java.util.logging.Logger

import scala.collection.mutable.ArrayBuffer

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

import instanceSelection.abstr.TraitIS

/**
 *
 * Locality Sensitive Hashing Instance Selection (LSH IS).
 *
 * It uses a Local Sensitive Hashing (LSH) algorithm on the initial dataset
 * in order to distribute the different instances into buckets of similar instances.
 * After that, we select, from each bucket, one instance of each class. This instance
 * will then be considered part of the filtered dataset.
 *
 * Arnaiz-González, Á., Díez-Pastor, J. F., Rodríguez, J. J., & García-Osorio,
 * C. (2016). Instance selection of linear complexity for big data. Knowledge-Based
 * Systems, 107, 83-95.
 *
 * @constructor Creates a new LSHISinstance selector with the attribute values by default.
 *
 * @author Alejandro González Rogel
 * @version 1.1.0
 */
class LSHIS extends TraitIS {

  /**
   * Path to the file with log strings.
   */
  private val bundleName = "resources.loggerStrings.stringsLSHIS";
  /**
   * Logger.
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  /**
   * Number of AND operations.
   */
  var ANDs: Int = 10
  /**
   * Number of OR operations.
   */
  var ORs: Int = 1
  /**
   * Bucket size.
   */
  var width: Double = 1
  /**
   * Seed.
   */
  var seed: Long = 1

  override def instSelection(
    parsedData: RDD[LabeledPoint]): RDD[LabeledPoint] = {

    parsedData.persist()

    parsedData.name = "TrainInLSHIS"

    val r = new Random(seed)

    val andTables = createANDTables(parsedData.first().features.size, r)

    // Stores the final result.
    var finalResult: RDD[LabeledPoint] = null

    for { i <- 0 until ORs } {
      val andTable = andTables(i)

      // Transform the original dataset into tuples
      // (assigned bucket, pair)-instance
      val keyInstRDD = parsedData.map { instancia =>
        ((andTable.hash(instancia), instancia.label), instancia)
      }
      // Choose one instance from each (bucket, class) pair.
      val partialResult = keyInstRDD.reduceByKey { (inst1, inst2) => inst1 }

      if (i == 0) { // If it is the first iteration of the ‘for’ loop.
        finalResult = partialResult.values.persist
        finalResult.name = "PartialResult"
      } else {
        // Compute again the assigned bucket for the already selected instances.
        val alreadySelectedInst = finalResult.map { instancia =>
          ((andTable.hash(instancia), instancia.label), instancia)
        }
        // Select those instances which key is not yet in the final subset.
        val keyClassRDDGroupBy = partialResult.subtractByKey(alreadySelectedInst)
        val selectedInstances = keyClassRDDGroupBy.map[LabeledPoint] {
          case (tupla, instancia) => instancia
        }

        // Merge the partial final result with the result of this iteration.
        finalResult = finalResult.union(selectedInstances).persist
      }
    }

    finalResult
  } // end instSelection

  /**
   * Create an array made of AND operations.
   *
   * Each one of these operations contains as many hash functions as stipulated by
   * by the input parameter.
   *
   * @param  dim  Hash functions dimension.
   * @param  r  Random number generator.
   * @return Array with all the tables.
   */
  private def createANDTables(dim: Int, r: Random): ArrayBuffer[ANDsTable] = {

    // Create as many AND operation as required.
    var andTables: ArrayBuffer[ANDsTable] = new ArrayBuffer[ANDsTable]
    for { i <- 0 until ORs } {
      andTables += new ANDsTable(ANDs, dim, width, r.nextInt)
    }
    andTables
  } // end createANDTables

  override def setParameters(args: Array[String]): Unit = {

    // Check if we have the correct number of attributes.
    if (args.size % 2 != 0) {
      logger.log(Level.SEVERE, "LSHISPairNumberParamError",
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
          logger.log(Level.SEVERE, "LSHISNoNumberError", args(i + 1))
          throw new IllegalArgumentException()
      }
    }

    // If any variable does not have the correct value.
    if (ANDs <= 0 || ORs <= 0 || width <= 0) {
      logger.log(Level.SEVERE, "LSHISWrongArgsValuesError")
      logger.log(Level.SEVERE, "LSHISPossibleArgs")
      throw new IllegalArgumentException()
    }

  } // end readArgs

  protected override def assignValToParam(identifier: String,
                                          value: String): Unit = {
    identifier match {
      case "-and" => ANDs = value.toInt
      case "-w"   => width = value.toDouble
      case "-s" => {
        seed = value.toInt
      }
      case "-or" => ORs = value.toInt
      case somethingElse: Any =>
        logger.log(Level.SEVERE, "LSHISWrongArgsError", somethingElse.toString())
        logger.log(Level.SEVERE, "LSHISPossibleArgs")
        throw new IllegalArgumentException()
    }
  }

}
