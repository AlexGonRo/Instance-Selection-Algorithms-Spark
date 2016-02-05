package gui.util

import scala.collection.mutable.ArrayBuffer

/**
 * Objeto que guarda métodos para el procesamiento de cadenas de texto.
 *
 */
object TextOperations {

  /**
   * Divide correctamente la cadena de texto, cuidando
   * de tener en cuenta los espacios en las rutas.
   *
   * @param initialText Texto inicial
   * @param Texto dividido
   */
  def splitTextWithSpaces(initialText: String): ArrayBuffer[String] = {
    var result = ArrayBuffer.empty[String]

    // Variable que almacena cuando debemos seguir concatenando una ruta.
    var keepConcatenating = false

    val itSplitBySpace = initialText.split(" ")

    for { i <- 0 until itSplitBySpace.size } {
      // Si no estamos leyendo una ruta con espacios.
      if (!keepConcatenating) {
        result += itSplitBySpace(i)
        if (itSplitBySpace(i).startsWith("\"")) {
          keepConcatenating = true
        }
        // Si estamos leyendo una ruta con espacios
      } else {
        val lastResult = result.last
        result = result.dropRight(1)
        result += lastResult.concat(" ").concat(itSplitBySpace(i))
      }
      // Comprobamos si hemos llegado al ginal de una ruta con espacios
      // y actuamos en consecuencia si fuera así.
      if (itSplitBySpace(i).endsWith("\"")) {
        var valueWithSpaces = result.last
        result = result.dropRight(1)
        valueWithSpaces = valueWithSpaces.drop(1)
        valueWithSpaces = valueWithSpaces.dropRight(1)
        result += valueWithSpaces
        keepConcatenating = false
      }
    }
    result

  }

}
