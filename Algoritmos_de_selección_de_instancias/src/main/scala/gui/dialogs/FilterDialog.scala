package gui.dialogs

import java.awt.Dimension
import java.awt.GridLayout

import scala.collection.mutable.ArrayBuffer

import instanceSelection.abstracts.TraitIS
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
import utils.Option

/**
 * Ventana que permite la selección de un un filtro para el conjunto de datos
 * así como ajustar los parámetros del mismo.
 *
 * En el momento de la invocación, este diálogo únicamente contará con una
 * serie de componentes para seleccionar un filtro. Una vez seleccionado el
 * filtro, este diálogo se actualizará permitiendo configurar todas las
 * opciones que posibilite el algoritmo seleccionado.
 *
 * @constructor Genera un nuevo panel con componentes que permiten elegir un
 * filtro.
 * @param  myParent  Panel que ha creado este diálogo.
 * @param  modal  Si el diálogo debe bloquear o no la interacción con el resto
 *   de la interfaz mientras esté abierto.
 *
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class FilterDialog(myParent: JPanel, modal: Boolean) extends JDialog {

  /**
   * Comando generado al traducir toda la información del diálogo a una cadena
   * de texto que la clase de ejecución pueda entender.
   */
  var command = ""

  /**
   * Opciones configurables del algoritmo seleccionado.
   */
  var algorithmOptions: Iterable[Option] = Iterable.empty[utils.Option]
  /**
   * Algoritmo de filtrado de instancias seleccionado.
   */
  var isAlgorithm: TraitIS = null
  /**
   * Listado con todos los componentes que permiten la selección y
   * configuración de las opciones del filtro.
   */
  var dinamicOptions: ArrayBuffer[JComponent] = ArrayBuffer.empty[JComponent]
  /**
   * Ruta donde se encuentra el fichero .xml con todas los posibles
   * filtros seleccionables.
   */
  val listOfFiltersPath = "/resources/availableFilters.xml"

  // Componentes de la ventana
  /**
   * Texto indicativo para indicar que estamos hablando sobre la selección
   * de un filtro.
   */
  private val filterLabel = new JLabel("Filtro")
  /**
   * Campo con la ruta del filtro seleccionado.
   */
  private val filterTextField = new JTextField("None")
  /**
   * Botón para permitir la selección de un filtro.
   */
  private val chooseButton = new JButton("Elegir...")

  /**
   * Botón de aceptar.
   */
  private val okButton = new JButton("Añadir")
  /**
   * Botón de cancelar.
   */
  private val cancelButton = new JButton("Cancelar")

  // Paneles del diálogo
  /**
   * Panel con todos los componentes para seleccionar un filtro
   */
  private val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(6, 10, 3, 10))
  panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS))
  panel1.add(filterLabel)
  panel1.add(filterTextField)
  panel1.add(chooseButton)

  /**
   * Panel para configurar todas las opciones del filtro seleccionado.
   *
   * Sus componentes pueden variar dependiendo del filtro seleccionado.
   */
  private var panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(6, 10, 3, 10))

  /**
   * Panel con los botones para aceptar/cancelar una determinada selección del
   * fitro.
   */
  private val panel3 = new JPanel()
  panel3.setBorder(new EmptyBorder(3, 10, 6, 10))
  panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS))
  panel3.add(cancelButton)
  panel3.add(okButton)

  // Añadimos los elementos a la ventana
  setTitle("Añadir nuevo filtro")
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)
  add(panel3)

  // Damos la capacidad a los botones de escuchar eventos cuando se hace
  // click sobre ellos.

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
  setLocationRelativeTo(myParent)
  setModal(modal)
  setVisible(true);

  /**
   * Acción realizada cuando presionamos el botón de selección.
   *
   * @param  evt  Evento lanzado al presionar sobre el botón.
   */
  private def chooseActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {

      // TODO Revisar este null
      // TODO y ese if...
      // TODO (como en todas las comunicaciones entre paneles y dialogos)
      val chooseElementDialog = new ChooseElementDialog(null,
        listOfFiltersPath)
      if (chooseElementDialog.chosenAlgorithm != "") {
        panel2.removeAll()
        panel2.revalidate()
        panel2.repaint()
        val algorithmName = chooseElementDialog.chosenAlgorithm
        filterTextField.setText(algorithmName)
        // Cargar la clase
        isAlgorithm =
          Class.forName(algorithmName).newInstance.asInstanceOf[TraitIS]
        // Cargar el número de atributos con toda su información
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
          // Dibujar
          // TODO Revisar por qué pack() no funciona correctamente
          setSize(new Dimension(400, 250))
          // pack()
          revalidate()
          repaint()

        })
      }
    }

  /**
   * Acción realizada cuando presionamos el botón de aceptar.
   *
   * @param  evt  Evento lanzado al presionar sobre el botón.
   */
  private def okActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {

      if (!algorithmOptions.isEmpty) {
        var iter = algorithmOptions.iterator
        // Seleccionamos el dataset
        command += filterTextField.getText + " "

        for { i <- 0 until algorithmOptions.size } {
          var actualOption = iter.next()
          if (actualOption.optionType == 0) {
            if (dinamicOptions(i).asInstanceOf[JCheckBox].isSelected()) {
              command += actualOption.command + " "
            }
          } else if (actualOption.optionType == 1) {
            command +=
              actualOption.command + " " +
              dinamicOptions(i).asInstanceOf[JTextField].getText + " "
          } else {
            // TODO Comprobar si esta opción funciona bien
            command +=
              actualOption.command + " " +
              dinamicOptions(i).asInstanceOf[JComboBox[String]].
              getSelectedItem.toString + " "
          }
        }
        this.dispose();
      }
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
