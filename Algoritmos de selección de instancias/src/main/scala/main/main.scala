package main

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import java.io.File
import instanceSelection.LSH_IS.LSHIS

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
 * @version 1.0.0
 */
object main {

  /**
   * Función principal encargada de aplicar un algoritmo de selección de
   * instancias sobre un conjunto de datos y mostrar el resultado.
   *
   * @param args	Argumentos indicados en la invocación del objeto.
   * 							La estructura de estos argumentos debería ser la siguiente:
   * 							args(0) debería contener la ruta al conjunto de datos
   * 							El resto de argumentos depende de cada algoritmo
   */
  def main(args: Array[String]) {

    //La configuración la proporcionamos a la hora de invocar Spark por consola 
    //la aplicación. Utilizar estas lineas solo para ejecutar 
    //directamente en Eclipse.
    //val conf = new SparkConf().setAppName("Lanzador").setMaster("local[2]")
    //val sc = new SparkContext(conf)

    //Esta linea es la utilizada en el producto final.
    //Los datos del contexto se obtienen de los parámetros utilizados al llamar
    //a Spark
    val sc = new SparkContext(new SparkConf())

    //Leemos del fichero que contiene el conjunto de datos y 
    //almacenamos las isntancias en una estructura RDD.
    val data = sc.textFile(args(0))

    //Realizamos una transformación sobre la RDD para almacenar las instancias en
    //LabeledPoints,que contienen un vector con los atributos y un valor de clase.
    //TODO Funciona suponiendo que el atributo de clase es el último de los
    //atributos
    val parsedData =
      //Si el atributo de clase es el último
      data.map { line =>
        val features = line.split(',')
        LabeledPoint(features.last.toDouble,
          Vectors.dense(features.dropRight(1).map(_.toDouble)))
      }

    //TODO El algoritmo a eleguir deberá ser indicado al lanzar el programa

    //Instanciamos el selector de instancias y lo ejecutamos
    var selector = new LSHIS(args.drop(1))
    var result = selector.instSelection(sc, parsedData)

    //TODO Mejorar la salida
    //Borramos el directorio(si existiese) donde guardaremos el resultado 
    //y añadimos el resultado de la última ejecución
    delete(new File("results"))
    result.saveAsTextFile("results")

  }

  /**
   * Dado una ruta a un fichero o directorio, lo elimina en caso de existir.
   *
   * En el caso de que el directorio contenga ficheros, estos son eliminados
   * también. No eliminará, en caso de existir, directorios dentro del
   * indicado.
   *
   * @param File	Fichero o directorio sobre el que vamos a operar
   */
  def delete(file: File) {
    //Si es un directorio, eliminamos los ficheros que haya en su interior.
    if (file.isDirectory)
      Option(file.listFiles).map(_.toList).getOrElse(Nil).foreach(delete(_))

    file.delete
  }

}