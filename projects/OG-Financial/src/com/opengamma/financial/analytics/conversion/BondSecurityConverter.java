/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondSecurityConverter implements BondSecurityVisitor<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public BondSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public InstrumentDefinition<?> visitCorporateBondSecurity(final CorporateBondSecurity security) {
    final String domicile = security.getIssuerDomicile();
    Validate.notNull(domicile, "bond security domicile cannot be null");
    final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, domicile + "_CORPORATE_BOND_CONVENTION"));
    return visitBondSecurity(security, convention);
  }

  @Override
  public InstrumentDefinition<?> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    final String domicile = security.getIssuerDomicile();
    Validate.notNull(domicile, "bond security domicile cannot be null");
    final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, domicile + "_TREASURY_BOND_CONVENTION"));
    return visitBondSecurity(security, convention);
  }

  public InstrumentDefinition<?> visitBondSecurity(final BondSecurity security, final ConventionBundle convention) {
    final ExternalId regionId = RegionUtils.financialRegionId(security.getIssuerDomicile());
    if (regionId == null) {
      throw new OpenGammaRuntimeException("Could not find region for " + security.getIssuerDomicile());
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = security.getCurrency();
    final ZonedDateTime firstAccrualDate = security.getInterestAccrualDate();
    final ZonedDateTime maturityDate = security.getLastTradeDate().getExpiry();
    final double rate = security.getCouponRate() / 100;
    final DayCount dayCount = security.getDayCount();
    final boolean isEOM = convention.isEOMConvention();
    final int settlementDays = convention.getSettlementDays();
    final BusinessDayConvention businessDay = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final YieldConvention yieldConvention = security.getYieldConvention();
    final Period paymentPeriod = getTenor(security.getCouponFrequency());
    return BondFixedSecurityDefinition.from(currency, maturityDate, firstAccrualDate, paymentPeriod, rate, settlementDays, calendar, dayCount, businessDay, yieldConvention, isEOM);
  }

  @Override
  public InstrumentDefinition<?> visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    throw new NotImplementedException();
  }

  private Period getTenor(final Frequency freq) {
    Period tenor;
    if (Frequency.ANNUAL_NAME.equals(freq.getConventionName())) {
      tenor = Period.ofMonths(12);
    } else if (Frequency.SEMI_ANNUAL_NAME.equals(freq.getConventionName())) {
      tenor = Period.ofMonths(6);
    } else if (Frequency.QUARTERLY_NAME.equals(freq.getConventionName())) {
      tenor = Period.ofMonths(3);
    } else if (Frequency.MONTHLY_NAME.equals(freq.getConventionName())) {
      tenor = Period.ofMonths(1);
    } else {
      throw new OpenGammaRuntimeException(
          "Can only handle annual, semi-annual, quarterly and monthly frequencies for floating swap legs");
    }
    return tenor;
  }
}
