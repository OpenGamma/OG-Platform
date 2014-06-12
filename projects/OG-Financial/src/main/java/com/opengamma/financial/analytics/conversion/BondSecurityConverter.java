/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link BondSecurity} to the equivalent {@BondFixedSecurityDefinition} or
 * {@link PaymentFixedDefinition} (for zero-coupons) for use in pricing.
 */
public class BondSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** Excluded coupon types */
  private static final Set<String> EXCLUDED_TYPES = Sets.newHashSet("FLOAT RATE NOTE", "TOGGLE PIK NOTES");
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The convention bundle source */
  private final ConventionBundleSource _conventionSource;
  /** The region source */
  private final RegionSource _regionSource;

  /**
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   */
  public BondSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public InstrumentDefinition<?> visitCorporateBondSecurity(final CorporateBondSecurity security) {
    final String domicile = security.getIssuerDomicile();
    ArgumentChecker.notNull(domicile, "bond security domicile cannot be null");
    final String conventionName = domicile + "_CORPORATE_BOND_CONVENTION";
    final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
    if (convention == null) {
      throw new OpenGammaRuntimeException("No corporate bond convention found for domicile " + domicile);
    }
    return visitBondSecurity(security, convention, conventionName);
  }

  @Override
  public InstrumentDefinition<?> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    final String domicile = security.getIssuerDomicile();
    if (domicile == null) {
      throw new OpenGammaRuntimeException("bond security domicile cannot be null");
    }
    final String conventionName = domicile + "_TREASURY_BOND_CONVENTION";
    final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
    if (convention == null) {
      throw new OpenGammaRuntimeException("Convention called " + conventionName + " was null");
    }
    return visitBondSecurity(security, convention, conventionName);
  }

  /**
   * Creates {@link BondFixedSecurityDefinition} for fixed-coupon bonds or {@link PaymentFixedDefinition}
   * for zero-coupon bonds.
   * @param security The security
   * @param convention The convention
   * @param conventionName The convention name
   * @return The definition
   */
  private InstrumentDefinition<?> visitBondSecurity(final BondSecurity security, final ConventionBundle convention,
      final String conventionName) {
    if (EXCLUDED_TYPES.contains(security.getCouponType())) {
      throw new UnsupportedOperationException("Cannot support bonds with coupon of type " + security.getCouponType());
    }
    final ExternalId regionId = ExternalSchemes.financialRegionId(security.getIssuerDomicile());
    if (regionId == null) {
      throw new OpenGammaRuntimeException("Could not find region for " + security.getIssuerDomicile());
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = security.getCurrency();
    final ZoneId zone = security.getInterestAccrualDate().getZone();
    final ZonedDateTime firstAccrualDate = ZonedDateTime.of(security.getInterestAccrualDate().toLocalDate().atStartOfDay(), zone);
    final ZonedDateTime maturityDate = ZonedDateTime.of(security.getLastTradeDate().getExpiry().toLocalDate().atStartOfDay(), zone);
    final double rate = security.getCouponRate() / 100;
    final DayCount dayCount = security.getDayCount();
    final BusinessDayConvention businessDay = BusinessDayConventions.FOLLOWING;
    if (convention.isEOMConvention() == null) {
      throw new OpenGammaRuntimeException("Could not get EOM convention information from " + conventionName);
    }
    final boolean isEOM = convention.isEOMConvention();
    final YieldConvention yieldConvention = security.getYieldConvention();
    if (security.getCouponType().equals("NONE") || security.getCouponType().equals("ZERO COUPON")) { //TODO find where string is
      return new PaymentFixedDefinition(currency, maturityDate, 1);
    }
    if (convention.getBondSettlementDays(firstAccrualDate, maturityDate) == null) {
      throw new OpenGammaRuntimeException("Could not get bond settlement days from " + conventionName);
    }
    final int settlementDays = convention.getBondSettlementDays(firstAccrualDate, maturityDate);
    final Period paymentPeriod = ConversionUtils.getTenor(security.getCouponFrequency());
    final ZonedDateTime firstCouponDate = ZonedDateTime.of(security.getFirstCouponDate().toLocalDate().atStartOfDay(), zone);
    return BondFixedSecurityDefinition.from(currency, firstAccrualDate, firstCouponDate, maturityDate, paymentPeriod, rate, settlementDays, calendar, dayCount, businessDay,
        yieldConvention, isEOM, security.getIssuerName());
  }

  @Override
  public InstrumentDefinition<?> visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    throw new NotImplementedException();
  }

}
