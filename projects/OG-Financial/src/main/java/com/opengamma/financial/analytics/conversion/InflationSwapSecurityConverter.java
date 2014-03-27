/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationYearOnYearDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link YearOnYearInflationSwapSecurity} and {@link ZeroCouponInflationSwapSecurity} into the
 * classes that the analytics library requires to calculate prices and risk.
 */
public class InflationSwapSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** A security source */
  private final SecuritySource _securitySource;
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;

  /**
   * @param securitySource The security source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   */
  public InflationSwapSecurityConverter(final SecuritySource securitySource, final ConventionSource conventionSource, final RegionSource regionSource,
      final HolidaySource holidaySource) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    _securitySource = securitySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
    _holidaySource = holidaySource;
  }

  @Override
  public InstrumentDefinition<?> visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    final FixedInflationSwapLeg fixedLeg;
    final InflationIndexSwapLeg indexLeg;
    final boolean isPayer;
    if (payLeg instanceof FixedInflationSwapLeg && receiveLeg instanceof InflationIndexSwapLeg) {
      fixedLeg = (FixedInflationSwapLeg) payLeg;
      indexLeg = (InflationIndexSwapLeg) receiveLeg;
      isPayer = true;
    } else if (payLeg instanceof InflationIndexSwapLeg && receiveLeg instanceof FixedInflationSwapLeg) {
      fixedLeg = (FixedInflationSwapLeg) receiveLeg;
      indexLeg = (InflationIndexSwapLeg) payLeg;
      isPayer = false;
    } else {
      throw new OpenGammaRuntimeException("Can only convert fixed / float inflation swaps");
    }

    final Security sec = _securitySource.getSingle(indexLeg.getIndexId().toBundle());
    if (sec == null) {
      throw new OpenGammaRuntimeException("Ibor index with id " + indexLeg.getIndexId() + " was null");
    }
    final com.opengamma.financial.security.index.PriceIndex indexSecurity = (com.opengamma.financial.security.index.PriceIndex) sec;
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final IndexPrice priceIndex = new IndexPrice(indexSecurity.getName(), currency);
    final boolean isEOM = fixedLeg.isEom();
    final DayCount fixedLegDayCount = fixedLeg.getDayCount();
    final BusinessDayConvention businessDayConvention = fixedLeg.getBusinessDayConvention();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, fixedLeg.getRegionId());
    final Period paymentPeriod = getTenor(indexLeg.getFrequency());
    final Period maturityTenor = security.getMaturityTenor().getPeriod();
    boolean isMonthly;
    switch (indexLeg.getInterpolationMethod()) {
      case MONTH_START_LINEAR:
        isMonthly = true;
        break;
      case NONE:
        isMonthly = false;
        break;
      default:
        throw new OpenGammaRuntimeException("Cannot handle interpolation method of type " + indexLeg.getInterpolationMethod());
    }
    final double fixedRate = fixedLeg.getRate();
    final int conventionalMonthLag = indexLeg.getConventionalIndexationLag();
    final int quotationMonthLag = indexLeg.getQuotationIndexationLag();
    final boolean exchangeNotional = security.isExchangeInitialNotional() && security.isExchangeFinalNotional();
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    if (isMonthly) {
      return SwapFixedInflationYearOnYearDefinition.fromMonthly(priceIndex, security.getEffectiveDate(), paymentPeriod, (int) (maturityTenor.toTotalMonths() / 12), fixedRate,
          notional, isPayer, businessDayConvention, calendar, isEOM, fixedLegDayCount, conventionalMonthLag, quotationMonthLag, exchangeNotional);
    }
    return SwapFixedInflationYearOnYearDefinition.fromInterpolation(priceIndex, security.getEffectiveDate(), paymentPeriod, maturityTenor, fixedRate,
        notional, isPayer, businessDayConvention, calendar, isEOM, fixedLegDayCount, conventionalMonthLag, quotationMonthLag, exchangeNotional);
  }

  @Override
  public InstrumentDefinition<?> visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    final FixedInflationSwapLeg fixedLeg;
    final InflationIndexSwapLeg indexLeg;
    final boolean isPayer;
    if (payLeg instanceof FixedInflationSwapLeg && receiveLeg instanceof InflationIndexSwapLeg) {
      fixedLeg = (FixedInflationSwapLeg) payLeg;
      indexLeg = (InflationIndexSwapLeg) receiveLeg;
      isPayer = true;
    } else if (payLeg instanceof InflationIndexSwapLeg && receiveLeg instanceof FixedInflationSwapLeg) {
      fixedLeg = (FixedInflationSwapLeg) receiveLeg;
      indexLeg = (InflationIndexSwapLeg) payLeg;
      isPayer = false;
    } else {
      throw new OpenGammaRuntimeException("Can only convert fixed / float inflation swaps");
    }

    final Security sec = _securitySource.getSingle(indexLeg.getIndexId().toBundle());
    if (sec == null) {
      throw new OpenGammaRuntimeException("Ibor index with id " + indexLeg.getIndexId() + " was null");
    }
    final com.opengamma.financial.security.index.PriceIndex indexSecurity = (com.opengamma.financial.security.index.PriceIndex) sec;
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final IndexPrice priceIndex = new IndexPrice(indexSecurity.getName(), currency);
    final boolean isEOM = fixedLeg.isEom();
    final BusinessDayConvention businessDayConvention = fixedLeg.getBusinessDayConvention();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, fixedLeg.getRegionId());
    final int swapMaturityTenor = (int) Math.round(indexLeg.getDayCount().getDayCountFraction(security.getEffectiveDate(), security.getMaturityDate()));
    boolean isMonthly;
    switch (indexLeg.getInterpolationMethod()) {
      case MONTH_START_LINEAR:
        isMonthly = true;
        break;
      case NONE:
        isMonthly = false;
        break;
      default:
        throw new OpenGammaRuntimeException("Cannot handle interpolation method of type " + indexLeg.getInterpolationMethod());
    }
    final double fixedRate = fixedLeg.getRate();
    final int conventionalMonthLag = indexLeg.getConventionalIndexationLag();
    final int quotationMonthLag = indexLeg.getQuotationIndexationLag();
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    if (isMonthly) {
      return SwapFixedInflationZeroCouponDefinition.fromMonthly(priceIndex, security.getEffectiveDate(), swapMaturityTenor, fixedRate,
          notional, isPayer, businessDayConvention, calendar, isEOM, conventionalMonthLag, quotationMonthLag);
    }
    return SwapFixedInflationZeroCouponDefinition.fromInterpolation(priceIndex, security.getEffectiveDate(), swapMaturityTenor, fixedRate,
        notional, isPayer, businessDayConvention, calendar, isEOM, conventionalMonthLag, quotationMonthLag);
  }

  private static Period getTenor(final Frequency freq) {
    if (freq instanceof PeriodFrequency) {
      return ((PeriodFrequency) freq).getPeriod();
    } else if (freq instanceof SimpleFrequency) {
      return ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
    }
    throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
  }
}
