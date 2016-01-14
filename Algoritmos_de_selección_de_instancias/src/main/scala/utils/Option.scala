package utils

/**
 * Clase que almacena toda la información necesaria de una opción de un
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
 *   2 indica múltiples opciones de entre cadenas de texto.
 *
 * @param possibilities  Posibles opciones seleccionables para el tipo de opción 2.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 *
 */
class Option(val name: String, val description:String, val command:String,
    val default:Any, val optionType:Int,val possibilities:Seq[String]=Seq.empty)

