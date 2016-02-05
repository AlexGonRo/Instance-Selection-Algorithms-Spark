package gui.panel

import java.awt.Color

import scala.collection.mutable.ArrayBuffer
import scala.swing.BoxPanel
import scala.swing.Dialog
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.TextField

import gui.UI
import gui.dialog.SparkDialog
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder

/**
 * Panel que contiene todo lo referente a la configuración del clasificador.
 *
 * Este panel contendrá dos secciones, una donde se podrán definir una serie de
 * parámetros comúnes para cualquier ejecución realizada y otra donde poder
 * indicar configuraciones específicas para cada ejecución.
 *
 * @constructor Genera un panel que permita seleccionar una o varias
 * configuraciones para Spark.
 * @param parent Ventana desde donde se han invocado este panel.
 * @param Orientación, vertical u horizontal, en la que se irán situando
 *   los diferentes componentes del panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class SparkPanel(val parent: UI,
                 orientation: Orientation.Value) extends BoxPanel(orientation) {

  /**
   * Configuraciones seleccionadas hasta el momento.
   */
  var seqConfigurations = new ArrayBuffer[String]

  // Componentes
  /**
   * Tamaño del magen superior e inferior de los subpaneles.
   */
  private val tdb = 3
  /**
   * Tamaño de los márgenes laterales de los subpaneles.
   */
  private val lb = 10
  /**
   * Espacio entre el texto del directorio de Spark y su campo de texto.
   */
  private val sparkHGap = 14
  /**
   * Espacio entre el texto del master y su campo de texto
   */
  private val masterHGap = 51
  /**
   * Texto descriptivo para el campo que indica el lugar de instalación
   * de Spark.
   */
  private val sparkHomeLabel = new Label("Spark_Home")
  /**
   * Campo donde introducir el lugar de instalación de Spark.
   */
  private val sparkHomeTextField = new TextField()
  sparkHomeTextField.tooltip = "Directorio de instalación de Spark."
  sparkHomeTextField.text = sys.env.get("SPARK_HOME").getOrElse("")

  /**
   * Texto descriptivo para indicar el master a usar en la ejecución.
   */
  private val masterLabel = new Label("Master")
  /**
   * Campo donde indicar el master a usar en la ejecución.
   */
  private val masterTextField = new TextField()
  masterTextField.tooltip = "URL del nodo master. local[n]" +
    " para ejecuciones locales, spark://ruta:puerto para el resto"

  private val sparkListPanel = new SparkListPanel

  border = new TitledBorder(new LineBorder(Color.BLACK, 1, true), "Spark")

  // Añadimos los componentes al panel

  contents += new BoxPanel(Orientation.Horizontal) {
    border = new EmptyBorder(tdb, lb, tdb, lb)
    contents += sparkHomeLabel
    contents += Swing.HStrut(sparkHGap)
    contents += sparkHomeTextField
  }

  contents += new BoxPanel(Orientation.Horizontal) {
    border = new EmptyBorder(tdb, lb, tdb, lb)
    contents += masterLabel
    contents += Swing.HStrut(masterHGap)
    contents += masterTextField
  }
  contents += sparkListPanel

  /**
   * Selecciona las opciones de Spark comunes a todas las ejecuciones.
   *
   * Los parámetros comunes son: Spark_Home y Master.
   *
   * @return Opciones comunes a todas las ejecuciones.
   */
  def getCommonSparkOptions(): Array[String] = {

    var sparkHomeText = sparkHomeTextField.text
    // Si la ruta al home no contiene la barra divisora de directorios
    // al final, la añadimos.
    if (!sparkHomeText.endsWith(System.getProperty("file.separator"))) {
      sparkHomeText = sparkHomeText.concat(System.getProperty("file.separator"))
    }
    Array(sparkHomeText, masterTextField.text)
  }

  /**
   * Selecciona todas las configuraciones de Spark seleccionadas hasta el
   * momento.
   *
   * @return Listado con todas las configuraciones.
   */
  def getSparkConfOptions(): Array[String] = {
    sparkListPanel.seqConfigurations.toArray
  }

  /**
   * Panel que contiene una lista con todas las configuraciones de Spark
   * seleccionadas hasta la fecha.
   *
   * También presenta unos botones, necesarios para administrar los
   * elementos de la mencionada lista,que permiten operaciones para añadir
   * o eliminar elementos.
   */
  private class SparkListPanel extends ListPanelAbst {

    protected def addButtonAction(): Unit = {
      val confDialog = new SparkDialog(this.peer, true)
      val conf = confDialog.command
      if (confAlreadyExists(conf)) {
        Dialog.showMessage(this, "La configuración introducida ya existía " +
          "con anterioridad.")
      } else if (conf != "") {
        seqConfigurations += conf
        confList.listData = seqConfigurations
      }
    }

  }

}
