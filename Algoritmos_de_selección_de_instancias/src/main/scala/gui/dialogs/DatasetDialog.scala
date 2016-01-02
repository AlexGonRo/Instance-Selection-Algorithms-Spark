package gui.dialogs

import java.awt.GridLayout

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
 * Ventana que permite la selección de un conjunto de datos y las selección de 
 * diferentes opciones que proporcionen las indicativas correctas para poder
 * leer el archivo.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DatasetDialog(myParent: DatasetPanel, modal: Boolean) extends JDialog {

  setTitle("Añadir nuevo conjunto de datos")
  var command = ""

  // Elementos de la ventana
  val cdLabel = new JLabel("Conjunto de datos")
  val cdTextField = new JTextField
  val chooseButton = new JButton("Elegir...")
  val classAttPosLabel = new JLabel("Atributo de clase")
  val classAttGroup = new ButtonGroup()
  val firstRadioButton = new JRadioButton("Primero")
  classAttGroup.add(firstRadioButton)
  val lastRadioButton = new JRadioButton("Último", true)
  classAttGroup.add(lastRadioButton)

  val headCheckBox = new JCheckBox("Cabecera")
  val numHeaderLinesTextField = new JTextField("0")

  var fileChooser = new JFileChooser()
  fileChooser.setCurrentDirectory(new java.io.File("."))
  fileChooser.setDialogTitle("Seleccionar conjunto de datos")
  fileChooser.addChoosableFileFilter(new CSVFilter())
  fileChooser.setAcceptAllFileFilterUsed(true)

  val okButton = new JButton("Añadir")
  val cancelButton = new JButton("Cancelar")


  // Paneles del diálogo
  val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(6, 10, 3, 10))
  panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS))
  panel1.add(cdLabel)
  panel1.add(cdTextField)
  panel1.add(chooseButton)

  val panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(3, 10, 6, 10))
  panel2.setLayout(new GridLayout(1, 2, 0, 20))
  val panel21 = new JPanel
  panel21.setLayout(new BoxLayout(panel21, BoxLayout.X_AXIS))
  panel21.add(classAttPosLabel)
  panel21.add(firstRadioButton)
  panel21.add(lastRadioButton)
  val panel22 = new JPanel
  panel22.setLayout(new BoxLayout(panel22, BoxLayout.X_AXIS))
  panel22.add(headCheckBox)
  panel22.add(numHeaderLinesTextField)

  panel2.add(panel21)
  panel2.add(panel22)

  val panel3 = new JPanel()
  panel3.setBorder(new EmptyBorder(3, 10, 6, 10))
  panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS))
  panel3.add(cancelButton)
  panel3.add(okButton)

  // Añadimos los paneles a nuestro dialogo
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)
  add(panel3)

  chooseButton.addActionListener(new java.awt.event.ActionListener() {
    def actionPerformed(evt: java.awt.event.ActionEvent) {
      val returnVal = fileChooser.showOpenDialog(chooseButton)

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        var path = fileChooser.getSelectedFile.getAbsolutePath
        cdTextField.setText(path)
      }
    }
  })

  okButton.addActionListener(new java.awt.event.ActionListener() {
    def actionPerformed(evt: java.awt.event.ActionEvent) {
      okActionPerformed(evt);
    }
  })

  cancelButton.addActionListener(new java.awt.event.ActionListener() {
    def actionPerformed(evt: java.awt.event.ActionEvent) {
      cancelActionPerformed(evt);
    }
  })

  pack()
  setLocationRelativeTo(myParent.peer)
  setModal(modal)
  setVisible(true);

  private def okActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      command += cdTextField.getText + " " //TODO PROBLEMA SI LA RUTA TIENE ESPACIOS
      if (firstRadioButton.isSelected()) {
        command += "-f "
      }
      if (headCheckBox.isSelected()) {
        command += "-hl " + numHeaderLinesTextField.getText + " "
      }
      this.dispose();
    }
  private def cancelActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      this.dispose();
    }

}
