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

/**
 * A market data shift specification which performs no shift operation.
 */
public class NoOpMarketDataSelector implements MarketDataSelector {

  private static final NoOpMarketDataSelector INSTANCE = new NoOpMarketDataSelector();

  private NoOpMarketDataSelector() {}

  /**
   * Return the singleton instance.
   *
   * @return the singleton instance
   */
  public static NoOpMarketDataSelector getInstance() {
    return INSTANCE;
  }

  @Override
  public MarketDataSelector findMatchingSelector(StructureIdentifier structureId,
                                                 String calculationConfigurationName) {
    return null;
  }

  @Override
  public StructureType getApplicableStructureType() {
    return StructureType.NONE;
  }

  @Override
  public boolean containsShifts() {
    return false;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    return serializer.newMessage();
  }

  public static NoOpMarketDataSelector fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return INSTANCE;
  }
}
