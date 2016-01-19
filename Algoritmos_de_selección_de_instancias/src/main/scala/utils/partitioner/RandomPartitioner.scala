package utils.partitioner

import org.apache.spark.Partitioner
import java.util.Random

/**
 * Particionador completamente aleatorio un conjunto de datos en una RDD.
 *
 * Las posibles particiones que pudiesen generarse con esta clase no tienen
 * garantizado el mismo número de instancias entre ellas.
 *
 * @constructor Crea un nuevo generador de particiones aleatorio
 * @param  numPartitions  Número de particiones en las que subdividiremos el
 *   conjunto de datos.
 * @param  seed  Semilla para el generador de números aleatorios.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class RandomPartitioner(val numPartitions: Int, seed: Long) extends Partitioner {

  /**
   * Generador de números aleatorios.
   */
  val r = new Random(seed)

  /**
   * Asigna una partición de manera conpletamente aleatoria.
   *
   * @param key Llave de la instancia a reasignar
   * @param Identificador de la partición asignada
   */
  def getPartition(key: Any): Int =
    r.nextInt(numPartitions)

}

