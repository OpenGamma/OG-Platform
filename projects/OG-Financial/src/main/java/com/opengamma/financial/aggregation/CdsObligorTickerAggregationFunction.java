/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.obligor.definition.Obligor;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.security.SecuritySource;

/**
 * Aggregation function which which extract the ticker from the reference
 * entity on a CDS security.
 */
public class CdsObligorTickerAggregationFunction extends AbstractRedCodeHandlingCdsAggregationFunction<Obligor> {

  /**
   * Function name.
   */
  private static final String NAME = "Reference Entity Tickers";

  /**
   * Creates an instance.
   * 
   * @param securitySource  the security source, not null
   * @param organizationSource  the organization source, not null
   */
  public CdsObligorTickerAggregationFunction(SecuritySource securitySource, OrganizationSource organizationSource) {
    super(NAME, securitySource, new CdsObligorExtractor(organizationSource));
  }

  //-------------------------------------------------------------------------
  @Override
  protected String handleExtractedData(Obligor obligor) {
    return obligor.getObligorTicker();
  }

}
