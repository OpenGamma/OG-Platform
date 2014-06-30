/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ExternalId;

/**
 * Exposure function that returns the trade attributes for a given trade. The external ids will be returned in the format
 * of TradeAttribute~&lt;KEY&gt;=&lt;VALUE&gt;.
 */
public class TradeAttributeExposureFunction implements ExposureFunction {

  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Trade Attribute";

  /**
   * Trade attribute identifier.
   */
  public static final String TRADE_ATTRIBUTE_IDENTIFIER = "TradeAttribute";
  
  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public List<ExternalId> getIds(Trade trade) {
    ImmutableList.Builder<ExternalId> builder = ImmutableList.builder();
    for (Map.Entry<String, String> entry: trade.getAttributes().entrySet()) {
      builder.add(ExternalId.of(TRADE_ATTRIBUTE_IDENTIFIER, entry.getKey() + "=" + entry.getValue()));
    }
    return builder.build();
  }
}
