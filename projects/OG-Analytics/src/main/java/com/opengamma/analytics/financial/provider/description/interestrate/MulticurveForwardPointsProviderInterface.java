/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for forward points for one currency pair.
 */
public interface MulticurveForwardPointsProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  MulticurveForwardPointsProviderInterface copy();

  /**
   * Returns the forward points curve.
   * @return The curve.
   */
  DoublesCurve getForwardPointsCurve();

  /**
   * Returns the currency pair for which the points are valid.
   * @return The currency pair.
   */
  Pair<Currency, Currency> getCurrencyPair();

}
