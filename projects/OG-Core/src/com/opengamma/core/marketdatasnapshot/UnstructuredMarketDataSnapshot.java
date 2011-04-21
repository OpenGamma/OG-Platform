/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

/**
 * A snapshot of market data taken at a particular instant, potentially altered by hand,
 * that should be applied to computations in some scope, such as a yield curve.
 */
public interface UnstructuredMarketDataSnapshot {

  /**
   * The snapshot values.
   * 
   * @return the values
   */
  Map<MarketDataValueSpecification, ValueSnapshot> getValues();

}
