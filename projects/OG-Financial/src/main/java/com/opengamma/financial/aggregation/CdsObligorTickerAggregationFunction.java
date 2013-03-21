/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.obligor.definition.Obligor;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.security.SecuritySource;

public class CdsObligorTickerAggregationFunction extends AbstractCdsObligorAggregationFunction {

  private static final String NAME = "Reference Entity Tickers";

  public CdsObligorTickerAggregationFunction(SecuritySource securitySource, OrganizationSource organizationSource) {
    super(NAME, securitySource, organizationSource);
  }

  @Override
  protected String extractDataUsingObligor(Obligor obligor) {
    return obligor.getObligorTicker();
  }
}