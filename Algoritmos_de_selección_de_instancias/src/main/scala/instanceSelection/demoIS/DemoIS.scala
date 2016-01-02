package instanceSelection.demoIS

import java.util.logging.Level
import java.util.logging.Logger

import scala.collection.mutable.MutableList

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

import classification.seq.knn.KNN
import instanceSelection.abstracts.AbstractIS
import instanceSelection.seq.abstracts.LinearISTrait
import instanceSelection.seq.cnn.CNN
import utils.Option
import utils.partitioner.RandomPartitioner

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
 * @author  Alejandro González Rogel
 * @version 1.0.0
 */
class DemoIS extends AbstractIS {

  private val bundleName = "strings.stringsDemoIS";
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  // Valores por defecto

  // Número de repeticiones que se realizará la votación.
  var numRepeticiones = 10

  // Peso de la precisión sobre el tamaño al calcular el criterio de selección
  var alpha = 0.75

  // Número de paticiones en las que dividiremos el conjunto de datos para
  // las realizar las votaciones.
  var numPartitions = 10

  // Semilla para el generador de números aleatorios
  var seed = 1

  // Número de vecinos cercanos a utilizar en el KNN
  var k = 1

  // Porcentaje del conjunto de datos inicial usado para calcular la precisión
  // de las diferentes aproximaciones según el número de votos de las instancias.
  var datasetPerc = 1.0

  override def instSelection(
    sc: SparkContext,
    parsedData: RDD[LabeledPoint]): RDD[LabeledPoint] = {

    // Añadimos a las instancias un contador para llevar la cuenta del número
    // de veces que han sido seleccionadas en el siguiente paso
    val RDDconContador = parsedData.map(instance => (0, instance))

    val resultIter = doIterations(RDDconContador)

    val (indexBestCrit, bestCrit) = lookForBestCriterion(resultIter)

    return resultIter.filter(
      tupla => tupla._1 < indexBestCrit + 1).map(tupla => tupla._2)

  }

  /**
   * Leemos cada valor del array pasado por parámetro y actualizamos
   * los atributos correspondientes.
   *
   * @param args  Argumentos del programa para inicializar el algoritmo.
   *  El formato requerido es el siguiente: Existirá un par
   *  "String"-"Valor" por cada atributo, siendo el String
   *  el que indicará a que atributo nos referimos.
   * @throws  IllegalArgumentException En caso de no respetarse el formato
   *  mencionado.
   */
  override def setParameters(args: Array[String]): Unit = {

    for (i <- 0 until args.size by 2) {
      args(i) match {
        case "-rep"    => numRepeticiones = args(i + 1).toInt
        case "-alpha"  => alpha = args(i + 1).toDouble
        case "-s"      => seed = args(i + 1).toInt
        case "-k"      => k = args(i + 1).toInt
        case "-np"     => numPartitions = args(i + 1).toInt
        case "-dsperc" => datasetPerc = args(i + 1).toDouble
        case any =>
          logger.log(Level.SEVERE, "DemoISWrongArgsError", any.toString())
          logger.log(Level.SEVERE, "DemoISPossibleArgs")
          throw new IllegalArgumentException()
      }

      if (numRepeticiones <= 0 || alpha < 0 || alpha > 1 || k <= 0 ||
        numPartitions <= 0 || datasetPerc <= 0 || datasetPerc >= 100) {
        logger.log(Level.SEVERE, "DemoISWrongArgsValuesError")
        logger.log(Level.SEVERE, "DemoISPossibleArgs")
        throw new IllegalArgumentException()
      }
    }

  }

  /**
   * Durante un número fijado de veces, reparticionará el conjunto original
   * y aplicará sobre cada subconjunto un algoritmo de selección de instancias,
   * indicando aquellas instancias que han sido seleccionadas.
   *
   * @param RDDconContador Conjunto de datos original y donde cada instancia
   *   tiene un contador asociado que indica el número de veces que ha sido
   *   seleccionada
   * @return Conjunto de datos original con los contadores actualizados
   */
  private def doIterations(
    RDDconContador: RDD[(Int, LabeledPoint)]): RDD[(Int, LabeledPoint)] = {

    // Algoritmo secuencial a usar en cada subconjunto de datos.
    // TODO De momento no se habilita la posibilidad de cambiar este parametro.
    val seqAlgorithm: LinearISTrait = new CNN()

    var isInNodes = new ISInNodes()
    var actualRDD = RDDconContador

    for (i <- 0 until numRepeticiones) {
      // Redistribuimos las instancias en los nodos
      actualRDD = actualRDD.partitionBy(
        new RandomPartitioner(numPartitions, seed + 1))

      // En cada nodo aplicamos el algoritmo de selección de instancias
      actualRDD = actualRDD.mapPartitions(instancesIterator => {
        isInNodes.applyIterationPerPartition(instancesIterator, seqAlgorithm)
      })
    }
    actualRDD
  }

  /**
   * Calcula la adecuación de cada una de las posibles soluciones y encuentra
   * aquella cuyo valor sea más bajo.
   *
   * @param  RDDconContador Conjunto de datos original y donde cada instancia
   *   tiene un contador asociado que indica el número de veces que ha sido
   *   seleccionada
   *
   * @return Tupla con el índice (número mínimo de veces que la instancia ha sido
   *   seleccionada) que conduce al criterio más bajo y el propio valor de
   *   dicho criterio.
   *
   */
  private def lookForBestCriterion(
    RDDconContador: RDD[(Int, LabeledPoint)]): (Int, Double) = {

    var criterion = new MutableList[Double]
    // Creamos un conjunto de test
    val testRDD =
      RDDconContador.sample(false, datasetPerc / 100, seed).map(tupla => tupla._2)

    for (i <- 1 to numRepeticiones) {

      // Seleccionamos todas las instancias que superan el tresshold parcial
      val selectedInst = RDDconContador.filter(
        tupla => tupla._1 < i).map(tupla => tupla._2)

      if (selectedInst.isEmpty())
        criterion += Double.MaxValue
      else
        criterion += calcCriterion(selectedInst.collect(), testRDD.collect(),
          RDDconContador.count())

    }

    (criterion.indexOf(criterion.min), criterion.min)
  }

  /**
   *
   * @param  selectedInst  Subconjunto de instancias cuyo contador ha superado
   *   un número determinado
   * @param  dataSetSize  Tamaño del conjunto original de datos.
   * @return Resultado numérico del criterio de selección.
   */
  private def calcCriterion(selectedInst: Array[LabeledPoint],
                            testRDD: Array[LabeledPoint],
                            dataSetSize: Long): Double = {

    // Calculamos el porcentaje del tamaño que corresponde al subconjunto
    val subDatasize = selectedInst.size.toDouble / dataSetSize

    // Calculamos la tasa de error
    val knn = new KNN()
    val knnParameters:Array[String] = Array.ofDim(2)
    knnParameters(0)="-k"
    knnParameters(1)=k.toString()
    knn.setParameters(knnParameters)
    knn.train(selectedInst)
    var failures = 0
    for (instancia <- testRDD) {
      var result = knn.classify(instancia)
      if (result != instancia.label)
        failures += 1
    }
    val testError = failures.toDouble / testRDD.size

    // Calculamos el criterio de selección.
    return alpha * testError + (1 - alpha) * subDatasize
  }

  override def listOptions: Iterable[Option] = {
    val options: MutableList[Option] = MutableList.empty[Option]
    options += new Option("Nº rondas", "Número de rondas de votación", "-rep",
      numRepeticiones, 1)
    options += new Option("Alpha", "Valor alpha", "-alpha", alpha, 1)
    options += new Option("Vecinos", "Número de vecinos más cercanos en KNN", "-k", k, 1)
    options += new Option("Parciciones", "Número de particiones en las" +
      "que se dividirá el conjunto de datos original", "-np", numPartitions, 1)
    options += new Option("Porcentaje error", "Porcentaje del conjunto de datos" +
      "utilizado para calcular el error durante el cálculo del fitness", "-dsperc", datasetPerc, 1)
    options += new Option("Semilla", "Semilla del generador de números aleatorios", "-s", seed, 1)

    options
  }

}
