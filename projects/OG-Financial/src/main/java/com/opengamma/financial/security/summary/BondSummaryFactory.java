/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.bond.BondSecurity;

/**
 * Summary factory for {@link BondSecurity}.
 */
public class BondSummaryFactory implements SummaryFactory<BondSecurity> {

  @Override
  public String getSecurityType() {
    return BondSecurity.SECURITY_TYPE;
  }
  
  @Override
  public Summary getSummary(BondSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.DESCRIPTION, security.getIssuerName())
        .with(SummaryField.STRIKE, security.getCouponRate())
        .with(SummaryField.FREQUENCY, security.getCouponFrequency().getName())
        .with(SummaryField.START, security.getFirstCouponDate())
        .with(SummaryField.MATURITY, security.getLastTradeDate()).build();
  }

}
