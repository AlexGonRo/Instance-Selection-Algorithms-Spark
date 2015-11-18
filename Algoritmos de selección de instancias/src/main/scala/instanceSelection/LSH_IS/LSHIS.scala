package instanceSelection.LSH_IS

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import instanceSelection.Abstract.AbstractIS

/**
 *
 * Implementación del algoritmo Locality Sensitive Hashing Instance Selection(LSH IS).
 *
 * LSH-IS es un algoritmo de selección de instancias apoyado en el uso de LSH.
 * La idea es aplicar un  algoritmo de LSH sobre el conjunto de instancias
 * inicial, de manera que podamos agrupar en un mismo bucket
 * aquellas instancias con un alto grado de similitud.
 * Posteriormente, de cada uno de esos buckets seleccionaremos una
 * instancia de cada clase, que pasará a formar parte del conjunto
 * de instancias final.
 *
 *
 * @param args 	Argumentos para inicializar los valores iniciales del algoritmo.
 *
 * @author Alejandro
 * @version 1.0.0
 */
class LSHIS(args: Array[String]) extends AbstractIS(args) {

  //TODO Podríamos sobrecargar el constructor para que además de aceptar el
  //array de strings acepte también valores sueltos.
  //El problema es que esta sobrecarga tiene restricciones en Scala, así que 
  //queda pendiente.

  //Número de funciones hash a utilizar.
  var numOfHashes: Int = _
  //Número de dimensiones de nuestro conjunto de datos.
  var dim: Int = _ //TODO No podemos leerlo directamente de los datos
  //si el vector es disperso. Se puede arreglar si lo 
  //indicamos si es disperso por parámetro
  //Tamaño de los "buckets".
  var width: Double = _
  //Semilla para los números aleatorios
  var seed: Long = _

  //Leemos los argumentos de entrada para, en caso de haber sido 
  //introducido alguno, modificar los valores por defecto de los atributos
  try {
    readArgs(args)
  } catch {
    case ex: IllegalArgumentException => {
      println("ERROR AL LEER LOS ATRIBUTOS DEL PROGRAMA")
      //TODO Mejorar la salida indicando los posibles parámetros del programa
    }
  }

  //TODO Mejora sugerida: Asignar a cada instancia un número para no tener 
  // que arrastrarla todo el proceso.
  override def instSelection(
    sc: SparkContext,
    parsedData: RDD[LabeledPoint]): RDD[LabeledPoint] = {

    //Creamos una tabla hash con los diferentes vectores aleatorios.
    val tablaHash = new HashTable(numOfHashes, dim, width, seed)

    //Transformamos la RDD para generar tuplas de (bucket asignado,clase) 
    //e instancia
    val bucketClassInstTuple = parsedData.map { instancia =>
      ((tablaHash.hash(instancia.features), instancia.label), instancia)
    }
    val cosa2 = bucketClassInstTuple.groupByKey.collect()

    //Agrupamos cada par (bucket, clase) y seleccionamos una instancia de 
    //cada grupo
    val result = bucketClassInstTuple.groupByKey.map[LabeledPoint] {
      case (tupla, instancias) => instancias.head
    }
    val cosa = result.collect()
    return result
  }

  /**
   * Leemos cada valor del array pasado por parámetro y actualizamos
   * los atributos correspondientes.
   *
   * @param args	Argumentos del programa para inicializar el algoritmo.
   * 							El formato requerido es el siguiente: Existirá un par
   * 							"String"-"Valor" por cada atributo, siendo el String
   * 							el que indicará a que atributo nos referimos.
   * @throws 	IllegalArgumentException En caso de no respetarse el formato
   * 																	 mencionado			.
   */
  override def readArgs(args: Array[String]) = {

    for (i <- 0 until args.size by 2) {
      if (args(i) == "-numH") {
        numOfHashes = args(i + 1).toInt
      } else if (args(i) == "-dim") {
        dim = args(i + 1).toInt
      } else if (args(i) == "-w") {
        width = args(i + 1).toInt
      } else if (args(i) == "-s") {
        seed = args(i + 1).toInt
      } else {
        throw new IllegalArgumentException
      }

    }

  }

}
