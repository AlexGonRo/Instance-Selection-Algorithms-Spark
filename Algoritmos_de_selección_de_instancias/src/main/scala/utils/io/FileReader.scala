package utils.io

import scala.collection.mutable.ArrayBuffer
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import java.util.logging.Logger
import java.util.logging.Level

// TODO Manera de leer valores nominales. Posible aproximación con StringIndexer

/**
 * Proporciona métodos para poder realizar la lectura de conjuntos de datos
 * almacenados en ficheros.
 *
 * @version 1.0.0
 * @author Alejandro González Rogel
 */
class FileReader {

  // Logger
  private val bundleName = "strings.stringsUtils";
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  // Atributo de clase es el primero
  var first = false
  // El fichero contiene cabecera
  var header = false
  // Número de lineas que tiene la cabecera.
  var headerLines = 0

  /**
   * Realiza la lectura de un fichero en formato CSV (con atributos separados por
   * comas) y genera una RDD con ellos.
   *
   * @param  sc  Contexto Spark
   * @param  args  Opciones para la lectura del fichero. El primer argumento ha de
   * ser el directorio donde se almacena el fichero a leer.
   * @return  RDD generada
   */
  def readCSV(sc: SparkContext, args: Array[String]): RDD[LabeledPoint] = {

    val path = args(0)

    readCSVParam(args.drop(1))

    var data = sc.textFile(path)

    // En el caso de que el fichero contenga una cabezera lo eliminamos
    if (header) {
      data = data.zipWithIndex().filter(_._2 >= headerLines).map(_._1)
    }

    // Transformación sobre la RDD para almacenar las instancias en LabeledPoints
    if (!first) {
      // Si el atributo de clase es el último
      data.map { line =>
        val features = line.split(',')
        LabeledPoint(features.last.toDouble,
          Vectors.dense(features.dropRight(1).map(_.toDouble)))
      }
    } else {
      // Si el atributo de clase es el primero
      data.map { line =>
        val features = line.split(',')
        LabeledPoint(features(0).toDouble, Vectors.dense(features.tail
          .map(_.toDouble)))
      }

    }

  }

  /**
   * Lee una serie de argumentos para actualizar los valores de los atributos de la
   * clase.
   *
   * @param  args  Serie de argumentos
   */
  private def readCSVParam(args: Array[String]): Unit = {
    var readingHL = false
    var it = args.iterator

    while (it.hasNext) { // Por cada argumento
      it.next() match {
        case "-f" => first = true
        case "-hl" => {
          header = true
          try {
            headerLines = it.next.toInt
          } catch {
            // Si el siguiente parámetro no es numérico o directamente no existe
            case e @ (_: IllegalStateException | _: NumberFormatException) =>
              logger.log(Level.SEVERE, "FileReaderWrongArgsCSVError")
              logger.log(Level.SEVERE, "FileReaderPossibleArgsCSV")
              throw new IllegalArgumentException("Wrong parameter format when" +
                "trying to read the dataset")
          }
        }
        case _ =>
          logger.log(Level.SEVERE, "FileReaderWrongArgsCSVError")
          logger.log(Level.SEVERE, "FileReaderPossibleArgsCSV")
          throw new IllegalArgumentException("Wrong parameter format when trying" +
            "to read the dataset")
      }

    }
  }

  /**
   * Dada una cadena de argumentos inicial, la subdivide en dos: una que contiene
   * los argumentos para el selector de instancias y otra que contiene el resto de
   * argumentos.
   *
   * La cadena inicial ha de contener los argumentos ordenados de la siguiente
   * manera: argumentosLector restoArgumentos.
   *
   * @param  args  Cadena de argumentos inicial.
   * @return  Dos cadenas de argumentos.
   */
  def divideArgs(args: Array[String]): (Array[String], Array[String]) = {

    var otherArgs: ArrayBuffer[String] = ArrayBuffer.empty[String]
    var readerArgs: ArrayBuffer[String] = ArrayBuffer.empty[String]
    readerArgs += args(1)

    var takeNext = false
    var weDone = false
    for (i <- 2 until args.size) {
      if (weDone) {
        otherArgs += args(i)
      } else if (takeNext) {
        readerArgs += args(i)
        takeNext = false
      } else if (args(i) == "-hl") {
        readerArgs += args(i)
        takeNext = true
      } else if (args(i) == "-f") {
        readerArgs += args(i)
      } else {
        otherArgs += args(i)
        weDone = true
      }

    }

    (otherArgs.toArray, readerArgs.toArray)
  }

}
