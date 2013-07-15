/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.obligor.definition.Obligor;
import com.opengamma.core.organization.Organization;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Class uses an organization source to extract the Obligor from a RED code.
 */
public class CdsObligorExtractor implements RedCodeHandler<Obligor> {

  /**
   * The organization source to look up the Obligor with.
   */
  private final OrganizationSource _organizationSource;

  /**
   * Create the extractor ensuring the organization source is not null.
   *
   * @param organizationSource the organization source, must not be null
   */
  public CdsObligorExtractor(OrganizationSource organizationSource) {
    ArgumentChecker.notNull(organizationSource, "organizationSource");
    _organizationSource = organizationSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Extract the Obligor from the RED code if it can be found.
   *
   * @param redCode  the RED code to extract the Obligor from, not null
   * @return the Obligor if found, null otherwise
   */
  @Override
  public Obligor extract(String redCode) {
    Organization organization = _organizationSource.getOrganizationByRedCode(redCode);
    return organization == null ? null : organization.getObligor();
  }

}
