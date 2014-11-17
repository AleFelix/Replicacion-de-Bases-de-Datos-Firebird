package cliente;

import java.util.List;

public class ControladorCliente {
	
	public static final String ARCHIVO_CONEXION = "ConexionCliente.xml";
	
	VistaVentanaCliente v;
	ModeloCliente m;

	public ControladorCliente() {
		v = new VistaVentanaCliente(this);
		m = new ModeloCliente(this);
	}

	public void enviarDatos(String tipo, String query) {
		m.enviarConsulta(tipo, query);
	}

	public void pasarRespuestaALaVista(List<String> respuesta) {
		v.mostrarRespuesta(respuesta);
	}

	public void avisarCierreDeSocket() {
		m.cerrarSocket();
	}
}
