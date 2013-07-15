/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.io.Serializable;

/**
 * Base class for different kinds of market data keys.
 */
public abstract class StructuredMarketDataKey implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Visitor interface for working with the different key implementations
   */
  public interface Visitor<T> {

    T visitYieldCurveKey(YieldCurveKey key);

    T visitVolatilitySurfaceKey(VolatilitySurfaceKey key);

    T visitVolatilityCubeKey(VolatilityCubeKey key);

    T visitCurveKey(CurveKey curveKey);
  }

  /**
   * Applies the visitor to this key.
   * 
   * @param <T> the return type of the visitor
   * @param visitor the visitor to apply, not null
   * @return the return value of the visitor
   */
  public abstract <T> T accept(Visitor<T> visitor);

}
