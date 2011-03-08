/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * A snapshot of market data taken at a particular time and potentially altered by hand
 */
@PublicSPI
public interface MarketDataSnapshot extends UniqueIdentifiable  {
  
  String getName();
  
  Map<Identifier, ValueSnapshot> getValues();

  Map<Pair<String, CurrencyUnit>, YieldCurveSnapshot> getYieldCurves();

  Map<Triple<String, CurrencyUnit, CurrencyUnit>, FXVolatilitySurfaceSnapshot> getFxVolatilitySurfaces();

}
