/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.id.ExternalId;
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
  private final StructureType _structureType;

  /**
   * The value of the key for the market data structure.
   */
  private final T _value;

  private StructureIdentifier(StructureType structureType, T value) {

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
  public static StructureIdentifier<YieldCurveKey> of(YieldCurveKey key) {
    return new StructureIdentifier<>(StructureType.YIELD_CURVE, key);
  }

  /**
   * Creates a structured identifier for a volatility surface key.
   *
   * @param key the volatility surface key, not null
   * @return a structured identifier for the volatility surface key
   */
  public static StructureIdentifier<VolatilitySurfaceKey> of(VolatilitySurfaceKey key) {
    return new StructureIdentifier<>(StructureType.VOLATILITY_SURFACE, key);
  }

  /**
   * Creates a structured identifier for a volatility cube key.
   *
   * @param key the volatility cube key, not null
   * @return a structured identifier for the volatility cube key
   */
  public static StructureIdentifier<VolatilityCubeKey> of(VolatilityCubeKey key) {
    return new StructureIdentifier<>(StructureType.VOLATILITY_CUBE, key);
  }

  /**
   * Creates a structured identifier for a market data point identified by an external id.
   *
   * @param key the market data point external id, not null
   * @return a structured identifier for the market data point external id
   */
  public static StructureIdentifier<ExternalId> of(ExternalId key) {
    return new StructureIdentifier<>(StructureType.MARKET_DATA_POINT, key);
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

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("structureType", _structureType);
    msg.add("value", _value);
    return msg;
  }

  public static StructureIdentifier<?> fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new StructureIdentifier<>(
        msg.getValue(StructureType.class, "structureType"), msg.getValue("value"));
  }
}
