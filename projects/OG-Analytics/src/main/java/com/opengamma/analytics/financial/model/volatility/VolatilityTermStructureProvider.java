/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;


/**
 * Interface for a VolatilityTermStructureProvider, i.e. something that produces a  VolatilityTermStructure (usually based on some data it is given)
 * @param <T> the type of data
 */
public interface VolatilityTermStructureProvider<T> {

  /**
   * Turn data into a VolatilityTermStructure
   * @param data the necessary data to produce a VolatilityTermStructure
   * @return a VolatilityTermStructure
   */
  VolatilityTermStructure evaluate(final T data);

}
