/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts interest rate future option securities into the form used by the analytics library.
 */
public class InterestRateFutureOptionSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The security source */
  private final SecuritySource _securitySource;
  /** Converter for the underlying future */
  private final InterestRateFutureSecurityConverter _underlyingConverter;

  /**
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   * @param securitySource The security source, not null
   */
  public InterestRateFutureOptionSecurityConverter(final HolidaySource holidaySource, final ConventionSource conventionSource, final RegionSource regionSource,
      final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "security source");
    _underlyingConverter = new InterestRateFutureSecurityConverter(securitySource, holidaySource, conventionSource, regionSource);
    _securitySource = securitySource;
  }

  @Override
  public InstrumentDefinition<?> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    // REVIEW Andrew 2012-01-17 -- This call to getSingle is not correct as the resolution time of the view cycle will not be considered
    final InterestRateFutureSecurity underlyingSecurity = ((InterestRateFutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(underlyingIdentifier)));
    if (underlyingSecurity == null) {
      throw new OpenGammaRuntimeException("Underlying security " + underlyingIdentifier + " was not found in database");
    }
    final InterestRateFutureSecurityDefinition underlyingFuture = _underlyingConverter.visitInterestRateFutureSecurity(underlyingSecurity);
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
