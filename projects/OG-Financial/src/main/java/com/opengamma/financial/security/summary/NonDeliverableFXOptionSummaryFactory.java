/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;

/**
 * 
 */
public class NonDeliverableFXOptionSummaryFactory implements SummaryFactory<NonDeliverableFXOptionSecurity> {

  @Override
  public String getSecurityType() {
    return IRFutureOptionSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(NonDeliverableFXOptionSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.MATURITY, security.getExpiry())
        .with(SummaryField.DESCRIPTION, security.getPutCurrency() + "/" + security.getCallCurrency())
        .with(SummaryField.NOTIONAL, security.getPutAmount() + "/" + security.getCallAmount()).build();
  }

}
