package gui.dialogs

import javax.swing.JDialog
import java.awt.FlowLayout
import java.awt.Dialog
import javax.swing.JLabel
import scala.annotation.meta.field
import java.awt.Dimension
import scala.annotation.meta.field
import javax.swing.JFrame
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JButton
import java.awt.Component
import scala.swing.Swing

/**
 * Diálogo que muestra información sobre la aplicación.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class AboutDialog(model: Boolean) extends JDialog {

  setTitle("About")
  setSize(new Dimension(600, 160))

  // Elementos de la ventana
  val titleLabel = new JLabel("Trabajo Final del Grado de Ingeniería Informática de" +
    " la Universidad de Burgos.")
  titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
  val autorLabel = new JLabel("Autor: Alejandro González Rogel")
  autorLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
  val tutor1Label = new JLabel("Tutor: Álvar Arnaiz González")
  tutor1Label.setAlignmentX(Component.CENTER_ALIGNMENT)
  val tutor2Label = new JLabel("Carlos López Nozal")
  tutor2Label.setAlignmentX(Component.CENTER_ALIGNMENT)

  val okButton = new javax.swing.JButton("Ok")
  okButton.setAlignmentX(Component.CENTER_ALIGNMENT)

  // Añadimos todos los componentes al diálogo
  
  add(Swing.VStrut(10).peer)
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(titleLabel)
  add(Swing.VStrut(10).peer)
  add(autorLabel)
  add(Swing.VStrut(15).peer)
  add(tutor1Label)
  add(tutor2Label)
  add(Swing.VStrut(10).peer)
  add(okButton)

  setVisible(true);

  okButton.addActionListener(new java.awt.event.ActionListener() {
    def actionPerformed(evt: java.awt.event.ActionEvent) {
      jbtOKActionPerformed(evt);
    }
  })

  private def jbtOKActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      this.dispose();
    }

}