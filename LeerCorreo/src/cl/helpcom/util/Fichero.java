package cl.helpcom.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SAAJResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cl.helpcom.dao.Conexion;
import cl.helpcom.dao.ConexionSql1;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author Mauricio Rodriguez
 *
 */
/**
 * @author mau
 *
 */
public class Fichero {

	public String[] nameFile = new String[10000];
	private Integer cantFiles;
	private LeerXML lXML = new LeerXML();

	Integer[][] subTotal = null;
	String[][] revisionDTE = null;
	String[][] detallesRevisionDTE = null;
	String carpetaProcesados = "";
	LectorFichero lectorFichero = new LectorFichero();

	/**
	 * @param filename
	 *            ruta del documento
	 * @return extension del documento
	 */
	public String getExtensionFile(String filename) {

		File f = new File(filename);
		if (f == null || f.isDirectory()) {
			return "nulo o directorio";
		} else if (f.isFile()) {
			int index = filename.lastIndexOf('.');
			if (index == -1) {
				return "";
			} else {
				// retorna extension
				return filename.substring(index + 1);
			}
		} else {
			return "que has enviado?";
		}
	}

	/**
	 * @param rutaArchivo
	 *            ruta del documento
	 * @return TRUE si comienza con DTEMAIL
	 */
	public Boolean beginFormatDTE(String rutaArchivo) {

		return rutaArchivo.contains("DTEMAIL");
	}

	/**
	 * Si Existe nombre del track Id en el fichero Lo guarda en RespuestaSII Si
	 * no Lo elimina de Recibidos
	 * 
	 * @param rutaArchivo
	 *            ruta del archivo
	 * @param track_id
	 *            trackID a comparar
	 * @return TRUE si existe
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void getTrackIDFichero(String carperaRaiz, String carperaRecibidos, Integer emp_id, String rutEmp,
		String carpetaPapelera, String carpetaSII) throws ClassNotFoundException, SQLException, IOException {
		
		ConexionSql1 conexionSql = new ConexionSql1();
		File f = new File(carperaRecibidos);
		File afile = null;
		String archivoS = "";
		String datos = "";
		String numeroS = "";
		String nombreArvchivo = "";
		String carpetaLibros = "Recepcion/Documentos/Libro/";
		String carpetaAcuse = "Recepcion/Documentos/Acuse";
		String carpetaTercero = "Recepcion/Documentos/Tercero";

		// Leer ficheros descargados
		char[] toCharArray = datos.toCharArray();

		if (!f.exists()) {

		} else {

			/** Primer Filtro RESCATAMOS LOS LIBROS [LBRMAIL___.txt] */
			File[] ficheros = f.listFiles();
			this.filtraLibros(ficheros, carperaRaiz, carpetaPapelera, emp_id, carpetaLibros);

			ficheros = f.listFiles();// Volver a armar arreglo de XML

			this.cantFiles = ficheros.length;
			for (int x = 0; x < ficheros.length; x++) {
				System.out.println("\n\t#"+x);
				archivoS = ficheros[x].getName();
				datos = archivoS;
				numeroS = this.obtieneNumero(datos);// obtiene numero trackID
													// del nombre del archivo
				// Cambiar de directorio y borrar los que no corresponden a DTE
				afile = new File(carperaRecibidos + "/" + ficheros[x].getName());

				String nombArchivo = afile.getName();
				System.out.println("\tNombre: "+nombArchivo);

				/**
				 * Segundo Filtro RECONOCE RESPUESTA SII [COMPARANDO TRACK ID EN
				 * BASE DATO]
				 */
				this.filtraRespuestaSII(numeroS, ficheros[x].getName(), emp_id, carperaRaiz, carpetaSII, conexionSql,afile);
				/** Tercer Filtro RECONOCE ACUSE DE RECIBO */
				this.filtrarAcuseRecibo(afile, emp_id, rutEmp, carperaRaiz, carpetaAcuse, carpetaPapelera, conexionSql);
				/** Cuarto Filtro RECONOCE DOCUMENTOS DE TERCERO */
				this.filtrarDTETercero(afile, rutEmp, carperaRaiz, carpetaTercero, carpetaPapelera, emp_id,conexionSql);
				/** En caso de que no sea ACUSE o TERCERO pasar a papelera */
				ArrayList<String> caratula = new ArrayList<String>();
				// caratula=lXML.capturaValoresCaratulaTercero(afile.getAbsolutePath());
				if (caratula.size() == 0) {
					nombArchivo = carperaRaiz + "/" + emp_id + "/" + carpetaPapelera + "/" + nombArchivo;
					afile.renameTo(new File(nombArchivo));// Cambia de directorio a carpeta Papelera
					try {
						afile.delete();
					} catch (Exception e) {
						// EN CASO DE QUE NO MUEVA A CARPETA PAPELERA
					}
					System.out.println("\tAgregando a ruta :"+afile.getAbsolutePath());

				}
			}
		}

	}

	public void filtrarDTETercero(File afile, String rutEmp, String carperaRaiz, String carpetaTercero,
			String carpetaPapelera, Integer emp_id, ConexionSql1 conexionSql)
			throws NumberFormatException, SQLException, FileNotFoundException {

		ArrayList<String> caratula = new ArrayList<String>();
		ArrayList<String> receptorTercero = new ArrayList<String>();
		ArrayList<String> totalesTercero = new ArrayList<String>();
		ArrayList<String> idDocTercero = new ArrayList<String>();
		ArrayList<String> emisTercero = new ArrayList<String>();
		ArrayList<String> emisTerceroVarios = new ArrayList<String>();
		caratula = lXML.capturaValoresCaratulaTercero(afile.getAbsolutePath());
		//emisTerceroVarios = lXML.capturaValoresVarios(afile.getAbsolutePath()); 
		
		if (caratula.size() > 0) {
			// VALIDA INFORMACION TERCERO
			if (caratula.get(2).equals(rutEmp)) {
				String rutaTercero = afile.getAbsolutePath();
				try {
					
					File file = new File(rutaTercero);
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document doc = db.parse(file);
					doc.getDocumentElement().normalize();
					NodeList nodeLst = doc.getElementsByTagName("DTE");
					


					for (int s = 0; s < nodeLst.getLength(); s++) {

						Node fstNode = nodeLst.item(s);
						
						emisTercero = lXML.capturaValoresEmisorTercero(nodeLst.item(s));
						receptorTercero = lXML.capturaValoresReceptorTercero(nodeLst.item(s));
						totalesTercero = lXML.capturaValoresTotalesTercero(nodeLst.item(s));
						idDocTercero = lXML.capturaValoresIdDocTercero(nodeLst.item(s));

						if (!emisTercero.isEmpty() && !receptorTercero.isEmpty() && !idDocTercero.isEmpty() && !totalesTercero.isEmpty()) {
							System.out.println("\tTipo Documento: " + Integer.valueOf(idDocTercero.get(0)));
							System.out.println("\tNumero Folio: " + Integer.valueOf(idDocTercero.get(1)));
							System.out.println("\tRut Emisor: " + emisTercero.get(0));
							
							String existeF=conexionSql.ExisteFactura(Integer.parseInt(idDocTercero.get(0)),
									Integer.parseInt(idDocTercero.get(1)), emisTercero.get(0));
							
							if (existeF.equals("")) {
							    conexionSql.addDocTerceroSQL(emp_id, Integer.parseInt(idDocTercero.get(0)),
								Integer.parseInt(idDocTercero.get(1)), emisTercero.get(0), (idDocTercero.get(2)),
								emisTercero.get(1), Integer.parseInt(totalesTercero.get(4)), "RECEPCIONADO");							
							
							} else {
								conexionSql.updateDocTerceroSQL(emp_id, Integer.parseInt(idDocTercero.get(0)),
								Integer.parseInt(idDocTercero.get(1)), emisTercero.get(0), (idDocTercero.get(2)),
								emisTercero.get(1), Integer.parseInt(totalesTercero.get(4)), "RECEPCIONADO");
							}
							
							
							int docId = conexionSql.getIdDoc(Integer.parseInt(idDocTercero.get(0)),
									Integer.parseInt(idDocTercero.get(1)), emisTercero.get(0));
							// Metodo procedureXml(),contiene la conexion al
							// procedimiento almacenado que agrega XML a
							// dte_rec_xmls en base de datos
							System.out.println("\tAgregado OK [TERCERO]");
							conexionSql.procedureXml(this.FileToString(rutaTercero), docId);
						}
					}					
				} catch (Exception e) {
					System.out.println("\tERROR: "+e);
				}
			} else {
				String rutaPapelera = carperaRaiz + "/" + emp_id + "/" + carpetaPapelera + "/" + afile.getName();
				afile.renameTo(new File(rutaPapelera));// Cambia de directorio a carpeta Acuse

				try {
					afile.delete();
				} catch (Exception e) {
					// En caso de que no cambie a carpeta acuse se elimina
				}
			}
		}
	}

	public void filtrarAcuseRecibo(File afile, Integer emp_id, String rutEmp, String carperaRaiz, String carpetaAcuse,
			String carpetaPapelera, ConexionSql1 conexionSql) throws SQLException {

		ArrayList<String> caratula = new ArrayList<String>();
		ArrayList<String> acuseComercial = new ArrayList<String>();// Acuse
																	// recibo
		ArrayList<String> acuseMercaderia = new ArrayList<String>();// Mercaderia
		ArrayList<String> acuseIntercambio = new ArrayList<String>();// Intercambio
		ArrayList<String> envioTerceroDTE = new ArrayList<String>();

		caratula = lXML.capturaValoresCaratulaAcuse(afile.getAbsolutePath());
		acuseComercial = lXML.capturaValoresResultadoDTE(afile.getAbsolutePath());// Obtiene
																					// valores
																					// ACEPTACION
																					// COMERCIAL
		acuseMercaderia = lXML.capturaValoresDocumentoRecibido(afile.getAbsolutePath());// Obtiene
																						// valores
																						// RECIBO
																						// MERCADERIA
																						// //ENTRA
																						// AQUI
		acuseIntercambio = lXML.capturaValoresRecepcionDTEAcuse(afile.getAbsolutePath());// Obtiene
																							// valores
																							// INTERCAMBIO
																							// ACUSE

		if (caratula.size() > 0) {
			// VALIDA INFORMACION ACUSE
			if (caratula.get(1).equals(rutEmp)) {
				String rutaAcuse = carperaRaiz + "/" + emp_id + "/" + carpetaAcuse + "/" + afile.getName();
				afile.renameTo(new File(rutaAcuse));
				// ACEPTACION COMERCIAL
				// <ResultadoDTE>
				if (acuseComercial.size() > 0) {
					conexionSql.existeDTE(acuseComercial.get(3), acuseComercial.get(0), acuseComercial.get(1),
							acuseComercial.get(5), "1", respuestaAceptacionComercial(acuseComercial.get(7)));
				}
				// RECIBO MERCADERIA
				// <DocumentoRecibo>
				if (acuseMercaderia.size() > 0) {
					conexionSql.existeDTE(acuseMercaderia.get(3), acuseMercaderia.get(0), acuseMercaderia.get(1),
							acuseMercaderia.get(5), "2", "Recibido OK");
				}
				// RECIBO INTERCAMBIO o ACUSE RECIBO
				// <RecepcionDTE>
				if (acuseIntercambio.size() > 0) {
					conexionSql.existeDTE(acuseIntercambio.get(3), acuseIntercambio.get(0), acuseIntercambio.get(1),
							acuseIntercambio.get(5), "3", this.respuestaAcuseRecibo(acuseIntercambio.get(6)));
				}
			} else {
				String rutaPapelera = carperaRaiz + "/" + emp_id + "/" + carpetaPapelera + "/" + afile.getName();
				afile.renameTo(new File(rutaPapelera));// Cambia de directorio a
														// carpeta Acuse
				try {
					System.out.println("\tAgregado OK [ACUSE DE RECIBO]");
					afile.delete();
				} catch (Exception e) {
					// En caso que no cambie archivos a carpeta acuse
				}
			}
		}
	}

	/**
	 * Filtra documentos que pertenecen a Libros de CV
	 * 
	 * @param ficheros
	 *            Arreglo de ficheros de carpeta RECIBIDOS
	 * @param carperaRaiz
	 *            Carpeta Raiz del sistema
	 * @param carpetaPapelera
	 *            Carpeta de Papelera
	 * @param emp_id
	 *            ID de la Empresa
	 * @param carpetaLibros
	 *            Carpeta donde se almacenarán los documentos
	 */
	public void filtraLibros(File[] ficheros, String carperaRaiz, String carpetaPapelera, Integer emp_id,
			String carpetaLibros) {
		String nombreArchivo;

		for (int i = 0; i < ficheros.length; i++) {
			if (!this.getExtensionFile(ficheros[i].getPath()).equals("xml")) {// Si
																				// NO
																				// es
																				// XML

				if (ficheros[i].getName().substring(0, 7).equals("LBRMAIL")
						& this.getExtensionFile(ficheros[i].getPath()).equals("txt")) {// SI
																						// comienza
																						// LBRMAIL
																						// y
																						// extension
																						// es
																						// .txt
					// Es Libro
					nombreArchivo = ficheros[i].getName();
					ficheros[i].renameTo(new File(carperaRaiz + "/" + emp_id + "/" + carpetaLibros + nombreArchivo));
					System.out.println("\tAgregado OK [LIBRO]");
				} else {
					// Es !xml y no es LIBRO
					nombreArchivo = ficheros[i].getName();
					ficheros[i].renameTo(new File(carpetaPapelera + nombreArchivo));
				}
			}
		}
	}

	/**
	 * @param numeroS
	 * @param archivoS
	 * @param emp_id
	 * @param carperaRaiz
	 * @param carpetaSII
	 * @param conexionSql
	 * @param afile
	 * @throws SQLException
	 */
	public void filtraRespuestaSII(String numeroS, String archivoS, Integer emp_id, String carperaRaiz,String carpetaSII, ConexionSql1 conexionSql, File afile) throws SQLException {
		Long numInt = null;
		try {
			numInt = Long.valueOf(numeroS);
		} catch (Exception e) {
			//Si pasa por catch No es una Respuesta de SII
		}
		//SI se encuentra TrackID en  BD
		if (conexionSql.isTrackID(numInt, emp_id).equals(Boolean.TRUE)) {
			System.out.println("\tExiste TrackID: " + numeroS + "=="+ conexionSql.isTrackID(Long.valueOf(numeroS), emp_id));
			afile.renameTo(new File(carpetaSII + "/" + afile.getName()));
			carpetaProcesados = carperaRaiz + "/" + emp_id + "/Recepcion/Procesados";
			// Llenar Respuesta SII
			try {
				// Obtengo los valores del TAG IDENTIFICACION del XML
				lXML.capturaValoresIdentificacion(carpetaSII + "/" + archivoS);
				Integer emp_idFile = conexionSql.getIdEmpresa(lXML.getRutEmisor());
				// Obtengo los valores del TAG SUBTOTAL del XML
				lXML.capturaValoresSubTotal2(carpetaSII + "/" + archivoS);
				
				subTotal = lXML.getSubTotal();
				// Obtengo FOLIO,TIPODTE,ESTADO
				lXML.capturaValoresRevisionDTE2(carpetaSII + "/" + archivoS, emp_id, lXML.getTrackID());// emp_id
				revisionDTE = lXML.getRevisionDTE();
				// Obtengo DETALLE
				detallesRevisionDTE = lXML.getDetalleRevisionDTE();
				// con.getRespuestaPaquete(emp_id,
				// lXML.getTrackID());//Respuesta a paquete
				conexionSql.updateFechaRespEnvio(lXML.getTrackID(), emp_id);

				afile.renameTo(new File(carpetaProcesados + "/" + archivoS));// Cambia
																				// de
																				// directorio
																				// a
																				// carpeta
																				// Procesados
				try {
					System.out.println("\tAgregado OK [RESPUESTA SII]");
					afile.delete();
				} catch (Exception e) {
					// EN CASO QUE NO SE MUEVA A CARPETA PROCESADOS
				}

			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	public void leer() {

		File f = new File("Attach/Respuesta_SII");
		File[] ficheros = f.listFiles();
		String archivoS = "";

		if (!f.exists()) {
		} else {
			for (int x = 0; x < ficheros.length; x++) {
				// Obtiene nombre archivos
				archivoS = ficheros[x].getName();
				System.out.println("\t" + archivoS);
			}
		}

	}

	public String FileToString(String rutaFile) throws FileNotFoundException {

		File fileBase = new File(rutaFile);
		BufferedReader entrada = null;
		String linea = "";
		String out = "";
		entrada = new BufferedReader(new FileReader(fileBase));

		try {
			while (entrada.ready()) {
				linea = entrada.readLine();// Capturo la linea
				out += linea;
			}

		} catch (Exception e) {
		}

		return out;
	}

	/**
	 * Obtiene una cadena de caracteres y regresa solo los numeros
	 * 
	 * @param nombreArchivo
	 * @return
	 */
	public String obtieneNumero(String nombreArchivo) {
		char[] toCharArray = nombreArchivo.toCharArray();
		String out = "";

		for (int i = 0; i < nombreArchivo.length(); i++) {
			char caracter = toCharArray[i];
			if (Character.isDigit(caracter)) {
				out += String.valueOf(caracter);// obtiene los numeros
			}
		}
		return out;
	}

	/**
	 * @param rutaArchivo
	 *            ruta del documento
	 * @return TRUE si comienza con LBRMAIL
	 */
	public Boolean beginFormatDTELibro(String rutaArchivo) {

		return rutaArchivo.contains("LBRMAIL");
	}

	/**
	 * Compara con formatoBegin, extension y trackID
	 * 
	 * @param sDirectorio
	 *            ruta del fichero
	 *
	 *            encuentra de qé tipo y extensión es el archivo DTE o LIBRO
	 *
	 */
	public Integer readFileFichero(String sDirectorio, Integer track_id) {
		File f = new File(sDirectorio);
		String archivoS = "";
		// Leer ficheros descargados

		if (!f.exists()) {
		} else {
			File[] ficheros = f.listFiles();
			this.cantFiles = ficheros.length;
			for (int x = 0; x < ficheros.length; x++) {
				// Obtiene nombre archivos
				archivoS = ficheros[x].getName();
				// System.out.println(this.getExtensionFile(sDirectorio+"/"+archivoS));
				if (this.getExtensionFile(sDirectorio + "/" + archivoS).equals("xml")) {
					if (this.beginFormatDTE(sDirectorio + "/" + archivoS).equals(Boolean.TRUE)) {
						// System.out.println(archivoS);
						return 1;
					}
				}
				if (this.getExtensionFile(sDirectorio + "/" + archivoS).equals("txt")) {
					if (this.beginFormatDTELibro(sDirectorio + "/" + archivoS).equals(Boolean.TRUE)) {
						// System.out.println(archivoS);

						return 2;
					}
				}

			}
		}
		return 0;
	}

	public Integer getCantFiles() {
		return cantFiles;
	}

	public void setCantFiles(Integer cantFiles) {
		this.cantFiles = cantFiles;
	}

	public Integer formatoTagDTE(String sDirectorio, String nomFile) {

		if (this.getExtensionFile(sDirectorio + "/" + nomFile).equals("xml")) {
			if (this.beginFormatDTE(sDirectorio + "/" + nomFile).equals(Boolean.TRUE)) {

				System.out.println("\tNombre del Archivo: " + nomFile);
				System.out.println("\tTipo de documento: DTE");
				return 1;
			}
		} else if (this.getExtensionFile(sDirectorio + "/" + nomFile).equals("txt")) {
			if (this.beginFormatDTELibro(sDirectorio + "/" + nomFile).equals(Boolean.TRUE)) {
				System.out.println(" \tNombre del archivo: " + nomFile);
				System.out.println(" \tTipo de documento: DTELibro");

			}
		} else {
			System.out.println("\tNombre del archivo: " + nomFile);
			System.out.println("\tTipo de documento: OTRO");
		}
		return 0;
	}

	/**
	 * 0 Envio Recibido Conforme 1 Envio Rechazado – Error de Schema 2 Envio
	 * Rechazado – Error de Firma 3 Envio Rechazado – RUT Receptor No
	 * Corresponde 90 Envio Rechazado – Archivo Repetido 91 Envio Rechazado –
	 * Archivo Ilegible 99 Envio Rechazado – Otros
	 * 
	 * @param numero
	 * @return
	 */
	public String respuestaAcuseRecibo(String numero) {

		ArrayList<String> out = new ArrayList<>();
		out.add("Envio Recibido Conforme");
		out.add("Envio Rechazado - Error de Schema");
		out.add("Envio Rechazado - Error de Firma");
		out.add("Envio Rechazado - RUT Receptor No Corresponde");
		for (int i = 4; i < 90; i++) {
			out.add("");
		}
		out.add(90, "Envio Rechazado - Archivo Repetido");
		out.add(91, "Envio Rechazado - Archivo Ilegible");
		for (int i = 92; i < 99; i++) {
			out.add("");
		}
		out.add(99, "Envio Rechazado - Otros");

		return out.get(Integer.valueOf(numero));
	}

	/**
	 * 0 DTE Aceptado OK 1 DTE Aceptado con Discrepancia 2 DTE Rechazado
	 * 
	 * @param numero
	 * @return
	 */
	public String respuestaAceptacionComercial(String numero) {

		ArrayList<String> out = new ArrayList<>();
		out.add("DTE Aceptado OK");
		out.add("DTE Aceptado con Discrepancia");
		out.add("DTE Rechazado");

		return out.get(Integer.valueOf(numero));
	}

}
