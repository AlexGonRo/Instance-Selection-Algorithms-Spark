package utils

import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.util.Calendar

import scala.io.Source

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
    delete(new File(tmpResultsPath))

    val writer = new PrintWriter(new File("results/" + algName + ": " +
        Calendar.getInstance.getTime))

    printSummaryInFile(writer, args)
    printResultInFile(writer, rdd, args)

    writer.close()
    delete(new File(tmpResultsPath))

  }

  /**
   * Copia el contenido de un fichero en otro.
   *
   * @param writer  Escritor del fichero al que queremos añadir la copia
   * @param file  Fichero a copiar
   */
  private def copyFileContent(writer: PrintWriter, file: File): Unit = {

    val bufferedSource = Source.fromFile(file.getPath)

    for (line <- bufferedSource.getLines) {
      writer.write(line + "\n")
      if (writer.checkError()) {
        writer.close()
        bufferedSource.close
        delete(new File(tmpResultsPath))
        throw new IOException("There was a problem while copying the result from" +
            "the tmp files to the final one. \n")

      }
    }

    bufferedSource.close

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
    val temporal = new File(tmpResultsPath)
    rdd.saveAsTextFile(tmpResultsPath)
    val cosa = temporal.listFiles
    for (file <- temporal.listFiles()) {
      if (file.isFile() && !file.getName.startsWith("."))
        copyFileContent(writer, file)
    }

  }

  /**
   * Dada una ruta a un fichero o directorio, lo elimina en caso de existir.
   *
   * En el caso de que el directorio contenga ficheros, estos son eliminados
   * también. No eliminará, en caso de existir, directorios dentro del
   * indicado.
   *
   * @param File  Fichero o directorio sobre el que vamos a operar
   */
  private def delete(file: File): Unit = {
    // Si es un directorio, eliminamos los ficheros que haya en su interior.
    if (file.isDirectory)
      Option(file.listFiles).map(_.toList).getOrElse(Nil).foreach(delete(_))
    file.delete
  }

}
