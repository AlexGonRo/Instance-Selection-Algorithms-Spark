package instanceSelection.lshis

import java.util.Arrays
import java.util.Random

import org.apache.spark.mllib.regression.LabeledPoint

/**
 *
 * Conjunto de funciones hash que permite asignar valores hash parecidos a
 * instancias similares.
 *
 * @param  numOfHashes  Número de vectores aleatorios que contendrá este conjunto.
 * @param  dim  Número de dimensiones que ha de tener el vector.
 *   Corresponde también al número de dimensiones que tienen las
 *   instancias del conjunto de datos que utilicemos.
 * @param  width  Anchura de los "buckets".
 * @param  seed  Semilla para calcular los valores aleatorios.
 *
 * @author Alejandro González Rogel
 * @version 1.1.0
 */
@SerialVersionUID(1L)
private class ANDsTable(numOfANDs: Int,
                        dim: Int,
                        width: Double,
                        seed: Long) extends Serializable {

  // Creamos tantos objetos EuclideanHash como se requieran
  val ands = for { i <- 0 until numOfANDs }
    yield new EuclideanHash(dim, width, seed + i)

  /**
   *
   * Función hash basada en la distancia euclidea entre
   * los puntos de dos vectores.
   *
   * @param  dim  Número de dimensiones del vector.
   *   Corresponde al número de dimensiones que tienen las
   *   instancias del conjunto de datos que utilicemos.
   * @param  width  Anchura de los "buckets".
   * @param  seed  Semilla para calcular los valores aleatorios.
   *
   * @constructor Genera un nuevo vector de valores aleatorios, junto con un
   *   desplazamiento también aleatorio.
   * @author Alejandro González Rogel
   * @version 1.1.0
   */
  @SerialVersionUID(1L)
  class EuclideanHash(dim: Int,
                      width: Double,
                      seed: Long) extends Serializable {

    /**
     * Generador de números aleatorios
     */
    private val rand = new Random(seed)

    /**
     * Desplazamiento para el cálculo del valor hash.
     */
    private val offset = {
      if (width < 1.0) {
        rand.nextInt((width * 10).toInt) / 10.0
      } else {
        rand.nextInt(width.toInt)
      }
    }

    /**
     * Vector con valores aleatorios
     */
    val randomProjection = for { i <- 0 until dim } yield rand.nextGaussian()

    /**
     * Genera un valor entero al pasar un vector sobre esta función Hash.
     *
     * @param attr  Vector correspondiente a los atributos de una instancia.
     *   Incluye atributo de clase.
     * @param Valor hash para el vector
     */
    def hash(attr: Array[Double]): Int = {

      var sum = 0.0;
      for { i <- 0 until attr.size } {
        sum += (randomProjection(i) * attr(i))
      }

      var result = (sum + offset) / width

      result.round.toInt
    }

  }

  /**
   *
   * Calcula el valor hash para una determinada instancia.
   *
   * Genera un valor al pasar un vector sobre todas las funciones hash
   * almacenadas y, vuelve a aplicar una función hash sobre los resultados
   * para obtener el valor final.
   *
   * @param  int  Instancia sobre la que calcula el hash
   * @param Valor hash para el vector
   */
  def hash(inst: LabeledPoint): Int = {

    val attr = inst.features.toArray
    // Calculamos todos los valores resultantes de pasar el vector por cada una
    // de las funciones hash (ANDs) que guarda el objeto.
    val hashValues = for { i <- 0 until ands.size }
      yield ands(i).hash(attr)

    Arrays.hashCode(hashValues.toArray)
  }

}
