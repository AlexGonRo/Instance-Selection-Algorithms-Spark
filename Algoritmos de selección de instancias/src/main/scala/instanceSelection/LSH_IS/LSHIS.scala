package instanceSelection.LSH_IS

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

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
 * @version 1.1.0
 */
class LSHIS(args: Array[String]) extends AbstractIS(args: Array[String]) {

  //TODO Podríamos sobrecargar el constructor para que además de aceptar el
  //array de strings acepte también valores sueltos.

  //Valores por defecto

  //Número de componentes-AND a utilizar.
  var ANDs: Int = 0
  //Número de componentes-OR a utilizar
  var ORs: Int = 0
  //Tamaño de los "buckets".
  var width: Double = 1
  //Semilla para los números aleatorios
  var seed: Long = 1

  //Reasignación de los valores por defecto en función de lo recibido al instanciar
  //la clase.
  readArgs(args)

  //Generador de números aleatorios.
  var r = new Random(seed)

  //TODO Mejora sugerida: Asignar a cada instancia un número para no tener 
  // que arrastrarla todo el proceso.
  override def instSelection(
    sc: SparkContext,
    parsedData: RDD[LabeledPoint]): RDD[LabeledPoint] = {

    //Número de dimensiones de nuestro conjunto de datos.
    var dim = parsedData.first().features.size + 1

    //Creamos tantos componentes AND como sean requeridos.
    var andTables: ArrayBuffer[HashTable] = new ArrayBuffer[HashTable]
    for (i <- 0 until ORs)
      andTables += new HashTable(ANDs, dim, width, r.nextInt())

    //Variable para almacenar el resultado final
    var finalResult: RDD[LabeledPoint] = null

    for (i <- 0 until ORs) {

      val andTable = andTables(i)

      //Transformamos la RDD para generar tuplas de (bucket asignado, clase) 
      // e instancia
      val keyInstRDD = parsedData.map { instancia =>
        ((andTable.hash(instancia.features), instancia.label), instancia)
      }

      val keyInstRDDGroupBy = keyInstRDD.groupByKey

      if (i == 0) { //Si es la primera iteración del bucle for
        //seleccionamos una instancia por key
        finalResult = keyInstRDDGroupBy.map[LabeledPoint] {
          case (tupla, instancias) => instancias.head
        }
      } else { //Si no es la primera iteración del bucle for (primer componente OR)

        //Recalculamos los buckets para las instancias ya seleccionadas
        //en otras iteraciones
        val alreadySelectedInst = finalResult.map { instancia =>
          ((andTable.hash(instancia.features), instancia.label), instancia)
        }

        //Sobre la RDD de la iteración, seleccionamos una instancia por key
        val keyClassRDD = keyInstRDDGroupBy.map[((Int, Double), LabeledPoint)] {
          case (tupla, instancias) => (tupla, instancias.head)
        }

        //Sobre la RDD de la iteración, seleccionamos aquellas las instancia
        //cuya key no esté repetida en el resultado final
        val keyClassRDDGroupBy = keyClassRDD.subtractByKey(alreadySelectedInst)
        val selectedInstances = keyClassRDDGroupBy.map[LabeledPoint] {
          case (tupla, instancia) => instancia
        }

        //Unimos el resultado de la iteración con el resultado parcial ya almacenado
        finalResult = finalResult.union(selectedInstances)
      }

    }

    return finalResult
  } //end instSelection

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
      args(i) match {
        case "-and" => ANDs = args(i + 1).toInt
        case "-w"   => width = args(i + 1).toInt
        case "-s"   => seed = args(i + 1).toInt
        case "-or"  => ORs = args(i + 1).toInt
        case _ =>
          printWrongArgsError()
          throw new IllegalArgumentException()
      }
    }

    //Si las variables no han sido asignadas con un valor correcto.
    if (ANDs <= 0 || ORs <= 0 || width <= 0) {
      printWrongArgsError()
      throw new IllegalArgumentException()
    }

  } //end readArgs

  override def printWrongArgsError() {
    System.err.println("Wrong input parameter format when launching the LSHIS algorithm.")
    System.err.println("Possible arguments are:")
    System.err.println("\t -and + int \t Number of AND-constructions to use. (Mandatory) ")
    System.err.println("\t -or + int \t Number of OR-constructions to use. (Mandatory)")
    System.err.println("\t -w + double\t Buckets width.(Default: 1)")
    System.err.println("\t -s + int\t Seed for the random number generator.(Default: 1)")
  }

}//end printWrongArgsError
