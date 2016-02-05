package utils

/**
 * Conjunto de divisores que, en un comando de ejecución, marcan el principio
 * de los argumentos para un determinado elemento del programa.
 *
 * Actualmente se cuentan con cuatro divisores.
 *
 */
object ArgsSeparator extends Enumeration {

  type ArgsDividers = Value
  /**
   * Indica el inicio de los valores destinados al lector de conjuntos de datos.
   */
  final val READER_SEPARATOR = Value("-r")
  /**
   * Indica el inicio de los valores destinados al filtro o selector de
   * instancias.
   */
  final val FILTER_SEPARATOR = Value("-f")
  /**
   * Indica el inicio de los valores destinados al clasificador.
   */
  final val CLASSIFIER_SEPARATOR = Value("-c")
  /**
   * Indica el inicio de los valores destinados a la validación cruzada
   */
  final val CROSSVALIDATION_SEPARATOR = Value("-cv")

}
