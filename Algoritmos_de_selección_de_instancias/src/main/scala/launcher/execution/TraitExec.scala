package launcher.execution

/**
 * Interface of the Job launcher.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
trait TraitExec {

  /**
   * Launches a data mining job.
   *
   * @param Execution arguments.
   */
  def launchExecution(args: Array[String]): Unit

}
