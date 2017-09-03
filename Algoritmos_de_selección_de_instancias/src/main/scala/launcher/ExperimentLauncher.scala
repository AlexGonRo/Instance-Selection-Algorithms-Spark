package launcher

import launcher.execution.ISClassExec
import launcher.execution.TraitExec
import launcher.execution.ISClassSeqExec
import launcher.execution.ClassExec
import launcher.execution.ISClassSeqExecTest
import launcher.execution.ClassSeqExec

/**
 * It starts any data mining job.
 * 
 * One job could contain zero or more filters and at least one classifier.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
object ExperimentLauncher {

  /**
   * Executes a data mining job.
   *
   * @param args Execution parameters.
   *   The first parameter states the execution type and does not condition any of
   *   the individual properties of the tasks that form the job.
   */
  @throws(classOf[IllegalArgumentException])
  def main(args: Array[String]): Unit = {

    val experimentType = args.head

    val execution: TraitExec = experimentType match {
      case "ClassExec"       => new ClassExec
      case "ClassSeqExec"       => new ClassSeqExec
      case "ISClassExec"     => new ISClassExec
      case "ISClassSeqExec"  => new ISClassSeqExec
      case "ISClassSeqExecTest" => new ISClassSeqExecTest
      case _ =>
        throw new IllegalArgumentException(experimentType + "is not an " +
          "execution type.")
    }

    execution.launchExecution(args.drop(1))

  }

}
