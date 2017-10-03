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
 * Dialog that presents the user a list of options from which the user can choose
 * one.
 *
 * @constructor Generates a list from an .xml document and allows the user to select
 *    one of them.
 * @param  myParent Parent panel
 * @param  xmlPath  Path to the .xml that contain the options.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ChooseElementDialog(myParent: Container, xmlPath: String) extends JDialog {

  /**
   * Path to the selected option.
   */
  var chosenAlgorithm = ""

  /**
   * Options for the selected algorithm.
   */
  var algorithmOptions: MutableList[Option] = MutableList.empty[Option]
  // PANEL ELEMENTS
  /**
   * Ok button.
   */
  private val okButton = new JButton("Elegir")
  /**
   * Cancel button.
   */
  private val cancelButton = new JButton("Cancelar")

  /**
   * Element of the list.
   */
  private val elementsList = new JList[String]
  /**
   * Array with all the elements of a list.
   */
  private var elements: ArrayBuffer[String] = findAvailableElements()
  /**
   * List component model.
   */
  private val listModel = new DefaultListModel[String]
  /**
   * Up and down margin of the children panels.
   */
  private val tdb = 6
  /**
   * Lateral margin of the children panels.
   */
  private val lb = 10
  // Dialog panels
  /**
   * Panel that shows all the options
   */
  private val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(tdb, lb, tdb / 2, lb))
  panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS))
  panel1.add(elementsList)

  /**
   * Panel with “Ok” and “Cancel” buttons.
   */
  private val panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(tdb / 2, lb, tdb, lb))
  panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS))
  panel2.add(cancelButton)
  panel2.add(okButton)

  // Add configurations to the list.
  elementsList.setBorder(new LineBorder(Color.GRAY, 1, true))
  elementsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)

  // Add elements to the list.
  for { i <- 0 until elements.size } {
    listModel.addElement(elements(i))
  }
  elementsList.setModel(listModel)

  // Add the panel to our dialog box.
  // HARDCODED TEXT
  setTitle(“Choose…”)
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
   * Action for when the “OK” button is pressed.
   * @param  evt  Event that fires when we press the “Ok” button.
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
   * Action that takes place when the user clicks the “Cancel” button.
   *
   * @param  evt  Event that fires when we press the “Ok” button.
   */
  private def cancelActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      this.dispose();
    }

  /**
   * Looks in the .xml file all those elements that are inside the “name” tag.
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
   * Selects one option if the uses has double clicked on it.
   *
   * @param evt Click event.
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
   * Looks for all the parametrised parameters of an algorithm.
   *
   * @return Parametrised parameters of the algorithm.
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

