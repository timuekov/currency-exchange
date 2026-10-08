package org.example.currencyexchange.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.currencyexchange.dao.CurrencyDao;
import org.example.currencyexchange.dao.ExchangeRateDao;
import org.example.currencyexchange.dto.ErrorResponse;
import org.example.currencyexchange.model.Currency;
import org.example.currencyexchange.model.ExchangeRate;
import org.sqlite.SQLiteException;

import java.math.BigDecimal;
import java.sql.SQLException;

import org.sqlite.SQLiteErrorCode;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
    private final CurrencyDao currencyDao = new CurrencyDao();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {


        try {
            List<ExchangeRate> exchangeRates = exchangeRateDao.findAll();
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), exchangeRates);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse("База данных недоступна");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {


        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");

        if (baseCurrencyCode == null || targetCurrencyCode == null || rate == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Отсутствует обязательное поле!");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;

        }

        if (baseCurrencyCode.isEmpty() || targetCurrencyCode.isEmpty() || rate.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Обязательное поле не может быть пустым");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        BigDecimal rateValue;

        try {
            rateValue = new BigDecimal(rate);

            if (rateValue.compareTo(BigDecimal.ZERO) <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                ErrorResponse errorResponse = new ErrorResponse("Курс должен быть больше нуля");
                objectMapper.writeValue(resp.getWriter(), errorResponse);
                return;
            }

            Optional<Currency> baseCurrency = currencyDao.findByCode(baseCurrencyCode);
            Optional<Currency> targetCurrency = currencyDao.findByCode(targetCurrencyCode);


            if (baseCurrency.isEmpty() || targetCurrency.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                ErrorResponse errorResponse = new ErrorResponse("Одна или обе валюты не найдены");
                objectMapper.writeValue(resp.getWriter(), errorResponse);
                return;
            }

            ExchangeRate exchangeRate = new ExchangeRate(
                    baseCurrency.get(),
                    targetCurrency.get(),
                    rateValue
            );

            exchangeRateDao.save(exchangeRate);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), exchangeRate);


        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Некорректное значение курса");
            objectMapper.writeValue(resp.getWriter(), errorResponse);

        } catch (SQLiteException e) {
            if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE
                    || (e.getMessage() != null
                    && e.getMessage().contains("UNIQUE constraint failed"))) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                ErrorResponse errorResponse = new ErrorResponse("Такая валютная пара уже существует");
                objectMapper.writeValue(resp.getWriter(), errorResponse);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse errorResponse = new ErrorResponse("Произошла ошибка! Попробуйте снова.");
                objectMapper.writeValue(resp.getWriter(), errorResponse);
            }


        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse("Произошла ошибка! Попробуйте снова.");
            objectMapper.writeValue(resp.getWriter(), errorResponse);

        }

    }
}
