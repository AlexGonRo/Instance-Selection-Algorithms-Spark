package utils.io

import java.io.File
import java.io.PrintWriter
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
   * Guarda una RDD en un único fichero en la máquina principal.
   *
   * El fichero será almacenado en una carpeta definida po (de no existir
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

    val resultDir = new File(resultPath)
    if (!resultDir.exists()) {
      resultDir.mkdir()
    }

    val fileName = resultPath + fileSeparator + fileId + ": " +
      Calendar.getInstance.getTime

    val writer = new PrintWriter(new File(fileName))
    try {
      printSummaryInFile(writer, args)
      printRDDInFile(writer, rdd)
    } finally {
      writer.close()
    }

  }

  /**
   * Almacena en un fichero de texto información sobre el resultado de las
   * operaciones.
   *
   * @param  args  argumentos de llamada de la ejecución.
   * @param  filterTime  Tiempo tardado en ejecutar la selección de instancias.
   * @param  reduction  Porcentaje de redución del conjunto de datos inicial tras
   *   aplicarse un selector de instancias.
   * @param classificationAccuracy  Porcentaje de acierto del clasificador.
   * @param  filterName  Nombre del filtro o selector de instancias utilizado.
   * @param  classifierName  Nombre del clasificador de instancias.
   *
   *
   */
  def storeResultsInFile(args: Array[String],
                         filterTime: Double,
                         reduction: Double,
                         classificationAccuracy: Double,
                         filterName: String,
                         classifierName: String): Unit = {

    val resultDir = new File(resultPath)
    if (!resultDir.exists()) {
      resultDir.mkdir()
    }

    val fileName = resultPath + fileSeparator + classifierName + ": " +
      Calendar.getInstance.getTime
    val writer = new PrintWriter(new File(fileName))
    try {
      printSummaryInFile(writer, args)
      printResultsInFile(writer, filterTime, reduction,
        classificationAccuracy, filterName, classifierName)
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
    for { param <- args } {
      writer.write(param.toString() + " ")
    }
    writer.write("\n======================\n")
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
   *
   */
  private def printResultsInFile(writer: PrintWriter,
                                 filterTime: Double,
                                 reduction: Double,
                                 classificationAccuracy: Double,
                                 filterName: String,
                                 classifierName: String): Unit = {
    val divisor = "++++++++++++++++++++++\n"
    writer.write("Filter: " + filterName + "\n")
    writer.write("Classifier: " + classifierName + "\n")
    writer.write(divisor)
    writer.write("Reduction(%) \t" + "+ " + reduction + "\n")
    writer.write(divisor)
    writer.write("Accuracy(%) \t" + "+ " + classificationAccuracy + "\n")
    writer.write(divisor)
    writer.write("Filter time(s) \t" + "+ " + filterTime / 1000 + "\n")
    writer.write(divisor)

  }
}
