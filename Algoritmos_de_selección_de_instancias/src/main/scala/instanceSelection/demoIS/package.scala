package instanceSelection

/**
 * Parallel implementation of the Democratic Instance Selection algorithm.
 *
 * Democratic Instance Selection is an instance selection algorithm that 
 * applies, for several rounds and over disjoint subsets of the original dataset,
 * simplier instance selection algorithms. Once this phase is over, is uses the obtained
 * results to decide which instances should be eliminated.
 *
 * García-Osorio, César, Aida de Haro-García, and Nicolás García-Pedrajas.
 * "Democratic instance selection: a linear complexity instance selection
 * algorithm based on classifier ensemble concepts." Artificial Intelligence
 * 174.5 (2010): 410-441.
 */
package object demoIS

