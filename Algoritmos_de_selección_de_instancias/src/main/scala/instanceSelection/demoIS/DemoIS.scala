package instanceSelection.demoIS

import java.util.logging.Level
import java.util.logging.Logger
import scala.collection.mutable.MutableList
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import classification.knn.KNN
import instanceSelection.abstr.TraitIS
import instanceSelection.seq.abstr.TraitSeqIS
import instanceSelection.seq.cnn.CNN
import utils.partitioner.RandomPartitioner
import scala.collection.mutable.ListBuffer
import org.apache.spark.storage.StorageLevel
import java.util.Calendar
import java.text.SimpleDateFormat
import java.io.File
import java.io.IOException
import java.io.FileWriter

/**
 * Algoritmo de selección de instancias Democratic Instance Selection.
 *
 * Este algoritmo se basa en dividir el conjunto original en múltiples
 * subconjuntos y aplicar sobre cada uno de ellos un algoritmo de selección
 * de instancias más simple. Con la salida de este proceso, repetido durante
 * un número indicado de veces, calcularemos la verdadera salida del algoritmo
 * en base a un criterio que busca adecuar precisión y tamaño del conjunto
 * final.
 *
 * Participante en el patrón de diseño "Strategy" en el que actúa con el
 * rol de estrategia concreta ("concrete strategies"). Hereda de la clase que
 * participa como estrategia ("Strategy")
 * [[instanceSelection.abstr.TraitIS]].
 *
 * @constructor Crea un nuevo selector de instancias con los parámetros por
 *   defecto.
 *
 * @author  Alejandro González Rogel
 * @version 1.0.0
 */
class DemoIS extends TraitIS {

  /**
   * Ruta al fichero que guarda los mensajes de log.
   */
  private val bundleName = "resources.loggerStrings.stringsDemoIS"
  /**
   * Logger.
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName)

  /**
   * Número de repeticiones que se realizará la votación.
   */
  var numRepeticiones = 10

  /**
   *  Peso de la precisión sobre el tamaño al calcular el criterio de selección.
   */
  var alpha = 0.75

  /**
   *  Número de paticiones en las que dividiremos el conjunto de datos para
   *  las realizar las votaciones.
   */
  var numPartitions = 10

  /**
   * Número de vecinos cercanos a utilizar en el KNN.
   */
  var k = 1

  /**
   * Número de particiones en las que dividir el conjunto de entrenamiento del KNN.
   */
  var numPartitionsKNN = 10

  /**
   * Número de veces que se aplica la fase "reduce" en el KNN.
   */
  var numReducesKNN = 1

  /**
   * Número de particiones en las que dividir el conjunto de test del KNN.
   */
  var numReducePartKNN = 1 // -1 auto-setting

  /**
   * Peso máximo de cada partición del conjunto de test.
   */
  var maxWeightKNN = 0.0

  /**
   * Semilla para el generador de números aleatorios.
   */
  var seed: Long = 1

  /**
   * Porcentaje del conjunto de datos inicial usado para calcular la precisión
   * de las diferentes aproximaciones según el número de votos de las instancias.
   */
  var datasetPerc = 1.0

  /**
   * Manera de calcular la distancia entre dos instancias.
   *
   * MANHATTAN = 1 ; EUCLIDEAN = 2 ; HVDM = 3
   * TODO Actualmente no cuenta con utilidad
   */
  var distType = 2

  /**
   * Semilla aleatoria para la distribución de instancias en el KNN
   */
  var sknn: Long = 1

  /**
   * Si se debería guardar información adicional sobre la ejecución.
   */
  var xtraInfo = false

  override def instSelection(
    originalData: RDD[LabeledPoint]): RDD[LabeledPoint] = {

    // Añadimos una clave a todas las instancias
    val dataAndVotes = originalData.map(inst => (0, inst))

    // Operación Map
    // Realizar votaciones
    val ratedData = doVoting(dataAndVotes)

    // Operación Reduce
    // Cálculo del umbral de votos y selección de resultado
    val (criterions, extraData) = lookForBestCriterion(ratedData)
    val indBestCrit = criterions.indexOf(criterions.min) + 1

    // Si se ha pedido, se guarda información adicional sobre la ejecución.
    if (xtraInfo) {
      writeExtraInfo(criterions, extraData)
    }

    ratedData.filter(
      tuple => tuple._1 < indBestCrit).map(tuple => tuple._2)

  }

  /**
   * Durante un número fijado de veces, reparticionará el conjunto original
   * y aplicará sobre cada subconjunto un algoritmo de selección de instancias,
   * actualizando los votos de aquellas instancias que han sido no seleccionadas.
   *
   * @param RDDconContador Conjunto de datos original y donde cada instancia
   *   tiene un contador asociado que indica el número de veces que ha sido
   *   seleccionada.
   * @return Conjunto de datos original con los contadores actualizados.
   *
   * @todo Habilitar la posibilidad de cambiar el algoritmo de selección
   * de instancias usado cuando exista más de uno implementado.
   */
  private def doVoting(
    RDDconContador: RDD[(Int, LabeledPoint)]): RDD[(Int, LabeledPoint)] = {

    // Algoritmo secuencial a usar en cada subconjunto de datos.
    val seqAlgorithm: TraitSeqIS = new CNN()

    // Operación a realizar en cada uno de los nodos.
    val votingInNodes = new VotingInNodes()
    // RDD usada en la iteración concreta.
    var actualRDD = RDDconContador.zipWithIndex().map(line => (line._2, line._1))
    // Número de instancias por partición
    // Particionador para asignar nuevas particiones a las instancias.
    val partitioner = new RandomPartitioner(numPartitions, actualRDD.count(), seed)

    for { i <- 0 until numRepeticiones } {

      // Redistribuimos las instancias en los nodos
      actualRDD = actualRDD.partitionBy(partitioner)
      // En cada nodo aplicamos el algoritmo de selección de instancias
      actualRDD = actualRDD.mapPartitions(instancesIterator =>
        votingInNodes
          .applyIterationPerPartition(instancesIterator, seqAlgorithm)).persist

      partitioner.rep += 1

      // TODO Necesario para forzar la opeación hasta este punto.
      actualRDD.foreachPartition(x => None)

    }
    actualRDD.persist()
    actualRDD.name = "InstancesAndVotes"
    actualRDD.map(tupla => tupla._2)

  }

  /**
   * Calcula la adecuación de cada una de las posibles soluciones y encuentra
   * aquella cuyo valor sea más bajo.
   *
   * @param  RDDconContador Conjunto de datos original. Cada instancia
   *   tiene un contador asociado que indica el número de veces que ha sido
   *   seleccionada.
   *
   * @return Tupla con el número máximo de veces que la instancia no ha
   *   sido seleccionada y que conduce al criterio más bajo y el propio valor de
   *   dicho criterio.
   *
   */
  private def lookForBestCriterion(
    RDDconContador: RDD[(Int, LabeledPoint)]): (MutableList[Double], MutableList[Array[Double]]) = {

    // Donde almacenar los valores del criterion de cada número de votos.
    var criterions = new MutableList[Double]
    // Donde almacenar los valores de parámetros en cada iteración en busca
    // del mejor criterio.
    var extraData = new MutableList[Array[Double]]
    // Creamos un conjunto de test.
    val testRDD =
      RDDconContador.sample(false, datasetPerc / 100, seed).map(tupla => tupla._2)

    val originalDatasetSize = RDDconContador.count()

    for { i <- 1 to numRepeticiones } {

      // Seleccionamos todas las instancias que no superan el tresshold parcial
      // TODO No estoy seguro de que este persist sea necesario.
      val selectedInst = RDDconContador.filter(
        tupla => tupla._1 < i).map(tupla => tupla._2).persist()

      if (selectedInst.isEmpty) {
        criterions += Double.MaxValue
      } else {
        var tmp = calcCriterion(selectedInst, testRDD,
          originalDatasetSize)
        criterions += tmp._1
        extraData += tmp._2
      }
    }

    (criterions, extraData)
  }

  /**
   * Calcula un valor fitness aplicando la fórmula de una selección basándose
   * en la fórmula: alpha * testError + (1 - alpha) * subDatasize
   *
   * @param  selectedInst  Subconjunto de instancias cuyo contador ha superado
   *   un número determinado
   * @param  testRDD  Conjunto de instanciasde test para pasar al KNN
   * @param  dataSetSize  Tamaño del conjunto original de datos.
   * @return Resultado numérico del criterio de selección.
   *         Información adicional sobre las variables usadas.
   */
  private def calcCriterion(selectedInst: RDD[LabeledPoint],
                            testRDD: RDD[LabeledPoint],
                            dataSetSize: Long): (Double, Array[Double]) = {

    // Calculamos el porcentaje del tamaño que corresponde al subconjunto
    val subSetSize = selectedInst.count.toDouble
    val subSetPerc = subSetSize / dataSetSize

    // Calculamos la tasa de error
    val knn = new KNN()
    // TODO
    // Metido todo a pelo
    val knnParameters: ListBuffer[String] = new ListBuffer[String]
    knnParameters += "-k"
    knnParameters += k.toString()
    knnParameters += "-np"
    knnParameters += numPartitionsKNN.toString()
    knnParameters += "-nr"
    knnParameters += numReducesKNN.toString()
    knnParameters += "-nrp"
    knnParameters += numReducePartKNN.toString()
    knnParameters += "-maxW"
    knnParameters += maxWeightKNN.toString()
    knnParameters += "-dt"
    knnParameters += distType.toString()
    knnParameters += "-s"
    knnParameters += sknn.toString()
    knn.setParameters(knnParameters.toArray)
    knn.train(selectedInst)
    val tmp = testRDD.zipWithIndex().map(line => (line._2, line._1)).persist
    val testFeatures = tmp.map(tuple => (tuple._1, tuple._2.features))
    val testClasses = tmp.map(tuple => (tuple._1, tuple._2.label))
    val classResults = knn.classify(testFeatures)

    val failures = classResults.join(testClasses).filter(tuple => tuple._2._1 != tuple._2._2).count()

    val testError = failures.toDouble / testRDD.count

    // Esta información solo será usada si el parámetro xtraInfo es verdadero.
    var info = Array(dataSetSize, subSetSize, subSetPerc, failures, testError)

    // Calculamos el criterio de selección.
    (alpha * testError + (1 - alpha) * subSetPerc, info)

  }

  /**
   * Almacena en un archivo .csv información adicional sobre la ejecución
   * del algoritmo.
   *
   * La información guardada corresponde a los valores de diferentes parámetros
   * durante el cálculo de los diferentes umbrales durante la selección del
   * threshold.
   * @param  criterion  Lista con todos los posibles umbrales calculados.
   * @param  extraData  Datos sobre los valores de los parámetros durante el cálculo
   *             de los diferentes umbrales. Los datos almacenados corresponden a:
   *             totalDataSize, subDataSize, Reduction, failures, testError,
   *             finalCriterion
   */
  private def writeExtraInfo(criterion: MutableList[Double],
                             extraData: MutableList[Array[Double]]): Unit = {

    val fileSeparator = System.getProperty("file.separator")

    val resultPath = "results" + fileSeparator + "extra_info"

    val myDateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");

    val now = Calendar.getInstance().getTime()

    val fileName = resultPath + fileSeparator + "info_criterion_demoIS" +
      "_" + myDateFormat.format(now) + ".csv"

    val resultDir = new File(resultPath)
    if (!resultDir.exists()) {
      resultDir.mkdir()
    }

    val file = new File(fileName)

    val writer = new FileWriter(file)
    try {
      writer.write("n (#),totalDataSize (#),subDataSize (#),Reduction (%)," +
        "failures (#),testError (%), finalCriterion\n")
      for { i <- 0 until extraData.size } {
        writer.write(i.toString() + ',' + extraData.get(i).get(0) + "," +
          extraData.get(i).get(1) + "," + (100 - extraData.get(i).get(2) * 100) +
          "," + extraData.get(i).get(3) + "," + extraData.get(i).get(4) + "," +
          criterion(i) + "\n")
      }
      writer.write("Best criterion:" + "," + criterion.indexOf(criterion.min) +
        "," + criterion.min + "\n")
    } catch {
      case e: IOException => None
      // TODO Añadir mensaje de error
    } finally {
      writer.close()
    }

  }

  override def setParameters(args: Array[String]): Unit = {

    // Comprobamos si tenemos el número de atributos correcto.
    checkIfCorrectNumber(args.size)

    for { i <- 0 until args.size by 2 } {
      try {
        val identifier = args(i)
        val value = args(i + 1)
        assignValToParam(identifier, value)
      } catch {
        case ex: NumberFormatException =>
          logger.log(Level.SEVERE, "DemoISNoNumberError", args(i + 1))
          throw new IllegalArgumentException()
      }
    }

    if (numRepeticiones <= 0 || alpha < 0 || alpha > 1 || k <= 0 ||
      numPartitions <= 0 || datasetPerc <= 0 || datasetPerc >= 100
      || distType < 1 || distType > 3) {
      logger.log(Level.SEVERE, "DemoISWrongArgsValuesError")
      logger.log(Level.SEVERE, "DemoISPossibleArgs")
      throw new IllegalArgumentException()
    }

  }

  protected override def assignValToParam(identifier: String,
                                          value: String): Unit = {
    identifier match {
      case "-rep"     => numRepeticiones = value.toInt
      case "-alpha"   => alpha = value.toDouble
      case "-s"       => seed = value.toInt
      case "-k"       => k = value.toInt
      case "-np"      => numPartitions = value.toInt
      case "-dsperc"  => datasetPerc = value.toDouble
      case "-npknn"   => numPartitionsKNN = value.toInt
      case "-nrknn"   => numReducesKNN = value.toInt
      case "-nrpknn"  => numReducePartKNN = value.toInt
      case "-maxWknn" => maxWeightKNN = value.toDouble
      case "-d"       => distType = value.toInt
      case "-sknn"    => sknn = value.toLong
      case "-ei"      => if (value.toLong > 0) xtraInfo = true
      case somethingElse: Any =>
        logger.log(Level.SEVERE, "DemoISWrongArgsError",
          somethingElse.toString())
        logger.log(Level.SEVERE, "DemoISPossibleArgs")
        throw new IllegalArgumentException()
    }
  }

  /**
   * Comprueba si el número de parámetros introducido puede corresponder a
   * al de una configuración válida.
   *
   * @param Número de parametros y valores introducidos al configurar.
   *
   * @throws IllegalArgumentException si el número de parametros no es
   *   correcto
   */
  @throws(classOf[IllegalArgumentException])
  def checkIfCorrectNumber(argsNumber: Int): Unit = {

    if (argsNumber % 2 != 0) {
      logger.log(Level.SEVERE, "DemoISPairNumberParamError",
        this.getClass.getName)
      throw new IllegalArgumentException()
    }

  }

}
