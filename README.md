# Parallel instance selection algorithms using Spark framework

## Description

This repository offers a variety of **parallel** data mining techniques using the Apache Spark™ and its RDD structures.

Although at the time of its creation the mayor concern of the library was the implementation of instance selection algorithms, the structure of the library still allows for the implementation and use of any other data mining tasks. 

In the current version, the most remarkable content this repository gives access to are the instance selection algorithms *Locality Sensitive Hashing Instance Selection* (LSHIS) and *Democratic Instance Selection* (DemoIS) and the classifier *k*nn.

*The first version of this work was presented in February 2016 as a Computer Science bachelor's thesis at the University of Burgos (http://www.ubu.es/english-version). The old repository can still be found here: https://bitbucket.org/agr00095/tfg-alg.-seleccion-instancias-spark

## DISTRIBUCIONES

* **ISAlgorithms_2_10**: Distribución general con todos los algoritmos de la librería e interfaz gráfica.

    * Versión Scala: 2.10

    * Versión Java: 1.7

    * Versión Spark: 1.6.1

* **ISAlgorithms_2_11**: Distribución general con todos los algoritmos de la librería e interfaz gráfica.

    * Versión Scala: 2.11

    * Versión Java: 1.8

    * Versión Spark: 1.6.1

* **ISAlgorithms_gui**: Distribución con únicamente la interfaz gráfica.

    * Versión Scala: 2.11

    * Versión Java: 1.8

![full_interface.png](https://bitbucket.org/repo/B6d96X/images/463284299-full_interface.png)


* **ISAlgorithms_cluster**: Distribución con únicamente la librería de algoritmos de minería.

    * Versión Scala: 2.10

    * Versión Java: 1.7

    * Versión Spark: 1.6.1

## USO

Actualmente, el programa únicamente permite lanzar experimentos que consten de un algoritmo de selección de instancias y un algoritmo de clasificación que se ejecute posterior al filtrado.

A continuación se presenta un ejemplo de uso mediante consola de comandos:


```

$SPARK_HOME/bin/spark-submit --master "URL" ["otros_argumentos_Spark"] \
--class "launcher.ExperimentLauncher" "ruta_jar" ISClassExec \
-r "ruta_dataset" ["otros_arguentos_lector"] -f "ruta_algoritmo_selector"\
 "argumentos_algoritmo" -c "ruta_algoritmo_classificación" \
"argumentos_algoritmo" [-cv "argumentos_validación_cruzada"]

```

* *$SPARK_HOME/bin/spark-submit*: Ruta al script spark-submit

* *"URL"*: Ruta a la máquina maestra o local[n] en caso de ejecución local, siendo 'n' el número de hilos a utilizar.

* *"otros_argumentos_Spark"*: Otros argumentos de configuración para Spark.

* *--class launcher.ExperimentLauncher "ruta_jar"*: Archivo jar y clase a utilizar.

* *ISClassExec*: Tipo de experimento.

* *-r "ruta_dataset" ["otros_arguentos_lector"] -f "ruta_algoritmo_selector" "argumentos_algoritmo" -c "ruta_algoritmo_classificación" "argumentos_algoritmo" [-cv "argumentos_validación_cruzada"]*: Argumentos del programa.

## Publicaciones
* [ERCIM] (http://ercim-news.ercim.eu/en108/r-i/mr-dis-a-scalable-instance-selection-algorithm-using-mapreduce-on-spark) 
* [PRAI]  (http://link.springer.com/article/10.1007/s13748-017-0117-5)


## Autor
* Alejandro González Rogel

## Tutores
* Álvar Arnaiz-González

* Carlos López-Nozal

## Cite
When citing this implementation, please use:

* BibTeX:
```
@Article{Arnaiz-Gonzalez2017,
   author="Arnaiz-Gonz{\'a}lez, {\'A}lvar and Gonz{\'a}lez-Rogel, Alejandro and 
           D{\'i}ez Pastor, Jos{\'e}-Francisco and L{\'o}pez-Nozal, Carlos",
   title="MR-DIS: democratic instance selection for big data by MapReduce",
   journal="Progress in Artificial Intelligence",
   year="2017",
   pages="1--9",
   issn="2192-6360",
   doi="10.1007/s13748-017-0117-5"
}
```
