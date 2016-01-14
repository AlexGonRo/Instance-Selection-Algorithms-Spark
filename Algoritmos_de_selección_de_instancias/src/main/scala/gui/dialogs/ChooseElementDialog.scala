package gui.dialogs

import java.awt.Color

import scala.collection.mutable.ArrayBuffer

import org.w3c.dom.Element
import org.w3c.dom.Node

import javax.swing.BoxLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Ventana que permite la selección de diferentes opciones mostradas en una
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
class ChooseElementDialog(myParent: JPanel, xmlPath: String) extends JDialog {

  /**
   * Texto que contiene la ruta al algoritmo seleccionado.
   */
  var chosenAlgorithm = ""

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

  for { i <- 0 until elements.size } {
    listModel.addElement(elements(i))
  }
  elementsList.setModel(listModel)
  elementsList.setBorder(new LineBorder(Color.GRAY, 1, true))
  elementsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)

  // Paneles de la ventana
  /**
   * Panel con una lista que muestra las diferentes opciones.
   */
  private val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(6, 10, 3, 10))
  panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS))
  panel1.add(elementsList)

  /**
   * Panel con los botones para aceptar o cancelar.
   */
  private val panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(3, 10, 6, 10))
  panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS))
  panel2.add(cancelButton)
  panel2.add(okButton)

  // Añadimos los paneles a nuestro dialogo
  setTitle("Elegir...")
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)

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
    val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val parsedDoc = db.parse(xmlFile)

    parsedDoc.getDocumentElement().normalize()

    // TODO Revisar el nivel de las etiquetas del XML
    val nList = parsedDoc.getElementsByTagName("name");

    for { i <- 0 until nList.getLength } {
      val node = nList.item(i)
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        elements += node.asInstanceOf[Element].getTextContent
      }
    }

    elements

  }

}

