package gui.panel

import java.awt.Color

import scala.collection.mutable.ArrayBuffer
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.BorderPanel.Position.West
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.GridPanel
import scala.swing.Label
import scala.swing.ListView
import scala.swing.ListView.IntervalMode
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.TextField
import scala.swing.event.ButtonClicked

import gui.UI
import gui.dialogs.SparkDialog
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder

/**
 * Panel que contiene todo lo referente a la configuración del clasificador.
 *
 * @constructor Genera un panel que permita seleccionar una o varias
 * configuraciones para Spark.
 * @param parent Ventana desde donde se han invocado este panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class SparkPanel(val parent: UI,
                 orientation: Orientation.Value) extends BoxPanel(orientation) {

  /**
   * Configuraciones seleccionadas hasta el momento.
   */
  var seqConfigurations = new ArrayBuffer[String]

  // Componentes
  /**
   * Texto descriptivo para el campo que indica el lugar de instalación
   * de Spark.
   */
  private val sparkHomeLabel = new Label("Spark_Home")
  /**
   * Campo donde introducir el lugar de instalación de Spark.
   */
  private val sparkHomeTextField = new TextField()
  sparkHomeTextField.tooltip = "Directorio de instalación de Spark"
  /**
   * Texto descriptivo para indicar el master a usar en la ejecución.
   */
  private val masterLabel = new Label("Master")
  /**
   * Campo donde indicar el master a usar en la ejecución.
   */
  private val masterTextField = new TextField()
  sparkHomeTextField.tooltip = "URL del nodo master. Local[_]" +
    " para ejecuciones locales."

  /**
   * Botón para añadir una nueva configuración
   */
  private val addButton = new Button("Añadir...")
  /**
   * Botón para eliminar la configuración seleccioanda.
   */
  private val rmButton = new Button("Eliminar")
  /**
   * Lista que muestra las configuraciones elegidas hasta el momento.
   */
  private val confList = new ListView(seqConfigurations)
  confList.border = new LineBorder(Color.GRAY, 1, true)
  confList.selection.intervalMode = IntervalMode.Single

  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true), "Spark")

  // Añadimos los componentes al panel

  contents += new BoxPanel(Orientation.Horizontal) {
    border = new EmptyBorder(3, 10, 3, 10)
    contents += sparkHomeLabel
    contents += Swing.HStrut(14)
    contents += sparkHomeTextField
  }

  contents += new BoxPanel(Orientation.Horizontal) {
    border = new EmptyBorder(3, 10, 3, 10)
    contents += masterLabel
    contents += Swing.HStrut(51)
    contents += masterTextField
  }
  contents += new BorderPanel() {
    border = new EmptyBorder(3, 10, 3, 10)
    layout += new GridPanel(2, 1) {
      border = new EmptyBorder(0, 0, 0, 7)
      vGap = 6
      contents += addButton
      contents += rmButton
    } -> West
    layout += confList -> Center
  }

  // Listener y eventos
  listenTo(addButton)
  listenTo(rmButton)
  reactions += {
    case ButtonClicked(`addButton`) => {
      addButtonAction
    }
    case ButtonClicked(`rmButton`) => {
      rmButtonAction
    }
  }

  /**
   * Abre un nuevo diálogo para permitir crear una nueva configuración.
   */
  private def addButtonAction(): Unit = {
    val confDialog = new SparkDialog(this.peer, true)
    if (confDialog.command != "") {
      seqConfigurations += confDialog.command
      confList.listData = seqConfigurations
    }
  }

  /**
   * Elimina la configuración actualmente seleccionada en la lista.
   */
  private def rmButtonAction(): Unit = {
    if (confList.selection.items.iterator.hasNext) {
      val confSelected = confList.selection.indices.head
      seqConfigurations.remove(confSelected)
      confList.listData = seqConfigurations
    }
  }

  /**
   * Selecciona las opciones de Spark comunes a todas las ejecuciones.
   *
   * Los parámetros comunes son: Spark_Home y Master.
   *
   * @return Opciones comunes a todas las ejecuciones.
   */
  def getCommonSparkOptions(): Array[String] = {
    Array(sparkHomeTextField.text, masterTextField.text)
  }

}
