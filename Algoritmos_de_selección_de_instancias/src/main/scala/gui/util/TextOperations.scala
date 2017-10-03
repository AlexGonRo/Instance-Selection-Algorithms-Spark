package gui.util

import scala.collection.mutable.ArrayBuffer

/**
 * Class that is able to perform the few string processing operations that will be needed when we use the graphical interface.
 *
 */
object TextOperations {

  /**
   * Given a string, it splits it by whitespaces.
   *
   * If there is a path to a file that contains a whitespace, it does not split it.
   *
   * @param initialText String
   * @param Array with the split text.
   */
   // TODO Change the name of this method to splitTextBySpace?
  def splitTextWithSpaces(initialText: String): ArrayBuffer[String] = {
    var result = ArrayBuffer.empty[String]

    // If we need to concatenate the next string,
    var keepConcatenating = false

    val itSplitBySpace = initialText.split(" ")

    for { i <- 0 until itSplitBySpace.size } {
      // If we are not reading a path with whitespaces.
      if (!keepConcatenating) {
        result += itSplitBySpace(i)
        if (itSplitBySpace(i).startsWith("\"")) {
          keepConcatenating = true
        }
        // If we are reading a path with whitespaces.
      } else {
        val lastResult = result.last
        result = result.dropRight(1)
        result += lastResult.concat(" ").concat(itSplitBySpace(i))
      }
      // Check if we are at the end of a path and update if necessary.
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
