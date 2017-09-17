package instanceSelection

/**
 * Parallel implementation of the algorithm
 * Locality Sensitive Hashing Instance Selection(LSH IS).
 *
 * LSH-IS is an instance selection algorithm.
 * 
 * It uses a Local Sensitive Hashing (LSH) algorithm on the initial dataset
 * in order to distribute the different instances into buckets of similar instances.
 * After that, we select, from each bucket, one instance of each class. This instance
 * will then be considered part of the filtered dataset.
 *
 * Arnaiz-González, Á., Díez-Pastor, J. F., Rodríguez, J. J., & García-Osorio,
 * C. (2016). Instance selection of linear complexity for big data. Knowledge-Based
 * Systems, 107, 83-95.
 */
package object lshis
