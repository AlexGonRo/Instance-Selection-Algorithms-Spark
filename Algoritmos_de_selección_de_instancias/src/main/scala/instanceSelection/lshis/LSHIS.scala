package instanceSelection.lshis

import java.util.logging.Level
import java.util.logging.Logger

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.MutableList
import scala.util.Random

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

import instanceSelection.abstracts.TraitIS
import utils.Option

/**
 *
 * Implementación del algoritmo Locality Sensitive Hashing Instance Selection
 * (LSH IS).
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
 * @constructor Crea un nuevo algoritmo LSHIS con los parámetros por defecto.
 *
 * @author Alejandro González Rogel
 * @version 1.1.0
 */
class LSHIS extends TraitIS {

  /**
   * Archivo que contiene la frases de log.
   */
  private val bundleName = "resources.loggerStrings.stringsLSHIS";
  /**
   * Logger del algoritmo.
   */
  private val logger = Logger.getLogger(this.getClass.getName(), bundleName);

  /**
   * Número de funciones-AND a utilizar.
   */
  var ANDs: Int = 10
  /**
   * Número de funciones-OR a utilizar.
   */
  var ORs: Int = 1
  /**
   * Tamaño de los "buckets".
   */
  var width: Double = 1
  /**
   * Semilla para los números aleatorios.
   */
  var seed: Long = 1
  /**
   * Generador de números aleatorios.
   */
  private var r: Random = new Random(seed)

  override def instSelection(
    sc: SparkContext,
    parsedData: RDD[LabeledPoint]): RDD[LabeledPoint] = {

    val andTables = createANDTables(parsedData.first().features.size /* + 1 */ )

    // Variable para almacenar el resultado final
    var finalResult: RDD[LabeledPoint] = null

    for { i <- 0 until ORs } {
      val andTable = andTables(i)

      // Transformamos la RDD para generar tuplas de (bucket asignado, clase)
      // e instancia
      val keyInstRDD = parsedData.map { instancia =>
        ((andTable.hash(instancia), instancia.label), instancia)
      }

      // Seleccionamos una instancia por cada par (bucket,clase)
      val partialResult = keyInstRDD.reduceByKey { (inst1, inst2) => inst1 }

      if (i == 0) { // Si es la primera iteración del bucle for
        finalResult = partialResult.values
      } else {
        // Recalculamos los buckets para las instancias ya seleccionadas
        // en otras iteraciones
        val alreadySelectedInst = finalResult.map { instancia =>
          ((andTable.hash(instancia), instancia.label), instancia)
        }

        // Sobre la RDD de la iteración, seleccionamos aquellas las instancia
        // cuya key no esté repetida en el resultado final
        val keyClassRDDGroupBy = partialResult.subtractByKey(alreadySelectedInst)
        val selectedInstances = keyClassRDDGroupBy.map[LabeledPoint] {
          case (tupla, instancia) => instancia
        }

        // Unimos el resultado de la iteración con el resultado parcial ya
        // almacenado
        finalResult = finalResult.union(selectedInstances)
      }
    }

    finalResult
  } // end instSelection

  /**
   * Genera un array de tablas AND, cada una de ellas con funciones hash de
   * dimensión indicada por parametro.
   *
   * @param  dim  Dimensión de las funciones hash
   * @return Array con todas las tablas instanciadas
   */
  private def createANDTables(dim: Int): ArrayBuffer[ANDsTable] = {

    // Creamos tantos componentes AND como sean requeridos.
    var andTables: ArrayBuffer[ANDsTable] = new ArrayBuffer[ANDsTable]
    for { i <- 0 until ORs } {
      andTables += new ANDsTable(ANDs, dim, width, r.nextInt())
    }
    andTables
  } // end createANDTables

  override def setParameters(args: Array[String]): Unit = {

    for { i <- 0 until args.size by 2 } {
      args(i) match {
        case "-and" => ANDs = args(i + 1).toInt
        case "-w"   => width = args(i + 1).toDouble
        case "-s" => {
          seed = args(i + 1).toInt
          r = new Random(seed)
        }
        case "-or" => ORs = args(i + 1).toInt
        case somethingElse: Any =>
          logger.log(Level.SEVERE, "LSHISWrongArgsError", somethingElse.toString())
          logger.log(Level.SEVERE, "LSHISPossibleArgs")
          throw new IllegalArgumentException()
      }
    }

    // Si las variables no han sido asignadas con un valor correcto.
    if (ANDs <= 0 || ORs <= 0 || width <= 0) {
      logger.log(Level.SEVERE, "LSHISWrongArgsValuesError")
      logger.log(Level.SEVERE, "LSHISPossibleArgs")
      throw new IllegalArgumentException()
    }

  } // end readArgs

  override def listOptions: Iterable[Option] = {
    val options: MutableList[Option] = MutableList.empty[Option]
    options += new Option("ANDs", "Número de funciones-AND", "-and", ANDs, 1)
    options += new Option("ORs", "Número de funciones-OR", "-or", ORs, 1)
    options += new Option("Anchura", "Anchura de los buckets", "-w", width, 1)
    options += new Option("Semilla", "Semilla del generador de números" +
      "aleatorios", "-s", seed, 1)

    options
  } // end listOptions

}
