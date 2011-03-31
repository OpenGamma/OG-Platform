/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;

/**
 * A snapshot of market data taken at a particular time and potentially altered by hand
 * 
 * It differs from ( LiveDataSnapshot + overrides ) in that market values can be overriden or updated separately for Yield Curves or other structured objects. 
 */
@PublicSPI
public interface StructuredMarketDataSnapshot extends UniqueIdentifiable {

  String getName();

  String getBasisViewName(); //TODO we need to record version information

  UnstructuredMarketDataSnapshot getGlobalValues();

  Map<YieldCurveKey, YieldCurveSnapshot> getYieldCurves();

}
