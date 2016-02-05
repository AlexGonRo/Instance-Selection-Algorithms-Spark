package launcher.execution

/**
 * Define los elementos necesarios para lanzar la ejecución de un trabajo de
 * minería de datos.
 *
 *
 * Participa en el patrón de diseño Strategy", en el que actúa con el rol
 * de estrategia("Strategy"). En el uso de este patrón será usado por la clase
 * [[launcher.ExperimentLauncher]], que actua como contexto("context")
 * y permitirá implementar clases que actuen como estrategias concretas
 * ("concrete strategies") dentro del patrón.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
trait TraitExec {

  /**
   * Ejecucuta una labor de minería de datos.
   *
   * @param Cadena de argumentos que contienen la configuración de la ejecución.
   */
  def launchExecution(args: Array[String]): Unit

}
