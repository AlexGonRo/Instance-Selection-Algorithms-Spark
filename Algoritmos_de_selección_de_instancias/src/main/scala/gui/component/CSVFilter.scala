package gui.component

import java.io.File

import javax.swing.filechooser.FileFilter

/**
 * Filter for the Java file selector [[javax.swing.filechooser]].
 *
 * Allows visualisation of the files or just those with a .csv extension.
 *
 * @constructor Creates a new filter for the .csv files.
 *
 * @version 1.0
 * @author Alejandro González Rogel
 */
class CSVFilter extends FileFilter {

  /**
   * Decides whether a file passes the filter.
   *
   * It accepts directories and all those files that end in “.csv”.
   *
   * @param  f  File or directory
   * @return Boolean. Whether the file was accepted or not.
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
   * Descriptive name of the filter.
   *
   * It is shown at the top of the filter window.
   *
   * @return Name of the filter.
   */
  override def getDescription(): String = {
    // TODO Hardcoded text.
    "CSV files”;
  }
}
