package utils.partitioner

import java.util.Random

import org.apache.spark.Partitioner

/**
 * Particionador completamente aleatorio de los objetos de una estructura
 * [[org.apache.spark.rdd.RDD]].
 *
 * Las posibles particiones que pudiesen generarse con esta clase no tienen
 * garantizado el mismo número de instancias entre ellas.
 *
 * @constructor Crea un nuevo generador de particiones aleatorio.
 * @param  numPartitions  Número de particiones en las que subdividiremos el
 *   conjunto de datos.
 * @param  totalInst Número total de instancias en la estructura a particionar
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class RandomPartitioner(val numPartitions: Int, val totalInst:Long, val seed:Long) extends Partitioner {

  /**
   * Repetición actual
   */
  var rep = 0

  /**
   * Asigna una partición a una instancia de manera aleatoria.
   *
   * @param key Llave de la instancia a reasignar
   * @return Identificador de la partición asignada
   */
  def getPartition(key: Any): Int = {
    val k = key.asInstanceOf[Long]
    val finalSeed = seed + k + (rep*totalInst)
    val r = new Random(finalSeed)
    r.nextInt(numPartitions)
  }
}

