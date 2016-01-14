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
 * @constructor  Genera un diálogo con todos los campos posibles para seleccionar
 *   opciones de lanzamiento de Spark.
 * @param  myParent  Panel que ha creado este diálogo.
 * @param  modal  Si el diálogo debe bloquear o no la interacción con el resto
 *   de la interfaz mientras esté abierto.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class SparkDialog(myParent: JPanel, modal: Boolean) extends JDialog {

  /**
   * Comando generado al traducir toda la información del diálogo a una cadena
   * de texto que la clase de ejecución pueda entender.
   */
  var command = ""

  // Componentes del diálogo
  /**
   * Texto indicando el número de nucleos por ejecutor.
   */
  private val coresExecutorLabel = new JLabel("Número de núcleos por ejecutor")
  /**
   * Campo para seleccionar el número de nucleos por ejecutor.
   */
  private val coresExecutorTextField = new JTextField(2)
  /**
   * Texto indicando el número de nucleos totales.
   */
  private val totalCoresLabel = new JLabel("Número de núcleos totales")
  /**
   * Campo para seleccionar el número de nucleos totales.
   */
  private val totalCoresTextField = new JTextField(2)
  /**
   * Texto indicando la memoria asignada a cada ejecutor.
   */
  private val memExecutorLabel = new JLabel("Memoria por ejecutor")
  /**
   * Campo para seleccionar la cantidad de memoria por ejecutor.
   */
  private val memExecutorTextField = new JTextField(2)

  /**
   * Botón para aceptar una nueva configuración.
   */
  private val okButton = new javax.swing.JButton("Añadir")
  /**
   * Botón para cancelar y cerrar el diálogo.
   */
  private val cancelButton = new javax.swing.JButton("Cancelar")

  // Paneles

  /**
   * Panel con los diferentes campos a rellenar.
   */
  private val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(6, 10, 3, 10))
  panel1.setLayout(new GridLayout(3, 2, 5, 5))
  panel1.add(coresExecutorLabel)
  panel1.add(coresExecutorTextField)
  panel1.add(totalCoresLabel)
  panel1.add(totalCoresTextField)
  panel1.add(memExecutorLabel)
  panel1.add(memExecutorTextField)

  /**
   * Panel con los botones para aceptar/cancelar una determinada configuración.
   */
  private val panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(3, 10, 6, 10))
  panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS))
  panel2.add(cancelButton)
  panel2.add(okButton)

  // Añadimos todos los elementos a la ventana
  setTitle("Añadir nueva configuración de Spark")
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)

  // Añadimos a los botones la capacidad de escuchar eventos lanzados
  // al seleccionarlos.
  okButton.addActionListener(new java.awt.event.ActionListener() {
    def actionPerformed(evt: java.awt.event.ActionEvent): Unit = {
      okActionPerformed(evt);
    }
  })

  cancelButton.addActionListener(new java.awt.event.ActionListener() {
    def actionPerformed(evt: java.awt.event.ActionEvent): Unit = {
      cancelActionPerformed(evt);
    }
  })

  pack()
  setLocationRelativeTo(myParent)
  setModal(modal)
  setVisible(true);

  /**
   * Acción realizada cuando presionamos el botón de aceptar.
   *
   * @param  evt  Evento lanzado al presionar sobre el botón.
   */
  private def okActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      command += "--executor-cores " + coresExecutorTextField.getText + " "
      command += "--total-executor-cores " + totalCoresTextField.getText + " "
      command += "--executor-memory " + memExecutorTextField.getText + " "
      this.dispose();
    }

  /**
   * Acción realizada cuando presionamos el botón de cancelar.
   *
   * @param  evt  Evento lanzado al presionar sobre el botón.
   */
  private def cancelActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      this.dispose();
    }

}
