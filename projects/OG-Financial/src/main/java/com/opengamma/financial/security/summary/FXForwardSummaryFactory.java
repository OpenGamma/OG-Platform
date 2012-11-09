/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.fx.FXForwardSecurity;

/**
 * Summary factory for {@link FXForwardSecurity}.
 */
public class FXForwardSummaryFactory implements SummaryFactory<FXForwardSecurity> {

  private final SecuritySource _securitySource;
  
  public FXForwardSummaryFactory(SecuritySource securitySource) {
    _securitySource = securitySource;
  }
  
  @Override
  public String getSecurityType() {
    return FXForwardSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(FXForwardSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.NOTIONAL, security.getPayAmount() + "/" + security.getReceiveAmount())
        .with(SummaryField.UNDERLYING, security.getPayCurrency() + "/" + security.getReceiveCurrency())
        .with(SummaryField.MATURITY, security.getForwardDate()).build();
  }

}
