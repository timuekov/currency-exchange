package org.example.currencyexchange.dao;

import org.example.currencyexchange.model.Currency;
import org.example.currencyexchange.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao {

    public List<Currency> findAll() throws SQLException {
        String query = "SELECT id, code, fullname, sign FROM currencies";
        List<Currency> currencies = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Currency currency = new Currency(
                        resultSet.getInt("id"),
                        resultSet.getString("code"),
                        resultSet.getString("fullname"),
                        resultSet.getString("sign")
                );
                currencies.add(currency);
            }
        }

        return currencies;
    }

    public Optional<Currency> findByCode(String code) throws SQLException {
        String query = "SELECT id, code, fullname, sign FROM currencies WHERE UPPER(code) = UPPER(?)";

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Currency currency = new Currency(
                            resultSet.getInt("id"),
                            resultSet.getString("code"),
                            resultSet.getString("fullname"),
                            resultSet.getString("sign")
                    );
                    return Optional.of(currency);
                }
            }
        }

        return Optional.empty();
    }
    public Currency save(Currency currency) throws SQLException {

        String query = "INSERT INTO currencies (code, fullname, sign) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {


            statement.setString(1, currency.getCode());
            statement.setString(2, currency.getFullName());
            statement.setString(3, currency.getSign());

            statement.executeUpdate();


            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    currency.setId(generatedKeys.getInt(1));
                }
            }
        }

        return currency;
    }
}