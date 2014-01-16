/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.financial.analytics.conversion.BondAndBondFutureTradeWithEntityConverter.MARKET_STRING;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.beans.impl.flexi.FlexiBean;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

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
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
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
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class BondNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The region source */
  private final RegionSource _regionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The convention source */
  private final ConventionBundleSource _conventionSource;
  /** The security source */
  private final SecuritySource _securitySource;
  /** The market data */
  private final SnapshotDataBundle _marketData;
  /** The market data id */
  private final ExternalId _dataId;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;
  /** Rating agency strings */
  private static final String[] RATING_STRINGS = new String[] {"RatingMoody", "RatingFitch"};

  /**
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param securitySource The security source, not null
   * @param marketData The market data, not null
   * @param dataId The market data id, not null
   * @param valuationTime The valuation time, not null
   */
  public BondNodeConverter(final ConventionBundleSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SecuritySource securitySource, final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    _regionSource = regionSource;
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _securitySource = securitySource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
  }

  @Override
  public InstrumentDefinition<?> visitBondNode(final BondNode bondNode) {
    final Double yield = _marketData.getDataPoint(_dataId);
    if (yield == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    final Security security = _securitySource.getSingle(_dataId.toBundle()); //TODO this is in here because
    // we can't ask for data by ISIN directly.
    if (!(security instanceof BondSecurity)) {
      throw new OpenGammaRuntimeException("Could not get security for " + security);
    }
    final BondSecurity bondSecurity = (BondSecurity) security;
    final ExternalId regionId = ExternalSchemes.financialRegionId(bondSecurity.getIssuerDomicile());
    if (regionId == null) {
      throw new OpenGammaRuntimeException("Could not find region for " + bondSecurity.getIssuerDomicile());
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = bondSecurity.getCurrency();
    final ZoneId zone = bondSecurity.getInterestAccrualDate().getZone();
    final ZonedDateTime firstAccrualDate = ZonedDateTime.of(bondSecurity.getInterestAccrualDate().toLocalDate().atStartOfDay(), zone);
    final ZonedDateTime maturityDate = ZonedDateTime.of(bondSecurity.getLastTradeDate().getExpiry().toLocalDate().atStartOfDay(), zone);
    final double rate = bondSecurity.getCouponRate() / 100;
    final DayCount dayCount = bondSecurity.getDayCount();
    final BusinessDayConvention businessDay = BusinessDayConventions.FOLLOWING;
    final String domicile = bondSecurity.getIssuerDomicile();
    if (domicile == null) {
      throw new OpenGammaRuntimeException("bond security domicile cannot be null");
    }
    final String conventionName = domicile + "_TREASURY_BOND_CONVENTION";
    final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
    if (convention == null) {
      throw new OpenGammaRuntimeException("Convention called " + conventionName + " was null");
    }
    if (convention.isEOMConvention() == null) {
      throw new OpenGammaRuntimeException("Could not get EOM convention information from " + conventionName);
    }
    final boolean isEOM = convention.isEOMConvention();
    final YieldConvention yieldConvention = bondSecurity.getYieldConvention();
    if (bondSecurity.getCouponType().equals("NONE") || bondSecurity.getCouponType().equals("ZERO COUPON")) { //TODO find where string is
      return new PaymentFixedDefinition(currency, maturityDate, 1);
    }
    if (convention.getBondSettlementDays(firstAccrualDate, maturityDate) == null) {
      throw new OpenGammaRuntimeException("Could not get bond settlement days from " + conventionName);
    }
    final int settlementDays = convention.getBondSettlementDays(firstAccrualDate, maturityDate);
    final Period paymentPeriod = getTenor(bondSecurity.getCouponFrequency());
    final ZonedDateTime firstCouponDate = ZonedDateTime.of(bondSecurity.getFirstCouponDate().toLocalDate().atStartOfDay(), zone);
    final ExternalIdBundle identifiers = security.getExternalIdBundle();
    final String isin = identifiers.getValue(ExternalSchemes.ISIN);
    final String ticker = isin == null ? null : isin;
    final String shortName = bondSecurity.getIssuerName();
    final String sectorName = bondSecurity.getIssuerType();
    final FlexiBean classifications = new FlexiBean();
    classifications.put(MARKET_STRING, bondSecurity.getMarket());
    final Sector sector = Sector.of(sectorName, classifications);
    final Region region = Region.of(bondSecurity.getIssuerDomicile(), Country.of(bondSecurity.getIssuerDomicile()), bondSecurity.getCurrency());
    final Map<String, String> securityAttributes = security.getAttributes();
    Set<CreditRating> creditRatings = null;
    for (final String ratingString : RATING_STRINGS) {
      if (securityAttributes.containsKey(ratingString)) {
        if (creditRatings == null) {
          creditRatings = new HashSet<>();
        }
        creditRatings.add(CreditRating.of(securityAttributes.get(ratingString), ratingString, true));
      }
    }
    final LegalEntity legalEntity = new LegalEntity(ticker, shortName, creditRatings, sector, region);
    final BondFixedSecurityDefinition securityDefinition = BondFixedSecurityDefinition.from(currency, firstAccrualDate,
        firstCouponDate, maturityDate, paymentPeriod, rate, settlementDays, calendar, dayCount, businessDay, yieldConvention, isEOM,
        legalEntity);
    // TODO: PLAT-5253 Standard days to settlement are missing in bond description.
    return BondFixedTransactionDefinition.fromYield(securityDefinition, 1, _valuationTime, yield);
  }

  /**
   * Gets the tenor for a frequency.
   * @param freq The frequency
   * @return The tenor
   */
  private static Period getTenor(final Frequency freq) {
    if (freq instanceof PeriodFrequency) {
      return ((PeriodFrequency) freq).getPeriod();
    } else if (freq instanceof SimpleFrequency) {
      return ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
    }
    throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
  }
}
