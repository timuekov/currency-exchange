package org.example.currencyexchange.util;

import org.sqlite.SQLiteConfig;

import java.io.File;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;


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
        // Находим файл базы данных в папке ресурсов приложения
        URL resource = DatabaseUtil.class.getClassLoader().getResource("currency.db");
        if (resource == null) {
            throw new IllegalStateException("Файл базы данных currency.db не найден в ресурсах");
        }

        File dbFile = new File(resource.getFile());
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();


        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()){
            statement.execute("PRAGMA foreign_keys = ON;");
        }
        return connection;
    }
}

