package instanceSelection.seq.cnn

import scala.Left
import scala.Right
import scala.collection.mutable.MutableList

import org.apache.spark.mllib.regression.LabeledPoint

import instanceSelection.seq.abstr.TraitSeqIS
import utils.DistCalculator

/**
 * Condensed Nearest Neighbor (CNN) instance selection algorithm.
 *
 * CNN construye un conjunto resultado S de un conjunto original T
 * tal que todo ejemplo de T está más cerca a un ejemplo de S de la misma
 * clase que a otro de S de clase distinta.
 *
 * @constructor Genera un nuevo selector de instancias basado en el algoritmo.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class CNN extends TraitSeqIS {

  /**
   * Calculadora de distancias entre puntos.
   */
  val distCalc = new DistCalculator

  def instSelection(data: Iterable[LabeledPoint]): Iterable[LabeledPoint] = {

    var s = new MutableList[LabeledPoint]

    // Comenzamos el conjunto S con únicamente una instancia
    s += data.head

    // Incrementamos iterativamente el conjunto S en base a la aplicación
    // del algoritmo
    var keepGoing = true
    while (keepGoing) {
      doTheLogic(s, data) match {
        case Left(inst) => s += inst
        case Right(_)   => keepGoing = false
      }

    }

    s

  }

  /**
   * Dados dos conjuntos de datos, recorre el segundo asegurandose de que toda
   * instancia existente en él posee un vecino más cercano de la misma clase
   * en el primer conjunto. En caso de no ser así, devuelve la instancia donde
   * no se ha complido la premisa
   *
   * @param  s  Subconjunto de data
   * @param  data Conjunto inicial de datos
   * @return Instancia a añadir en el subconjunto S o el booleano "false" para
   *   indicar la finalización del algoritmo
   */
  private def doTheLogic(
    s: Iterable[LabeledPoint],
    data: Iterable[LabeledPoint]): Either[LabeledPoint, Boolean] = {

    var iter = data.iterator
    var nextInstanceFound = false
    var nextInstance: LabeledPoint = null

    while (iter.hasNext && !nextInstanceFound) {
      val actualInst = iter.next()
      // Calculamos la instancia o instancias más cercanas
      val closestInst = closestInstances(s, actualInst)

      // Comprobamos si alguna de las instancias más cercanas es de la misma
      // clase
      var addToS = true
      for { instance <- closestInst } {
        if (instance.label == actualInst.label) {
          addToS = false
        }
      }

      // Si no hay instancia cercana de la misma clase, devolvemos la
      // instancia problemática
      if (addToS) {
        nextInstanceFound = true
        nextInstance = actualInst
      }
    }

    if (nextInstanceFound) {
      Left(nextInstance)
    } else {
      // Fin del algoritmo
      Right(false)

    }

  }

  /**
   * Calcula la instancia más cercana a un elemento dado.
   *
   * @param  s  Conjunto de datos sobre el que buscar el vecino más cercano.
   * @param  inst  Instancia a comparar
   * @return  Instancia más cercana
   */
  private def closestInstances(s: Iterable[LabeledPoint],
                               inst: LabeledPoint): Iterable[LabeledPoint] = {

    var minDist = Double.MaxValue
    var closest: MutableList[LabeledPoint] = MutableList.empty[LabeledPoint]
    var iterador = s.iterator
    while (iterador.hasNext) {
      var actualInstance = iterador.next()
      val actualDist = distCalc.euclideanDistance(
        inst.features.toArray, actualInstance.features.toArray)
      if (actualDist == minDist) {
        closest += actualInstance
      } else if (actualDist < minDist) {
        minDist = actualDist
        closest = MutableList.empty[LabeledPoint]
        closest += actualInstance
      }
    }

    closest
  }

  /**
   *
   * CNN no necesita ningún parámetro para su funcionamiento.
   *
   * Método vacío.
   *
   * @param args Cadena de texto con argumentos.
   */
  override def setParameters(args: Array[String]): Unit = {
  }

}
