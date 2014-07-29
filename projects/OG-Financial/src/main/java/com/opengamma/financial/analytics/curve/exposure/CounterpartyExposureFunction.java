/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ExternalId;

/**
 * Exposure function that returns the counterparty {@link ExternalId} for a given trade.
 */
public final class CounterpartyExposureFunction implements ExposureFunction {

  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Counterparty";

  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public List<ExternalId> getIds(Trade trade) {
    return Lists.newArrayList(trade.getCounterparty().getExternalId());
  }
}
