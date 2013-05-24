/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.google.common.collect.ImmutableSet;
import com.opengamma.id.ExternalId;

/**
 * Selector for market data points in a dependency graph.
 */
public class MarketDataPointSelector extends ExactMatchMarketDataSelector<ExternalId> {

  private MarketDataPointSelector(ExternalId dataPointId) {
    this(StructureIdentifier.of(dataPointId));
  }

  private MarketDataPointSelector(StructureIdentifier<ExternalId> structureId) {
    super(structureId);
  }

  /**
   * Construct a selector for the supplied external id.
   *
   * @param dataPointId the external id of the market data point to be selected, not null
   * @return a new MarketDataSelector for themarket data point
   */
  public static DistinctMarketDataSelector of(ExternalId dataPointId) {
    return new MarketDataPointSelector(dataPointId);
  }

  @Override
  public Set<StructureType> getApplicableStructureTypes() {
    return ImmutableSet.of(StructureType.MARKET_DATA_POINT);
  }

  @SuppressWarnings("unchecked")
  public static MarketDataSelector fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new MarketDataPointSelector(msg.getValue(StructureIdentifier.class, "structureId"));
  }
}
