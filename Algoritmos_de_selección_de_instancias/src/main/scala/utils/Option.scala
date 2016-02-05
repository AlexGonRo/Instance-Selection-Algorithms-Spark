package utils

/**
 * Almacena toda la información necesaria sobre una opción configurable de un
 * clasificador o selector de instancias.
 *
 * @param  name  Nombre formal.
 * @param  description  Descripción de la función de la opción.
 * @param  command  Comando de consola para configuar la opción.
 * @param  default  Valor por defecto.
 * @param  optionType  Tipo de opción:
 *
 *   0 indica atributo booleano.
 *
 *   1 indica atributo numérico.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 *
 */
class Option(val name: String, val description: String, val command: String,
             val default: String, val optionType: Int)

