package gui

import scala.swing._

/**
 * 
 * Clase lanzadora de la interfaz gráfica.
 * 
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
object SparkISGUI extends App{
  
  override def main(args: Array[String]) {
    val ui = new UI
    ui.visible = true
  }
}