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

import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.util.ArgumentChecker;

/**
 * A MarketDataSelector which specifies a yield curve to be shifted. Note that this
 * class is not responsible for specifying the actual manipulation to be done.
 */
public class YieldCurveSelector implements MarketDataSelector {

  /**
   * The key indicating the yield curve that needs to be shifted.
   */
  private final YieldCurveKey _yieldCurveKey;

  private YieldCurveSelector(YieldCurveKey yieldCurveKey) {
    ArgumentChecker.notNull(yieldCurveKey, "yieldCurveKey");
    _yieldCurveKey = yieldCurveKey;
  }

  /**
   * Construct a specification for the supplied yield curve key.
   *
   * @param yieldCurveKey the key of the yield curve to be shifted, not null
   * @return a new MarketDataSelector for the yield curve
   */
  public static MarketDataSelector of(YieldCurveKey yieldCurveKey) {
    return new YieldCurveSelector(yieldCurveKey);
  }

  @Override
  public MarketDataSelector findMatchingSelector(StructureIdentifier structureId,
                                                 String calculationConfigurationName) {
    return StructureIdentifier.of(_yieldCurveKey).equals(structureId) ? this : null;
  }

  @Override
  public StructureType getApplicableStructureType() {
    return StructureType.YIELD_CURVE;
  }

  @Override
  public boolean containsShifts() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    YieldCurveSelector that = (YieldCurveSelector) o;
    return _yieldCurveKey.equals(that._yieldCurveKey);
  }

  @Override
  public int hashCode() {
    return _yieldCurveKey.hashCode();
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("yieldCurveKey", _yieldCurveKey);
    return msg;
  }

  public static MarketDataSelector fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return of(msg.getValue(YieldCurveKey.class, "yieldCurveKey"));
  }
}
