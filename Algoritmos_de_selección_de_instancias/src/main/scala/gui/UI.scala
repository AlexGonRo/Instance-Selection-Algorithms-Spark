package gui

import scala.swing.BoxPanel
import scala.swing.Dimension
import scala.swing.GridPanel
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.Swing

import gui.panel.ClassifierPanel
import gui.panel.DatasetPanel
import gui.panel.DownMenuPanel
import gui.panel.FilterPanel
import gui.panel.SparkPanel
import gui.panel.UpMenuPanel

/**
 * Ventana principal de la interfaz gráfica.
 *
 * @constructor Genera la ventana principal de nuestra aplicación.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class UI extends MainFrame {

  /**
   * Anchura de la ventana.
   */
  private val xDim = 800
  /**
   * Largo de la ventana.
   */
  private val yDim = 500
  /**
   * Espacio superior de la interfaz.
   */
  val upMenuPanel = new UpMenuPanel(this)
  /**
   * Espacio inferior de la interfaz.
   */
  val downMenuPanel = new DownMenuPanel(this)
  /**
   * Espacio dedicado a la selección de conjuntos de datos.
   */
  val datasetPanel = new DatasetPanel(this)
  /**
   * Espacio dedicado a la selección de filtros.
   */
  val filterPanel = new FilterPanel(this)
  /**
   * Espacio dedicado a la selección de clasificador y a los parámetros
   * de la validación cruzada.
   */
  val classifierPanel = new ClassifierPanel(this, Orientation.Vertical)
  /**
   * Espacio dedicado a la selección las opciones de Spark.
   */
  val sparkPanel = new SparkPanel(this, Orientation.Vertical)

  title = "Spark IS GUI"
  preferredSize = new Dimension(xDim, yDim)
  contents = new BoxPanel(Orientation.Vertical) {
    contents += upMenuPanel
    contents += Swing.VStrut(10)
    contents += sparkPanel
    contents += Swing.VStrut(10)
    contents += datasetPanel
    contents += Swing.VStrut(10)
    contents += new GridPanel(1, 2) {
      contents += filterPanel
      contents += classifierPanel
    }
    contents += Swing.VStrut(10)
    contents += downMenuPanel
    border = Swing.EmptyBorder(10, 10, 10, 10)
  }

}
