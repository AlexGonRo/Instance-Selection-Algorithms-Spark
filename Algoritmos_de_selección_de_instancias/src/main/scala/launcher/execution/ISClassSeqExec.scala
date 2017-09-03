package launcher.execution

import utils.ArgsSeparator
import org.apache.spark.SparkContext
import instanceSelection.abstr.TraitIS
import utils.io.ResultSaver
import org.apache.spark.rdd.RDD
import classification.seq.abstr.TraitSeqClassifier
import org.apache.spark.mllib.regression.LabeledPoint
import scala.collection.mutable.ArrayBuffer
import java.util.logging.Logger
import utils.io.FileReader
import java.util.logging.Level
import org.apache.spark.SparkConf

/**
 * Executes a data mining job with only one instance selection task and
 * a posterior sequential classification task.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ISClassSeqExec extends ISClassExec {

  /**
   * Path to the file that contains the logger strings.
   */
  private val bundleName = "resources.loggerStrings.stringsExec";
  /**
   * Logger.
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

    // Get a new instance selector
    val instSelector = createInstSelector(instSelectorArgs)
    val instSelectorName = instSelectorArgs(0).split("\\.").last

    // Get a new classifier.
    val classifier = createClassifier(classiArgs)
    val classifierName = classiArgs(0).split("\\.").last

    // Get a new Spark context
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
        // For each train-test pair.
        case (train, test) => {
          executeExperiment(sc, instSelector, classifier, train, test)
          logger.log(Level.INFO, "iterationDone")
          resultSaver.storeResultsFilterClassInFile(counter, reduction(counter), classificationResults(counter), 0, 0)
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

  /**
   * Main method. Performs the instance selection and classification tasks.
   *
   * @param sc Spark context.
   * @param instSelector Filter.
   * @param classifier Classifier.
   * @param train Training set.
   * @param test Test set.
   *
   */
  protected def executeExperiment(sc: SparkContext,
                                  instSelector: TraitIS,
                                  classifier: TraitSeqClassifier,
                                  train: RDD[LabeledPoint],
                                  test: RDD[LabeledPoint]): Unit = {

    // Use instance selector
    val trainSize = train.count
    val resultInstSelector = applyInstSelector(instSelector, train).persist
    reduction += (1 - (resultInstSelector.count() / trainSize.toDouble)) * 100
	
	// Classify
    val classifierResults = applyClassifier(classifier,
      resultInstSelector, test, sc)
    classificationResults += classifierResults

  }

  /**
   * Creates and configures the classifier.
   *
   * @param classifierArgs  Arguments for the classifier.
   */
  protected def createClassifier(
    classifierArgs: Array[String]): TraitSeqClassifier = {

    val classifierName = classifierArgs.head
    val argsWithoutClassiName = classifierArgs.drop(1)
    val classifier =
      Class.forName(classifierName).newInstance.asInstanceOf[TraitSeqClassifier]
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
  protected def applyClassifier(classifier: TraitSeqClassifier,
                                trainData: RDD[LabeledPoint],
                                testData: RDD[LabeledPoint],
                                sc: SparkContext): Double = {

	logger.log(Level.INFO, "ApplyingClassifier")

    // Train
    classifier.train(trainData.collect())
    // Test
    val testFeatures = testData.map(instance => instance.features).collect()
    val testLabels = testData.map(instance => instance.label).collect()
    val prediction = classifier.classify(testFeatures)
	
    
    var hits = 0;
    for { i <- 0 until testLabels.size } {
      if (testLabels(i) == prediction(i)) {
        hits += 1
      }
    }
    (hits.toDouble / testLabels.size) * 100

  }
  
  /**
   * Stores information and results of the job execution.
   *
   * This method does not store a detailed report of the execution.
   *
   * @param  resultSaver	Object that saves the results.
   */
  override protected def saveResults(resultSaver: ResultSaver): Unit = {

    
    val numFolds = reduction.size.toDouble

    // Get mean dataset reduction and accuracy of the
	// classifier.
    val meanReduction =
      reduction.reduceLeft { _ + _ } / numFolds
    val meanAccuracy =
      classificationResults.reduceLeft { _ + _ } / numFolds

    // Save results
    resultSaver.storeResultsFilterClassInFile(-1,meanReduction, meanAccuracy, 0, 0)

  }

}
