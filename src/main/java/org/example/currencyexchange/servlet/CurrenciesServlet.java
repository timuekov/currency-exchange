package org.example.currencyexchange.servlet;

import org.sqlite.SQLiteException;
import org.sqlite.SQLiteErrorCode;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.currencyexchange.dao.CurrencyDao;
import org.example.currencyexchange.dto.ErrorResponse;
import org.example.currencyexchange.model.Currency;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final CurrencyDao currencyDao = new CurrencyDao();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {


        try {
            List<Currency> currencies = currencyDao.findAll();
            resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
            objectMapper.writeValue(resp.getWriter(), currencies);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            ErrorResponse errorResponse = new ErrorResponse("База данных недоступна");
            objectMapper.writeValue(resp.getWriter(), errorResponse);

        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {


        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        if (name == null || code == null || sign == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Отсутствует обязательное поле");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;

        } else if (name.isEmpty() || code.isEmpty() || sign.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Обязательное поле не может быть пустым");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        if (!code.matches("[A-Z]{3}")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse("Код валюты должен состоять из 3 заглавных латинских букв!");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }

        Currency currency = new Currency(code, name, sign);
        try {
            currencyDao.save(currency);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), currency);
        } catch (SQLiteException e) {
            if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE
                    || (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed"))) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                ErrorResponse errorResponse = new ErrorResponse("Валюта с таким кодом уже существует");
                objectMapper.writeValue(resp.getWriter(), errorResponse);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse errorResponse = new ErrorResponse("Ошибка при работе с базой данных");
                objectMapper.writeValue(resp.getWriter(), errorResponse);
                System.out.println(e.getMessage());
                System.out.println(e.getClass());

            }


        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse("Ошибка при работе с базой данных");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            System.out.println(e.getMessage());
            System.out.println(e.getClass());


        }

    }
}