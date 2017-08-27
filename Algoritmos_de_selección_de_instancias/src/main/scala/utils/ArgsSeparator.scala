package utils

/**
 * Set of flags that, introduced as a command argument, indicate the program
 * to which part of the execution the following values belongs to.
 * There are currently 4 flags.
 *
 */
object ArgsSeparator extends Enumeration {

  type ArgsDividers = Value
  
  /**
   * The following values will modify file reader attributes.
   */
  final val READER_SEPARATOR = Value("-r")
  /**
   * The following values will modify the attributes of a data filter
   or an instance selector.
   */
  final val FILTER_SEPARATOR = Value("-f")
  /**
   * The following values will modify the attributes of a classifier.
   */
  final val CLASSIFIER_SEPARATOR = Value("-c")
  /**
   * The following values will modify cross validation attributes.
   */
  final val CROSSVALIDATION_SEPARATOR = Value("-cv")

}
