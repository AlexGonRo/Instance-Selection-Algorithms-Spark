package instanceSelection.seq.cnn

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.mllib.linalg.Vector
import scala.collection.mutable.MutableList
import instanceSelection.seq.abstracts.LinearISTrait

/**
 * Algoritmo de selección de instancias Condensed Nearest Neighbor (CNN)
 *
 * CNN construye un conjunto resultado S de un conjunto original T
 * tal que todo ejemplo de T está más cerca a un ejemplo de S de la misma
 * clase que a otro de S de clase distinta.
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class CNN extends LinearISTrait {

  def instSelection(data: Iterable[LabeledPoint]): Iterable[LabeledPoint] = {

    var s = new MutableList[LabeledPoint]

    // Comenzamos el conjunto S con únicamente una instancia
    s += data.head

    // Incrementamos iterativamente el conjunto S en base a la aplicación
    // del algoritmo
    var keepGoing = true
    while (keepGoing) {
      doTheLogic(s, data) match{
        case Left(inst) => s += inst
        case Right(_) => keepGoing = false
      }

    }

    return s

  }

  /**
   * Dados dos conjuntos de datos, recorre el segundo asegurandose de que toda
   * instancia existente en él posee un vecino más cercano de la misma clase
   * en el primer conjunto. En caso de no ser así, devuelve la instancia donde no se
   * ha complido la premisa
   *
   * @param  s  Subconjunto de data
   * @param  data Conjunto inicial de datos
   * @return Instancia a añadir en el subconjunto S o el booleano "false" para
   *   indicar la finalización del algoritmo
   */
  private def doTheLogic(
      s: Iterable[LabeledPoint],
      data: Iterable[LabeledPoint]): Either[LabeledPoint,Boolean] = {

    var iterador = data.iterator
    while (iterador.hasNext) {
      val actualInst = iterador.next()
      val closestInst = closestInstance(s, actualInst)
      if (closestInst.label != actualInst.label) {
        return Left(actualInst)
      }
    }

    return Right(false)
  }

  /**
   * Calcula la instancia más cercana a un elemento dado.
   *
   * @param  s  Conjunto de datos sobre el que buscar el vecino más cercano.
   * @param  inst  Instancia a comparar
   * @return  Instancia más cercana
   */
  private def closestInstance(s: Iterable[LabeledPoint],
      inst: LabeledPoint): LabeledPoint = {

    var minDist = Double.MaxValue
    var closest: LabeledPoint = null
    var iterador = s.iterator
    while (iterador.hasNext) {
      var actualInstance = iterador.next()
      val actualDist = euclideanDistance(
          inst.features.toArray, actualInstance.features.toArray)
      if (actualDist < minDist) {
        minDist = actualDist
        closest = actualInstance
      }
    }

    return closest
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

}
