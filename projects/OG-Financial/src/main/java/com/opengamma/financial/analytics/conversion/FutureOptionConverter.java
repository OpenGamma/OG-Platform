/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureOptionDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 *  Converts an CommodityFutureOptionSecurity into OG-Financial's version of one: CommodityFutureOptionDefinition
 */
public class FutureOptionConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {

  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  private final SecuritySource _securitySource;
  private final CommodityFutureConverter _underlyingConverter;

  private static final Logger s_logger = LoggerFactory.getLogger(FutureOptionConverter.class);

  public FutureOptionConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource,
                                final SecuritySource securitySource) {

    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    Validate.notNull(securitySource, "security source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
    _securitySource = securitySource;
    _underlyingConverter = new CommodityFutureConverter();
  }

  @Override
  public InstrumentDefinition<?> visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    // Future details
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    final InterestRateFutureSecurity underlyingSecurity = ((InterestRateFutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(underlyingIdentifier)));
    if (underlyingSecurity == null) { s_logger.error("Couldn't find underlying security with ExternalId[{}] of security {}", new Object[] {underlyingIdentifier, security.toString() }); }
    final AgricultureFutureDefinition underlyingFuture = (AgricultureFutureDefinition) underlyingSecurity.accept(_underlyingConverter); // START HERE !!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // Option details
    final ZonedDateTime expiryDate = security.getExpiry().getExpiry();
    final double strike = security.getStrike();
    ExerciseDecisionType exerciseType =  ExerciseDecisionType.from(security.getExerciseType().getName());
    if (exerciseType == null) {
      exerciseType = ExerciseDecisionType.EUROPEAN;
    }
    final boolean isCall = security.getOptionType() == OptionType.CALL;

    return new AgricultureFutureOptionDefinition(expiryDate, underlyingFuture, strike, exerciseType, isCall);
  }

}
