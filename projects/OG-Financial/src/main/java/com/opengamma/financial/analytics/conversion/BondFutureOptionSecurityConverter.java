/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Bond future option converter to create OG-Analytics representations from OG-Financial types.
 */
public class BondFutureOptionSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  
  /**
   * SecuritySource used to look up underlying bond future.
   */
  private final SecuritySource _securitySource;
  /**
   * Converter used for premium based bond future option.
   */
  private final BondFutureSecurityConverter _underlyingConverter;
  /**
   * Converter used for margin based bond future option.
   */
  private final BondAndBondFutureTradeConverter _bondAndBondFutureConverter;

  /**
   * Constructs a bond future option converter.
   * @param holidaySource the holiday source, not null.
   * @param conventionBundleSource the convention bundle source, not null.
   * @param regionSource the region source, not null.
   * @param securitySource the security source, not null.
   * @param conventionSource the convention source, not null.
   * @param legalEntitySource the legal entity source, not null.
   */
  public BondFutureOptionSecurityConverter(final HolidaySource holidaySource,
                                           final ConventionBundleSource conventionBundleSource,
                                           final RegionSource regionSource,
                                           final SecuritySource securitySource,
                                           final ConventionSource conventionSource,
                                           final LegalEntitySource legalEntitySource) {
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(conventionBundleSource, "conventionBundleSource");
    ArgumentChecker.notNull(regionSource, "regionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(legalEntitySource, "legalEntitySource");
    final BondSecurityConverter bondSecurityConverter = new BondSecurityConverter(holidaySource, conventionBundleSource, regionSource);
    _underlyingConverter = new BondFutureSecurityConverter(securitySource, bondSecurityConverter);
    _securitySource = securitySource;
    _bondAndBondFutureConverter = new BondAndBondFutureTradeConverter(holidaySource, conventionBundleSource, conventionSource, regionSource, securitySource, legalEntitySource);
  }

  @Override
  public InstrumentDefinition<?> visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    final BondFutureSecurity underlyingSecurity = ((BondFutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(underlyingIdentifier)));
    if (underlyingSecurity == null) {
      throw new OpenGammaRuntimeException("Underlying security " + underlyingIdentifier + " was not found in database");
    }
    final ZonedDateTime expirationDate = security.getExpiry().getExpiry();
    final double strike = security.getStrike();
    final boolean isCall = security.getOptionType() == OptionType.CALL ? true : false;
    if (security.isMargined()) {
      final BondFuturesSecurityDefinition underlyingFuture = _bondAndBondFutureConverter.getBondFuture(underlyingSecurity);
      return new BondFuturesOptionMarginSecurityDefinition(underlyingFuture, expirationDate, expirationDate, strike, isCall);
    }
    final BondFutureDefinition underlyingFuture = _underlyingConverter.visitBondFutureSecurity(underlyingSecurity);
    return new BondFutureOptionPremiumSecurityDefinition(underlyingFuture, expirationDate, strike, isCall);
  }
}
