package instanceSelection.seq.abstracts

import org.apache.spark.mllib.regression.LabeledPoint

/**
 * Interfaz que proporciona todos los métodos requeridos para la implementación
 * de algoritmos lineales de selección de instancias.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */

@SerialVersionUID(1L)
trait LinearISTrait extends Serializable{

  /**
   *
   * Dado un conjunto inicial, devuelve un conjunto de tamaño igual o menor
   * resultante de aplicar el algoritmo de selección de instancias.
   *
   * @param  data  Conjunto inicial.
   * @return  Conjunto resultante tras aplicar el algoritmo.
   */
  def instSelection(data: Iterable[LabeledPoint]): Iterable[LabeledPoint]
}
