package gui.panel

import java.awt.Color

import scala.collection.mutable.ArrayBuffer
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.BorderPanel.Position.West
import scala.swing.Button
import scala.swing.GridPanel
import scala.swing.ListView
import scala.swing.ListView.IntervalMode
import scala.swing.event.ButtonClicked

import gui.UI
import gui.dialogs.DatasetDialog
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder

/**
 * Panel que contiene todo lo referente a la configuración del conjunto de
 * datos.
 *
 * @constructor Genera un panel que permita seleccionar una o varias
 * configuraciones para conjuntos de datos.
 * @param parent Ventana desde donde se han invocado este panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DatasetPanel(parent: UI) extends BorderPanel {

  // Elementos del panel
  /**
   * Configuraciones seleccionadas hasta el momento.
   */
  var seqConfigurations = new ArrayBuffer[String]
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

  // Añadimos todos los elementos al panel
  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true),
    "Conjuntos de datos")
  layout += new BorderPanel {
    border = new EmptyBorder(3, 10, 3, 10)
    layout += new GridPanel(2, 1) {
      border = new EmptyBorder(0, 0, 0, 7)
      vGap = 6
      contents += addButton
      contents += rmButton
    } -> West
    layout += confList -> Center
  } -> Center

  // Añadimos eventos y la capacidad de detectarlos.
  listenTo(addButton)
  listenTo(rmButton)
  reactions += {
    case ButtonClicked(`addButton`) => {
      addButtonAction()
    }
    case ButtonClicked(`rmButton`) => {
      rmButtonAction()
    }
  }

  /**
   * Abre un nuevo diálogo para permitir crear una nueva configuración.
   */
  private def addButtonAction(): Unit = {
    val confDialog = new DatasetDialog(this, true)
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

}
