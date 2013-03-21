/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;

/**
 * Simple aggregator function to allow positions to be aggregated by RED code. This is
 * generally only applicable to CDS securities, and if applied to securities with no
 * RED code, the result of {@link #classifyPosition(Position)} will be "N/A".
 */
public class ObligorRedCodeAggregationFunction extends AbstractCdsObligorAggregationFunction {

  private static final String NAME = "RED Codes";

  public ObligorRedCodeAggregationFunction(SecuritySource securitySource) {
    super(securitySource, NAME);
  }

  @Override
  protected String extractDataUsingRedCode(String redCode) {
    return redCode;
  }
}
