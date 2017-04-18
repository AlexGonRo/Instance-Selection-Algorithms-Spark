package instanceSelection.lshis

import java.util.Arrays
import java.util.Random

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint

/**
 * Table of hash functions that assign the same bucket to similar instances.
 * Its performing is supported on locality sensitive hashing (LSH).
 *
 * @param  numOfANDs Number of hash functions combined by AND operator.
 * @param  dim   Size of the hash functions. It also corresponds with the number
 *               of attributes of the instance.
 * @param  width Width of each bucket.
 * @param  seed  Seed for random generator.
 *
 * @author Alejandro González Rogel
 * @author Álvar Arnaiz-González
 * @version 1.2.0
 */
@SerialVersionUID(1L)
class ANDsTable(numOfANDs: Int,
                dim: Int,
                width: Double,
                seed: Long) extends Serializable {

  // Random generator for hashes.
  val rand = new Random(seed)

  // Creamos tantos objetos EuclideanHash como se requieran
  val ands = for { i <- 0 until numOfANDs }
    yield new EuclideanHash(dim, width, rand.nextLong())

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
   * @author Álvar Arnaiz-González
   * @version 1.2.0
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
    hash(inst.features.toArray)
  }

  /**
   * Computes the hash value for an instance.
   *
   * Generates a value by passing the instance through the whole set of
   * hash functions and combine all of them.
   *
   * @param  inst  Instance to hash.
   * @return hash value (bucket).
   */
  def hash(inst: Vector): Int = {
    hash(inst.toArray)
  }

  /**
   * Computes the hash value for an array of doubles.
   *
   * Generates a hash value by passing the array through the whole set of
   * hash functions and, finally, it combines all of them.
   *
   * @param  attr  Array of doubles to hash.
   * @return hash value (bucket).
   */
  def hash(attr: Array[Double]): Int = {

    // Compute the hash value of every single hash function.
    val hashValues = for { i <- 0 until ands.size }
      yield ands(i).hash(attr)

    // Finally all of hash values are combined to return a final hash code.
    Arrays.hashCode(hashValues.toArray)
  }

}
