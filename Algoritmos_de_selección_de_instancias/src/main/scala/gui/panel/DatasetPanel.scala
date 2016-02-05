package gui.panel

import scala.swing.Dialog

import gui.dialog.DatasetDialog

/**
 * Panel que contiene todo lo referente a la configuración del conjunto de
 * datos.
 *
 * @constructor Genera un panel que permita seleccionar una o varias
 * configuraciones para conjuntos de datos.
 * @param printBorder Indica si se desea generar un borde alrededor del panel
 *  que defina sus límites.
 * @param Genera un panel compuesto por una lista y dos botones que
 *   permiten gestionarla.
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
      Dialog.showMessage(this, "La configuración introducida ya existía " +
        "con anterioridad.")
    } else if (conf != "") {
      seqConfigurations += conf
      confList.listData = seqConfigurations
    }
  }

}
