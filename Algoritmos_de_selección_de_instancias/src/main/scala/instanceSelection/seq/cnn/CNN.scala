package instanceSelection.seq.cnn

import scala.collection.mutable.MutableList

import org.apache.spark.mllib.regression.LabeledPoint

import instanceSelection.seq.abstracts.LinearISTrait
import utils.Option

/**
 * Algoritmo de selección de instancias Condensed Nearest Neighbor (CNN)
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
class CNN extends LinearISTrait {

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
    while (iter.hasNext) {
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
        return Left(actualInst)
      }

    }

    // Fin del algoritmo
    return Right(false)
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
      val actualDist = euclideanDistance(
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
   * Calcula la distancia euclidea entre dos vectores de datos numéricos.
   *
   * El cálculo de esta distancia no es completo, se suprime la operación de la
   * raiz cuadrada con la intención de ahorrar operaciones.
   *
   * @param point1  Un punto de la medición.
   * @param point2  Segundo punto.
   */
  private def euclideanDistance(point1: Array[Double],
                                point2: Array[Double]): Double = {

    var dist = 0.0
    for { i <- 0 until point1.size } {
      dist += Math.pow((point1(i) - point2(i)), 2)
    }

    dist
  }

  /**
   * Devuelve un elemento iterable que contiene todas las opciones que ofrece
   * configurar el selector de instancias.
   *
   * @return Listado de opciones que admite el el selector de instancias.
   */
  override def listOptions: Iterable[Option] = {
    MutableList.empty[Option]
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
