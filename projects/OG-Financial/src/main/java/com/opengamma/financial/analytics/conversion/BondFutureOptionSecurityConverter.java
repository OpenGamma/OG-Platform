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
import com.opengamma.core.holiday.HolidaySource;
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
 *
 */
public class BondFutureOptionSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final SecuritySource _securitySource;
  private final BondFutureSecurityConverter _underlyingConverter;

  public BondFutureOptionSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource,
      final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "security source");
    final BondSecurityConverter bondSecurityConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    _underlyingConverter = new BondFutureSecurityConverter(securitySource, bondSecurityConverter);
    _securitySource = securitySource;
  }

  @Override
  public InstrumentDefinition<?> visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    final BondFutureSecurity underlyingSecurity = ((BondFutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(underlyingIdentifier)));
    if (underlyingSecurity == null) {
      throw new OpenGammaRuntimeException("Underlying security " + underlyingIdentifier + " was not found in database");
    }
    final BondFutureDefinition underlyingFuture = _underlyingConverter.visitBondFutureSecurity(underlyingSecurity);
    final ZonedDateTime expirationDate = security.getExpiry().getExpiry();
    final double strike = security.getStrike();
    final boolean isCall = security.getOptionType() == OptionType.CALL ? true : false;
    return new BondFutureOptionPremiumSecurityDefinition(underlyingFuture, expirationDate, strike, isCall);
  }
}
