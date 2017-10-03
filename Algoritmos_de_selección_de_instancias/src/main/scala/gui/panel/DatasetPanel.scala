package gui.panel

import scala.swing.Dialog

import gui.dialog.DatasetDialog

/**
 * Panel with information about the datasets.
 *
 * @constructor Creates and draws a panel that allows the user to select the
 * datasets.
 * @param printBorder Whether we want to draw a line around this panel components.
 * @param Title Title of this panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DatasetPanel(printBorder: Boolean,
                   title: String)
    extends ListPanelAbst(printBorder: Boolean, title: String) {

  protected def addButtonAction(): Unit = {
    val confDialog = new DatasetDialog(this, true)
    val conf = confDialog.command
    if (confAlreadyExists(conf)) {
      // TODO HARDCODED TEXT
      Dialog.showMessage(this, “This configuration had been already defined.”)
    } else if (conf != "") {
      seqConfigurations += conf
      confList.listData = seqConfigurations
    }
  }

}
