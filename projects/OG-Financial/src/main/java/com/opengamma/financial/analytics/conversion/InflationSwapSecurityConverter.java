/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.INFLATION_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IRS_FIXED_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.PRICE_INDEX;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationYearOnYearDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
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
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;

  /**
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   */
  public InflationSwapSecurityConverter(final ConventionSource conventionSource, final RegionSource regionSource,
      final HolidaySource holidaySource) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    _conventionSource = conventionSource;
    _regionSource = regionSource;
    _holidaySource = holidaySource;
  }

  @Override
  public InstrumentDefinition<?> visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final PriceIndexConvention indexConvention = _conventionSource.getConvention(PriceIndexConvention.class, getIds(currency, PRICE_INDEX));
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Price index convention with id " + getIds(currency, PRICE_INDEX) + " was null");
    }
    final SwapFixedLegConvention fixedLegConvention = _conventionSource.getConvention(SwapFixedLegConvention.class, getIds(currency, IRS_FIXED_LEG));
    if (fixedLegConvention == null) {
      throw new OpenGammaRuntimeException("Swap fixed leg convention with id " + getIds(currency, IRS_FIXED_LEG) + " was null");
    }
    final InflationLegConvention inflationLegConvention = _conventionSource.getConvention(InflationLegConvention.class, getIds(currency, INFLATION_LEG));
    if (inflationLegConvention == null) {
      throw new OpenGammaRuntimeException("Inflation leg convention with id " + getIds(currency, INFLATION_LEG) + " was null");
    }
    final IndexPrice priceIndex = new IndexPrice(indexConvention.getName(), currency);
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
    final int settlementDays = fixedLegConvention.getSettlementDays();
    final boolean isEOM = fixedLegConvention.isIsEOM();
    final DayCount fixedLegDayCount = fixedLeg.getDayCount();
    final BusinessDayConvention businessDayConvention = fixedLeg.getBusinessDayConvention();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegion());
    final ZoneOffset zone = ZoneOffset.UTC; //TODO
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
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(security.getEffectiveDate(), settlementDays, calendar).toLocalDate().atStartOfDay(zone);
    final double fixedRate = fixedLeg.getRate();
    final int conventionalMonthLag = indexLeg.getConventionalIndexationLag();
    final int quotationMonthLag = indexLeg.getQuotationIndexationLag();
    final boolean exchangeNotional = security.isExchangeInitialNotional() && security.isExchangeFinalNotional();
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    if (isMonthly) {
      return SwapFixedInflationYearOnYearDefinition.fromMonthly(priceIndex, settlementDate, paymentPeriod, (int) (maturityTenor.toTotalMonths() / 12), fixedRate,
          notional, isPayer, businessDayConvention, calendar, isEOM, fixedLegDayCount, conventionalMonthLag, quotationMonthLag, exchangeNotional);
    }
    return SwapFixedInflationYearOnYearDefinition.fromInterpolation(priceIndex, settlementDate, paymentPeriod, maturityTenor, fixedRate,
        notional, isPayer, businessDayConvention, calendar, isEOM, fixedLegDayCount, conventionalMonthLag, quotationMonthLag, exchangeNotional);
  }

  @Override
  public InstrumentDefinition<?> visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final PriceIndexConvention indexConvention = _conventionSource.getConvention(PriceIndexConvention.class, getIds(currency, PRICE_INDEX));
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Price index convention with id " + getIds(currency, PRICE_INDEX) + " was null");
    }
    final SwapFixedLegConvention fixedLegConvention = _conventionSource.getConvention(SwapFixedLegConvention.class, getIds(currency, IRS_FIXED_LEG));
    if (fixedLegConvention == null) {
      throw new OpenGammaRuntimeException("Swap fixed leg convention with id " + getIds(currency, IRS_FIXED_LEG) + " was null");
    }
    final InflationLegConvention inflationLegConvention = _conventionSource.getConvention(InflationLegConvention.class, getIds(currency, INFLATION_LEG));
    if (inflationLegConvention == null) {
      throw new OpenGammaRuntimeException("Inflation leg convention with id " + getIds(currency, INFLATION_LEG) + " was null");
    }
    final IndexPrice priceIndex = new IndexPrice(indexConvention.getName(), currency);
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
    final int settlementDays = fixedLegConvention.getSettlementDays();
    final boolean isEOM = fixedLegConvention.isIsEOM();
    final BusinessDayConvention businessDayConvention = fixedLeg.getBusinessDayConvention();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegion());
    final ZoneOffset zone = ZoneOffset.UTC; //TODO
    
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
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(security.getEffectiveDate(), settlementDays, calendar).toLocalDate().atStartOfDay(zone);
    final double fixedRate = fixedLeg.getRate();
    final int conventionalMonthLag = indexLeg.getConventionalIndexationLag();
    final int quotationMonthLag = indexLeg.getQuotationIndexationLag();
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    if (isMonthly) {
      return SwapFixedInflationZeroCouponDefinition.fromMonthly(priceIndex, settlementDate, swapMaturityTenor, fixedRate,
          notional, isPayer, businessDayConvention, calendar, isEOM, conventionalMonthLag, quotationMonthLag);
    }
    return SwapFixedInflationZeroCouponDefinition.fromInterpolation(priceIndex, settlementDate, swapMaturityTenor, fixedRate,
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
