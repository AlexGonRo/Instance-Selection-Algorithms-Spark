package instanceSelection.Abstract

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint

/**
 * Abstracción que incluye todos los métodos que deberán ser implementados por
 * un algoritmo de selección de instancias para funcionar bajo la clase principal
 * definida en [[main.main]]
 *
 * @param args	Argumentos destinados a dar un valor inicial a
 * 							parámetros del algoritmo.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
trait AbstractIS {

  /**
   *
   * Dado un conjunto de datos inicial devuelve un conjunto de tamaño igual (en el
   * peor de los casos) o menor, resultante de aplicar el algoritmo de selección
   * de instancias.
   *
   * @param		sc	Contexto Spark en el que se ejecuta la aplicación.
   * @param		parsedData	Conjunto inicial.
   * @return	Conjunto resultante tras aplicar el algoritmo.
   */
  def instSelection(sc: SparkContext, parsedData: RDD[LabeledPoint]): RDD[LabeledPoint]

  /**
   * Dado un conjunto de datos, este método es capaz de analizarlos e inicializar
   * con ellos los valores iniciales del algoritmo.
   *
   * @param args	Argumentos para inicializar el algoritmo.
   */
  def readArgs(args: Array[String])

  /**
   * Imprime una serie de mensajes de error indicando el correcto
   * formato de los argumentos de entrada en la instanciación de la clase.
   *
   */
  def printWrongArgsError()

}
