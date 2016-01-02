package gui.panel

import scala.swing._
import javax.swing.border.TitledBorder
import javax.swing.border.LineBorder
import java.awt.Color
import scala.swing.BorderPanel.Position._
import scala.swing.event.ButtonClicked
import gui.dialogs.SparkDialog
import scala.collection.mutable.ArrayBuffer
import scala.swing.ListView._
import javax.swing.border.EmptyBorder

/**
 * Panel que contiene todo lo referente a la configuración del clasificador.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class SparkPanel(val parent: MainFrame, orientation: Orientation.Value) extends BoxPanel(orientation) {

  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true), "Spark")

  var seqConfigurations = new ArrayBuffer[String]

  
  //Componentes
  val sparkHomeLabel = new Label("Spark_Home")
  val sparkHomeTextField = new TextField()
  val masterLabel = new Label("Master")
  val masterTextField = new TextField()
  val addButton = new Button("Añadir...")
  val rmButton = new Button("Eliminar")
  val confList = new ListView(seqConfigurations)
  confList.border = new LineBorder(Color.GRAY, 1, true) 
  confList.selection.intervalMode = IntervalMode.Single

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
    layout += new GridPanel(2,1){
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
      val confDialog = new SparkDialog(this, true)
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