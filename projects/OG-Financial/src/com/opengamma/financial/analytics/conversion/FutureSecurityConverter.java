/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;

/**
 * 
 */
public class FutureSecurityConverter extends AbstractFutureSecurityVisitor<FixedIncomeInstrumentConverter<?>> {
  private final BondFutureSecurityConverter _bondFutureConverter;
  private final InterestRateFutureSecurityConverter _irFutureConverter;

  public FutureSecurityConverter(final BondFutureSecurityConverter bondFutureConverter, final InterestRateFutureSecurityConverter irFutureConverter) {
    Validate.notNull(bondFutureConverter, "bond future converter");
    Validate.notNull(irFutureConverter, "interest rate future converter");
    _bondFutureConverter = bondFutureConverter;
    _irFutureConverter = irFutureConverter;
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitBondFutureSecurity(final BondFutureSecurity security) {
    return security.accept(_bondFutureConverter);
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return security.accept(_irFutureConverter);
  }

}
