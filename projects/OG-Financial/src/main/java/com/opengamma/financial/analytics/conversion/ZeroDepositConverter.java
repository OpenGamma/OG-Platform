/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ZeroDepositConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final ConventionBundleSource _conventionSource;
  
  public ZeroDepositConverter(final ConventionBundleSource conventionSource) {
    ArgumentChecker.notNull(conventionSource, "convention sourceo");
    _conventionSource = conventionSource;
  }
  
  @Override
  public InstrumentDefinition<?> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final Currency currency = security.getCurrency();
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime endDate = security.getMaturityDate();
    final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_ZERO_DEPOSIT"));
    final DayCount daycount = convention.getDayCount();
    final InterestRate rate = new ContinuousInterestRate(security.getRate());
    return DepositZeroDefinition.from(currency, startDate, endDate, daycount, rate);
  }

  @Override
  public InstrumentDefinition<?> visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public InstrumentDefinition<?> visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final Currency currency = security.getCurrency();
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime endDate = security.getMaturityDate();
    final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_ZERO_DEPOSIT"));
    final DayCount daycount = convention.getDayCount();
    final InterestRate rate = new PeriodicInterestRate(security.getRate(), (int) security.getCompoundingPeriodsPerYear());
    return DepositZeroDefinition.from(currency, startDate, endDate, daycount, rate);
  }

}
