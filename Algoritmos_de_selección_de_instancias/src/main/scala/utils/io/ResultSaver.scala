package utils.io

import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import java.io.IOException

//TODO Check the documentation of this class.

/**
 * Stores the results obtained after the classification proccess.
 *
 *
 * @constructor Creates a new object to save date.
 * 		It also creates an empty CSV file in the path idicated by
 * 		''resultPath''. This file stores all the information that will be safed.
 *     
 * @param  args Arguments for this class attributes.
 * @param  classifierName Name of the classifier used during this execution.
 * @param  filterName Name of the filter algorithm used during this execution.
 * @version 1.0.0
 * @author Alejandro González Rogel
 *
 */
class ResultSaver(val args: Array[String], val classifierName: String, val filterName: String = "NoFilter") {

  /**
   * Folder where all the data will be safed.
   *
   * It can contain a relative path. In the current version, this variable cannot be changed.
   * TODO Allow for the configuration of this attribute.
  */
  val resultPath = "results"

  /**
   * File separator of the operative system.
   */
  final private val fileSeparator = System.getProperty("file.separator")

  /**
   * Date format.
   */
  val myDateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");

  /**
   * Name of the file containing the dataset.
   */
  // TODO That ''1'' there is hrdcoded.
  val datasetName = new File(args(1)).getName

  /*
  * Actual time and date.
  */
  private val now = Calendar.getInstance().getTime()
  
  /**
   * Path (path to folder + file name) where the data will be safed.
   */
  val path = resultPath + fileSeparator + filterName + "_" +
    classifierName + "_" + myDateFormat.format(now) + ".csv"

  val resultDir = new File(resultPath)
  if (!resultDir.exists()) {
    resultDir.mkdir()
  }
  /**
   * File that stores the results.
   */
  val file = new File(path)

  /**
   * Saves a whole RDD in one single file in the master node.
   * 
   * This file will be located in a folder called ''RDD''. This
   * folder will be created if necessary.
   *
   * @param  args  Arguments used during the execution of the data mining task.
   * @param  fileId  Name of the file.
   *   resultante.
   * @param  rdd  RDD a almacenar.
   */
  def storeRDDInFile(args: Array[String],
                     fileId: String,
                     rdd: RDD[LabeledPoint]): Unit = {

    val path = resultPath + fileSeparator + "RDDs"

    val resultDir = new File(path)
    if (!resultDir.exists()) {
      resultDir.mkdir()
    }

    val now = Calendar.getInstance().getTime()
    val path = path + fileSeparator + fileId + "_" +
      myDateFormat.format(now)

    val writer = new FileWriter(new File(path))
    try {
      printRDDInFile(writer, rdd)
    } catch {
      case e: IOException => None
      // TODO Añadir mensaje de error
    } finally {

      writer.close()
    }

  }

  /**
   * Stores information about the execution.
   *
   * Must be used when the execution consisted in a filter and a classifier.
   *
   * @param  iter Cross-validation iteration. If the value <0, the information refers
   *   to the mean value of the execution.
   * @param  reduction  Percentaje of the instances of the original dataset that
   *   still remain after the instance selection process.	TODO Check this definition.
   * @param classificationAccuracy  Accuracy(%) of the classifier.
   * @param  filterTime  Time(seconds) spent during the preprocessing phase.
   * @param  classifierTime Time(seconds) spent during the classification phase.
   *
   */
  def storeResultsFilterClassInFile(iter: Int, reduction: Double,
                                    classificationAccuracy: Double,
                                    filterTime: Double,
                                    classifierTime: Double): Unit = {
    printIterResultsInFile(iter, reduction,
      classificationAccuracy, filterTime, classifierTime)
  }

  /**
   * Stores information about the execution
   *
   * Must be used when only a classification phase has been performed.
   *
   * @param  iter Cross-validation iteration. If the value <0, the information refers
   *   to the mean value of the execution.
   * @param  reduction  Percentaje of the instances of the original dataset that
   *   still remain after the instance selection process.	TODO Check this definition.
   * @param classificationAccuracy  Accuracy(%) of the classifier.
   * @param  execTime Time(seconds) spent during the classification phase.
   *
   */
  def storeResultsClassInFile(iter: Int, classificationAccuracy: Double,
                              execTime: Double): Unit = {

    printIterResultsInFile(iter, 0, classificationAccuracy, 0, execTime)

  }

  /**
   * Print the header of the results file.
   *
   * This header consists of just one line. It presents the name of the parameters
   * that have been meassured during the execution. These parameters are, in order,
   * iteration of the cross-validation, execution arguments, name of the dataset file,
   * name of he filter algorithm, name of the classifier, reduction rate, accuracy,
   * filtering time and classification time.
   *
   */
  def writeHeaderInFile(): Unit = {

    val writer = new FileWriter(file)
    try {
      writer.write("#cv,Program arguments,Dataset,Filter,Classifier,Reduction(%)," +
        "Accuracy(%),Filter time(s),Classification time(s)\n")
    } catch {
      case e: IOException => None
      // TODO Add an error message.
    } finally {

      writer.close()
    }
  }

  /**
   * Writes a new line to the file. It contains the values meassured during the
   * execution of one interation of the cross-validation.
   *
   * @param  iter Cross-validation iteration. If the value <0, the information refers
   *   to the mean value of the execution.
   * @param  reduction  Percentaje of the instances of the original dataset that
   *   still remain after the instance selection process.	TODO Check this definition.
   * @param classificationAccuracy  Accuracy(%) of the classifier.
   * @param  execTime Time(seconds) spent during the classification phase.
   */
  private def printIterResultsInFile(iter: Int,
                                     reduction: Double,
                                     classificationAccuracy: Double,
                                     filterTime: Double,
                                     classifierTime: Double): Unit = {

    val writer = new FileWriter(file, true)
    try {
      if (iter >= 0) {
        writer.write(iter.toString() + "," + argsToString(args) + "," + datasetName + "," + filterName + "," + classifierName + "," + reduction +
          "," + classificationAccuracy + "," + filterTime / 1000 + "," + classifierTime / 1000 + "\n")
      } else {
        writer.write("Average values," + argsToString(args) + "," + datasetName + "," + filterName + "," + classifierName + "," + reduction +
          "," + classificationAccuracy + "," + filterTime / 1000 + "," + classifierTime / 1000 + "\n")
      }
    } catch {
      case e: IOException => None
      // TODO Add error message.
    } finally {

      writer.close()
    }

  }

  /**
   * Stores a RDD structure in a single file in the master node.
   *
   * @param writer  file writer.
   * @param rdd  Data we want to store.
   */
  private def printRDDInFile(writer: FileWriter,
                             rdd: RDD[LabeledPoint]): Unit = {

    val rddLocalCopy = rdd.collect()
    rddLocalCopy.foreach { lp =>
      var line = ""
      lp.features.toArray.map { value => line += value.toString() + "," }
      line += lp.label.toString + "\n"
      writer.write(line)
    }

  }

  /**
   * Given a strings array, it creates a single string where the individual strings
   * are separated by empty spaces.
   */
  private def argsToString(args: Array[String]): String = {
    var result: String = ""
    for { param <- args } {
      result = result.concat(" " + param.toString())
    }
    result
  }

}
