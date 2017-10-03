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
 *
 * Class that is able to create a .zip file with an execution script for running our
 * data mining jobs. It also contains all the required datasets. 
 *
 * The .zip creation is executed in the background.
 *
 * @param parent Main window of our GUI.
 * @constructor Creates a new object.
 *
 */
class ZipCreator(parent: UI) extends Runnable {

  /**
   * Name of the .sh file.
   */
  // TODO This name is in Spanish
  private val nameSHFile = "Bateria_de_Ejecucion.sh"
  /**
   * File separator of the operative system.
   */
  private val fsep = System.getProperty("file.separator")

  /**
   * Compresses all the required files into a .zip file.
   */
  def run(): Unit = {
    parent.working = true
    // TODO Hardcoded text
    parent.changeInformationText("Create a ZIP file.”)

    // Create an .sh file with the command for all the defined executions.
    val commands = parent.getAllExecutionCommands
    val shCommands: Array[String] = Array.ofDim(commands.size)
    for { i <- 0 until commands.size } {
      shCommands(i) = createSHZipCommand(commands(i))
    }

    val commandsFilePath = createSHFile(shCommands)

    // Collect the path to all the required datasets.
    val datasetOptions = parent.datasetPanel.seqConfigurations
    val elementsPath = getAllDatasetsPath(datasetOptions)
    // Add the path to the .sh file too.
    elementsPath += commandsFilePath

    try {
      createZIP(elementsPath)
      // Once we have the .sh file inside the .zip, delete the old version.
      new File(commandsFilePath).delete()

      // TODO Hardcoded text.
      parent.changeInformationText(“Creating ZIP file.”)
      Dialog.showMessage(null, “The ZIP file has been created“)
    } catch {
      case ex: Exception => Dialog.showMessage(null, “There has been an " +
        "error during the creation of the ZIP file.")
    } finally {
      parent.changeInformationText("")
      parent.working = false
    }
  }

  /**
   * Creates an command that is able to launch one data mining task.
   *
   * We asume the path to the datasets to be the directory that
   * contains the command.
   * 
   * @param command String with all the information about the execution.
   *
   * @return String with the command line.
   */
  private def createSHZipCommand(command: String): String = {
    var commandSplited = TextOperations.splitTextWithSpaces(command)

    var thisJarPath =
      commandSplited(0) + "lib" + fsep + "ISAlgorithms.jar"
    var commandSH =
      commandSplited(0) + "bin" + fsep + "spark-submit --master " +
        commandSplited(1) + " "

    // Look for the ID of the reader so we can use it in the future.
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

    // Keep adding parameters to the command until we reach the dataset.
    // The dataset requires a different treatment.
    commandSH += "--class " + parent.execClass + " " + thisJarPath + " "
    commandSH += parent.execType + " "
    commandSH += READER_SEPARATOR.toString() + " "
    commandSH += commandSplited(count + 1).split(fsep).last + " "
    count += 2
    // Add the rest of the parameters to the command string.
    for { i <- count until commandSplited.size } {
      commandSH += commandSplited(i) + " "
    }

    commandSH

  }

  /**
   * Given a sequence with different commands, create an .sh file with all of them.
   *
   * @param  shCommands  List with all the commands we need to include in the .sh file.
   *
   * @return Path to the .sh file we just created.
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
   * Creates a .zip file.
   *
   * It contains an .sh file and all the datasets required by the .sh file.
   *
   * @param elementsPath  Path to all the elements we want to include in the .zip.
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
   * Finds the path to all the required datasets.
   *
   * @param dConfs Part of the execution commands that contain information
   * about the datasets.
   *
   * @return Path to all the datasets.
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
