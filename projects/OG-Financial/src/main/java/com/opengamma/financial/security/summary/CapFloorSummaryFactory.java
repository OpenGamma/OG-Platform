/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.capfloor.CapFloorSecurity;

/**
 * Summary factory for {@link CapFloorSecurity}.
 */
public class CapFloorSummaryFactory implements SummaryFactory<CapFloorSecurity> {

  @Override
  public String getSecurityType() {
    return CapFloorSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(CapFloorSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.START, security.startDate())
        .with(SummaryField.MATURITY, security.getMaturityDate())
        .with(SummaryField.NOTIONAL, security.getNotional())
        .with(SummaryField.STRIKE, security.getStrike())
        .with(SummaryField.FREQUENCY, security.getFrequency().getName())
        .with(SummaryField.UNDERLYING, security.getUnderlyingId()).build();
  }

}
