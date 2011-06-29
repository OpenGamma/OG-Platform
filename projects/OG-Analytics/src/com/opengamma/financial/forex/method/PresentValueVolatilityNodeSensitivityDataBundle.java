/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing the present value sensitivity to a Forex currency pair volatility grid.
 */
public class PresentValueVolatilityNodeSensitivityDataBundle {

  /**
   * The currency pair.
   */
  private final Pair<Currency, Currency> _currencyPair;
  /**
   * The volatility sensitivity as a matrix with same dimension as the input. The sensitivity value is in second/domestic currency.
   */
  private final DoubleMatrix2D _vega;

  /**
   * Constructor with empty sensitivities for a given currency pair.
   * @param ccy1 First currency.
   * @param ccy2 Second currency.
   * @param numberExpiry The number of expiries.
   * @param numberStrike The number of strikes.
   */
  public PresentValueVolatilityNodeSensitivityDataBundle(final Currency ccy1, final Currency ccy2, int numberExpiry, int numberStrike) {
    _currencyPair = ObjectsPair.of(ccy1, ccy2);
    _vega = new DoubleMatrix2D(numberExpiry, numberStrike);
  }

  /**
   * Constructor with initial sensitivities for a given currency pair.
   * @param ccy1 First currency.
   * @param ccy2 Second currency.
   * @param vega The initial sensitivity.
   */
  public PresentValueVolatilityNodeSensitivityDataBundle(final Currency ccy1, final Currency ccy2, final DoubleMatrix2D vega) {
    Validate.notNull(vega, "Matrix");
    _currencyPair = ObjectsPair.of(ccy1, ccy2);
    _vega = vega;
  }

  /**
   * Gets the currency pair.
   * @return The currency pair.
   */
  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
  }

  /**
   * Gets the volatility sensitivity (vega) map.
   * @return The sensitivity.
   */
  public DoubleMatrix2D getVega() {
    return _vega;
  }

  //TODO Add possibility to add a sensitivity?

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currencyPair.hashCode();
    result = prime * result + _vega.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PresentValueVolatilityNodeSensitivityDataBundle other = (PresentValueVolatilityNodeSensitivityDataBundle) obj;
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_vega, other._vega)) {
      return false;
    }
    return true;
  }

}
