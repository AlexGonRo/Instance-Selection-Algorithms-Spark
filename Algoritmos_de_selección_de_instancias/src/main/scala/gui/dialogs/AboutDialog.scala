package gui.dialogs

import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import scala.swing.Swing

import javax.swing.BoxLayout
import javax.swing.JDialog
import javax.swing.JLabel

/**
 * Diálogo que muestra información sobre la aplicación.
 *
 * @constructor Genera una ventana con toda la información sobre el proyecto.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class AboutDialog extends JDialog {

  /**
   * Tamaño horizontal del diálogo.
   */
  private val xDim = 600
  /**
   * Tamaño vertical del diálogo.
   */
  private val yDim = 160

  // Elementos de la ventana
  /**
   * Texto con el título del proyecto.
   */
  private val titleLabel = new JLabel("Trabajo Final del Grado de Ingeniería " +
    "Informática de la Universidad de Burgos.")
  titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
  /**
   * Texto con el autor.
   */
  private val autorLabel = new JLabel("Autor: Alejandro González Rogel")
  autorLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
  /**
   * Texto con uno de los tutores.
   */
  private val tutor1Label = new JLabel("Tutor: Álvar Arnaiz González")
  tutor1Label.setAlignmentX(Component.CENTER_ALIGNMENT)
  /**
   * Texto con otro de los tutores.
   */
  private val tutor2Label = new JLabel("Carlos López Nozal")
  tutor2Label.setAlignmentX(Component.CENTER_ALIGNMENT)
  /**
   * Botón de aceptar.
   */
  private val okButton = new javax.swing.JButton("Ok")
  okButton.setAlignmentX(Component.CENTER_ALIGNMENT)

  // Añadimos todos los elementos al diálogo
  setTitle("About")
  setSize(new Dimension(xDim, yDim))
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(Swing.VStrut(10).peer)
  add(titleLabel)
  add(Swing.VStrut(10).peer)
  add(autorLabel)
  add(Swing.VStrut(15).peer)
  add(tutor1Label)
  add(tutor2Label)
  add(Swing.VStrut(10).peer)
  add(okButton)

  setVisible(true);

  okButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: ActionEvent): Unit = {
      okButtonAction(evt);
    }
  })

  /**
   * Acción realizada cuando se pulsa el botón de OK del diálogo.
   *
   * @param  evt Acción producida al pinchar sobre el botón.
   */
  private def okButtonAction(evt: ActionEvent): Unit =
    {
      this.dispose();
    }

}
