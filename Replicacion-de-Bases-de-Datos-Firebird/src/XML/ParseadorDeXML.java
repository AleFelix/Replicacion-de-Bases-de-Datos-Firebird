package XML;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseadorDeXML {
	public static final String ACTIVA = "activa";
	public static final String SERVIDOR = "servidor";
	public static final String PUERTO = "puerto";
	public static final String BASE_DE_DATOS = "base_de_datos";
	public static final String USUARIO = "usuario";
	public static final String PASSWORD = "password";
	public static final String CARPETA = "/XML";

	private Document documento;
	
	public ParseadorDeXML() {
		new File(CARPETA).mkdirs();
	}
	
	public boolean chequearSiExisteXML(String archivo) throws URISyntaxException {
		return new File(getClass().getResource(CARPETA+"/"+archivo).toURI()).isFile();
	}
	
	

	public void parsearXML(String archivoXML) throws URISyntaxException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			documento = db.parse(new File(getClass().getResource(CARPETA+"/"+archivoXML).toURI()));
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public String obtenerValor(String etiqueta) {
		Element elemento = documento.getDocumentElement();
		NodeList list = elemento.getElementsByTagName(etiqueta);
		return list.item(0).getTextContent();
	}
	
	public List<String> obtenerValores(String etiqueta) {
		Element elemento = documento.getDocumentElement();
		NodeList nodoRaiz = elemento.getElementsByTagName(etiqueta);
		List<String> lista = null;
		if (nodoRaiz.item(0) != null) {
			NodeList nodosHijos = nodoRaiz.item(0).getChildNodes();
			lista = new ArrayList<String>();
			for (int i = 0; i < nodosHijos.getLength(); i++)
				if (nodosHijos.item(i).getNodeType() == Node.ELEMENT_NODE)
					lista.add(nodosHijos.item(i).getTextContent());
		}
		return lista;
	}

}
