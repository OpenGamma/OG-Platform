/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterestRateFutureOptionSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionSecurityConverter.class);
  private final SecuritySource _securitySource;
  private final InterestRateFutureSecurityConverter _underlyingConverter;

  public InterestRateFutureOptionSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource,
      final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "security source");
    _underlyingConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    _securitySource = securitySource;
  }

  @Override
  public InstrumentDefinition<?> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    final InterestRateFutureSecurity underlyingSecurity = ((InterestRateFutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(underlyingIdentifier)));
    if (underlyingSecurity == null) { s_logger.error("Couldn't find underlying security with ExternalId[{}] of security {}", new Object[] {underlyingIdentifier, security.toString() }); }
    final InterestRateFutureDefinition underlyingFuture = _underlyingConverter.visitInterestRateFutureSecurity(underlyingSecurity);
    final ZonedDateTime expirationDate = security.getExpiry().getExpiry();
    final double strike = security.getStrike();
    final boolean isCall = security.getOptionType() == OptionType.CALL ? true : false;
    final boolean isMargined = security.isMargined();
    if (isMargined) {
      return new InterestRateFutureOptionMarginSecurityDefinition(underlyingFuture, expirationDate, strike, isCall);
    }
    return new InterestRateFutureOptionPremiumSecurityDefinition(underlyingFuture, expirationDate, strike, isCall);
  }

}
