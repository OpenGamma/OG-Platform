/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.description;

import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Triple;

/**
 * Object representing a one point sensitivity to a forward curve. The underlying is a Pair of a Triple and a Double. 
 * The Triple represents the reference point as the start time, end time and accrual factor; the Double is the value at that point.
 */
public class MarketForwardSensitivity {

  /**
   * The point and value Pair.
   */
  private final ObjectsPair<Triple<Double, Double, Double>, Double> _underlying;

  /**
   * Constructor
   * @param point The pair.
   * @param value The sensitivity value.
   */
  public MarketForwardSensitivity(Triple<Double, Double, Double> point, Double value) {
    _underlying = new ObjectsPair<Triple<Double, Double, Double>, Double>(point, value);
  }

  public Triple<Double, Double, Double> getPoint() {
    return _underlying.first;
  }

  public Double getValue() {
    return _underlying.second;
  }

}
