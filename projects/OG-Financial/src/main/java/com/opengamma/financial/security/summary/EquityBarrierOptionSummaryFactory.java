/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;

/**
 * Summary factory for {@link EquityBarrierOptionSecurity}.
 */
public class EquityBarrierOptionSummaryFactory implements SummaryFactory<EquityBarrierOptionSecurity> {

  @Override
  public String getSecurityType() {
    return EquityBarrierOptionSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(EquityBarrierOptionSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.MATURITY, security.getExpiry())
        .with(SummaryField.DIRECTION, security.getOptionType())
        .with(SummaryField.STRIKE, security.getStrike())
        .with(SummaryField.UNDERLYING, security.getUnderlyingId())
        .with(SummaryField.FREQUENCY, security.getSamplingFrequency()).build();
  }

}
