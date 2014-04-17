/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.DateSet;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.FinancialConventionVisitor;
import com.opengamma.financial.convention.FinancialConventionVisitorAdapter;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.ONCompoundedLegRollDateConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarBusinessDateUtils;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Triple;

/**
 * Utilities to convert the nodes used in curve construction to OG-Analytics objects.
 */
public class NodeConverterUtils {

  /** The convention used to adjust date after roll date adjuster. **/
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;

  /**
   * Creates a {@link SwapDefinition} from the pay and receive conventions of a swap.
   * If both legs are floating, the spread will be on the receiver leg.
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param startTenor The start tenor, not null
   * @param maturityTenor The maturity tenor, not null
   * @param securitySource The security source, not null
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param marketData The market data, not null
   * @param dataId The data id
   * @param valuationTime The valuation time, not null
   * @param fx The FXMatrix with the exchange rates. Not null.
   * @return A swap definition
   */
  public static SwapDefinition getSwapDefinition(
      final FinancialConvention payLegConvention, final FinancialConvention receiveLegConvention, final Period startTenor, final Period maturityTenor, 
      final SecuritySource securitySource, final RegionSource regionSource, final HolidaySource holidaySource, final ConventionSource conventionSource, 
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime, final FXMatrix fx) {
    ArgumentChecker.notNull(payLegConvention, "pay leg convention");
    ArgumentChecker.notNull(receiveLegConvention, "receive leg convention");
    ArgumentChecker.notNull(startTenor, "start tenor");
    ArgumentChecker.notNull(maturityTenor, "maturity tenor");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    ArgumentChecker.notNull(fx, "FX matrix");
    final boolean isFloatFloat = isFloatFloat(payLegConvention, receiveLegConvention);
    final CurveNodeCurrencyVisitor ccyVisitor = new CurveNodeCurrencyVisitor(conventionSource, securitySource);
    final Set<Currency> ccySetPay = payLegConvention.accept(ccyVisitor);
    final Set<Currency> ccySetRec = receiveLegConvention.accept(ccyVisitor);
    ArgumentChecker.isTrue(ccySetPay.size() == 1, "More than one currency for one leg");
    ArgumentChecker.isTrue(ccySetRec.size() == 1, "More than one currency for one leg");
    final Currency ccyPay = ccySetPay.iterator().next();
    final Currency ccyRec = ccySetRec.iterator().next();
    final boolean isXCcy = !(ccyPay.equals(ccyRec));
    final double notionalRec = fx.getFxRate(ccyPay, ccyRec);
    final AnnuityDefinition<? extends PaymentDefinition> payLeg = getSwapLeg(payLegConvention, startTenor, maturityTenor, securitySource, regionSource, holidaySource,
        conventionSource, marketData, dataId, valuationTime, true, isFloatFloat, isXCcy, 1.0);
    final AnnuityDefinition<? extends PaymentDefinition> receiveLeg = getSwapLeg(receiveLegConvention, startTenor, maturityTenor, securitySource, regionSource, holidaySource,
        conventionSource, marketData, dataId, valuationTime, false, isFloatFloat, isXCcy, notionalRec);
    return new SwapDefinition(payLeg, receiveLeg);
  }

  /**
   * Creates a {@link SwapDefinition} from the pay and receive conventions of a swap.
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param unadjustedStartDate Unadjusted start date. The roll date adjuster will start at that date.
   * @param startNumberRollDate The roll date adjuster start number.
   * @param endNumberRollDate The roll date adjuster end number.
   * @param adjuster The roll date adjuster. Not null.
   * @param securitySource The security source, not null
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param marketData The market data, not null
   * @param dataId The data id
   * @param valuationTime The valuation time, not null
   * @return A swap definition
   */
  public static SwapDefinition getSwapRollDateDefinition(
      final FinancialConvention payLegConvention, final FinancialConvention receiveLegConvention, final ZonedDateTime unadjustedStartDate,
      final int startNumberRollDate, final int endNumberRollDate, final RollDateAdjuster adjuster, 
      final SecuritySource securitySource, final RegionSource regionSource, final HolidaySource holidaySource, final ConventionSource conventionSource, 
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(payLegConvention, "pay leg convention");
    ArgumentChecker.notNull(receiveLegConvention, "receive leg convention");
    ArgumentChecker.notNull(unadjustedStartDate, "start date");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    final boolean isFloatFloat = isFloatFloatRoll(payLegConvention, receiveLegConvention);
    final AnnuityDefinition<? extends PaymentDefinition> payLeg = getRollDateSwapLeg(payLegConvention, unadjustedStartDate, startNumberRollDate, endNumberRollDate, adjuster, 
        securitySource, regionSource, holidaySource, conventionSource, marketData, dataId, valuationTime, true, isFloatFloat);
    final AnnuityDefinition<? extends PaymentDefinition> receiveLeg = getRollDateSwapLeg(receiveLegConvention, unadjustedStartDate, startNumberRollDate, endNumberRollDate, adjuster, 
        securitySource, regionSource, holidaySource, conventionSource, marketData, dataId, valuationTime, false, isFloatFloat);
    return new SwapDefinition(payLeg, receiveLeg);
  }

  /**
   * Creates a {@link SwapDefinition} from the pay and receive conventions of a calendar swap.
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param unadjustedStartDate Unadjusted start date. The roll date adjuster will start at that date.
   * @param startDateNumber The number of the start date in the calendar non-good business days.
   * @param endDateNumber The number of the end date in the calendar non-good business days.
   * @param calendarStartEndDate The calendar with the start and end dates of the swap.
   * @param securitySource The security source, not null
   * @param regionSource The region source, not null
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param marketData The market data, not null
   * @param dataId The data id
   * @param valuationTime The valuation time, not null
   * @return A swap definition
   */
  public static SwapDefinition getSwapCalendarDefinition(
      final FinancialConvention payLegConvention, final FinancialConvention receiveLegConvention, final ZonedDateTime unadjustedStartDate,
      final int startDateNumber, final int endDateNumber, final DateSet calendarStartEndDate,
      final SecuritySource securitySource, final RegionSource regionSource, final HolidaySource holidaySource, final ConventionSource conventionSource, 
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(payLegConvention, "pay leg convention");
    ArgumentChecker.notNull(receiveLegConvention, "receive leg convention");
    ArgumentChecker.notNull(unadjustedStartDate, "start date");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    final boolean isFloatFloat = isFloatFloatRoll(payLegConvention, receiveLegConvention);
    final AnnuityDefinition<? extends PaymentDefinition> payLeg = getCalendarSwapLeg(payLegConvention, unadjustedStartDate, startDateNumber, endDateNumber, calendarStartEndDate, 
        securitySource, regionSource, holidaySource, conventionSource, marketData, dataId, valuationTime, true, isFloatFloat);
    final AnnuityDefinition<? extends PaymentDefinition> receiveLeg = getCalendarSwapLeg(receiveLegConvention, unadjustedStartDate, startDateNumber, endDateNumber, calendarStartEndDate, 
        securitySource, regionSource, holidaySource, conventionSource, marketData, dataId, valuationTime, false, isFloatFloat);
    return new SwapDefinition(payLeg, receiveLeg);
  }

  /**
   * Constructs an {@link AnnuityDefinition} from the pay and receive conventions of a swap.
   * @param legConvention The leg convention
   * @param startTenor The start tenor of the swap
   * @param maturityTenor The maturity tenor of the swap
   * @param securitySource A security source
   * @param regionSource A region source
   * @param holidaySource A holiday source
   * @param conventionSource A convention source
   * @param marketData The market data
   * @param dataId The market data id
   * @param valuationTime The valuation time
   * @param isPayer True if the leg is to be paid
   * @param isMarketDataSpread True if the market data is a spread paid on a floating leg
   * @param isXCcy Flag indicating if the swap is cross-currency.
   * @param notional Notional of the leg. Used in particular for cross-currency swaps.
   * @return An annuity definition
   * @throws UnsupportedOperationException If the convention type has not been implemented
   */
  public static AnnuityDefinition<? extends PaymentDefinition> getSwapLeg(
      final FinancialConvention legConvention, final Period startTenor, final Period maturityTenor, 
      final SecuritySource securitySource, final RegionSource regionSource, final HolidaySource holidaySource, final ConventionSource conventionSource, 
      final SnapshotDataBundle marketData, final ExternalId dataId,
      final ZonedDateTime valuationTime, final boolean isPayer, final boolean isMarketDataSpread, final boolean isXCcy, final double notional) {

    final FinancialConventionVisitor<AnnuityDefinition<? extends PaymentDefinition>> visitor = new FinancialConventionVisitorAdapter<AnnuityDefinition<? extends PaymentDefinition>>() {

      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitONArithmeticAverageLegConvention(final ONArithmeticAverageLegConvention convention) {
        if (isXCcy) {
          throw new NotImplementedException("Cross-currency ON Arithmetic Average leg not implemented.");
        }
        final Security sec = securitySource.getSingle(convention.getOvernightIndexConvention().toBundle());
        if (sec == null) {
          throw new OpenGammaRuntimeException("Overnight index with id " + convention.getOvernightIndexConvention() + " was null");
        }
        final OvernightIndex index = (OvernightIndex) sec;
        final OvernightIndexConvention indexConvention = conventionSource.getSingle(index.getConventionId(), OvernightIndexConvention.class);
        final IndexON indexON = ConverterUtils.indexON(index.getName(), indexConvention);
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        final boolean eomLeg = convention.isIsEOM();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
        // TODO: Add the payment lag in the builder.
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
        final StubType stub = convention.getStubType();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        if (isPayer && isMarketDataSpread) { // Implementation note: Market data is used as spread on pay leg
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return AnnuityDefinitionBuilder.couponONArithmeticAverageSpreadSimplified(startDate, maturityDate, paymentPeriod, notional, spread, indexON, isPayer,
              businessDayConvention, eomLeg, calendar, stub);
        }
        return AnnuityDefinitionBuilder.couponONArithmeticAverageSpreadSimplified(startDate, maturityDate, paymentPeriod, notional, 0.0d, indexON, isPayer,
            businessDayConvention, eomLeg, calendar, stub);
      }

      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitCompoundingIborLegConvention(final CompoundingIborLegConvention convention) {
        if (isXCcy) {
          throw new NotImplementedException("Cross-currency Ibor compounded leg not implemented.");
        }
        final Security sec = securitySource.getSingle(convention.getIborIndexConvention().toBundle()); 
        if (sec == null) {
          throw new OpenGammaRuntimeException("Ibor index with id " + convention.getIborIndexConvention() + " was null");
        }
        final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
        final IborIndexConvention indexConvention = conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
        final IborIndex index = ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final boolean eomLeg = convention.isIsEOM();
        final Period paymentTenor = convention.getPaymentTenor().getPeriod();
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, index.getBusinessDayConvention(), calendar, eomLeg);
        final StubType stubLeg = convention.getStubTypeLeg();
        final StubType stubComp = convention.getStubTypeCompound();
        final CompoundingType compound = convention.getCompoundingType();
        final boolean eom = convention.isIsEOM();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        if (isPayer && isMarketDataSpread) { // Implementation note: Market data is used as spread on pay leg
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          switch (compound) {
            case COMPOUNDING:
              return AnnuityDefinitionBuilder.couponIborCompoundingSpread(startDate, maturityDate, paymentTenor, notional, spread, index, stubComp, isPayer,
                  index.getBusinessDayConvention(), eom, calendar, stubLeg);
            case FLAT_COMPOUNDING:
              return AnnuityDefinitionBuilder.couponIborCompoundingFlatSpread(startDate, maturityDate, paymentTenor, notional, spread, index, stubComp, isPayer,
                  index.getBusinessDayConvention(), eom, calendar, stubLeg);
            default:
              throw new NotImplementedException("Compounding method unimplemented: only COMPOUNDING and FLAT_COMPOUNDING implemented");
          }
        }
        switch (compound) {
          case COMPOUNDING:
            return AnnuityDefinitionBuilder.couponIborCompounding(startDate, maturityDate, paymentTenor, notional, index, stubComp, isPayer,
              index.getBusinessDayConvention(), eom, calendar, stubLeg);
          case FLAT_COMPOUNDING:
            return AnnuityDefinitionBuilder.couponIborCompoundingFlatSpread(startDate, maturityDate, paymentTenor, notional, 0.0, index, stubComp, isPayer,
                index.getBusinessDayConvention(), eom, calendar, stubLeg);
          default:
            throw new NotImplementedException("Compounding method unimplemented: only COMPOUNDING and FLAT_COMPOUNDING implemented");
        }
      }

      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitOISLegConvention(final OISLegConvention convention) {
        if (isXCcy) {
          throw new NotImplementedException("Cross-currency OIS leg not implemented.");
        }
        final Security sec = securitySource.getSingle(convention.getOvernightIndexConvention().toBundle());
        if (sec == null) {
          throw new OpenGammaRuntimeException("Overnight index with id " + convention.getOvernightIndexConvention() + " was null");
        }
        final OvernightIndex index = (OvernightIndex) sec;
        final OvernightIndexConvention indexConvention = conventionSource.getSingle(index.getConventionId(), OvernightIndexConvention.class);
        final IndexON indexON = ConverterUtils.indexON(index.getName(), indexConvention);
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        final boolean eomLeg = convention.isIsEOM();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
        final int paymentLag = convention.getPaymentLag();
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
        final StubType stub = convention.getStubType();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        if (isPayer && isMarketDataSpread) { // Implementation note: Market data is used as spread on pay leg
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplified(startDate, maturityDate, paymentPeriod, notional, spread, indexON, isPayer,
              businessDayConvention, eomLeg, calendar, stub, paymentLag);
        }
        return AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplified(startDate, maturityDate, paymentPeriod, notional, 0.0d, indexON, isPayer,
            businessDayConvention, eomLeg, calendar, stub, paymentLag);
      }

      @Override
      public AnnuityCouponFixedDefinition visitSwapFixedLegConvention(final SwapFixedLegConvention convention) {
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, convention.getRegionCalendar());
        final Double rate = marketData.getDataPoint(dataId);
        if (rate == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
        }
        final Currency currency = convention.getCurrency();
        final DayCount dayCount = convention.getDayCount();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
        final boolean eomLeg = convention.isIsEOM();
        final int spotLagLeg = convention.getSettlementDays();
        final int paymentLag = convention.getPaymentLag();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
        final StubType stub = convention.getStubType();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        if (isXCcy) {
          return AnnuityDefinitionBuilder.couponFixedWithNotional(currency, startDate, maturityDate, paymentPeriod, calendar, dayCount, businessDayConvention, eomLeg,
              notional, rate, isPayer, stub, paymentLag, true, true);
        }
        return AnnuityDefinitionBuilder.couponFixed(currency, startDate, maturityDate, paymentPeriod, calendar, dayCount, businessDayConvention, eomLeg,
            notional, rate, isPayer, stub, paymentLag);
      }

      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitVanillaIborLegConvention(final VanillaIborLegConvention convention) {
        final Security sec = securitySource.getSingle(convention.getIborIndexConvention().toBundle()); 
        if (sec == null) {
          throw new OpenGammaRuntimeException("Ibor index with id " + convention.getIborIndexConvention() + " was null");
        }
        final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
        final IborIndexConvention indexConvention = conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
        final IborIndex index = ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final boolean eomLeg = convention.isIsEOM();
        final Period indexTenor = convention.getResetTenor().getPeriod();
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, index.getBusinessDayConvention(), calendar, eomLeg);
        final StubType stub = convention.getStubType();
        final int paymentLag = convention.getPaymentLag();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        if (isPayer && isMarketDataSpread) { // Implementation note: Market data is used as spread on pay leg
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          if (isXCcy) {
            return AnnuityDefinitionBuilder.couponIborSpreadWithNotional(startDate, maturityDate, notional, spread, index, isPayer, calendar, stub, paymentLag, true, true);
          }
          return AnnuityDefinitionBuilder.couponIborSpread(startDate, maturityDate, indexTenor, notional, spread, index, isPayer, index.getDayCount(),
              index.getBusinessDayConvention(), eomLeg, calendar, stub, paymentLag);
        }
        if (isXCcy) {
          return AnnuityDefinitionBuilder.couponIborWithNotional(startDate, maturityDate, notional, index, isPayer, calendar, stub, paymentLag, true, true);
        }
        return AnnuityDefinitionBuilder.couponIbor(startDate, maturityDate, indexTenor, notional, index, isPayer, index.getDayCount(),
            index.getBusinessDayConvention(), eomLeg, calendar, stub, paymentLag);
      }
    };

    return legConvention.accept(visitor);
  }

  /**
   * Constructs an {@link AnnuityDefinition} from the pay and receive conventions of a swap built with roll date adjuster.
   * @param legConvention The leg convention
   * @param unadjustedStartDate The swap unadjusted start date.
   * @param rollDateStartNumber The start number for the roll date adjustment.
   * @param rollDateEndNumber The end number for the roll date adjustment.
   * @param adjuster The roll date adjuster.
   * @param securitySource A security source
   * @param regionSource A region source
   * @param holidaySource A holiday source
   * @param conventionSource A convention source
   * @param marketData The market data
   * @param dataId The market data id
   * @param valuationTime The valuation time
   * @param isPayer True if the leg is to be paid
   * @param isMarketDataSpread True if the market data is a spread paid on a floating leg
   * @return An annuity definition
   * @throws UnsupportedOperationException If the convention type has not been implemented
   */
  private static AnnuityDefinition<? extends PaymentDefinition> getRollDateSwapLeg(
      final FinancialConvention legConvention, final ZonedDateTime unadjustedStartDate, final int rollDateStartNumber, final int rollDateEndNumber, final RollDateAdjuster adjuster, 
      final SecuritySource securitySource, final RegionSource regionSource, final HolidaySource holidaySource, final ConventionSource conventionSource, 
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime, final boolean isPayer,
      final boolean isMarketDataSpread) {
    final FinancialConventionVisitor<AnnuityDefinition<? extends PaymentDefinition>> visitor = new FinancialConventionVisitorAdapter<AnnuityDefinition<? extends PaymentDefinition>>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitFixedLegRollDateConvention(final FixedLegRollDateConvention convention) {
        final Calendar calendarHolidays = CalendarUtils.getCalendar(regionSource, holidaySource, convention.getRegionCalendar());
        final Double rate = marketData.getDataPoint(dataId);
        if (rate == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
        }
        final Currency currency = convention.getCurrency();
        final DayCount dayCount = convention.getDayCount();
        final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(calendarHolidays, unadjustedStartDate);
        final StubType stub = convention.getStubType();
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        final int paymentLag = convention.getPaymentLag();
        return AnnuityDefinitionBuilder.couponFixedRollDate(currency, adjustedStartDate, rollDateStartNumber, rollDateEndNumber, adjuster, paymentPeriod, 1, rate,
            isPayer, dayCount, calendarHolidays, stub, paymentLag);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitVanillaIborLegRollDateConvention(final VanillaIborLegRollDateConvention convention) {
        final Security sec = securitySource.getSingle(convention.getIborIndexConvention().toBundle()); 
        if (sec == null) {
          throw new OpenGammaRuntimeException("Ibor index with id " + convention.getIborIndexConvention() + " was null");
        }
        final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
        final IborIndexConvention indexConvention = conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final IborIndex indexIbor = ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
        final StubType stub = convention.getStubType();
        final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(calendar, unadjustedStartDate);
        if (isPayer && isMarketDataSpread) { // Implementation note: Market data is used as spread on pay leg
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(adjustedStartDate, rollDateStartNumber, rollDateEndNumber, adjuster, indexIbor, 1, spread,
              isPayer, indexIbor.getDayCount(), calendar, stub);
        }
        return AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(adjustedStartDate, rollDateStartNumber, rollDateEndNumber, adjuster, indexIbor, 1,
            isPayer, indexIbor.getDayCount(), calendar, stub);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitONCompoundedLegRollDateConvention(final ONCompoundedLegRollDateConvention convention) {
        final Security sec = securitySource.getSingle(convention.getOvernightIndexConvention().toBundle());
        if (sec == null) {
          throw new OpenGammaRuntimeException("Overnight index with id " + convention.getOvernightIndexConvention() + " was null");
        }
        final OvernightIndex index = (OvernightIndex) sec;
        final OvernightIndexConvention indexConvention = conventionSource.getSingle(index.getConventionId(), OvernightIndexConvention.class);
        final IndexON indexON = ConverterUtils.indexON(index.getName(), indexConvention);
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final Period paymentTenor = convention.getPaymentTenor().getPeriod();
        final StubType stub = convention.getStubType();
        final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(calendar, unadjustedStartDate);
        final int paymentLag = convention.getPaymentLag();
        if (isPayer && isMarketDataSpread) { // Implementation note: Market data is used as spread on pay leg
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplifiedRollDate(adjustedStartDate, rollDateStartNumber, rollDateEndNumber,
              adjuster, paymentTenor, 1.0d, spread, indexON, isPayer, calendar, stub, paymentLag);
        }
        return AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplifiedRollDate(adjustedStartDate, rollDateStartNumber, rollDateEndNumber,
            adjuster, paymentTenor, 1.0d, 0.0, indexON, isPayer, calendar, stub, paymentLag);
      }

    };

    return legConvention.accept(visitor);
  }

  private static AnnuityDefinition<? extends PaymentDefinition> getCalendarSwapLeg(
      final FinancialConvention legConvention, final ZonedDateTime unadjustedStartDate, final int calendarDateStartOffset, final int calendarDateEndOffset, final DateSet offsetDates,
      final SecuritySource securitySource, final RegionSource regionSource, final HolidaySource holidaySource, final ConventionSource conventionSource, 
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime, final boolean isPayer, final boolean isMarketDataSpread) {

    final FinancialConventionVisitor<AnnuityDefinition<? extends PaymentDefinition>> visitor = new FinancialConventionVisitorAdapter<AnnuityDefinition<? extends PaymentDefinition>>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitOISLegConvention(final OISLegConvention convention) {
        final OvernightIndexConvention indexConvention = conventionSource.getSingle(convention.getOvernightIndexConvention(), OvernightIndexConvention.class);
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final IndexON indexON = ConverterUtils.indexON(indexConvention.getName(), indexConvention);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        final boolean eomLeg = convention.isIsEOM();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
        final int paymentLag = convention.getPaymentLag();
        final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(calendar, unadjustedStartDate);
        final ZonedDateTime effectiveDate = offsetDates.getNextDate(adjustedStartDate.toLocalDate(), calendarDateStartOffset).atTime(adjustedStartDate.toLocalTime()).atZone(
            adjustedStartDate.getZone());
        //CalendarBusinessDateUtils.nthNonGoodBusinessDate(adjustedStartDate.toLocalDate(), calendarStartEndDate,
        //    calendarDateStartOffset).atTime(adjustedStartDate.toLocalTime()).atZone(adjustedStartDate.getZone());
        final ZonedDateTime maturityDate = offsetDates.getNextDate(adjustedStartDate.toLocalDate(), calendarDateEndOffset).atTime(adjustedStartDate.toLocalTime()).atZone(adjustedStartDate.getZone());
            //CalendarBusinessDateUtils.nthNonGoodBusinessDate(effectiveDate.toLocalDate().plusDays(1), calendarStartEndDate,
            //calendarDateEndOffset - calendarDateStartOffset).atTime(adjustedStartDate.toLocalTime()).atZone(adjustedStartDate.getZone());
        final StubType stub = convention.getStubType();
        if (isPayer && isMarketDataSpread) { // Implementation note: Market data is used as spread on pay leg
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplified(effectiveDate, maturityDate, paymentPeriod, 1.0d, spread, indexON, isPayer,
              businessDayConvention, eomLeg, calendar, stub, paymentLag);
        }
        return AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplified(effectiveDate, maturityDate, paymentPeriod, 1.0d, 0.0d, indexON, isPayer,
            businessDayConvention, eomLeg, calendar, stub, paymentLag);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public AnnuityCouponFixedDefinition visitSwapFixedLegConvention(final SwapFixedLegConvention convention) {
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, convention.getRegionCalendar());
        final Double rate = marketData.getDataPoint(dataId);
        if (rate == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
        }
        final Currency currency = convention.getCurrency();
        final DayCount dayCount = convention.getDayCount();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
        final boolean eomLeg = convention.isIsEOM();
        final int paymentLag = convention.getPaymentLag();
        final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(calendar, unadjustedStartDate);
        //final ZonedDateTime effectiveDate = CalendarBusinessDateUtils.nthNonGoodBusinessDate(adjustedStartDate.toLocalDate(), calendar,
        //    calendarDateStartOffset).atStartOfDay(ZoneId.of("UTC"));
        final ZonedDateTime effectiveDate = offsetDates.getNextDate(adjustedStartDate.toLocalDate(), calendarDateStartOffset).atStartOfDay(ZoneId.of("UTC"));
        //final ZonedDateTime maturityDate = CalendarBusinessDateUtils.nthNonGoodBusinessDate(effectiveDate.toLocalDate().plusDays(1), calendar,
        //    calendarDateEndOffset - calendarDateStartOffset).atStartOfDay(ZoneId.of("UTC"));
        final ZonedDateTime maturityDate = offsetDates.getNextDate(adjustedStartDate.toLocalDate(), calendarDateEndOffset).atStartOfDay(ZoneId.of("UTC"));
        final StubType stub = convention.getStubType();
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        return AnnuityDefinitionBuilder.couponFixed(currency, effectiveDate, maturityDate, paymentPeriod, calendar, dayCount, businessDayConvention, eomLeg,
              1.0d, rate, isPayer, stub, paymentLag);
      }

    };

    return legConvention.accept(visitor);
  }

  /**
   * Constructs an {@link AnnuityDefinition} from the pay and receive conventions of a swap.
   * @param legConvention The leg convention
   * @param startTenor The start tenor of the swap
   * @param maturityTenor The maturity tenor of the swap
   * @param regionSource A region source
   * @param holidaySource A holiday source
   * @param conventionSource A convention source
   * @param marketData The market data
   * @param dataId The market data id
   * @param valuationTime The valuation time
   * @param isPayer True if the leg is to be paid
   * @param isMarketDataSpread True if the market data is a spread paid on a floating leg
   * @return An annuity definition
   * @throws UnsupportedOperationException If the convention type has not been implemented
   */
  public static Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime> createSwapLeg(
      final FinancialConvention legConvention, final Period startTenor,  final Period maturityTenor, final RegionSource regionSource,
      final HolidaySource holidaySource, final ConventionSource conventionSource, final SnapshotDataBundle marketData,
      final ExternalId dataId, final ZonedDateTime valuationTime, final boolean isPayer, final boolean isMarketDataSpread) {
    final FinancialConventionVisitor<Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime>> visitor = new FinancialConventionVisitorAdapter<
        Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime>>() {

      @Override
      public Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime> visitONArithmeticAverageLegConvention(final ONArithmeticAverageLegConvention convention) {
        final OvernightIndexConvention indexConvention = conventionSource.getSingle(convention.getOvernightIndexConvention(), OvernightIndexConvention.class);
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final Currency currency = indexConvention.getCurrency();
        final DayCount dayCount = indexConvention.getDayCount();
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        final boolean eomLeg = convention.isIsEOM();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        final ExternalId indexId = ExternalId.of("UNKNOWN", indexConvention.getName()); // TODO: The proper Id of the ON index should be provided but is currently not available.
        if (!isPayer && isMarketDataSpread) {
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return Triple.of(
              new FloatingSpreadIRLeg(dayCount, PeriodFrequency.of(paymentPeriod), indexConvention.getRegionCalendar(), businessDayConvention,
              new InterestRateNotional(currency, 1), eomLeg, indexId, FloatingRateType.OVERNIGHT_ARITHMETIC_AVERAGE, spread),
              startDate,
              maturityDate);
        }
        return Triple.of(
            new FloatingInterestRateLeg(dayCount, PeriodFrequency.of(paymentPeriod), indexConvention.getRegionCalendar(), businessDayConvention,
            new InterestRateNotional(currency, 1), eomLeg, indexId, FloatingRateType.OVERNIGHT_ARITHMETIC_AVERAGE),
            startDate,
            maturityDate);
      }

      @Override
      public Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime> visitCompoundingIborLegConvention(final CompoundingIborLegConvention convention) {
        final IborIndexConvention indexConvention = conventionSource.getSingle(convention.getIborIndexConvention(), IborIndexConvention.class);
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final Currency currency = indexConvention.getCurrency();
        final DayCount dayCount = indexConvention.getDayCount();
        final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
//        final boolean eomIndex = indexConvention.isIsEOM();
        final boolean eomLeg = convention.isIsEOM();
//        final Period indexTenor = convention.getCompositionTenor().getPeriod();
//        final Period paymentTenor = convention.getPaymentTenor().getPeriod();
//        final int spotLag = indexConvention.getSettlementDays();
//        final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eomIndex, indexConvention.getName());
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
//        final StubType stubLeg = convention.getStubTypeLeg();
//        final StubType stubComp = convention.getStubTypeCompound();
//        final boolean eom = convention.isIsEOM();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        if (!isPayer && isMarketDataSpread) {
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return Triple.of(new FloatingSpreadIRLeg(dayCount,
                                         PeriodFrequency.of(paymentPeriod),
                                         indexConvention.getRegionCalendar(),
                                         businessDayConvention,
                                         new InterestRateNotional(currency, 1),
                                         eomLeg,
                                         dataId,
                                         FloatingRateType.IBOR,
                                         spread), startDate, maturityDate);
        }
        return Triple.of(new FloatingInterestRateLeg(dayCount,
                                           PeriodFrequency.of(paymentPeriod),
                                           indexConvention.getRegionCalendar(),
                                           businessDayConvention,
                                           new InterestRateNotional(currency, 1),
                                           eomLeg,
                                           dataId, FloatingRateType.IBOR), startDate, maturityDate);
      }

      @Override
      public Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime> visitOISLegConvention(final OISLegConvention convention) {
        final OvernightIndexConvention indexConvention = conventionSource.getSingle(convention.getOvernightIndexConvention(), OvernightIndexConvention.class);
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final Currency currency = indexConvention.getCurrency();
        final DayCount dayCount = indexConvention.getDayCount();
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        final boolean eomLeg = convention.isIsEOM();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        final ExternalId indexId = ExternalId.of("UNKNOWN", indexConvention.getName()); // TODO: The proper Id of the ON index should be provided but is currently not available.
        if (!isPayer && isMarketDataSpread) {
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return Triple.of(new FloatingSpreadIRLeg(dayCount,
                                         PeriodFrequency.of(paymentPeriod),
                                         indexConvention.getRegionCalendar(),
                                         businessDayConvention,
                                         new InterestRateNotional(currency, 1),
                                         eomLeg,
                                         indexId,
                                         FloatingRateType.OIS,
                                         spread), startDate, maturityDate);
        }
        return Triple.of(new FloatingInterestRateLeg(dayCount,
                                           PeriodFrequency.of(paymentPeriod),
                                           indexConvention.getRegionCalendar(),
                                           businessDayConvention,
                                           new InterestRateNotional(currency, 1),
                                           eomLeg,
                                           indexId, FloatingRateType.OIS), startDate, maturityDate);
      }

      @Override
      public Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime> visitSwapFixedLegConvention(final SwapFixedLegConvention convention) {
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, convention.getRegionCalendar());
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
//        final StubType stub = convention.getStubType();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();

        return Triple.of(new FixedInterestRateLeg(dayCount,
                                 PeriodFrequency.of(paymentPeriod),
                                 convention.getRegionCalendar(),
                                 businessDayConvention,
                                 new InterestRateNotional(currency, 1),
                                 eomLeg,
                                 rate), startDate, maturityDate);
      }

      @Override
      public Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime> visitVanillaIborLegConvention(final VanillaIborLegConvention convention) {
        final IborIndexConvention indexConvention = conventionSource.getSingle(convention.getIborIndexConvention(), IborIndexConvention.class);
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final Currency currency = indexConvention.getCurrency();
        final DayCount dayCount = indexConvention.getDayCount();
        final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
//        final boolean eomIndex = indexConvention.isIsEOM();
        final boolean eomLeg = convention.isIsEOM();
//        final Period indexTenor = convention.getResetTenor().getPeriod();
//        final int spotLag = indexConvention.getSettlementDays();
//        final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eomIndex, indexConvention.getName());
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
//        final StubType stub = convention.getStubType();
//        final boolean eom = convention.isIsEOM();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        if (!isPayer && isMarketDataSpread) {
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return Triple.of(new FloatingSpreadIRLeg(dayCount,
                                         PeriodFrequency.of(convention.getResetTenor().getPeriod()),
                                         indexConvention.getRegionCalendar(),
                                         businessDayConvention,
                                         new InterestRateNotional(currency, 1),
                                         eomLeg,
                                         dataId, // TODO: Should it be the Ibor Id, not the rate source?
                                         FloatingRateType.IBOR,
                                         spread), startDate, maturityDate);
        }
        return Triple.of(new FloatingInterestRateLeg(dayCount,
                                           PeriodFrequency.of(convention.getResetTenor().getPeriod()),
                                           indexConvention.getRegionCalendar(),
                                           businessDayConvention,
                                           new InterestRateNotional(currency, 1),
                                           eomLeg,
                                           dataId, FloatingRateType.IBOR), startDate, maturityDate);
      }
    };
    return legConvention.accept(visitor);
  }

  /**
   * Determines if a swap is float / float
   * @param payLegConvention The pay leg convention
   * @param receiveLegConvention The receive leg convention
   * @return True if both legs are floating
   */
  public static boolean isFloatFloat(final Convention payLegConvention, final Convention receiveLegConvention) {
    final boolean isFloatFloat = ((payLegConvention instanceof VanillaIborLegConvention) ||
                                  (payLegConvention instanceof OISLegConvention) ||
                                  (payLegConvention instanceof CompoundingIborLegConvention) ||
                                  (payLegConvention instanceof ONArithmeticAverageLegConvention))
        && ((receiveLegConvention instanceof VanillaIborLegConvention) ||
            (receiveLegConvention instanceof OISLegConvention) ||
            (receiveLegConvention instanceof CompoundingIborLegConvention) ||
            (receiveLegConvention instanceof ONArithmeticAverageLegConvention));
    return isFloatFloat;
  }

  /**
   * Determines if a swap is float / float for swaps with roll date conventions
   * @param payLegConvention The pay leg convention
   * @param receiveLegConvention The receive leg convention
   * @return True if both legs are floating
   */
  private static boolean isFloatFloatRoll(final Convention payLegConvention, final Convention receiveLegConvention) {
    final boolean isFloatFloat = ((payLegConvention instanceof VanillaIborLegRollDateConvention) ||
        (payLegConvention instanceof ONCompoundedLegRollDateConvention))
        && ((receiveLegConvention instanceof VanillaIborLegRollDateConvention) ||
            (receiveLegConvention instanceof ONCompoundedLegRollDateConvention));
    return isFloatFloat;
  }

  /**
   * Create a IborIndex object from the convention and the tenor.
   * @param indexConvention The index convention.
   * @param indexTenor The index tenor.
   * @return The IborIndex object.
   */
  public static IborIndex indexIbor(final IborIndexConvention indexConvention, final Period indexTenor) {
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final boolean eomIndex = indexConvention.isIsEOM();
    final int spotLag = indexConvention.getSettlementDays();
    final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eomIndex, indexConvention.getName());
    return iborIndex;
  }

}
