package utils.partitioner

import org.apache.spark.Partitioner
import scala.util.Random

/**
 * Particionador completamente aleatorio un conjunto de datos en una RDD.
 * 
 * @param numPartitions Número de particiones en las que subdividiremos el
 * 	conjunto de datos
 * @param seed Semilla para el generador de números aleatorios.
 */
class RandomPartitioner(val numPartitions: Int, seed: Long) extends Partitioner {

  val r = new Random(seed)

  def getPartition(key: Any): Int =
    r.nextInt(numPartitions)

} 
