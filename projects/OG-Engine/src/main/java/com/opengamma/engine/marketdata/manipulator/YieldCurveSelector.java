/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;

/**
 * A MarketDataSelector which specifies a yield curve to be shifted. Note that this
 * class is not responsible for specifying the actual manipulation to be done.
 */
public final class YieldCurveSelector extends ExactMatchMarketDataSelector<YieldCurveKey> {

  private static final String STRUCTURE_ID = "structureId";

  private YieldCurveSelector(YieldCurveKey yieldCurveKey) {
    this(StructureIdentifier.of(yieldCurveKey));
  }

  private YieldCurveSelector(StructureIdentifier<YieldCurveKey> structureId) {
    super(structureId);
  }

  /**
   * Construct a specification for the supplied yield curve key.
   *
   * @param yieldCurveKey the key of the yield curve to be shifted, not null
   * @return a new MarketDataSelector for the yield curve
   */
  public static DistinctMarketDataSelector of(YieldCurveKey yieldCurveKey) {
    return new YieldCurveSelector(yieldCurveKey);
  }

  @Override
  public Set<StructureType> getApplicableStructureTypes() {
    return ImmutableSet.of(StructureType.YIELD_CURVE);
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, STRUCTURE_ID, null, getStructureId());
    return msg;
  }

  @SuppressWarnings("unchecked")
  public static MarketDataSelector fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    StructureIdentifier structureId = deserializer.fieldValueToObject(StructureIdentifier.class,
                                                                      msg.getByName(STRUCTURE_ID));
    return new YieldCurveSelector(structureId);
  }
}
