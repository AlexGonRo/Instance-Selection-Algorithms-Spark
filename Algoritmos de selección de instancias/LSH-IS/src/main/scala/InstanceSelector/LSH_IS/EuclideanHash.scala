package InstanceSelector.LSH_IS

import scala.util.Random 
import org.apache.spark.mllib.linalg.Vector

@SerialVersionUID(1L)
class EuclideanHash(var dimensions: Int, var width: Double) extends Serializable {
  //TODO Comprobar a la accesibilidad de los parámetros.

  //Objeto destinado a crear números aleatorios.
  val r = new Random

  //Calculamos el desplazamiento que vamos a utilizar
  val mOffset = {
    if (width < 1.0)
      r.nextInt(width.toInt * 10) / 10.0
    else
      r.nextInt(r.nextInt(width.toInt))
  }

  //Creamos el vector aleatorio
  var i = 0 
  val mRandomProjection = for (i <- 0 until dimensions) yield r.nextGaussian()

  def hash(attr: Vector): Int = {

    var sum=0.0;
    var i=0;
    var result = 0.0;
    for(i<-0 until attr.size){
      sum = sum + (mRandomProjection(i) * attr(i))
    }
    
    result = (sum+mOffset)/width
    
    return result.round.toInt

  }

}
