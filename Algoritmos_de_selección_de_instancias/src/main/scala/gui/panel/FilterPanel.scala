package gui.panel

import scala.swing._
import javax.swing.JFileChooser
import scala.swing.event.ButtonClicked
import scala.swing.event.MouseClicked
import javax.swing.border.LineBorder
import java.awt.Color
import javax.swing.border.TitledBorder
import gui.UI
import scala.collection.mutable.ArrayBuffer
import scala.swing.ListView._
import gui.dialogs.DatasetDialog
import scala.swing.BorderPanel.Position._
import gui.dialogs.FilterDialog
import javax.swing.border.EmptyBorder

/**
 * Panel que contiene todo lo referente a la selección y configuración del filtro.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class FilterPanel(val parentPanel: UI) extends BorderPanel { 
  
  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true),
    "Filtro")
  
  // Componentes del panel
  val addButton = new Button("Añadir...")
  val rmButton = new Button("Eliminar")
  var seqConfigurations = new ArrayBuffer[String]
  val confList = new ListView(seqConfigurations)
  confList.border = new LineBorder(Color.GRAY, 1, true)
  confList.selection.intervalMode = IntervalMode.Single

  //Añadimos los componentes
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
      val confDialog = new FilterDialog(this, true)
      if (confDialog.command != "") {
        seqConfigurations += confDialog.command
        confList.listData = seqConfigurations
      }
    }
    case ButtonClicked(`rmButton`) => {
      if (confList.selection.items.iterator.hasNext == true) {
        val confSelected = confList.selection.indices.head
        seqConfigurations.remove(confSelected)
        confList.listData = seqConfigurations
      }

    }

  }

}