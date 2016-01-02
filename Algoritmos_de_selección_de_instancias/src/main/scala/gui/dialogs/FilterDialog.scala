package gui.dialogs

import java.awt.Dimension
import java.awt.GridLayout

import scala.collection.mutable.ArrayBuffer

import gui.panel.FilterPanel
import instanceSelection.abstracts.AbstractIS
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.EmptyBorder

/**
 * Ventana que permite la selección de un un filtro para el conjunto de datos
 * así como ajustar los parámetros del mismo.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class FilterDialog(myParent: FilterPanel, modal: Boolean) extends JDialog {

  setTitle("Añadir nuevo filtro")
  var command = ""
  var algorithmOptions: Iterable[utils.Option] = Iterable.empty[utils.Option]
  var isAlgorithm: AbstractIS = null 
  var dinamicOptions: ArrayBuffer[JComponent] = ArrayBuffer.empty[JComponent]
  
  // Componentes de la ventana
  val filterLabel = new JLabel("Filtro")
  val filterTextField = new JTextField("None")
  val chooseButton = new JButton("Elegir...")

  val okButton = new JButton("Añadir")
  val cancelButton = new JButton("Cancelar")

  // Paneles del diálogo
  val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(6, 10, 3, 10))
  panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS))
  panel1.add(filterLabel)
  panel1.add(filterTextField)
  panel1.add(chooseButton)

  var panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(6, 10, 3, 10))

  val panel3 = new JPanel()
  panel3.setBorder(new EmptyBorder(3, 10, 6, 10))
  panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS))
  panel3.add(cancelButton)
  panel3.add(okButton)

  // Añadimos los elementos a la ventana
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)
  add(panel3)

  chooseButton.addActionListener(new java.awt.event.ActionListener() {
    def actionPerformed(evt: java.awt.event.ActionEvent) {

      //TODO Revisar este null
      //TODO y ese if...
      //TODO (como en todas las comunicaciones entre paneles y dialogos)
      val chooseElementDialog = new ChooseElementDialog(null,
          "resources/availableFilters.xml")
      if (chooseElementDialog.chosenAlgorithm != "") {
        panel2.removeAll()
        panel2.revalidate()
        panel2.repaint()
        val algorithmName = chooseElementDialog.chosenAlgorithm
        filterTextField.setText(algorithmName)
        //Cargar la clase
        isAlgorithm =
          Class.forName(algorithmName).newInstance.asInstanceOf[AbstractIS]
        //Cargar el número de atributos con toda su información
        dinamicOptions = ArrayBuffer.empty[JComponent]
        algorithmOptions = isAlgorithm.listOptions

        panel2.setLayout(new GridLayout(algorithmOptions.size, 2) {

          algorithmOptions.foreach { option =>
            panel2.add(new JLabel(option.name))
            if (option.optionType == 0) {
              var tmp = new JCheckBox()
              tmp.setToolTipText(option.description)
              tmp.hasFocus == option.default
              panel2.add(tmp)
              dinamicOptions += tmp
            } else if (option.optionType == 1) {
              var tmp = new JTextField()
              tmp.setText(option.default.toString)
              tmp.setToolTipText(option.description)
              panel2.add(tmp)
              dinamicOptions += tmp
            } else {
              var tmp = new JComboBox(option.possibilities.toArray)
              tmp.setToolTipText(option.description)
              panel2.add(tmp)
              dinamicOptions += tmp
            }
          }
          //Dibujar
          //TODO REvisar por qué pack() no funciona correctamente
          setSize(new Dimension(400, 250)) 
          //  pack()
          revalidate()
          repaint()

        })
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

      if (!algorithmOptions.isEmpty) {
        var iter = algorithmOptions.iterator
        //Seleccionamos el dataset
        command += filterTextField.getText + " "

        for (i <- 0 until algorithmOptions.size) {
          var actualOption = iter.next()
          if (actualOption.optionType == 0) {
            if (dinamicOptions(i).asInstanceOf[JCheckBox].isSelected())
              command += actualOption.command + " "
          } else if (actualOption.optionType == 1) {
            command += 
              actualOption.command + " " + dinamicOptions(i).asInstanceOf[JTextField].getText + " "
          } else {
            command += 
              actualOption.command + " " + dinamicOptions(i).asInstanceOf[JComboBox[String]].getSelectedItem.toString + " " //TODO Comprobar si esta opción funciona bien
          }
        }
        this.dispose();
      }
    }
  private def cancelActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      this.dispose();
    }

}