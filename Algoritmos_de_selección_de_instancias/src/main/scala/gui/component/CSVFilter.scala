package gui.component

import java.io.File

import javax.swing.filechooser.FileFilter

/**
 * Filtro del selector de archivos de Java [[javax.swing.filechooser]].
 *
 * Permite visualizar todos los archivos o únicamente aquellos con
 * extensión .csv.
 *
 * @constructor Crea un nuevo filtro.
 *
 * @version 1.0
 * @author Alejandro González Rogel
 */
class CSVFilter extends FileFilter {

  /**
   * Decide si un archivo o directorio pasa o no el filtro.
   *
   * Este filtro en concreto, permite el paso de directorios y de todos aquellos
   * archivos terminados con la extensión .csv.
   *
   * @param  f  Archivo que queremos filtrar.
   * @return Booleano indicando si el archivo debe mostrarse o no.
   */
  override def accept(f: File): Boolean = {
    if (f.isDirectory()) {
      true
    } else {
      val fileName = f.getName()
      val extension = fileName.substring(fileName.lastIndexOf(".") + 1)
      if (extension != null && extension.equals("csv")) {
        true
      } else {
        false
      }
    }
  }

  /**
   * Nombre descriptivo de la funcionalidad del filtro.
   *
   * @return Nombre descriptivo para el filtro.
   */
  override def getDescription(): String = {
    "Archivos CSV";
  }
}
