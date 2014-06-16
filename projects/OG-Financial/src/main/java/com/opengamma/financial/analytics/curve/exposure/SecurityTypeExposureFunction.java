/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.position.Trade;
import com.opengamma.id.ExternalId;

/**
 * Exposure function that returns the security type for a given trade.
 */
public class SecurityTypeExposureFunction implements ExposureFunction {

  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Security Type";

  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public List<ExternalId> getIds(Trade trade) {
    return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, trade.getSecurity().getSecurityType()));
  }
}
