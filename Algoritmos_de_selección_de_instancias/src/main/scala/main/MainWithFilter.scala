package main

import java.util.logging.Level
import java.util.logging.Logger

import scala.collection.mutable.ArrayBuffer

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

import classification.seq.abstracts.TraitClassifier
import instanceSelection.abstracts.AbstractIS
import utils.io.FileReader
import utils.io.ResultSaver

/**
 * Clase lanzadora de las ejecuciones del programa.
 *
 * @author Alejandro González Rogel
 * @version 2.0.0
 */
object MainWithFilter {

  private val bundleName = "strings.stringsMain";
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  /**
   * Función principal de la ejecución.
   *
   * Encargada de aplicar un algoritmo de selección de
   * instancias sobre un conjunto de datos y mostrar el resultado.
   *
   * @param  args  Argumentos indicados en la invocación del objeto.
   *  La estructura de estos argumentos debería ser la siguiente:
   *  args(0)  debería contener el nombre del IS a utilizar
   *  args(1)  debería contener la ruta al conjunto de datos
   *  (Opcional)  Argumentos para el lector del conjunto de datos
   *  Argumentos para el IS
   */
  def main(args: Array[String]): Unit = {

    var crossValidationFolds = 1
    var crossValidationSeed = 1
    var executionTimes = ArrayBuffer.empty[Long]
    var reduction = ArrayBuffer.empty[Double]
    var classificationResults = ArrayBuffer.empty[Double]

    val argsDivided = divideArgs(args)

    val readerArgs = argsDivided(0)
    val filterArgs = argsDivided(1)
    val classifierArgs = argsDivided(2)
    val crossValidationArgs = argsDivided(3)

    //Utilizado solo para pruebas lanzadas desde Eclipse
        val sparkConf = new SparkConf().setMaster("local[2]").setAppName("Prueba_Eclipse")
        val sc = new SparkContext(sparkConf)

    //Creamos un nuevo contexto de Spark
//    val sc = new SparkContext(new SparkConf())

    try {

      // Instanciamos y utilizamos el lector para el conjunto de datos
      val originalData = readDataset(readerArgs, sc)
      //TODO ¿Forzar operacion?
      originalData.foreachPartition { i => None }


      //Vemos si existe validación cruzada
      if (!crossValidationArgs.isEmpty) {
        crossValidationFolds = crossValidationArgs.head.toInt
        if (crossValidationArgs.size == 2)
          crossValidationSeed = crossValidationArgs(1).toInt
      }

      //Subdividimos el conjunto inicial para la validación cruzada
      //TODO Obligatorio de momento
      val cvfolds =
        MLUtils.kFold(originalData, crossValidationFolds, crossValidationSeed)

      // Obtenemos los datos de clasificación
      val foldResult = cvfolds.map {
        // Por cada par de entrenamiento-test
        case (train, test) => {

          // Instanciamos y utilizamos el selector de instancias
          val start = System.currentTimeMillis

          val resultFilter = applyFilter(filterArgs, train, sc)
          //TODO ¿Forzar operacion?
          resultFilter.foreachPartition { i => None }
          executionTimes += System.currentTimeMillis - start
          reduction += 1- (resultFilter.count()/train.count().toDouble)

          val classifierResults = applyClassifier(classifierArgs,
            resultFilter, test, sc)
          classificationResults += classifierResults
        }
      }


      val meanFilterExecTime = 
        executionTimes.reduceLeft { _ + _ } / crossValidationFolds
      val meanReduction =
        reduction.reduceLeft { _ + _ } / crossValidationFolds
      val meanAccuracy =
        classificationResults.reduceLeft{ _ + _ } / crossValidationFolds

      // salvamos el resultado del filtro en un fichero
      val filterName = filterArgs(0).split("\\.").last
      val classifierName = classifierArgs(0).split("\\.").last
      saveResults(args, meanFilterExecTime,meanReduction, meanAccuracy, filterName, classifierName)

      logger.log(Level.INFO, "Done")

    } finally {
      sc.stop()
    }
  }

  /**
   * Divide los argumentos de entrada según sean para el lector, filtro,
   * clasificador o la validación cruzada
   *
   * @param   args Argumentos de entrada al programa
   *
   * @return Cadenas de argumentos divididas según su objetivo.
   *
   * @throws IllegalArgumentException  Si el formato de los argumentos es
   * erroneo.
   *
   */
  protected def divideArgs(args: Array[String]): Array[Array[String]] = {

    var step = 0
    var optionsArrays: Array[ArrayBuffer[String]] = Array.ofDim(4)
    val flags = Array("-r", "-f", "-c", "-cv")

    for (i <- 0 until flags.size)
      optionsArrays(i) = new ArrayBuffer[String]

    if (args(0) != "-r")
      throw new IllegalArgumentException() //TODO INDICAR UN ERROR AQUÍ SI LA SENTENCIA NO EMPIEZA POR -r

    for (i <- 0 until args.size) {
      if (step == flags.size || args(i) != flags(step)) {
        optionsArrays(step - 1) += args(i)
      } else {
        step += 1
      }
    }
    // Comprobamos que se cumplen los requisitos mínimos:
    // Debe existir al menos un atributo para el el lector 
    // Debe haber opciones para el filtro o para un clasificador
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
    for (i <- 0 until result.size) {
      result(i) = optionsArrays(i).toArray
    }

    return result
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
   * Aplica un algoritmo de selección de instancias sobre el conjunto de datos
   * inicial
   *
   * @param  filterArgs  Argumentos para el ajuste de los parámetros del
   *   filtro
   * @param originalData  Conjunto de datos inicial
   * @param  sc  Contexto Spark
   * @return Conjunto de instancias resultado
   */
  protected def applyFilter(filterArgs: Array[String],
                            originalData: RDD[LabeledPoint],
                            sc: SparkContext): RDD[LabeledPoint] = {
    logger.log(Level.INFO, "ApplyingIS")
    val filterName = filterArgs.head //Seleccionamos el nombre del algoritmo
    val argsWithoutFilterName = filterArgs.drop(1)
    val filter = Class.forName(filterName).newInstance.asInstanceOf[AbstractIS]
    filter.setParameters(argsWithoutFilterName)
    filter.instSelection(sc, originalData)
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
  protected def applyClassifier(classifierArgs: Array[String],
                                trainData: RDD[LabeledPoint],
                                testData: RDD[LabeledPoint],
                                sc: SparkContext): Double = {
    //Iniciamos el clasficicador
    logger.log(Level.INFO, "ApplyingClassifier")

    val classifierName = classifierArgs.head //Seleccionamos el nombre del algoritmo
    val argsWithoutClassiName = classifierArgs.drop(1)
    val classifier = Class.forName(classifierName).newInstance.asInstanceOf[TraitClassifier]
    classifier.setParameters(argsWithoutClassiName)

    //Entrenamos
    classifier.train(trainData.collect())
    //Clasificamos el test
    val testArray = testData.collect()
    val prediction = classifier.classify(testArray)
    //Comprobamos que porcentaje de aciertos hemos tenido.
    var hits = 0;
    for (i <- 0 until testArray.size) {
      if (testArray(i).label == prediction(i))
        hits += 1
    }
    (hits.toDouble / testArray.size) * 100
    
  }

  /**
   * Almacena todos los datos recogidos de la ejecución en un fichero de texto.
   *
   * Otro, contendrá un resumen de la ejecución.
   *
   * @param  args  argumentos de llamada de la ejecución
   * @param  initialData  Conjunto de datos inicial
   * @param  classifierName  Nombre del clasificador de instancias
   * @param  classificationResult  Tasa de acierto de clasificación
   * @param  filterTime  Tiempo tardado en ejecutar la selección de instancias.
   *
   */
  protected def saveResults(args: Array[String],
                            filterTime: Double,
                            reduction:Double,
                            classifierResult: Double,
                            filterName: String,
                            classifierName: String): Unit = {
    logger.log(Level.INFO, "Saving")
    val resultSaver = new ResultSaver()
    resultSaver.storeResultsInFile(args,filterTime,reduction, classifierResult,
      filterName, classifierName)
  }

}
