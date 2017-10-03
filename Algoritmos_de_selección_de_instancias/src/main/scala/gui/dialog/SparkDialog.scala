package gui.dialog

import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.EmptyBorder

/**
 * Dialog window that allows the user to tune some Spark parameters for the execution
 * of the data mining tasks.
 *
 * @constructor Create and draw a new dialog.
 * 
 * @param  myParent  Parent panel.
 * @param  modal  Whether this dialog should bock the rest of the interface.

 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class SparkDialog(myParent: JPanel, modal: Boolean) extends JDialog {
  
  /*
   * Command that translates the selected configuration into a string for the
   * library.
   */
  var command = ""

  // DIALOG COMPONENTS
  /**
   * Up and down margin of the children panels.
   */
  private val tdb = 6
  /**
   * Lateral margin of the children panels.
   */
  private val lb = 10
  /**
   * Distance between the different components of the grid layout.
   */
  private val gGap = 5
  /**
   * Field for the number of cores per executor.
   */
  private val coresExecutorLabel = new JLabel(“Number of cores per executor“)
  /**
   * Field to add the number of cores per executor.
   */
  private val coresExecutorTextField = new JTextField(2)
  /**
   * Label total number of cores.
   */
  private val totalCoresLabel = new JLabel(“Total number of cores“)
  /**
   * Field to select the maximum number of cores.
   */
  private val totalCoresTextField = new JTextField(2)
  /**
   * Label “Memory per executor.”
   */
  private val memExecutorLabel = new JLabel("Memory per executor“)
  /**
   * Field to specify the memory used per executor.
   */
  private val memExecutorTextField = new JTextField(2)

  /**
   * Button to add a new configuration.
   */
  private val okButton = new JButton("Añadir")
  /**
   * Cancel button.
   *
   * It closes the dialog.
   */
  private val cancelButton = new JButton("Cancelar")

  // Add tips to the Textfilds.
  // TODO Hardcoded text.
  coresExecutorTextField.setToolTipText(“Number of cores used“ +
    “per executor")
  totalCoresTextField.setToolTipText(“Number of cores per task")
  memExecutorTextField.setToolTipText(“Memory per executor. " +
    “YOU MUST SPECIFY THE ORDER OF MAGNITUDE " +
    "(m=mebibytes,g=gibibytes)")

  //coresExecutorTextField.setToolTipText("Número de núcleos usados" +
  //  "por cada unidad ejecutora (executor).")
  //totalCoresTextField.setToolTipText("Número total de núcleos para " +
  //  "usignar a una tarea.")
  //memExecutorTextField.setToolTipText("Cantidad de memoria usada por " +
  //  "unidad ejecutora (executor). DEBE INDICARSE UNIDAD " +
  //  "(m=mebibytes,g=gibibytes)")


  // PANELS
  /**
   * Panel with all the options we can modify.
   */
  private val panel1 = new JPanel()
  panel1.setBorder(new EmptyBorder(tdb, lb, tdb / 2, lb))
  panel1.setLayout(new GridLayout(3, 2, gGap, gGap))
  panel1.add(coresExecutorLabel)
  panel1.add(coresExecutorTextField)
  panel1.add(totalCoresLabel)
  panel1.add(totalCoresTextField)
  panel1.add(memExecutorLabel)
  panel1.add(memExecutorTextField)

  /**
   * Panel with add/cancel a new configuration.
   */
  private val panel2 = new JPanel()
  panel2.setBorder(new EmptyBorder(tdb / 2, lb, tdb, lb))
  panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS))
  panel2.add(cancelButton)
  panel2.add(okButton)

  // Add all the components to the panel.
  setTitle("Añadir nueva configuración de Spark")
  setLayout(new BoxLayout(this.getContentPane, BoxLayout.Y_AXIS))
  add(panel1)
  add(panel2)

  // Add listeners
  okButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: ActionEvent): Unit = {
      okActionPerformed(evt);
    }
  })

  cancelButton.addActionListener(new ActionListener() {
    def actionPerformed(evt: ActionEvent): Unit = {
      cancelActionPerformed(evt);
    }
  })

  pack()
  setLocationRelativeTo(myParent)
  setModal(modal)
  setVisible(true);

  /**
   * Action for when the user presses the Ok button.
   *
   * @param  evt  Event generated after clicking the Ok button.
   */
  private def okActionPerformed(evt: ActionEvent): Unit =
    {
      command += "--executor-cores " + coresExecutorTextField.getText + " "
      command += "--total-executor-cores " + totalCoresTextField.getText + " "
      command += "--executor-memory " + memExecutorTextField.getText + " "
      this.dispose();
    }

  /**
   * Action for when the user presses the Cancel button.
   *
   * @param  evt  Event generated after clicking the Cancel button.
   */
  private def cancelActionPerformed(evt: ActionEvent): Unit =
    {
      this.dispose();
    }

}
