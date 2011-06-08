/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Set;

import com.opengamma.core.marketdatasnapshot.YieldCurveKey;

/**
 * A function which requires a structured block of market data
 */
public interface YieldCurveDataSourcingFunction extends CompiledFunctionDefinition {
  /**
   * @return the keys of the curves for which data is required
   */
  Set<YieldCurveKey> getYieldCurveKeys();
}
