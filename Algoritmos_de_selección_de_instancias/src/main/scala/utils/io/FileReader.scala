package utils.io

import java.util.logging.Level
import java.util.logging.Logger

import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
 * Reads CSV files and generate a [[org.apache.spark.rdd.RDD]] out of it.
 *
 * @constructor Create a new file reader.
 *
 * @version 1.0.0
 * @author Alejandro González Rogel
 */
class FileReader {

  /**
   * Path to the file that contains the log messages.
   */
  private val bundleName = "resources.loggerStrings.stringsUtils";
  /**
   * Logger.
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  /**
   * The first attribute of an instance is the class attribute.
   *
   * If this value is "false", the class attribute is the last one.
   */
  var first = false

  /**
   * The first lines of the file are a header, i.e they are not part of the dataset.
   * 
   * This value is "False" by default.
   */
  var header = false

  /**
   * Number of header lines.
   *
   * Unused if attribute header==false. 
   */
  var headerLines = 0

  /**
   * Reads from a CSV file and transforms the dataset into a [[org.apache.spark.rdd.RDD]]
   * structure.
   * 
   * TODO Allow the user to specify the separator of the file.
   * 
   * Attributes must be separated by commas.
   *
   * @param  sc  Spark context.
   * @param  filePath  File path.
   * @return  Generated RDD.
   */
  def readCSV(sc: SparkContext, filePath: String): RDD[LabeledPoint] = {

    var data = sc.textFile(filePath)

    // Remove header if necessary.
    if (header) {
      val broadcastVar = sc.broadcast(headerLines)
      data = data.zipWithIndex().filter(_._2 >= broadcastVar.value).map(_._1)
    }

    // Store all the instances as [[org.apache.spark.mllib.regression.LabeledPoint]].
    if (!first) {
      // If the class attribute is the last one.
      data.map { line =>
        val features = line.split(',')
        LabeledPoint(features.last.toDouble,
          Vectors.dense(features.dropRight(1).map(_.toDouble)))
      }
    } else {
      // If the first attribute is the class attribute.
      data.map { line =>
        val features = line.split(',')
        LabeledPoint(features(0).toDouble, Vectors.dense(features.tail
          .map(_.toDouble)))
      }

    }

  }

  /**
   * Reads a string of arguments and sets this class attributes according to it.
   *
   * In the current version, this method only sets the attributes required to
   * read CSV files.
   * 
   * @param  args  Set of arguments in the form "-nameAttribute [Value]".
   * @throws IllegalArgumentException If one of the arguments does not exist
   *  or has an illegal value.
   */
  @throws(classOf[IllegalArgumentException])
  def setCSVParam(args: Array[String]): Unit = {
    var readingHL = false

    for { i <- 0 until args.size } { // Por cada argumento
      if (readingHL) {
        try {
          headerLines = args(i).toInt
        } catch {
          // If the following value is a number or does not exist
          case e @ (_: IllegalStateException | _: NumberFormatException) =>
            printErrorReadingCSVArgs
        }
        readingHL = false
      } else {
        args(i) match {
          case "-first" => first = true
          case "-hl" => {
            header = true
            readingHL = true
          }
          case _ =>
            printErrorReadingCSVArgs
        }
      }
    }
  }

  /**
   * Outputs an error message if an error has occured when setting up the attributes
   * of this class.
   *
   * It uses the standard error output.
   *
   * @throws IllegalArgumentException If one of the arguments is not correct.
   */
  @throws(classOf[IllegalArgumentException])
  private def printErrorReadingCSVArgs(): Unit = {
    logger.log(Level.SEVERE, "FileReaderWrongArgsCSVError")
    logger.log(Level.SEVERE, "FileReaderPossibleArgsCSV")
    throw new IllegalArgumentException("Wrong parameter format when trying" +
      "to read the dataset")
  }

}
