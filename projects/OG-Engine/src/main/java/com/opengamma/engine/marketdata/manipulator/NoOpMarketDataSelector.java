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

/**
 * A market data selector which never performs an extraction operation.
 */
public final class NoOpMarketDataSelector implements MarketDataSelector {

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
  public DistinctMarketDataSelector findMatchingSelector(StructureIdentifier<?> structureId,
                                                         String calculationConfigurationName,
                                                         SelectorResolver resolver) {
    return null;
  }

  @Override
  public Set<StructureType> getApplicableStructureTypes() {
    return ImmutableSet.of();
  }

  @Override
  public boolean hasSelectionsDefined() {
    return false;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    return serializer.newMessage();
  }

  public static NoOpMarketDataSelector fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return INSTANCE;
  }
}
