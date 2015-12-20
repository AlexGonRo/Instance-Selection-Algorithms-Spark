package instanceSelection.demoIS

import java.util.logging.Level
import java.util.logging.Logger

import scala.collection.mutable.MutableList

import org.apache.spark.HashPartitioner
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

import classification.seq.knn.KNN
import instanceSelection.abstracts.AbstractIS
import instanceSelection.seq.cnn.CNN

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
class DemoIS(args: Array[String]) extends AbstractIS(args: Array[String]) {

  private val bundleName = "strings.stringsDemoIS";
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  // Valores por defecto

  // Número de repeticiones que se realizará la votación.
  var numRepeticiones = 10

  // Peso de la precisión sobre el tamaño al calcular el criterio de selección
  var alpha = 0.75

  // Semilla para el generador de números aleatorios
  var seed = 1

  // Número de vecinos cercanos a utilizar en el KNN
  var k = 5

  // Leemos los argumentos de entrada para, en caso de haber sido
  // introducido alguno, modificar los valores por defecto de los atributos
  readArgs(args)

  override def instSelection(
    sc: SparkContext,
    parsedData: RDD[LabeledPoint]): RDD[LabeledPoint] = {

    // Añadimos a las instancias un contador para llevar la cuenta del número
    // de veces que han sido seleccionadas en el siguiente paso
    var RDDconContador = parsedData.map(instance => (0, instance))

    val resultIter = doIterations(RDDconContador)

    val (indexBestCrit, bestCrit) = lookForBestCriterion(resultIter)

    return resultIter.filter(
        tupla => tupla._1 >= indexBestCrit + 1).map(tupla => tupla._2)

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
  override def readArgs(args: Array[String]): Unit = {

    for (i <- 0 until args.size by 2) {
      args(i) match {
        case "-rep"   => numRepeticiones = args(i + 1).toInt
        case "-alpha" => alpha = args(i + 1).toDouble
        case "-s"     => seed = args(i + 1).toInt
        case "-k"     => k = args(i + 1).toInt
        case _ =>
          logger.log(Level.SEVERE, "DemoISWrongArgsError")
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

    // TODO De momento se aplica el mismo algoritmo de IS en cada repetición

    val cnnAlgorithm = new CNN()
    var isInNodes = new ISInNodes()
    var actualRDD = RDDconContador

    for (i <- 0 until numRepeticiones) {
      // Redistribuimos las instancias en los nodos
      if (i > 0) {
        var numPartitions = actualRDD.partitions.size
        actualRDD = actualRDD.partitionBy(new HashPartitioner(numPartitions))
      }

      // En cada nodo aplicamos el algoritmo de selección de instancias
      actualRDD = actualRDD.mapPartitions(instancesIterator => {
        isInNodes.applyIterationPerPartition(instancesIterator, cnnAlgorithm)

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
    for (i <- 1 to numRepeticiones) {
      // TODO Este porcentaje debería variar en función del número de instancias
      // que tengamos y, probablemente, ser introducido por parámetro.
      val partOrininalRDD = RDDconContador.sample(false, 0.1, seed)

      // Seleccionamos todas las instancias que superan el tresshold parcial
      val selectedInst = partOrininalRDD.filter(
        tupla => tupla._1 >= i).map(tupla => tupla._2)

      if (selectedInst.isEmpty())
        criterion += Double.MaxValue
      else
        criterion += calcCriterion(selectedInst.collect(),
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
                            dataSetSize: Long): Double = {

    // Calculamos el porcentaje del tamaño que corresponde al subconjunto
    val subDatasize = selectedInst.size.toDouble / dataSetSize

    // Calculamos la tasa de error
    val knn = new KNN(k, selectedInst)
    var failures = 0
    for (instancia <- selectedInst) {
      var result = knn.classify(instancia)
      if (result != instancia.label)
        failures += 1
    }
    val trainingError = failures.toDouble / selectedInst.size

    // Calculamos el criterio de selección.
    return alpha * trainingError + (1 - alpha) * subDatasize
  }

}
