/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.Convention;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.fra.FRADefinition;
import com.opengamma.financial.instrument.swap.FixedFloatSwapDefinition;
import com.opengamma.financial.instrument.swap.FixedSwapLegDefinition;
import com.opengamma.financial.instrument.swap.FloatingSwapLegDefinition;
import com.opengamma.financial.instrument.swap.SwapConvention;
import com.opengamma.financial.instrument.swap.TenorSwapDefinition;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class FixedIncomeStripToFixedIncomeDefinitionConverter {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public FixedIncomeStripToFixedIncomeDefinitionConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  public FixedIncomeInstrumentDefinition<?> getDefinitionForSecurity(final FixedIncomeStripWithSecurity strip) {
    Validate.notNull(strip, "strip");
    final StripInstrumentType type = strip.getInstrumentType();
    final Security security = strip.getSecurity();
    switch (type) {
      case SWAP:
        return getFixedFloatSwapDefinition((SwapSecurity) security);
      case CASH:
        return getCashDefinition((CashSecurity) security);
      case FRA:
        return getFRADefinition((FRASecurity) security);
      case LIBOR:
        return getCashDefinition((CashSecurity) security);
      case TENOR_SWAP:
        return getTenorSwapDefinition((SwapSecurity) security);
      default:
        throw new OpenGammaRuntimeException("Do not know how to handle StripInstrumentType " + type);
    }
  }

  private FixedIncomeInstrumentDefinition<FixedCouponSwap<Payment>> getFixedFloatSwapDefinition(final SwapSecurity swapSecurity) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate().toZonedDateTime();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate().toZonedDateTime();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    if (!payLeg.getRegionIdentifier().equals(receiveLeg.getRegionIdentifier())) {
      throw new OpenGammaRuntimeException("Pay and receive legs must be from same region");
    }
    FixedInterestRateLeg fixedLeg;
    FloatingInterestRateLeg floatLeg;
    if (payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      fixedLeg = (FixedInterestRateLeg) payLeg;
      floatLeg = (FloatingInterestRateLeg) receiveLeg;
    } else if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg) {
      fixedLeg = (FixedInterestRateLeg) receiveLeg;
      floatLeg = (FloatingInterestRateLeg) payLeg;
    } else {
      throw new OpenGammaRuntimeException("Can only handle fixed-floating swaps");
    }
    final Identifier regionId = payLeg.getRegionIdentifier();
    final Calendar calendar = getCalendar(regionId);
    final String currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency().getISOCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_SWAP"));
    final FixedSwapLegDefinition fixedLegDefinition = getFixedSwapLegDefinition(effectiveDate, maturityDate, fixedLeg, calendar, currency, conventions);
    final FloatingSwapLegDefinition floatingLegDefinition = getFloatingSwapLegDefinition(effectiveDate, maturityDate, floatLeg, calendar, currency, conventions);
    return new FixedFloatSwapDefinition(fixedLegDefinition, floatingLegDefinition);
  }

  private FixedIncomeInstrumentDefinition<Cash> getCashDefinition(final CashSecurity cashSecurity) {
    final ConventionBundle conventions = _conventionSource.getConventionBundle(cashSecurity.getIdentifiers());
    final Identifier regionId = cashSecurity.getRegion().getIdentityKey();
    final Calendar calendar = getCalendar(regionId);
    final Currency currency = cashSecurity.getCurrency();
    final ZonedDateTime maturityDate = cashSecurity.getMaturity().toZonedDateTime();
    final Convention convention = new Convention(conventions.getSettlementDays(), conventions.getDayCount(), conventions.getBusinessDayConvention(), calendar, currency.getISOCode()
        + "_CASH_CONVENTION");
    //return new CashDefinition(maturityDate, cashSecurity.getRate(), convention);
    return new CashDefinition(maturityDate, 0, convention);
  }

  private FixedIncomeInstrumentDefinition<ForwardRateAgreement> getFRADefinition(final FRASecurity fraSecurity) {
    final String currency = fraSecurity.getCurrency().getISOCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_FRA"));
    final Identifier regionId = fraSecurity.getRegion().getIdentityKey();
    final Calendar calendar = getCalendar(regionId);
    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    final ZonedDateTime startDate = fraSecurity.getStartDate().toZonedDateTime();
    final ZonedDateTime fixingDate = businessDayConvention.adjustDate(calendar, startDate); // just in case
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(startDate, businessDayConvention, calendar, conventions.getSettlementDays());
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, fraSecurity.getEndDate().toZonedDateTime()); // just in case
    final Convention convention = new Convention(conventions.getSettlementDays(), conventions.getDayCount(), conventions.getBusinessDayConvention(), calendar, currency + "_FRA_CONVENTION");
    //return new FRADefinition(startDate, maturityDate, fraSecurity.getRate(), convention);
    return new FRADefinition(startDate, maturityDate, 0, convention);
  }

  private FixedIncomeInstrumentDefinition<TenorSwap<Payment>> getTenorSwapDefinition(final SwapSecurity swapSecurity) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate().toZonedDateTime();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate().toZonedDateTime();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    FloatingInterestRateLeg floatPayLeg;
    FloatingInterestRateLeg floatReceiveLeg;
    if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      floatPayLeg = (FloatingInterestRateLeg) payLeg;
      floatReceiveLeg = (FloatingInterestRateLeg) receiveLeg;
    } else {
      throw new OpenGammaRuntimeException("can only handle float-float legs");
    }
    if (!payLeg.getRegionIdentifier().equals(receiveLeg.getRegionIdentifier())) {
      throw new OpenGammaRuntimeException("Pay and receive legs must be from same region");
    }
    final Calendar calendar = getCalendar(payLeg.getRegionIdentifier());
    final String currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency().getISOCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_TENOR_SWAP"));
    final FloatingSwapLegDefinition payLegDefinition = getFloatingSwapLegDefinition(effectiveDate, maturityDate, floatPayLeg, calendar, currency, conventions);
    final FloatingSwapLegDefinition receiveLegDefinition = getFloatingSwapLegDefinition(effectiveDate, maturityDate, floatReceiveLeg, calendar, currency, conventions);
    return new TenorSwapDefinition(payLegDefinition, receiveLegDefinition);
  }

  private FixedSwapLegDefinition getFixedSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FixedInterestRateLeg fixedLeg, final Calendar calendar,
      final String currency, final ConventionBundle conventions) {
    final ZonedDateTime[] nominalDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, fixedLeg.getFrequency());
    //TODO are settlement days really 0 for swaps?    
    final ZonedDateTime[] settlementDates = ScheduleCalculator.getAdjustedDateSchedule(nominalDates, fixedLeg.getBusinessDayConvention(), calendar, 0);
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final double fixedRate = fixedLeg.getRate();
    final SwapConvention convention = new SwapConvention(0, conventions.getSwapFixedLegDayCount(), conventions.getBusinessDayConvention(), calendar, false, currency + "_FIXED_LEG_SWAP_CONVENTION");
    return new FixedSwapLegDefinition(effectiveDate, nominalDates, settlementDates, notional, fixedRate, convention);
  }

  private FloatingSwapLegDefinition getFloatingSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingInterestRateLeg floatLeg, final Calendar calendar,
      final String currency, final ConventionBundle conventions) {
    final ZonedDateTime[] nominalDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, floatLeg.getFrequency());
    //TODO are settlement days really 0 for swaps?    
    final ZonedDateTime[] settlementDates = ScheduleCalculator.getAdjustedDateSchedule(nominalDates, floatLeg.getBusinessDayConvention(), calendar, 0);
    final ZonedDateTime[] resetDates = ScheduleCalculator.getAdjustedResetDateSchedule(effectiveDate, nominalDates, floatLeg.getBusinessDayConvention(), calendar,
        conventions.getSwapFloatingLegSettlementDays());
    final ZonedDateTime[] maturityDates = ScheduleCalculator.getAdjustedMaturityDateSchedule(effectiveDate, nominalDates, floatLeg.getBusinessDayConvention(), calendar, floatLeg.getFrequency());
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    final double initialRate = floatLeg.getInitialFloatingRate();
    final double spread = floatLeg.getSpread();
    final SwapConvention convention = new SwapConvention(0, conventions.getSwapFloatingLegDayCount(), conventions.getSwapFloatingLegBusinessDayConvention(), calendar, false, currency
        + "_FLOATING_LEG_SWAP_CONVENTION");
    return new FloatingSwapLegDefinition(effectiveDate, nominalDates, settlementDates, resetDates, maturityDates, notional, initialRate, spread, convention);
  }

  // REVIEW: jim 8-Oct-2010 -- we might want to move this logic inside the RegionMaster.
  private Calendar getCalendar(final Identifier regionId) {
    if (regionId.isScheme(RegionUtils.FINANCIAL) && regionId.getValue().contains("+")) {
      final String[] regions = regionId.getValue().split("\\+");
      final Set<Region> resultRegions = new HashSet<Region>();
      for (final String region : regions) {
        resultRegions.add(_regionSource.getHighestLevelRegion(RegionUtils.financialRegionId(region)));
      }
      return new HolidaySourceCalendarAdapter(_holidaySource, resultRegions);
    } else {
      final Region payRegion = _regionSource.getHighestLevelRegion(regionId); // we've checked that they are the same.
      return new HolidaySourceCalendarAdapter(_holidaySource, payRegion);
    }
  }

}
