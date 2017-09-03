package launcher.execution

import java.util.logging.Level
import java.util.logging.Logger
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import instanceSelection.abstr.TraitIS
import utils.ArgsSeparator
import utils.io.FileReader
import utils.io.ResultSaver
import classification.abstr.TraitClassifier
import org.apache.spark.mllib.util.MLUtils

/**
 * Executes a data mining job with only a classification task.
 *
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ClassExec extends TraitExec {

  /**
   * Path to the file that contains the logger strings.
   */
  private val bundleName = "resources.loggerStrings.stringsExec";
  /**
   * Logger.
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  /**
   * Accuracy rate of the classifier in each cross-validation iteration
   */
  protected val classificationResults = ArrayBuffer.empty[Double]

  /**
   * Time taken by the classifier in each iteration of the cross-validation
   */
  protected val execTimes = ArrayBuffer.empty[Long]

  /**
   * Data mining job launcher for those executions with one sequential classifier.
   * 
   * This class does not only generate the instances of the required algorithms, but
   * it also reads the data set and stores the results of the job.
   *
   * It allows for cross-validation if specified by the input arguments.
   *
   * @param  args	String of arguments for the configuration of all the tasks
   *   executed by this class.
   *
   *   This string contains 4 diferenciable subdivisions: arguments for the dataset
   *   reader, for the filter, for the classifier and for the cross-validation.
   *   This last subdivision is not mandatory.
   */
  override def launchExecution(args: Array[String]): Unit = {

    // Arguments divided according to the task they refer to.
    val Array(readerArgs, instSelectorArgs, classiArgs, cvArgs) =
      divideArgs(args)

    // Get the classifier
    val classifier = createClassifier(classiArgs)
    val classifierName = classiArgs(0).split("\\.").last

    // Get a new Spark context
    val sc = new SparkContext()
    // Used for debugging purposes
    //    val master = "local[2]"
    //    val sparkConf =
    //      new SparkConf().setMaster(master).setAppName("Prueba_Eclipse")
    //      val sc = new SparkContext(sparkConf)

    val resultSaver = new ResultSaver(args,classifierName)
    try {

      val originalData = readDataset(readerArgs, sc).persist
      originalData.name = "OriginalData"

      val cvfolds = createCVFolds(originalData, cvArgs)
      var counter = 0
      resultSaver.writeHeaderInFile()
      cvfolds.map {
        // For each train-test pair.
        case (train, test) => {
          executeExperiment(sc, classifier, train, test)
          logger.log(Level.INFO, "iterationDone")
          resultSaver.storeResultsClassInFile(counter,classificationResults(counter), execTimes(counter))
          counter+=1
        }
      }

      logger.log(Level.INFO, "Saving")
      saveResults(resultSaver)
      logger.log(Level.INFO, "Done")

    } finally {
      sc.stop()
    }
  }

  /**
   * Divide the arguments according to the task they influence. 
   *
   * @param  args Arguments of the data mining job.
   *
   * @return Divided arguments.
   *
   * @throws IllegalArgumentException  If the arguments format is incorrect.
   *
   */
  protected def divideArgs(args: Array[String]): Array[Array[String]] = {

    var step = 0
    val maxDivisions = ArgsSeparator.maxId
    var optionsArrays: Array[ArrayBuffer[String]] = Array.ofDim(maxDivisions)

    for { i <- 0 until maxDivisions } {
      optionsArrays(i) = new ArrayBuffer[String]
    }

    if (args(0) != ArgsSeparator.READER_SEPARATOR.toString) {
      logger.log(Level.SEVERE, "WrongBeginningParam")
      throw new IllegalArgumentException()
    }
    for { i <- 0 until args.size } {
      if (step == maxDivisions || args(i) != ArgsSeparator(step).toString()) {
        optionsArrays(step - 1) += args(i)
      } else {
        step += 1
      }
    }
    // Check the minimum requirements are fulfilled:
    // There must be at least one argument for the reader
    // There must be arguments for at least one filter or one classifier.
    if (optionsArrays(0).isEmpty) {
      logger.log(Level.SEVERE, "NoCommonParameters")
      throw new IllegalArgumentException()
    }
    if (optionsArrays(1).isEmpty && optionsArrays(2).isEmpty) {
      logger.log(Level.SEVERE, "NoFilterORClassifierParameters")
      throw new IllegalArgumentException()
    }

    // Change the datatype ArrayBuffer for normal Arrays
    var result: Array[Array[String]] = Array.ofDim(optionsArrays.size)
    for { i <- 0 until result.size } {
      result(i) = optionsArrays(i).toArray
    }

    result
  }

  /**
   * Reads a dataset from a file.
   *
   * @param readerArgs  Arguments for the reader class.
   * @param sc Spark context.
   *
   * @return Dataset.
   */
  protected def readDataset(readerArgs: Array[String],
                            sc: SparkContext): RDD[LabeledPoint] = {
    logger.log(Level.INFO, "ReadingDataset")
    val reader = new FileReader
    reader.setCSVParam(readerArgs.drop(1))
    reader.readCSV(sc, readerArgs.head)
  }

  
  /**
   * Creates train-test subsets from the dataset.
   *
   * By default, only 10% of the instances are used for the test set.
   *
   * @param  originalData  Given dataset.
   * @param  crossValidationArgs  Cross-validation arguments.
   *
   * @return pares entrenamiento-test
   */
  protected def createCVFolds(originalData: RDD[LabeledPoint],
                              crossValidationArgs: Array[String]): Array[(RDD[LabeledPoint], RDD[LabeledPoint])] = {

    var cvFolds = 1
    var cvSeed = 1
    // Check for cross-validation
    if (!crossValidationArgs.isEmpty) {
      cvFolds = crossValidationArgs.head.toInt
      if (crossValidationArgs.size == 2) {
        cvSeed = crossValidationArgs(1).toInt
      }
    }

    if (cvFolds > 1) {
      MLUtils.kFold(originalData, cvFolds, cvSeed)
    } else {
      val cv: Array[(RDD[LabeledPoint], RDD[LabeledPoint])] = new Array(1)
      val tmp = originalData.randomSplit(Array(0.9, 0.1), cvSeed)
      cv(0) = (tmp(0), tmp(1))
      cv
    }
  }

  /**
   * Main method. Performs the classification task.
   *
   * @param sc Spark context.
   * @param instSelector Filter.
   * @param classifier Classifier.
   * @param train Training set.
   * @param test Test set.
   *
   */
  private def executeExperiment(sc: SparkContext,
                                classifier: TraitClassifier,
                                train: RDD[LabeledPoint],
                                test: RDD[LabeledPoint]): Unit = {

    // Use the classifier
    val start = System.currentTimeMillis
    val classifierResults = applyClassifier(classifier,
      train, test, sc)
    execTimes += System.currentTimeMillis - start
    classificationResults += classifierResults

  }

  /**
   * Creates and configures the classifier.
   *
   * @param classifierArgs  Arguments for the classifier.
   */
  private def createClassifier(
    classifierArgs: Array[String]): TraitClassifier = {

    val classifierName = classifierArgs.head
    val argsWithoutClassiName = classifierArgs.drop(1)
    val classifier =
      Class.forName(classifierName).newInstance.asInstanceOf[TraitClassifier]
    classifier.setParameters(argsWithoutClassiName)
    classifier
  }

  /**
   * Applies the classifier to the given dataset.
   *
   * It both trains and tests the classifier.
   *
   * @param  classifierArgs  Arguments for the classifier.
   * @param trainData  Training dataset.
   * @param postFilterData  Test dataset.
   * @param  sc  Spark context.
   *
   * @return accuracy results
   */
  protected def applyClassifier(classifier: TraitClassifier,
                                trainData: RDD[LabeledPoint],
                                testData: RDD[LabeledPoint],
                                sc: SparkContext): Double = {

    logger.log(Level.INFO, "ApplyingClassifier")

    // Train
    classifier.train(trainData)
    // Test
    val tmp = testData.zipWithIndex().map(line => (line._2, line._1)).persist
    val testFeatures = tmp.map(tuple => (tuple._1, tuple._2.features))
    val testClasses = tmp.map(tuple => (tuple._1, tuple._2.label))
    val classResults = classifier.classify(testFeatures)

    val hits =
      classResults.join(testClasses).filter(tuple => tuple._2._1 == tuple._2._2)
        .count()

    hits.toDouble / testData.count

  }

  /**
   * Stores information and results of the job execution.
   *
   * This method does not store a detailed report of the execution.
   *
   * @param  resultSaver	Object that saves the results.
   */
  protected def saveResults(resultSaver: ResultSaver): Unit = {

    // Number of folds
    val numFolds = classificationResults.size.toDouble

    // Calculamos los resultados medios de la ejecución
    val meanAccuracy =
      classificationResults.reduceLeft { _ + _ } / numFolds

    // Calculamos los resultados medios de la ejecución
    val meanClassifyTime =
      execTimes.reduceLeft { _ + _ } / numFolds

    // Save results
    resultSaver.storeResultsClassInFile(-1,meanAccuracy, meanClassifyTime)
  }

}
