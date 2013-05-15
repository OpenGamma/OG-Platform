/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.obligor.definition.Obligor;
import com.opengamma.core.organization.Organization;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.id.ExternalId;

/**
 * Simple aggregator function to allow positions to be aggregated by debt seniority. This is
 * generally only applicable to CDS securities, and if applied to securities with no
 * debt seniority, the result of {@link #classifyPosition(Position)} will be "N/A".
 */
public class CdsOptionReferenceNameAggregationFunction extends AbstractCdsOptionAggregationFunction<Obligor> {

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
  public CdsOptionReferenceNameAggregationFunction(final SecuritySource securitySource, final OrganizationSource organizationSource) {
    super(NAME, securitySource, new CdsOptionValueExtractor<Obligor>() {
      @Override
      public Obligor extract(CreditDefaultSwapOptionSecurity cdsOption) {
        ExternalId underlyingId = cdsOption.getUnderlyingId();
        Security underlying = securitySource.getSingle(underlyingId.toBundle());
        if (underlying instanceof AbstractCreditDefaultSwapSecurity) {
          String redCode = ((CreditDefaultSwapSecurity) underlying).getReferenceEntity().getValue();
          Organization organisation = organizationSource.getOrganizationByRedCode(redCode);
          return organisation.getObligor();
        } else {
          // CreditDefaultSwapOptionSecurity
          // null communicates N/A
          return null;
        }

      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  protected String handleExtractedData(Obligor extracted) {
    return extracted.getObligorShortName();
  }

}
