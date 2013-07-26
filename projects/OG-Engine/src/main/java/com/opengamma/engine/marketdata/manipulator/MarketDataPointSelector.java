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
public final class MarketDataPointSelector extends ExactMatchMarketDataSelector<ExternalId> {

  /**
   * Construct a selector for the supplied external id.
   *
   * @param dataPointId  the external id of the market data point to be selected, not null
   * @return a new MarketDataSelector for the market data point, not null
   */
  public static DistinctMarketDataSelector of(ExternalId dataPointId) {
    return new MarketDataPointSelector(dataPointId);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param dataPointId  the data point id, not null
   */
  private MarketDataPointSelector(ExternalId dataPointId) {
    this(StructureIdentifier.of(dataPointId));
  }

  /**
   * Creates an instance.
   * 
   * @param structureId  the structure id, not null
   */
  private MarketDataPointSelector(StructureIdentifier<ExternalId> structureId) {
    super(structureId);
  }

  //-------------------------------------------------------------------------
  @Override
  public Set<StructureType> getApplicableStructureTypes() {
    return ImmutableSet.of(StructureType.MARKET_DATA_POINT);
  }

  @SuppressWarnings("unchecked")
  public static MarketDataSelector fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new MarketDataPointSelector(msg.getValue(StructureIdentifier.class, "structureId"));
  }

}
