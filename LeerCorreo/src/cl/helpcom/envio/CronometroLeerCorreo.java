package cl.helpcom.envio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Properties;

import cl.helpcom.dao.ConexionSql1;

public class CronometroLeerCorreo extends Thread { // una clase que hereda de la
													// clase Thread
	private Properties propiedades = new Properties();
	private InputStream entrada = null;

	private ServerSocket sSocket;

	public CronometroLeerCorreo() {// Contructor porque la clase es heredada
		super();
	}

	public void run() {

		try {

			for (;;) {
				sSocket = new ServerSocket(1249);// instancia una ejecucion
				ConexionSql1 conSql = new ConexionSql1();
				conSql.getInfoEmpresa();
				sSocket.close();
				Thread.sleep(Long.valueOf("1800000"));
			}

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (entrada != null) {
				try {
					entrada.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}