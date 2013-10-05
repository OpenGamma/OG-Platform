/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
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
public class NodeConverterUtils {

  /**
   * Creates a {@link SwapDefinition} from the pay and receive conventions of a swap.
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param startTenor The start tenor, not null
   * @param maturityTenor The maturity tenor, not null
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param marketData The market data, not null
   * @param dataId The data id
   * @param valuationTime The valuation time, not null
   * @return A swap definition
   */
  public static SwapDefinition getSwapDefinition(final Convention payLegConvention, final Convention receiveLegConvention, final Period startTenor, final Period maturityTenor,
      final RegionSource regionSource, final HolidaySource holidaySource, final ConventionSource conventionSource, final SnapshotDataBundle marketData,
      final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(payLegConvention, "pay leg convention");
    ArgumentChecker.notNull(receiveLegConvention, "receive leg convention");
    ArgumentChecker.notNull(startTenor, "start tenor");
    ArgumentChecker.notNull(maturityTenor, "maturity tenor");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    final AnnuityDefinition<? extends PaymentDefinition> payLeg;
    final AnnuityDefinition<? extends PaymentDefinition> receiveLeg;
    final boolean isFloatFloat = ((payLegConvention instanceof VanillaIborLegConvention) || (payLegConvention instanceof OISLegConvention) ||
        (payLegConvention instanceof CompoundingIborLegConvention))
        &&  ((receiveLegConvention instanceof VanillaIborLegConvention) || (receiveLegConvention instanceof OISLegConvention) ||
            (receiveLegConvention instanceof CompoundingIborLegConvention));
    if (payLegConvention instanceof SwapFixedLegConvention) {
      final SwapFixedLegConvention fixedLegConvention = (SwapFixedLegConvention) payLegConvention;
      final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, fixedLegConvention.getRegionCalendar());
      payLeg = getFixedLeg(fixedLegConvention, startTenor, maturityTenor, true, calendar, marketData, dataId, valuationTime);
    } else if (payLegConvention instanceof VanillaIborLegConvention) {
      final VanillaIborLegConvention iborLegConvention = (VanillaIborLegConvention) payLegConvention;
      final IborIndexConvention indexConvention = conventionSource.getConvention(IborIndexConvention.class, iborLegConvention.getIborIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
      payLeg = getIborLeg(iborLegConvention, indexConvention, startTenor, maturityTenor, true, calendar, false, marketData, dataId, valuationTime);
    } else if (payLegConvention instanceof OISLegConvention) {
      final OISLegConvention oisLegConvention = (OISLegConvention) payLegConvention;
      final OvernightIndexConvention indexConvention = conventionSource.getConvention(OvernightIndexConvention.class, oisLegConvention.getOvernightIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
      payLeg = getOISLeg(oisLegConvention, indexConvention, startTenor, maturityTenor, true, calendar, false, marketData, dataId, valuationTime);
    } else if (payLegConvention instanceof CompoundingIborLegConvention) {
      final CompoundingIborLegConvention iborLegConvention = (CompoundingIborLegConvention) payLegConvention;
      final IborIndexConvention indexConvention = conventionSource.getConvention(IborIndexConvention.class, iborLegConvention.getIborIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
      payLeg = getIborCompoundingLeg(iborLegConvention, indexConvention, startTenor, maturityTenor, true, calendar, false, marketData, dataId, valuationTime);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle convention type " + payLegConvention.getClass());
    }
    if (receiveLegConvention instanceof SwapFixedLegConvention) {
      final SwapFixedLegConvention fixedLegConvention = (SwapFixedLegConvention) receiveLegConvention;
      final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, fixedLegConvention.getRegionCalendar());
      receiveLeg = getFixedLeg(fixedLegConvention, startTenor, maturityTenor, false, calendar, marketData, dataId, valuationTime);
    } else if (receiveLegConvention instanceof VanillaIborLegConvention) {
      final VanillaIborLegConvention iborLegConvention = (VanillaIborLegConvention) receiveLegConvention;
      final IborIndexConvention indexConvention = conventionSource.getConvention(IborIndexConvention.class, iborLegConvention.getIborIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
      receiveLeg = getIborLeg(iborLegConvention, indexConvention, startTenor, maturityTenor, false, calendar, isFloatFloat, marketData, dataId, valuationTime);
    } else if (receiveLegConvention instanceof OISLegConvention) {
      final OISLegConvention oisLegConvention = (OISLegConvention) receiveLegConvention;
      final OvernightIndexConvention indexConvention = conventionSource.getConvention(OvernightIndexConvention.class, oisLegConvention.getOvernightIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
      receiveLeg = getOISLeg(oisLegConvention, indexConvention, startTenor, maturityTenor, false, calendar, isFloatFloat, marketData, dataId, valuationTime);
    } else if (receiveLegConvention instanceof CompoundingIborLegConvention) {
      final CompoundingIborLegConvention iborLegConvention = (CompoundingIborLegConvention) receiveLegConvention;
      final IborIndexConvention indexConvention = conventionSource.getConvention(IborIndexConvention.class, iborLegConvention.getIborIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
      receiveLeg = getIborCompoundingLeg(iborLegConvention, indexConvention, startTenor, maturityTenor, false, calendar, isFloatFloat, marketData, dataId, valuationTime);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle convention type " + receiveLegConvention.getClass());
    }
    return new SwapDefinition(payLeg, receiveLeg);
  }

  /**
   * Constructs an {@link AnnuityCouponFixedDefinition} from the convention.
   * @param convention The fixed leg convention, not null
   * @param startTenor The start tenor of the swap, not null
   * @param maturityTenor The maturity tenor of the swap, not null
   * @param isPayer True if the swap is a payer
   * @param calendar The holiday calendar, not null
   * @param marketData The market data, not null
   * @param dataId The market data id of the fixed rate, not null
   * @param valuationTime The valuation time, not null
   * @return A fixed swap leg definition
   */
  public static AnnuityCouponFixedDefinition getFixedLeg(final SwapFixedLegConvention convention, final Period startTenor, final Period maturityTenor, final boolean isPayer,
      final Calendar calendar, final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(startTenor, "start tenor");
    ArgumentChecker.notNull(maturityTenor, "maturity tenor");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    final Double rate = marketData.getDataPoint(dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
    }
    final Currency currency = convention.getCurrency();
    final DayCount dayCount = convention.getDayCount();
    final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
    final boolean eomLeg = convention.isIsEOM();
    final int spotLagLeg = convention.getSettlementDays();
    final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
    final StubType stub = convention.getStubType();
    final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
    final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
    return AnnuityCouponFixedDefinition.from(currency, startDate, maturityDate, paymentPeriod, calendar, dayCount, businessDayConvention, eomLeg, 1, rate, isPayer, stub);
  }

  /**
   * Constructs either an {@link AnnuityCouponIborDefinition} or {@link AnnuityCouponIborSpreadDefinition} from the convention.
   * @param convention The fixed leg convention, not null
   * @param indexConvention The ibor index convention, not null
   * @param startTenor The start tenor of the swap, not null
   * @param maturityTenor The maturity tenor of the swap, not null
   * @param isPayer True if the swap is a payer
   * @param calendar The holiday calendar, not null
   * @param isMarketDataSpread True if the market data is a spread rate
   * @param marketData The market data, not null
   * @param dataId The market data id of the fixed rate, not null
   * @param valuationTime The valuation time, not null
   * @return A fixed swap leg definition
   */
  public static AnnuityDefinition<? extends PaymentDefinition> getIborLeg(final VanillaIborLegConvention convention, final IborIndexConvention indexConvention,
      final Period startTenor, final Period maturityTenor, final boolean isPayer, final Calendar calendar, final boolean isMarketDataSpread, final SnapshotDataBundle marketData,
      final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(startTenor, "start tenor");
    ArgumentChecker.notNull(maturityTenor, "maturity tenor");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final boolean eomIndex = indexConvention.isIsEOM();
    final boolean eomLeg = convention.isIsEOM();
    final Period indexTenor = convention.getResetTenor().getPeriod();
    final int spotLag = indexConvention.getSettlementDays();
    final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eomIndex, indexConvention.getName());
    final int spotLagLeg = convention.getSettlementDays();
    final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
    final StubType stub = convention.getStubType();
    final boolean eom = convention.isIsEOM();
    final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
    if (!isPayer && isMarketDataSpread) {
      final Double spread = marketData.getDataPoint(dataId);
      if (spread == null) {
        throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
      }
      return AnnuityCouponIborSpreadDefinition.from(startDate, maturityDate, indexTenor, 1, spread, iborIndex, isPayer, businessDayConvention, eom, dayCount, calendar, stub);
    }
    return AnnuityCouponIborDefinition.from(startDate, maturityDate, indexTenor, 1, iborIndex, isPayer, businessDayConvention, eom, dayCount, calendar, stub);
  }

  /**
   * Constructs annuities of compounding ibor legs from the convention.
   * @param convention The fixed leg convention, not null
   * @param indexConvention The ibor index convention, not null
   * @param startTenor The start tenor of the swap, not null
   * @param maturityTenor The maturity tenor of the swap, not null
   * @param isPayer True if the swap is a payer
   * @param calendar The holiday calendar, not null
   * @param isMarketDataSpread True if the market data is a spread rate
   * @param marketData The market data, not null
   * @param dataId The market data id of the fixed rate, not null
   * @param valuationTime The valuation time, not null
   * @return A fixed swap leg definition
   */
  public static AnnuityDefinition<? extends PaymentDefinition> getIborCompoundingLeg(final CompoundingIborLegConvention convention, final IborIndexConvention indexConvention,
      final Period startTenor, final Period maturityTenor, final boolean isPayer, final Calendar calendar, final boolean isMarketDataSpread, final SnapshotDataBundle marketData,
      final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(startTenor, "start tenor");
    ArgumentChecker.notNull(maturityTenor, "maturity tenor");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final boolean eomIndex = indexConvention.isIsEOM();
    final boolean eomLeg = convention.isIsEOM();
    final Period indexTenor = convention.getCompositionTenor().getPeriod();
    final Period paymentTenor = convention.getPaymentTenor().getPeriod();
    final int spotLag = indexConvention.getSettlementDays();
    final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eomIndex, indexConvention.getName());
    final int spotLagLeg = convention.getSettlementDays();
    final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
    final StubType stubLeg = convention.getStubTypeLeg();
    final StubType stubComp = convention.getStubTypeCompound();
    final boolean eom = convention.isIsEOM();
    final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
    if (!isPayer && isMarketDataSpread) {
      final Double spread = marketData.getDataPoint(dataId);
      if (spread == null) {
        throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
      }
      return AnnuityDefinitionBuilder.annuityIborCompoundingSpreadFrom(startDate, maturityDate, paymentTenor, 1, spread, iborIndex, stubComp, isPayer,
          businessDayConvention, eom, calendar, stubLeg);
    }
    return AnnuityDefinitionBuilder.annuityIborCompoundingFrom(startDate, maturityDate, paymentTenor, 1, iborIndex, stubComp, isPayer,
        businessDayConvention, eom, calendar, stubLeg);
  }

  /**
   * Constructs OIS legs from the convention.
   * @param convention The fixed leg convention, not null
   * @param indexConvention The ibor index convention, not null
   * @param startTenor The start tenor of the swap, not null
   * @param maturityTenor The maturity tenor of the swap, not null
   * @param isPayer True if the swap is a payer
   * @param calendar The holiday calendar, not null
   * @param isMarketDataSpread True if the market data is a spread rate
   * @param marketData The market data, not null
   * @param dataId The market data id of the fixed rate, not null
   * @param valuationTime The valuation time, not null
   * @return A fixed swap leg definition
   */
  public static AnnuityDefinition<? extends PaymentDefinition> getOISLeg(final OISLegConvention convention, final OvernightIndexConvention indexConvention,
      final Period startTenor, final Period maturityTenor, final boolean isPayer, final Calendar calendar, final boolean isMarketDataSpread, final SnapshotDataBundle marketData,
      final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(startTenor, "start tenor");
    ArgumentChecker.notNull(maturityTenor, "maturity tenor");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final int publicationLag = indexConvention.getPublicationLag();
    final int spotLagLeg = convention.getSettlementDays();
    final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
    final IndexON indexON = new IndexON(indexConvention.getName(), currency, dayCount, publicationLag);
    final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
    final boolean eomLeg = convention.isIsEOM();
    final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
    final int paymentLag = convention.getPaymentLag();
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
    final StubType stub = convention.getStubType();
    final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
    if (isMarketDataSpread) {
      final Double spread = marketData.getDataPoint(dataId);
      if (spread == null) {
        throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
      }
      return AnnuityCouponONSpreadSimplifiedDefinition.from(startDate, maturityDate, 1, spread, isPayer, paymentPeriod, indexON, paymentLag, businessDayConvention, eomLeg, calendar, stub);
    }
    return AnnuityCouponONSimplifiedDefinition.from(startDate, maturityDate, 1, isPayer, paymentPeriod, indexON, paymentLag, businessDayConvention, eomLeg, calendar, stub);
  }

}
