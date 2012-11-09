/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;

/**
 * Summary factor for {@link SwaptionSecurity}.
 */
public class SwaptionSummaryFactory implements SummaryFactory<SwaptionSecurity> {

  private final SecuritySource _securitySource;
  
  public SwaptionSummaryFactory(SecuritySource securitySource) {
    _securitySource = securitySource;
  }
  
  @Override
  public String getSecurityType() {
    return SwaptionSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(SwaptionSecurity security) {
    SwapSecurity underlyingSecurity = (SwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    return SwapSummaryFactory.append(SummaryBuilder.create(security), underlyingSecurity)
        .with(SummaryField.MATURITY, security.getExpiry()).build();
  }

}
