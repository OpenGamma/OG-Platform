/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

/**
 * Match details for a currency pair. It records whether the match was for the pair in the filter
 * or its reciprocal pair.
 */
public class CurrencyPairMatchDetails implements MatchDetails {

  /** False if the currency pair in the filter was matched, true if its reciprocal pair was matched. */
  private final boolean _inverse;

  /**
   * @param inverse false if the currency pair in the filter was matched, true if its reciprocal pair was matched
   */
  public CurrencyPairMatchDetails(boolean inverse) {
    _inverse = inverse;
  }

  /**
   * @return false if the currency pair in the filter was matched, true if its reciprocal pair was matched.
   */
  public boolean isInverse() {
    return _inverse;
  }
}
