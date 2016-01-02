package utils.io

import java.io.File
import java.io.PrintWriter
import java.util.Calendar

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
 * Proporciona métodos para almacenar el resultado obtenido tras la ejecución de un
 * algoritmo de selección de instancias.
 * @version 1.0.0
 * @author Alejandro González Rogel
 */
class ResultSaver {

  val resultPath = "results"

  /**
   * Guarda una RDD en un único fichero en la máquina principal.
   *
   * El fichero será almacenado en una carpeta de nombre "results"(de no existir
   * será creada).
   *
   * @param  args  Argumentos utilizados en el lanzamiento de la aplicación.
   * @param  result  Resultado a almacenar
   */
  def storeRDDInFile(args: Array[String], filterName: String, rdd: RDD[LabeledPoint]): Unit = {

    // Borramos el directorio(si existiese) donde guardaremos el resultado
    // y añadimos el resultado de la última ejecución

    // TODO La carpeta results se crea donde sea que llamemos a la función main.
    // ¿Variables del sistema para arreglarlo?
    val resultFile = new File(resultPath)
    if (!resultFile.exists())
      resultFile.mkdir()

    val writer = new PrintWriter(new File(resultPath + System.getProperty("file.separator") + filterName + ": " +
      Calendar.getInstance.getTime))
    try {
      printSummaryInFile(writer, args)
      printRDDInFile(writer, rdd, args)
    } finally {
      writer.close()
    }

  }

  /**
   * Almacena en un fichero de texto información sobre el resultado de las
   * operaciones.
   * 
   * @param  args  argumentos de llamada de la ejecución
   * @param  initialData  Conjunto de datos inicial
   * @param  dataPostFilter  Conjunto de datos tras la operación de selección 
   *   de instancias.
   * @param  classifierName  Nombre del clasificador de instancias
   * @param  classificationResult  Tasa de acierto de clasificación
   * @param  filterTime  Tiempo tardado en ejecutar la selección de instancias.
   * 
   */
  def storeResultsInFile(args: Array[String],
      filterTime: Double,
      reduction:Double,
      classificationResult: Double,
      filterName:String,
      classifierName: String): Unit = {

    val writer = new PrintWriter(new File(resultPath + System.getProperty("file.separator") + classifierName + ": " +
      Calendar.getInstance.getTime))
    try {
      printSummaryInFile(writer, args)
      printResultsInFile(writer, filterTime,reduction,classificationResult,filterName,classifierName)
    } finally {
      writer.close()
    }
  }

  /**
   * Imprime unas lineas ya predefinidas en un fichero. Lo realmente importante
   * de esta escritura es que indica que argumentos se han usado en la ejecución
   * del algoritmo de selección de instancias.
   *
   * @param writer  Escritor
   * @param  args  Argumentos utilizados para la ejecución del algoritmo.
   */
  private def printSummaryInFile(writer: PrintWriter, args: Array[String]) = {
    writer.write("======================\n")
    writer.write("Program arguments\n")
    for (param <- args)
      writer.write(param.toString() + " ")
    writer.write("\n======================\n")
  }

  /**
   * Agrupa el contenido de una RDD en un único fichero.
   * @param writer  Escritor del fichero
   * @param rdd  Estructura RDD que vamos a almacenar en un fichero.
   * @param args  Argumentos utilizados en la ejecución del algoritmo que ha
   *   producido esta RDD
   */
  private def printRDDInFile(writer: PrintWriter,
                             rdd: RDD[LabeledPoint],
                             args: Array[String]) = {

    val rddLocalCopy = rdd.collect()
    rddLocalCopy.foreach { lp =>
      var line = ""
      lp.features.toArray.map { value => line += value.toString() + "," }
      line += lp.label.toString + "\n"
      writer.write(line)

    }

  }

  private def printResultsInFile(writer: PrintWriter,
      filterTime: Double,
      reduction:Double,
      classificationResult: Double,
      filterName:String,
      classifierName: String ): Unit = {
    writer.write("Filter: "+filterName+"\n")
    writer.write("Classifier: "+classifierName+"\n")
    writer.write("++++++++++++++++++++++\n")
    writer.write("Reduction(%) \t" + "+ " + reduction + "\n")
    writer.write("++++++++++++++++++++++\n")
    writer.write("Accuracy(%) \t" + "+ " + classificationResult + "\n")
    writer.write("++++++++++++++++++++++\n")
    writer.write("Filter time(s) \t" + "+ " + filterTime/1000 + "\n") 
    writer.write("++++++++++++++++++++++\n")

  }
}
