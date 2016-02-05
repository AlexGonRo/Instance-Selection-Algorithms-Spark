package utils

/**
 * Clase que ofrece diferentes opciones de cálculo de distancias
 * entre dos o más puntos.
 *
 * @constructor Crea una nueva calculadora de distancias.
 *
 * @author Alejandro González Rogel
 * @version 1.0.0
 */
class DistCalculator extends Serializable {

  /**
   * Calcula la distancia euclidea entre dos vectores de datos numéricos.
   *
   * El cálculo de esta distancia no es completo, se suprime la operación de la
   * raiz cuadrada con la intención de ahorrar operaciones.
   *
   * @param point1  Un punto de la medición.
   * @param point2  Segundo punto.
   */
  def euclideanDistance(point1: Array[Double],
                        point2: Array[Double]): Double = {

    var dist = 0.0
    for { i <- 0 until point1.size } {
      dist += Math.pow((point1(i) - point2(i)), 2)
    }

    dist
  }

}
