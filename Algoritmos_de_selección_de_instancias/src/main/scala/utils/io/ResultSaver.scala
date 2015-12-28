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

  val tmpResultsPath = "results/tmp"
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
  def storeInFile(args: Array[String], rdd: RDD[LabeledPoint]): Unit = {

    // Borramos el directorio(si existiese) donde guardaremos el resultado
    // y añadimos el resultado de la última ejecución

    val algName = args(0)
    // TODO La carpeta results se crea donde sea que llamemos a la función main.
    // ¿Variables del sistema para arreglarlo?
    val resultFile = new File(resultPath)
    if (!resultFile.exists())
      resultFile.mkdir()

    val writer = new PrintWriter(new File("results/" + algName + ": " +
      Calendar.getInstance.getTime))

    printSummaryInFile(writer, args)
    printResultInFile(writer, rdd, args)

    writer.close()

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
  private def printResultInFile(writer: PrintWriter,
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
}
