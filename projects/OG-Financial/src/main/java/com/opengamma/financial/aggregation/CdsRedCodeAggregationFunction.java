/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;

/**
 * Simple aggregator function to allow positions to be aggregated by RED code. This is
 * generally only applicable to CDS-like securities, and if applied to securities with no
 * RED code, the result of {@link #classifyPosition(Position)} will be "N/A".
 */
public class CdsRedCodeAggregationFunction extends AbstractRedCodeHandlingCdsAggregationFunction<String> {

  /**
   * Function name.
   */
  private static final String NAME = "RED Codes";

  /**
   * Creates an instance.
   * 
   * @param securitySource  the security source, not null
   */
  public CdsRedCodeAggregationFunction(SecuritySource securitySource) {
    super(NAME, securitySource, new RedCodeHandler<String>() {
      @Override
      public String extract(String redCode) {
        return redCode;
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  protected String handleExtractedData(String redCode) {
    return redCode;
  }

}
