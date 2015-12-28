package utils

import java.lang.reflect.Constructor
import org.reflections.Reflections
import instanceSelection.abstracts.AbstractIS

/**
 * Encuentra e instancia el algoritmo de selección de instancias especificado.
 *
 * @version 1.0
 * @author Alejandro González Rogel
 */
class ISSelector {

  /**
   *
   * Dado el nombre de la clase que contiene el IS, localiza e instancia un objeto
   * de dicha clase si lo hubiese.
   *
   *
   * @param args  Argumentos necesarios para instanciar el algoritmo
   * @param algName  Nombre del algoritmo a instanciar
   * @return Instancia del algoritmo de selección de instancias
   * @throws ClassNotFoundException Si no hemos podido encontrar una clase
   *   que coincida con el parametro de entrada.
   */
  def instanceAlgorithm(algName: String): AbstractIS = {

    var reflections = new Reflections("instanceSelection");
    var subClasses = reflections.getSubTypesOf(classOf[AbstractIS]);

    var it = subClasses.iterator()

    // Por cada clase encontrada que herede de AbstractIS.
    while (it.hasNext()) {
      var className = it.next()
      if (className.getName.endsWith(algName)) {
        // Buscamos el constructor que ha tenido que heredar de AbstractIS
        var constructor: Constructor[_] = null
        for (const <- className.getConstructors()) {
          if (const.getParameterCount == 0)
            constructor = const
        }
        // Instanciamos y devolvemos la clase.
        val selector = constructor.newInstance().asInstanceOf[AbstractIS]
        return selector
      }
    }

    // Si no se ha conseguido encontrar la clase requerida
    throw new ClassNotFoundException("Couldn't find a class named " + algName +
        ".Are you sure this class is included in the class path?")

  } // end instanceAlgorithm

}
