package classification.linear.knn

import scala.collection.mutable.MutableList

import org.apache.spark.mllib.regression.LabeledPoint

import classification.linear.abstracts.TraitClassifier

/**
 * Clasificador KNN.
 *
 * Este algoritmo de clasificación basa sus predicciones en las distancias
 * entre la instancia a clasificar y el resto del conjunto de datos, siendo
 * las instancias más próximas a la que nos interesa las que tendremos en cuenta
 * a la hora de predecir una clasificación.
 *
 * @param  k  Número de vecinos cercanos a tener en cuenta
 * @param  data  Conjunto de datos sobre el que se trabaja.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class KNN(var k: Int, var data: Iterable[LabeledPoint]) extends TraitClassifier {

  def classify(inst: LabeledPoint): Double = {

    // Calculamos la distancia a cada una de las instancias del conjunto de
    // datos
    val distances = for (actualInst <- data)
      yield (actualInst.label, euclideanDistance(
      inst.features.toArray, actualInst.features.toArray))

    // Almacenamos las K instancias más cercanas
    var closest: MutableList[(Double, Double)] = knearestInstances(inst, distances)

    // Calculamos cuál es la clase predominante y la devolvemos.
    val classification = closest.groupBy(t => t._1).maxBy(t => t._2.length)

    return classification._1
  }

  /**
   * Calcula la distancia euclidea entre dos vectores de datos numéricos.
   *
   * El cálculo de esta distancia no es completo, se suprime la operación de la
   * raiz cuadrada con la intención de ahorrar operaciones.
   */
  private def euclideanDistance(point1: Array[Double],
                                point2: Array[Double]): Double = {
    var dist = 0.0
    var i = 0
    for (i <- 0 until point1.size)
      dist += Math.pow((point1(i) - point2(i)), 2)

    return dist
  }

  /**
   * Calcula aquellas instancias más cercanas a aquella que nos interesa.
   *
   * @param  inst Instancia a la que encontrar los vecinos más cercanos
   * @param  distances  Conjunto de clase-distancia a cada elemento del conjunto
   * @return Conjunto de K elementos con clase-distancia al vecino más cercano
   */
  private def knearestInstances(inst: LabeledPoint,
                                distances: Iterable[(Double, Double)]): MutableList[(Double, Double)] = {

    var closest: MutableList[(Double, Double)] = MutableList.empty
    var iter = distances.iterator
    closest += iter.next()

    // Recorremos todas las distancias a las diferentes instancias
    while (iter.hasNext) {
      var actualInst = iter.next
      // Si no tenemos todavía K vecinos almacenados
      if (closest.size < k)
        closest += actualInst
      else {
        var maxDist = closest.maxBy((t) => t._2)._2
        // Si la distancia a una instancia es menor de lo encontrado hasta el
        // momento
        if (actualInst._2 < maxDist) {
          closest(closest.indexOf(closest.maxBy((t) => t._2))) = actualInst
        }
      }
    }

    closest
  }

}
