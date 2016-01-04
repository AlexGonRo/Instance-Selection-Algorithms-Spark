package gui.dialogs

import javax.swing.JDialog
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import scala.collection.mutable.ArrayBuffer
import java.io.File
import javax.swing.border.LineBorder
import java.awt.Color
import javax.swing.JList
import javax.swing.DefaultListModel
import javax.swing.ListSelectionModel
import org.w3c.dom._
import javax.xml.parsers._
import java.io._

/**
 * Ventana que permite la selección de diferentes opciones mostradas en una 
 * lista.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ChooseElementDialog(myParent: JPanel, xmlPath: String) extends JDialog {

  setTitle("Elegir...")
  var chosenAlgorithm = ""
  
  // Elementos de la ventana
  val okButton = new JButton("Elegir")
  val cancelButton = new JButton("Cancelar")

  var elements: ArrayBuffer[String] = findAvailableElements()
  val elementsList = new JList[String]
  val listModel = new DefaultListModel[String]
  for (i <- 0 until elements.size)
    listModel.addElement(elements(i))
  elementsList.setModel(listModel)
  elementsList.setBorder(new LineBorder(Color.GRAY, 1, true))
  elementsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)

  
  // Paneles de la ventana
  val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(6, 10, 3, 10))
  panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS))
  panel1.add(elementsList)

  val panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(3, 10, 6, 10))
  panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS))
  panel2.add(cancelButton)
  panel2.add(okButton)

  // Añadimos los paneles a nuestro dialogo
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
  setLocationRelativeTo(myParent)
  setModal(true)
  setVisible(true);

  private def okActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      if (elementsList.getSelectedIndices().length != 0) {
        var indexChosen = elementsList.getSelectedIndices()(0)
        chosenAlgorithm = listModel.getElementAt(indexChosen)
        this.dispose();
      }
    }
  private def cancelActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      this.dispose();
    }

  private def findAvailableElements(): ArrayBuffer[String] = {
 
    val xmlFile = getClass.getResourceAsStream(xmlPath)
    /*   val xmlFile = new File(xmlPath)
*/   
    val elements = ArrayBuffer.empty[String]
    val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val parsedDoc = db.parse(xmlFile)

    parsedDoc.getDocumentElement().normalize()

    val nList = parsedDoc.getElementsByTagName("name"); //TODO Revisar el nivel de las etiquetas del XML

    for (i <- 0 until nList.getLength) {
      val node = nList.item(i)
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        elements += node.asInstanceOf[Element].getTextContent
      }
    }

    return elements

  }

}