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
 * Ejecuta una labor de minería de datos que contenga un
 * selector de instancias y un classificador, utilizado tras el filtrado.
 *
 * ¡
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ClassExec extends TraitExec {

  /**
   * Ruta del fichero donde se almacenan los mensajes de log.
   */
  private val bundleName = "resources.loggerStrings.stringsExec";
  /**
   * Logger.
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  /**
   * Porcentajes de acierto en test del clasificador en cada iteración
   * de la validación cruzada.
   */
  protected val classificationResults = ArrayBuffer.empty[Double]

  /**
   * Tiempos de ejecucion del filtro en cada iteración de la validación
   * cruzada.
   */
  protected val executionTimes = ArrayBuffer.empty[Long]

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

    // Creamos el classificador
    val classifier = createClassifier(classiArgs)
    val classifierName = classiArgs(0).split("\\.").last

    // Creamos un nuevo contexto de Spark
    val sc = new SparkContext()
    // Utilizarlo solo para pruebas lanzadas desde Eclipse
    //    val master = "local[2]"
    //    val sparkConf =
    //      new SparkConf().setMaster(master).setAppName("Prueba_Eclipse")
    //      val sc = new SparkContext(sparkConf)

    try {

      val originalData = readDataset(readerArgs, sc).persist
      originalData.name = "OriginalData"

      val cvfolds = createCVFolds(originalData, cvArgs)

      cvfolds.map {
        // Por cada par de entrenamiento-test
        case (train, test) => {
          executeExperiment(sc, classifier, train, test)
          logger.log(Level.INFO, "iterationDone")
        }
      }

      logger.log(Level.INFO, "Saving")
      saveResults(args, classifierName)
      logger.log(Level.INFO, "Done")

    } finally {
      sc.stop()
    }
  }

  /**
   * Divide los argumentos de entrada según sean para el lector, selector de
   * instancias, clasificador o la validación cruzada
   *
   * @param  args Argumentos de entrada al programa
   *
   * @return Cadenas de argumentos divididas según su objetivo.
   *
   * @throws IllegalArgumentException  Si el formato de los argumentos es
   * erroneo.
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
    // Comprobamos que se cumplen los requisitos mínimos:
    // Debe existir al menos un atributo para el el lector
    // Debe haber opciones para el selector o para un clasificador
    if (optionsArrays(0).isEmpty) {
      logger.log(Level.SEVERE, "NoCommonParameters")
      throw new IllegalArgumentException()
    }
    if (optionsArrays(1).isEmpty && optionsArrays(2).isEmpty) {
      logger.log(Level.SEVERE, "NoFilterORClassifierParameters")
      throw new IllegalArgumentException()
    }

    // Convertimos los ArrayBuffer en Arrays normales
    var result: Array[Array[String]] = Array.ofDim(optionsArrays.size)
    for { i <- 0 until result.size } {
      result(i) = optionsArrays(i).toArray
    }

    result
  }

  /**
   * Lee un conjunto de datos desde un fichero de texto.
   *
   * @param readerArgs  Argumentos para la correcta lectura del fichero.
   * @param sc Contexto Spark
   *
   * @return Conjunto de datos resultante de la lectura.
   */
  protected def readDataset(readerArgs: Array[String],
                            sc: SparkContext): RDD[LabeledPoint] = {
    logger.log(Level.INFO, "ReadingDataset")
    val reader = new FileReader
    reader.setCSVParam(readerArgs.drop(1))
    reader.readCSV(sc, readerArgs.head)
  }

  /**
   * Genera tantos pares de conjuntos entrenamiento-test como se hayan requerido
   * por parámetro.
   *
   * De no indicarse nada mediante parámetro, se generará un conjunto de test
   * con el 10% de las instancias.
   *
   * @param  originalData  Conjunto de datos inicial
   * @param  crossValidationArgs  Argumentos para la validación cruzada
   *
   * @return pares entrenamiento-test
   */
  protected def createCVFolds(originalData: RDD[LabeledPoint],
                              crossValidationArgs: Array[String]):
                              Array[(RDD[LabeledPoint], RDD[LabeledPoint])] = {

    var cvFolds = 1
    var cvSeed = 1
    // Vemos si existe validación cruzada
    if (!crossValidationArgs.isEmpty) {
      cvFolds = crossValidationArgs.head.toInt
      if (crossValidationArgs.size == 2) {
        cvSeed = crossValidationArgs(1).toInt
      }
    }

    if (cvFolds > 1) {
      MLUtils.kFold(originalData, cvFolds, cvSeed)
    }
    else {
      val cv:Array[(RDD[LabeledPoint], RDD[LabeledPoint])] = new Array(1)
      val tmp = originalData.randomSplit(Array(0.9, 0.1), cvSeed)
      cv(0) = (tmp(0),tmp(1))
      cv
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
  private def executeExperiment(sc: SparkContext,
                                classifier: TraitClassifier,
                                train: RDD[LabeledPoint],
                                test: RDD[LabeledPoint]): Unit = {

    // Instanciamos y utilizamos el clasificador
    val start = System.currentTimeMillis
    val classifierResults = applyClassifier(classifier,
      train, test, sc)
    executionTimes += System.currentTimeMillis - start
    classificationResults += classifierResults

  }

  /**
   * Intancia y configura un clasificador.
   *
   * @param classifierArgs  Argumentos de configuración del clasificador.
   */
  private def createClassifier(
    classifierArgs: Array[String]): TraitClassifier = {
    // Seleccionamos el nombre del algoritmo
    val classifierName = classifierArgs.head
    val argsWithoutClassiName = classifierArgs.drop(1)
    val classifier =
      Class.forName(classifierName).newInstance.asInstanceOf[TraitClassifier]
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
  protected def applyClassifier(classifier: TraitClassifier,
                                trainData: RDD[LabeledPoint],
                                testData: RDD[LabeledPoint],
                                sc: SparkContext): Double = {
    // Iniciamos el clasficicador
    logger.log(Level.INFO, "ApplyingClassifier")

    // Entrenamos
    classifier.train(trainData)
    // Clasificamos el test
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
   * Almacena todos los datos recogidos de la ejecución en un fichero de texto.
   *
   * Otro, contendrá un resumen de la ejecución.
   *
   * @param  args  argumentos de llamada de la ejecución
   * @param  instSelectorName Nombre del selector de instancias
   * @param  classifierName  Nombre del clasificador de instancias
   */
  protected def saveResults(args: Array[String],
                            classifierName: String): Unit = {

    // Número de folds que hemos utilizado
    val numFolds = classificationResults.size.toDouble

    // Calculamos los resultados medios de la ejecución
    val meanAccuracy =
      classificationResults.reduceLeft { _ + _ } / numFolds

    // Calculamos los resultados medios de la ejecución
    val meanExecTime =
      executionTimes.reduceLeft { _ + _ } / numFolds

    // Salvamos los resultados
    val resultSaver = new ResultSaver()
    resultSaver.storeResultsClassInFile(args,
      meanAccuracy, classifierName, true, meanExecTime)
  }

}
