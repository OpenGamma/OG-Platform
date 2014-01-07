/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing the present value sensitivity to a Forex currency pair volatility grid.
 */
public class PresentValueForexBlackVolatilityNodeSensitivityDataBundle {

  /**
   * The currency pair.
   */
  private final Pair<Currency, Currency> _currencyPair;
  /**
   * The volatility sensitivity as a matrix with same dimension as the input. The sensitivity value is in second/domestic currency.
   */
  private final DoubleMatrix2D _vega;
  private final DoubleMatrix1D _expiries;
  private final DoubleMatrix1D _delta;

  /**
   * Constructor with empty sensitivities for a given currency pair.
   * @param ccy1 First currency, not null
   * @param ccy2 Second currency, not null
   * @param numberExpiry The number of expiries, not negative
   * @param numberDelta The number of deltas, not negative
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle(final Currency ccy1, final Currency ccy2, final int numberExpiry, final int numberDelta) {
    ArgumentChecker.notNull(ccy1, "currency 1");
    ArgumentChecker.notNull(ccy2, "currency 2");
    ArgumentChecker.isTrue(numberExpiry >= 0, "number of expiries must be greater than or equal to zero");
    ArgumentChecker.isTrue(numberDelta >= 0, "number of deltas must be greater than or equal to zero");
    _currencyPair = ObjectsPair.of(ccy1, ccy2);
    _expiries = new DoubleMatrix1D(new double[numberExpiry]);
    _delta = new DoubleMatrix1D(new double[numberDelta]);
    _vega = new DoubleMatrix2D(numberExpiry, numberDelta);
  }

  /**
   * Constructor with initial sensitivities for a given currency pair.
   * @param ccy1 First currency, not null
   * @param ccy2 Second currency, not null
   * @param expiries The expiries for the vega matrix, not null
   * @param delta The deltas for the vega matrix, not null
   * @param vega The initial sensitivity, not null
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle(final Currency ccy1, final Currency ccy2, final DoubleMatrix1D expiries, final DoubleMatrix1D delta, final DoubleMatrix2D vega) {
    ArgumentChecker.notNull(ccy1, "currency 1");
    ArgumentChecker.notNull(ccy2, "currency 2");
    ArgumentChecker.notNull(expiries, "expiries");
    ArgumentChecker.notNull(delta, "strikes");
    ArgumentChecker.notNull(vega, "Matrix");
    ArgumentChecker.isTrue(vega.getNumberOfRows() == expiries.getNumberOfElements(), "Number of rows did not match number of expiries");
    ArgumentChecker.isTrue(vega.getNumberOfColumns() == delta.getNumberOfElements(), "Number of columns did not match number of delta");
    _currencyPair = ObjectsPair.of(ccy1, ccy2);
    _expiries = expiries;
    _delta = delta;
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

  public DoubleMatrix1D getExpiries() {
    return _expiries;
  }

  public DoubleMatrix1D getDelta() {
    return _delta;
  }

  /**
   * Compare two sensitivities with a given tolerance. Return "true" if the currency pairs are the same and all the sensitivities are within the tolerance.
   * @param value1 The first sensitivity.
   * @param value2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return The comparison flag.
   */
  public static boolean compare(final PresentValueForexBlackVolatilityNodeSensitivityDataBundle value1, final PresentValueForexBlackVolatilityNodeSensitivityDataBundle value2,
      final double tolerance) {
    if (!value1._currencyPair.equals(value2._currencyPair)) {
      return false;
    }
    if ((value1._expiries.getNumberOfElements() != value2._expiries.getNumberOfElements()) || (value1._delta.getNumberOfElements() != value2._delta.getNumberOfElements())) {
      return false;
    }
    for (int loopexp = 0; loopexp < value1._expiries.getNumberOfElements(); loopexp++) {
      for (int loopdel = 0; loopdel < value1._delta.getNumberOfElements(); loopdel++) {
        if (value1._vega.getEntry(loopexp, loopdel) - value2._vega.getEntry(loopexp, loopdel) > tolerance) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Computes the volatility sensitivities with respect to quoted data (ATM, RR, strangle) related to the (strike) node sensitivity.
   * @return The volatility quote sensitivities. The sensitivity figures are in the same currency as the node sensitivity.
   * The first dimension is the expiration and the second dimension the quotes: ATM, Risk Reversal, Strangle.
   */
  public PresentValueForexBlackVolatilityQuoteSensitivityDataBundle quoteSensitivity() {
    final double[][] vegaStrike = getVega().getData();
    final int nbStrike = vegaStrike[0].length;
    final double[][] result = new double[vegaStrike.length][nbStrike];
    final int nbQuote = (vegaStrike[0].length - 1) / 2;
    for (int loopexp = 0; loopexp < vegaStrike.length; loopexp++) {
      result[loopexp][0] = vegaStrike[loopexp][nbQuote]; // ATM
      for (int loopstrike = 0; loopstrike < nbQuote; loopstrike++) {
        result[loopexp][loopstrike + 1] = -vegaStrike[loopexp][loopstrike] / 2.0 + vegaStrike[loopexp][nbStrike - 1 - loopstrike] / 2.0; // Risk Reversal
        result[loopexp][loopstrike + nbQuote + 1] = vegaStrike[loopexp][loopstrike] + vegaStrike[loopexp][nbStrike - 1 - loopstrike]; // Strangle
        result[loopexp][0] += vegaStrike[loopexp][loopstrike] + vegaStrike[loopexp][nbStrike - 1 - loopstrike];
      }
    }
    return new PresentValueForexBlackVolatilityQuoteSensitivityDataBundle(_currencyPair.getFirst(), _currencyPair.getSecond(), _expiries.getData(), _delta.getData(), result);
  }

  //TODO Add possibility to add a sensitivity?

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currencyPair.hashCode();
    result = prime * result + _expiries.hashCode();
    result = prime * result + _delta.hashCode();
    result = prime * result + _vega.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle other = (PresentValueForexBlackVolatilityNodeSensitivityDataBundle) obj;
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_vega, other._vega)) {
      return false;
    }
    if (!ObjectUtils.equals(_expiries, other._expiries)) {
      return false;
    }
    if (!ObjectUtils.equals(_delta, other._delta)) {
      return false;
    }
    return true;
  }

}
