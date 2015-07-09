package com.opengamma.sesame.fxrates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.CurrenciesVisitor;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.marketdata.FxRateId;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

/**
 * Default implementation to obtain fx rates for a given Security against a base currency.
 */
public class DefaultFxRatesFn implements FxRatesFn {

  private final Currency _baseCurrency;

  /**
   * Create the function.
   *
   * @param baseCurrency base currency, not null
   */
  public DefaultFxRatesFn(Currency baseCurrency) {
    _baseCurrency = ArgumentChecker.notNull(baseCurrency, "baseCurrency");
  }

  @Override
  public Result<Map<Currency, Double>> getFxRates(Environment env, Security security) {

    Collection<Currency> currencies = CurrenciesVisitor.getCurrencies(security, null);
    Map<Currency, Double> rates = new HashMap<>();
    List<Result<?>> failures = new ArrayList<>();

    for (Currency currency : currencies) {
      if (currency == _baseCurrency) {
        rates.put(currency, 1.0);
        continue;
      }
      CurrencyPair currencyPair = CurrencyPair.of(_baseCurrency, currency);
      Result<Double> fxRateResult = env.getMarketDataBundle().get(FxRateId.of(currencyPair), Double.class);
      if (fxRateResult.isSuccess()) {
        rates.put(currency, fxRateResult.getValue());
      } else {
        failures.add(fxRateResult);
      }
    }

    if (failures.isEmpty()) {
      return Result.success(rates);
    } else {
      return Result.failure(failures);
    }

  }

  @Override
  public Result<Map<Currency, Double>> getFxRates(Environment env, TradeWrapper trade) {
    return getFxRates(env, trade.getSecurity());
  }
}
