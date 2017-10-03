package gui.dialog

import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import scala.collection.mutable.ArrayBuffer

import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.EmptyBorder
import utils.Option

/**
 * Dialog for selecting a filter and tune all its parameters.
 *
 * When it is first drawn, the dialog will only show a list of all the available
 * filters. Once an option has been chosen, the dialog will update and will allow
 * the user to change the by-default values of its parameters.
 *
 * @constructor Create and draw a new dialog.
 * 
 * @param  myParent  Parent panel.
 * @param  modal  Whether this dialog should bock the rest of the interface.
 *
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class FilterDialog(myParent: JPanel, modal: Boolean) extends JDialog {

  /**
   * Command that translates the selected configuration to a string for the
   * library.
   */
  var command = ""

  /**
   * Parametrised values of the selected algorithm.
   */
  var algorithmOptions: Iterable[Option] = Iterable.empty[utils.Option]

  /**
   * List with all the available filters.
   */
  var dinamicOptions: ArrayBuffer[JComponent] = ArrayBuffer.empty[JComponent]
  /**
   * Path to the .xml that contains all the information about the filters that can
   * be selected.
   */
  val listOfFiltersPath = "/resources/availableFilters.xml"

  // PANEL COMPONENTS
  /**
   * Up and down margin of the children panels.
   */
  private val tdb = 6
  /**
   * Lateral margin of the children panels.
   */
  private val lb = 10
  /**
   * Label with the name “Filter”
   */
  private val filterLabel = new JLabel("Filter”)
  /**
   * Path to the selected filter.
   */
  private val filterTextField = new JTextField("None")
  /**
   * Button for adding a new filter.
   */
  private val chooseButton = new JButton(“Choose…”)

  /**
   * Ok button.
   */
  private val okButton = new JButton("Añadir")
  /**
   * Cancel button.
   */
  private val cancelButton = new JButton("Cancelar")

  // PANELS OF THE DIALOG
  /**
   * Panel with all the components that allow us to select a new
   * filter.
   */
  private val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(tdb, lb, tdb / 2, lb))
  panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS))
  panel1.add(filterLabel)
  panel1.add(filterTextField)
  panel1.add(chooseButton)

  /**
   * Panel with all the configurable options of a chosen algorithm.
   *
   * The number of components can vary depending on the algorithm.
   */
  private var panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(tdb, lb, tdb / 2, lb))

  /**
   * Ok and Cancel buttons to add a new filter configuration.
   */
  private val panel3 = new JPanel()
  panel3.setBorder(new EmptyBorder(tdb / 2, lb, tdb, lb))
  panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS))
  panel3.add(cancelButton)
  panel3.add(okButton)

  // Add all the elements to the dialog.
  setTitle("Añadir nuevo filtro")
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)
  add(panel3)

  // Listeners

  chooseButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: ActionEvent): Unit = {
      chooseActionPerformed(evt)
    }
  })

  okButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: ActionEvent): Unit = {
      okActionPerformed(evt);
    }
  })

  cancelButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: ActionEvent): Unit = {
      cancelActionPerformed(evt);
    }
  })

  pack()
  setLocationRelativeTo(myParent)
  setModal(modal)
  setVisible(true);

  /**
   * Action for when the user presses the Select button.
   *
   * @param  evt  Event generated after clicking the Select button.
   */
  private def chooseActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {

      val chooseElementDialog = new ChooseElementDialog(this.getContentPane,
        listOfFiltersPath)
      if (chooseElementDialog.chosenAlgorithm != "") {
        panel2.removeAll()
        panel2.revalidate()
        panel2.repaint()
        val algorithmName = chooseElementDialog.chosenAlgorithm
        filterTextField.setText(algorithmName)

        // Load all the attributes of the chosen algorithm.
        dinamicOptions = ArrayBuffer.empty[JComponent]
        algorithmOptions = chooseElementDialog.algorithmOptions

        panel2.setLayout(new GridLayout(algorithmOptions.size, 2) {

          algorithmOptions.foreach { option =>
            panel2.add(new JLabel(option.name))
            if (option.optionType == 0) {
              var tmp = new JCheckBox()
              tmp.setToolTipText(option.description)
              tmp.setSelected(option.default.toBoolean)
              panel2.add(tmp)
              dinamicOptions += tmp
            } else if (option.optionType == 1) {
              var tmp = new JTextField()
              tmp.setText(option.default.toString)
              tmp.setToolTipText(option.description)
              panel2.add(tmp)
              dinamicOptions += tmp
            }
          }
          // Draw
          // pack()
          // scalastyle:off
          setSize(new Dimension(400, 250))
          // scalastyle:on
          // pack()
          revalidate()
          repaint()

        })
      }
    }

  /**
   * Action for when the user presses the Ok button.
   *
   * @param  evt  Event generated after clicking the Ok button.
   */
  private def okActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {

      if (!algorithmOptions.isEmpty) {
        var iter = algorithmOptions.iterator
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
          }
        }
        this.dispose();
      }
    }

  /**
   * Action for when the user presses the Cancel button.
   *
   * @param  evt  Event generated after clicking the Cancel button.
   */
  private def cancelActionPerformed(evt: java.awt.event.ActionEvent): Unit =
    {
      this.dispose();
    }

}
