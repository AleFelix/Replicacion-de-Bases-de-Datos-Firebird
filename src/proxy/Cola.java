package proxy;

import java.util.*;
/**
 * Clase generica que sirve para encolar objetos de tipo T
 * 
 * @author A.Giordano 
 * @version 1.0
 */
public class Cola<T>
{
    // instance variables - replace the example below with your own
    private ArrayList<T> cola = null;
    private int cabeza = -1;
    
    /**
     * Contructor para la cola
     */
 
    public Cola()
    {
        cola = new ArrayList<T>();
    }

    /**
     * Encola un elemento
     * @param x
     */
    public void encolar(T x) {
        cola.add(0, x);
        cabeza++;
    }
    /**
     * Saca la cabeza de la cola, pero no la borra
     * 
     * @param  y   a sample parameter for a method
     * @return     the sum of x and y 
     */
    public T obtenerCabeza() {
        return cola.get(cabeza);
    }
    
    /**
     * Borra la cabeza de la cola
     */
    public void desencolar(){
    	if (cabeza > -1){
    		cola.remove(cabeza);
    		cabeza--;
    	}
    }
    
    public boolean hasElements(){
    	if (cabeza > -1) {
    		return true;
    	}else return false;
    }
}

