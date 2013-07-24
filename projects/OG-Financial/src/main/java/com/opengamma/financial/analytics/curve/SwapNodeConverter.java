/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class SwapNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The market data */
  private final SnapshotDataBundle _marketData;
  /** The market data id */
  private final ExternalId _dataId;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   */
  public SwapNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
  }

  @SuppressWarnings("synthetic-access")
  @Override
  public InstrumentDefinition<?> visitSwapNode(final SwapNode swapNode) {
    final Convention payLegConvention = _conventionSource.getConvention(swapNode.getPayLegConvention());
    if (payLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + swapNode.getPayLegConvention() + " was null");
    }
    final Convention receiveLegConvention = _conventionSource.getConvention(swapNode.getReceiveLegConvention());
    if (receiveLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + swapNode.getPayLegConvention() + " was null");
    }
    final AnnuityDefinition<? extends PaymentDefinition> payLeg;
    final AnnuityDefinition<? extends PaymentDefinition> receiveLeg;
    if (payLegConvention instanceof SwapFixedLegConvention) {
      payLeg = getFixedLeg((SwapFixedLegConvention) payLegConvention, swapNode, true);
    } else if (payLegConvention instanceof VanillaIborLegConvention) {
      payLeg = getIborLeg((VanillaIborLegConvention) payLegConvention, swapNode, true);
    } else if (payLegConvention instanceof OISLegConvention) {
      payLeg = getOISLeg((OISLegConvention) payLegConvention, swapNode, true);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle convention type " + payLegConvention.getClass());
    }
    if (receiveLegConvention instanceof SwapFixedLegConvention) {
      receiveLeg = getFixedLeg((SwapFixedLegConvention) receiveLegConvention, swapNode, false);
    } else if (receiveLegConvention instanceof VanillaIborLegConvention) {
      receiveLeg = getIborLeg((VanillaIborLegConvention) receiveLegConvention, swapNode, false);
    } else if (receiveLegConvention instanceof OISLegConvention) {
      receiveLeg = getOISLeg((OISLegConvention) receiveLegConvention, swapNode, false);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle convention type " + receiveLegConvention.getClass());
    }
    return new SwapDefinition(payLeg, receiveLeg);
  }


  @SuppressWarnings("synthetic-access")
  private AnnuityCouponFixedDefinition getFixedLeg(final SwapFixedLegConvention convention, final SwapNode swapNode, final boolean isPayer) {
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getRegionCalendar());
    final Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    final Currency currency = convention.getCurrency();
    final DayCount dayCount = convention.getDayCount();
    final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
    final boolean eom = convention.isIsEOM();
    final int settlementDays = convention.getSettlementDays();
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(_valuationTime.plus(swapNode.getStartTenor().getPeriod()), settlementDays, calendar);
    final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
    final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();
    return AnnuityCouponFixedDefinition.from(currency, settlementDate, maturityTenor, paymentPeriod, calendar, dayCount, businessDayConvention, eom, 1, rate, isPayer);
  }

  //TODO do we actually need the settlement days for the swap, not the index?
  @SuppressWarnings("synthetic-access")
  private AnnuityCouponIborDefinition getIborLeg(final VanillaIborLegConvention convention, final SwapNode swapNode, final boolean isPayer) {
    final Convention underlyingConvention = _conventionSource.getConvention(convention.getIborIndexConvention());
    if (!(underlyingConvention instanceof IborIndexConvention)) {
      if (underlyingConvention == null) {
        throw new OpenGammaRuntimeException("Could not get convention with id " + convention.getIborIndexConvention());
      }
      throw new OpenGammaRuntimeException("Convention of the underlying was not an ibor index convention; have " + underlyingConvention.getClass());
    }
    final IborIndexConvention indexConvention = (IborIndexConvention) underlyingConvention;
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final boolean eom = indexConvention.isIsEOM();
    final Period indexTenor = convention.getResetTenor().getPeriod();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
    final int spotLag = 0; //TODO
    final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eom, indexConvention.getName());
    final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();
    final int settlementDays = convention.getSettlementDays();
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(_valuationTime.plus(swapNode.getStartTenor().getPeriod()), settlementDays, calendar);
    return AnnuityCouponIborDefinition.from(settlementDate, maturityTenor, 1, iborIndex, isPayer, calendar);
  }

  @SuppressWarnings("synthetic-access")
  private AnnuityCouponONSimplifiedDefinition getOISLeg(final OISLegConvention convention, final SwapNode swapNode, final boolean isPayer) {
    final OvernightIndexConvention indexConvention = (OvernightIndexConvention) _conventionSource.getConvention(convention.getOvernightIndexConvention());
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final int publicationLag = indexConvention.getPublicationLag();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
    final int settlementDays = convention.getSettlementDays();
    final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();
    final IndexON indexON = new IndexON(indexConvention.getName(), currency, dayCount, publicationLag);
    final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
    final boolean isEOM = convention.isIsEOM();
    final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
    final int paymentLag = convention.getPaymentLag();
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(_valuationTime.plus(swapNode.getStartTenor().getPeriod()), settlementDays, calendar);
    return AnnuityCouponONSimplifiedDefinition.from(settlementDate, maturityTenor, 1, isPayer, indexON, paymentLag, calendar, businessDayConvention,
        paymentPeriod, isEOM);
  }
}
