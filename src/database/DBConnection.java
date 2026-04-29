package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String JAVA_URL = "jdbc:mysql://localhost:3306/java";
    private static final String HEALTH_URL = "jdbc:mysql://localhost:3306/health_assist";
    private static final String USER = "root";
    private static final String PASSWORD = "godeater164";

    public static Connection connectJava() {
        return getConnection(JAVA_URL);
    }

    public static Connection connectHealth() {
        return getConnection(HEALTH_URL);
    }

    public static Connection connect() {
        return connectHealth();
    }

    private static Connection getConnection(String url) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, USER, PASSWORD);
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("DB Connection failed to " + url + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

