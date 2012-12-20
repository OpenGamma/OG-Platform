/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.fra.FRASecurity;

/**
 * Summary factory for {@link FRASecurity}.
 */
public class FRASummaryFactory implements SummaryFactory<FRASecurity> {

  @Override
  public String getSecurityType() {
    return FRASecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(FRASecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.DESCRIPTION, security.getCurrency())
        .with(SummaryField.NOTIONAL, security.getAmount())
        .with(SummaryField.START, security.getStartDate())
        .with(SummaryField.MATURITY, security.getEndDate())
        .with(SummaryField.STRIKE, security.getRate())
        .with(SummaryField.UNDERLYING, security.getUnderlyingId()).build();
  }

}
