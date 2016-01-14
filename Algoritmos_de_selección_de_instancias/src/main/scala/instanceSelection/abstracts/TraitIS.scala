package instanceSelection.abstracts

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

import utils.Option

/**
 * Abstracción que incluye todos los métodos que deberán ser implementados por
 * un algoritmo de selección de instancias.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
trait TraitIS {

  /**
   *
   * Dado un conjunto de datos inicial devuelve un conjunto de tamaño igual (en el
   * peor de los casos) o menor, resultante de aplicar el algoritmo de selección
   * de instancias.
   *
   * @param  sc  Contexto Spark en el que se ejecuta la aplicación.
   * @param  data  Conjunto inicial.
   * @return  Conjunto resultante tras aplicar el algoritmo.
   */
  def instSelection(sc: SparkContext, data: RDD[LabeledPoint]): RDD[LabeledPoint]

  /**
   * Dada una lista de parámetros, este método es capaz de analizarlos e inicializar
   * con ellos los valores iniciales del algoritmo.
   *
   * @param  args  Argumentos para inicializar el algoritmo.
   * @throws IllegalArgumentException Si alguno de los parámetros introducidos
   *   no es correcto.
   */
  @throws(classOf[IllegalArgumentException])
  def setParameters(args: Array[String]): Unit

  /**
   * Devuelve un elemento iterable que contiene todas las opciones del algoritmo
   * que pueden configurarse.
   *
   * @return Listado de opciones que admite el el selector de instancias.
   */
  def listOptions: Iterable[Option]

}
