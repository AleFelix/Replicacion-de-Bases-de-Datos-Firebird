package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import XML.ParseadorDeXML;

/**
 * 
 * @author ANDRES
 * @version 1.2
 */

public class ProxfirMain {

	private static String TAG_BASE = "conexion";
	private static int INDEX_ESTADO = 0;
	private static String ACTIVA = "true";
	private static int INDEX_SERVIDOR = 1;
	private static int INDEX_PUERTO = 2;
	private static int INDEX_BASE_DE_DATOS = 3;
	private static int INDEX_USUARIO = 4;
	private static int INDEX_PASSWORD = 5;

	static ArrayList<ConexionBD> Conexiones = new ArrayList<ConexionBD>();
	static ArrayList<Thread> replicadores = new ArrayList<Thread>();
	
	static int contadorDeConexiones = 0;

	public static void main(String[] arg) throws URISyntaxException {

		/**
		 * Genero las conexiones a las BDs. Si puede generar una conexion,
		 * tambien lanza su thread de replicacion
		 */
		System.out.println("Generando conexiones");

		genConexiones("ConexionesFirebird.xml");

		/*
		 * genConexion("ConexionFirebird1.xml");
		 * genConexion("ConexionFirebird2.xml");
		 * genConexion("ConexionFirebird3.xml");
		 */

		genReplicadores();
		
		iniciarListener();

		/*
		 * // LINEA DE COMANDOS-------------------------------
		 * 
		 * Scanner sc = new Scanner(System.in);
		 * 
		 * System.out .println(
		 * "INSERTE LAS LINEAS DML REQUERIDAS . CON 'exit' FINALIZA, cualquier\notro comando sera enviado a la BD!!!"
		 * ); System.out.print("replicatorUser@middleware-proc # ");
		 * 
		 * String consultaSQL = sc.nextLine(); System.out.println();
		 * 
		 * while (!consultaSQL.equals("exit")) {
		 * 
		 * /** Si se ingreso "prueba" inserta 200 tuplas en una tabla para hacer
		 * pruebas.
		 * 
		 * if (consultaSQL.equals("prueba")) { java.util.Date date = new
		 * java.util.Date(); System.out.println(new Timestamp(date.getTime()));
		 * for (int i = 0; i < 200; i++) {
		 * ejecutarSentencia("insert into tabla3(nro,descr3) values(" + i +
		 * ",'descr" + i + "');"); } System.out.println(new
		 * Timestamp(date.getTime())); } else ejecutarSentencia(consultaSQL);
		 * 
		 * System.out.println();
		 * System.out.print("replicatorUser@middleware-proc # "); consultaSQL =
		 * sc.nextLine(); }
		 * 
		 * // --------------------------------LINEA DE COMANDOS
		 */
	}

	private static void iniciarListener() {
		Thread hilo = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					@SuppressWarnings("resource")
					ServerSocket socketServidor = new ServerSocket(1111);
					while (true) {
						Socket socketCliente = socketServidor.accept();
						System.out.println("DEBUG: Cliente encontrado!");
						Runnable manejador = new ManejadorDeConsultas(socketCliente);
						Thread hilo = new Thread(manejador);
						hilo.start();
						System.out.println("DEBUG: Hilo de cliente iniciado!");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		hilo.start();
	}

	private static void genConexiones(String archiConf) throws URISyntaxException {
		System.out.println("Generando conexiones del " + archiConf);
		ParseadorDeXML parser = new ParseadorDeXML();
		if (parser.chequearSiExisteXML(archiConf)) {
			parser.parsearXML(archiConf);
			int index = 1;
			String tagBase = TAG_BASE;
			List<String> lista = null;
			do {
				lista = parser.obtenerValores(tagBase + index);
				if (lista != null) {
					System.out.println("DEBUG: Estado: "+ lista.get(INDEX_ESTADO));
					if (lista.get(INDEX_ESTADO).equals(ACTIVA)) {
						ConexionBD conexionAux = new ConexionBD(
								lista.get(INDEX_SERVIDOR),
								lista.get(INDEX_PUERTO),
								lista.get(INDEX_BASE_DE_DATOS),
								lista.get(INDEX_USUARIO),
								lista.get(INDEX_PASSWORD));
						Conexiones.add(conexionAux);
					} else {
						System.out.println("Conexion desactivada");
					}
				}
				index++;
			} while (lista != null);
		} else {
			System.out.println("No se encontro el archivo");
		}
	}

	/**
	 * Ejecuta una sentencia SQL para una conexion a una base de datos
	 * especifica. Lo que hace en realidad es pedirle a la BD que loguee la
	 * consulta
	 * 
	 * @param bd
	 * @param consultaSQL
	 */
	static void ejecutarSentencia(String consultaSQL) {
		for (ConexionBD c : Conexiones) {
			System.out.println("Logeando consulta");
			c.logear(consultaSQL);
		}
	}
	
	/**
	 * Ejecuta una consulta SQL para una conexion a una base de datos
	 * especifica y devuelve el resultado.
	 * 
	 * @param consultaSQL
	 */
	static String ejecutarConsulta(String consultaSQL) {
		String respuesta = Conexiones.get(contadorDeConexiones).consultar(consultaSQL);
		contadorDeConexiones++;
		if (contadorDeConexiones >= Conexiones.size())
			contadorDeConexiones = 0;
		return respuesta;
	}

	/**
	 * Crea una nueva clase de conexion a una BD , pasandole como parametro el
	 * archivo de configuracion de conexion a dicha clase.
	 * 
	 * @param archiConf
	 * @deprecated Ya no es necesario generar conexiones de manera individual,
	 *             el XML las tiene todas juntas
	 */
	private static void genConexion(String archiConf) {

		System.out.println("Generando conexion " + archiConf);

		try {
			ConexionBD nuevaC = new ConexionBD(archiConf);
			if (nuevaC.getDb() != null)
				Conexiones.add(nuevaC);
			System.out.println("Conexion " + archiConf + " generada");
		} catch (SQLException e) {
			System.out.println("Error en conexion 1");
			System.out.println(e.getErrorCode());
			System.out.println(e.getSQLState());
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}

	private static void genReplicadores() {
		for (ConexionBD c : Conexiones) {
			replicadores.add(new Thread(new ReplicatorThread(c)));
		}

		for (Thread t : replicadores) {
			t.start();
		}
	}
}
