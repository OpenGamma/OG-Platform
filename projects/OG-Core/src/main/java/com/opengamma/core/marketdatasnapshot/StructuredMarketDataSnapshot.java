/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;

/**
 * A snapshot of market data taken at a particular instant, potentially altered by hand.
 * <p>
 * This snapshot differs from {@code ( LiveDataSnapshot + overrides )} in that market
 * values can be overridden or updated separately for yield curves or other structured objects.
 */
@PublicSPI
public interface StructuredMarketDataSnapshot extends UniqueIdentifiable {

  /**
   * Gets the name of the snapshot.
   * 
   * @return the name
   */
  String getName();

  /**
   * Gets the basis view name.
   * 
   * @return the basis view name
   */
  String getBasisViewName(); // TODO we need to record version information

  /**
   * Gets the global set of values.
   * 
   * @return the override values
   */
  UnstructuredMarketDataSnapshot getGlobalValues();

  /**
   * Gets the yield curves.
   * 
   * @return the yield curves
   */
  Map<YieldCurveKey, YieldCurveSnapshot> getYieldCurves();

  /**
   * Gets the yield curves.
   *
   * @return the yield curves
   */
  Map<CurveKey, CurveSnapshot> getCurves();

  /**
   * Gets the volatility cubes.
   * 
   * @return the volatility cubes
   */
  Map<VolatilityCubeKey, VolatilityCubeSnapshot> getVolatilityCubes();

  /**
   * Gets the volatility surfaces.
   * 
   * @return the volatility surfaces
   */
  Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> getVolatilitySurfaces();

  /**
   * Gets the surfaces.
   * 
   * @return the surfaces
   */
  Map<SurfaceKey, SurfaceSnapshot> getSurfaces();

  /**
   * Gets the valuation time of the snapshot
   * 
   * @return the valuation time of the snapshot, or null if
   * no valuation time could be inferred from the snapshot
   */
  Instant getValuationTime();



}
