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
 * Panel que contiene las opciones de ejecución de la interfaz.
 *
 * Contiene, además de un texto informativo sobre el estado de la interfaz,
 * un par de botones que permiten la inicialización de las tareas para las que
 * está pensada la interfaz: la creación de un fichero .zip con las
 * configuraciones seleccionadas previamente y la propia ejecución de los
 * experimentos.
 *
 * @constructor Genera un panel con los componentes necesarios
 *   para poder lanzar una ejecución.
 * @param parent Ventana desde donde se han invocado este panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DownMenuPanel(parent: UI) extends BorderPanel {

  /**
   * Texto que indica si existe una operación en proceso.
   *
   * De ser este el caso, este texto será descriptivo de la tarea que se
   * esté realizando.
   */
  val actualOperation = new Label()

  // Componentes
  /**
   * Botón para realizar la compresión de los ficheros para realizar un
   * experimento.
   */
  private val zipButton = new Button("ZIP")
  /**
   * Botón para ejecutar un experimento desde la interfaz.
   */
  private val executeButton = new Button("Ejecutar")

  // Añadimos los componentes

  layout += actualOperation -> West
  layout += new BoxPanel(Orientation.Horizontal) {
    contents += zipButton
    contents += executeButton
  } -> East

  // Listeners y eventos
  listenTo(zipButton)
  listenTo(executeButton)
  reactions += {

    case ButtonClicked(`executeButton`) => {
      if (parent.working) {
        Dialog.showMessage(this, "Hay una operación en curso")
      } else {
        var execThread = new Thread(new ExecutionsLauncher(parent))
        execThread.start()
      }

    }

    case ButtonClicked(`zipButton`) => {
      if (parent.working) {
        Dialog.showMessage(this, "Hay una operación en curso")
      } else {
        var zipThread = new Thread(new ZipCreator(parent))
        zipThread.start()
      }
    }

  }

}
