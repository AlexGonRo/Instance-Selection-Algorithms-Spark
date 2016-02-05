package gui

import scala.collection.mutable.ArrayBuffer
import scala.swing.BoxPanel
import scala.swing.Dimension
import scala.swing.GridPanel
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.Swing

import gui.panel.ClassifierPanel
import gui.panel.DatasetPanel
import gui.panel.DownMenuPanel
import gui.panel.FilterPanel
import gui.panel.SparkPanel
import gui.panel.UpMenuPanel
import utils.ArgsSeparator.CLASSIFIER_SEPARATOR
import utils.ArgsSeparator.CROSSVALIDATION_SEPARATOR
import utils.ArgsSeparator.FILTER_SEPARATOR
import utils.ArgsSeparator.READER_SEPARATOR

/**
 * Ventana principal de la interfaz gráfica.
 *
 * Esta ventana proporciona una serie de elementos que permitan al usuario
 * de la aplicación configurar adecuadamente experimentos para nuestra
 * biblioteca.
 *
 *
 * @constructor Genera la ventana principal de nuestra aplicación.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class UI extends MainFrame {

  /**
   * Indica si estamos ejecutando actualmente alguna operación.
   */
  var working = false
  /**
   * Clase principal para el lanzamiento de ejecuciones.
   *
   * Actualmente este parámetro no es configurable.
   */
  val execClass = "launcher.ExperimentLauncher"

  /**
   * Tipo de ejecución.
   *
   * Actualmente este parámetro no es configurable.
   */
  val execType = "ISClassExec"

  /**
   * Anchura de la ventana.
   */
  private val xDim = 800
  /**
   * Largo de la ventana.
   */
  private val yDim = 460
  /**
   * Espacio superior de la interfaz.
   */
  val upMenuPanel = new UpMenuPanel(this)
  /**
   * Espacio inferior de la interfaz.
   */
  val downMenuPanel = new DownMenuPanel(this)
  /**
   * Espacio dedicado a la selección de conjuntos de datos.
   */
  val datasetPanel = new DatasetPanel(true, "Conjunto de Datos")
  /**
   * Espacio dedicado a la selección de filtros.
   */
  val filterPanel = new FilterPanel(true, "Filtro")
  /**
   * Espacio dedicado a la selección de clasificador y a los parámetros
   * de la validación cruzada.
   */
  val classifierPanel = new ClassifierPanel(this, Orientation.Vertical)
  /**
   * Espacio dedicado a la selección las opciones de Spark.
   */
  val sparkPanel = new SparkPanel(this, Orientation.Vertical)

  title = "Spark IS GUI"
  preferredSize = new Dimension(xDim, yDim)
  contents = new BoxPanel(Orientation.Vertical) {
    contents += upMenuPanel
    contents += Swing.VStrut(10)
    contents += sparkPanel
    contents += Swing.VStrut(10)
    contents += datasetPanel
    contents += Swing.VStrut(10)
    contents += new GridPanel(1, 2) {
      contents += filterPanel
      contents += classifierPanel
    }
    contents += Swing.VStrut(10)
    contents += downMenuPanel
    border = Swing.EmptyBorder(10, 10, 10, 10)
  }

  /**
   * Recolecta toda la información contenida en los diferentes paneles de la
   * aplicación y genera con ella todos los comandos de ejecución posibles.
   *
   * @return Secuencia con todos los comandos de ejecución.
   */
  def getAllExecutionCommands(): IndexedSeq[String] = {

    // Seleccionamos todas las opciones existentes en cada uno de los paneles
    // de la aplicación
    val sparkCommonOptions = sparkPanel.getCommonSparkOptions()
    val sparkHome = sparkCommonOptions(0)
    val sparkMaster = sparkCommonOptions(1)
    var sparkOptions = sparkPanel.getSparkConfOptions()
    var datasetOptions = datasetPanel.seqConfigurations
    var filterOptions = filterPanel.seqConfigurations
    var classifierOptions = classifierPanel.getClassifierOptions()
    var crossValidationOptions =
      classifierPanel.getCrossValidationOptions()
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
   * Cambia el texto que informa de la operación que se está realizando
   * actualmente.
   * @param sentence Nuevo texto
   */
  def changeInformationText(sentence: String): Unit = {
    downMenuPanel.actualOperation.text = sentence

  }

}
