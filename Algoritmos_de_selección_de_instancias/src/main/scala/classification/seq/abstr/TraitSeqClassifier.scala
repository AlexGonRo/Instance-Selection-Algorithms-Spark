package classification.seq.abstr

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint

/**
 * Interfaz que define los métodos básicos que deberá contener un clasificador
 * secuencial.
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
trait TraitSeqClassifier {

  /**
   * Dado un conjunto de entrenamiento realiza el entrenamiento del clasificador
   * en base a dicho conjunto.
   *
   * @param  trainingSet  Conjunto de entrenamiento.
   */
  def train(trainingSet: Iterable[LabeledPoint]): Unit

  /**
   * Realiza la clasificación de una determinada instancia.
   *
   * @param inst  Instancia a clasificar.
   * @return Número correspondiente a la clase predicha por el clasificador.
   */
  def classify(inst: Vector): Double

  /**
   * Realiza la clasificación de un conjunto de instancias.
   *
   * @param instances  Instancias a clasificar.
   * @return Número correspondiente a la clase predicha por el clasificador.
   */
  def classify(instances: Iterable[Vector]): Array[Double]

  /**
   * Dada una lista de parámetos, este método es capaz de analizarlos e inicializar
   * con ellos los valores iniciales del algoritmo.
   *
   * @param  args  Argumentos para inicializar el algoritmo.
   */
  def setParameters(args: Array[String]): Unit

}
