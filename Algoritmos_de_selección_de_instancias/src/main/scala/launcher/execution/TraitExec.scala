package launcher.execution

/**
 * Define los elementos necesarios para lanzar una ejecución de un trabajo de
 * minería de datos.
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
