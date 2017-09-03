package launcher.execution

import java.util.logging.Logger
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import classification.seq.abstr.TraitSeqClassifier
import instanceSelection.abstr.TraitIS
import utils.io.ResultSaver
import java.util.logging.Level

/**
 * Executes a data mining job with only one instance selection task and
 * a posterior classification task.
 *
 * It does also meassure instance selection execution time.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ISClassSeqExecTest extends ISClassSeqExec {

  /**
   * Path to the file that contains the logger strings.
   */
  private val bundleName = "resources.loggerStrings.stringsExec";
  /**
   * Logger
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

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

    // Create a new instance selector.
    val instSelector = createInstSelector(instSelectorArgs)
    val instSelectorName = instSelectorArgs(0).split("\\.").last

    // Create a new filter.
    val classifier = createClassifier(classiArgs)
    val classifierName = classiArgs(0).split("\\.").last

    // Create a new Spark context.
    val sc = new SparkContext()
    // Used for debugging purposes.
    // val master = "local[2]"
    // val sparkConf =
    //  new SparkConf().setMaster(master).setAppName("Prueba_Eclipse")
    // val sc = new SparkContext(sparkConf)

    val resultSaver = new ResultSaver(args, classifierName, instSelectorName)
    try {

      val originalData = readDataset(readerArgs, sc).persist
      originalData.name = "OriginalData"

      val cvfolds = createCVFolds(originalData, cvArgs)

      var counter = 0
      resultSaver.writeHeaderInFile()
      cvfolds.map {
        // For each train-test set.
        case (train, test) => {
          executeExperiment(sc, instSelector, classifier, train, test)
          logger.log(Level.INFO, "iterationDone")
          resultSaver.storeResultsFilterClassInFile(counter, reduction(counter), classificationResults(counter), filterTimes(counter), classifierTimes(counter))
          counter += 1
        }
      }

      logger.log(Level.INFO, "Saving")
      saveResults(resultSaver)
      logger.log(Level.INFO, "Done")

    } finally {
      sc.stop()
    }
  }

  override protected def executeExperiment(sc: SparkContext,
                                           instSelector: TraitIS,
                                           classifier: TraitSeqClassifier,
                                           train: RDD[LabeledPoint],
                                           test: RDD[LabeledPoint]): Unit = {

    train.persist()
    train.name = "TrainData"
    train.foreachPartition { x => None }

    // Use instance selector
    var start = System.currentTimeMillis
    val resultInstSelector = applyInstSelector(instSelector, train).persist
    resultInstSelector.foreachPartition { x => None }
    filterTimes += System.currentTimeMillis - start

    reduction += (1 - (resultInstSelector.count() / train.count().toDouble)) * 100

	// Use classifier
    start = System.currentTimeMillis
    val classifierResults = applyClassifier(classifier,
      resultInstSelector, test, sc)
    classifierTimes += System.currentTimeMillis - start

    classificationResults += classifierResults

  }

  /**
   * Stores information and results of the job execution.
   *
   * This method does not store a detailed report of the execution.
   *
   * @param  resultSaver	Object that saves the results.
   */
  override protected def saveResults(resultSaver: ResultSaver): Unit = {

    val numFolds = filterTimes.size.toDouble

    // Get mean execution times, dataset reduction and accuracy of the
	// classifier.
    val meanInstSelectorExecTime =
      filterTimes.reduceLeft { _ + _ } / numFolds
    val meanClassifierTime =
      classifierTimes.reduceLeft { _ + _ } / numFolds
    val meanReduction =
      reduction.reduceLeft { _ + _ } / numFolds
    val meanAccuracy =
      classificationResults.reduceLeft { _ + _ } / numFolds

    // Save results
    resultSaver.storeResultsFilterClassInFile(-1,meanReduction, meanAccuracy, meanInstSelectorExecTime, meanClassifierTime)
  }

}
