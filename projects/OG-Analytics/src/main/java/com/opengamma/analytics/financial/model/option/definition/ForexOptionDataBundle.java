/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexFlatProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * @param <T> The type of the volatility model
 * @deprecated Use {@link BlackForexFlatProviderInterface}
 */
@Deprecated
public abstract class ForexOptionDataBundle<T extends VolatilityModel<?>> extends YieldCurveBundle {
  /**
   * The volatility model for one currency pair.
   */
  private final T _volatilityModel;
  /**
   * The currency pair for which the volatility data are valid.
   */
  private final Pair<Currency, Currency> _currencyPair;

  public ForexOptionDataBundle(final YieldCurveBundle curves, final T volatilityModel, final Pair<Currency, Currency> currencyPair) {
    super(curves);
    ArgumentChecker.notNull(volatilityModel, "volatility model");
    ArgumentChecker.notNull(currencyPair, "currency pair");
    final Collection<Currency> currencies = getCurrencyMap().values();
    final Currency firstCurrency = currencyPair.getFirst();
    final Currency secondCurrency = currencyPair.getSecond();
    final FXMatrix fxMatrix = curves.getFxRates();
    ArgumentChecker.isTrue(fxMatrix.containsPair(firstCurrency, secondCurrency), "FX matrix does not contain rates for {} and {}", firstCurrency, secondCurrency);
    ArgumentChecker.isTrue(currencies.contains(firstCurrency), "Curve currency map does not contain currency {}; have {}", firstCurrency, currencies);
    ArgumentChecker.isTrue(currencies.contains(secondCurrency), "Curve currency map does not contain currency {}, have {}", secondCurrency, currencies);
    _volatilityModel = volatilityModel;
    _currencyPair = currencyPair;
  }

  /**
   * Create a copy of the bundle.
   * @return The bundle.
   */
  @Override
  public abstract ForexOptionDataBundle<T> copy();

  /**
   * Gets the underlying volatility model.
   * @return The underlying volatility model
   */
  public T getVolatilityModel() {
    return _volatilityModel;
  }

  /**
   * Returns the currency pair for which the Forex volatility data is valid.
   * @return The pair.
   */
  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
  }

  /**
   * Check that two given currencies are compatible with the data currency pair.
   * @param ccy1 One currency.
   * @param ccy2 The other currency.
   * @return True if the currencies match the pair (in any order) and False otherwise.
   */
  public boolean checkCurrencies(final Currency ccy1, final Currency ccy2) {
    if ((ccy1.equals(_currencyPair.getFirst())) && ccy2.equals(_currencyPair.getSecond())) {
      return true;
    }
    if ((ccy2.equals(_currencyPair.getFirst())) && ccy1.equals(_currencyPair.getSecond())) {
      return true;
    }
    return false;
  }

  protected YieldCurveBundle getCurvesCopy() {
    return super.copy();
  }

  public abstract ForexOptionDataBundle<T> with(YieldCurveBundle ycBundle);

  public abstract ForexOptionDataBundle<T> with(T volatilityModel);

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _volatilityModel.hashCode();
    result = prime * result + _currencyPair.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ForexOptionDataBundle<?> other = (ForexOptionDataBundle<?>) obj;
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatilityModel, other._volatilityModel)) {
      return false;
    }
    return true;
  }
}
