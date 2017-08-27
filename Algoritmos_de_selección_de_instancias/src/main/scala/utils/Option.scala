package utils

/**
 * Stores information about a configurable parameter of the execution.
 *
 * @param  name  Complete name.
 * @param  description  Description.
 * @param  command  Command associated to this option.
 * @param  default  Default value.
 * @param  optionType  Option type:
 *
 *   0 if the Option is a boolean Option.
 *
 *   1 otherwise.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 *
 */
class Option(val name: String, val description: String, val command: String,
             val default: String, val optionType: Int)

