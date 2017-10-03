package gui.panel

import java.awt.Color

import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.CheckBox
import scala.swing.ComboBox
import scala.swing.GridPanel
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.TextField
import scala.swing.event.ButtonClicked

import gui.UI
import gui.dialog.ChooseElementDialog
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder

/**
 * Panel for the classifier information and cross-validation options.
 *
 * About the classifiers, it shows which configuration have been defined so far
 * and it allows to create a new one. Information about cross-validation is
 * always present.

 * @constructor  Create and draw a new panel.
 * @param parent  Parent panel. 
 * @param orientation  Orientation of the panel elements.
 *
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ClassifierPanel(parent: UI,
                      orientation: Orientation.Value)
    extends BoxPanel(orientation) {

  /**
   * Path to the .xml file that contains all the existing classifiers and their
   * properties.
   */
  val listOfClassifiersPath = "/resources/availableClassifiers.xml"

  /**
   * Parameters for the selected algorithm.
   */
  var options = Iterable.empty[utils.Option]

  // PANEL COMPONENTS
  /**
   * Label with the text “Classifier”.
   *
   */
  private val classifierLabel = new Label(“Classifier”)
  /**
   * Text box that includes the path to the classifier.
   */
  private val classifierTextField = new TextField("None")
  /**
   * Button that allows the user to select a new classifier.
   */
  private val chooseButton = new Button(“Choose…”)

  /**
   * Label with the “Cross-validation” text.
   */
  private val crossValidationLabel = new Label("Cross-validation")
  /**
   * Checkbox that activates the cross-validation.
   */
  private val crossValidationCheckBox = new CheckBox
  /**
   * Text box for the number of folds we want to use.
   */
  private val crossValidationTextField = new TextField()
  crossValidationTextField.tooltip = “Cross-validation folds.”
  /**
   * Label for the seed of the cross-validation.
   */
  private val cvSeedLabel = new Label(“Seed”)
  /**
   * Text box for the seed of the cross-validation.
   */
  private val cvSeedTextField = new TextField()
  cvSeedTextField.tooltip = "Semilla para la validación cruzada"

  /**
   * Spacing between the panel elements.
   */
  private val hstrctSize = 6
  /**
   * Up and down margin for the children panels.
   */
  private val tdb = 3
  /**
   * Lateral margin for the children panels.
   */
  private val lb = 10
  // Children panels.
  /**
   * Panel with components about the classifier.
   */
  private val panel1 = new BoxPanel(Orientation.Horizontal) {
    contents += classifierLabel
    contents += Swing.HStrut(hstrctSize)
    contents += classifierTextField
    contents += Swing.HStrut(hstrctSize)
    contents += chooseButton
  }

  /**
   * Panel with components about the cross-validation.
   */
  private val panel3 = new BoxPanel(Orientation.Horizontal) {
    contents += crossValidationLabel
    contents += crossValidationCheckBox
    contents += Swing.HStrut(hstrctSize)
    contents += crossValidationTextField
    contents += Swing.HStrut(hstrctSize)
    contents += cvSeedLabel
    contents += Swing.HStrut(hstrctSize)
    contents += cvSeedTextField
  }

  /**
   * Panel that displays the options of the selected classifier.
   */
  private var panel2 = new GridPanel(1, 1)

  /**
   * Panel that wraps the classifier panel and the cross-validation panel.
   *
   * It does not include the buttons that allow us to configure the classifier
   * options.
   */
  private val biggestPanel = new BoxPanel(orientation) {
    border = new EmptyBorder(tdb, lb, tdb, lb)
    contents += panel1
    contents += Swing.VStrut(hstrctSize)
    contents += panel3
    contents += Swing.VStrut(hstrctSize)
  }

  // Add all the components.
  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true),
    “Classifier”)
  contents += biggestPanel

  // Add listeners and events.

  listenTo(classifierTextField)
  listenTo(chooseButton)
  reactions += {
    case ButtonClicked(`chooseButton`) => {
      chooseClassifier()
    }
  }

  /**
   * Allows the user to select a new classifier and adapts the panel content to\
   * display all the configurable options.
   * 
   */
  private def chooseClassifier(): Unit = {

    val chooseElementDialog = new ChooseElementDialog(this.peer,
      listOfClassifiersPath)
    if (chooseElementDialog.chosenAlgorithm != "") {
      panel2.contents.remove(0, panel2.contents.size)
      val algorithmName = chooseElementDialog.chosenAlgorithm
      classifierTextField.text = algorithmName

      // Load all the attributes and their descriptive texts.

      options = chooseElementDialog.algorithmOptions
      panel2 = new GridPanel(options.size, 2) {

        options.foreach { option =>
          contents += new Label(option.name)
          if (option.optionType == 0) {
            var tmp = new CheckBox()
            tmp.tooltip = option.description
            tmp.selected = option.default.toBoolean
            contents += tmp
          } else if (option.optionType == 1) {
            var tmp = new TextField()
            tmp.text = option.default.toString
            tmp.tooltip = option.description
            contents += tmp

          }
        }

      }
      // Draw everything.
      biggestPanel.contents += panel2
      biggestPanel.revalidate()
      biggestPanel.repaint()

    }
  }

  /**
   * Selects all the information about the new classifier configuration.
   *
   * @return String with all the classifier options.
   */
  def getClassifierOptions(): String = {

    var iter = options.iterator
    var command: String = classifierTextField.text + " "

    for { i <- 1 until panel2.contents.size by 2 } {
      var actualOption = iter.next()
      if (actualOption.optionType == 0) {
        if (panel2.contents(i).asInstanceOf[CheckBox].selected) {
          command += actualOption.command + " "
        }
      } else if (actualOption.optionType == 1) {
        command +=
          actualOption.command + " " +
          panel2.contents(i).asInstanceOf[TextField].text + " "
      } else {
        command +=
          actualOption.command + " " +
          panel2.contents(i).asInstanceOf[ComboBox[Seq[String]]].
          selection + " "
      }
    }
    command
  }

  /**
   * Collects all the information that refers to the cross-validation process.
   *
   * @return String with the cross-validation options. Empty string if there
   * is no cross-validation.
   */
  def getCrossValidationOptions(): String = {
    if (crossValidationCheckBox.selected) {
      crossValidationTextField.text + " " + cvSeedTextField.text + " "
    } else {
      ""
    }
  }

}
