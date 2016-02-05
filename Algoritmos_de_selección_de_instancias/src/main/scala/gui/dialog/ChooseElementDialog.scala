package gui.dialog

import java.awt.Color
import java.awt.Container
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.MutableList
import scala.xml.XML

import javax.swing.BoxLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import utils.Option

/**
 * Diálogo que permite la selección de diferentes opciones mostradas en una
 * lista.
 *
 * @constructor Genera una lista con diferentes opciones leidas de un archivo
 *   .xml y permite la selección de una de ellas.
 * @param  myParent Panel desde donde se ha invocado este diálogo.
 * @param  xmlPath  Ruta al fichero .xml de donde leeremos los datos para la lista.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ChooseElementDialog(myParent: Container, xmlPath: String) extends JDialog {

  /**
   * Texto que contiene la ruta al algoritmo seleccionado.
   */
  var chosenAlgorithm = ""

  /**
   * Listado de opciones del algoritmo seleccionado.
   */
  var algorithmOptions: MutableList[Option] = MutableList.empty[Option]
  // Elementos de la ventana
  /**
   * Botón de aceptar.
   */
  private val okButton = new JButton("Elegir")
  /**
   * Botón de cancelar.
   */
  private val cancelButton = new JButton("Cancelar")

  /**
   * Componente lista
   */
  private val elementsList = new JList[String]
  /**
   * Array con todos los elementos a aparecer en el listado.
   */
  private var elements: ArrayBuffer[String] = findAvailableElements()
  /**
   * Modelo del componente lista.
   */
  private val listModel = new DefaultListModel[String]
  /**
   * Tamaño del magen superior e inferior de los subpaneles.
   */
  private val tdb = 6
  /**
   * Tamaño de los márgenes laterales de los subpaneles.
   */
  private val lb = 10
  // Paneles de la ventana
  /**
   * Panel con una lista que muestra las diferentes opciones.
   */
  private val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(tdb, lb, tdb / 2, lb))
  panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS))
  panel1.add(elementsList)

  /**
   * Panel con los botones para aceptar o cancelar.
   */
  private val panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(tdb / 2, lb, tdb, lb))
  panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS))
  panel2.add(cancelButton)
  panel2.add(okButton)

  // Añadimos alguna configuración a la lista
  elementsList.setBorder(new LineBorder(Color.GRAY, 1, true))
  elementsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)

  // Añadimos componentes a la lista
  for { i <- 0 until elements.size } {
    listModel.addElement(elements(i))
  }
  elementsList.setModel(listModel)

  // Añadimos los paneles a nuestro dialogo
  setTitle("Elegir...")
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)

  okButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: java.awt.event.ActionEvent): Unit = {
      okActionPerformed(evt);
    }
  })

  cancelButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: java.awt.event.ActionEvent): Unit = {
      cancelActionPerformed(evt);
    }
  })

  elementsList.addMouseListener(new MouseAdapter() {
    override def mouseClicked(evt: MouseEvent): Unit = {
      clickPerformed(evt)
    }
  })

  pack()
  setLocationRelativeTo(myParent)
  setModal(true)
  setVisible(true);

  /**
   * Acción realizada cuando presionamos el botón de aceptar.
   *
   * @param  evt  Evento lanzado al presionar sobre el botón.
   */
  private def okActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      if (elementsList.getSelectedIndices().length != 0) {
        var indexChosen = elementsList.getSelectedIndices()(0)
        chosenAlgorithm = listModel.getElementAt(indexChosen)
        algorithmOptions = getSelectedAlgorithmOptions
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

  /**
   * Busca en un fichero .xml aquellos elementos contenidos dentro de la etiqueta
   * "name".
   *
   * @return Todos los elementos contenidos dentro de la etiqueta "name".
   */
  private def findAvailableElements(): ArrayBuffer[String] = {

    val xmlFile = getClass.getResourceAsStream(xmlPath)
    val elements = ArrayBuffer.empty[String]

    val xmlElem = XML.load(xmlFile)

    (xmlElem \ "algorithm").foreach { algorithm =>
      (algorithm \ "name").foreach { name =>
        elements += name.text
      }
    }
    elements
  }

  /**
   * Selecciona una opción de la lista si se ha hecho clic sobre ella dos
   * veces seguidas.
   *
   * @param evt Evento de clic
   */
  private def clickPerformed(evt: MouseEvent): Unit = {
    if (evt.getClickCount() == 2) {
      var indexChosen = elementsList.getSelectedIndices()(0)
      chosenAlgorithm = listModel.getElementAt(indexChosen)
      algorithmOptions = getSelectedAlgorithmOptions
      this.dispose();
    }
  }

  /**
   * Busca todas sus opciones del algoritmo seleccionado.
   *
   * @return Opciones del algoritmo.
   */
  private def getSelectedAlgorithmOptions: MutableList[Option] = {
    val optionsList: MutableList[Option] = MutableList.empty[Option]
    val xmlFile = getClass.getResourceAsStream(xmlPath)
    val elements = ArrayBuffer.empty[String]
    val xmlElem = XML.load(xmlFile)

    val index =
      findAvailableElements.indexOf(chosenAlgorithm)

    ((xmlElem \ "algorithm")(index) \ "options").foreach { options =>
      (options \ "option").foreach { option =>
        val name = (option \ "name").text
        val description = (option \ "description").text
        val command = (option \ "command").text
        val default = (option \ "default").text
        val optionType = (option \ "optionType").text.toInt
        optionsList +=
          new Option(name, description, command, default, optionType)
      }

    }

    optionsList
  }

}

