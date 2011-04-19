/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.ActualActualISDA;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.bond.BondConvention;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.schedule.ScheduleFactory;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class BondSecurityConverter implements BondSecurityVisitor<FixedIncomeInstrumentDefinition<?>> {
  private static final Logger s_logger = LoggerFactory.getLogger(BondSecurityConverter.class);
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public BondSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public BondDefinition visitCorporateBondSecurity(final CorporateBondSecurity security) {
    final String domicile = security.getIssuerDomicile();
    Validate.notNull(domicile, "bond security domicile cannot be null");
    final ConventionBundle convention = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, domicile + "_CORPORATE_BOND_CONVENTION"));
    return visitBondSecurity(security, convention);
  }

  @Override
  public BondDefinition visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    final String domicile = security.getIssuerDomicile();
    Validate.notNull(domicile, "bond security domicile cannot be null");
    final ConventionBundle convention = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, domicile + "_TREASURY_BOND_CONVENTION"));
    return visitBondSecurity(security, convention);
  }

  public BondDefinition visitBondSecurity(final BondSecurity security, final ConventionBundle convention) {
    final LocalDate lastTradeDate = security.getLastTradeDate().getExpiry().toLocalDate();
    final Calendar calendar = CalendarUtil.getCalendar(_regionSource, _holidaySource, RegionUtils.financialRegionId(security.getIssuerDomicile()));
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
    DayCount daycount = security.getDayCountConvention();
    //TODO remove this when the bonds load correctly
    if (daycount instanceof ActualActualISDA) {
      daycount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
    }
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
  public BondDefinition visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    throw new NotImplementedException();
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
      s_logger.info("Security first coupon date did not match calculated first coupon date: " + schedule[1].toLocalDate() + ", " + security.getFirstCouponDate().toZonedDateTime().toLocalDate());
    }
    return schedule;
  }
}
