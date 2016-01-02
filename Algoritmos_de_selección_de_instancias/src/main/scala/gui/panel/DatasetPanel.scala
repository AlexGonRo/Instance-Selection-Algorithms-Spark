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
import javax.swing.border.EmptyBorder


/**
 * Panel que contiene todo lo referente a la configuración del conjunto de datos.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DatasetPanel(parent: UI) extends BorderPanel {

  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true),
    "Conjuntos de datos")

  // Elementos del panel
  val addButton = new Button("Añadir...")
  val rmButton = new Button("Eliminar")
  var seqConfigurations = new ArrayBuffer[String]
  val confList = new ListView(seqConfigurations)
  confList.border = new LineBorder(Color.GRAY, 1, true) 
  confList.selection.intervalMode = IntervalMode.Single
  

  // Añadimos todos los elementos al panel
  
  layout += new BorderPanel {
    border = new EmptyBorder(3, 10, 3, 10)
    layout += new GridPanel(2, 1) {
      border = new EmptyBorder(0,0,0,7)
      vGap = 6
      contents += addButton
      contents += rmButton
    } -> West
    layout += confList -> Center
  } -> Center
  
  
  //Añadimos eventos y la capacidad de detectarlos.
  listenTo(addButton)
  listenTo(rmButton)
  reactions += {
    case ButtonClicked(`addButton`) => {
      val confDialog = new DatasetDialog(this, true)
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