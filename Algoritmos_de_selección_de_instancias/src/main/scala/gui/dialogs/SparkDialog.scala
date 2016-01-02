package gui.dialogs

import java.awt.GridLayout

import gui.panel.SparkPanel
import javax.swing.BoxLayout
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.EmptyBorder

/**
 * Diálogo que permite configurar algunas de las opciones de ejecución de Spark.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class SparkDialog(myParent: SparkPanel, modal: Boolean) extends JDialog {

  setTitle("Añadir nueva configuración de Spark")
  var command = ""

  // Componentes del diálogo
  val coresExecutorLabel = new JLabel("Número de núcleos por ejecutor")
  val coresExecutorTextField = new JTextField(2)
  val totalCoresLabel = new JLabel("Número de núcleos totales")
  val totalCoresTextField = new JTextField(2)
  val memExecutorLabel = new JLabel("Memoria por ejecutor")
  val memExecutorTextField = new JTextField(2)

  val okButton = new javax.swing.JButton("Añadir")
  val cancelButton = new javax.swing.JButton("Cancelar")

  // Paneles
  
  val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(6, 10, 3, 10))
  panel1.setLayout(new GridLayout(3, 2, 5, 5))
  panel1.add(coresExecutorLabel)
  panel1.add(coresExecutorTextField)
  panel1.add(totalCoresLabel)
  panel1.add(totalCoresTextField)
  panel1.add(memExecutorLabel)
  panel1.add(memExecutorTextField)

  val panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(3, 10, 6, 10))
  panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS))
  panel2.add(cancelButton)
  panel2.add(okButton)


  // Añadimos todos los elementos a la ventana
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)

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
      command += "--executor-cores " + coresExecutorTextField.getText + " "
      command += "--total-executor-cores " + totalCoresTextField.getText + " "
      command += "--executor-memory " + memExecutorTextField.getText + " "
      this.dispose();
    }
  private def cancelActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      this.dispose();
    }

}