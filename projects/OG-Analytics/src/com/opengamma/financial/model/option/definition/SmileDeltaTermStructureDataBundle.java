/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.method.FXMatrix;
import com.opengamma.financial.forex.method.YieldCurveWithFXBundle;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing the data required to price instruments with the volatility delta and time dependent.
 */
public class SmileDeltaTermStructureDataBundle extends YieldCurveWithFXBundle {

  /**
   * The smile parameters for one currency pair.
   */
  private final SmileDeltaTermStructureParameter _smile;
  /**
   * The currency pair for which the smile data is valid.
   */
  private final Pair<Currency, Currency> _currencyPair;

  /**
   * Constructor from the smile parameters and the curves.
   * @param ycBundle The curves bundle.
   * @param fxRates The FX cross rate matrix.
   * @param smile The smile parameters.
   * @param currencyPair The currency pair for which the smile is valid.
   */
  public SmileDeltaTermStructureDataBundle(final YieldCurveBundle ycBundle, final FXMatrix fxRates, final SmileDeltaTermStructureParameter smile, Pair<Currency, Currency> currencyPair) {
    super(fxRates, ycBundle);
    Validate.notNull(smile, "Smile parameters");
    //TODO: check rate is available for currency pair.
    _smile = smile;
    _currencyPair = currencyPair;
  }

  /**
   * Gets the smile parameters.
   * @return The smile parameters.
   */
  public SmileDeltaTermStructureParameter getSmile() {
    return _smile;
  }

  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
  }

  /**
   * Get the volatility at a given time/strike/forward taking the currency pair order in account. See {@link SmileDeltaTermStructureParameter} for the interpolation/extrapolation. 
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The time to expiration.
   * @param strike The strike.
   * @param forward The forward.
   * @return The volatility.
   */
  public double getVolatility(final Currency ccy1, final Currency ccy2, double time, double strike, double forward) {
    if ((ccy1 == _currencyPair.getFirst()) && (ccy2 == _currencyPair.getSecond())) {
      return _smile.getVolatility(time, strike, forward);
    }
    if ((ccy2 == _currencyPair.getFirst()) && (ccy1 == _currencyPair.getSecond())) {
      return _smile.getVolatility(time, 1.0 / strike, 1.0 / forward);
    }
    Validate.isTrue(false, "Currencies not compatible with smile data");
    return 0.0;
  }

  /**
   * Computes the volatility and the volatility sensitivity with respect to the volatility data points.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The time to expiration.
   * @param strike The strike.
   * @param forward The forward.
   * @param bucketSensitivity The array is changed by the method. The array should have the correct size. After the methods, it contains the volatility sensitivity to the data points. 
   * Only the lines of impacted dates are changed. The input data on the other lines will not be changed.
   * @return The volatility.
   */
  public double getVolatility(final Currency ccy1, final Currency ccy2, double time, double strike, double forward, double[][] bucketSensitivity) {
    if ((ccy1 == _currencyPair.getFirst()) && (ccy2 == _currencyPair.getSecond())) {
      return _smile.getVolatility(time, strike, forward, bucketSensitivity);
    }
    if ((ccy2 == _currencyPair.getFirst()) && (ccy1 == _currencyPair.getSecond())) {
      return _smile.getVolatility(time, 1.0 / strike, 1.0 / forward, bucketSensitivity);
    }
    Validate.isTrue(false, "Currencies not compatible with smile data");
    return 0.0;
  }

  /**
   * Check that two given currencies are compatible with the data currency pair.
   * @param ccy1 One currency.
   * @param ccy2 The other currency.
   * @return True if the currencies match the pair (in any order) and False otherwise.
   */
  public boolean checkCurrencies(Currency ccy1, Currency ccy2) {
    if ((ccy1 == _currencyPair.getFirst()) && (ccy2 == _currencyPair.getSecond())) {
      return true;
    }
    if ((ccy2 == _currencyPair.getFirst()) && (ccy1 == _currencyPair.getSecond())) {
      return true;
    }
    return false;
  }

}
