/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.option.EquityIndexOptionSecurity;

/**
 * Summary factory for {@link EquityIndexOptionSecurity}.
 */
public class EquityIndexOptionSummaryFactory implements SummaryFactory<EquityIndexOptionSecurity> {

  @Override
  public String getSecurityType() {
    return EquityIndexOptionSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(EquityIndexOptionSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.MATURITY, security.getExpiry())
        .with(SummaryField.DIRECTION, security.getOptionType())
        .with(SummaryField.STRIKE, security.getStrike())
        .with(SummaryField.UNDERLYING, security.getUnderlyingId()).build();
  }

}
