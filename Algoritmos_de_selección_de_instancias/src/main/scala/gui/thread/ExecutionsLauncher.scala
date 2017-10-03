package gui.thread

import scala.collection.mutable.ArrayBuffer
import scala.swing.Dialog
import scala.sys.process.Process
import scala.util.control.Breaks

import gui.UI
import gui.util.TextOperations
import utils.ArgsSeparator.READER_SEPARATOR

/**
 * Runs a batch of data mining jobs in a different thread.
 *
 * The parameters of these data mining jobs will be taken from the different panels
 * of our graphical interface.
 *
 * @param parent Main window of the interface.
 * @constructor Creates a new object that is able to launch a data mining job in a
 *    different thread.
 */
class ExecutionsLauncher(parent: UI) extends Runnable {

  /**
   * File separator of the system.
   */
  private val fsep = System.getProperty("file.separator")
  /**
   * Error code if Spark is not found.
   */
  private val noSparkError = 127
  /**
   * Error code if we do not find our library jar.
   */
  private val noLibError = 101
  /**
   * Código de error emitido si nuestra librería falla.
   */
  private val executionError = 1
  /**
   * Code of a successful execution.
   */
  private val correctExecution = 0

  /**
   * Run a batch of experiments.
   *
   * These experiments have been defined by the user using the graphical interface.
   */
  def run(): Unit = {
    parent.working = true
    val commands = parent.getAllExecutionCommands
    var fatalError = false
    var numberOfErrors = 0
    var lastOutput = 0
    val loop = new Breaks;
    val posErrors = ArrayBuffer.empty[Int]

    loop.breakable {
      for { i <- 0 until commands.size } {
        val shCommand = createSHCommand(commands(i))
        // TODO HARDCODED text bellow.
        parent.changeInformationText(
          “Running experiment " + (i + 1) + " out of " + commands.size + "...")
        lastOutput = Process("sh", TextOperations.splitTextWithSpaces(shCommand)).!
        val (goodExec, fatal) = checkOutput(lastOutput)
        if (!goodExec && !fatal) {
          numberOfErrors += 1
          posErrors += i + 1
        } else if (!goodExec && fatal) {
          fatalError = fatal
          loop.break
        }
      }
    }
    parent.changeInformationText("")
    printFinalMessage(numberOfErrors, posErrors, commands.size,
      lastOutput, fatalError)
    parent.working = false
  }

  /**
   * Checks if the command we launched emitted an error code.
   * @param Output code of our execution.
   * @return Tuple of boolean values. The first value states whether the experiment
   *    was correctly executed and the second one if there was a fatal error during
   *    execution.
   */
  private def checkOutput(output: Int): (Boolean, Boolean) = {

    if (output == noLibError || output == noSparkError) {
      (false, true)
    } else if (output == executionError) {
      (false, false)
    } else {
      (true, false)
    }

  }

  /**
   * Prints final message with the number of errors that were found during
   * the execution of the batch.
   *
   * @param numErrors Number of errors found
   * @param numExec Total number of data mining tasks.
   */
  private def printFinalMessage(numErrors: Int,
                                posErrors: ArrayBuffer[Int],
                                numExec: Int,
                                lastOutput: Int,
                                fatalError: Boolean): Unit = {
    // TODO Strings here are hardcoded
    if (fatalError) {
      if (lastOutput == noSparkError) {
        Dialog.showMessage(null, “Spark could not be found. The " +
          “Execution has been cancelled.”)
      } else {
        Dialog.showMessage(null, “I could not find the " +
          "ISAlgorithms library. The execution has been cancelled.")
      }
    } else {
      if (numErrors == 0) {
        Dialog.showMessage(null, “All the tasks were completed successfully.“)
      } else if (numErrors < numExec) {
        var failedExecutions = ""
        for { x <- posErrors } {
          failedExecutions = failedExecutions.concat(x.toString).concat(", ")
        }
        failedExecutions = failedExecutions.dropRight(2)
        Dialog.showMessage(null, “Some of the executions could not be " +
          “Completed due to an execution error. ID of the uncompleted executions: " +
          + failedExecutions + ".")
      } else {
        Dialog.showMessage(null, “There has been an error in absolutely all the executions.“)
      }
    }
  }

  /**
   * Creates an SH command that is able to launch a data mining task.
   *
   * @param command Text that contains all the information we want to include in
   * the execution command.
   *
   * @return SH command.
   */
  private def createSHCommand(command: String): String = {
    var commandSplited = command.split(" ")
    var thisJarPath =
      commandSplited(0) + "lib" + fsep + "ISAlgorithms.jar"
    var commandSH =
      commandSplited(0) + "bin" + fsep + "spark-submit --master " +
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

    commandSH += "--class " + parent.execClass + " " + thisJarPath + " "
    commandSH += parent.execType + " "
    for { i <- count until commandSplited.size } {
      commandSH += commandSplited(i) + " "
    }
    commandSH
  }

}
