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
 * Ejecuta una labor de minería de datos que contenga un
 * selector de instancias y un classificador secuencial, utilizado tras el filtrado.
 *
 * Participante en el patrón de diseño "Strategy" en el que actúa con el
 * rol de estrategia concreta ("concrete strategies"). Hereda de la clase que
 * actua como estrategia ("Strategy") [[launcher.execution.TraitExec]] y que
 * será usada por [[launcher.ExperimentLauncher]] en la aplicación de este
 * patrón.
 *
 * También es participante de su propio patrón "Strategy", donde actua como
 * contexto de dos estrategias diferentes: [[instanceSelection.abstr.TraitIS]]
 * y [[classification.seq.abstr.TraitSeqClassifier]].
 *
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ISClassSeqExec extends ISClassExec {

  /**
   * Ruta del fichero donde se almacenan los mensajes de log.
   */
  private val bundleName = "resources.loggerStrings.stringsExec";
  /**
   * Logger.
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

    try {

      val originalData = readDataset(readerArgs, sc).persist
      originalData.name = "OriginalData"

      val cvfolds = createCVFolds(originalData, cvArgs)

      cvfolds.map {
        // Por cada par de entrenamiento-test
        case (train, test) => {
          executeExperiment(sc, instSelector, classifier, train, test)
          logger.log(Level.INFO, "iterationDone")
        }
      }

      logger.log(Level.INFO, "Saving")
      saveResults(args, instSelectorName, classifierName)
      logger.log(Level.INFO, "Done")

    } finally {
      sc.stop()
    }
  }

  /**
   * Realiza las labores de filtrado y clasificación.
   *
   * @param sc Contexto Spark
   * @param instSelector Selector de instancias
   * @param classifier Clasificado
   * @param train Conjunto de entrenamiento
   * @param test Conjunto de test
   *
   */
  protected def executeExperiment(sc: SparkContext,
                                  instSelector: TraitIS,
                                  classifier: TraitSeqClassifier,
                                  train: RDD[LabeledPoint],
                                  test: RDD[LabeledPoint]): Unit = {

    // Instanciamos y utilizamos el selector de instancias
    val trainSize = train.count
    val resultInstSelector = applyInstSelector(instSelector, train, sc).persist
    reduction += (1 - (resultInstSelector.count() / trainSize.toDouble)) * 100

    val classifierResults = applyClassifier(classifier,
      resultInstSelector, test, sc)
    classificationResults += classifierResults

  }

  /**
   * Intancia y configura un clasificador.
   *
   * @param classifierArgs  Argumentos de configuración del clasificador.
   */
  protected def createClassifier(
    classifierArgs: Array[String]): TraitSeqClassifier = {
    // Seleccionamos el nombre del algoritmo
    val classifierName = classifierArgs.head
    val argsWithoutClassiName = classifierArgs.drop(1)
    val classifier =
      Class.forName(classifierName).newInstance.asInstanceOf[TraitSeqClassifier]
    classifier.setParameters(argsWithoutClassiName)
    classifier
  }

  /**
   * Aplica el algoritmo de classificación sobre un conjunto de datos.
   *
   * De indicarse mediante parámetro, se ejecutará una validación cruzada utilizando
   * como conjunto de test instancias del conjunto de datos inicial que no
   * necesariamente se encontrarán en el conjunto de instancias tras el filtrado.
   *
   * @param  classifierArgs  Argumentos para el ajuste de los parámetros del
   *   clasificador
   * @param trainData  Conjunto de datos de entrenamiento
   * @param postFilterData  Conjunto de datos de test
   * @param  sc  Contexto Spark
   */
  protected def applyClassifier(classifier: TraitSeqClassifier,
                                trainData: RDD[LabeledPoint],
                                testData: RDD[LabeledPoint],
                                sc: SparkContext): Double = {
    // Iniciamos el clasficicador
    logger.log(Level.INFO, "ApplyingClassifier")

    // Entrenamos
    classifier.train(trainData.collect())
    // Clasificamos el test
    val testFeatures = testData.map(instance => instance.features).collect()
    val testLabels = testData.map(instance => instance.label).collect()
    val prediction = classifier.classify(testFeatures)
    // Comprobamos que porcentaje de aciertos hemos tenido.
    var hits = 0;
    for { i <- 0 until testLabels.size } {
      if (testLabels(i) == prediction(i)) {
        hits += 1
      }
    }
    (hits.toDouble / testLabels.size) * 100

  }

}
