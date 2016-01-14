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

import classification.seq.abstracts.TraitClassifier
import gui.UI
import gui.dialogs.ChooseElementDialog
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder

/**
 * Panel que contiene todo lo referente a la configuración del clasificador y de
 * la validación cruzada.
 *
 * En un primer momento, ningún clasificador aparece seleccionado, pero es
 * posible que la elección de uno afecte al contenido del panel, que incluirá
 * nuevos componentes para permitir configurar el clasificar indicado.
 *
 * @constructor  Genera un nuevo espacio donde poder seleccionar un nuevo
 * clasificador.
 * @param parent  Ventana desde donde se ha invocado este panel
 * @param orientation  Orientación del contenido mostrado en el panel.
 *
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ClassifierPanel(parent: UI,
                      orientation: Orientation.Value)
    extends BoxPanel(orientation) {

  /**
   * Ruta donde encontrar un archivo .xml que permita indicar que
   * classificadores son seleccionables.
   */
  val listOfClassifiersPath = "/resources/availableClassifiers.xml"

  /**
   * Opciones de configuración del algoritmo seleccionado.
   */
  var options = Iterable.empty[utils.Option]
  /**
   * Algoritmo de classificación elegido.
   */
  var classificationAlg: TraitClassifier = null

  // Elementos del panel
  /**
   * Texto para indicar que estamos refiriendonos a la selección del
   * classifiador.
   *
   */
  private val classifierLabel = new Label("Clasificador")
  /**
   * Campo de texto donde se incluirá la ruta al clasificador.
   */
  private val classifierTextField = new TextField("None")
  /**
   * Botón para permitir la selección de un clasificador.
   */
  private val chooseButton = new Button("Elegir...")

  /**
   * Texto para indicar que estamos refiriendonos a las opciones de la
   * validación cruzada.
   */
  private val crossValidationLabel = new Label("Cross-validation")
  /**
   * Campo para habilitar la validación cruzada.
   */
  private val crossValidationCheckBox = new CheckBox
  /**
   * Campo para indicar el número de folds que crearemos durante la validación
   * cruzada
   */
  private val crossValidationTextField = new TextField()
  crossValidationTextField.tooltip = "Número de iteraciones en la validación cruzada"
  /**
   * Texto para indicar que estamos hablando de la semilla.
   */
  private val cvSeedLabel = new Label("Semilla")
  /**
   * Campo para indicar la semilla con la que queremos que se generen los
   * k-folds.
   */
  private val cvSeedTextField = new TextField()
  cvSeedTextField.tooltip = "Semilla para la validación cruzada"

  // Subpaneles dentro del panel
  /**
   * Panel que contiene los elementos para la selección de un classificador.
   */
  private val panel1 = new BoxPanel(Orientation.Horizontal) {
    contents += classifierLabel
    contents += Swing.HStrut(6)
    contents += classifierTextField
    contents += Swing.HStrut(6)
    contents += chooseButton
  }

  /**
   * Panel que contiene los componentes que permitan configurar la validación
   * cruzada.
   */
  private val panel3 = new BoxPanel(Orientation.Horizontal) {
    contents += crossValidationLabel
    contents += crossValidationCheckBox
    contents += Swing.HStrut(6)
    contents += crossValidationTextField
    contents += Swing.HStrut(6)
    contents += cvSeedLabel
    contents += Swing.HStrut(6)
    contents += cvSeedTextField
  }

  /**
   * Panel que contendrá las opciones de configuración del del classificador
   * elegido.
   */
  private var panel2 = new GridPanel(1, 1)

  /**
   * Panel que incluye tanto las opciones de selección de classificador como
   * las de validación cruzada.
   *
   * No incluye los componentes que permiten la configuración de las opciones
   * del clasificador.
   */
  private val biggestPanel = new BoxPanel(orientation) {
    border = new EmptyBorder(3, 10, 3, 10)
    contents += panel1
    contents += Swing.VStrut(6)
    contents += panel3
    contents += Swing.VStrut(6)
  }

  // Añadimos los contenidos.
  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true),
    "Clasificador")
  contents += biggestPanel

  // Añadimos listeners y eventos a los botones.

  listenTo(classifierTextField)
  listenTo(chooseButton)
  reactions += {
    case ButtonClicked(`chooseButton`) => {
      chooseClassifier()
    }
  }

  /**
   * Permite la selección de un clasificador y adaptar el contenido del panel
   * de acuerdo a las opciones de configuración que contiene dicho classificador.
   *
   */
  private def chooseClassifier(): Unit = {

    // TODO Ese if...(como en todas las comunicaciones entre paneles y dialogos)
    val chooseElementDialog = new ChooseElementDialog(this.peer,
      listOfClassifiersPath)
    if (chooseElementDialog.chosenAlgorithm != "") {
      panel2.contents.remove(0, panel2.contents.size)
      val algorithmName = chooseElementDialog.chosenAlgorithm
      classifierTextField.text = algorithmName

      // Cargar la clase

      classificationAlg =
        Class.forName(algorithmName).newInstance.asInstanceOf[TraitClassifier]

      // Cargar el número de atributos con tips y demás

      options = classificationAlg.listOptions
      panel2 = new GridPanel(options.size, 2) {

        options.foreach { option =>
          contents += new Label(option.name)
          if (option.optionType == 0) {
            var tmp = new CheckBox()
            tmp.tooltip = option.description
            tmp.hasFocus == option.default
            contents += tmp
          } else if (option.optionType == 1) {
            var tmp = new TextField()
            tmp.text = option.default.toString
            tmp.tooltip = option.description
            contents += tmp

          } else {
            var tmp = new ComboBox(option.possibilities)
            tmp.tooltip = option.description
            contents += tmp
          }
        }

      }
      // Dibujar
      biggestPanel.contents += panel2
      biggestPanel.revalidate()
      biggestPanel.repaint()

    }
  }

  /**
   * Selecciona toda la información de los componentes relacionados con el
   * classificador.
   *
   * @return Cadena de texto con los elementos relacionados con el clasificador.
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
   * Selecciona toda la información de los componentes del panel referida a la
   * validación cruzada.
   *
   * @return Cadena de texto con los elementos de la validación cruzada
   */
  def getCrossValidationOptions(): String = {
    if (crossValidationCheckBox.selected) {
      crossValidationTextField.text + " " + cvSeedTextField.text + " "
    } else {
      ""
    }
  }

}
