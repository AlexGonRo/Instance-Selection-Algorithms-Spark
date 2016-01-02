package classification.seq.abstracts

import org.apache.spark.mllib.regression.LabeledPoint
import utils.Option

/**
 * Interfaz que define los métodos bñasicos que deberá contener un clasificador.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
abstract class TraitClassifier {
  
  /**
   * Dado un conjunto de entrenamiento realiza el entrenamiento del clasificador
   * en base a dicho conjunto
   *
   * @param  trainingSet	Conjunto de entrenamiento 
   */
  def train(trainingSet: Iterable[LabeledPoint]): Unit

  /**
   * Realiza la clasificación de una determinada instancia.
   *
   * @param inst  Instancia a clasificar.
   * @return Número correspondiente a la clase predicha por el clasificador.
   */
  def classify(inst: LabeledPoint): Double
  
  /**
   * Realiza la clasificación de un conjunto de instancias
   *
   * @param inst  Instancias a clasificar.
   * @return Número correspondiente a la clase predicha por el clasificador.
   */
  def classify(inst: Iterable[LabeledPoint]): Array[Double]
  
  /**
   * Devuelve un elemento iterable que contiene todas las opciones que ofrece
   * configurar el selector de instancias.
   * 
   * @return Listado de opciones que admite el el selector de instancias. 
   */
  def listOptions: Iterable[Option]
  
    /**
   * Dada una lista de parámetos, este método es capaz de analizarlos e inicializar
   * con ellos los valores iniciales del algoritmo.
   *
   * @param  args  Argumentos para inicializar el algoritmo.
   */
  def setParameters(args: Array[String]): Unit
  
  

}
