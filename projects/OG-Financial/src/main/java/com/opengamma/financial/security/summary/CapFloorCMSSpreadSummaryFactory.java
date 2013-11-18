/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;

/**
 * Summary factory for {@link CapFloorCMSSpreadSecurity}.
 */
public class CapFloorCMSSpreadSummaryFactory implements SummaryFactory<CapFloorCMSSpreadSecurity> {

  @Override
  public String getSecurityType() {
    return CapFloorCMSSpreadSecurity.SECURITY_TYPE;
  }
  
  @Override
  public Summary getSummary(CapFloorCMSSpreadSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.DESCRIPTION, security.getLongId() + "/" + security.getShortId())
        .with(SummaryField.START, security.getStartDate())
        .with(SummaryField.MATURITY, security.getMaturityDate())
        .with(SummaryField.NOTIONAL, security.getNotional())
        .with(SummaryField.STRIKE, security.getStrike())
        .with(SummaryField.FREQUENCY, security.getFrequency().getName()).build();
  }

}
