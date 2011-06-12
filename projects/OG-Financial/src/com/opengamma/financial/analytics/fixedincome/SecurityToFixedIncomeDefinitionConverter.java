/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.financial.instrument.bond.BondConvention;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.fra.FRADefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.schedule.ScheduleFactory;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SecurityToFixedIncomeDefinitionConverter extends FinancialSecurityVisitorAdapter<FixedIncomeInstrumentConverter<?>> {
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
  public FixedIncomeInstrumentConverter<?> visitBondSecurity(final BondSecurity security) {
    final Currency currency = security.getCurrency();
    final ConventionBundle convention = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_TREASURY_BOND_CONVENTION"));
    return visitBondSecurity(security, convention);
  }

  public FixedIncomeInstrumentConverter<?> visitBondSecurity(final BondSecurity security, final ConventionBundle convention) {
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
    final LocalDate datedDate = security.getInterestAccrualDate().toLocalDate();
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
    return new BondDefinition(security.getCurrency(), nominalDates, settlementDates, coupon, periodsPerYear, bondConvention);
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitCashSecurity(final CashSecurity security) {
    final ConventionBundle conventions = _conventionSource.getConventionBundle(security.getIdentifiers());
    final Identifier regionId = security.getRegion().getIdentityKey();
    final Calendar calendar = getCalendar(regionId);
    final Currency currency = security.getCurrency();
    final ZonedDateTime maturityDate = security.getMaturity();
    final Convention convention = new Convention(conventions.getSettlementDays(), conventions.getDayCount(), conventions.getBusinessDayConvention(), calendar, currency.getCode() + "_CASH_CONVENTION");
    //return new CashDefinition(maturityDate, cashSecurity.getRate(), convention);
    return new CashDefinition(maturityDate, 0, convention);
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitFRASecurity(final FRASecurity security) {
    final String currency = security.getCurrency().getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_FRA"));
    final Identifier regionId = security.getRegion().getIdentityKey();
    final Calendar calendar = getCalendar(regionId);
    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, security.getEndDate()); // just in case
    final Convention convention = new Convention(conventions.getSettlementDays(), conventions.getDayCount(), conventions.getBusinessDayConvention(), calendar, currency + "_FRA_CONVENTION");
    //return new FRADefinition(startDate, maturityDate, fraSecurity.getRate(), convention);
    return new FRADefinition(startDate, maturityDate, 0, convention);
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitSwapSecurity(final SwapSecurity security) {
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

  private SwapFixedIborSpreadDefinition getFixedFloatSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed) { //FixedIncomeInstrumentDefinition<FixedCouponSwap<Payment>>
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final Identifier regionId = payLeg.getRegionIdentifier();
    final Calendar calendar = getCalendar(regionId);
    final String currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency().getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_SWAP"));
    final AnnuityCouponFixedDefinition fixedLegDefinition = getFixedSwapLegDefinition(effectiveDate, maturityDate, fixedLeg, calendar, currency, conventions, payFixed);
    final AnnuityCouponIborSpreadDefinition floatingLegDefinition = getFloatingSwapLegDefinition(effectiveDate, maturityDate, floatLeg, calendar, currency, conventions, !payFixed);
    return new SwapFixedIborSpreadDefinition(fixedLegDefinition, floatingLegDefinition);
  }

  private SwapIborIborDefinition getTenorSwapDefinition(final SwapSecurity swapSecurity) { //FixedIncomeInstrumentDefinition<TenorSwap<Payment>> 
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FloatingInterestRateLeg floatPayLeg = (FloatingInterestRateLeg) payLeg;
    final FloatingInterestRateLeg floatReceiveLeg = (FloatingInterestRateLeg) receiveLeg;
    final Calendar calendar = getCalendar(payLeg.getRegionIdentifier());
    final String currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency().getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_TENOR_SWAP"));
    final AnnuityCouponIborSpreadDefinition payLegDefinition = getFloatingSwapLegDefinition(effectiveDate, maturityDate, floatPayLeg, calendar, currency, conventions, true);
    final AnnuityCouponIborSpreadDefinition receiveLegDefinition = getFloatingSwapLegDefinition(effectiveDate, maturityDate, floatReceiveLeg, calendar, currency, conventions, false);
    return new SwapIborIborDefinition(payLegDefinition, receiveLegDefinition);
  }

  private AnnuityCouponFixedDefinition getFixedSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FixedInterestRateLeg fixedLeg, final Calendar calendar,
      final String currency, final ConventionBundle conventions, boolean isPayer) {
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final double fixedRate = fixedLeg.getRate();
    return AnnuityCouponFixedDefinition.from(((InterestRateNotional) fixedLeg.getNotional()).getCurrency(), effectiveDate, maturityDate, fixedLeg.getFrequency(), calendar, fixedLeg.getDayCount(),
        fixedLeg.getBusinessDayConvention(), conventions.isEOMConvention(), notional, fixedRate, isPayer);
  }

  private AnnuityCouponIborSpreadDefinition getFloatingSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingInterestRateLeg floatLeg,
      final Calendar calendar, final String currency, final ConventionBundle conventions, boolean isPayer) {
    //    final ZonedDateTime[] nominalDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, floatLeg.getFrequency());
    //TODO are settlement days really 0 for swaps?    
    //    final ZonedDateTime[] settlementDates = ScheduleCalculator.getAdjustedDateSchedule(nominalDates, floatLeg.getBusinessDayConvention(), calendar, 0);
    //    final ZonedDateTime[] resetDates = ScheduleCalculator.getAdjustedResetDateSchedule(effectiveDate, nominalDates, floatLeg.getBusinessDayConvention(), calendar,
    //        conventions.getSwapFloatingLegSettlementDays());
    //    final ZonedDateTime[] maturityDates = ScheduleCalculator.getAdjustedMaturityDateSchedule(effectiveDate, nominalDates, floatLeg.getBusinessDayConvention(), calendar, floatLeg.getFrequency());
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    //final double initialRate = floatLeg.getInitialFloatingRate();
    final double spread = floatLeg.getSpread();
    //    final SwapConvention convention = new SwapConvention(0, conventions.getSwapFloatingLegDayCount(), conventions.getSwapFloatingLegBusinessDayConvention(), calendar, false, currency
    //        + "_FLOATING_LEG_SWAP_CONVENTION");
    // TODO: index period can be different from leg period
    // FIXME: convert frequency to period in a better way
    Frequency freq = floatLeg.getFrequency();
    Period tenor = Period.ofMonths(3);
    if (freq.getConventionName() == Frequency.ANNUAL_NAME) {
      tenor = Period.ofMonths(12);
    } else {
      if (freq.getConventionName() == Frequency.SEMI_ANNUAL_NAME) {
        tenor = Period.ofMonths(6);
      } else {
        if (freq.getConventionName() == Frequency.QUARTERLY_NAME) {
          tenor = Period.ofMonths(3);
        } else {
          if (freq.getConventionName() == Frequency.MONTHLY_NAME) {
            tenor = Period.ofMonths(1);
          }
        }
      }
    }

    final IborIndex index = new IborIndex(((InterestRateNotional) floatLeg.getNotional()).getCurrency(), tenor, conventions.getSettlementDays(), calendar, floatLeg.getDayCount(),
        floatLeg.getBusinessDayConvention(), conventions.isEOMConvention());
    AnnuityCouponIborSpreadDefinition annuity = AnnuityCouponIborSpreadDefinition.from(effectiveDate, maturityDate, notional, index, spread, isPayer);
    //annuity.getNthPayment(0).fixingProcess(initialRate);
    return annuity;
  }

  private LocalDate[] getBondSchedule(final BondSecurity security, final LocalDate maturityDate, final SimpleFrequency simpleFrequency, final ConventionBundle convention, final LocalDate datedDate) {
    LocalDate[] schedule = ScheduleFactory.getSchedule(datedDate, maturityDate, simpleFrequency, convention.isEOMConvention(), convention.calculateScheduleFromMaturity(), false);
    // front stub
    if (schedule[0].equals(security.getFirstCouponDate().toLocalDate())) {
      final int n = schedule.length;
      final LocalDate[] temp = new LocalDate[n + 1];
      temp[0] = datedDate;
      for (int i = 1; i < n + 1; i++) {
        temp[i] = schedule[i - 1];
      }
      schedule = temp;
    }
    if (!schedule[1].toLocalDate().equals(security.getFirstCouponDate().toLocalDate())) {
      s_logger.warn("Security first coupon date did not match calculated first coupon date: " + schedule[1].toLocalDate() + ", " + security.getFirstCouponDate().toLocalDate());
    }
    return schedule;
  }

  private Calendar getCalendar(final Identifier regionId) {
    return new HolidaySourceCalendarAdapter(_holidaySource, RegionUtils.getRegions(_regionSource, regionId));
  }
}
