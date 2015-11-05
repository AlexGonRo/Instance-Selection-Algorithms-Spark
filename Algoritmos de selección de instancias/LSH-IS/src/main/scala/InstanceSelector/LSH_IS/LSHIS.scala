package InstanceSelector.LSH_IS

import scala.io.StdIn
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

/**
 * @author Alejandro
 *
 */
object App {

  //TODO Queda pendiente asignar a cada instancia un número para 
  //saber cual es cual y no tener que arrastrarla todo el proceso.
  def main(args: Array[String]) {
    //La configuración la pasamos a la hora de invocar por consola la aplicación.
    //Utilizar estas lineas solo para pruebas directamente en Eclipse
    //val conf = new SparkConf().setAppName("Lanzador").setMaster("local[2]")
    //val sc = new SparkContext(conf)

    val sc = new SparkContext(new SparkConf())

    //Creamos una tabla hash con los diferentes vectores.
    
    //TODO Los argumentos tienen que ser pasados por parámetro
    val tablaHash = new HashTable(10, 2, 1)
    
    
    //Leemos del fichero que contiene el conjunto de datos y 
    //almacenamos sus datos.
    val data = sc.textFile(args(0))

    //Creamos una RDD que contenga los datos en el formato que queremos, un labeled point con clase y vector con los atributos
    //TODO Funciona solo si la clase es el último de los atributos
    val parsedData = 
        //Si el atributo de clase es el último
        data.map { line =>
          val parts = line.split(',')
          LabeledPoint(parts.last.toDouble, Vectors.dense(parts.dropRight(1).map(_.toDouble)))
        }

    //Creamos una RDD con tuplas de (bucket asignado,clase) e instancia
    val ParBucketInstancia = parsedData.map{instancia =>
      ((tablaHash.hash(instancia.features),instancia.label),instancia)
    }
    
    
    //Agrupamos cada par (bucket, clase) y seleccionamos una instancia de cada grupo
    val resultado = ParBucketInstancia.groupByKey.take(1)
 
    
    
    
    
  }

}
