/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

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
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.conversion.ConversionUtils;
import com.opengamma.financial.analytics.conversion.LegalEntityUtils;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
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
  /** The market data */
  private final SnapshotDataBundle _marketData;
  /** The market data id */
  private final ExternalId _dataId;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;

  /**
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param marketData The market data, not null
   * @param dataId The market data id, not null
   * @param valuationTime The valuation time, not null
   */
  //TODO PLAT-6801 replace conventionBundleSource with conventionSource
  public BondNodeConverter(
      RegionSource regionSource,
      HolidaySource holidaySource,
      ConventionBundleSource conventionSource,
      SnapshotDataBundle marketData,
      ExternalId dataId,
      ZonedDateTime valuationTime) {
    _regionSource = ArgumentChecker.notNull(regionSource, "regionSource");
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _conventionSource = ArgumentChecker.notNull(conventionSource, "conventionSource");
    _marketData = ArgumentChecker.notNull(marketData, "marketData");
    _dataId = ArgumentChecker.notNull(dataId, "dataId");
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
  }

  /**
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param securitySource The security source, not null
   * @param marketData The market data, not null
   * @param dataId The market data id, not null
   * @param valuationTime The valuation time, not null
   * @deprecated use constructor without securitySource
   */
  @Deprecated
  public BondNodeConverter(ConventionBundleSource conventionSource, HolidaySource holidaySource, RegionSource regionSource,
      SecuritySource securitySource, SnapshotDataBundle marketData, ExternalId dataId, ZonedDateTime valuationTime) {
    this(regionSource, holidaySource, conventionSource, marketData, dataId, valuationTime);
  }

  @Override
  public InstrumentDefinition<?> visitBondNode(BondNode bondNode) {
    Double yield = _marketData.getDataPoint(_dataId);
    if (yield == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    // TODO this is in here because we can't ask for data by ISIN directly.
    BondSecurity bondSecurity = SecurityLink.resolvable(_dataId, BondSecurity.class).resolve();
    ExternalId regionId = ExternalSchemes.financialRegionId(bondSecurity.getIssuerDomicile());
    if (regionId == null) {
      throw new OpenGammaRuntimeException("Could not find region for " + bondSecurity.getIssuerDomicile());
    }
    Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    Currency currency = bondSecurity.getCurrency();
    ZoneId zone = bondSecurity.getInterestAccrualDate().getZone();
    ZonedDateTime firstAccrualDate =
        ZonedDateTime.of(bondSecurity.getInterestAccrualDate().toLocalDate().atStartOfDay(), zone);
    ZonedDateTime maturityDate =
        ZonedDateTime.of(bondSecurity.getLastTradeDate().getExpiry().toLocalDate().atStartOfDay(), zone);
    double rate = bondSecurity.getCouponRate() / 100;
    DayCount dayCount = bondSecurity.getDayCount();
    BusinessDayConvention businessDay = BusinessDayConventions.FOLLOWING;
    String domicile = bondSecurity.getIssuerDomicile();
    if (domicile == null) {
      throw new OpenGammaRuntimeException("bond security domicile cannot be null");
    }
    String conventionName = domicile + "_TREASURY_BOND_CONVENTION";
    ConventionBundle convention = _conventionSource.getConventionBundle(
        ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
    if (convention == null) {
      throw new OpenGammaRuntimeException("Convention called " + conventionName + " was null");
    }
    if (convention.isEOMConvention() == null) {
      throw new OpenGammaRuntimeException("Could not get EOM convention information from " + conventionName);
    }
    boolean isEOM = convention.isEOMConvention();
    YieldConvention yieldConvention = bondSecurity.getYieldConvention();
    if (bondSecurity.getCouponType().equals("NONE") || bondSecurity.getCouponType().equals("ZERO COUPON")) { //TODO find where string is
      return new PaymentFixedDefinition(currency, maturityDate, 1);
    }
    if (convention.getBondSettlementDays(firstAccrualDate, maturityDate) == null) {
      throw new OpenGammaRuntimeException("Could not get bond settlement days from " + conventionName);
    }
    int settlementDays = convention.getBondSettlementDays(firstAccrualDate, maturityDate);
    int exCouponDays = convention.getExDividendDays();
    Period paymentPeriod = ConversionUtils.getTenor(bondSecurity.getCouponFrequency());
    ZonedDateTime firstCouponDate =
        ZonedDateTime.of(bondSecurity.getFirstCouponDate().toLocalDate().atStartOfDay(), zone);
    ExternalIdBundle identifiers = bondSecurity.getExternalIdBundle();
    String isin = identifiers.getValue(ExternalSchemes.ISIN);
    String ticker = isin == null ? null : isin;
    String shortName = bondSecurity.getIssuerName();
    String sectorName = bondSecurity.getIssuerType();
    FlexiBean classifications = new FlexiBean();
    classifications.put(LegalEntityUtils.MARKET_STRING, bondSecurity.getMarket());
    Sector sector = Sector.of(sectorName, classifications);
    Region region = Region.of(
        bondSecurity.getIssuerDomicile(), Country.of(bondSecurity.getIssuerDomicile()), bondSecurity.getCurrency());
    Map<String, String> securityAttributes = bondSecurity.getAttributes();
    Set<CreditRating> creditRatings = null;
    for (String ratingString : LegalEntityUtils.RATING_STRINGS) {
      if (securityAttributes.containsKey(ratingString)) {
        if (creditRatings == null) {
          creditRatings = new HashSet<>();
        }
        creditRatings.add(CreditRating.of(securityAttributes.get(ratingString), ratingString, true));
      }
    }
    LegalEntity legalEntity = new LegalEntity(ticker, shortName, creditRatings, sector, region);
    BondFixedSecurityDefinition securityDefinition = BondFixedSecurityDefinition.from(currency, firstAccrualDate,
        firstCouponDate, maturityDate, paymentPeriod, rate, settlementDays, exCouponDays, calendar, dayCount, 
        businessDay, yieldConvention, isEOM, legalEntity);
    // TODO: PLAT-5253 Standard days to settlement are missing in bond description.
    ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, calendar);
    return BondFixedTransactionDefinition.fromYield(securityDefinition, 1, settleDate, yield);
    // TODO: User should choose between yield and price.
  }

}
