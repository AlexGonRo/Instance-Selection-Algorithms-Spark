package instanceSelection.abstr

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
 * Abstracción que incluye todos los métodos que deberán ser implementados por
 * un algoritmo de selección de instancias.
 *
 * Participa en el patrón de diseño "Strategy" actuando como estrategia
 * ("Strategy"). Las clases que hereden de esta interfaz participarán en este
 * mismo patrón como estrategias concretas("concrete strategy") y su
 * participante contexto podría ser alguna clase que herede de
 * [[launcher.execution.executionTraitExec]] (no necesariamente todas).
 * En la versión actual [[launcher.execution.ISClassExec]] y
 * [[launcher.execution.ISClassExecTest]] podrían actuar como contexto.
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
  def instSelection(data: RDD[LabeledPoint]): RDD[LabeledPoint]

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
   * Dado una cadena identificativa de un parámetro y un valor,
   * intenta asignar dicho valor al parámetro concreto.
   *
   * @param  identifier Identificador del parámetro en un comando de consola.
   * @param  value  Valor a asignar.
   *
   * @throws IllegalArgumentException Si alguno de los parámetros
   *   no es correcto: el identificador no existe o el valor no es el esperado.
   */
  @throws(classOf[IllegalArgumentException])
  protected def assignValToParam(identifier: String, value: String): Unit

}
