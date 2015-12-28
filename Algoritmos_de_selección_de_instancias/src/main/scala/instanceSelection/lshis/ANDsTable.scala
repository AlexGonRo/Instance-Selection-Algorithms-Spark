package instanceSelection.lshis

import scala.util.Random
import org.apache.spark.mllib.regression.LabeledPoint
import java.util.Arrays

/**
 *
 * Conjunto de funciones hash [[EuclideanHash]].
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
private class ANDsTable(var numOfANDs: Int,
                        var dim: Int,
                        var width: Double,
                        seed: Long) extends Serializable {

  // Creamos tantos objetos EuclideanHash como se requieran
    val ands = for (i <- 0 until numOfANDs)
      yield new EuclideanHash(dim, width, seed + i)

    
     
  
  /**
   *
   * Clase que contiene una función hash basada en la distancia euclidea entre
   * los puntos de dos vectores.
   *
   * @param  dim  Número de dimensiones del vector.
   *   Corresponde al número de dimensiones que tienen las
   *   instancias del conjunto de datos que utilicemos.
   * @param  width  Anchura de los "buckets".
   * @param  seed  Semilla para calcular los valores aleatorios.
   *
   * @author Alejandro González Rogel
   * @version 1.1.0
   */
  @SerialVersionUID(1L)
  class EuclideanHash(var dim: Int,
                      var width: Double,
                      seed: Long) extends Serializable {

    // Generador de números aleatorios.
    val rand = new Random(seed)

    // El desplazamiento para el cálculo del valor hash.
    val offset = {
      if (width < 1.0)
        rand.nextInt((width * 10).toInt) / 10.0
      else
        rand.nextInt(width.toInt)
    }

    // Vector con valores aleatorios
    val randomProjection = for (i <- 0 until dim) yield rand.nextGaussian()

    /**
     * Genera un valor entero al pasar un vector sobre esta función Hash.
     *
     * @param attr  Vector correspondiente a los atributos de una instancia.
     *   Incluye atributo de clase.
     * @param Valor hash para el vector
     */
    def hash(attr: Array[Double]): Int = {

      var sum = 0.0;
      for (i <- 0 until attr.size) {
        sum += (randomProjection(i) * attr(i))
      }

      var result = (sum + offset) / width

      result.round.toInt
    }

  }

  /**
   * Genera un valor al pasar un vector sobre todas las funciones [[EuclideanHash]]
   * almacenadas.
   *
   * @param  int  Instancia sobre la que calcula el hash
   * @param Valor hash para el vector
   */
  def hash(inst: LabeledPoint): Int = {

    val attr = inst.features.toArray :+ inst.label
    // Calculamos todos los valores resultantes de pasar el vector por cada una
    // de las funciones hash (ANDs) que guarda el objeto.
    val hashValues = for (i <- 0 until ands.size)
      yield ands(i).hash(attr)

    Arrays.hashCode(hashValues.toArray)
  }

}
