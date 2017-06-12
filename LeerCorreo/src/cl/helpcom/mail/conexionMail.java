package cl.helpcom.mail;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import cl.helpcom.dao.ConexionSql1;
import cl.helpcom.dao.UsuarioMail;
import cl.helpcom.util.Fichero;
import cl.helpcom.util.LeerXML;


/**
 * @author mau
 *
 */
public class conexionMail {

	private Properties propiedades = new Properties();
    private InputStream entrada = null;
	/**
	 * @param con  Conexion de ConexionSql1
	 * @param empID ID Empresa a leer
	 * @param rut_empresa	RUT de la empresa a leer
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * @throws InterruptedException 
	 * @throws NumberFormatException 
	 */
	public void conectarMail(ConexionSql1 con, Integer empID,String rut_empresa) throws ClassNotFoundException, SQLException, IOException, NumberFormatException, InterruptedException{

		entrada = new FileInputStream("/usr/local/F_E/Configuraciones/Propiedades/configuracionServidor.properties");
        // cargamos el archivo de propiedades
        propiedades.load(entrada);
		UsuarioMail userMail = new UsuarioMail();
		Integer emp_id=empID;
		String carpetaRaiz=propiedades.getProperty("RUTA_RAIZ");
		String carpetaRecibidos=carpetaRaiz+"/"+emp_id+"/Recepcion/Recibidos";
	    String carpetaRespuestaSII= carpetaRaiz+"/"+emp_id+"/Recepcion/Documentos/RespuestaSII";
	    String carpetaLibro= carpetaRaiz+"/"+emp_id+"/Recepcion/Documentos/Libro";
	    String carpetaPapelera= carpetaRaiz+"/"+emp_id+"/Recepcion/Papelera/";
	    
		con.getUserDate(emp_id);//Datos de la empresa emp_id
		Integer[][] subTotal = null;
		String[][] revisionDTE = null;
		String[][] detallesRevisionDTE = null;

		System.out.println("Iniciando Conexion a: ["+ userMail.getUsername()+"]\n");
		//DATOS DE CONEXION
		String host = userMail.getHost();
		String port = userMail.getPort().toString();
		String userName = userMail.getUsername();
		String password = userMail.getPassword();
		String carpetaDescarga = carpetaRecibidos+"/";//Donde se reciben
		String sujeto = "";

		Fichero f = new Fichero();
//		LeerXML lXML = new LeerXML();
		// CONEXION AL CORREO
		EmailAttachmentReceiver receiver = new EmailAttachmentReceiver();
		receiver.setSaveDirectory(carpetaRecibidos);
		receiver.downloadEmailAttachments(host, port,userName, password,carpetaDescarga, sujeto);//Descarga adjuntos
		System.out.println("\nProcesando Descargas\n");
		f.getTrackIDFichero(carpetaRaiz,carpetaRecibidos,emp_id,rut_empresa,carpetaPapelera,carpetaRespuestaSII);//Revisa esquema y encuentra RespuestasSII y Libros
		
	}
}
