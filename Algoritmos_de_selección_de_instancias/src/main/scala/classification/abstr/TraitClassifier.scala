package classification.abstr

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
 * Interfaz que define los métodos básicos que deberá contener un clasificador.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
trait TraitClassifier {

  /**
   * Dado un conjunto de entrenamiento realiza el entrenamiento del clasificador
   * en base a dicho conjunto.
   *
   * @param  trainingSet  Conjunto de entrenamiento.
   */
  def train(trainingSet: RDD[LabeledPoint]): Unit


  /**
   * Realiza la clasificación de un conjunto de instancias.
   *
   * @param instances  Identificador único de cada instancia junto con la instancias 
   *     a clasificar.
   * @return Número correspondiente a la clase predicha por el clasificador.
   */
  def classify(instances: RDD[(Long,Vector)]): RDD[(Long,Double)]

  /**
   * Dada una lista de parámetos, este método es capaz de analizarlos e inicializar
   * con ellos los valores iniciales del algoritmo.
   *
   * @param  args  Argumentos para inicializar el algoritmo.
   */
  def setParameters(args: Array[String]): Unit

}
