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

  /**
   * Ejecución de un clasificador tras la aplicación de un filtro.
   *
   * Este algoritmo, además de instancias los algoritmos deseados, también es
   * el encargado de ordenar la lectura de conjunto de datos y el almacenamiento
   * de los resultados.
   *
   * Se pueden indicar opciones en la cadena de entrada para realizar la
   * ejecución bajo una validación cruzada.
   *
   * @param  args  Cadena de argumentos que se traducen en la configuración
   *   de la ejecución.
   *
   *   Esta cadena deberá estar subdividida en cuatro partes: información para
   *   el lector, para el filtr o selector de instancias, para el clasificador y
   *   para la validación cruzada. La única partición que puede no aparecer será
   *   la relacionada con la validación cruzada
   */
  override def launchExecution(args: Array[String]): Unit = {

    // Argumentos divididos según el objeto al que afectarán
    val Array(readerArgs, instSelectorArgs, classiArgs, cvArgs) =
      divideArgs(args)

    // Creamos el selector de instancias
    val instSelector = createInstSelector(instSelectorArgs)
    val instSelectorName = instSelectorArgs(0).split("\\.").last

    // Creamos el classificador
    val classifier = createClassifier(classiArgs)
    val classifierName = classiArgs(0).split("\\.").last

    // Creamos un nuevo contexto de Spark
    val sc = new SparkContext()
    // Utilizarlo solo para pruebas lanzadas desde Eclipse
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
        // Por cada par de entrenamiento-test
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

    // Instanciamos y utilizamos el selector de instancias
    var start = System.currentTimeMillis
    val resultInstSelector = applyInstSelector(instSelector, train).persist
    resultInstSelector.foreachPartition { x => None }
    filterTimes += System.currentTimeMillis - start

    reduction += (1 - (resultInstSelector.count() / train.count().toDouble)) * 100

    start = System.currentTimeMillis
    val classifierResults = applyClassifier(classifier,
      resultInstSelector, test, sc)
    classifierTimes += System.currentTimeMillis - start

    classificationResults += classifierResults

  }

  override protected def saveResults(resultSaver: ResultSaver): Unit = {

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
    resultSaver.storeResultsFilterClassInFile(-1,meanReduction, meanAccuracy, meanInstSelectorExecTime, meanClassifierTime)
  }

}
