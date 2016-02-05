package utils.io

import java.util.logging.Level
import java.util.logging.Logger

import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
 * Proporciona métodos para la lectura de conjuntos de datos almacenados
 * en ficheros y realiza su conversión a estructuras
 * [[org.apache.spark.rdd.RDD]].
 *
 *
 * @constructor Genera un nuevo lector de ficheros
 *
 * @version 1.0.0
 * @author Alejandro González Rogel
 */
class FileReader {

  /**
   * Ruta al fichero que almacena los mensajes de log.
   */
  private val bundleName = "resources.loggerStrings.stringsUtils";
  /**
   * Logger.
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  /**
   * Indica si el atributo de clase es el primero de los atributos de una
   * instancia.
   *
   * Por defecto, el atributo de clase es el último de los atributos.
   */
  var first = false

  /**
   * Indica si el fichero contiene unas lineas descriptivas antes de los datos.
   *
   * Por defecto, se asume que un fichero no tiene cabecera.
   */
  var header = false

  /**
   * Número de lineas que tiene la cabecera.
   */
  var headerLines = 0

  /**
   * Realiza la lectura de un fichero en formato CSV y genera una RDD con ellos.
   *
   * Los atributos han de estar separados por comas.
   *
   * @param  sc  Contexto Spark.
   * @param  filePath  Ruta al fichero.
   * @return  RDD generada.
   */
  def readCSV(sc: SparkContext, filePath: String): RDD[LabeledPoint] = {

    var data = sc.textFile(filePath)

    // En el caso de que el fichero contenga una cabezera lo eliminamos
    if (header) {
      val broadcastVar = sc.broadcast(headerLines)
      data = data.zipWithIndex().filter(_._2 >= broadcastVar.value).map(_._1)
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
   * Lee una cadena de argumentos para actualizar los valores de los
   * atributos de la clase.
   *
   * Este método está pensado para actualizar aquellos atributos relacionados
   * con la lectura de ficheros en formato CSV.
   *
   * @param  args  Serie de argumentos.
   * @throws IllegalArgumentException Si alguno de los parámetros introducidos
   *   no es correcto.
   */
  @throws(classOf[IllegalArgumentException])
  def setCSVParam(args: Array[String]): Unit = {
    var readingHL = false

    for { i <- 0 until args.size } { // Por cada argumento
      if (readingHL) {
        try {
          headerLines = args(i).toInt
        } catch {
          // Si el siguiente parámetro no es numérico o directamente no existe
          case e @ (_: IllegalStateException | _: NumberFormatException) =>
            printErrorReadingCSVArgs
        }
        readingHL = false
      } else {
        args(i) match {
          case "-first" => first = true
          case "-hl" => {
            header = true
            readingHL = true
          }
          case _ =>
            printErrorReadingCSVArgs
        }
      }
    }
  }

  /**
   * Emite mensajes de error por la salida estándar, generados por un error
   * durante la lectura de argumentos para el lector.
   *
   * @throws IllegalArgumentException Si alguno de los parámetros introducidos
   *   no es correcto.
   */
  @throws(classOf[IllegalArgumentException])
  private def printErrorReadingCSVArgs(): Unit = {
    logger.log(Level.SEVERE, "FileReaderWrongArgsCSVError")
    logger.log(Level.SEVERE, "FileReaderPossibleArgsCSV")
    throw new IllegalArgumentException("Wrong parameter format when trying" +
      "to read the dataset")
  }

}
