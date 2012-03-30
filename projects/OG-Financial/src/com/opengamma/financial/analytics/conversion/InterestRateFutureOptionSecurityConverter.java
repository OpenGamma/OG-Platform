/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * 
 */
public class InterestRateFutureOptionSecurityConverter {
  private final SecuritySource _securitySource;
  private final InterestRateFutureSecurityConverter _underlyingConverter;

  public InterestRateFutureOptionSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource,
      final SecuritySource securitySource) {
    Validate.notNull(securitySource, "security source");
    _underlyingConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    _securitySource = securitySource;
  }

  public Object convert(final IRFutureOptionSecurity security) {
    Validate.notNull(security, "security");
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    final InterestRateFutureSecurity underlyingSecurity = ((InterestRateFutureSecurity) _securitySource.getSecurity(ExternalIdBundle.of(underlyingIdentifier)));
    final InterestRateFutureDefinition underlyingFuture = _underlyingConverter.visitInterestRateFutureSecurity(underlyingSecurity);
    //    visitInterestRateFutureSecurity(underlyingSecurity);
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
