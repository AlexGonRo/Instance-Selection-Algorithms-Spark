package InstanceSelector.LSH_IS

import org.apache.spark.mllib.linalg.Vector

@SerialVersionUID(1L)
class HashTable(numberOfHashes: Int, dimensions: Int, width: Double) extends Serializable {
  //TODO Vistazo a la accesibilidad de los parámetros.

  
  var i = 0  //TODO Echar un vistazo para quitar esta linea y meterla en el for
  val mHashFunctions = for (i <- 0 to numberOfHashes)
    yield new EuclideanHash(dimensions, width)


  

  def hash(attr : Vector):Int = {
    
    var i=0 
    val hashValues = for(i<-0 until attr.size ) yield mHashFunctions(i).hash(attr)
      
    //TODO De momento usaamos el código de hash de Arrays de Java puesto directamente
    i=0
    var resultado = 0
    for(i<-0 until hashValues.size)
      resultado = resultado*31 + hashValues(i)
      
    return resultado
  }
  
  
}
