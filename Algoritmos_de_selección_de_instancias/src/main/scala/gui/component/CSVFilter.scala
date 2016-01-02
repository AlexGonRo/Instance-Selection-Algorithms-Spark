package gui.component

import java.io.File
import javax.swing.filechooser.FileFilter
import javax.swing._
import javax.swing.filechooser._
 
/**
 * Filtro del selector de archivos de Java que permite visualizar unicamente
 * los archivos con extensión .csv
 * 
 * @version 1.0
 * @author Alejandro González Rogel
 */
class CSVFilter extends FileFilter {

  override def accept(f: File): Boolean = {
    if (f.isDirectory()) {
      return true
    }

    val fileName = f.getName()
    val extension = fileName.substring(fileName.lastIndexOf(".") + 1)
    if (extension != null) {
      if (extension.equals("csv")) {
        return true;
      } else {
        return false;
      }
    }

    return false;
  }

  override def getDescription(): String = {
    "Archivos CSV";
  }
}