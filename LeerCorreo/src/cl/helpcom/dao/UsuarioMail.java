package cl.helpcom.dao;

/**
 * @author Mauricio Rodriguez
 *
 */
public class UsuarioMail {


	public static String host;
	public static Integer port;
	public static String username;
	public static String password;
	public static String carpeta;
	public static String getHost() {
		return host;
	}
	public static void setHost(String host) {
		UsuarioMail.host = host;
	}
	public static Integer getPort() {
		return port;
	}
	public static void setPort(Integer port) {
		UsuarioMail.port = port;
	}
	public static String getUsername() {
		return username;
	}
	public static void setUsername(String username) {
		UsuarioMail.username = username;
	}
	public static String getPassword() {
		return password;
	}
	public static void setPassword(String password) {
		UsuarioMail.password = password;
	}
	public static String getCarpeta() {
		return carpeta;
	}
	public static void setCarpeta(String carpeta) {
		UsuarioMail.carpeta = carpeta;
	}



}
