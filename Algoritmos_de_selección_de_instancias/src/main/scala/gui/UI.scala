package gui

import scala.swing._
import java.awt.Color
import gui.panel._

/**
 * Frame principal de la interfaz gráfica.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class UI extends MainFrame {

  title = "Spark IS GUI"
  preferredSize = new Dimension(800, 500)
  val upMenuPanel = new UpMenuPanel(this)
  val downMenuPanel = new DownMenuPanel(this)
  val datasetPanel = new DatasetPanel(this)    
  val filterPanel = new FilterPanel(this)
  val classifierPanel = new ClassifierPanel(this,Orientation.Vertical)
  val sparkPanel = new SparkPanel(this,Orientation.Vertical)

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

}


//TODO
//TODO
//EL MODELO SON LOS CLASIFICADORES Y FILTROS QUE HEMOS CREADO
//LA VISTA ES LA QUE DEFINIMOS EN MFrame
//LOS CONTROLADORES TIENEN SUS TRES CLASES APARTE PARA RESPONDER A LOS EVENTOS
