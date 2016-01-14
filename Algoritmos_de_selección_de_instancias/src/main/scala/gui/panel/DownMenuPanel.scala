package gui.panel

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import scala.collection.immutable.Stream.consWrapper
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.East
import scala.swing.BorderPanel.Position.West
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.event.ButtonClicked
import scala.sys.process.Process

import gui.UI
import utils.ArgsSeparator.CLASSIFIER_SEPARATOR
import utils.ArgsSeparator.CROSSVALIDATION_SEPARATOR
import utils.ArgsSeparator.FILTER_SEPARATOR
import utils.ArgsSeparator.READER_SEPARATOR

/**
 * Panel que contiene las opciones de ejecución del programa.
 *
 * @constructor Genera un panel con los componentes necesarios
 *   para poder lanzar una ejecución.
 * @param parent Ventana desde donde se han invocado este panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DownMenuPanel(parent: UI) extends BorderPanel {

  /**
   * Indica si estamos ejecutando un comando o no.
   */
  private var working = false

  // Componentes
  /**
   * Botón para realizar la compresión de los ficheros para realizar un
   * experimento.
   */
  private val zipButton = new Button("ZIP")
  /**
   * Botón para ejecutar un experimento desde la interfaz.
   */
  private val executeButton = new Button("Ejecutar")
  /**
   * Texto que indica si existe una operación en proceso.
   */
  private val actualOperation = new Label()

  // Añadimos los componentes

  layout += actualOperation -> West
  layout += new BoxPanel(Orientation.Horizontal) {
    contents += zipButton
    contents += executeButton
  } -> East

  // Listeners y eventos
  listenTo(zipButton)
  listenTo(executeButton)
  reactions += {

    case ButtonClicked(`executeButton`) => {
      if (working) {
        Dialog.showMessage(this, "Hay una operación en curso")
      } else {
        var execThread = new Thread(new executionRunnable)
        execThread.start()
      }

    }

    case ButtonClicked(`zipButton`) => {
      if (working) {
        Dialog.showMessage(this, "Hay una operación en curso")
      } else {
        var zipThread = new Thread(new zipRunnable)
        zipThread.start()
      }
    }

  }

  /**
   * Recolecta toda la información contenida en los diferentes paneles de la
   * aplicación y genera con ella todos los comandos de ejecución posibles.
   *
   * @return Secuencia con todos los comandos de ejecución.
   */
  private def getAllExecutionCommands(): IndexedSeq[String] = {

    // Seleccionamos todas las opciones existentes en cada uno de los paneles
    // de la aplicación
    val sparkCommonOptions = parent.sparkPanel.getCommonSparkOptions()
    val sparkHome = sparkCommonOptions(0)
    val sparkMaster = sparkCommonOptions(1)
    var sparkOptions = parent.sparkPanel.seqConfigurations
    var datasetOptions = parent.datasetPanel.seqConfigurations
    var filterOptions = parent.filterPanel.seqConfigurations
    var classifierOptions = parent.classifierPanel.getClassifierOptions()
    var crossValidationOptions =
      parent.classifierPanel.getCrossValidationOptions()
    // Variable resultado donde almacenaremos todas las opciones.
    var commands: ArrayBuffer[String] = ArrayBuffer.empty[String]

    // Generamos todas las combinaciones de configuraciones posibles
    sparkOptions.foreach { sparkOption =>
      var partialCommand = sparkOption
      datasetOptions.foreach { datasetOption =>
        var tmp1 = partialCommand + READER_SEPARATOR.toString() + " " +
          datasetOption
        filterOptions.foreach { filterOptions =>
          {
            var tmp2 = if (crossValidationOptions.equals("")) {
              tmp1 + FILTER_SEPARATOR.toString() + " " + filterOptions +
                CLASSIFIER_SEPARATOR.toString() + " " + classifierOptions
            } else {
              tmp1 + FILTER_SEPARATOR.toString() + " " + filterOptions +
                CLASSIFIER_SEPARATOR.toString() + " " + classifierOptions +
                CROSSVALIDATION_SEPARATOR.toString() + " " + crossValidationOptions
            }
            commands += tmp2
          }
        }
      }
    }

    // Añadimos a cada comando un apéndice al principio con información de
    // Spark común para todos los comandos
    val completeCommands = for { i <- 0 until commands.size }
      yield sparkHome + " " + sparkMaster + " " + commands(i)

    completeCommands

  }

  /**
   * Genera una sentencia que corresponde a la ejecución de una configuración
   * en una consola de comandos.
   *
   * @param command Texto con la configuración que queremos convertir.
   *
   * @return Comando de ejecución en consola.
   */
  private def createSHCommand(command: String): String = {
    var commandSplited = command.split(" ")
    val fsep = System.getProperty("file.separator")
    // TODO Revisar este comando

    // TODO SUPONEMOS QUE EL JAR ESTÁ EN /lib de spark_home
    var thisJarPath =
      commandSplited(0) + fsep + "lib" + fsep + "ISAlgorithms.jar"
    var commandSH =
      commandSplited(0) + fsep + "bin" + fsep + "spark-submit --master " +
        commandSplited(1) + " "

    var foundr = false
    var count = 2
    while (!foundr) {
      if (commandSplited(count).equals(READER_SEPARATOR.toString)) {
        foundr = true
      } else {
        commandSH += commandSplited(count) + " "
        count += 1
      }
    }

    commandSH += "--class main.MainWithIS " + thisJarPath + " "

    for { i <- count until commandSplited.size } {
      commandSH += commandSplited(i) + " "
    }
    commandSH
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
    var commandSplited = command.split(" ")
    val fsep = System.getProperty("file.separator")
    // TODO Revisar este comando

    // TODO SUPONEMOS QUE EL JAR ESTÁ EN /lib de spark_home
    var thisJarPath =
      commandSplited(0) + fsep + "lib" + fsep + "ISAlgorithms.jar"
    var commandSH =
      commandSplited(0) + fsep + "bin" + fsep + "spark-submit --master " +
        commandSplited(1) + " "

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

    commandSH += "--class main.MainWithIS " + thisJarPath + " "

    commandSH += READER_SEPARATOR.toString() + " "
    commandSH += commandSplited(count + 1).split(fsep).last + " "
    count += 2

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

    val commandsFilePath =
      resultPath + System.getProperty("file.separator") + "Bateria_de_Ejecucion"
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

    var zipFilePath = "zip" + System.getProperty("file.separator") + "zip" + ": " +
      System.currentTimeMillis()

    var files: ListBuffer[File] = ListBuffer.empty[File]
    elementsPath.foreach { path => files += new File(path) }

    val zip = new ZipOutputStream(new FileOutputStream(zipFilePath));
    try {
      for { file <- files } {

        zip.putNextEntry(new ZipEntry(file.getName));

        val in = Source.fromFile(file.getCanonicalPath).bufferedReader();
        try {
          readByte(in).takeWhile(_ > -1).toList.foreach(zip.write(_));
        } finally {
          in.close();
        }

        zip.closeEntry();
      }
    } finally {
      zip.close();
    }

    def readByte(bufferedReader: BufferedReader): Stream[Int] = {
      bufferedReader.read() #:: readByte(bufferedReader);
    }
  }

  /**
   * Clase destinada a la ejecución de una batería de ejecuciones de Spark sin
   * que dichas ejecuciones paralicen el funcionamiento normal de la interfaz.
   *
   */
  class executionRunnable extends Runnable {

    /**
     * Realiza el experimento definido a lo largo de la interfaz.
     */
    def run(): Unit = {
      working = true
      val commands = getAllExecutionCommands
      for { i <- 0 until commands.size } {
        val shCommand = createSHCommand(commands(i))
        actualOperation.text =
          "Realizando operación " + (i + 1) + " de " + commands.size + "..."
        Process("sh", shCommand.split(" ")).!
      }
      actualOperation.text = ""
      Dialog.showMessage(null, "Todas las operaciones han sido completadas")
      working = false
    }
  }

  /**
   * Clase destinada a la creación de un archivo .zip sin que la creación de
   * dicho documento bloquee el funcionamiento normal de la interfaz.
   */
  class zipRunnable extends Runnable {
    /**
     * Realiza la compresión de todos los archivos necesarios para llevar a cabo
     * un experimento.
     */
    def run(): Unit = {
      working = true
      actualOperation.text =
        "Creando archivo ZIP"

      val commands = getAllExecutionCommands
      val shCommands: Array[String] = Array.ofDim(commands.size)
      for { i <- 0 until commands.size } {
        shCommands(i) = createSHZipCommand(commands(i))
      }

      val commandsFilePath = createSHFile(shCommands)

      // Seleccionamos todos los datasets diferentes
      var datasetOptions = parent.datasetPanel.seqConfigurations
      var elementsPath = ArrayBuffer.empty[String]
      datasetOptions.foreach { option =>
        var datasetpath = option.split(" ")(0)
        if (!elementsPath.contains(datasetpath)) {
          elementsPath += datasetpath
        }
      }
      elementsPath += commandsFilePath

      createZIP(elementsPath)

      actualOperation.text =
        ""
      Dialog.showMessage(null, "El archivo ZIP ha sido creado")
      working = false
    }
  }
}
