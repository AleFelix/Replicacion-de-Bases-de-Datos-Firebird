package proxy;

import java.sql.SQLException;

/**
 * Esta clase es la que se encarga de leer y replicar la transaccion que se encuentra
 * en la cabeza de la cola de transacciones pendientes a realizarse en la base de 
 * datos. Dicha base de datos se especifica al momento de instanciarse esta clase.
 * 
 * @author ANDRES
 *
 */
public class ReplicatorThread implements Runnable{
	
	private SentenceExecuter exec = null;
	private Cola<String> ColaTransacciones;
	private ConexionBD conexion;
	private final int ERRORNETWORK1 =  335544726;
	private final int ERRORNETWORK2 =  335544721;

	/**
	 * Constructor. Se pasa la cola de transacciones a realizar y la conexion a la
	 * base de datos donde debera tratar de replicar.
	 * 
	 * @param logFallos
	 * @param conexionBD
	 */
	public ReplicatorThread(ConexionBD conexionBD){
		this.ColaTransacciones = conexionBD.getColaTransacciones();
		this.conexion = conexionBD;
		this.exec = new SentenceExecuter(conexionBD.getDb());
	}
	
	
	@Override
	public void run() {
		
		System.out.println("Hilo de replicacion corriendo para "+conexion.getCadenaConexion());
		int res;
		
		/**
		 * Constantemente el hilo va a estar tratando de replicar, averiguando
		 * si la cola de transacciones tiene elementos, en caso de que tenga por lo menos un elemento
		 * toma la cabeza de la cola y trata de replicarlo. En caso de errores, trata de reconectarse a la base de datos.
		 */
		while (true){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(ColaTransacciones.hasElements()){
				System.out.println("Replicando");
				String consultaSQL = null;
				//synchronized(ColaTransacciones.getClass()){
					 consultaSQL = ColaTransacciones.obtenerCabeza();
				//}
				res = exec.execute(consultaSQL );
				
	        	//Si hay errores al tratar de replicar
				if (res == this.ERRORNETWORK1 || res == this.ERRORNETWORK2){
	        		//Codigo para recuperar la conexion
					recuperarConexion();
					System.out.println("Recuperando conexion");
	        	}else{
	        		//Si no hay errores, quito la cabeza de la cola de transacciones
	        		ColaTransacciones.desencolar();
	        		System.out.println("Replicacion hecha");
	        	}//FIN if
				
			}//FIN if
				
		}//FIN while
		
	}//FIN run
	
	
	/**
	 * Este metodo tiene la funcionalidad de reiniciar la conexion a la BD en caso de 
	 * que se encuentre caida.
	 */
	private void recuperarConexion(){
		
		boolean conexionRecuperada = false;
		
		/*
		 * Mientras la conexion no se logre recuperar
		 */
		while (!conexionRecuperada){
			/*
			 * Duermo 1 segundo
			 */
			try {
				Thread.sleep(1000);
				System.out.println("Hilo durmiendo zzz");
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			System.out.println("Hilo despierto O.O");
			
			conexionRecuperada = true;
			
			/*
			 * Ahora intento recuperar la conexion ejecutando el metodo
			 * tryReconnect de la clase ConexionBD.
			 */
			try {
				conexion.tryReconnect();
				System.out.println("Try reconect hecho");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("Sql state: "+ e.getSQLState());
				System.out.println("Mensaje "+ e.getMessage());
				if (e.getErrorCode() == this.ERRORNETWORK2) {
					conexionRecuperada = false;
					System.out.println("La conexion sigue caida");
				}else System.out.println("Conexion recuperada!!!");
				
			}
		}
		
	}
}
