package classification.knn

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.util.logging.Level
import java.util.logging.Logger
import java.util.StringTokenizer
import scala.collection.mutable.ListBuffer
import java.io._
import org.apache.spark.broadcast.Broadcast
import org.apache.hadoop.fs.Path
import org.apache.hadoop.fs.FSDataInputStream
import java.util.Scanner
import org.apache.hadoop.conf.Configuration
import classification.abstr.TraitClassifier
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vector

/**
 * Clasificador KNN.
 *
 * Este algoritmo de clasificación basa sus predicciones en las distancias
 * entre la instancia a clasificar y el resto del conjunto de datos, siendo
 * las instancias más próximas a la que nos interesa las que tendremos en cuenta
 * a la hora de predecir una clasificación.
 *
 * La implementación se ha basado en el resultado propuesto en el siguiente trabajo,
 * que ha sido adaptando para satisfacer las necesidades de esta librería:
 *
 * Jesús Maillo, Isaac Triguero and Francisco Herrera. "Un enfoque MapReduce del algoritmo
 * k-vecinos más cercanos para Big Data" In Actas de la XVI Edición Conferencia de la
 * Asociación Española para la Inteligencia Artificial CAEPIA 2015
 * Repositorio del algoritmo: https://github.com/JMailloH/kNN_IS
 */

class KNN extends TraitClassifier {

  /**
   * Ruta donde se encuentran las cadenas a mostrar por el logger.
   */
  private val bundleName = "resources.loggerStrings.stringsKNN";
  /**
   * Logger del clasificador.
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  /**
   * Número de vecinos cercanos.
   */
  var k = 1

  /**
   * Conjunto de datos almacenado tras la etapa de entrenamiento.
   */
  var trainingData: RDD[LabeledPoint] = null

  /**
   * Número de particiones del conjunto de entrenamiento
   */
  var numPartitions = 2
  /**
   * Número de particiones en las que presentar el resultado del reduce.
   */
  var numReduces = 1
  /**
   * Número de particiones en las que dividir el conjunto de test.
   *
   * -1 para automáticamente calcular el número de acuerdo al peso máximo que
   * indiquemos de cada partición ("auto-setting")
   *
   * TODO Actualmente no existe modo "auto-setting" implementado.
   */
  var numReducePartitions = 1
  /**
   * Tamaño máximo de una partición del conjunto de datos test.
   * Únicamente tiene utilidad en el modo "auto-setting" del cálculo del número de
   * particiones del conjunto de test.
   * TODO Actualmetne no tiene uso.
   */
  var maxWeight = 0.0

  /**
   * Algoritmo para calcular la distancia entre dos instancias.
   * MANHATTAN = 1 ; EUCLIDEAN = 2 ; HVDM = 3
   * TODO Actualmente no tiene uso.
   */
  var distType = 2

  /**
   * Contexto Spark en el que se ejecuta el algoritmo.
   */
  private var sc: SparkContext = null

  override def train(trainingSet: RDD[LabeledPoint]): Unit = {

    if (numPartitions != trainingSet.getNumPartitions) {
      trainingData = trainingSet.repartition(numPartitions)
    } else {
      trainingData = trainingSet
    }
    trainingData.persist
    sc = trainingSet.sparkContext
  }

  override def classify(instances: RDD[(Long, Vector)]): RDD[(Long, Double)] = {

    //Count the samples of each data set and the number of classes
    val numSamplesTrain = trainingData.count()
    instances.persist
    val numSamplesTest = instances.count()

    // numSamplesTest por iteración
    var numIterations = numReducePartitions
    // Número de instancias de test por cada iteración.
    var inc = (numSamplesTest / numIterations).toInt
    //top delimitador y sub delitmirador¿?
    var subdel = 0
    var topdel = inc - 1
    if (numIterations == 1) { // If only one partition
      topdel = numSamplesTest.toInt + 1
    }

    // TODO
    // Muchos null aquí
    var test: Broadcast[Array[Vector]] = null
    var result: RDD[(Long, Double)] = null
    var rightPredictedClasses: Array[Array[Array[Int]]] =
      new Array[Array[Array[Int]]](numIterations)

    val knnInNodes = new KNNInNodes();
    knnInNodes.k = k
    knnInNodes.distType = distType

    for (i <- 0 to numIterations - 1) {

      if (i == numIterations - 1) {
        // TODO
        // Este *2 es un apaño para coger todo lo que sobre.
        test = broadcastTest(instances.filterByRange(subdel, topdel * 2).map(line => line._2).collect)

      } else {
        test = broadcastTest(instances.filterByRange(subdel, topdel).map(line => line._2).collect)
      }

      // Calling KNN (Map Phase)  

      knnInNodes.subdel = subdel
      var resultKNNPartitioned =
        trainingData.mapPartitions(part => knnInNodes.knn(part, test))

      // Reduce phase
      var partResult =
        resultKNNPartitioned.reduceByKey(knnInNodes.combine(_, _), numReduces)

      if (result == null) {
        result = partResult.map(tupla => knnInNodes.calcPredictedClass(tupla))
      } else {
        val tmp = partResult.map(tupla => knnInNodes.calcPredictedClass(tupla))
        result = result.union(tmp)

      }

      // TODO El count es un apaño aquí para forzar la operación cada
      // iteración, porque de lo contrario el
      // planificador de tareas se lia con la sentencia knnInNodes.subdel = subdel
      // Buscar solución
      result.count

      subdel = subdel + inc
      topdel = topdel + inc
      // TODO
      // Esto era un destroy
      test.unpersist

    }
    result.repartition(numReduces)
  }

  override def setParameters(args: Array[String]): Unit = {

    // Comprobamos si tenemos el número de atributos correcto.
    if (args.size % 2 != 0) {
      logger.log(Level.SEVERE, "KNNPairNumberParamError",
        this.getClass.getName)
      throw new IllegalArgumentException()
    }

    for { i <- 0 until args.size by 2 } {
      try {
        args(i) match {
          case "-k"    => k = args(i + 1).toInt
          case "-np"   => numPartitions = args(i + 1).toInt
          case "-nr"   => numReduces = args(i + 1).toInt
          case "-nrp"  => numReducePartitions = args(i + 1).toInt
          case "-maxW" => maxWeight = args(i + 1).toDouble
          case "-dt"   => distType = args(i + 1).toInt
          case somethingElse: Any =>
            logger.log(Level.SEVERE, "KNNWrongArgsError", somethingElse.toString())
            logger.log(Level.SEVERE, "KNNISPossibleArgs")
            throw new IllegalArgumentException()
        }
      } catch {
        case ex: NumberFormatException =>
          logger.log(Level.SEVERE, "KNNNoNumberError", args(i + 1))
          throw new IllegalArgumentException()
      }
    }

    // Si las variables no han sido asignadas con un valor correcto.
    if (k <= 0 || numPartitions <= 0 || numReduces <= 0 || numReducePartitions <= -1
      || maxWeight < 0  || distType < 1 || distType > 3) {
      logger.log(Level.SEVERE, "KNNWrongArgsValuesError")
      logger.log(Level.SEVERE, "KNNPossibleArgs")
      throw new IllegalArgumentException()
    }

  }

  /**
   * Crea yna variable de broadcast para distribuir un conjunto de instancias
   * entre los nodos del sistema.
   *
   * @param data    Conjunto de instancias a distribuir.
   * @param context Contexto Spark.
   *
   */
  private def broadcastTest(data: Array[Vector]) = sc.broadcast(data)

}