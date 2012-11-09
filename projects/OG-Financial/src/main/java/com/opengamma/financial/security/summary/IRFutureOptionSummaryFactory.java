/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.option.IRFutureOptionSecurity;

/**
 * Summary factory for {@link IRFutureOptionSecurity}.
 */
public class IRFutureOptionSummaryFactory implements SummaryFactory<IRFutureOptionSecurity> {

  @Override
  public String getSecurityType() {
    return IRFutureOptionSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(IRFutureOptionSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.MATURITY, security.getExpiry())
        .with(SummaryField.STRIKE, security.getStrike())
        .with(SummaryField.UNDERLYING, security.getUnderlyingId())
        .with(SummaryField.DIRECTION, security.getOptionType()).build();
  }

}
