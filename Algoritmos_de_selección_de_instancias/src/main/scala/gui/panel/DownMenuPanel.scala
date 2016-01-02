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

/**
 * Panel que contiene las opciones de ejecución del programa.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DownMenuPanel(parent: UI) extends BorderPanel {

  // Componentes
  val zipButton = new Button("ZIP")
  val executeButton = new Button("Ejecutar")
  val actualOperation = new Label()

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
      val commands = getAllExecutionCommands
      for (i <- 0 until commands.size) {
        val shCommand = createSHCommand(commands(i))
        actualOperation.text =
          "Realizando operación " + (i + 1) + " de " + commands.size + "..."
          //TODO LANZAR ESTO EN UN NUEVO HILO QE NO DIFICULTE EL USO DE LA GUI
        Process("sh", shCommand.split(" ")).!! 
      }
      actualOperation.text = ""
      Dialog.showMessage(this, "Todas las operaciones han sido completadas")

    }

    case ButtonClicked(`zipButton`) => {
      val commands = getAllExecutionCommands
      val shCommands: Array[String] = Array.ofDim(commands.size)
      for (i <- 0 until commands.size) {
        shCommands(i) = createSHZipCommand(commands(i))
      }

      val commandsFilePath = createSHFile(shCommands)

      // Seleccionamos todos los datasets diferentes
      var datasetOptions = parent.datasetPanel.seqConfigurations
      var elementsPath = ArrayBuffer.empty[String]
      datasetOptions.foreach { option =>
        var datasetpath = option.split(" ")(0)
        if (!elementsPath.contains(datasetpath))
          elementsPath += datasetpath

      }

      elementsPath += commandsFilePath
      createZIP(elementsPath) //Lanzar esto desde un hilo aparte
      Dialog.showMessage(this, "El archivo ZIP ha sido creado")
    }

  }

  private def getAllExecutionCommands(): IndexedSeq[String] = {

    // Seleccionamos todas las opciones existentes en cada uno de los paneles
    // de la aplicación
    val sparkHome = parent.sparkPanel.sparkHomeTextField.text
    val sparkMaster = parent.sparkPanel.masterTextField.text
    var sparkOptions = parent.sparkPanel.seqConfigurations
    var datasetOptions = parent.datasetPanel.seqConfigurations
    var filterOptions = parent.filterPanel.seqConfigurations
    var classifierOptions = parent.classifierPanel.getClassifierOptions()
    var crossValidationOptions = 
      parent.classifierPanel.getCrossValidationOptions()
    var commands: ArrayBuffer[String] = ArrayBuffer.empty[String]

    sparkOptions.foreach { sparkOption =>
      var partialCommand = sparkOption
      datasetOptions.foreach { datasetOption =>
        var tmp1 = partialCommand + "-r " + datasetOption
        filterOptions.foreach { filterOptions =>
          var tmp2 = 
            tmp1 + "-f " + filterOptions + "-c " + classifierOptions + "-cv " + crossValidationOptions
          commands += tmp2
        }
      }
    }

    val completeCommands = for (i <- 0 until commands.size)
      yield sparkHome + " " + sparkMaster + " " + commands(i)

    return completeCommands

  }

  private def createSHCommand(command: String): String = {
    var commandSplited = command.split(" ")
    //TODO Revisar este comando
    //    var thisJarName = new java.io.File(this.getClass.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()
    var thisJarPath =
      commandSplited(0) + System.getProperty("file.separator") + "lib" + System.getProperty("file.separator") + "ISAlgorithms-0.0.1-SNAPSHOT-jar-with-dependencies.jar" //TODO SUPONEMOS QUE EL JAR ESTÁ EN /lib de spark_home
    var commandSH =
      commandSplited(0) + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "spark-submit --master " + commandSplited(1) + " "

    var foundr = false
    var count = 2
    while (!foundr) {
      if (commandSplited(count).equals("-r")) {
        foundr = true
      } else {
        commandSH += commandSplited(count) + " "
        count += 1
      }
    }

    commandSH += "--class main.MainWithFilter " + thisJarPath + " "

    for (i <- count until commandSplited.size) {
      commandSH += commandSplited(i) + " "
    }
    return commandSH
  }

  private def createSHZipCommand(command: String): String = {
    var commandSplited = command.split(" ")
    //TODO Revisar este comando
    //    var thisJarName = new java.io.File(this.getClass.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()
    var thisJarPath =
      commandSplited(0) + System.getProperty("file.separator") + "lib" + System.getProperty("file.separator") + "ISAlgorithms-0.0.1-SNAPSHOT-jar-with-dependencies.jar" //TODO SUPONEMOS QUE EL JAR ESTÁ EN /lib de spark_home
    var commandSH =
      commandSplited(0) + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "spark-submit --master " + commandSplited(1) + " "

    var foundr = false
    var count = 2
    while (!foundr) {
      if (commandSplited(count).equals("-r")) {
        foundr = true
      } else {
        commandSH += commandSplited(count) + " "
        count += 1
      }
    }

    commandSH += "--class main.MainWithFilter " + thisJarPath + " "

    commandSH += "-r "
    commandSH +=
      commandSplited(count + 1).split(System.getProperty("file.separator")).last + " "
    count += 2

    for (i <- count until commandSplited.size) {
      commandSH += commandSplited(i) + " "
    }
    return commandSH

  }

  private def createSHFile(shCommands: Array[String]): String = {
    val resultPath = "zip"
    val resultFile = new File(resultPath)
    if (!resultFile.exists())
      resultFile.mkdir()

    val commandsFile = new File(resultPath + System.getProperty("file.separator") + "Bateria_de_Ejecucion")
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

  private def createZIP(elementsPath: ArrayBuffer[String]): Unit = {

    var zipFilePath = "zip" + System.getProperty("file.separator") + "zip" + ": " +
      System.currentTimeMillis()

    var files: ListBuffer[File] = ListBuffer.empty[File]
    elementsPath.foreach { path => files += new File(path) }

    val zip = new ZipOutputStream(new FileOutputStream(zipFilePath));
    try {
      for (file <- files) {

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

}