package org.example.currencyexchange.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.currencyexchange.dao.CurrencyDao;
import org.example.currencyexchange.dao.ExchangeRateDao;
import org.example.currencyexchange.dto.ErrorResponse;
import org.example.currencyexchange.dto.ExchangeResponse;
import org.example.currencyexchange.exception.CurrencyNotFoundException;
import org.example.currencyexchange.exception.ExchangeServiceException;
import org.example.currencyexchange.exception.RateNotFoundException;
import org.example.currencyexchange.service.ExchangeService;

import java.io.IOException;
import java.math.BigDecimal;


@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CurrencyDao currencyDao = new CurrencyDao();
    private final ExchangeRateDao exchangeRateDao = new ExchangeRateDao();

    private final ExchangeService exchangeService = new ExchangeService(currencyDao, exchangeRateDao);


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {


        String from = req.getParameter("from");
        String to = req.getParameter("to");
        String amount = req.getParameter("amount");

        if (from == null || to == null || amount == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Отсутствует обязательное поле!");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }
        if (from.isEmpty() || to.isEmpty() || amount.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Обязательное поле не может быть пустым");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        BigDecimal amountValue;


        try {
            amountValue = new BigDecimal(amount);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Некорректное значение суммы");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        if (amountValue.compareTo(BigDecimal.ZERO) < 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Сумма не может быть отрицательной");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        if (from.length() != 3 || to.length() != 3) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Код валюты должен состоять из 3 символов");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        from = from.toUpperCase();
        to = to.toUpperCase();

        ExchangeResponse exchangeResponse;

        try {
            exchangeResponse = exchangeService.exchange(from, to, amountValue);
        } catch (ExchangeServiceException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse("Произошла ошибка! Попробуйте снова.");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        } catch (CurrencyNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());

            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        } catch (RateNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;


        }
        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), exchangeResponse);
        return;


    }
}
