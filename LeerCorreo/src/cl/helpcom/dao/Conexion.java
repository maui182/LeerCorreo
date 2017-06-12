/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.helpcom.dao;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Mauricio Rodriguez
 */
public class Conexion {

    private static Connection conexion;
    static String bd = "Base_de_Datos";
    static String user = "Usuario_Base_de_Datos";
    static String password = "Contraseña";
    static String server = "jdbc:mysql://localhost/" + bd;
    Properties propiedades = new Properties();

    //static String rut="";
    static boolean debug = true;

    public Conexion() throws ClassNotFoundException, SQLException, IOException {

    	Properties propiedades = new Properties();
		InputStream entrada = null;
//		entrada = new FileInputStream("Configuraciones/confMysqlServidor.properties");
		entrada = new FileInputStream("/usr/local/F_E/Configuraciones/Propiedades/configuracionMysql.properties");

		// cargamos el archivo de propiedades
		propiedades.load(entrada);

        server = propiedades.getProperty("SERVER_SERVER").toString();
        bd = propiedades.getProperty("BASE_DATOS_SERVER").toString();
        user = propiedades.getProperty("USUARIO_SERVER").toString();
        password = propiedades.getProperty("PASSWORD_SERVER").toString();

        Class.forName("com.mysql.jdbc.Driver");

        this.conexion = DriverManager.getConnection(server+"/"+bd, user, password);
        //conexion = DriverManager.getConnection(server, user, password);
       // System.out.println("Conexión a Servidor: \t" + server +"\t ESTABLECIDA");

    }

    public Connection getConexion() {
        return conexion;
    }

    public void cerrar(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                System.out.print("No es posible cerrar la Conexion");
            }
        }
    }

    public void cerrar(java.sql.Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
    }

    public void destruir() {

        if (conexion != null) {

            try {
                conexion.close();
            } catch (Exception e) {
            }
        }
    }
}
