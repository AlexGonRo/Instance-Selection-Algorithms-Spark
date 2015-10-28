package lanzador.clasificador

import scala.io.StdIn
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

/**
 *
 * Objeto encargado de realizar todas las tareas necesarias para clasificar
 * un conjunto de datos con el método de clasificación Naive Bayes ya
 * implementado en Spark.
 *
 * Su labor es la de leer un conjunto de datos de un fichero(creando la estructura
 * RDD correspondiente), entrenar y evaluar el método Naive Bayes mediante una
 * validación cruzada de 10 iteraciones, para lo que previamente hemos
 * necesitado crear los diferentes pliegues(folds), y mostrar por pantalla
 * la media de acierto.
 *
 * @author Alejandro González Rogel
 * @version 1.0
 */

object lanzadorNaiveBayes {

  /**
   * Clase principal y única del objeto que contiene toda la lógica del mismo.
   *
   * @param args Argumentos de entrada en la invocación de la aplicación.
   * 								Posición 0 (Obligatorio) Ruta al archivo que contiene el
   * conjunto de datos.
   * 								Posición 1 (Opcional)	Semilla a utilizar para generar
   * los folds aleatoriamente. Por defecto este valor es 1.
   * 								Posición 2 (Opcional) Número de pliegues para la validación
   * cruzada. El valor por defecto es 10.
   * 								Posición 3 (Opcional) Indica si el atributo de clase
   * es el primero de cada instancia, para lo que habría que escribir
   * la palabra "first". Por defecto, el atributo de clase es el último de todos.
   */
  def main(args: Array[String]) {

    //La configuración la pasamos a la hora de invocar por consola la aplicación.
    //Utilizar estas lineas solo para pruebas directamente en Eclipse
    //val conf = new SparkConf().setAppName("Lanzador").setMaster("local[2]")
    //val sc = new SparkContext(conf)

    val sc = new SparkContext(new SparkConf())

    //Detenemos la ejecución del programa hasta que se indique.
    println("PULSE ENTER PARA CONTINUAR")
    StdIn.readLine()

    //Anotamos el tiempo en el que iniciamos las operaciones que deseamos medir.
    val start = System.currentTimeMillis

    //Asignamos un valor a la semilla que más adelante utilizaremos
    // para crear los conjuntos de validación cruzada
    val seed = 1 //Valor por defecto
    //En caso de un nuevo valor
    if (args.size >= 2) {
      val seed = args(1)
    }

    //Misma operación con el número de folds a usar en la validación cruzada.
    val numFolds = 10 //Valor por defecto
    //En caso de un nuevo valor
    if (args.size >= 3) {
      val numFolds = args(2)
    }

    //Leemos del fichero que contiene el conjunto de datos y 
    //almacenamos sus datos.
    val data = sc.textFile(args(0))

    //Leemos linea por linea y vamos generando cada instancia
    //(par de atributos-atributo de clase).
    val parsedData = {
      if (args.size >= 3 && args(2) == "first") {
        //Si el atributo de clase es el primero
        data.map { line =>
          val parts = line.split(',')
          LabeledPoint(parts(0).toDouble, Vectors.dense(parts.tail.map(_.toDouble)))
        }
      } else {
        //Si el atributo de clase es el último
        data.map { line =>
          val parts = line.split(',')
          LabeledPoint(parts.last.toDouble, Vectors.dense(parts.dropRight(1).map(_.toDouble)))
        }
      }

    }

    //Creamos las particiones para la validación cruzada
    val cvfolds = MLUtils.kFold(parsedData, numFolds, seed)

    //Almacenamos en un array el porcentaje de acierto del algoritmo en cada
    //una de las particiones.
    var foldResult = cvfolds.map {
      //Por cada par de entrenamiento-test
      case (training, test) => {
        //Entrenamos
        val model = NaiveBayes.train(training)
        //Clasificamos el test
        val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
        //Comprobamos que porcentaje de aciertos hemos tenido.
        predictionAndLabel.filter(x => x._1 == x._2).count().toDouble / test.count().toDouble
      }
    }

    //Calculamos y mostramos la precisión media del algoritmo
    val sumaPorcentajesAcierto = foldResult.reduceLeft { _ + _ }
    val finalResult = sumaPorcentajesAcierto / foldResult.size
    println("La media de acierto en test es: " + finalResult)
    //Mostramos el tiempo de ejecución que ha sido necesario para realizar
    //la prueba.
    val tiempo = System.currentTimeMillis - start
    println("El tiempo empleado es " + tiempo)


  }

}