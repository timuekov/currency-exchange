package org.example.currencyexchange.servlet;

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
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private final CurrencyDao currencyDao = new CurrencyDao();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {


        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            ErrorResponse errorResponse = new ErrorResponse("Код валюты отсутствует в адресе");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;

        }

        String currencyCode = pathInfo.replaceFirst("/", "");

        if (!currencyCode.matches("[A-Z]{3}")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            ErrorResponse errorResponse = new ErrorResponse("Код валюты должен содержать 3 латинских символа в верхнем регистре!");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
            return;
        }
        try {
            Optional<Currency> currencyOptional = currencyDao.findByCode(currencyCode);

            if (currencyOptional.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
                ErrorResponse errorResponse = new ErrorResponse("Валюта не найдена");
                objectMapper.writeValue(resp.getWriter(), errorResponse);
            } else {
                resp.setStatus(HttpServletResponse.SC_OK); // 200
                objectMapper.writeValue(resp.getWriter(), currencyOptional.get());
            }

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            ErrorResponse errorResponse = new ErrorResponse("База данных недоступна");
            objectMapper.writeValue(resp.getWriter(), errorResponse);
        }
    }
}