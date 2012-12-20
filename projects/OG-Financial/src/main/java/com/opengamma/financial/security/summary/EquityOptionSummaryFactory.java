/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalIdBundle;

/**
 * Summary factory for {@link EquityOptionSecurity}.
 */
public class EquityOptionSummaryFactory implements SummaryFactory<EquityOptionSecurity> {

  private final SecuritySource _securitySource;
  
  public EquityOptionSummaryFactory(SecuritySource securitySource) {
    _securitySource = securitySource;
  }
  
  @Override
  public String getSecurityType() {
    return EquityOptionSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(EquityOptionSecurity security) {
    EquitySecurity underlyingSecurity = (EquitySecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    return SummaryBuilder.create(security)
        .with(SummaryField.DESCRIPTION, underlyingSecurity.getShortName())
        .with(SummaryField.STRIKE, security.getStrike())
        .with(SummaryField.MATURITY, security.getExpiry())
        .with(SummaryField.DIRECTION, security.getOptionType())
        .with(SummaryField.UNDERLYING, security.getUnderlyingId()).build();
  }

}
