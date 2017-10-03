package gui.panel

import java.awt.Color

import scala.collection.mutable.ArrayBuffer
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.BorderPanel.Position.West
import scala.swing.Button
import scala.swing.GridPanel
import scala.swing.ListView
import scala.swing.ListView.IntervalMode
import scala.swing.ScrollPane
import scala.swing.event.ButtonClicked

import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder

/**
 * Abstract panel that defines the basic structure of a list on the interface.
 * 
 * It also incorporates the two buttons that allow us to modify elements of the list.
 *
 * It defines the way we can eliminate elements of the list, but it does not define
 * they way we can add them.
 *
 * @param title Panel title
 * @param printBorder Whether we want to print a border around this panel.
 * @constructor Creates a panel with one list item and two buttons.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
abstract class ListPanelAbst(printBorder: Boolean = false,
                             title: String = "") extends BorderPanel {

  // ELEMENTS OF THIS PANEL
  /**
   * Configurations selected so far.
   */
  var seqConfigurations = new ArrayBuffer[String]
  /**
   * Up and down margin of the children panels.
   */
  private val tdb = 3
  /**
   * Lateral margin of the children panels.
   */
  private val lb = 10
  /**
   * Spacing between elements in a grid layout.
   */
  private val vgap = 6
  /**
   * Distance between the list and the buttons.
   */
  private val hgapButList = 7
  /**
   * Button to add a new element.
   */
  private val addButton = new Button(“Add…”)
  /**
   * Botón to eliminate an element.
   */
  private val rmButton = new Button(“Delete”)
  /**
   * List with all the configurations selected so far.
   */
  protected val confList = new ListView(seqConfigurations)
  /**
   * Lateral bar for the list.
   */
  private val listWithScroll = new ScrollPane(confList)

  confList.selection.intervalMode = IntervalMode.Single

  // Add elements to the panel.
  if (printBorder) {
    border = new TitledBorder(new LineBorder(Color.BLACK, 1, true),
      title)
  }
  layout += new BorderPanel {
    border = new EmptyBorder(tdb, lb, tdb, lb)
    layout += new GridPanel(2, 1) {
      border = new EmptyBorder(0, 0, 0, hgapButList)
      vGap = vgap
      contents += addButton
      contents += rmButton
    } -> West
    layout += listWithScroll -> Center
  } -> Center

  // Add events and listeners.
  listenTo(addButton)
  listenTo(rmButton)
  reactions += {
    case ButtonClicked(`addButton`) => {
      addButtonAction()
    }
    case ButtonClicked(`rmButton`) => {
      rmButtonAction()
    }
  }

  /**
   * Opens a new dialog to add an element to the list.
   */
  protected def addButtonAction(): Unit

  /**
   * If we want to delete an element of the list, it must be highlighted by the time
   * we click the button.
   */
  protected def rmButtonAction(): Unit = {
    if (confList.selection.items.iterator.hasNext) {
      val confSelected = confList.selection.indices.head
      seqConfigurations.remove(confSelected)
      confList.listData = seqConfigurations
    }
  }

  /**
   * Check whether the defined configuration is correct.
   *
   * @param conf Configuration
   * @return True if the configuration is correct.
   */
  protected def confAlreadyExists(conf: String): Boolean = {
    if (seqConfigurations.contains(conf)) {
      true
    } else {
      false
    }
  }

}
