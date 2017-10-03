package gui.panel

import scala.swing.Dialog

import gui.dialog.FilterDialog

/**
 * Panel that allows the user to select which filter to apply to the dataset before
 * it is classifier.
 *
 * It displays a list with all the filters that have already been selected
 * and a couple of buttons that allow the user to modify that list.
 *
 * @constructor Creates and draws the panel.
 * @param printBorder Whether we want to draw a line around the panel.
 * @param title Title of the panel.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class FilterPanel(printBorder: Boolean,
                  title: String) extends
                  ListPanelAbst(printBorder: Boolean, title: String) {

  protected def addButtonAction(): Unit = {

    val confDialog = new FilterDialog(this.peer, true)
    val conf = confDialog.command
    if (confAlreadyExists(conf)) {
      // TODO HARDCODED TEXT
      Dialog.showMessage(this, “The chosen configuration had been already defined.”)
    } else if (conf != "") {
      seqConfigurations += conf
      confList.listData = seqConfigurations
    }
  }

}
