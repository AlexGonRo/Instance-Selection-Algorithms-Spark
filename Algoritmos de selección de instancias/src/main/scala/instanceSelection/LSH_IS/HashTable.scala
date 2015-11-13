package instanceSelection.LSH_IS

import org.apache.spark.mllib.linalg.Vector

/**
 *
 * Conjunto de vectores aleatorios con los que calcular un valor hash.
 *
 * @param numOfHashes	Número de vectores aleatorios que contendrá este conjunto.
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
class HashTable(numOfHashes: Int, dim: Int, width: Double, seed: Long) extends Serializable {

  //Creamos tantos objetos EuclideanHash como se requieran
  val hashFunctions = for (i <- 0 until numOfHashes)
    yield new EuclideanHash(dim, width, seed)

  /**
   * Genera un valor al pasar un vector sobre una función Hash.
   *
   * @param features		Vector correspondiente a los atributos de una instancia.
   * @param Valor hash para el vector
   */
  def hash(features: Vector): Int = {

    //Calculamos todos los valores resultantes de pasar el vector por cada una
    //de las funciones hash que hemos generado al inicializar este objeto.
    val hashValues = for (i <- 0 until hashFunctions.size) 
      yield hashFunctions(i).hash(features)

    //Calculamos el valor hash producido al pasar los datos obtenidos 
    //anteriormente por la función HashCode de la clase IndexedSeq

    return hashValues.hashCode()

  }

}