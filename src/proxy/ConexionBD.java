package proxy;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import XML.ParseadorDeXML;
import json.QueryManager;

public class ConexionBD {

	private final int INDICE_COLUMNAS = 0;
	private final int INDICE_VALORES = 1;

	private String cadenaConexion;
	private String usuario = null;
	private String clave = null;
	private Connection db = null;
	private Cola<String> ColaTransacciones = new Cola<String>();
	private Thread tAnterior = null;

	private Statement query;
	private boolean error = false;
	private String msgError;

	/**
	 * Constructor. Se le pasa el path donde se encuentra el XML de
	 * configuracion de la BD a la que se requiera establecer la conexion.
	 * 
	 * @param configuracion
	 * @throws URISyntaxException
	 * @throws SQLException
	 * @deprecated La clase ahora recibe directamente como parametros el
	 *             servidor, el puerto, la base de datos, el usuario y la
	 *             password
	 */
	@SuppressWarnings("static-access")
	public ConexionBD(String configuracion) throws URISyntaxException,
			SQLException {

		// Creo un pareseador de archivos XML
		ParseadorDeXML p = new ParseadorDeXML();

		// Chequeo si existe el XML
		if (p.chequearSiExisteXML(configuracion)) {
			/*
			 * Parseo la configuracion , es decir , el archivo XML
			 */
			p.parsearXML(configuracion);

			/**
			 * Si la conexion esta en modo "habilitada", entonces la genero.
			 */
			if (p.obtenerValor(p.ACTIVA).equals("true")) {
				// Creo la cadena de conexion
				cadenaConexion = "jdbc:firebirdsql:"
						+ p.obtenerValor(p.SERVIDOR) + "/"
						+ p.obtenerValor(p.PUERTO) + ":"
						+ p.obtenerValor(p.BASE_DE_DATOS);
				// Obtengo el usuario con el que me conectare a la BD.
				usuario = p.obtenerValor(p.USUARIO);
				// Obtengo el password del usuario
				clave = p.obtenerValor(p.PASSWORD);

				// Intento conectarme a la BD
				Connect();
			} else
				System.out.println("Conexion desactivada");
		} else {
			System.out.println("No se encontro el archivo");
		}

	}

	public ConexionBD(String servidor, String puerto, String baseDeDatos,
			String usuario, String password) {
		// Creo la cadena de conexion
		cadenaConexion = "jdbc:firebirdsql:" + servidor + "/" + puerto + ":"
				+ baseDeDatos;
		// Obtengo el usuario con el que me conectare a la BD.
		this.usuario = usuario;
		// Obtengo el password del usuario
		clave = password;
		// Intento conectarme a la BD
		try {
			Connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Devuelve la cadena de conexion obtenida desde el XML de configuracion de
	 * la conexion a la base de datos
	 * 
	 * @return cadenaConexion
	 */
	public String getCadenaConexion() {
		return cadenaConexion;
	}

	/**
	 * Devuelve la conexion a la base de datos.
	 * 
	 * @return db : Connection
	 */
	public Connection getDb() {
		return db;
	}

	/**
	 * Logea una consulta en la cola de transacciones. Lo hace lanzando un hilo
	 * QueueWriter que encolara esa consulta en la cola de transacciones.
	 * 
	 * @param consultaSQL
	 */
	public void logear(String consultaSQL) {
		// creo un thread que recibira un QueueWriter
		System.out.println("Creando QueueWriter");
		Thread w = new Thread(new QueueWriter(tAnterior, ColaTransacciones,
				consultaSQL));
		// Lo inicio
		tAnterior = w;
		tAnterior.start();
		// Indico cual es el ultimo QueueWriter que se lanzo.

	}

	/**
	 * Devuelve verdadero o false dependiendo si la BD esta consistente o no. Se
	 * basa en preguntar si quedan transacciones en la cola por replicarse.
	 * 
	 * @return Si es consistente "true", si no lo es devolvera false
	 * @deprecated En esta version no es necesario ya que la BD se pone
	 *             consistente automaticamente a traves del proceso hilo
	 *             "ReplicatorThread"
	 */
	public boolean isConsistent() {
		if (this.ColaTransacciones.hasElements()) {
			return false;
		} else
			return true;
	}

	/**
	 * Intenta conectarse a la base de datos
	 * 
	 * @throws SQLException
	 */
	private void Connect() throws SQLException {
		// Indico que conector JDBC voy a usar para conectarme a la BD
		try {
			Class.forName("org.firebirdsql.jdbc.FBDriver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(cadenaConexion);
		// Finalmente genero la conexion a la BD
		db = DriverManager.getConnection(cadenaConexion, usuario, clave);
		// e inserto los triggers si es que aun no fueron insertados
		InsertadorDeTriggers insertador = new InsertadorDeTriggers(db);
		insertador.insertarTriggers();
	}

	/**
	 * Intenta reconectarse a la BD
	 * 
	 * @throws SQLException
	 */
	public void tryReconnect() throws SQLException {
		Connect();
	}

	public Cola<String> getColaTransacciones() {
		return this.ColaTransacciones;
	}

	public String consultar(String consultaSQL) {
		ArrayList<ArrayList<String>> arreglos = null;
		ResultSet resultado = enviarConsulta(consultaSQL);
		String respuesta;
		if (resultado != null) {
			arreglos = respuestaDeQueryAListas(resultado);
		}
		if (error) {
			respuesta = QueryManager.crearError(msgError);
		} else {
			respuesta = QueryManager
					.crearRespuesta(arreglos.get(INDICE_COLUMNAS),
							arreglos.get(INDICE_VALORES));
		}
		return respuesta;
	}

	public ResultSet enviarConsulta(String consulta) {
		ResultSet resultado = null;
		try {
			query = db.createStatement();
			System.out.println("DEBUG: Consulta: " + consulta);
			query.execute(consulta);
			resultado = query.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			error = true;
			msgError = e.getMessage();
			return resultado;
		}
		return resultado;
	}

	public ArrayList<ArrayList<String>> respuestaDeQueryAListas(ResultSet rs) {
		ArrayList<ArrayList<String>> arreglos = null;
		ArrayList<String> arregloColumnas = null;
		ArrayList<String> arregloValores = null;
		try {
			arreglos = new ArrayList<ArrayList<String>>();
			arregloColumnas = new ArrayList<String>();
			arregloValores = new ArrayList<String>();
			ResultSetMetaData metadata = rs.getMetaData();
			int numColumnas = metadata.getColumnCount();
			for (int i = 1; i <= numColumnas; i++) {
				arregloColumnas.add(metadata.getColumnName(i));
			}
			while (rs.next()) {
				int i = 1;
				while (i <= numColumnas) {
					arregloValores.add(rs.getString(i));
					i++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		arreglos.add(arregloColumnas);
		arreglos.add(arregloValores);
		return arreglos;
	}

}
