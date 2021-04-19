package auxiliar;

import java.sql.*;


/**
 * Clase DatabaseConnection: Permite realizar conexiones con la bbdd
 */
public class DatabaseConnection {
    private static Connection conn = null;
    private static final String url = "jdbc:postgresql://localhost/proiectusdb";
    private static final String driver = "org.postgresql.Driver";
    private static final String username = "admindb";
    private static final String password = "cmasana";

    /**
     * Realiza la conexión con la bbdd
     * @return devuelve un objeto de tipo Connection
     */
    public static Connection getConnection() {
        if (conn != null) {
            return conn;
        }
        return getDbConnection();
    }

    /**
     * Permite realizar una conexión con la bbdd utilizando una serie de parámetros
     * @return devuelve un objeto de tipo Connection
     */
    private static Connection getDbConnection() {
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
