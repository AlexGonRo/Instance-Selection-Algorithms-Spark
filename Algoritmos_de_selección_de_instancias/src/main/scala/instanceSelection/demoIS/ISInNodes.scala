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
    var seleccionadas = linearIS.instSelection(instancias)

    // Actualizamos
    var iterador = seleccionadas.iterator
    while (iterador.hasNext) {
      var instActual = iterador.next
      var otroIterador = contInstTuple.iterator
      var cuenta = -1
      while (otroIterador.hasNext) {
        cuenta += 1
        var cosa = otroIterador.next
        var otraInstActual = cosa._2
        var contador = cosa._1
        if (instActual.eq(otraInstActual)) {
          contInstTuple.update(cuenta,
              (contInstTuple.get(cuenta).get._1 + 1,
                  contInstTuple.get(cuenta).get._2))
        }
      }
    }
    contInstTuple.iterator
  }

}
