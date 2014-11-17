package proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ejecuta sentencias SQL 
 * 
 * @author ANDRES
 */

public class SentenceExecuter {
	
	private Connection conexion = null;
	
	
	public SentenceExecuter(Connection conexion){
		this.conexion = conexion;
	}
	
	/**
	 * Para la conexion a una BD que se le envie crea una consulta y la
	 * ejecuta en la BD. La consulta SQL se pasa como parametro en "consultaSQL"
	 * 
	 * @param conexion
	 * @param consultaSQL
	 * @throws SQLException 
	 */
	
	public int execute(String consultaSQL){
        int res = 0;
        //PreparedStatement pe = null;
			if (conexion != null){    
				
			    System.out.println("Generando sentencia SQL");
		    	System.out.println("Voy a consultar esto: " + consultaSQL);
			    System.out.println("Replicando");
			    try {
			    	//pe = conexion.prepareStatement(consultaSQL);
					//pe.executeUpdate();
			    	Statement sentencia = conexion.createStatement();
			    	sentencia.execute(consultaSQL);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
					System.out.println(e.getMessage());
					res = e.getErrorCode();
				}
			    
			}
		return res;
	}
}
