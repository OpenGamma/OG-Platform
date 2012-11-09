/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.equity.EquitySecurity;

/**
 * Summary factory for {@link EquitySecurity}.
 */
public class EquitySummaryFactory implements SummaryFactory<EquitySecurity> {

  @Override
  public String getSecurityType() {
    return EquitySecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(EquitySecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.DESCRIPTION, security.getShortName()).build();
  }

}
