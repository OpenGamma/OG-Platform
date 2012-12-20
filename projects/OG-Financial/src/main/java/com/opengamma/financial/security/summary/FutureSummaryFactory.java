/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.future.FutureSecurity;

/**
 * Summary factory for {@link FutureSecurity}.
 */
public class FutureSummaryFactory implements SummaryFactory<FutureSecurity> {

  @Override
  public String getSecurityType() {
    return FutureSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(FutureSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.MATURITY, security.getExpiry())
        .with(SummaryField.NOTIONAL, security.getUnitAmount()).build();
  }

}
