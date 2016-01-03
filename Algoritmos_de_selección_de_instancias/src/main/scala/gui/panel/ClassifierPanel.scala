package gui.panel

import scala.swing._
import instanceSelection.abstracts.AbstractIS
import javax.swing.JFileChooser
import scala.swing.event.ButtonClicked
import gui.UI
import BorderPanel.Position._
import javax.swing.border.LineBorder
import java.awt.Color
import javax.swing.border.TitledBorder
import classification.seq.abstracts.TraitClassifier
import javax.swing.border.EmptyBorder
import gui.dialogs.ChooseElementDialog

/**
 * Panel que contiene todo lo referente a la configuración del clasificador.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class ClassifierPanel(parentPanel: UI, 
    orientation: Orientation.Value) extends BoxPanel(orientation) {

  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true),
    "Clasificador")

  // Elementos del panel
  val classifierLabel = new Label("Clasificador")
  val classifierTextField = new TextField("None")
  val chooseButton = new Button("Elegir...")

  val crossValidationLabel = new Label("Cross-validation")
  val crossValidationCheckBox = new CheckBox
  val crossValidationTextField = new TextField()
  val cvSeedLabel = new Label("Semilla")
  val cvSeedTextField = new TextField()

  var options = Iterable.empty[utils.Option]
  var classificationAlg: TraitClassifier = null

  
  // Subpaneles dentro del panel
  val panel1 = new BoxPanel(Orientation.Horizontal) {
    contents += classifierLabel
    contents += Swing.HStrut(6)
    contents += classifierTextField
    contents += Swing.HStrut(6)
    contents += chooseButton
  }

  val panel3 = new BoxPanel(Orientation.Horizontal) {
    contents += crossValidationLabel
    contents += crossValidationCheckBox
    contents += Swing.HStrut(6)
    contents += crossValidationTextField
    contents += Swing.HStrut(6)
    contents += cvSeedLabel
    contents += Swing.HStrut(6)
    contents += cvSeedTextField
  }

  //TODO SOLO INSTANCIAMOS ALGO SIN SENTIDO QUE EN NINGÚN MOMENTO USAMOS. Revisarlo
  var panel2 = new GridPanel(1, 1)

  val biggestPanel = new BoxPanel(orientation) {
    border = new EmptyBorder(3, 10, 3, 10)
    contents += panel1
    contents += Swing.VStrut(6)
    contents += panel3
    contents += Swing.VStrut(6)
  }

  // Añadimos los contendis
  contents += biggestPanel

  // Añadimos listeners y eventos a los botones.
  
  listenTo(classifierTextField)
  listenTo(chooseButton)
  reactions += {
    case ButtonClicked(`chooseButton`) => {

      //TODO Ese if...(como en todas las comunicaciones entre paneles y dialogos)
      val chooseElementDialog = new ChooseElementDialog(this.peer,
          "resources/availableClassifiers.xml")
      if (chooseElementDialog.chosenAlgorithm != "") {
        panel2.contents.remove(0, panel2.contents.size)
        val algorithmName = chooseElementDialog.chosenAlgorithm
        classifierTextField.text = algorithmName

        //Cargar la clase

        classificationAlg = Class.forName(algorithmName).newInstance.asInstanceOf[TraitClassifier]

        //Cargar el número de atributos con tips y demás

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
        //Dibujar
        biggestPanel.contents += panel2
        biggestPanel.revalidate()
        biggestPanel.repaint()

      }
    }
  }

  def getClassifierOptions(): String = {

    var iter = options.iterator
    var command: String = classifierTextField.text + " "
    
    for (i <- 1 until panel2.contents.size by 2) {
      var actualOption = iter.next()
      if (actualOption.optionType == 0) {
        if (panel2.contents(i).asInstanceOf[CheckBox].selected)
          command += actualOption.command + " "
      } else if (actualOption.optionType == 1) {
        command +=
          actualOption.command + " " + panel2.contents(i).asInstanceOf[TextField].text + " "
      } else {
        command +=
          actualOption.command + " " + panel2.contents(i).asInstanceOf[ComboBox[Seq[String]]].selection + " "
      }
    }
    command
  }
  
  def getCrossValidationOptions(): String = {
    if(crossValidationCheckBox.selected){
      //TODO No hay campo para la semilla, así que se mete aquí de momento
      crossValidationTextField.text + " " + cvSeedTextField.text + " "
    }
    else{
      ""
    }
  }

}