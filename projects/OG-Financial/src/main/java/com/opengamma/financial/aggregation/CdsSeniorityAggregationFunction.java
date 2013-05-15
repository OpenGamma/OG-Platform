/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;

/**
 * Simple aggregator function to allow positions to be aggregated by debt seniority. This is
 * generally only applicable to CDS securities, and if applied to securities with no
 * debt seniority, the result of {@link #classifyPosition(Position)} will be "N/A".
 */
public class CdsSeniorityAggregationFunction extends AbstractCdsAggregationFunction<DebtSeniority> {

  /**
   * Function name.
   */
  private static final String NAME = "Seniority";

  /**
   * Creates an instance.
   * 
   * @param securitySource  the security source, not null
   */
  public CdsSeniorityAggregationFunction(SecuritySource securitySource) {
    super(NAME, securitySource, new CdsValueExtractor<DebtSeniority>() {
      @Override
      public DebtSeniority extract(AbstractCreditDefaultSwapSecurity cds) {
        if (cds instanceof CreditDefaultSwapSecurity) {
          return ((CreditDefaultSwapSecurity) cds).getDebtSeniority();
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
  protected String handleExtractedData(DebtSeniority extracted) {
    return extracted.toString();
  }

}
