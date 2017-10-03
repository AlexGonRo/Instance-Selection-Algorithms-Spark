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
 * GUI header.
 *
 * It shows a couple of buttons that allow access to some basic information about this library and a small tutorial on how to use it.
 *
 * @param parent Parent panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class UpMenuPanel(val parent: UI) extends BorderPanel {

  // PANEL COMPONENTS
  /**
   * Width of the help dialog.
   */
  private val helpHSize = 500
  /**
   * Height of the help dialog.
   */
  private val helpVSize = 900
  /**
   * Button that shows information about this library.
   */
   // TODO Hardcoded text
  private val aboutButton = new Button(“About…”)
  /**
   * Help button.
   */
   // TODO Hardcoded text
  private val helpButton = new Button(“Help…”)
  /**
   * Title of this library.
   */
  private val titleLabel = new Label("TFG - Alejandro González Rogel")
  titleLabel.font =
    new Font(titleLabel.font.toString, Font.BOLD, titleLabel.font.getSize + 5)

  // Add all the components.
  layout += titleLabel -> West
  layout += new BoxPanel(Orientation.Horizontal) {
    contents += aboutButton
    contents += helpButton
  } -> East

  // Listener and events.
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
   * Shows a dialog panel with information about the library.
   */
  private def helpButtonAction(): Unit = {

    val fsep = System.getProperty("file.separator")
    val htmlPath = fsep + "resources" + fsep + "gui" + fsep + "html" +
      fsep + "help.html"
    val in = getClass.getResource(htmlPath).toString()
    val url = new URL(in);
    val panelEditor = new JEditorPane(url);
    // TODO Hardcoded text
    val newFrame = new JFrame(“Help”);
    newFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    newFrame.add(new JScrollPane(panelEditor));
    newFrame.setSize(new Dimension(helpVSize, helpHSize))
    newFrame.setVisible(true);

  }

}
