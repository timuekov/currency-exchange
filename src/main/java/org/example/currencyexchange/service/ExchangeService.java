package org.example.currencyexchange.service;

import org.example.currencyexchange.dao.CurrencyDao;
import org.example.currencyexchange.dao.ExchangeRateDao;
import org.example.currencyexchange.dto.ExchangeResponse;
import org.example.currencyexchange.exception.CurrencyNotFoundException;
import org.example.currencyexchange.exception.ExchangeServiceException;
import org.example.currencyexchange.exception.RateNotFoundException;
import org.example.currencyexchange.model.Currency;
import org.example.currencyexchange.model.ExchangeRate;

import java.math.BigDecimal;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Optional;

public class ExchangeService {
    CurrencyDao currencyDao;
    ExchangeRateDao exchangeRateDao;


    public ExchangeService(CurrencyDao currencyDao, ExchangeRateDao exchangeRateDao) {
        this.currencyDao = currencyDao;
        this.exchangeRateDao = exchangeRateDao;


    }

    public ExchangeResponse exchange(String from, String to, BigDecimal amount) throws CurrencyNotFoundException, RateNotFoundException, ExchangeServiceException {
        Optional<Currency> baseCurrency;
        Optional<Currency> targetCurrency;

        try {
            baseCurrency = currencyDao.findByCode(from);
            targetCurrency = currencyDao.findByCode(to);

        } catch (SQLException e) {
            throw new ExchangeServiceException("Ошибка при получении валют");
        }

        if (baseCurrency.isEmpty()) {
            throw new CurrencyNotFoundException("Исходная валюта не найдена");
        }

        if (targetCurrency.isEmpty()) {
            throw new CurrencyNotFoundException("Целевая валюта не найдена");
        }

        Optional<ExchangeRate> directRate;
        Optional<ExchangeRate> reverseRate = Optional.empty();


        try {
            directRate = exchangeRateDao.findByPair(from, to);
            if (directRate.isEmpty()) {
                reverseRate = exchangeRateDao.findByPair(to, from);
            }
        } catch (SQLException e) {
            throw new ExchangeServiceException("Ошибка при получении курса");
        }


        BigDecimal rate;

        if (directRate.isPresent()) {
            rate = directRate.get().getRate();
        } else if (reverseRate.isPresent()) {
            rate = BigDecimal.ONE.divide(reverseRate.get().getRate(), 10, RoundingMode.HALF_UP);
        } else {
            Optional<ExchangeRate> usdToFrom;
            Optional<ExchangeRate> usdToTo = Optional.empty();

            try {
                usdToFrom = exchangeRateDao.findByPair("USD", from);
                usdToTo = exchangeRateDao.findByPair("USD", to);
            } catch (SQLException e) {
                throw new ExchangeServiceException("Ошибка при получении кросс-курса");
            }

            if (usdToFrom.isEmpty() || usdToTo.isEmpty()) {
                throw new RateNotFoundException("Не удалось найти подходящий курс для конвертации");
            }
            rate = usdToTo.get().getRate().divide(usdToFrom.get().getRate(), 10, RoundingMode.HALF_UP);

        }
        BigDecimal convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        ExchangeResponse exchangeResponse = new ExchangeResponse(baseCurrency.get(), targetCurrency.get(), rate, amount, convertedAmount);
        return exchangeResponse;
    }
}
