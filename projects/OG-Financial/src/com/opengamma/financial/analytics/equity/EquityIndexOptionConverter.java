/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.equity;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * TODO We may want to have additional converters that create the same Definition from a different
 * Security parameterisation. It might be convenient for some to quote month/year and for the system
 * to establish expiry and settlement dates from conventions
 */
public class EquityIndexOptionConverter {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public EquityIndexOptionConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  public EquityIndexOptionDefinition visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    Validate.notNull(security, "security");

    final boolean isCall = security.getOptionType() == OptionType.CALL;
    final double strike = security.getStrike();
    final ZonedDateTime expiryDT = security.getExpiry().getExpiry();
    final Currency ccy = security.getCurrency();
    final double unitNotional = security.getPointValue();
    ExerciseDecisionType exerciseType =  ExerciseDecisionType.from(security.getExerciseType().getName());
    if (exerciseType == null) {
      exerciseType = ExerciseDecisionType.EUROPEAN; // TODO Review. MAYBE BETTER TO USE BOOLEAN?!?
    }
    final ExternalIdBundle indexId = security.getExternalIdBundle();
    final ConventionBundle indexConventions = _conventionSource.getConventionBundle(indexId);
    // TODO !!! We need to know how long after expiry does settlement occur?
    // IndexOptions are obviously Cash Settled
    final LocalDate settlementDate = expiryDT.toLocalDate(); // FIXME !!! Needs to come from convention !!!

    return new EquityIndexOptionDefinition(isCall, strike, ccy, indexId, exerciseType, expiryDT, settlementDate, unitNotional);
  }





}


