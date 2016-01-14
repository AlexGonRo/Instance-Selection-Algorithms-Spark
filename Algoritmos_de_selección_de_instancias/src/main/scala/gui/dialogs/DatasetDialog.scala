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
 * @constructor Genera un nuevo panel con todos los componentes para poder indicar
 *   la selección de un nuevo conjunto de datos.
 * @param  myParent  Panel que ha creado este diálogo.
 * @param  modal  Si el diálogo debe bloquear o no la interacción con el resto
 *   de la interfaz mientras esté abierto.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DatasetDialog(myParent: DatasetPanel, modal: Boolean) extends JDialog {

  /**
   * Comando generado al traducir toda la información del diálogo a una cadena
   * de texto que la clase de ejecución pueda entender.
   */
  var command = ""

  // Elementos de la ventana
  /**
   * Texto para el conjunto de datos.
   */
  private val cdLabel = new JLabel("Conjunto de datos")
  /**
   * Campo de texto donde incluir la ruta del fichero.
   */
  private val cdTextField = new JTextField
  /**
   * Botón para poder seleccionar un fichero que contenga el conjunto
   * de datos.
   */
  private val chooseButton = new JButton("Elegir...")
  /**
   * Texto para indicar que estamos tratando opciones referentes al
   * atributo de clase.
   */
  private val classAttPosLabel = new JLabel("Atributo de clase")
  /**
   * Conjunto de opciones para seleccionar la posición del atributo de clase.
   */
  private val classAttGroup = new ButtonGroup()
  /**
   * Opción para seleccionar el atributo de clase como el primero
   */
  private val firstRadioButton = new JRadioButton("Primero")
  classAttGroup.add(firstRadioButton)
  /**
   * Opción para seleccionar el atributo de clase como el último.
   */
  private val lastRadioButton = new JRadioButton("Último", true)
  classAttGroup.add(lastRadioButton)

  /**
   * Texto para indicar que estamos hablando sobre la cabecera.
   */
  private val headCheckBox = new JCheckBox("Cabecera")
  /**
   * Campo para indicar el número de lineas de cabecera.
   */
  private val numHeaderLinesTextField = new JTextField("0")

  /**
   * Selector de ficheros para buscar un conjunto de datos.
   */
  private var fileChooser = new JFileChooser()
  fileChooser.setCurrentDirectory(new java.io.File("."))
  fileChooser.setDialogTitle("Seleccionar conjunto de datos")
  fileChooser.addChoosableFileFilter(new CSVFilter())
  fileChooser.setAcceptAllFileFilterUsed(true)

  /**
   * Botón para aceptar.
   */
  private val okButton = new JButton("Añadir")
  /**
   * Botón para cancelar.
   */
  private val cancelButton = new JButton("Cancelar")

  // Paneles del diálogo
  /**
   * Panel que permite seleccionar un conjuto de datos.
   */
  private val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(6, 10, 3, 10))
  panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS))
  panel1.add(cdLabel)
  panel1.add(cdTextField)
  panel1.add(chooseButton)

  /**
   * Panel para cofigurar las diferentes opciones de lectura del
   * conjunto de datos
   */
  private val panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(3, 10, 6, 10))
  panel2.setLayout(new GridLayout(1, 2, 0, 20))
  /**
   * Muestra opciones para indicar donde estará el atributo de clase de las
   * instancias.
   */
  private val panel21 = new JPanel
  panel21.setLayout(new BoxLayout(panel21, BoxLayout.X_AXIS))
  panel21.add(classAttPosLabel)
  panel21.add(firstRadioButton)
  panel21.add(lastRadioButton)
  /**
   * Muestra opciones para configurar la lectura de la cabecera del fichero,
   * si es que la tiene.
   */
  private val panel22 = new JPanel
  panel22.setLayout(new BoxLayout(panel22, BoxLayout.X_AXIS))
  panel22.add(headCheckBox)
  panel22.add(numHeaderLinesTextField)

  panel2.add(panel21)
  panel2.add(panel22)

  /**
   * Contiene los botones para aceptar/cancelar el contenido del diálogo.
   */
  private val panel3 = new JPanel()
  panel3.setBorder(new EmptyBorder(3, 10, 6, 10))
  panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS))
  panel3.add(cancelButton)
  panel3.add(okButton)

  // Añadimos los paneles a nuestro dialogo
  setTitle("Añadir nuevo conjunto de datos")
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)
  add(panel3)

  // Añadimos diferentes "listener" a los componentes
  chooseButton.addActionListener(new java.awt.event.ActionListener() {
    def actionPerformed(evt: java.awt.event.ActionEvent): Unit = {
      chooseActionPerformed(evt)
    }
  })

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
  setLocationRelativeTo(myParent.peer)
  setModal(modal)
  setVisible(true);

  /**
   * Acción realizada cuando presionamos el botón de selección.
   *
   * @param  evt  Evento lanzado al presionar sobre el botón.
   */
  private def chooseActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      val returnVal = fileChooser.showOpenDialog(chooseButton)

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        var path = fileChooser.getSelectedFile.getAbsolutePath
        cdTextField.setText(path)
      }
    }

  /**
   * Acción realizada cuando presionamos el botón de aceptar.
   *
   * @param  evt  Evento lanzado al presionar sobre el botón.
   */
  private def okActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      // TODO PROBLEMA SI LA RUTA TIENE ESPACIOS
      command += cdTextField.getText + " "
      if (firstRadioButton.isSelected()) {
        command += "-f "
      }
      if (headCheckBox.isSelected()) {
        command += "-hl " + numHeaderLinesTextField.getText + " "
      }
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
