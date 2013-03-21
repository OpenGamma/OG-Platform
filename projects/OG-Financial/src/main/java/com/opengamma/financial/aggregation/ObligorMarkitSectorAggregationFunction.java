/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.organization.Organization;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple aggregator function to allow positions to be aggregated by Markit sector.
 * This is only applicable to CDS securities and needs to be extracted
 * from the reference entity on the CDS. If applied to securities with no
 * reference entity, the result of {@link #classifyPosition(Position)} will be "N/A".
 */
public class ObligorMarkitSectorAggregationFunction extends AbstractCdsObligorAggregationFunction {

  public static final String NAME = "Markit Sectors";

  private final OrganizationSource _organizationSource;

  public ObligorMarkitSectorAggregationFunction(SecuritySource securitySource,
                                                OrganizationSource organizationSource) {
    super(securitySource, NAME);

    ArgumentChecker.notNull(organizationSource, "organizationSource");
    _organizationSource = organizationSource;
  }

  @Override
  protected String extractDataUsingRedCode(String redCode) {
    Organization organization = _organizationSource.getOrganizationByRedCode(redCode);
    return organization.getObligor().getSector().name();
  }
}