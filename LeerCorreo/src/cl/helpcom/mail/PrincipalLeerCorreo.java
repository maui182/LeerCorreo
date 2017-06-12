package cl.helpcom.mail;
import java.io.IOException;
import java.sql.SQLException;

import javax.mail.MessagingException;

import cl.helpcom.dao.ConexionSql1;
import cl.helpcom.envio.CronometroLeerCorreo;
import cl.helpcom.util.Fichero;
/**
 * @author Mauricio Rodriguez G.	
 *
 */
public class PrincipalLeerCorreo {
	public static void main(String[] args) throws MessagingException, IOException, ClassNotFoundException, SQLException {

		CronometroLeerCorreo cronometroLeerCorreo = new CronometroLeerCorreo();
		cronometroLeerCorreo.run();
	}	
}