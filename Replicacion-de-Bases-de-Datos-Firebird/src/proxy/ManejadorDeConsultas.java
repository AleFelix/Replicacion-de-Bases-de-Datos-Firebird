package proxy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

import json.QueryManager;

public class ManejadorDeConsultas implements Runnable {

	private Socket socketCliente;

	public ManejadorDeConsultas(Socket socketCliente) {
		this.socketCliente = socketCliente;
	}

	@Override
	public void run() {
		try {
			DataInputStream ois = new DataInputStream(
					socketCliente.getInputStream());
			System.out.println("DEBUG: A punto de leer la consulta!");
			String consultaJSON = ois.readUTF();
			System.out.println("DEBUG: Consulta leida!");
			List<String> listaDeConsulta = QueryManager
					.decodificarQuery(consultaJSON);
			System.out.println("DEBUG: Consulta decodificada!");
			String respuesta = null;
			if (listaDeConsulta.get(QueryManager.INDICE_TIPO).equals(QueryManager.TIPO_CONSULTA)) {
				respuesta = ProxfirMain
						.ejecutarConsulta(listaDeConsulta.get(QueryManager.INDICE_QUERY));
				DataOutputStream oos = new DataOutputStream(
						socketCliente.getOutputStream());
				oos.writeUTF(respuesta);
				oos.close();
			} else {
				ProxfirMain.ejecutarSentencia(listaDeConsulta.get(QueryManager.INDICE_QUERY));
			}
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
