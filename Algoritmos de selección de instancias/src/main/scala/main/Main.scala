package main

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import utils.FileReader
import utils.ISSelector
import utils.ResultSaver

/**
 *
 * Clase principal encargada de del lanzamiento de los diferentes algoritmos de
 * selección.
 *
 * Lanzará el hilo principal de la ejecución, que estará encargado de inicializar
 * el contexto Spark en el que trabajaremos, leer el conjunto de entrada, crear y
 * ejecutar un algoritmo de selección de instancias e imprimir la salida resultante
 * de dicha ejecución.
 *
 * @author Alejandro González Rogel
 * @version 1.1.0
 */
object Main {

  /**
   * Función principal de la ejecución.
   *
   * Encargada de aplicar un algoritmo de selección de
   * instancias sobre un conjunto de datos y mostrar el resultado.
   *
   * @param  args  Argumentos indicados en la invocación del objeto.
   *  La estructura de estos argumentos debería ser la siguiente:
   *  args(0)  debería contener el nombre del IS a utilizar
   *  args(1)  debería contener la ruta al conjunto de datos
   *  (Opcional)  Argumentos para el lector del conjunto de datos
   *  Argumentos para el IS
   */
  def main(args: Array[String]): Unit = {

    // La configuración la proporcionamos a la hora de invocar Spark por consola
    // la aplicación. Utilizar estas lineas solo para ejecutar
    // directamente en Eclipse.
    val conf = new SparkConf().setAppName("Lanzador").setMaster("local[2]")
    val sc = new SparkContext(conf)

    // Esta linea es la utilizada en el producto final.
    // Los datos del contexto se obtienen de los parámetros utilizados al llamar
    // a Spark
    // val sc = new SparkContext(new SparkConf())

    // Leemos el conjunto de datos
    val reader = new FileReader
    val (isArgs, readerArgs) = reader.divideArgs(args)
    val data = reader.readCSV(sc, readerArgs)

    // Instanciamos y ejecutamos el IS
    val isSelector = new ISSelector()
    val selector = isSelector.instanceAlgorithm(args(0), isArgs);
    val result = selector.instSelection(sc, data)

    // Salvamos el resultado en un fichero
    val resultSaver = new ResultSaver()
    resultSaver.storeInFile(args, result)

  }

}