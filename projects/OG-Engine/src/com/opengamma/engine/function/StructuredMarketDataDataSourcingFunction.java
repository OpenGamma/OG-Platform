/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Set;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * A function which requires a structured block of market data
 */
public interface StructuredMarketDataDataSourcingFunction extends CompiledFunctionDefinition {
  /**
   * @return the elements for which data is provided by this function
   */
  Set<Pair<StructuredMarketDataKey, ValueSpecification>> getStructuredMarketData();
}
