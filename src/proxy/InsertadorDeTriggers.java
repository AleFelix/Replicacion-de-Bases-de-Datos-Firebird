package proxy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class InsertadorDeTriggers {
	private String sql;
	private Statement sentencia;
	private Connection conexion;

	public InsertadorDeTriggers(Connection conexion) throws SQLException {
		this.conexion = conexion;
	}

	public void insertarExcepcion() throws SQLException {
		String consultaSQL = new String(
				"select count(*) FROM RDB$EXCEPTIONS a WHERE RDB$EXCEPTION_NAME = 'EX_ERRORACTUALIZACION'");
		System.out
				.println("DEBUG: A punto de ejecutar consulta de excepciones");
		Statement s = conexion.createStatement();
		ResultSet resultados = s.executeQuery(consultaSQL);
		resultados.next();
		if (resultados.getInt(1) == 0) {
			String insercionSQL = "CREATE EXCEPTION ex_erroractualizacion 'No puede actualizar si no es a traves del usuario sysdba'";
			System.out.println("DEBUG: A punto de insertar excepcion");
			Statement s2 = conexion.createStatement();
			s2.execute(insercionSQL);
		}
	}

	public void insertarTriggers() throws SQLException {
		insertarExcepcion();
		String consultaSQL = new String(
				"select a.RDB$RELATION_NAME FROM RDB$RELATIONS a WHERE RDB$SYSTEM_FLAG=0 AND RDB$RELATION_TYPE=0");
		System.out.println("DEBUG: A punto de ejecutar consulta triggers");
		sentencia = conexion.createStatement();
		ResultSet resultados = sentencia.executeQuery(consultaSQL);
		ArrayList<String> tablas = new ArrayList<>();
		while (resultados.next()) {
			System.out.println("DEBUG: Añadiendo un trigger para enviar");
			tablas.add(resultados.getString("RDB$RELATION_NAME"));
		}
		for (int i = 0; i < tablas.size(); i++) {
			if (!verificarSiExisteTrigger("TRG_CHANGES"
					+ tablas.get(i))) {
				sentencia = conexion.createStatement();
				sql = new String(
						"create trigger trg_changes"
								+ tablas.get(i)
								+ " for "
								+ tablas.get(i)
								+ " active before insert or update or delete position 0 "
								+ " as begin "
								+ " if (current_user <> 'SYSDBA') then exception ex_erroractualizacion ' no puede actualizar si no es a traves del usuario sysdba'; "
								+ " end ");
				System.out.println("DEBUG: A punto de enviar un trigger");
				sentencia.execute(sql);
				System.out.println("DEBUG: Un trigger enviado");
			}
		}
	}

	public boolean verificarSiExisteTrigger(String nombreTrigger)
			throws SQLException {
		boolean existe = false;
		nombreTrigger = nombreTrigger.split(" ")[0];
		String consultaSQL = "select count(*) FROM RDB$TRIGGERS WHERE RDB$TRIGGER_NAME = '"
				+ nombreTrigger + "'";
		System.out.println("DEBUG: A punto de consultar si existe el trigger " + nombreTrigger);
		System.out.println(consultaSQL);
		Statement consulta = conexion.createStatement();
		ResultSet resultados = consulta.executeQuery(consultaSQL);
		resultados.next();
		System.out.println(resultados.getInt(1));
		if (resultados.getInt(1) != 0) {
			existe = true;
		}
		return existe;
	}
}
