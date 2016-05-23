package utils.io

import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import java.io.IOException

//TODO REVISAR TODA LA DOCUMENTACIÓN DE ESTA CLASE

/**
 * Proporciona métodos para almacenar el resultado obtenido tras la ejecución de
 * un algoritmo de selección de instancias.
 *
 *
 * @constructor Crea una nueva clase encargada de guardar resultados.
 *     Además, se creará un fichero .csv en la ruta especificada en la variable
 *     ''resultPath'' que será el que guarde la información que deseamos almacenar.
 *     Esta variable, de momento, no es configurable.
 * @param  args Argumentos de la ejecución de la cual vamos a guardar los resultados.
 * @param  classifierName Nombre del clasificador usado durante la ejecución.
 * @param  filterName Nombre del filtro utilizado durante la ejecución
 * @version 1.0.0
 * @author Alejandro González Rogel
 *
 */
class ResultSaver(val args: Array[String], val classifierName: String, val filterName: String = "NoFilter") {

  /**
   * Ruta donde se almacenarán los ficheros resultado.
   *
   * Puede ser una ruta relativa.
Nt   */
  val resultPath = "results"

  /**
   * Separador de ficheros el sistema operativo.
   */
  final private val fileSeparator = System.getProperty("file.separator")

  /**
   * Formato de escritura de la fecha y hora actual
   */
  val myDateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");

  /**
   * Nombre del conjunto de datos utilizado
   */
  // TODO ese 1 está metido a pelo
  val datasetName = new File(args(1)).getName

  private val now = Calendar.getInstance().getTime()
  /**
   * Nombre del fichero donde guardaremos el resultado, incluyengo la ruta a dicho
   * fichero.
   */
  val fileName = resultPath + fileSeparator + filterName + "_" +
    classifierName + "_" + myDateFormat.format(now) + ".csv"

  val resultDir = new File(resultPath)
  if (!resultDir.exists()) {
    resultDir.mkdir()
  }
  /**
   * Fichero que almacena los resultados a guardar.
   */
  val file = new File(fileName)

  /**
   * Guarda una RDD en un único fichero en la máquina principal.
   *
   * El fichero será almacenado en una carpeta definida llamada RDDs (de no existir
   * será creada).
   *
   * @param  args  Argumentos utilizados en el lanzamiento de la aplicación.
   * @param  fileId  Nombre identificativo a incluir en el nombre del fichero
   *   resultante.
   * @param  rdd  RDD a almacenar.
   */
  def storeRDDInFile(args: Array[String],
                     fileId: String,
                     rdd: RDD[LabeledPoint]): Unit = {

    val path = resultPath + fileSeparator + "RDDs"

    val resultDir = new File(path)
    if (!resultDir.exists()) {
      resultDir.mkdir()
    }

    val now = Calendar.getInstance().getTime()
    val fileName = path + fileSeparator + fileId + "_" +
      myDateFormat.format(now)

    val writer = new FileWriter(new File(fileName))
    try {
      printRDDInFile(writer, rdd)
    } catch {
      case e: IOException => None
      // TODO Añadir mensaje de error
    } finally {

      writer.close()
    }

  }

  /**
   * Almacena información sobre los resultados de una ejecución.
   *
   * Debe ser utilizado cuando la ejecución ha constado de un filtro y un
   * clasificador.
   *
   * @param  iter Iteración de la validación cruzada. Si el valor es <0, indica
   *   que estamos tratando de imprimir la media de los resultados de la ejecución.
   * @param  reduction  Porcentaje de redución del conjunto de datos inicial tras
   *   aplicarse un selector de instancias.
   * @param classificationAccuracy  Porcentaje de acierto del clasificador.
   * @param  filterTime  Tiempo medio utilizado por la etapa de filtrado.
   * @param  classifierTime Tiempo medio utilizado por la etapa de clasificación.
   *
   */
  def storeResultsFilterClassInFile(iter: Int, reduction: Double,
                                    classificationAccuracy: Double,
                                    filterTime: Double,
                                    classifierTime: Double): Unit = {
    printIterResultsInFile(iter, reduction,
      classificationAccuracy, filterTime, classifierTime)
  }

  /**
   * Almacena información sobre los resultados de una ejecución.
   *
   * Debe ser utilizado cuando la ejecución ha sido una labor únicamente de
   * clasificación, sin filtrado previo.
   *
   * @param  iter Iteración de la validación cruzada. Si el valor es <0, indica
   *   que estamos tratando de imprimir la media de los resultados de la ejecución.
   * @param classificationAccuracy  Porcentaje de acierto del clasificador.
   * @param  execTime  Tiempo tardado en ejecutar el proceso medido.
   *
   */
  def storeResultsClassInFile(iter: Int, classificationAccuracy: Double,
                              execTime: Double): Unit = {

    printIterResultsInFile(iter, 0, classificationAccuracy, 0, execTime)

  }

  /**
   * Imprime la cabecera del fichero.
   *
   * Esta linea contiene el nombre de los posibles parámetros a medir durante
   * el experimento. Por orden, estos parámetros son: Iteración validación cruzada,
   * argumentos del programa, nombre del conjunto de datos, nombre filtro,
   * nombre clasificador, reducción, tasa de acierto, tiempo de filtrado y
   * tiempo de clasificación.
   *
   */
  def writeHeaderInFile(): Unit = {

    val writer = new FileWriter(file)
    try {
      writer.write("#cv,Program arguments,Dataset,Filter,Classifier,Reduction(%)," +
        "Accuracy(%),Filter time(s),Classification time(s)\n")
    } catch {
      case e: IOException => None
      // TODO Añadir mensaje de error
    } finally {

      writer.close()
    }
  }

  /**
   * Escribe en el fichero una nueva linea con datos sobre la ejecución.
   *
   * @param  iter Iteración de la validación cruzada. Si el valor es <0, indica
   *   que estamos tratando de imprimir la media de los resultados de la ejecución.
   * @param  reduction  Porcentaje de redución del conjunto de datos inicial tras
   *   aplicarse un selector de instancias.
   * @param classificationAccuracy  Porcentaje de acierto del clasificador.
   * @param  filterTime  Tiempo medio utilizado por la etapa de filtrado.
   * @param  classifierTime Tiempo medio utilizado por la etapa de clasificación.
   */
  private def printIterResultsInFile(iter: Int,
                                     reduction: Double,
                                     classificationAccuracy: Double,
                                     filterTime: Double,
                                     classifierTime: Double): Unit = {

    val writer = new FileWriter(file, true)
    try {
      if (iter >= 0) {
        writer.write(iter.toString() + "," + argsToString(args) + "," + datasetName + "," + filterName + "," + classifierName + "," + reduction +
          "," + classificationAccuracy + "," + filterTime / 1000 + "," + classifierTime / 1000 + "\n")
      } else {
        writer.write("Average values," + argsToString(args) + "," + datasetName + "," + filterName + "," + classifierName + "," + reduction +
          "," + classificationAccuracy + "," + filterTime / 1000 + "," + classifierTime / 1000 + "\n")
      }
    } catch {
      case e: IOException => None
      // TODO Añadir mensaje de error
    } finally {

      writer.close()
    }

  }

  /**
   * Almacena el contenido de una estrucutra RDD en un único fichero.
   *
   * @param writer  Escritor del fichero.
   * @param rdd  Estructura RDD que vamos a almacenar en un fichero.
   */
  private def printRDDInFile(writer: FileWriter,
                             rdd: RDD[LabeledPoint]): Unit = {

    val rddLocalCopy = rdd.collect()
    rddLocalCopy.foreach { lp =>
      var line = ""
      lp.features.toArray.map { value => line += value.toString() + "," }
      line += lp.label.toString + "\n"
      writer.write(line)
    }

  }

  /**
   * Convierte un array de Strings en un único valor String con los valores
   * separados por un espacio.
   */
  private def argsToString(args: Array[String]): String = {
    var result: String = ""
    for { param <- args } {
      result = result.concat(" " + param.toString())
    }
    result
  }

}
