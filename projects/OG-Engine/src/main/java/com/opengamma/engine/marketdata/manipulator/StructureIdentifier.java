/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.util.ArgumentChecker;

/**
 * Wraps a structured object's identifier allowing comparison between them. (For instance,
 * a yield curve wil be identified by a YieldCurveKey whereas a VolatilityCube by a
 * VolatilityCubeKey but there is no common interface between the two.)
 *
 * @param <T> the underlying type of the key used
 */
public class StructureIdentifier<T> {

  /**
   * The type of market data structure which is to be manipulated.
   */
  private final MarketDataShiftSpecification.StructureType _structureType;

  /**
   * The value of the key for the market data structure.
   */
  private final T _value;

  private StructureIdentifier(MarketDataShiftSpecification.StructureType structureType, T value) {

    ArgumentChecker.notNull(structureType, "structureType");
    ArgumentChecker.notNull(value, "value");
    _structureType = structureType;
    _value = value;
  }

  /**
   * Creates a structured identifier for a yield curve key.
   *
   * @param key the yield curve key, not null
   * @return a structured identifier for the yield curve key
   */
  public static StructureIdentifier of(YieldCurveKey key) {
    return new StructureIdentifier<>(MarketDataShiftSpecification.StructureType.YIELD_CURVE, key);
  }

  /**
   * Creates a structured identifier for a volatility surface key.
   *
   * @param key the volatility surface key, not null
   * @return a structured identifier for the volatility surface key
   */
  public static StructureIdentifier of(VolatilitySurfaceKey key) {
    return new StructureIdentifier<>(MarketDataShiftSpecification.StructureType.VOLATILITY_SURFACE, key);
  }

  /**
   * Creates a structured identifier for a volatility cube key.
   *
   * @param key the volatility cube key, not null
   * @return a structured identifier for the volatility cube key
   */
  public static StructureIdentifier of(VolatilityCubeKey key) {
    return new StructureIdentifier<>(MarketDataShiftSpecification.StructureType.VOLATILITY_CUBE, key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StructureIdentifier that = (StructureIdentifier) o;
    return _structureType == that._structureType && _value.equals(that._value);
  }

  @Override
  public int hashCode() {
    int result = _structureType.hashCode();
    result = 31 * result + _value.hashCode();
    return result;
  }
}
