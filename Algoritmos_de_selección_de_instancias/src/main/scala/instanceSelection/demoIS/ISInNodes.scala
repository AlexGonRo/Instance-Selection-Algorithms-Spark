package instanceSelection.demoIS

import scala.collection.mutable.MutableList

import org.apache.spark.mllib.regression.LabeledPoint

import instanceSelection.seq.abstracts.LinearISTrait

/**
 *
 */
@SerialVersionUID(1L)
private class ISInNodes extends Serializable {

  def applyIterationPerPartition(
      instancesIterator: Iterator[(Int, LabeledPoint)],
      linearIS: LinearISTrait): Iterator[(Int, LabeledPoint)] = {
    
    // Almacenamos todos los valores en listas
    var instancias = new MutableList[LabeledPoint]
    var contInstTuple = new MutableList[(Int, LabeledPoint)]
    while (instancesIterator.hasNext) {
      var tmp = instancesIterator.next
      contInstTuple += tmp
      instancias += tmp._2
    }

    // Ejecutamos el algoritmo
    var selected = linearIS.instSelection(instancias)

    // Actualizamos
      var iter = contInstTuple.iterator
      var actIndex = -1
      while (iter.hasNext) {
        actIndex += 1
        var actualInst = iter.next._2
        if (!selected.exists { inst => inst.eq(actualInst) }) {
          contInstTuple.update(actIndex,
              (contInstTuple.get(actIndex).get._1 + 1,
                  contInstTuple.get(actIndex).get._2))
        }
      }
    
    
    
    
    /*
    var iter1 = selected.iterator
    while (iter1.hasNext) {
      var actualInt1 = iter1.next
      var iter2 = contInstTuple.iterator
      var actIndex = -1
      while (iter2.hasNext) {
        actIndex += 1
        var actualInst2 = iter2.next
        var otraInstActual = actualInst2._2
        if (actualInt1.eq(otraInstActual)) {
          contInstTuple.update(actIndex,
              (contInstTuple.get(actIndex).get._1 + 1,
                  contInstTuple.get(actIndex).get._2))
        }
      }
    }
    
    */
    contInstTuple.iterator
  }

}
