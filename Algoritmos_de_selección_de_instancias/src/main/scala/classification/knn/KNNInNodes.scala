package classification.knn

import scala.collection.mutable.ListBuffer
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vector
import classification.seq.knn.KNNSeq
import scala.collection.mutable.ListBuffer
import java.util.ArrayList

/**
 *
 * Conjunto de operaciones que son necesarias para el algoritmo [[classification.knn.KNN]]
 * pero que necesitan ser serializables entre la red de nodos.
 *
 * La implementación se ha basado en el resultado propuesto en el siguiente trabajo,
 * que ha sido adaptando para satisfacer las necesidades de esta librería:
 * Jesús Maillo, Isaac Triguero and Francisco Herrera. "Un enfoque MapReduce del algoritmo
 * k-vecinos más cercanos para Big Data" In Actas de la XVI Edición Conferencia de la
 * Asociación Española para la Inteligencia Artificial CAEPIA 2015
 * Repositorio del algoritmo: https://github.com/JMailloH/kNN_IS
 *
 * @constructor
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
@SerialVersionUID(1L)
private class KNNInNodes extends Serializable {


  var k = 0
  var distType = 0
  var subdel = 0

  /**
   * Dado un conjunto de entrenamiento y otro de test, devuelve el resultado de aplicar
   * el algoritmo KNN.
   *
   * @param iter Iterador sobre el conjunto de entrenamiento
   * @param testSet Conjunto de test
   *
   * @return Iterador sobre un nuevo conjunto formado por id de la instancia
   *   correspondiente y una lista ordenada de clase-distancia con los K vecinos
   *   más cercanos.
   */
  def knn(iter: Iterator[LabeledPoint], testSet: Broadcast[Array[Vector]]): Iterator[(Long, ListBuffer[(Double, Double)])] = { 

    // Initialization
    var results: ListBuffer[(Long, ListBuffer[(Double, Double)])] = ListBuffer.empty

    // Create object KNN with the necesary information
    val knn = new KNNSeq()
    val knnParameters: Array[String] = Array.ofDim(2)
    knnParameters(0) = "-k"
    knnParameters(1) = k.toString()
    knn.setParameters(knnParameters)
    knn.train(iter.toList)

    var auxSubDel: Long = subdel
    for { instancia <- testSet.value } {
      results += ((auxSubDel, knn.knearestClasses(instancia)))
      auxSubDel = auxSubDel + 1
    }

    results.iterator

  }

  /**
   * Dadas dos listas con distancias a instancias más cercanas.
   *
   * Las listas han de estar ordenadas según la distancia, siendo el primer registro
   * la distancia más cercana y el último la más alejada.
   *
   * @param mapOut1 Una lista con elementos clase-distancia ordenada por la distancia.
   * @param mapOut2 Una lista con elementos clase-distancia ordenada por la distancia.
   * @result Una lista con elementos clase-distancia ordenada por la distancia con los
   *    elementos más cercanos de las dos listas introducidas por parámetro.
   */

  def combine(mapOut1: ListBuffer[(Double, Double)], mapOut2: ListBuffer[(Double, Double)]): ListBuffer[(Double, Double)] = {

    val numNeighbors = mapOut1.length
    var itOut1 = 0

    for (j <- 0 to numNeighbors - 1) { // Loop for the k neighbors
      if (mapOut1(itOut1)._2 <= mapOut2(j)._2) {
        mapOut2(j) = (mapOut1(itOut1)._1, mapOut1(itOut1)._2)
        itOut1 = itOut1 + 1
      }
    }

    mapOut2
  }

  /**
   * Dado un conjunto de instancias cercanas, evalúa y devuelve cual es la más común.
   * @param tupla id de la instancia clasificada junto con una lista de
   *     clase-distancia de las instancias más cercanas.
   * @return tupla con id de la instancia clasificada y la clase predicha.
   */
  def calcPredictedClass(tupla: (Long, ListBuffer[(Double, Double)])): (Long, Double) = {

    val classification = tupla._2.groupBy(t => t._1).maxBy(t => t._2.length)

    (tupla._1, classification._1)

  }

}
