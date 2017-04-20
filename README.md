# Parallel instance selection algorithms using Spark framework

## Description

This repository offers a variety of **parallel** data mining techniques using the Apache Spark™ and its RDD structures.

This library offers the possibility to easily setup a data mining pipeline and run it in a parallel enviroment. We recommend the use of the console user interface when launching a task. However, in an effort to make the use of our work easier, we also offer a basic GUI that offers the basic functionalities.

![full_interface.png](https://bitbucket.org/repo/B6d96X/images/463284299-full_interface.png)

Although at the time of its creation the mayor concern of the library was the implementation of instance selection algorithms, the structure of the library still allows for the implementation and use of any other data mining tasks.

Rigth now, the most remarkable content this repository gives access to are the instance selection algorithms *Locality Sensitive Hashing Instance Selection* (LSHIS) and *Democratic Instance Selection* (DemoIS) and the classifier *k*nn.

*The first version of this work was presented in February 2016 as a Computer Science bachelor's thesis at [University of Burgos](http://www.ubu.es/english-version)*. The old repository can still be found here: https://bitbucket.org/agr00095/tfg-alg.-seleccion-instancias-spark

## Before running

Please, check that your system fulfill all the following requirements. We do not guarantee that this library will work under a different configuation.

 * Scala version: 2.11

 * Java version: 1.8

 * Spark version: 1.6.1

 * Maven version (if you need to build the library yourself): 3.3.3


## Releases

You can find the newest version of the library (already built) in our [Releases](https://github.com/AlexGonRo/Instance-Selection-Algorithms-Spark/releases) section.

Two different files are provided:

 * **ISAlgorithms:** Contains all the algorithms but not the graphical interface.
 
 * **ISAlgorithms_gui:** Contains all the algorithms and the graphical interface.

## Build it yourself

If you need an older version of this program or want to build it using a different configuration, we provide the possibility to do so.

Open a command window and locate the root directory. Modify the POM XML file if needed and execute the following line:

```
$ mvn clean package
```

## Execution

Right now, the program only allows for the execution of the following types of pipelines:

* Instance selection algorithm + classifier
* Classifier alone


You can have a look to the squeleton of the execution command:  

```
$SPARK_HOME/bin/spark-submit --master "URL" ["OTHER_SPARK_ARGS"] \
--class "launcher.ExperimentLauncher" "PATH_JAR" ISClassExec \
-r "PATH_DATASET" ["OTHER_LOADING_ARGS"] -f "PATH_INSTANCE_SELECTION_ALG"\
 "ALGORITHM_ARGS" -c "PATH_CLASSIFIER" \
"CLASSIFIER_ARGS" [-cv "CV_ARGS"]

```
In the command above:

* *$SPARK_HOME/bin/spark-submit*: Path to the *spark-submit* script

* *"URL"*: Path to master or *local[n]* if it is a local execution (where *n* refers to the number of threads).

* *"OTHER_SPARK_ARGS"*: Spark arguments.

* *--class launcher.ExperimentLauncher "PATH_JAR"*: Path to our JAR library.

* *ISClassExec*: Pipeline type

* *-r "PATH_DATASET" ["OTHER_LOADING_ARGS"] -f "PATH_INSTANCE_SELECTION_ALG" "ALGORITHM_ARGS" -c "PATH_CLASSIFIER" "CLASSIFIER_ARGS" [-cv "CV_ARGS"]*: Job arguments.

## Example

```

```

## Additional notes


## Articles

* [ERCIM] (http://ercim-news.ercim.eu/en108/r-i/mr-dis-a-scalable-instance-selection-algorithm-using-mapreduce-on-spark) 
* [PRAI]  (http://link.springer.com/article/10.1007/s13748-017-0117-5)

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
