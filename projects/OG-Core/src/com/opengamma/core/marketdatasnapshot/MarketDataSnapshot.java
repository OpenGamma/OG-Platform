/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * A snapshot of market data taken at a particular time and potentially altered by hand
 */
@PublicSPI
public interface MarketDataSnapshot extends UniqueIdentifiable  {
  
  String getName();
  
  Map<UniqueIdentifier, ValueSnapshot> getValues();

  Map<Pair<String, Currency>, YieldCurveSnapshot> getYieldCurves();

  Map<Triple<String, Currency, Currency>, FXVolatilitySurfaceSnapshot> getFxVolatilitySurfaces();

}
