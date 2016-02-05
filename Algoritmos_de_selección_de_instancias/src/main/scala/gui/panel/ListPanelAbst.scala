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
 * Panel que define una estructura con una lista y un par
 * de botones que permiten añadir o eliminar elementos de dicha lista.
 *
 * Los elementos de la lista serán eliminados cuando, al estar seleccionados, se
 * presione sobre el botón destinado a tal fin. En cambio, la opción para
 * añadir nuevos elementos queda por definir en las clases hijas.
 *
 * @param title Título del panel.
 * @param printBorder Indica si se desea generar un borde alrededor del panel
 *  que defina sus límites.
 * @constructor Genera un panel compuesto por una lista y dos botones que
 *   permiten gestionarla.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
abstract class ListPanelAbst(printBorder: Boolean = false,
                             title: String = "") extends BorderPanel {

  // Elementos del panel
  /**
   * Configuraciones seleccionadas hasta el momento.
   */
  var seqConfigurations = new ArrayBuffer[String]
  /**
   * Tamaño del magen superior e inferior de los subpaneles.
   */
  private val tdb = 3
  /**
   * Tamaño de los márgenes laterales de los subpaneles.
   */
  private val lb = 10
  /**
   * Separación entre los componentes de un layout cuadriculado.
   */
  private val vgap = 6
  /**
   * Separación lateral entre los botones y la lista.
   */
  private val hgapButList = 7
  /**
   * Botón para añadir una nueva configuración
   */
  private val addButton = new Button("Añadir...")
  /**
   * Botón para eliminar la configuración seleccioanda.
   */
  private val rmButton = new Button("Eliminar")
  /**
   * Lista que muestra las configuraciones elegidas hasta el momento.
   */
  protected val confList = new ListView(seqConfigurations)
  /**
   * Barra de movimiento.
   */
  private val listWithScroll = new ScrollPane(confList)

  confList.selection.intervalMode = IntervalMode.Single

  // Añadimos todos los elementos al panel
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

  // Añadimos eventos y la capacidad de detectarlos.
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
   * Abre un nuevo diálogo para permitir crear una nueva configuración.
   */
  protected def addButtonAction(): Unit

  /**
   * Elimina la configuración actualmente seleccionada en la lista.
   */
  protected def rmButtonAction(): Unit = {
    if (confList.selection.items.iterator.hasNext) {
      val confSelected = confList.selection.indices.head
      seqConfigurations.remove(confSelected)
      confList.listData = seqConfigurations
    }
  }

  /**
   * Comprueba si una configuración ya ha sido definida anteriormente
   *
   * @param conf Configuración que deseamos comprobar
   * @return Verdadero si existe, falso si no
   */
  protected def confAlreadyExists(conf: String): Boolean = {
    if (seqConfigurations.contains(conf)) {
      true
    } else {
      false
    }
  }

}
