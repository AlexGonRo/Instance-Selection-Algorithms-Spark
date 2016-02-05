package gui.thread

import scala.swing.Dialog
import scala.collection.mutable.ArrayBuffer
import gui.UI
import java.io.FileInputStream
import java.io.PrintWriter
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import java.io.BufferedInputStream
import scala.collection.mutable.ListBuffer
import java.util.zip.ZipEntry
import java.io.File
import utils.ArgsSeparator.CLASSIFIER_SEPARATOR
import utils.ArgsSeparator.CROSSVALIDATION_SEPARATOR
import utils.ArgsSeparator.FILTER_SEPARATOR
import utils.ArgsSeparator.READER_SEPARATOR
import gui.util.TextOperations

/**
 * Clase destinada a la creación de un archivo .zip.
 *
 * El archivo zip ha de contener todos aquellos conjuntos de datos que hayan
 * sido seleccionados con posterioridad. Igualmente, ha de contener un fichero
 * .sh generado dinámicamente que permita la ejecución futura de todos los
 * posibles experimentos definidos a lo largo de la aplicación.
 *
 * @param parent Ventana principal de la aplicación.
 * @constructor Genera una nueva clase con capacidad de realizar la creación
 *   de un fichero .zip en segundo plano
 */
class ZipCreator(parent: UI) extends Runnable {

  /**
   * Nombre del fichero .sh generado al realizar un archivo zip.
   */
  private val nameSHFile = "Bateria_de_Ejecucion.sh"
  /**
   * Representación del separador de directorios.
   */
  private val fsep = System.getProperty("file.separator")

  /**
   * Realiza la compresión de todos los archivos necesarios para llevar a cabo
   * un experimento.
   */
  def run(): Unit = {
    parent.working = true
    parent.changeInformationText("Creando archivo ZIP")

    // Creamos el script de ejecución con todas las ejecuciones
    val commands = parent.getAllExecutionCommands
    val shCommands: Array[String] = Array.ofDim(commands.size)
    for { i <- 0 until commands.size } {
      shCommands(i) = createSHZipCommand(commands(i))
    }

    val commandsFilePath = createSHFile(shCommands)

    // Seleccionamos la ruta a todos los datasets diferentes
    val datasetOptions = parent.datasetPanel.seqConfigurations
    val elementsPath = getAllDatasetsPath(datasetOptions)
    // Añadimos al conjunto de rutas aquella del fichero .sh
    elementsPath += commandsFilePath

    try {
      createZIP(elementsPath)
      // Eliminamos el fichero .sh después de haberlo comprimido
      new File(commandsFilePath).delete()

      parent.changeInformationText("Creando archivo ZIP")
      Dialog.showMessage(null, "El archivo ZIP ha sido creado")
    } catch {
      case ex: Exception => Dialog.showMessage(null, "Ha ocurrido un " +
        "error durante la creación del fichero")
    } finally {
      parent.changeInformationText("")
      parent.working = false
    }
  }

  /**
   * Genera una sentencia que corresponde a la ejecución de una configuración
   * en una consola de comandos.
   *
   * La ruta donde podemos encontrar los conjuntos de datos es el propio
   * directorio, pues se supone que estos comandos serán incluidos más adelante
   * en un archivo .zip que contendrá todos los conjuntos de datos necesarios.
   *
   * @param command Texto con la configuración que queremos convertir.
   *
   * @return Comando de ejecución en consola.
   */
  private def createSHZipCommand(command: String): String = {
    var commandSplited = TextOperations.splitTextWithSpaces(command)

    var thisJarPath =
      commandSplited(0) + "lib" + fsep + "ISAlgorithms.jar"
    var commandSH =
      commandSplited(0) + "bin" + fsep + "spark-submit --master " +
        commandSplited(1) + " "

    // Buscamos el identificador del lector para almacenar su posición
    // para el futuro
    var foundr = false
    var count = 2
    while (!foundr) {
      if (commandSplited(count).equals(READER_SEPARATOR.toString())) {
        foundr = true
      } else {
        commandSH += commandSplited(count) + " "
        count += 1
      }
    }

    // Continuamos añadiendo atributos de código hasta llegar a la
    // ruta del conjunto de datos, que tiene una secuencia de invocación
    // especial
    commandSH += "--class " + parent.execClass + " " + thisJarPath + " "
    commandSH += parent.execType + " "
    commandSH += READER_SEPARATOR.toString() + " "
    commandSH += commandSplited(count + 1).split(fsep).last + " "
    count += 2
    // Continuamos añadiendo el resto de atributos
    for { i <- count until commandSplited.size } {
      commandSH += commandSplited(i) + " "
    }

    commandSH

  }

  /**
   * Dada una secuencia de comandos, genera un archivo .sh con el listado de
   * dichos comandos.
   *
   * @param  shCommands  Listado con todos los comandos sh a añadir al fichero.
   *
   * @return Ruta del fichero generado
   */
  private def createSHFile(shCommands: Array[String]): String = {
    val resultPath = "zip"
    val resultFile = new File(resultPath)

    if (!resultFile.exists()) {
      resultFile.mkdir()
    }

    val commandsFilePath = resultPath + fsep + nameSHFile
    val commandsFile =
      new File(commandsFilePath)
    val writer = new PrintWriter(commandsFile)

    writer.write("#!/bin/bash\n\n ")

    var actualCommand = 0
    var totalCommands = shCommands.size
    shCommands.foreach { command =>
      actualCommand += 1
      writer.write(
        command + "\n\necho " + actualCommand + " de " + totalCommands + "\n\n")
    }

    writer.close()

    commandsFile.getAbsolutePath

  }

  /**
   * Genera un archivo .zip con un archivo de ejecución .sh dentro y todos
   * los conjuntos de datos requeridos para la ejecución de dicho achivo.
   *
   * @param elementsPath  Ruta de todos los archivos a añadir al zip
   *
   */
  private def createZIP(elementsPath: ArrayBuffer[String]): Unit = {

    val Buffer = 2 * 1024

    var zipFilePath = "zip" + fsep + "zip_" +
      System.currentTimeMillis()

    var files: ListBuffer[File] = ListBuffer.empty[File]
    elementsPath.foreach { path => files += new File(path) }

    var data = new Array[Byte](Buffer)
    val zip = new ZipOutputStream(new FileOutputStream(zipFilePath))
    elementsPath.foreach { name =>

      val zipEntry =
        new ZipEntry(name.splitAt(name.lastIndexOf(fsep.charAt(0)) + 1)._2)
      zip.putNextEntry(zipEntry)
      val in = new BufferedInputStream(new FileInputStream(name), Buffer)
      var b = in.read(data, 0, Buffer)
      while (b != -1) {
        zip.write(data, 0, b)
        b = in.read(data, 0, Buffer)
      }
      in.close()
      zip.closeEntry()
    }
    zip.close()

  }

  /**
   * Devuelve un listado de todos los conjuntos de datos que necesitan las
   * configuraciones propuestas.
   *
   * @param dConfs Conjunto de configuraciones de conjuntos de datos.
   *
   * @return Rutas de los diferentes conjuntos de datos necesarios.
   *
   */
  private def getAllDatasetsPath(dConfs: ArrayBuffer[String]):
    ArrayBuffer[String] = {
    var elementsPath = ArrayBuffer.empty[String]
    dConfs.foreach { option =>
      var datasetpath = TextOperations.splitTextWithSpaces(option)(0)
      if (!elementsPath.contains(datasetpath)) {
        elementsPath += datasetpath
      }
    }
    elementsPath
  }
}
