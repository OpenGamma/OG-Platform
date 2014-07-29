/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class BillNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The region source */
  private final RegionSource _regionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The security source */
  private final SecuritySource _securitySource;
  /** The legal entity source */
  private final LegalEntitySource _legalEntitySource;
  /** The market data */
  private final SnapshotDataBundle _marketData;
  /** The market data id */
  private final ExternalId _dataId;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;

  /**
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   * @param securitySource The security source, not null
   * @param legalEntitySource The legal entity source, not null
   * @param marketData The market data, not null
   * @param dataId The market data id, not null
   * @param valuationTime The valuation time, not null
   */
  public BillNodeConverter(final HolidaySource holidaySource, final RegionSource regionSource, final SecuritySource securitySource, final LegalEntitySource legalEntitySource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(legalEntitySource, "legalEntitySource");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    _regionSource = regionSource;
    _holidaySource = holidaySource;
    _securitySource = securitySource;
    _legalEntitySource = legalEntitySource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
  }

  @Override
  public InstrumentDefinition<?> visitBillNode(final BillNode billNode) {
    final Double yield = _marketData.getDataPoint(_dataId);
    if (yield == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    final Security security = _securitySource.getSingle(_dataId.toBundle()); //TODO this is in here because we can't ask for data by ISIN directly.
    if (!(security instanceof BillSecurity)) {
      throw new OpenGammaRuntimeException("Could not get security for " + security);
    }
    final BillSecurity billSecurity = (BillSecurity) security;
    final ExternalId regionId = billSecurity.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = billSecurity.getCurrency();
    final ZonedDateTime maturityDate = billSecurity.getMaturityDate().getExpiry();
    final DayCount dayCount = billSecurity.getDayCount();
    final YieldConvention yieldConvention = billSecurity.getYieldConvention();
    final int settlementDays = billSecurity.getDaysToSettle();
    final ExternalIdBundle identifiers = security.getExternalIdBundle();
    // TODO: [PLAT-5905] Add legal entity to node.
    // Legal Entity
    final com.opengamma.core.legalentity.LegalEntity legalEntityFromSource = _legalEntitySource.getSingle(billSecurity.getLegalEntityId());
    final Collection<Rating> ratings = legalEntityFromSource.getRatings();
    final String ticker;
    if (identifiers != null) {
      final String isin = identifiers.getValue(ExternalSchemes.ISIN);
      ticker = isin == null ? null : isin;
    } else {
      ticker = null;
    }
    final String shortName = legalEntityFromSource.getName();
    Set<CreditRating> creditRatings = null;
    for (final Rating rating : ratings) {
      if (creditRatings == null) {
        creditRatings = new HashSet<>();
      }
      //TODO seniority level needs to go into the credit rating
      creditRatings.add(CreditRating.of(rating.getRater(), rating.getScore().toString(), true));
    }
    final Region region = Region.of(regionId.getValue(), Country.of(regionId.getValue()), billSecurity.getCurrency());
    final LegalEntity legalEntity = new LegalEntity(ticker, shortName, creditRatings, null, region);
    final BillSecurityDefinition securityDefinition = new BillSecurityDefinition(currency, maturityDate, 1, settlementDays, calendar, yieldConvention, dayCount, legalEntity);
    return BillTransactionDefinition.fromYield(securityDefinition, 1, _valuationTime, yield, calendar);
  }

}
