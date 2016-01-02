package gui.panel

import java.awt.Font
import scala.swing._
import scala.swing.BorderPanel.Position._
import scala.swing.Panel
import javax.swing.JEditorPane
import scala.swing.event.ButtonClicked
import javax.swing.JOptionPane
import gui.UI
import gui.dialogs.AboutDialog
import java.awt.Desktop
import java.io.File

/**
 * Panel que contiene la cabecera de la interfaz gráfica.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class UpMenuPanel(val parent: UI) extends BorderPanel {

  // Componentes
  val aboutButton = new Button("About...")
  val helpButton = new Button("Help...")
  val titleLabel = new Label("TFG - Alejandro González Rogel")

  // Seleccionamos la fuente por defecto y asignamos al título la misma fuente
  // pero con un tamaño menor
  val outputArea = new JEditorPane
  val defaultFontFamily = outputArea.getFont.getFamily
  val defaultFontSize = outputArea.getFont.getSize
  titleLabel.font = new Font(defaultFontFamily, Font.BOLD, defaultFontSize + 5)

  // Añadimos los componentes
  layout += titleLabel -> West
  layout += new BoxPanel(Orientation.Horizontal) {
    contents += aboutButton
    contents += helpButton
  } -> East

  
  // Listener y eventos
  listenTo(aboutButton)
  listenTo(helpButton)
  reactions += {
    case ButtonClicked(`aboutButton`) => {
      new AboutDialog(false)
    }
    case ButtonClicked(`helpButton`) => {
      val htmlPath = "resources"+System.getProperty("file.separator")+"html"+
          System.getProperty("file.separator")+"help.html"
      val htmlFile = new File(htmlPath
          );
      Desktop.getDesktop().browse(htmlFile.toURI());
    }
  }

}