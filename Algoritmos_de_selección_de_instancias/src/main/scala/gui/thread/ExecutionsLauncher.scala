package gui.thread

import scala.collection.mutable.ArrayBuffer
import scala.swing.Dialog
import scala.sys.process.Process
import scala.util.control.Breaks

import gui.UI
import gui.util.TextOperations
import utils.ArgsSeparator.READER_SEPARATOR

/**
 * Clase destinada a la ejecución de una batería de ejecuciones de Spark.
 *
 * Los datos obtenidos para las ejecuciones serán los proporcionados por los
 * diferentes campos de la interfaz gráfica.
 *
 * @param parent Ventana principal de la aplicación.
 * @constructor Genera una nueva clase con capacidad de realizar ejecuciones
 *   de tareas de minería en segundo plano.
 */
class ExecutionsLauncher(parent: UI) extends Runnable {

  /**
   * Representación del separador de directorios.
   */
  private val fsep = System.getProperty("file.separator")
  /**
   * Código de error si no encontramos el script de Spark.
   */
  private val noSparkError = 127
  /**
   * Código de error si no encontramos nuestra librería.
   */
  private val noLibError = 101
  /**
   * Código de error emitido si nuestra librería falla.
   */
  private val executionError = 1
  /**
   * Código del programa si ha funcionado correctamente.
   */
  private val correctExecution = 0

  /**
   * Realiza el experimento definido a lo largo de la interfaz.
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
        parent.changeInformationText(
          "Realizando operación " + (i + 1) + " de " + commands.size + "...")
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
   * Comprueba si la ejecución ha lanzado un código de error conocido
   * @param Código de salida de una ejecución
   * @return Primer valor indica si la ejecución es buena o mala. Segundo,
   *   si el error es fatal.
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
   * Emite un diálogo final en función de cómo ha ido la ejecución
   * @param numErros Número de errores contabilizados
   * @param numExec Número total de ejecuciones
   */
  private def printFinalMessage(numErrors: Int,
                                posErrors: ArrayBuffer[Int],
                                numExec: Int,
                                lastOutput: Int,
                                fatalError: Boolean): Unit = {

    if (fatalError) {
      if (lastOutput == noSparkError) {
        Dialog.showMessage(null, "No se pudo localizar Spark y " +
          "las ejecuciones quedan canceladas.")
      } else {
        Dialog.showMessage(null, "No pudo localizarse la librería " +
          "ISAlgorithms y las ejecuciones quedan canceladas")
      }
    } else {
      if (numErrors == 0) {
        Dialog.showMessage(null, "Todas las operaciones han sido completadas.")
      } else if (numErrors < numExec) {
        var failedExecutions = ""
        for { x <- posErrors } {
          failedExecutions = failedExecutions.concat(x.toString).concat(", ")
        }
        failedExecutions = failedExecutions.dropRight(2)
        Dialog.showMessage(null, "Algunas operaciones no pudieron " +
          "completarse debido a un error. Identificador de las ejecución/es " +
          "fallidas: " + failedExecutions + ".")
      } else {
        Dialog.showMessage(null, "Ha habido un error en todas las ejecuciones.")
      }
    }
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
