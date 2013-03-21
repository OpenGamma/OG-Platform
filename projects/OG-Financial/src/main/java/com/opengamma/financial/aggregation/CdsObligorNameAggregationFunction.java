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

public class CdsObligorNameAggregationFunction extends AbstractCdsObligorAggregationFunction {

  private static final String NAME = "Reference Entity Names";

  public CdsObligorNameAggregationFunction(SecuritySource securitySource, OrganizationSource organizationSource) {
    super(NAME, securitySource, organizationSource);
  }

  @Override
  protected String extractDataUsingObligor(Obligor obligor) {
    return obligor.getObligorShortName();
  }
}