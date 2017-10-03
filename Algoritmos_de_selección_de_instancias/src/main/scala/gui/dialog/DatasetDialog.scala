package gui.dialog

import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import gui.component.CSVFilter
import gui.panel.DatasetPanel
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTextField
import javax.swing.border.EmptyBorder

/**
 * Allows the user to select a dataset and specify all the necessary parameters
 * to read such dataset.
 *
 * @constructor Creates and draws the panel.
 * @param  myParent  Parent panel.
 * @param  modal  If the dialog must block the parent window or not.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DatasetDialog(myParent: DatasetPanel, modal: Boolean) extends JDialog {

  /**
   * Part of the execution command that states which and how a dataset should be read.
   */
  var command = ""

  // PANEL COMPONENTS
  /**
   * Up and down margins of the children panels.
   */
  private val tdb = 6
  /**
   * Lateral margins of the children panels.
   */
  private val lb = 10
  /**
   * Distance between labels and the text boxes that allow us to configure those
   * panels.
   */
  private val vgap = 20
  /**
   * Dataset label.
   */
  // TODO More hardcoded text.
  private val cdLabel = new JLabel(“Dataset”)
  /**
   * Text box for the path to the dataset.
   */
  private val cdTextField = new JTextField
  /**
   * Button that allows us to choose the dataset.
   */
  private val chooseButton = new JButton(“Choose…”)
  /**
   * Label for the “Class attribute”.
   */
  private val classAttPosLabel = new JLabel(“Class attribute“)
  /**
   * Options for the class attribute position.
   */
  private val classAttGroup = new ButtonGroup()
  /**
   * Option to highlight that the first attribute is the class attribute.
   */
  private val firstRadioButton = new JRadioButton(“First”)
  classAttGroup.add(firstRadioButton)
  /**
   * Option to highlight that the last attribute is the class attribute.
   */
  private val lastRadioButton = new JRadioButton("Último", true)
  classAttGroup.add(lastRadioButton)

  /**
   * Header label.
   */
  private val headCheckBox = new JCheckBox(“Header”)
  /**
   * Text field for the number of header lines.
   */
  private val numHeaderLinesTextField = new JTextField("0")

  /**
   * File selector for the dataset.
   */
  private var fileChooser = new JFileChooser()
  fileChooser.setCurrentDirectory(new java.io.File("."))
  fileChooser.setDialogTitle("Seleccionar conjunto de datos")
  fileChooser.addChoosableFileFilter(new CSVFilter())
  fileChooser.setAcceptAllFileFilterUsed(true)

  /**
   * “Ok” button.
   */
  private val okButton = new JButton("Añadir")
  /**
   * “Cancel” button.
   */
  private val cancelButton = new JButton("Cancelar")

  // DIALOG PANELS.
  /**
   * Panel that allows the user to select a dataset.
   */
  private val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(tdb, lb, tdb / 2, lb))
  panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS))
  panel1.add(cdLabel)
  panel1.add(cdTextField)
  panel1.add(chooseButton)

  /**
   * Panel with the different options for reading the dataset.
   */
  private val panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(tdb / 2, lb, tdb, lb))
  panel2.setLayout(new GridLayout(1, 2, 0, vgap))
  /**
   * Options that allow the user to indicate where the class attribute
   * is.
   */
  private val panel21 = new JPanel
  panel21.setLayout(new BoxLayout(panel21, BoxLayout.X_AXIS))
  panel21.add(classAttPosLabel)
  panel21.add(firstRadioButton)
  panel21.add(lastRadioButton)
  /**
   * Options to remove the header of the dataset (if any).
   */
  private val panel22 = new JPanel
  panel22.setLayout(new BoxLayout(panel22, BoxLayout.X_AXIS))
  panel22.add(headCheckBox)
  panel22.add(numHeaderLinesTextField)

  panel2.add(panel21)
  panel2.add(panel22)

  /**
   * “Ok” and “Cancel” buttons
   */
  private val panel3 = new JPanel()
  panel3.setBorder(new EmptyBorder(tdb / 2, lb, tdb, lb))
  panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS))
  panel3.add(cancelButton)
  panel3.add(okButton)

  // Add panels to our dialog.
  setTitle("Añadir nuevo conjunto de datos")
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)
  add(panel3)

  // Add listeners.
  chooseButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: ActionEvent): Unit = {
      chooseActionPerformed(evt)
    }
  })

  okButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: ActionEvent): Unit = {
      okActionPerformed(evt);
    }
  })

  cancelButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: ActionEvent): Unit = {
      cancelActionPerformed(evt);
    }
  })

  pack()
  setLocationRelativeTo(myParent.peer)
  setModal(modal)
  setVisible(true);

  /**
   * Action after pressing the “Select” button.
   *
   * @param  evt  Event generated after pressing the “Select” button.
   */
  private def chooseActionPerformed(evt: ActionEvent): Unit =
    {
      val returnVal = fileChooser.showOpenDialog(chooseButton)

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        var path = fileChooser.getSelectedFile.getAbsolutePath
        cdTextField.setText(path)
      }
    }

  /**
   * Action for when the user clicks the “Ok” button.
   *
   * @param  evt  Event of the “Ok” button.
   */
  private def okActionPerformed(evt: ActionEvent): Unit =
    {
      command += "\"" + cdTextField.getText + "\" "
      if (firstRadioButton.isSelected()) {
        command += "-first "
      }
      if (headCheckBox.isSelected()) {
        command += "-hl " + numHeaderLinesTextField.getText + " "
      }
      this.dispose();
    }

  /**
   * Action for when the user clicks the “Cancel” button.
   *
   * @param  evt  Event of the “Cancel” button.
   */
  private def cancelActionPerformed(evt: ActionEvent): Unit =
    {
      this.dispose();
    }

}
