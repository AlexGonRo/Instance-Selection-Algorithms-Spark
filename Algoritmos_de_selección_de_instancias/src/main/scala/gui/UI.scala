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
 * Main window of the user grafical interface (GUI).
 *
 * This window allows the user to launch or plan different experiments.
 *
 *
 * @constructor Draws the main window.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class UI extends MainFrame {

  /**
   * We are already executing a data mining job.
   */
  var working = false
  /**
   * Class of this library that launches the data mining jobs.
   *
   * This parameter should not be changed.
   */
  val execClass = "launcher.ExperimentLauncher"

  /**
   * Execution time.
   * 
   * The only supported execution is the one that contains
   * an filter task and a classification task.
   *
   * This parameter should not be changed
   * TODO We need to create a grafical interface that
   *    will allow for different data mining jobs.
   */
  val execType = "ISClassExec"

  /**
   * Main window width.
   */
  private val xDim = 800
  /**
   * Main window height
   */
  private val yDim = 460
  /**
   * Header.
   */
  val upMenuPanel = new UpMenuPanel(this)
  /**
   * Panel at the bottom of the window.
   */
  val downMenuPanel = new DownMenuPanel(this)
  /**
   * Panel for selecting the dataset.
   */
  val datasetPanel = new DatasetPanel(true, "Conjunto de Datos")
  /**
   * Panel for selecting the filtering techniques.
   */
  val filterPanel = new FilterPanel(true, "Filtro")
  /**
   * Panel for selecting the classification algorithm and the
   * cross-validation parameters.
   */
  val classifierPanel = new ClassifierPanel(this, Orientation.Vertical)
  /**
   * Panel with Spark options.
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
   * Collect information from all the panels of the interface and generates all possible 
   * execution commands for launching those data mining tasks.
   * 
   * @return Secuence with all the commands.
   */
  def getAllExecutionCommands(): IndexedSeq[String] = {

    // Collect all the information from the panels.
    val sparkCommonOptions = sparkPanel.getCommonSparkOptions()
    val sparkHome = sparkCommonOptions(0)
    val sparkMaster = sparkCommonOptions(1)
    var sparkOptions = sparkPanel.getSparkConfOptions()
    var datasetOptions = datasetPanel.seqConfigurations
    var filterOptions = filterPanel.seqConfigurations
    var classifierOptions = classifierPanel.getClassifierOptions()
    var crossValidationOptions =
      classifierPanel.getCrossValidationOptions()
    // Stores all the execution commands.
    var commands: ArrayBuffer[String] = ArrayBuffer.empty[String]

    // Generate all possible executions.
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
    // Add Spark parameters to all the commands.
    val completeCommands = for { i <- 0 until commands.size }
      yield sparkHome + " " + sparkMaster + " " + commands(i)

    completeCommands

  }

  /**
   * Changes the value of the string that indicated whether
   * there is an execution already in progress.
   *
   * @param sentence New string.
   */
  def changeInformationText(sentence: String): Unit = {
    downMenuPanel.actualOperation.text = sentence

  }

}
