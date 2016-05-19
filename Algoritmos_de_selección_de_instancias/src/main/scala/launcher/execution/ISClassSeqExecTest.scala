package launcher.execution

import java.util.logging.Logger

import scala.collection.mutable.ArrayBuffer

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

import classification.seq.abstr.TraitSeqClassifier
import instanceSelection.abstr.TraitIS
import utils.io.ResultSaver

/**
 * Ejecuta de una labor de minería de datos que contenga un
 * selector de instancias y un classificador, utilizado tras el filtrado.
 *
 * Además, realiza la medición del tiempo de filtrado.
 *
 * Participa en un patrón "Strategy" de la misma manera y en el
 * mismo rol que la clase [[launcher.execution.ISClassExec]] de la que hereda.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ISClassSeqExecTest extends ISClassSeqExec {

  /**
   * Ruta del fichero donde se almacenan los mensajes de log.
   */
  private val bundleName = "resources.loggerStrings.stringsExec";
  /**
   * Logger
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);


  override protected def executeExperiment(sc: SparkContext,
                                           instSelector: TraitIS,
                                           classifier: TraitSeqClassifier,
                                           train: RDD[LabeledPoint],
                                           test: RDD[LabeledPoint]): Unit = {

    train.persist()
    train.name = "TrainData"
    train.foreachPartition { x => None }

    // Instanciamos y utilizamos el selector de instancias
    var start = System.currentTimeMillis
    val resultInstSelector = applyInstSelector(instSelector, train, sc).persist
    resultInstSelector.foreachPartition { x => None }
    filterTimes += System.currentTimeMillis - start

    reduction += (1 - (resultInstSelector.count() / train.count().toDouble)) * 100

    start = System.currentTimeMillis
    val classifierResults = applyClassifier(classifier,
      resultInstSelector, test, sc)
    classifierTimes += System.currentTimeMillis - start

    classificationResults += classifierResults

  }

  override protected def saveResults(args: Array[String],
                                     instSelectorName: String,
                                     classifierName: String): Unit = {

    // Número de folds que hemos utilizado
    val numFolds = filterTimes.size.toDouble

    // Calculamos los resultados medios de la ejecución
    val meanInstSelectorExecTime =
      filterTimes.reduceLeft { _ + _ } / numFolds
    val meanClassifierTime =
      classifierTimes.reduceLeft { _ + _ } / numFolds
    val meanReduction =
      reduction.reduceLeft { _ + _ } / numFolds
    val meanAccuracy =
      classificationResults.reduceLeft { _ + _ } / numFolds

    // Salvamos los resultados
    val resultSaver = new ResultSaver()
    resultSaver.storeResultsFilterClassInFile(args, meanReduction,
      meanAccuracy, instSelectorName, classifierName,
      meanInstSelectorExecTime, meanClassifierTime)
  }

}
