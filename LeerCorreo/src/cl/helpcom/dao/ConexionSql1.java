/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.helpcom.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import com.mysql.jdbc.ServerPreparedStatement;

public class ConexionSql1 {

	final String SUFIJO = "";
	private Conexion conexion;
	Connection con;
	UsuarioMail userMail = new UsuarioMail();

	private Integer docIDInt;
	private Integer canDetalle;

	private String rutEmisor;
	private Integer trackID;
	private Integer empID;

	public ConexionSql1() throws ClassNotFoundException, SQLException, IOException {
		conexion = new Conexion();
		this.con = conexion.getConexion();
	}

	/**
	 * Obtiene el ID de la Empresa
	 * 
	 * @param rutEmpresa
	 *            rut del documento XML
	 * @return ID de la empresa
	 * @throws SQLException
	 */
	public Integer getIdEmpresa(String rutEmpresa) throws SQLException {

		Statement comando = this.con.createStatement();

		ResultSet registro;
		Integer res = 0;

		registro = comando.executeQuery("SELECT emp_id FROM sys_empresa WHERE emp_rut='" + rutEmpresa + "'");
		while (registro.next()) {

			res = registro.getInt("emp_id");
		}
		return res;
	}

	/**
	 * Recorre todas las empresas con emp_activo=SI obteniendo datos de conexion
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws NumberFormatException
	 */
	public void getInfoEmpresa()
			throws SQLException, ClassNotFoundException, IOException, NumberFormatException, InterruptedException {

		Statement comando = this.con.createStatement();
		cl.helpcom.mail.conexionMail conMail = new cl.helpcom.mail.conexionMail();
		ResultSet registro;
		Integer res = 0;
		registro = comando.executeQuery(
				"SELECT emp_id,emp_rut,emp_ccsii_host,emp_ccsii_port,emp_ccsii_username,emp_ccsii_password,emp_ccsii_carpeta,emp_activa FROM sys_empresa WHERE emp_activa='SI'");

		while (registro.next()) {
			this.setRutEmisor(registro.getString("emp_rut"));
			this.setEmpID(registro.getInt("emp_id"));
			System.out.println("---------------------------------------------------------");
			System.out.println("RUT: " + this.getRutEmisor() + "  ||  " + "EMPID: " + this.getEmpID());
			conMail.conectarMail(this, this.getEmpID(), this.getRutEmisor());
		}
		if (comando!=null){
			comando.close();
		}
		if (registro!=null){
			registro.close();
		}
	}

	/**
	 * Metodo que consulta en la base de datos el estado de doc_respuesta_sii
	 * 
	 * @param folio
	 * @param emp_id
	 * @param tipo_doc
	 * @return
	 * @throws SQLException
	 */
	public String VerificarAcuse(Integer folio, Integer emp_id, Integer tipo_doc) throws SQLException {
		Statement comando = this.con.createStatement();
		ResultSet registro;
		registro = comando.executeQuery("SELECT doc_respuesta_sii FROM dte_emi_documento WHERE emp_id='" + emp_id
				+ "' AND tdo_id='" + tipo_doc + "' AND doc_folio='" + folio + "';");
		String verificar = "";

		while (registro.next()) {

			verificar = registro.getString("doc_respuesta_sii");
		}
		// System.out.println("RESULTADO BASE DE DATOS "+ verificar);
		return verificar;
	}

	/**
	 * Cambiar estado de respuesta SII en la tabla "dte_emi_documento"
	 * 
	 * @param folio
	 * @param emp_id
	 * @param tipo_doc
	 * @param resp_sii
	 * @throws SQLException
	 */
	public void updateEstado(Integer folio, Integer emp_id, Integer tipo_doc, String resp_sii) throws SQLException {
		Statement comando = this.con.createStatement();
		comando.executeUpdate("UPDATE dte_emi_documento SET doc_respuesta_sii='" + resp_sii + "' WHERE emp_id='"
				+ emp_id + "' AND tdo_id='" + tipo_doc + "' AND doc_folio='" + folio + "';");

	}

	public void updateEstadoAcepta(String resp_sii, Integer emp_id, Long trackID) throws SQLException {
		Statement comando = this.con.createStatement();
		comando.executeUpdate("UPDATE dte_emi_documento SET doc_respuesta_sii='" + resp_sii + "' WHERE emp_id='"
				+ emp_id + "' AND doc_track_id='" + trackID + "';");
	}

	// SI EXISTEN REPAROS RECHAZO ACEPTA
	public String isREPARO_RECHAZO(Long track_id) throws SQLException {

		Statement comando = this.con.createStatement();
		ResultSet registro;
		Integer rechazo = 0;
		Integer reparo = 0;
		registro = comando
				.executeQuery("SELECT doc_respuesta_sii FROM dte_emi_documento WHERE doc_track_id='" + track_id + "';");

		while (registro.next()) {
			if (registro.getString("doc_respuesta_sii").equals("RECHAZADO")) {
				rechazo++;
			}
			if (registro.getString("doc_respuesta_sii").equals("ACEPTADO CON REPARO")) {
				reparo++;
			}
		}

		if (rechazo > 0) {
			return "PAQUETE RECHAZADO";// Existe un rechazo
		} else if (reparo > 0) {
			return "PAQUETE CON REPARO";
		}
		return "PAQUETE ACEPTADO";
	}

	// FECHAS
	public void updateFechaRespEnvio(Long trackID, Integer emp_id) throws SQLException {
		Statement comando = this.con.createStatement();
		String estadoPkg = this.isREPARO_RECHAZO(trackID);
		comando.executeUpdate("UPDATE rec_emi_envios SET env_fecha_respuesta_sii=NOW(), env_estado='" + estadoPkg
				+ "' WHERE env_track_id='" + trackID + "' AND emp_id='" + emp_id + "';");
		comando.close();
	}

	// CONSULTA 3.2
	public void updateRespuestaEnvio(Integer trackID, Integer emp_id, Integer tipo_doc, String resp_sii)
			throws SQLException {

		Statement comando = this.con.createStatement();
		comando.executeUpdate("UPDATE dte_emi_documento SET doc_respuesta_sii='" + resp_sii + "' WHERE doc_track_id='"
				+ trackID + "' AND emp_id='" + emp_id + "' AND tdo_id='" + tipo_doc + "';");
	}

	/**
	 * Se agrega registro de REPARO o RECHAZO para su posterior arbol de
	 * detalles
	 * 
	 * @param trackID
	 * @param emp_id
	 * @param tdo_id
	 * @param doc_folio
	 * @param ere_estado
	 * @throws SQLException
	 */
	public void addEnviosRechazo(Long trackID, Integer emp_id, Integer tdo_id, Integer doc_folio, String ere_estado)
			throws SQLException {

		Statement comando = this.con.createStatement();
		String sql = "INSERT INTO rec_emi_envios_rechazados (env_track_id,emp_id,tdo_id,doc_folio,ere_estado) VALUES (?,?,?,?,?);";

		PreparedStatement statement = (PreparedStatement) this.con.prepareStatement(sql);
		statement.setLong(1, trackID);
		statement.setInt(2, emp_id);
		statement.setInt(3, tdo_id);
		statement.setInt(4, doc_folio);
		statement.setString(5, ere_estado);

		int rowsInserted = statement.executeUpdate();

		if (rowsInserted > 0) {
			// System.out.println("Se ha guardado correctamente!");
		} else {
			System.out.println("NO se ha podido insertar el registro correctamente");
		}
	}

	/**
	 * Obtener ID de tabla "rec_emi_envios_rechazados" donde
	 * "TrackID,emp_id,folio,tipoDoc"
	 * 
	 * @param trackID
	 * @param emp_id
	 * @param folio
	 * @param tipoDoc
	 * @return
	 * @throws SQLException
	 */
	public Integer getEre_Id(Long trackID, Integer emp_id, Integer folio, Integer tipoDoc) throws SQLException {

		Statement comando = this.con.createStatement();
		ResultSet registro;
		Integer res = 0;

		registro = comando.executeQuery("SELECT ere_id FROM rec_emi_envios_rechazados WHERE env_track_id='" + trackID
				+ "' AND emp_id='" + emp_id + "' AND doc_folio='" + folio + "' AND tdo_id='" + tipoDoc + "';");
		while (registro.next()) {

			res = registro.getInt("ere_id");
			if (!res.equals(0)) {
				return res;
			}
		}
		return 0;
	}

	/**
	 * Se agregar DETALLE de cada envio con REPADO o RECHAZO
	 * 
	 * @param ere_id
	 * @param erm_detalle
	 * @throws SQLException
	 */
	public void addEnviosRechazoDetalle(Integer ere_id, String erm_detalle) throws SQLException {
		Statement comando = this.con.createStatement();
		String sql = "INSERT INTO rec_emi_envios_rechazados_detalle (ere_id,erm_detalle) VALUES (?,?);";

		PreparedStatement statement = (PreparedStatement) this.con.prepareStatement(sql);
		statement.setInt(1, ere_id);
		statement.setString(2, erm_detalle);

		int rowsInserted = statement.executeUpdate();

		if (rowsInserted > 0) {
			// System.out.println("Se ha guardado correctamente!");
		} else {
			System.out.println("NO se ha podido insertar el registro correctamente");
		}
	}

	public Boolean existeTrackID(Integer track_id, String emp_rut) throws SQLException {

		Statement comando = this.con.createStatement();
		System.out.println();
		ResultSet registro;
		Integer res = 0;

		registro = comando.executeQuery(
				"SELECT env_track_id,emp_rut FROM rec_emi_envios a INNER JOIN sys_empresa b USING(emp_id) WHERE env_track_id="
						+ track_id + " AND emp_id=" + emp_rut + ";");
		while (registro.next()) {

			res = registro.getInt("env_track_id");
			if (!res.equals(0)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Este metodo invoca el procedimiento almacenado (AddTercero) de la base de
	 * datos db_portal_dte El procedimiento (AddTercero) basicamente pregunta a
	 * la base de datos si existen documentos de acuerdo a un tdo_id doc_folio y
	 * doc_rut_emisor, si existe se actualizan los datos, sino entonces agrega
	 * el nuevo registro
	 * 
	 * @param int
	 *            empId(emp_id): id de la empresa
	 * @param int
	 *            tdoId(tdo_id): id del tipo de documento
	 * @param int
	 *            docFolio(doc_folio): numero de folio
	 * @param String
	 *            docRutEmisor (doc_rut_emisor): rut del emisor de documento
	 * @param String
	 *            docFechaEmision (doc_fecha_emision): fecha de emision de
	 *            documento
	 * @param String
	 *            docRazon (doc_razon): razon social de empresa
	 * @param int
	 *            (doc_monto): monto total de ingresos emisor
	 * @param String
	 *            docEstado (doc_estado): estado de documento 'RECEPCIONADO'
	 */
	public void addDocTercero(int empId, int tdoId, int docFolio, String docRutEmisor, String docFechaEmision,
			String docRazon, int docMonto, String docEstado) {
		String resultado = "";
		try {
			// se crea instancia a procedimiento, los parametros de entrada y
			// salida se simbolizan con el signo ?
			CallableStatement proc = con.prepareCall(" CALL AddTercero(?,?,?,?,?,?,?,?)");

			// se cargan los parametros de entrada
			proc.setInt("empId", empId);// Tipo String
			proc.setInt("tdoId", tdoId);// Tipo entero
			proc.setInt("docFolio", docFolio);// Tipo entero
			proc.setString("docRutEmisor", docRutEmisor);// Tipo String
			proc.setString("docFechaEmision", docFechaEmision);// Tipo String
			proc.setString("docRazon", docRazon);// Tipo String
			proc.setInt("docMonto", docMonto);// Tipo entero
			proc.setString("docEstado", docEstado);// Tipo String

			System.out.println("empID " + empId + " tdoId " + tdoId + " docFolio " + docFolio + " Rut emisor:  "
					+ docRutEmisor + " doc Fecha emision: " + docFechaEmision + " doc Razon " + docRazon
					+ " doc monto : " + docMonto + " doc Estado : " + docEstado);

			// Se ejecuta el procedimiento almacenado
			// proc.registerOutParameter("resultado", Types.VARCHAR);
			proc.execute();
			//
			// System.out.println(proc.getString("resultado"));
			// System.out.println("Salida: "+ proc.execute());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Este metodo invoca el procedimiento almacenado (AddXml) de la base de
	 * datos db_portal_dte El procedimiento (AddXml) basicamente verifica el
	 * docId(doc_id), si ya existe entonces actualiza el Xml, pero si no
	 * encuentra agrega el nuevo Xml de acuerdo a doc_id
	 * 
	 * @param String
	 *            dexXml: Ruta de Xml que se agrega a ted_rec_documento
	 * @param int
	 *            docId: id del doc que debe actualizar o agregar xml
	 */
	public void procedureXml(String dexXml, int docId) {

		try {

			// se crea instancia a procedimiento, los parametros de entrada y
			// salida se simbolizan con el signo ?
			CallableStatement proc = con.prepareCall(" CALL AddXml(?,?)");

			// se cargan los parametros de entrada

			proc.setString("dexXml", dexXml);// Tipo String
			proc.setInt("docId", docId);// Tipo entero

			// Se ejecuta el procedimiento almacenado
			// System.out.println("ruta XML :: "+ dexXml +" docId :: "+docId);
			proc.execute();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void addDocTerceroSQL(int empId, int tdoId, int docFolio, String docRutEmisor, String docFechaEmision,
			String docRazon, int docMonto, String docEstado) {

		try {
			Statement comando = this.con.createStatement();
			String sql2 = "INSERT INTO dte_rec_documento (emp_id,tdo_id, doc_folio,doc_rut_emisor,doc_fecha_emision,doc_razon_social,doc_monto,doc_estado) VALUES (?,?,?,?,?,?,?,?);";
			PreparedStatement statement = (PreparedStatement) this.con.prepareStatement(sql2);
			statement.setInt(1, empId);
			statement.setInt(2, tdoId);
			statement.setInt(3, docFolio);
			statement.setString(4, docRutEmisor);
			statement.setString(5, docFechaEmision);
			statement.setString(6, docRazon);
			statement.setInt(7, docMonto);
			statement.setString(8, docEstado);

			int rowsInserted = statement.executeUpdate();

			if (rowsInserted > 0) {

			} else {
				System.out.println("NO se ha podido insertar el registro correctamente");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateDocTerceroSQL(int empId, int tdoId, int docFolio, String docRutEmisor, String docFechaEmision,
			String docRazon, int docMonto, String docEstado) throws SQLException {

		Statement comando = this.con.createStatement();
		comando.executeUpdate("UPDATE dte_rec_documento SET emp_id=" + empId + ",doc_fecha_emision='" + docFechaEmision
				+ "',doc_razon_social='" + docRazon + "',doc_monto=" + docMonto + ",doc_estado='" + docEstado
				+ "' WHERE tdo_id=" + tdoId + " and doc_folio=" + docFolio + " and doc_rut_emisor='" + docRutEmisor
				+ "';");
	}

	public String ExisteFactura(int tdoId, int docFolio, String docRutEmisor) {

		try {
			Statement comando = this.con.createStatement();
			ResultSet registro;
			Integer res = 0;
			String resultado = "";
			registro = comando.executeQuery("SELECT * FROM dte_rec_documento where tdo_id=" + tdoId + " and doc_folio="
					+ docFolio + " and doc_rut_emisor='" + docRutEmisor + "';");

			while (registro.next()) {

				res = registro.getInt("doc_folio");
				// System.out.println("a ver que sale aqui "+res);
				return res + "";

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Verifica con TRUE si existe el documento en la base de datos validando
	 * TipoDTE, Folio,Rut Emisor, Mnt Total
	 * 
	 * @param emp_rut
	 * @param tipoDTE
	 * @param folio
	 * @param montoTotal
	 * @param tpoAcuse
	 * @param respuestaACUSE
	 * @throws SQLException
	 */
	public void existeDTE(String emp_rut, String tipoDTE, String folio, String montoTotal, String tpoAcuse,
			String respuestaACUSE) throws SQLException {

		Statement comando = this.con.createStatement();
		ResultSet registro;
		Integer res_doc_id = 0;
		Integer res_tdo_id = 0;
		Integer res_folio = 0;
		String res_rut = "";
		Integer res_monto = -1;

		registro = comando.executeQuery(
				"SELECT doc_id, tdo_id,doc_folio,emp_rut,doc_monto FROM dte_emi_documento a INNER JOIN sys_empresa b USING(emp_id) WHERE tdo_id="
						+ tipoDTE + " AND doc_folio=" + folio + " AND emp_rut='" + emp_rut + "' AND doc_monto="
						+ montoTotal + ";");

		while (registro.next()) {

			res_tdo_id = registro.getInt("tdo_id");
			res_folio = registro.getInt("doc_folio");
			res_rut = registro.getString("emp_rut");
			res_monto = registro.getInt("doc_monto");
			res_doc_id = registro.getInt("doc_id");
			System.out.println("\tDoc ID:" + res_doc_id);
			System.out.println("\tTipo:" + res_tdo_id);
			System.out.println("\tFolio:" + res_folio);
			System.out.println("\tMonto:" + res_monto);

			// coinciden los datos del documento con algun registro de BD
			if (!res_tdo_id.equals(0) && !res_tdo_id.equals(0) && !res_rut.equals("") && !res_monto.equals(-1)) {
				if (tpoAcuse.equals("1")) {
					this.updateEstadoAcuse(res_doc_id, "doc_acuse", respuestaACUSE);// cambia
																					// respuesta
					System.out.println("\t[ACUSE]");
				}
				if (tpoAcuse.equals("2")) {
					this.updateEstadoAcuse(res_doc_id, "doc_acuse_mercaderia", respuestaACUSE);// cambia
																								// respuesta
					System.out.println("\t[ACUSE MERCADERIA]");
				}
				if (tpoAcuse.equals("3")) {
					this.updateEstadoAcuse(res_doc_id, "doc_acuse_intercambio", respuestaACUSE);// cambia
																								// respuesta
					System.out.println("\t[ACUSE INTERCAMBIO]");
				}
			}
		}

	}

	/**
	 * @param track_id
	 *            Obtenido de fichero
	 * @param emp_id
	 *            ID Empresa
	 * @return TRUE= Si encuentra coincidencia FALSE= No encuentra coincidencia
	 * @throws SQLException
	 */
	public Boolean isTrackID(Long track_id, Integer emp_id) throws SQLException {

		Statement comando = this.con.createStatement();
		ResultSet registro;
		Long res = null;

		registro = comando.executeQuery("SELECT doc_track_id,emp_id FROM dte_emi_documento WHERE doc_track_id="
				+ track_id + " AND emp_id=" + emp_id + ";");
		while (registro.next()) {

			res = registro.getLong("doc_track_id");

			if (!res.equals(0)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	// OBTENER DATOS DE USUARIO

	public void getUserDate(Integer emp_id) throws SQLException, NumberFormatException, InterruptedException {

		Integer reLoad = 0;
		Boolean flag = false;
		Statement comando= null;
		ResultSet registro=null;
		
		while (flag.equals(false)) {
			try {
				if (comando != null){
					System.out.println("Closing conexion");
					comando.close();
				}
				if (registro!= null){
					System.out.println("Closing registro");
					registro.close();
				}
				comando= this.con.createStatement();
				registro = comando.executeQuery(
						"SELECT emp_ccsii_host,emp_ccsii_port,emp_ccsii_username,emp_ccsii_password,emp_ccsii_carpeta FROM sys_empresa WHERE emp_id='"+ emp_id + "';");

				while (registro.next()) {
					this.userMail.setHost(registro.getString("emp_ccsii_host"));
					this.userMail.setPort(registro.getInt("emp_ccsii_port"));
					this.userMail.setUsername(registro.getString("emp_ccsii_username"));
					this.userMail.setPassword(registro.getString("emp_ccsii_password"));
					this.userMail.setCarpeta(registro.getString("emp_ccsii_carpeta"));
					flag = true;
				}
				if (comando != null){
					comando.close();
				}
			} catch (Exception e) {
				
				reLoad++;
				System.out.println("Intento N° " + reLoad + " de reconexion, tardará aproximadamente 10 segundos");
				Thread.sleep(Long.valueOf("100000"));
				if (reLoad.equals(3)) {
					flag = true;
				}
				
			}
		}

	}

	// MUESTRA SI PAQUETE ENTERO ES RECHAZO, REPARO, ACEPTA
	public void updateEstadoPacket(Boolean isRechazo, Boolean isReparo, Integer trackID, Integer emp_id)
			throws SQLException {

		Statement comando = this.con.createStatement();
		String estado = "";
		if (isRechazo.equals(Boolean.TRUE)) {
			estado = "Paquete Rechazado";
		} else if (isRechazo.equals(Boolean.FALSE) && isReparo.equals(Boolean.TRUE)) {
			estado = "Paquete Aceptado con Reparo";
		} else if (isRechazo.equals(Boolean.FALSE) && isReparo.equals(Boolean.FALSE)) {
			estado = "Paquete Aceptado";
		}
		comando.executeUpdate("UPDATE rec_emi_envios SET env_estado='" + estado + "' WHERE env_track_id='" + trackID
				+ "' AND emp_id='" + emp_id + "';");
	}

	/**
	 * Cambia el estado del acuse de recibo al correspondiente
	 * 
	 * @param doc_id
	 *            ID del registro de la bd del documento
	 * @param recepDTEGlosa
	 *            Valor de la glosa de respuesta
	 * @throws SQLException
	 */
	public void updateEstadoAcuse(Integer doc_id, String campo, String RESPUESTA) throws SQLException {

		Statement comando = this.con.createStatement();
		comando.executeUpdate(
				"UPDATE dte_emi_documento SET " + campo + "='" + RESPUESTA + "' WHERE doc_id='" + doc_id + "';");
	}

	public void getRespuestaPaquete(Integer emp_id, Integer trackID) throws SQLException {

		Statement comando = this.con.createStatement();

		ResultSet registro;
		registro = comando.executeQuery("SELECT doc_respuesta_sii FROM dte_emi_documento WHERE emp_id='" + emp_id
				+ "' AND doc_track_id='" + trackID + "';");
		Boolean isRep = false;
		Boolean isRch = false;

		while (registro.next()) {

			if (registro.getString("doc_respuesta_sii").equals("RECHAZADO")) {
				isRch = true;
			} else if (registro.getString("doc_respuesta_sii").equals("ACEPTADO CON REPARO")) {
				isRep = true;
			}
		}

		this.updateEstadoPacket(isRch, isRep, trackID, emp_id);
	}

	/**
	 * Obtiene id de documento, este metodo sirve para que luego se inserte el
	 * xml de acuerdo a doc_id
	 * 
	 * @param tdoId:
	 *            tipo de documento (33,35,etc)
	 * @param docFolio:
	 *            numero de folio de documento, este numero es unico segun tipo
	 *            de documento
	 * @param docRutEmisor:
	 *            rut de empresa emisora de documento
	 * @return id: retorna id de documento
	 * @throws SQLException
	 */
	public int getIdDoc(int tdoId, int docFolio, String docRutEmisor) throws SQLException {

		Statement comando = this.con.createStatement();
		ResultSet registro;
		registro = comando.executeQuery("SELECT doc_id FROM dte_rec_documento where tdo_id=" + tdoId + " and doc_folio="
				+ docFolio + " and doc_rut_emisor='" + docRutEmisor + "';");

		int id = 0;

		while (registro.next()) {

			id = registro.getInt("doc_id");
		}

		return id;
	}

	public Integer getDocIDInt() {
		return docIDInt;
	}

	public void setDocIDInt(Integer docIDInt) {
		this.docIDInt = docIDInt;
	}

	public Integer getCanDetalle() {
		return canDetalle;
	}

	public void setCanDetalle(Integer canDetalle) {
		this.canDetalle = canDetalle;
	}

	public String getRutEmisor() {
		return rutEmisor;
	}

	public void setRutEmisor(String rutEmisor) {
		this.rutEmisor = rutEmisor;
	}

	public Integer getTrackID() {
		return trackID;
	}

	public void setTrackID(Integer trackID) {
		this.trackID = trackID;
	}

	public Integer getEmpID() {
		return empID;
	}

	public void setEmpID(Integer empID) {
		this.empID = empID;
	}

}