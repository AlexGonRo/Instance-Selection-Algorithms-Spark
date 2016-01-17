package launcher.execution

import java.util.logging.Level
import java.util.logging.Logger

import scala.collection.mutable.ArrayBuffer

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

import classification.seq.abstracts.TraitClassifier
import instanceSelection.abstracts.TraitIS
import utils.io.ResultSaver

/**
 * Permite la ejecución de una labor de minería de datos que contenga un
 * selector de instancias y un classificador, utilizado tras el filtrado.
 *
 * Además, realiza la medición del tiempo de filtrado.
 *
 * @author Alejandro González Rogel
 * @version 2.0.0
 */
class ISClassExecTest extends ISClassExec {

  /**
   * Ruta del fichero donde se almacenan los mensajes de log.
   */
  private val bundleName = "resources.loggerStrings.stringsExec";
  /**
   * Logger
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  /**
   * Tiempos de ejecucion del filtro en cada iteración de la validación
   * cruzada.
   */
  protected val executionTimes = ArrayBuffer.empty[Long]

  override protected def executeExperiment(sc: SparkContext,
                                           instSelector: TraitIS,
                                           classifier: TraitClassifier,
                                           train: RDD[LabeledPoint],
                                           test: RDD[LabeledPoint]): Unit = {

    train.persist()
    train.name = "TrainData"
    train.foreach { x => None }

    // Instanciamos y utilizamos el selector de instancias
    val start = System.currentTimeMillis
    val resultInstSelector = applyInstSelector(instSelector, train, sc)
    executionTimes += System.currentTimeMillis - start

    reduction += 1 - (resultInstSelector.count() / train.count().toDouble)

    val classifierResults = applyClassifier(classifier,
      resultInstSelector, test, sc)

    classificationResults += classifierResults
    classificationResults += 1
  }

  override protected def saveResults(args: Array[String],
                                     instSelectorName: String,
                                     classifierName: String): Unit = {

    // Número de folds que hemos utilizado
    val numFolds = executionTimes.size.toDouble

    // Calculamos los resultados medios de la ejecución
    val meanInstSelectorExecTime =
      executionTimes.reduceLeft { _ + _ } / numFolds
    val meanReduction =
      reduction.reduceLeft { _ + _ } / numFolds
    val meanAccuracy =
      classificationResults.reduceLeft { _ + _ } / numFolds

    // Salvamos los resultados
    val resultSaver = new ResultSaver()
    resultSaver.storeResultsInFile(args, meanReduction,
      meanAccuracy, instSelectorName, classifierName,
      true, meanInstSelectorExecTime)
  }

}
