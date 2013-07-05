/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.option.FXBarrierOptionSecurity;

/**
 * Summary factory for {@link FXBarrierOptionSecurity}.
 */
public class FXBarrierOptionSummaryFactory implements SummaryFactory<FXBarrierOptionSecurity> {

  @Override
  public String getSecurityType() {
    return FXBarrierOptionSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(final FXBarrierOptionSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.DESCRIPTION, security.getPutCurrency() + "/" + security.getCallCurrency())
        .with(SummaryField.MATURITY, security.getExpiry())
        .with(SummaryField.NOTIONAL, security.getPutAmount() + "/" + security.getCallAmount())
        .with(SummaryField.FREQUENCY, security.getSamplingFrequency()).build();
  }

}
