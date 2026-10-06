package org.example.currencyexchange.dao;

import org.example.currencyexchange.model.Currency;
import org.example.currencyexchange.model.ExchangeRate;
import org.example.currencyexchange.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {

    private final CurrencyDao currencyDao = new CurrencyDao();

    public List<ExchangeRate> findAll() throws SQLException {

        String query = "SELECT ExchangeRates.ID, ExchangeRates.Rate, "
                + "base.ID AS base_id, base.Code AS base_code, base.FullName AS base_full_name, base.Sign AS base_sign, "
                + "target.ID AS target_id, target.Code AS target_code, target.FullName AS target_full_name, target.Sign AS target_sign "
                + " FROM ExchangeRates "
                + "JOIN Currencies AS base ON ExchangeRates.BaseCurrencyId = base.ID "
                + "JOIN Currencies AS target ON ExchangeRates.TargetCurrencyId = target.ID";

        List<ExchangeRate> exchangeRates = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Currency baseCurrency = new Currency(

                        resultSet.getInt("base_id"),
                        resultSet.getString("base_code"),
                        resultSet.getString("base_full_name"),
                        resultSet.getString("base_sign")
                );

                Currency targetCurrency = new Currency(
                        resultSet.getInt("target_id"),
                        resultSet.getString("target_code"),
                        resultSet.getString("target_full_name"),
                        resultSet.getString("target_sign")
                );

                ExchangeRate exchangeRate = new ExchangeRate(
                        resultSet.getInt("ID"),
                        baseCurrency,
                        targetCurrency,
                        resultSet.getBigDecimal("Rate")

                );
                exchangeRates.add(exchangeRate);

            }
        }

        return exchangeRates;
    }

    public Optional<ExchangeRate> findByPair(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        String query = "SELECT ExchangeRates.ID, ExchangeRates.Rate, "
                + "base.ID AS base_id, base.Code AS base_code, base.FullName AS base_full_name, base.Sign AS base_sign, "
                + "target.ID AS target_id, target.Code AS target_code, target.FullName AS target_full_name, target.Sign AS target_sign "
                + " FROM ExchangeRates "
                + "JOIN Currencies AS base ON ExchangeRates.BaseCurrencyId = base.ID "
                + "JOIN Currencies AS target ON ExchangeRates.TargetCurrencyId = target.ID "
                + "WHERE base.Code = ? AND target.Code = ?";


        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Currency baseCurrency = new Currency(
                            resultSet.getInt("base_id"),
                            resultSet.getString("base_code"),
                            resultSet.getString("base_full_name"),
                            resultSet.getString("base_sign")
                    );

                    Currency targetCurrency = new Currency(
                            resultSet.getInt("target_id"),
                            resultSet.getString("target_code"),
                            resultSet.getString("target_full_name"),
                            resultSet.getString("target_sign")

                    );

                    ExchangeRate exchangeRate = new ExchangeRate(
                            resultSet.getInt("ID"),
                            baseCurrency,
                            targetCurrency,
                            resultSet.getBigDecimal("Rate")
                    );
                    return Optional.of(exchangeRate);

                }
            }

        }
        return Optional.empty();
    }

    public ExchangeRate save(ExchangeRate exchangeRate) throws SQLException {

        String query = "INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate) VALUES (?, ?, ?)";

        try (
                Connection connection = DatabaseUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, exchangeRate.getBaseCurrency().getId());
            statement.setInt(2, exchangeRate.getTargetCurrency().getId());
            statement.setBigDecimal(3, exchangeRate.getRate());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    exchangeRate.setId(generatedKeys.getInt(1));
                }
            }
        }
        return exchangeRate;
    }

    public Optional<ExchangeRate> updateRate(
            String baseCurrencyCode,
            String targetCurrencyCode,
            BigDecimal newRate)
            throws SQLException {


        Optional<Currency> baseCurrency = currencyDao.findByCode(baseCurrencyCode);
        Optional<Currency> targetCurrency = currencyDao.findByCode(targetCurrencyCode);

        String query = "UPDATE ExchangeRates "
                + "SET Rate = ? "
                + "WHERE BaseCurrencyId = ? AND TargetCurrencyId = ?";


        if (baseCurrency.isEmpty() || targetCurrency.isEmpty()) {
            return Optional.empty();
        }

        try (
                Connection connection = DatabaseUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBigDecimal(1, newRate);
            statement.setInt(2, baseCurrency.get().getId());
            statement.setInt(3, targetCurrency.get().getId());

            int updatedRows = statement.executeUpdate();

            if (updatedRows == 0) {
                return Optional.empty();
            }
            return findByPair(baseCurrencyCode, targetCurrencyCode);


        }
    }


}

