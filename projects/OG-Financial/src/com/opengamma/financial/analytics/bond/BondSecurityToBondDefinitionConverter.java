/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.analytics.schedule.ScheduleCalculator;
import com.opengamma.financial.bond.BondConvention;
import com.opengamma.financial.bond.BondDefinition;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.schedule.ScheduleFactory;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class BondSecurityToBondDefinitionConverter {
  private static final Logger s_log = LoggerFactory.getLogger(BondSecurityToBondDefinitionConverter.class);
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;

  public BondSecurityToBondDefinitionConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  public BondDefinition getBond(final BondSecurity security) {
    return getBond(security, true);
  }

  public BondDefinition getBond(final BondSecurity security, final boolean rollToSettlement) {
    Validate.notNull(security, "security");
    final Currency currency = security.getCurrency();
    final Identifier id = Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_TREASURY_BOND_CONVENTION");
    final ConventionBundle convention = _conventionSource.getConventionBundle(id);
    return getBond(security, rollToSettlement, convention);
  }

  /**
   * 
   * @param security A BondSecurity, not null
   * @param rollToSettlement if true the cash-flow calculation date is rolled forward to the settlement date 
   * @param convention A convention bundle, not null
   * @return a Bond
   */
  public BondDefinition getBond(final BondSecurity security, final boolean rollToSettlement, final ConventionBundle convention) {
    Validate.notNull(security, "security");
    Validate.notNull(convention, "convention");
    final LocalDate lastTradeDate = security.getLastTradeDate().getExpiry().toLocalDate(); // was maturity
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
    final Currency currency = security.getCurrency();
    final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final LocalDate datedDate = security.getInterestAccrualDate().toZonedDateTime().toLocalDate();
    final int periodsPerYear = (int) simpleFrequency.getPeriodsPerYear();
    //TODO remove this when the definitions for USD treasuries are correct
    final DayCount daycount = currency.equals(Currency.getInstance("USD")) ? DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA") : security.getDayCountConvention();
    final boolean isEOMConvention = convention.isEOMConvention();
    final int settlementDays = convention.getSettlementDays();
    final LocalDate[] nominalDates = getBondSchedule(security, lastTradeDate, simpleFrequency, convention, datedDate);
    //    //TODO should be in schedule factory 
    final LocalDate[] settlementDates = (rollToSettlement ? ScheduleCalculator.getSettlementDateSchedule(nominalDates, calendar, businessDayConvention, convention.getSettlementDays())
        : ScheduleCalculator.getSettlementDateSchedule(nominalDates, calendar, businessDayConvention, 0));
    final double coupon = security.getCouponRate() / 100;
    final BondConvention bondConvention = new BondConvention(settlementDays, daycount, businessDayConvention, calendar, isEOMConvention, convention.getName(), convention.getExDividendDays(),
        SimpleYieldConvention.US_TREASURY_EQUIVALANT);
    return new BondDefinition(nominalDates, settlementDates, coupon, periodsPerYear, bondConvention);
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
      s_log.warn("Security first coupon date did not match calculated first coupon date: " + schedule[1].toLocalDate() + ", "
          + security.getFirstCouponDate().toZonedDateTime().toLocalDate());
      //throw new IllegalArgumentException("Security first coupon date did not match calculated first coupon date: " + schedule[1].toLocalDate() + ", "
      //    + security.getFirstCouponDate().toZonedDateTime().toLocalDate());
    }
    return schedule;
  }
}
