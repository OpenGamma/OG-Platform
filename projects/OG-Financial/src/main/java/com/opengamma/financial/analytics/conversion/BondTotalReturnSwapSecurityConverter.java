/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Collections;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.NotionalExchange;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link BondTotalReturnSwapSecurity} and {@link EquityTotalReturnSwapSecurity} classes to
 * {@link BondTotalReturnSwapDefinition}s and {@link EquityTotalReturnSwapDefinition} respectively,
 * which are required for use in the analytics library.
 */
public class BondTotalReturnSwapSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The security source */
  private final SecuritySource _securitySource;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param securitySource The security source, not null
   */
  public BondTotalReturnSwapSecurityConverter(final ConventionSource conventionSource, final HolidaySource holidaySource,
      final RegionSource regionSource, final SecuritySource securitySource) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(regionSource, "regionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _securitySource = securitySource;
  }

  @Override
  public BondTotalReturnSwapDefinition visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final boolean isPayer = false;
    final LocalDate startDate = security.getEffectiveDate();
    final LocalDate endDate = security.getMaturityDate();
    final FloatingInterestRateSwapLeg leg = security.getFundingLeg();
    final NotionalExchange notionalExchange = NotionalExchange.NO_EXCHANGE;
    final AnnuityDefinition<? extends PaymentDefinition> annuityDefinition = AnnuityUtils.buildFloatingAnnuityDefinition(_conventionSource, _holidaySource, isPayer,
        startDate, endDate, notionalExchange, leg);
    final FinancialSecurity underlying = (FinancialSecurity) _securitySource.getSingle(security.getAssetId().toBundle()); //TODO ignoring version
    if (underlying instanceof BondSecurity) {
      throw new OpenGammaRuntimeException("Underlying for bond TRS was not a bond");
    }
    final BondSecurity bond = (BondSecurity) underlying;
    final BondConvention convention = getConvention(bond, _conventionSource);
    final LegalEntity legalEntity = BondAndBondFutureTradeWithEntityConverter.getLegalEntityForBond(Collections.<String, String>emptyMap(), bond);
    final ExternalId regionId = ExternalSchemes.financialRegionId(bond.getIssuerDomicile());
    if (regionId == null) {
      throw new OpenGammaRuntimeException("Could not find region for " + bond.getIssuerDomicile());
    }
    final Currency currency = bond.getCurrency();
    final Calendar calendar;
    // If the bond is Supranational, we use the calendar derived from the currency of the bond.
    // this may need revisiting.
    if (regionId.getValue().equals("SNAT")) { // Supranational
      calendar = CalendarUtils.getCalendar(_holidaySource, currency);
    } else {
      calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    }
    if (bond.getInterestAccrualDate() == null) {
      throw new OpenGammaRuntimeException("Bond first interest accrual date was null");
    }
    final ZoneId zone = bond.getInterestAccrualDate().getZone();
    final ZonedDateTime firstAccrualDate = ZonedDateTime.of(bond.getInterestAccrualDate().toLocalDate().atStartOfDay(), zone);
    final ZonedDateTime maturityDate = ZonedDateTime.of(bond.getLastTradeDate().getExpiry().toLocalDate().atStartOfDay(), zone);
    final double rate = bond.getCouponRate() / 100;
    final DayCount dayCount = bond.getDayCount();
    final BusinessDayConvention businessDay = BusinessDayConventions.FOLLOWING;
    final boolean isEOM = convention.isIsEOM();
    final YieldConvention yieldConvention = bond.getYieldConvention();
    final int settlementDays = convention.getSettlementDays();
    final Period paymentPeriod = ConversionUtils.getTenor(bond.getCouponFrequency());
    final ZonedDateTime firstCouponDate = ZonedDateTime.of(bond.getFirstCouponDate().toLocalDate().atStartOfDay(), zone);
    final double notional = security.getNotionalAmount();
    final int exDividendDays = 0;
    final BondFixedSecurityDefinition bondDefinition = BondFixedSecurityDefinition.from(currency, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, rate, settlementDays, notional, exDividendDays, calendar, dayCount, businessDay,
        yieldConvention, isEOM, legalEntity);
    return new BondTotalReturnSwapDefinition(annuityDefinition, bondDefinition);
  }

  /**
   * Gets the convention for a bond from its domicile.
   * @param bond The bond
   * @return The convention
   */
  private static BondConvention getConvention(final BondSecurity bond, final ConventionSource conventionSource) {
    return bond.accept(new FinancialSecurityVisitorAdapter<BondConvention>() {

      @Override
      public BondConvention visitCorporateBondSecurity(final CorporateBondSecurity corporateBond) {
        final String domicile = corporateBond.getIssuerDomicile();
        final ExternalId countryId = ExternalSchemes.countryRegionId(Country.of(domicile));
        final ExternalId currencyId = ExternalSchemes.currencyRegionId(corporateBond.getCurrency());
        final ExternalIdBundle conventionId = ExternalIdBundle.of(countryId, currencyId);
        final BondConvention convention = conventionSource.getSingle(conventionId, BondConvention.class);
        if (convention == null) {
          throw new OpenGammaRuntimeException("No bond convention found for domicile " + domicile);
        }
        return convention;
      }

      @Override
      public BondConvention visitGovernmentBondSecurity(final GovernmentBondSecurity governmentBond) {
        final String domicile = governmentBond.getIssuerDomicile();
        final ExternalId countryId = ExternalSchemes.countryRegionId(Country.of(domicile));
        final ExternalId currencyId = ExternalSchemes.currencyRegionId(governmentBond.getCurrency());
        final ExternalIdBundle conventionId = ExternalIdBundle.of(countryId, currencyId);
        final BondConvention convention = conventionSource.getSingle(conventionId, BondConvention.class);
        if (convention == null) {
          throw new OpenGammaRuntimeException("No bond convention found for domicile " + domicile);
        }
        return convention;
      }
    });
  }
}
