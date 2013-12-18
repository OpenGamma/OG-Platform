/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.beans.impl.flexi.FlexiBean;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class BondTradeWithEntityConverter {
  /** Excluded coupon types */
  private static final Set<String> EXCLUDED_TYPES = Sets.newHashSet("FLOAT RATE NOTE", "TOGGLE PIK NOTES");
  /** Rating agency strings */
  private static final String[] RATING_STRINGS = new String[] {"RatingMoody"};
  /** Sector name string */
  public static final String SECTOR_STRING = "IndustrySector";
  /** Market type string */
  public static final String MARKET_STRING = "Market";
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
  public BondTradeWithEntityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  /**
   * Converts a fixed coupon bond trade into a {@link BondFixedTransactionDefinition}.
   * @param trade The trade, not null. Must be a {@link BondSecurity}
   * @return The transaction definition
   */
  public BondFixedTransactionDefinition convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.isTrue(trade.getSecurity() instanceof BondSecurity, "Can only handle trades with security type BondSecurity");
    final LocalDate tradeDate = trade.getTradeDate();
    if (tradeDate == null) {
      throw new OpenGammaRuntimeException("Trade date should not be null");
    }
    if (trade.getTradeTime() == null) {
      throw new OpenGammaRuntimeException("Trade time should not be null");
    }
    if (trade.getPremium() == null) {
      throw new OpenGammaRuntimeException("Trade premium should not be null.");
    }
    final BondSecurity security = (BondSecurity) trade.getSecurity();
    final ExternalIdBundle identifiers = security.getExternalIdBundle();
    final String isin = identifiers.getValue(ExternalSchemes.ISIN);
    final String ticker = isin == null ? null : isin;
    final String shortName = security.getIssuerName();
    Set<CreditRating> creditRatings = null;
    final Map<String, String> attributes = trade.getAttributes();
    for (final String ratingString : RATING_STRINGS) {
      if (attributes.containsKey(ratingString)) {
        if (creditRatings == null) {
          creditRatings = new HashSet<>();
        }
        creditRatings.add(CreditRating.of(attributes.get(ratingString), ratingString, true));
      }
    }
    final String sectorName = security.getIssuerType();
    final FlexiBean classifications = new FlexiBean();
    classifications.put(MARKET_STRING, security.getMarket());
    if (attributes.containsKey(SECTOR_STRING)) {
      classifications.put(SECTOR_STRING, attributes.get(SECTOR_STRING));
    }
    final Sector sector = Sector.of(sectorName, classifications);
    final Region region = Region.of(security.getIssuerDomicile(), Country.of(security.getIssuerDomicile()), security.getCurrency());
    final LegalEntity legalEntity = new LegalEntity(ticker, shortName, creditRatings, sector, region);
    final InstrumentDefinition<?> underlying = getUnderlyingBond(security, legalEntity);
    if (!(underlying instanceof BondFixedSecurityDefinition)) {
      throw new OpenGammaRuntimeException("Can only handle fixed coupon bonds");
    }
    final BondFixedSecurityDefinition bond = (BondFixedSecurityDefinition) underlying;
    final int quantity = trade.getQuantity().intValue(); // MH - 9-May-2013: changed from 1. // REVIEW: The quantity mechanism should be reviewed.
    final ZonedDateTime settlementDate = trade.getTradeDate().atTime(trade.getTradeTime()).atZoneSameInstant(ZoneOffset.UTC); //TODO get the real time zone
    final double price = trade.getPremium().doubleValue();
    return new BondFixedTransactionDefinition(bond, quantity, settlementDate, price);
  }

  /**
   * Creates the underlying bond using the full legal entity information available.
   * @param security The bond security
   * @param legalEntity The legal entity
   * @return The underlying bond definition
   */
  @SuppressWarnings("synthetic-access")
  private InstrumentDefinition<?> getUnderlyingBond(final FinancialSecurity security, final LegalEntity legalEntity) {
    return security.accept(new FinancialSecurityVisitorAdapter<InstrumentDefinition<?>>() {

      @Override
      public InstrumentDefinition<?> visitCorporateBondSecurity(final CorporateBondSecurity bond) {
        final String domicile = bond.getIssuerDomicile();
        ArgumentChecker.notNull(domicile, "bond security domicile cannot be null");
        final String conventionName = domicile + "_CORPORATE_BOND_CONVENTION";
        final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
        if (convention == null) {
          throw new OpenGammaRuntimeException("No corporate bond convention found for domicile " + domicile);
        }
        return visitBondSecurity(bond, convention, conventionName);
      }

      @Override
      public InstrumentDefinition<?> visitGovernmentBondSecurity(final GovernmentBondSecurity bond) {
        final String domicile = bond.getIssuerDomicile();
        if (domicile == null) {
          throw new OpenGammaRuntimeException("bond security domicile cannot be null");
        }
        final String conventionName = domicile + "_TREASURY_BOND_CONVENTION";
        final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
        if (convention == null) {
          throw new OpenGammaRuntimeException("Convention called " + conventionName + " was null");
        }
        return visitBondSecurity(bond, convention, conventionName);
      }

      /**
       * Creates {@link BondFixedSecurityDefinition} for fixed-coupon bonds or {@link PaymentFixedDefinition}
       * for zero-coupon bonds.
       * @param bond The security
       * @param convention The convention
       * @param conventionName The convention name
       * @return The definition
       */
      private InstrumentDefinition<?> visitBondSecurity(final BondSecurity bond, final ConventionBundle convention,
          final String conventionName) {
        if (EXCLUDED_TYPES.contains(bond.getCouponType())) {
          throw new UnsupportedOperationException("Cannot support bonds with coupon of type " + bond.getCouponType());
        }
        final ExternalId regionId = ExternalSchemes.financialRegionId(bond.getIssuerDomicile());
        if (regionId == null) {
          throw new OpenGammaRuntimeException("Could not find region for " + bond.getIssuerDomicile());
        }
        final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
        final Currency currency = bond.getCurrency();
        final ZoneId zone = bond.getInterestAccrualDate().getZone();
        final ZonedDateTime firstAccrualDate = ZonedDateTime.of(bond.getInterestAccrualDate().toLocalDate().atStartOfDay(), zone);
        final ZonedDateTime maturityDate = ZonedDateTime.of(bond.getLastTradeDate().getExpiry().toLocalDate().atStartOfDay(), zone);
        final double rate = bond.getCouponRate() / 100;
        final DayCount dayCount = bond.getDayCount();
        final BusinessDayConvention businessDay = BusinessDayConventions.FOLLOWING;
        if (convention.isEOMConvention() == null) {
          throw new OpenGammaRuntimeException("Could not get EOM convention information from " + conventionName);
        }
        final boolean isEOM = convention.isEOMConvention();
        final YieldConvention yieldConvention = bond.getYieldConvention();
        if (bond.getCouponType().equals("NONE") || bond.getCouponType().equals("ZERO COUPON")) { //TODO find where string is
          return new PaymentFixedDefinition(currency, maturityDate, 1);
        }
        if (convention.getBondSettlementDays(firstAccrualDate, maturityDate) == null) {
          throw new OpenGammaRuntimeException("Could not get bond settlement days from " + conventionName);
        }
        final int settlementDays = convention.getBondSettlementDays(firstAccrualDate, maturityDate);
        final Period paymentPeriod = getTenor(bond.getCouponFrequency());
        final ZonedDateTime firstCouponDate = ZonedDateTime.of(bond.getFirstCouponDate().toLocalDate().atStartOfDay(), zone);
        return BondFixedSecurityDefinition.from(currency, firstAccrualDate, firstCouponDate, maturityDate, paymentPeriod, rate, settlementDays, calendar, dayCount, businessDay,
            yieldConvention, isEOM, legalEntity);
      }

    });
  }

  /**
   * Gets the tenor for a frequency.
   * @param freq The frequency
   * @return The tenor
   */
  /* package */ static Period getTenor(final Frequency freq) {
    if (freq instanceof PeriodFrequency) {
      return ((PeriodFrequency) freq).getPeriod();
    } else if (freq instanceof SimpleFrequency) {
      return ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
    }
    throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
  }

}
