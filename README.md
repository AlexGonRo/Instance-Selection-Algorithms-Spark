# Paralelización de algoritmos de selección de instancias con la arquitectura Spark

##Descripción

Este proyecto software ofrece una herramienta capaz de aplicar técnicas de minería de datos **de manera paralela** apoyándose en Apache Spark™ y sus estructuras RDD. 

El área concreta en la que está centrado este trabajo es la implementación de algoritmos de selección de instancias. Sin embargo, su estructua no impide la creación y utilización de cualquier otro tipo de técnicas de minería, como algoritmos de clasificación (implementado el *K*NN secuencial).

Actualmente, su contenido más destacable es la existencia de dos algoritmos de selección de instancias: *Locality Sensitive Hashing Instance Selection* (LSHIS) y *Democratic Instance Selection* (DemoIS).

**
Este trabajo ha sido presentado como proyecto de fin de grado del Grado en Ingeniería Informática por la Universidad de Burgos en la convocatoria de Febrero 2016.** Cualquier modificación realizada posteriormente no corresponde al trabajo realizado durante el curso, sino a la evolución del proyecto fuera del ámbito de evaluación.

##DISTRIBUCIONES

* **ISAlgorithms**: Distribución general con todos los algoritmos de la librería e interfaz gráfica.

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

##USO

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

##Publicaciones
* [ERCIM] (http://ercim-news.ercim.eu/en108/r-i/mr-dis-a-scalable-instance-selection-algorithm-using-mapreduce-on-spark) 


##Autor
* Alejandro González Rogel

##Tutores
* Álvar Arnaiz González

* Carlos López Nozal
