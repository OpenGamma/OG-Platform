/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.NotionalExchange;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.swap.BillTotalReturnSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link BillTotalReturnSwapSecurity} classes to {@link BillTotalReturnSwapDefinition},
 * which are required for use in the analytics library.
 * The asset leg notional amount is used as bill quantity and the underlying bill has a notional of 1.0.
 * The bond TRS notional currency is not used, the bill currency is used in the bill description.
 */
public class BillTotalReturnSwapSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {

  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The security source */
  private final SecuritySource _securitySource;
  /** The legal entity source */
  private final LegalEntitySource _legalEntitySource;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param securitySource The security source, not null
   * @param legalEntitySource The legal entity source, not null
   */
  public BillTotalReturnSwapSecurityConverter(ConventionSource conventionSource, HolidaySource holidaySource,
                                              RegionSource regionSource, SecuritySource securitySource,
                                              LegalEntitySource legalEntitySource) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(regionSource, "regionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(legalEntitySource, "legalEntitySource");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _securitySource = securitySource;
    _legalEntitySource = legalEntitySource;
  }

  @Override
  public InstrumentDefinition<?> visitBillTotalReturnSwapSecurity(BillTotalReturnSwapSecurity security) {
    ArgumentChecker.notNull(security, "security");

    FinancialSecurity underlying = (FinancialSecurity) _securitySource.getSingle(security.getAssetId().toBundle());
    if (!(underlying instanceof BillSecurity)) {
      throw new OpenGammaRuntimeException("Underlying for bill TRS was not a bill");
    }
    BillSecurity bill = (BillSecurity) underlying;
    FloatingInterestRateSwapLeg fundingLeg = security.getFundingLeg();
    boolean isPayer = fundingLeg.getPayReceiveType() == PayReceiveType.PAY ? true : false;
    LocalDate startDate = security.getEffectiveDate();
    LocalDate endDate = security.getMaturityDate();
    NotionalExchange notionalExchange = NotionalExchange.builder().exchangeFinalNotional(true).build();
    AnnuityDefinition<? extends PaymentDefinition> annuityDefinition =
        AnnuityUtils.buildFloatingAnnuityDefinition(_conventionSource, _holidaySource, _securitySource, isPayer,
                                                    startDate, endDate, notionalExchange, fundingLeg);


    ExternalId regionId = bill.getRegionId();
    if (regionId == null) {
      throw new OpenGammaRuntimeException("Could not find region for bill: " + bill.toString());
    }
    Currency currency = bill.getCurrency();
    Calendar calendar;
    // If the bill is Supranational, we use the calendar derived from the currency of the bill.
    // this may need revisiting.
    if (regionId.getValue().equals("SNAT")) { // Supranational
      calendar = CalendarUtils.getCalendar(_holidaySource, currency);
    } else {
      calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    }

    double notional = security.getNotionalAmount();
    com.opengamma.core.legalentity.LegalEntity legalEntityFromSource = _legalEntitySource.getSingle(bill.getLegalEntityId());
    LegalEntity legalEntity = LegalEntityUtils.convertFrom(legalEntityFromSource, bill);

    BillSecurityDefinition billDefinition = new BillSecurityDefinition(currency, bill.getMaturityDate().getExpiry(),
                                                                       1.0d, bill.getDaysToSettle(), calendar,
                                                                       bill.getYieldConvention(), bill.getDayCount(),
                                                                       legalEntity);

    ZonedDateTime startDateTime = startDate.atTime(LocalTime.MIN).atZone(ZoneOffset.UTC);
    ZonedDateTime endDateTime = endDate.atTime(LocalTime.MIN).atZone(ZoneOffset.UTC);
    return new BillTotalReturnSwapDefinition(startDateTime, endDateTime, annuityDefinition, billDefinition, notional);
  }
}
