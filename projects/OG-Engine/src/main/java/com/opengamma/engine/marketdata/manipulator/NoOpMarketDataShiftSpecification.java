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

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;

/**
 * A market data shift specification which performs no shift operation.
 */
public class NoOpMarketDataShiftSpecification implements MarketDataShiftSpecification {

  private static final NoOpMarketDataShiftSpecification INSTANCE = new NoOpMarketDataShiftSpecification();

  private NoOpMarketDataShiftSpecification() {}

  /**
   * Return the singleton instance.
   *
   * @return the singleton instance
   */
  public static NoOpMarketDataShiftSpecification getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean appliesTo(StructureIdentifier structureId,
                           String calculationConfigurationName) {
    return false;
  }

  @Override
  public StructureType getApplicableStructureType() {
    return StructureType.NONE;
  }

  @Override
  public StructuredMarketDataSnapshot apply(StructuredMarketDataSnapshot structuredSnapshot) {
    return null;
  }

  @Override
  public boolean containsShifts() {
    return false;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    return serializer.newMessage();
  }

  public static NoOpMarketDataShiftSpecification fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return INSTANCE;
  }
}
