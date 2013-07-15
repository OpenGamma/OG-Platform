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
 * Aggregation function which which extract the short name from the reference
 * entity on a CDS security.
 */
public class CdsObligorNameAggregationFunction extends AbstractRedCodeHandlingCdsAggregationFunction<Obligor> {

  /**
   * Function name.
   */
  private static final String NAME = "Reference Entity Names";

  /**
   * Creates an instance.
   * 
   * @param securitySource  the security source, not null
   * @param organizationSource  the organization source, not null
   */
  public CdsObligorNameAggregationFunction(SecuritySource securitySource, OrganizationSource organizationSource) {
    super(NAME, securitySource, new CdsObligorExtractor(organizationSource));
  }

  //-------------------------------------------------------------------------
  @Override
  protected String handleExtractedData(Obligor obligor) {
    return obligor.getObligorShortName();
  }

}
