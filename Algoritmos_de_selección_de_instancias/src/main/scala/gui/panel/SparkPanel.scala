package gui.panel

import java.awt.Color

import scala.collection.mutable.ArrayBuffer
import scala.swing.BoxPanel
import scala.swing.Dialog
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.TextField

import gui.UI
import gui.dialog.SparkDialog
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder

/**
 * Panel with information about the Spark configuration.
 *
 * This panel has two main sections: one allows to select Spark parameters that
 * will affect all the programmed executions and a second section that allows
 * for the configuration of some parameters that will be execution specific.
 *
 * @constructor Create and display this panel.
 * @param parent Parent window where this panel will be drawn.
 * @param Position for this panel on the general layout.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class SparkPanel(val parent: UI,
                 orientation: Orientation.Value) extends BoxPanel(orientation) {

  /**
   * Defined configurations so far.
   */
  var seqConfigurations = new ArrayBuffer[String]

  //COMPONENTS OF THE INTERFACE
  /**
   * Up and down margins for the children panels.
   */
  private val tdb = 3
  /**
   * Latteral margins for the children panels.
   */
  private val lb = 10
  /**
   * Distance between the Spark path text and the text box.
   */
  private val sparkHGap = 14
  /**
   * Distance between the “Master” text and the text box.
   */
  private val masterHGap = 51
  /**
   * Text for the “Spark path” label
   */
  private val sparkHomeLabel = new Label("Spark_Home")
  /**
   * Text box for Spark’s path.
   */
  private val sparkHomeTextField = new TextField()
  sparkHomeTextField.tooltip = "Directorio de instalación de Spark."
  sparkHomeTextField.text = sys.env.get("SPARK_HOME").getOrElse("")

  /**
   * Text for the Master label.
   */
  private val masterLabel = new Label("Master")
  /**
   * Text field for writing the master node.
   */
  private val masterTextField = new TextField()
  masterTextField.tooltip = "URL del nodo master. local[n]" +
    " para ejecuciones locales, spark://ruta:puerto para el resto"

  private val sparkListPanel = new SparkListPanel

  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true), "Spark")

  // Add all the component to the panel.

  contents += new BoxPanel(Orientation.Horizontal) {
    border = new EmptyBorder(tdb, lb, tdb, lb)
    contents += sparkHomeLabel
    contents += Swing.HStrut(sparkHGap)
    contents += sparkHomeTextField
  }

  contents += new BoxPanel(Orientation.Horizontal) {
    border = new EmptyBorder(tdb, lb, tdb, lb)
    contents += masterLabel
    contents += Swing.HStrut(masterHGap)
    contents += masterTextField
  }
  contents += sparkListPanel

  /**
   * Collect all the options that are common for all the executions.
   *
   * The common options are: Spark_Home y Master.
   *
   * @return Common options.
   */
  def getCommonSparkOptions(): Array[String] = {

    var sparkHomeText = sparkHomeTextField.text
    // If the path does not have a file separator at the end, add it.
    if (!sparkHomeText.endsWith(System.getProperty("file.separator"))) {
      sparkHomeText = sparkHomeText.concat(System.getProperty("file.separator"))
    }
    Array(sparkHomeText, masterTextField.text)
  }

  /**
   * Select all the individual Spark configurations defined so far.
   *
   * @return Specific Spark configurations.
   */
  def getSparkConfOptions(): Array[String] = {
    sparkListPanel.seqConfigurations.toArray
  }

  /**
   * Panel that shows all the Spark configurations defined so far.
   *
   * It also contains a few buttons to add or delete elements.
   */
  private class SparkListPanel extends ListPanelAbst {

    protected def addButtonAction(): Unit = {
      val confDialog = new SparkDialog(this.peer, true)
      val conf = confDialog.command
      if (confAlreadyExists(conf)) {
	// TODO Hardcoded text.
        Dialog.showMessage(this, “The chosen configuraiton " +
          “had been already defined.”)
      } else if (conf != "") {
        seqConfigurations += conf
        confList.listData = seqConfigurations
      }
    }

  }

}
