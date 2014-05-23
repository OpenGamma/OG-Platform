/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.security.SecuritySource;

/**
 * Aggregation function which which extract the short name from the reference
 * entity on a CDS security.
 */
public class CdsObligorNameAggregationFunction extends AbstractRedCodeHandlingCdsAggregationFunction<LegalEntity> {

  /**
   * Function name.
   */
  private static final String NAME = "Reference Entity Names";

  /**
   * Creates an instance.
   * 
   * @param securitySource  the security source, not null
   * @param legalEntitySource  the organization source, not null
   */
  public CdsObligorNameAggregationFunction(SecuritySource securitySource, LegalEntitySource legalEntitySource) {
    super(NAME, securitySource, new CdsObligorExtractor(legalEntitySource));
  }

  //-------------------------------------------------------------------------
  @Override
  protected String handleExtractedData(LegalEntity legalEntity) {
    return legalEntity.getName();
  }

}
