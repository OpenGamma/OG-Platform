/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;

/**
 * Summary factory for {@link EquityVarianceSwapSecurity}.
 */
public class EquityVarianceSwapSummaryFactory implements SummaryFactory<EquityVarianceSwapSecurity> {

  @Override
  public String getSecurityType() {
    return EquityVarianceSwapSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(EquityVarianceSwapSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.NOTIONAL, security.getNotional())
        .with(SummaryField.STRIKE, security.getStrike())
        .with(SummaryField.UNDERLYING, security.getSpotUnderlyingId())
        .with(SummaryField.START, security.getFirstObservationDate())
        .with(SummaryField.MATURITY, security.getLastObservationDate())
        .with(SummaryField.FREQUENCY, security.getObservationFrequency().getName()).build();
  }

}
