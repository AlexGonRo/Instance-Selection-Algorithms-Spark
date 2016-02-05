package gui.panel

import java.awt.Font
import java.net.URL

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.East
import scala.swing.BorderPanel.Position.West
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dimension
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.event.ButtonClicked

import gui.UI
import gui.dialog.AboutDialog
import javax.swing.JEditorPane
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.WindowConstants

/**
 * Panel que contiene la cabecera de la interfaz gráfica.
 *
 * Esta cabecerá está dedicada a posibilitar el acceso a información sobre
 * la aplicación y haciendo posible el poder acceder a un área de ayuda donde
 * se explique el funcionamiento de cada uno de los componentes de la interfaz.
 *
 * @constructor Genera un panel que permita acceder a las
 *   secciones de ayuda e información.
 * @param parent Ventana desde donde se han invocado este panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class UpMenuPanel(val parent: UI) extends BorderPanel {

  // Componentes
  /**
   * Tamaño horizontal de la ventana de ayuda.
   */
  private val helpHSize = 500
  /**
   * Tamaño vertical de la ventana de ayuda.
   */
  private val helpVSize = 900
  /**
   * Botón para invocar la información sobre el proyecto.
   */
  private val aboutButton = new Button("Acerca de...")
  /**
   * Botón para invocar la ayuda.
   */
  private val helpButton = new Button("Ayuda...")
  /**
   * Título de la interfaz.
   */
  private val titleLabel = new Label("TFG - Alejandro González Rogel")
  titleLabel.font =
    new Font(titleLabel.font.toString, Font.BOLD, titleLabel.font.getSize + 5)

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
      new AboutDialog
    }
    case ButtonClicked(`helpButton`) => {
      helpButtonAction
    }
  }

  /**
   * Muestra un cuadro de diálogo con la ayuda de la aplicación.
   */
  private def helpButtonAction(): Unit = {

    val fsep = System.getProperty("file.separator")
    val htmlPath = fsep + "resources" + fsep + "gui" + fsep + "html" +
      fsep + "help.html"
    val in = getClass.getResource(htmlPath).toString()
    val url = new URL(in);
    val panelEditor = new JEditorPane(url);
    val newFrame = new JFrame("Ayuda");
    newFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    newFrame.add(new JScrollPane(panelEditor));
    newFrame.setSize(new Dimension(helpVSize, helpHSize))
    newFrame.setVisible(true);

  }

}
