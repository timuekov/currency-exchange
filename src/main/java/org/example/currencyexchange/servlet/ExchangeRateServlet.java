package org.example.currencyexchange.servlet;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.currencyexchange.dao.ExchangeRateDao;
import org.example.currencyexchange.dto.ErrorResponse;
import org.example.currencyexchange.model.ExchangeRate;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {


        String pathInfo = req.getPathInfo();


        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Не указана валютная пара!");
            objectMapper.writeValue(resp.getWriter(), errorResponse);

            return;
        }

        String pair = pathInfo.substring(1);

        if (!pair.matches("[A-Z]{6}")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Код валютной пары должен содержать 6 латинских символов в верхнем регистре!");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        String baseCurrencyCode = pair.substring(0, 3);
        String targetCurrencyCode = pair.substring(3, 6);

        if (baseCurrencyCode.equals(targetCurrencyCode)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Базовая и целевая валюты должны отличаться");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        try {
            Optional<ExchangeRate> exchangeRate = exchangeRateDao.findByPair(baseCurrencyCode, targetCurrencyCode);
            if (exchangeRate.isPresent()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), exchangeRate.get());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                ErrorResponse errorResponse = new ErrorResponse("Валютной пары с таким кодом не существует");
                objectMapper.writeValue(resp.getWriter(), errorResponse);
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse("Произошла ошибка! Попробуйте снова.");
            objectMapper.writeValue(resp.getWriter(), errorResponse);

        }
    }


    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();


        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Не указана валютная пара!");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }
        String pair = pathInfo.substring(1);

        if (!pair.matches("[A-Z]{6}")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Код валютной пары должен содержать 6 латинских символов в верхнем регистре!");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        String baseCurrencyCode = pair.substring(0, 3);
        String targetCurrencyCode = pair.substring(3, 6);

        if (baseCurrencyCode.equals(targetCurrencyCode)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Базовая и целевая валюты должны отличаться");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        String body = req.getReader().readLine();

        if (body == null || body.isEmpty()) {

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Поле rate обязательно");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;

        }

        if (!body.startsWith("rate=")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Поле rate обязательно");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;

        }

        String rate = body.substring(5);

        if (rate == null || rate.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Поле rate обязательно");
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

            Optional<ExchangeRate> updatedRate = exchangeRateDao.updateRate(
                    baseCurrencyCode,
                    targetCurrencyCode,
                    rateValue
            );
            if (updatedRate.isPresent()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), updatedRate.get());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                ErrorResponse errorResponse = new ErrorResponse("Курс обмена не найден!");
                objectMapper.writeValue(resp.getWriter(), errorResponse);
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Некорректное значение курса");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse("Произошла ошибка! Попробуйте снова.");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
        }


    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equals(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

}

