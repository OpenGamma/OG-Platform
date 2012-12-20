/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.CoalescingSecuritySource;

/**
 * Specialization of {@link CoalescingSecuritySource} for {@link FinancialSecuritySource}.
 */
public class CoalescingFinancialSecuritySource extends CoalescingSecuritySource implements FinancialSecuritySource {

  public CoalescingFinancialSecuritySource(final FinancialSecuritySource underlying) {
    super(underlying);
  }

  protected FinancialSecuritySource getFinancialUnderlying() {
    return (FinancialSecuritySource) getUnderlying();
  }

  @Override
  public Collection<Security> getBondsWithIssuerName(final String issuerName) {
    return getFinancialUnderlying().getBondsWithIssuerName(issuerName);
  }

}
