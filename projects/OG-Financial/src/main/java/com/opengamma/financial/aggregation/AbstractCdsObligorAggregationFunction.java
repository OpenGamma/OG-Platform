/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.obligor.definition.Obligor;
import com.opengamma.core.organization.Organization;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.ArgumentChecker;

public abstract class AbstractCdsObligorAggregationFunction extends AbstractCdsAggregationFunction {

  protected final OrganizationSource _organizationSource;

  public AbstractCdsObligorAggregationFunction(String name, SecuritySource securitySource,
                                               OrganizationSource organizationSource) {
    super(name, securitySource);
    ArgumentChecker.notNull(organizationSource, "organizationSource");
    _organizationSource = organizationSource;
  }

  @Override
  protected String extractDataUsingRedCode(String redCode) {
    Organization organization = _organizationSource.getOrganizationByRedCode(redCode);
    return extractDataUsingObligor(organization.getObligor());
  }

  protected abstract String extractDataUsingObligor(Obligor obligor);
}