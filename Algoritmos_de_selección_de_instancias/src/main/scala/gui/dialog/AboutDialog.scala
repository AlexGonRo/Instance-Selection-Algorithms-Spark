package gui.dialog

import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import scala.swing.Swing

import javax.swing.BoxLayout
import javax.swing.JDialog
import javax.swing.JLabel

/**
 * Dialog box with information about this library.
 *
 * @constructor Creates a new dialog.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class AboutDialog extends JDialog {

  /**
   * Width of the dialog box.
   */
  private val xDim = 600
  /**
   * Height of the dialog box.
   */
  private val yDim = 160

  /**
   * Horizontal space between elements.
   */
  private val hspace = 10

  /**
   * Title of the project.
   */
  // TODO HARDCODED TEXT
  private val titleLabel = new JLabel("Trabajo Final del Grado de Ingeniería " +
    "Informática de la Universidad de Burgos.")
  titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
  /**
   * Author text.
   */
  private val autorLabel = new JLabel("Autor: Alejandro González Rogel")
  autorLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
  /**
   * First supervisor text.
   */
  private val tutor1Label = new JLabel("Tutor: Álvar Arnaiz González")
  tutor1Label.setAlignmentX(Component.CENTER_ALIGNMENT)
  /**
   * Second supervisor text.
   */
  private val tutor2Label = new JLabel("Carlos López Nozal")
  tutor2Label.setAlignmentX(Component.CENTER_ALIGNMENT)
  /**
   * Ok button.
   */
  private val okButton = new javax.swing.JButton("Ok")
  okButton.setAlignmentX(Component.CENTER_ALIGNMENT)

  // Add all the elements.
  // TODO Hardcoded text
  setTitle(“About…”)
  setSize(new Dimension(xDim, yDim))
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(Swing.VStrut(hspace).peer)
  add(titleLabel)
  add(Swing.VStrut(hspace).peer)
  add(autorLabel)
  add(Swing.VStrut(hspace).peer)
  add(tutor1Label)
  add(tutor2Label)
  add(Swing.VStrut(hspace).peer)
  add(okButton)

  setVisible(true);

  okButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: ActionEvent): Unit = {
      okButtonAction(evt);
    }
  })

  /**
   * Action for when the user clicks the OK button.
   *
   * @param  evt Event produced when the OK button is clicked.
   */
  private def okButtonAction(evt: ActionEvent): Unit =
    {
      this.dispose();
    }

}
