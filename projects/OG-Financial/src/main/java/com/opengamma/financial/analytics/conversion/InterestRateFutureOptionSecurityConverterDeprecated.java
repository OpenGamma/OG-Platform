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
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts interest rate future option securities into the form used by the analytics library
 * 
 * @deprecated Use {@link InterestRateFutureOptionSecurityConverter}. {@link ConventionBundleSource} should not be used, as the conventions are not typed.
 */
@Deprecated
public class InterestRateFutureOptionSecurityConverterDeprecated extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {

  /** The security source */
  private final SecuritySource _securitySource;
  /** Converter for the underlying future */
  private final InterestRateFutureSecurityConverterDeprecated _underlyingConverter;
  /** Lookup version/correction */
  private final VersionCorrection _versionCorrection;

  /**
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   * @param securitySource The security source, not null
   * @param versionCorrection The resolution timestamp, not null
   */
  public InterestRateFutureOptionSecurityConverterDeprecated(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource,
      final SecuritySource securitySource, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    _underlyingConverter = new InterestRateFutureSecurityConverterDeprecated(holidaySource, conventionSource, regionSource);
    _securitySource = securitySource;
    _versionCorrection = versionCorrection;
  }

  @Override
  public InstrumentDefinition<?> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    final InterestRateFutureSecurity underlyingSecurity = ((InterestRateFutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(underlyingIdentifier), _versionCorrection));
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
