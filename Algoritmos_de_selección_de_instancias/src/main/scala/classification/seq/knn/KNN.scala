package classification.seq.knn

import java.util.logging.Level
import java.util.logging.Logger

import scala.collection.mutable.MutableList

import org.apache.spark.mllib.regression.LabeledPoint

import classification.seq.abstr.TraitSeqClassifier
import utils.DistCalculator

/**
 * Clasificador KNN.
 *
 * Este algoritmo de clasificación basa sus predicciones en las distancias
 * entre la instancia a clasificar y el resto del conjunto de datos, siendo
 * las instancias más próximas a la que nos interesa las que tendremos en cuenta
 * a la hora de predecir una clasificación.
 *
 * Participante en el patrón de diseño "Strategy" en el que actúa con el
 * rol de estrategia concreta ("concrete strategies"). Hereda de la clase que
 * participa como estrategia ("Strategy")
 * [[classification.seq.abstr.TraitSeqClassifier]].
 *
 * @constructor Genera un nuevo clasificador con los atributos por defecto
 *   y sin entrenar.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class KNN extends TraitSeqClassifier {

  /**
   * Ruta donde se encuentran las cadenas a mostrar por el logger.
   */
  private val bundleName = "resources.loggerStrings.stringsKNN";
  /**
   * Logger del clasificador.
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);
  /**
   * Calculadora de distancias entre puntos.
   */
  val distCalc = new DistCalculator

  /**
   * Número de vecinos cercanos.
   */
  var k = 1

  /**
   * Conjunto de datos almacenado tras la etapa de entrenamiento.
   */
  var trainingData: Iterable[LabeledPoint] = Iterable.empty[LabeledPoint]

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
          case "-k" => k = args(i + 1).toInt
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
    if (k <= 0) {
      logger.log(Level.SEVERE, "KNNWrongArgsValuesError")
      logger.log(Level.SEVERE, "KNNPossibleArgs")
      throw new IllegalArgumentException()
    }
  }

  override def train(trainingSet: Iterable[LabeledPoint]): Unit = {
    trainingData = trainingSet
  }

  override def classify(inst: LabeledPoint): Double = {

    // Calculamos la distancia a cada una de las instancias del conjunto de
    // datos
    val distances = for { actualInst <- trainingData }
      yield (actualInst.label, distCalc.euclideanDistance(
      inst.features.toArray, actualInst.features.toArray))

    // Almacenamos las K instancias más cercanas
    var closest: MutableList[(Double, Double)] =
      knearestInstances(inst, distances)

    // Calculamos cuál es la clase predominante y la devolvemos.
    val classification = closest.groupBy(t => t._1).maxBy(t => t._2.length)

    classification._1
  }

  override def classify(instances: Iterable[LabeledPoint]): Array[Double] = {

    var result = Array.ofDim[Double](instances.size)
    val iter = instances.iterator
    var count = 0
    while (iter.hasNext) {
      val inst = iter.next
      result(count) = classify(inst)
      count += 1
    }
    result
  }

  /**
   * Calcula aquellas instancias más cercanas a aquella que nos interesa.
   *
   * @param  inst Instancia a la que encontrar los vecinos más cercanos
   * @param  distances  Conjunto de clase-distancia a cada elemento del conjunto
   * @return Conjunto de K elementos con clase-distancia al vecino más cercano
   */
  private def knearestInstances(inst: LabeledPoint,
                                distances: Iterable[(Double, Double)]):
                                MutableList[(Double, Double)] = {

    var closest: MutableList[(Double, Double)] = MutableList.empty
    var iter = distances.iterator
    closest += iter.next()

    // Recorremos todas las distancias a las diferentes instancias
    while (iter.hasNext) {
      var actualInst = iter.next
      // Si no tenemos todavía K vecinos almacenados
      if (closest.size < k) {
        closest += actualInst
      } else {
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
