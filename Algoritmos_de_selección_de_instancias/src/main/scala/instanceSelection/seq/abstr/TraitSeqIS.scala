package instanceSelection.seq.abstr

import org.apache.spark.mllib.regression.LabeledPoint

/**
 * Interfaz que proporciona todos los métodos requeridos para la implementación
 * de algoritmos lineales de selección de instancias.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */

@SerialVersionUID(1L)
trait TraitSeqIS extends Serializable {

  /**
   *
   * Dado un conjunto inicial, devuelve un conjunto de tamaño igual o menor
   * resultante de aplicar el algoritmo de selección de instancias.
   *
   * @param  data  Conjunto inicial.
   * @return  Conjunto resultante tras aplicar el algoritmo.
   *
   * @throws IllegalArgumentException Si alguno de los parámetros introducidos
   *   no es correcto.
   */
  @throws(classOf[IllegalArgumentException])
  def instSelection(data: Iterable[LabeledPoint]): Iterable[LabeledPoint]

  /**
   * Dada una lista de parametros, este método es capaz de analizarlos y
   * modificar los atributos del algoritmo de acuerdo a esos datos.
   *
   * @param  args  Argumentos para inicializar el algoritmo.
   */
  def setParameters(args: Array[String]): Unit

}
