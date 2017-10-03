package gui.panel

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.East
import scala.swing.BorderPanel.Position.West
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.event.ButtonClicked

import gui.UI
import gui.thread.ExecutionsLauncher
import gui.thread.ZipCreator

/**
 * Panel that allows the user to execute an action (execute task or create .zip).
 *
 * It also contains a text that informs the user if there is any other
 * operation running.
 *
 * @constructor Create and draw this panel.
 * @param parent Parent panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DownMenuPanel(parent: UI) extends BorderPanel {

  /**
   * Text that informs the user if there is any other operation already running.
   *
   */
  val actualOperation = new Label()

  // Componentes
  /**
   * Button that starts the creation of a .zip file with all the necessary
   * information and data for the execution of a data mining task in a remote
   * computer.
   */
  private val zipButton = new Button("ZIP")
  /**
   * Button that executes a data mining task directly on this computer.
   */
  private val executeButton = new Button(“Run”)

  // Add all the component.

  layout += actualOperation -> West
  layout += new BoxPanel(Orientation.Horizontal) {
    contents += zipButton
    contents += executeButton
  } -> East

  // Listeners and events
  listenTo(zipButton)
  listenTo(executeButton)
  reactions += {

    case ButtonClicked(`executeButton`) => {
      if (parent.working) {
        // TODO More hardcoded text.
        Dialog.showMessage(this, “There is some other operation already running.“)
      } else {
        var execThread = new Thread(new ExecutionsLauncher(parent))
        execThread.start()
      }

    }

    case ButtonClicked(`zipButton`) => {
      if (parent.working) {
        // TODO More hardcoded text.
        Dialog.showMessage(this, "There is some other operation already running.")
      } else {
        var zipThread = new Thread(new ZipCreator(parent))
        zipThread.start()
      }
    }

  }

}
