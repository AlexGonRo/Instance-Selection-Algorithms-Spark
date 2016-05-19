package utils.io

import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Calendar

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
 * Proporciona métodos para almacenar el resultado obtenido tras la ejecución de
 * un algoritmo de selección de instancias.
 *
 *
 * @constructor Crea una nueva clase para guardar resultados.
 * @version 1.0.0
 * @author Alejandro González Rogel
 *
 */
class ResultSaver {

  /**
   * Ruta donde se almacenarán los ficheros resultado.
   *
   * Puede ser una ruta relativa.
   */
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
   * Guarda una RDD en un único fichero en la máquina principal.
   *
   * El fichero será almacenado en una carpeta definida llamada RDDs (de no existir
   * será creada).
   *
   * @param  args  Argumentos utilizados en el lanzamiento de la aplicación.
   * @param  fileId  Nombre identificativo a incluir en el nombre del fichero
   *   resultante.
   * @param  rdd  RDD a almacenar
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

    val writer = new PrintWriter(new File(fileName))
    try {
      printRDDInFile(writer, rdd)
    } finally {
      writer.close()
    }

  }

  /**
   * Almacena en un fichero de texto información sobre el resultado de las
   * operaciones.
   *
   * Debe ser utilizado cuando la ejecución ha constado de un filtro y un
   * clasificador.
   *
   * @param  args  argumentos de llamada de la ejecución.
   * @param  reduction  Porcentaje de redución del conjunto de datos inicial tras
   *   aplicarse un selector de instancias.
   * @param classificationAccuracy  Porcentaje de acierto del clasificador.
   * @param  filterName  Nombre del filtro o selector de instancias utilizado.
   * @param  classifierName  Nombre del clasificador de instancias.
   * @param  filterTime  Tiempo medio utilizado por la etapa de filtrado.
   * @param  classifierTime Tiempo medio utilizado por la etapa de clasificación.
   *
   */
  def storeResultsFilterClassInFile(args: Array[String],
                                    reduction: Double,
                                    classificationAccuracy: Double,
                                    filterName: String,
                                    classifierName: String,
                                    filterTime: Double,
                                    classifierTime: Double): Unit = {

    val resultDir = new File(resultPath)
    if (!resultDir.exists()) {
      resultDir.mkdir()
    }

    val now = Calendar.getInstance().getTime()
    val fileName = resultPath + fileSeparator + filterName + "_" +
      classifierName + "_" + myDateFormat.format(now) + ".csv"

    val writer = new PrintWriter(new File(fileName))
    try {
      val datasetname = new File(args(2)).getName
      printResultsInFile(writer, args, datasetname, reduction,
        classificationAccuracy, filterName, classifierName, filterTime, classifierTime)
    } finally {
      writer.close()
    }
  }

  /**
   * Almacena en un fichero de texto información sobre el resultado de las
   * operaciones.
   *
   * Debe ser utilizado cuando la ejecución ha sido una labor únicamente de
   * clasificación, sin filtrado previo.
   *
   * @param  args  argumentos de llamada de la ejecución.
   * @param  reduction  Porcentaje de redución del conjunto de datos inicial tras
   *   aplicarse un selector de instancias.
   * @param classificationAccuracy  Porcentaje de acierto del clasificador.
   * @param  classifierName  Nombre del clasificador de instancias.
   * @param  execTime  Tiempo tardado en ejecutar el proceso medido.
   *
   */
  def storeResultsClassInFile(args: Array[String],
                              classificationAccuracy: Double,
                              classifierName: String,
                              execTime: Double): Unit = {

    val resultDir = new File(resultPath)
    if (!resultDir.exists()) {
      resultDir.mkdir()
    }

    val now = Calendar.getInstance().getTime()
    val fileName = resultPath + fileSeparator + classifierName + "_" +
      myDateFormat.format(now) + ".csv"

    val datasetname = new File(args(2)).getName
    val writer = new PrintWriter(new File(fileName))
    try {
      printResultsInFile(writer, args, datasetname, 0, classificationAccuracy, "None", classifierName, 0, execTime)
    } finally {
      writer.close()
    }
  }

  /**
   * Imprime unas lineas ya predefinidas en un fichero.
   *
   * Estas lineas contienen información sobre los parámetros iniciales con los
   * que se inició la ejecución del programa o experimento.
   *
   * @param writer  Escritor en ficheros.
   * @param  args  Argumentos utilizados para la ejecución del algoritmo.
   */
  private def printSummaryInFile(writer: PrintWriter, args: Array[String]): Unit = {
    writer.write("======================\n")
    writer.write("Program arguments\n")
    writer.write(argsToString(args))
    writer.write("\n======================\n")
  }

  /**
   * Escribe una serie de resultados de ejecución en un fichero.
   *
   * @param  writer  Objeto dedicado a la escritura en el fichero.
   * @param  filterTime  Tiempo medio de filtrado en la ejecución.
   * @param  reduction  Porcentaje de reducción de instancias tras la aplicación
   *     de un filtro.
   * @param  classificationAccuracy  Porcentaje de acierto de la clasificación.
   * @param  filterName  Nombre del filtro utilizado.
   * @param  classifierName  Nombre del clasificador utilizado.
   * @param  filterTime  Tiempo medio en realizar la operación de filtrado. Solo cuando
   *   la ejecución es de tipo test.
   * @param classifierTime Tiempo medio empleado en la clasificación.
   */
  private def printResultsInFile(writer: PrintWriter,
                                 args: Array[String],
                                 datasetName: String,
                                 reduction: Double,
                                 classificationAccuracy: Double,
                                 filterName: String,
                                 classifierName: String,
                                 filterTime: Double,
                                 classifierTime: Double): Unit = {
    writer.write("Program arguments,Dataset,Filter,Classifier,Mean reduction(%)," +
        "Mean accuracy(%),Mean filter time(s),Mean classifier time(s)\n")

    writer.write(argsToString(args) + "," + datasetName + "," + filterName + "," + classifierName + "," + reduction +
      "," + classificationAccuracy + "," + filterTime / 1000 + ","+ classifierTime / 1000+"\n")

  }

  /**
   * Almacena el contenido de una estrucutra RDD en un único fichero.
   *
   * @param writer  Escritor del fichero.
   * @param rdd  Estructura RDD que vamos a almacenar en un fichero.
   */
  private def printRDDInFile(writer: PrintWriter,
                             rdd: RDD[LabeledPoint]): Unit = {

    val rddLocalCopy = rdd.collect()
    rddLocalCopy.foreach { lp =>
      var line = ""
      lp.features.toArray.map { value => line += value.toString() + "," }
      line += lp.label.toString + "\n"
      writer.write(line)

    }

  }

  private def argsToString(args: Array[String]): String = {
    var result: String = ""
    for { param <- args } {
      result = result.concat(" " + param.toString())
    }
    result
  }

}
