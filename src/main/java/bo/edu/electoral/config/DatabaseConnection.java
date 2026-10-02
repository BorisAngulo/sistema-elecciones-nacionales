package bo.edu.electoral.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Conexión JDBC singleton hacia MySQL.
 * Lee las credenciales desde el archivo {@code .env} de la raíz del proyecto.
 *
 * <pre>
 * Connection cn = DatabaseConnection.getConnection();
 * </pre>
 */
public class DatabaseConnection {

    private static Connection connection;

    private DatabaseConnection() {
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = openConnection();
        }
        return connection;
    }

    /** Conexión independiente para una transacción de servicio. */
    public static Connection openConnection() throws SQLException {
            Map<String, String> env = cargarEnv();
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "No se encontró el driver de MySQL. Revisa la dependencia mysql-connector-j en pom.xml",
                        e);
            }

            String url = env.get("DB_URL");
            if (url == null || url.isBlank()) {
                String host = valor(env, "DB_HOST", "localhost");
                String port = valor(env, "DB_PORT", "3306");
                String name = valor(env, "DB_NAME", "elecciones_nacionales");
                url = "jdbc:mysql://" + host + ":" + port + "/" + name + "?characterEncoding=UTF-8&connectionTimeZone=LOCAL&sslMode=PREFERRED&allowPublicKeyRetrieval=true";
            }

            String user = env.get("DB_USER");
            String password = valor(env, "DB_PASSWORD", "");

            if (user == null || user.isBlank()) {
                throw new SQLException("Falta DB_USER en el archivo .env");
            }

            return DriverManager.getConnection(url, user, password);
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("No se pudo cerrar la conexión: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    private static String valor(Map<String, String> env, String clave, String porDefecto) {
        String valor = env.get(clave);
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        return valor;
    }

    private static Map<String, String> cargarEnv() throws SQLException {
        Path archivo = Path.of(System.getProperty("user.dir"), ".env");
        if (!Files.exists(archivo)) {
            throw new SQLException(
                    "No se encontró el archivo .env en " + archivo.toAbsolutePath()
                            + ". Copia .env.example a .env y completa usuario y contraseña.");
        }

        Map<String, String> env = new HashMap<>();
        try {
            for (String linea : Files.readAllLines(archivo)) {
                String recorte = linea.trim();
                if (recorte.isEmpty() || recorte.startsWith("#")) {
                    continue;
                }
                int separador = recorte.indexOf('=');
                if (separador <= 0) {
                    continue;
                }
                String clave = recorte.substring(0, separador).trim();
                String valor = recorte.substring(separador + 1).trim();
                if ((valor.startsWith("\"") && valor.endsWith("\""))
                        || (valor.startsWith("'") && valor.endsWith("'"))) {
                    valor = valor.substring(1, valor.length() - 1);
                }
                env.put(clave, valor);
            }
        } catch (IOException e) {
            throw new SQLException("Error al leer el archivo .env", e);
        }
        return env;
    }
}

