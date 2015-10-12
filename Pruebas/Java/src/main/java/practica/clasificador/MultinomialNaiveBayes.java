package practica.clasificador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.stat.MultivariateStatisticalSummary;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.mllib.util.MLUtils;
import org.apache.spark.rdd.RDD;

import scala.Tuple2;

/**
 * Clasificador bayesiano multinomial utilizado para la comprensión y práctica
 * de Spark y sus funciones.
 * 
 * No se encuentra acabado.
 * 
 * Tanto los datos de entrenamiento como los de clasificación han de encontrarse
 * en formato LibSVM. La única diferencia entre los datos de entrenamiento y de
 * clasificación será que estos últimos no cuentan con la clase a la que
 * pertenece la entrada a analizar.
 * 
 * @author Alejandro González Rogel
 * @version 0.1
 */

public class MultinomialNaiveBayes {

	/**
	 * Número de clases que tiene el conjunto de datos a analizar.
	 */
	private int numClases = 0;

	/**
	 * Array list que almacena vectores con la media y la varianza de los
	 * diferentes atributos del conjunto de entrenamiento.
	 * 
	 * La media y la varianza se calculan por cada clase existente en el
	 * conjunto de entrenamiento, lo que indica que habrá una clase Vector para
	 * cada media de cada clase y una clase Vector para cada varianza de cada
	 * clase.
	 * 
	 * Los Vectores 0,2,4,... contendrán la media de los atributos de la cada
	 * clase, y los vectores 1,3,5,etc la varianza.
	 * 
	 */
	private ArrayList<Vector> stats;

	/**
	 * Constructor.
	 */
	public MultinomialNaiveBayes() {
		numClases = 0;
		stats = new ArrayList<Vector>();
	}

	/**
	 * Entrena el clasificador, analizando los datos de entrenamiento y
	 * almacenando/generando los datos que posteriormente utilizaremos para la
	 * clasificación: el número de clases y la media y varianza de los
	 * diferentes atributos.
	 * 
	 * @param trainData
	 *            Ruta para el archivo que contiene los datos de entrenamiento
	 * @param jsc
	 *            Contexto Spark en el que se ejecutará la aplicación.
	 * @param clasificador
	 *            Clasificador a entrenar
	 */
	private static void train(String trainData, JavaSparkContext jsc, MultinomialNaiveBayes clasificador) {

		// Lectura y cambio de formato de los datos de entrada.
		RDD<LabeledPoint> training = MLUtils.loadLibSVMFile(JavaSparkContext.toSparkContext(jsc), trainData);
		// Necesario hacerlo porque MLUtils arriba no funciona con JavaRDD
		JavaRDD<LabeledPoint> trainingJavaRDD = training.toJavaRDD();

		// Calculamos el número de calses que contiene el conjunto de
		// entrenamiento
		setUp(trainingJavaRDD, jsc, clasificador);

		// Generamos una nueva RDD que almacene los datos, esta vez agrupados
		// por grupo
		JavaPairRDD<Double, Vector> groups = trainingJavaRDD
				.mapToPair(lp -> new Tuple2<Double, Vector>(lp.label(), lp.features()));
		List<Double> numgrupos = (ArrayList<Double>) groups.keys().collect();

		// Adquirimos la media y varianza de los atributos según su clase.
		for (int i = 0; i < numgrupos.size(); i++) {
			// TODO
			// Es posible que exista una mejor manera de obtener este resultado
			// sin la necesidad de crear nuevas RDD para cada grupo.
			MultivariateStatisticalSummary summary = Statistics
					.colStats(jsc.parallelize(groups.lookup(numgrupos.get(i))).rdd());
			clasificador.stats.add(summary.mean());
			clasificador.stats.add(summary.variance());
		}

	}

	/**
	 * Almacena el número de clases diferentes que contiene el conjunto de datos
	 * de entrenamiento.
	 * 
	 * @param trainingJavaRDD
	 *            Conjunto de entrenamiento.
	 * @param jsc
	 *            Contexto Spark en el que se ejecuta el clasificador.
	 * @param clasificador
	 *            Clasificador en estado de entrenamiento.
	 */
	private static void setUp(JavaRDD<LabeledPoint> trainingJavaRDD, JavaSparkContext jsc,
			MultinomialNaiveBayes clasificador) {

		JavaRDD<Double> labels = trainingJavaRDD.flatMap(lp -> Arrays.asList(lp.label()));
		ArrayList<Double> difClases = (ArrayList<Double>) labels.distinct().collect();
		clasificador.numClases = difClases.size();

	}

	private static void classify(String testData, JavaSparkContext jsc, MultinomialNaiveBayes clasificador) {

		ArrayList<ArrayList<Double>> test = readFile(testData);
		// TODO Sin implemetación.
		throw new UnsupportedOperationException("This classifier hasn't been completely implemented.");

	}

	/**
	 * Clase que permite la lectura de un fichero en formato LibSVM al que le
	 * falta la clase de cada entrada.
	 * 
	 * Se supone que, pese al formato del fichero, todos los atributos del
	 * fichero son no nulos.
	 * 
	 * @param testData
	 *            Ruta al archivo de test
	 * @return Entradas existentes en el archivo de test.
	 */
	private static ArrayList<ArrayList<Double>> readFile(String testData) {

		ArrayList<ArrayList<Double>> test = new ArrayList<ArrayList<Double>>();
		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;

		try {

			archivo = new File(testData);
			fr = new FileReader(archivo);
			br = new BufferedReader(fr);

			// Lectura del fichero
			String linea;
			while ((linea = br.readLine()) != null) {

				ArrayList<Double> features = new ArrayList<Double>();
				String[] featuresTemp = linea.split(" \\d:");
				featuresTemp[0] = featuresTemp[0].split("\\d:")[1];
				for (String valor : featuresTemp) {
					features.add(Double.parseDouble(valor));
				}
				test.add(features);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// En el finally cerramos el fichero
			try {
				if (null != fr) {
					fr.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return test;
	}

	/**
	 * 
	 * @return Número de clases del conjunto de entrenamiento.
	 */
	public int getNumClases() {
		return numClases;
	}

	/**
	 * Actualiza el número de clases del conjunto de entrenamiento
	 * 
	 * @param numClases
	 */
	public void setNumClases(int numClases) {
		this.numClases = numClases;
	}

	/**
	 * Devuelve un array que contiene Vectores con la media y la desviación
	 * típica de cada parámetro según la clase a la que pertenezca una entrada.
	 * 
	 * @return Datos de media y desviación típica de los atributos en función de
	 *         la clase.
	 */
	public ArrayList<Vector> getStats() {
		return stats;
	}

	/**
	 * Permite añadir un array que contiene Vectores con la media y la
	 * desviación típica de cada parámetro según la clase a la que pertenezca
	 * una entrada.
	 * 
	 * @param stats
	 */
	public void setStats(ArrayList<Vector> stats) {
		this.stats = stats;
	}

	/**
	 * Main class
	 * 
	 * @param args
	 *            Ruta del archivo que guarda el conjunto de entrenamiento Ruta
	 *            del archivo que guarda el conjunto de test
	 */
	public static void main(String[] args) {
		// Necesitamos crear el contexto Spark para la aplicación.
		JavaSparkContext jsc = new JavaSparkContext("local", "MultinomialNaiveBayes");
		MultinomialNaiveBayes clasificador = new MultinomialNaiveBayes();

		train(args[0], jsc, clasificador);
		classify(args[1], jsc, clasificador);

		jsc.stop();

	}

}
