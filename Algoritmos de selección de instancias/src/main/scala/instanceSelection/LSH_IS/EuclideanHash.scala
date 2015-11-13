package instanceSelection.LSH_IS

import scala.util.Random
import org.apache.spark.mllib.linalg.Vector

/**
 *
 * Vector de valores aleatorios destinado a calcular un valor hash
 * al recibir otro vector de dimensión similar.
 *
 * @param dim	Número de dimensiones que ha de tener el vector.
 * 										Corresponde también al número de dimensiones que tienen las
 * 										instancias del conjunto de datos que utilicemos.
 * @param width		Anchura de los "buckets".
 * @param seed		Semilla para calcular los valores aleatorios.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
@SerialVersionUID(1L)
class EuclideanHash(var dim: Int, var width: Double, seed: Long) extends Serializable {

  //Objeto destinado a crear números aleatorios.
  val rand = new Random(seed)

  //El desplazamiento que vamos a utilizar en el cálculo del valor hash.
  //Si width es menor a 1, se calcula el desplazamiento como doble.
  val offset = {
    if (width < 1.0)
      rand.nextInt(width.toInt * 10) / 10.0
    else
      rand.nextInt(width.toInt)
  }

  //Vector con valores aleatorios de una gaussiana con centro en 0 y 
  //desviación típica 1.
  val randomProjection = for (i <- 0 until dim) yield rand.nextGaussian()

  /**
   * Genera un valor al pasar un vector sobre una función Hash.
   *
   * @param features		Vector correspondiente a los atributos de una instancia.
   * @param Valor hash para el vector
   */
  def hash(features: Vector): Int = {

    var sum = 0.0;
    for (i <- 0 until features.size) {
      sum = sum + (randomProjection(i) * features(i))
    }

    var result = (sum + offset) / width

    return result.round.toInt
  }

}