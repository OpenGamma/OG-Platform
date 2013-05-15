/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.obligor.definition.Obligor;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;

/**
 * Simple aggregator function to allow positions to be aggregated by Markit sector.
 * This is only applicable to CDS securities and needs to be extracted
 * from the reference entity on the CDS. If applied to securities with no
 * reference entity, the result of {@link #classifyPosition(Position)} will be "N/A".
 */
public class ObligorMarkitSectorAggregationFunction extends AbstractRedCodeHandlingCdsAggregationFunction<Obligor> {

  /**
   * Function name.
   */
  public static final String NAME = "Markit Sectors";

  /**
   * Creates an instance.
   * 
   * @param securitySource  the security source, not null
   * @param organizationSource  the organization source, not null
   */
  public ObligorMarkitSectorAggregationFunction(
      SecuritySource securitySource, OrganizationSource organizationSource) {
    super(NAME, securitySource, new CdsObligorExtractor(organizationSource));
  }

  //-------------------------------------------------------------------------
  @Override
  protected String handleExtractedData(Obligor obligor) {
    return obligor.getSector().name();
  }

}
