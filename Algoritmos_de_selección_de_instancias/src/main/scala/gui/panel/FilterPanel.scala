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
import gui.dialogs.FilterDialog
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder

/**
 * Panel que contiene todo lo referente a la selección y configuración del
 * filtro.
 *
 * @constructor Genera un panel que permita seleccionar una o varias
 * configuraciones para filtros.
 * @param parent Ventana desde donde se han invocado este panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class FilterPanel(val parent: UI) extends BorderPanel {

  /**
   * Configuraciones seleccionadas hasta el momento.
   */
  var seqConfigurations = new ArrayBuffer[String]

  // Componentes del panel
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

  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true),
    "Filtro")
  // Añadimos los componentes
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

  // Listener y eventos
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
    val confDialog = new FilterDialog(this.peer, true)
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
