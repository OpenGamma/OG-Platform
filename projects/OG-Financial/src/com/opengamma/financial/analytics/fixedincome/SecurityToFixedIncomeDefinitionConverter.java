/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.instrument.Convention;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.bond.BondConvention;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.fra.FRADefinition;
import com.opengamma.financial.instrument.swap.FixedFloatSwapDefinition;
import com.opengamma.financial.instrument.swap.FixedSwapLegDefinition;
import com.opengamma.financial.instrument.swap.FloatingSwapLegDefinition;
import com.opengamma.financial.instrument.swap.SwapConvention;
import com.opengamma.financial.instrument.swap.TenorSwapDefinition;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.schedule.ScheduleFactory;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class SecurityToFixedIncomeDefinitionConverter implements FinancialSecurityVisitor<FixedIncomeInstrumentDefinition<?>> {
  private static final Logger s_logger = LoggerFactory.getLogger(SecurityToFixedIncomeDefinitionConverter.class);
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public SecurityToFixedIncomeDefinitionConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public FixedIncomeInstrumentDefinition<?> visitBondSecurity(final BondSecurity security) {
    final CurrencyUnit currency = security.getCurrency();
    final ConventionBundle convention = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_TREASURY_BOND_CONVENTION"));
    return visitBondSecurity(security, convention);
  }

  public FixedIncomeInstrumentDefinition<?> visitBondSecurity(final BondSecurity security, final ConventionBundle convention) {
    final LocalDate lastTradeDate = security.getLastTradeDate().getExpiry().toLocalDate();
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency());
    final Frequency frequency = security.getCouponFrequency();
    final SimpleFrequency simpleFrequency;
    if (frequency instanceof PeriodFrequency) {
      simpleFrequency = ((PeriodFrequency) frequency).toSimpleFrequency();
    } else if (frequency instanceof SimpleFrequency) {
      simpleFrequency = (SimpleFrequency) frequency;
    } else {
      throw new IllegalArgumentException("Can only handle PeriodFrequency and SimpleFrequency");
    }
    final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final LocalDate datedDate = security.getInterestAccrualDate().toZonedDateTime().toLocalDate();
    final int periodsPerYear = (int) simpleFrequency.getPeriodsPerYear();
    final DayCount daycount = security.getDayCountConvention();
    final boolean isEOMConvention = convention.isEOMConvention();
    final int settlementDays = convention.getSettlementDays();
    final LocalDate[] nominalDates = getBondSchedule(security, lastTradeDate, simpleFrequency, convention, datedDate);
    final boolean rollToSettlement = convention.rollToSettlement();
    final LocalDate[] settlementDates = (rollToSettlement ? ScheduleCalculator.getSettlementDateSchedule(nominalDates, calendar, businessDayConvention, convention.getSettlementDays())
        : ScheduleCalculator.getSettlementDateSchedule(nominalDates, calendar, businessDayConvention, 0));
    final double coupon = security.getCouponRate() / 100;
    final BondConvention bondConvention = new BondConvention(settlementDays, daycount, businessDayConvention, calendar, isEOMConvention, convention.getName(), convention.getExDividendDays(),
        SimpleYieldConvention.US_TREASURY_EQUIVALANT);
    return new BondDefinition(nominalDates, settlementDates, coupon, periodsPerYear, bondConvention);
  }

  @Override
  public FixedIncomeInstrumentDefinition<?> visitCashSecurity(final CashSecurity security) {
    final ConventionBundle conventions = _conventionSource.getConventionBundle(security.getIdentifiers());
    final Identifier regionId = security.getRegion().getIdentityKey();
    final Calendar calendar = getCalendar(regionId);
    final CurrencyUnit currency = security.getCurrency();
    final ZonedDateTime maturityDate = security.getMaturity().toZonedDateTime();
    final Convention convention = new Convention(conventions.getSettlementDays(), conventions.getDayCount(), conventions.getBusinessDayConvention(), calendar, currency.getCode()
        + "_CASH_CONVENTION");
    //return new CashDefinition(maturityDate, cashSecurity.getRate(), convention);
    return new CashDefinition(maturityDate, 0, convention);
  }

  @Override
  public FixedIncomeInstrumentDefinition<?> visitEquitySecurity(final EquitySecurity security) {
    throw new OpenGammaRuntimeException("Cannot construct a FixedIncomeInstrumentDefinition from an EquitySecurity");
  }

  @Override
  public FixedIncomeInstrumentDefinition<?> visitFRASecurity(final FRASecurity security) {
    final String currency = security.getCurrency().getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_FRA"));
    final Identifier regionId = security.getRegion().getIdentityKey();
    final Calendar calendar = getCalendar(regionId);
    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    final ZonedDateTime startDate = security.getStartDate().toZonedDateTime();
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, security.getEndDate().toZonedDateTime()); // just in case
    final Convention convention = new Convention(conventions.getSettlementDays(), conventions.getDayCount(), conventions.getBusinessDayConvention(), calendar, currency + "_FRA_CONVENTION");
    //return new FRADefinition(startDate, maturityDate, fraSecurity.getRate(), convention);
    return new FRADefinition(startDate, maturityDate, 0, convention);
  }

  @Override
  public FixedIncomeInstrumentDefinition<?> visitFutureSecurity(final FutureSecurity security) {
    throw new OpenGammaRuntimeException("Cannot construct a FixedIncomeInstrumentDefinition from a FutureSecurity");
  }

  @Override
  public FixedIncomeInstrumentDefinition<?> visitOptionSecurity(final OptionSecurity security) {
    throw new OpenGammaRuntimeException("Cannot construct a FixedIncomeInstrumentDefinition from an OptionSecurity");
  }

  @Override
  public FixedIncomeInstrumentDefinition<?> visitSwapSecurity(final SwapSecurity security) {
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    if (!payLeg.getRegionIdentifier().equals(receiveLeg.getRegionIdentifier())) {
      throw new OpenGammaRuntimeException("Pay and receive legs must be from same region");
    }
    if (payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      return getFixedFloatSwapDefinition(security, true);
    } else if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg) {
      return getFixedFloatSwapDefinition(security, false);
    } else if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      return getTenorSwapDefinition(security);
    } else {
      throw new OpenGammaRuntimeException("Can only handle fixed-floating swaps and floating-floating swaps");
    }
  }

  private FixedIncomeInstrumentDefinition<FixedCouponSwap<Payment>> getFixedFloatSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate().toZonedDateTime();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate().toZonedDateTime();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final Identifier regionId = payLeg.getRegionIdentifier();
    final Calendar calendar = getCalendar(regionId);
    final String currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency().getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_SWAP"));
    final FixedSwapLegDefinition fixedLegDefinition = getFixedSwapLegDefinition(effectiveDate, maturityDate, fixedLeg, calendar, currency, conventions);
    final FloatingSwapLegDefinition floatingLegDefinition = getFloatingSwapLegDefinition(effectiveDate, maturityDate, floatLeg, calendar, currency, conventions);
    return new FixedFloatSwapDefinition(fixedLegDefinition, floatingLegDefinition);
  }

  private FixedIncomeInstrumentDefinition<TenorSwap<Payment>> getTenorSwapDefinition(final SwapSecurity swapSecurity) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate().toZonedDateTime();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate().toZonedDateTime();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FloatingInterestRateLeg floatPayLeg = (FloatingInterestRateLeg) payLeg;
    final FloatingInterestRateLeg floatReceiveLeg = (FloatingInterestRateLeg) receiveLeg;
    final Calendar calendar = getCalendar(payLeg.getRegionIdentifier());
    final String currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency().getCode();
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

  private LocalDate[] getBondSchedule(final BondSecurity security, final LocalDate maturityDate, final SimpleFrequency simpleFrequency, final ConventionBundle convention, final LocalDate datedDate) {
    LocalDate[] schedule = ScheduleFactory.getSchedule(datedDate, maturityDate, simpleFrequency, convention.isEOMConvention(), convention.calculateScheduleFromMaturity(), false);
    // front stub
    if (schedule[0].equals(security.getFirstCouponDate().toZonedDateTime().toLocalDate())) {
      final int n = schedule.length;
      final LocalDate[] temp = new LocalDate[n + 1];
      temp[0] = datedDate;
      for (int i = 1; i < n + 1; i++) {
        temp[i] = schedule[i - 1];
      }
      schedule = temp;
    }
    if (!schedule[1].toLocalDate().equals(security.getFirstCouponDate().toZonedDateTime().toLocalDate())) {
      s_logger.warn("Security first coupon date did not match calculated first coupon date: " + schedule[1].toLocalDate() + ", " + security.getFirstCouponDate().toZonedDateTime().toLocalDate());
    }
    return schedule;
  }

  private Calendar getCalendar(final Identifier regionId) {
    return new HolidaySourceCalendarAdapter(_holidaySource, RegionUtils.getRegions(_regionSource, regionId));
  }
}
