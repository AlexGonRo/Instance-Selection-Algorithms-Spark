package instanceSelection

/**
 * Contiene todos aquellos algoritmos que, pese a ser
 * ejecutados en Spark, no poseen una implementación paralela.
 *
 * Las causas para la implementación linear son ,bien por imposibilidad
 * de ejecutarse en paralelo, o bien porque no se les ha querido
 * implemenar de tal forma.
 *
 */

package object seq
