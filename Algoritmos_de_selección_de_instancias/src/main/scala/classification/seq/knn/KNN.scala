package classification.seq.knn

import scala.collection.mutable.MutableList
import org.apache.spark.mllib.regression.LabeledPoint
import classification.seq.abstracts.TraitClassifier
import utils.Option
import java.util.logging.Logger
import java.util.logging.Level

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
class KNN extends TraitClassifier {

  private val bundleName = "strings.stringsKNN";
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);
  
  var k = 1
  var data:Iterable[LabeledPoint] = Iterable.empty[LabeledPoint]
  

  override def setParameters(args: Array[String]): Unit = {
        for (i <- 0 until args.size by 2) {
      args(i) match {
        case "-k" => k = args(i + 1).toInt
        case any =>
          logger.log(Level.SEVERE, "KNNWrongArgsError", any.toString())
          logger.log(Level.SEVERE, "KNNISPossibleArgs")
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
  
  override def train(trainingSet:Iterable[LabeledPoint]):Unit = {
    data = trainingSet
  }
  
  override def classify(inst: LabeledPoint): Double = {

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
  
    override def classify(instances: Iterable[LabeledPoint]): Array[Double] = {
      
      var result = Array.ofDim[Double](instances.size)
      val iter = instances.iterator
      var count = 0
      while(iter.hasNext){
        val inst = iter.next
        result(count) = classify(inst)
        count +=1
      }   
      result
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
  
    override def listOptions: Iterable[Option] = {
    val options: MutableList[Option] = MutableList.empty[Option]
    options += new Option("Vecinos", "Número de vecinos cercanos", "-k", k, 1)
    options
  } // end listOptions

}
