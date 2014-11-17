package cliente;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import json.QueryManager;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class VistaVentanaCliente extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JPanel contentPane;
	private ControladorCliente controlador;
	private JTextField txtQuery;
	private JLabel lblEstado;
	private JLabel lblResultado;
	DefaultTableModel dtm;
	private JComboBox<String> comboBox;

	public VistaVentanaCliente(ControladorCliente c) {

		controlador = c;
		this.setTitle("Modo Cliente");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JLabel lblClienteDeBases = new JLabel(
				"Cliente de Bases de Datos Remotas");
		lblClienteDeBases.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblClienteDeBases, BorderLayout.NORTH);

		lblEstado = new JLabel("Esperando iniciar conexión...");
		lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblEstado, BorderLayout.SOUTH);

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new BorderLayout(0, 0));

		JPanel panel_3 = new JPanel();
		panel_2.add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel = new JLabel("Tipo: ");
		panel_3.add(lblNewLabel, BorderLayout.WEST);
		
		comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {QueryManager.TIPO_CONSULTA, QueryManager.TIPO_MODIFICACION}));
		panel_3.add(comboBox, BorderLayout.CENTER);

		JPanel panel_4 = new JPanel();
		panel_2.add(panel_4, BorderLayout.SOUTH);
		panel_4.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel_1 = new JLabel("Query: ");
		panel_4.add(lblNewLabel_1, BorderLayout.WEST);

		txtQuery = new JTextField();
		txtQuery.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					ejecutarQuery();
				}
			}
		});
		panel_4.add(txtQuery, BorderLayout.CENTER);
		txtQuery.setColumns(10);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		dtm = new DefaultTableModel();
		table = new JTable(dtm);

		JScrollPane scrollPane = new JScrollPane(table);
		panel_1.add(scrollPane);

		lblResultado = new JLabel("Esperando realizar una consulta...");
		panel_1.add(lblResultado, BorderLayout.SOUTH);
		lblResultado.setHorizontalAlignment(SwingConstants.CENTER);
		this.setVisible(true);
	}

	private void ejecutarQuery() {
		if (txtQuery.getText().equals("")) {
			lblResultado.setText("Falta escribir la consulta");
		} else {
			lblResultado.setText("Enviando consulta...");
			lblEstado.setText("Iniciando conexion...");
			controlador.enviarDatos((String) comboBox.getSelectedItem(), txtQuery.getText());
		}
	}

	public void mostrarRespuesta(List<String> respuesta) {
		if (respuesta == null) {
			lblResultado
					.setText("Respuesta nula, hubo un error en el procesamiento de la consulta");
		} else if (respuesta.get(0).equals("0")) {
			lblResultado.setText("No se ha obtenido ninguna tupla");
		} else if (respuesta.get(0).equals(QueryManager.ERROR)) {
			lblResultado.setText("La consulta ha retornado un error");
			JOptionPane.showMessageDialog(null, respuesta.get(1),
					"Mensaje de error", JOptionPane.WARNING_MESSAGE);
		} else if (respuesta.get(0).equals(QueryManager.CANTFILAS))
			if (respuesta.get(1).equals("0"))
				lblResultado
						.setText("La consulta no ha actualizado ninguna fila");
			else
				lblResultado.setText("La consulta ha actualizado "
						+ respuesta.get(1) + " filas");
		else {
			int cantColumnas = Integer.valueOf(respuesta.get(0));
			int cantFilas = (respuesta.size() - 1) / cantColumnas;
			dtm.setRowCount(cantFilas - 1);
			dtm.setColumnCount(cantColumnas);
			Vector<String> v = new Vector<String>();
			for (int i = 1; i <= cantColumnas; i++)
				v.add(respuesta.get(i));
			dtm.setColumnIdentifiers(v);
			for (int i = 1; i < cantFilas; i++) {
				for (int j = 0; j < cantColumnas; j++) {
					table.getModel()
							.setValueAt(
									respuesta.get(j + 1 + (cantColumnas * i)),
									i - 1, j);
				}
			}
			lblResultado.setText("La consulta se ha ejecutado");
			lblResultado.setSize(lblResultado.getPreferredSize());
		}
		lblEstado.setText("Conexión establecida");
		controlador.avisarCierreDeSocket();
	}
}
