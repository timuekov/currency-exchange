package org.example.currencyexchange.util;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {

    static {
        try {
            // Явная загрузка драйвера SQLite для работы внутри Tomcat
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC Driver not found", e);
        }
    }

    private DatabaseUtil() {
    }

    public static Connection getConnection() throws SQLException {
        // 1. Проверяем: не указали ли нам путь к базе через переменную окружения?
        String dbPath = System.getenv("APP_DB_PATH");

        // 2. Если нет — берем файл currency.db из ресурсов проекта, как и раньше
        if (dbPath == null || dbPath.isBlank()) {
            URL resource = DatabaseUtil.class.getClassLoader().getResource("currency.db");
            if (resource == null) {
                throw new IllegalStateException("Файл базы данных currency.db не найден в ресурсах");
            }
            dbPath = new File(resource.getFile()).getAbsolutePath();
        }

        String url = "jdbc:sqlite:" + dbPath;

        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        }
        return connection;
    }
}