
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

/**
 * Realiza la lectura de dos ficheros y 
 * almacena todas las veces que la palabra "Spark" aparece en ellos.
 * Posteriormente junta el resultado obtenido de cada fichero para 
 * mostrar el número total de veces que la palabra aparece en los textos.
 * 
 * @autor Alejandro González Rogel
 * @version 0.1
 */

object joiningExercise {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val readme = sc.textFile("./resources/README.md")
    val readme_wc = readme.flatMap(l => l.split(" ")).filter(_ == "Spark").map(word => (word, 1)).reduceByKey(_ + _)

    val changes = sc.textFile("./resources/CHANGES.txt")
    val changes_wc = changes.flatMap(l => l.split(" ")).filter(_ == "Spark").map(word => (word, 1)).reduceByKey(_ + _)

    val resultado = readme_wc.join(changes_wc).collect()

    println(resultado(0))

  }

}