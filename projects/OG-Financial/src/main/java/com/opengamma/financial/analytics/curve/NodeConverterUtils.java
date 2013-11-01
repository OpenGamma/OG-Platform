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
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.FinancialConventionVisitor;
import com.opengamma.financial.convention.FinancialConventionVisitorAdapter;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
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
 *
 */
public class NodeConverterUtils {

  /** The convention used to adjust date after roll date adjuster. **/
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  /**
   * Creates a {@link SwapDefinition} from the pay and receive conventions of a swap.
   * If both legs are floating, the spread will be on the receiver leg.
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
  public static SwapDefinition getSwapDefinition(
      final FinancialConvention payLegConvention, final FinancialConvention receiveLegConvention, final Period startTenor, final Period maturityTenor,
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
    final boolean isFloatFloat = isFloatFloat(payLegConvention, receiveLegConvention);
    final AnnuityDefinition<? extends PaymentDefinition> payLeg = getSwapLeg(payLegConvention, startTenor, maturityTenor, regionSource, holidaySource,
        conventionSource, marketData, dataId, valuationTime, true, isFloatFloat);
    final AnnuityDefinition<? extends PaymentDefinition> receiveLeg = getSwapLeg(receiveLegConvention, startTenor, maturityTenor, regionSource, holidaySource,
        conventionSource, marketData, dataId, valuationTime, false, isFloatFloat);
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
      final int startNumberRollDate, final int endNumberRollDate, final RollDateAdjuster adjuster, final RegionSource regionSource, final HolidaySource holidaySource, 
      final ConventionSource conventionSource, final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
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
    final AnnuityDefinition<? extends PaymentDefinition> payLeg = getRollDateSwapLeg(payLegConvention, unadjustedStartDate, startNumberRollDate, endNumberRollDate, adjuster, regionSource, 
        holidaySource, conventionSource, marketData, dataId, valuationTime, true, isFloatFloat);
    final AnnuityDefinition<? extends PaymentDefinition> receiveLeg = getRollDateSwapLeg(receiveLegConvention, unadjustedStartDate, startNumberRollDate, endNumberRollDate, adjuster, regionSource, 
        holidaySource, conventionSource, marketData, dataId, valuationTime, false, isFloatFloat);
    return new SwapDefinition(payLeg, receiveLeg);
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
  public static AnnuityDefinition<? extends PaymentDefinition> getSwapLeg(
      final FinancialConvention legConvention, final Period startTenor,
      final Period maturityTenor, final RegionSource regionSource, final HolidaySource holidaySource, final ConventionSource conventionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime, final boolean isPayer,
      final boolean isMarketDataSpread) {
    final FinancialConventionVisitor<AnnuityDefinition<? extends PaymentDefinition>> visitor = new FinancialConventionVisitorAdapter<AnnuityDefinition<? extends PaymentDefinition>>() {

      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitONArithmeticAverageLegConvention(final ONArithmeticAverageLegConvention convention) {
        final OvernightIndexConvention indexConvention = conventionSource.getConvention(OvernightIndexConvention.class, convention.getOvernightIndexConvention());
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final IndexON indexON = indexON(indexConvention);
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        final boolean eomLeg = convention.isIsEOM();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
        // final int paymentLag = convention.getPaymentLag();
        // TODO: Add the payment lag in the builder.
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
        final StubType stub = convention.getStubType();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        if (isMarketDataSpread) {
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return AnnuityDefinitionBuilder.couponONArithmeticAverageSpreadSimplified(startDate, maturityDate, paymentPeriod, 1.0d, spread, indexON, isPayer, 
              businessDayConvention, eomLeg, calendar, stub);
        }
        return AnnuityDefinitionBuilder.couponONArithmeticAverageSpreadSimplified(startDate, maturityDate, paymentPeriod, 1.0d, 0.0d, indexON, isPayer, 
            businessDayConvention, eomLeg, calendar, stub);
      }
      
      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitCompoundingIborLegConvention(final CompoundingIborLegConvention convention) {
        final IborIndexConvention indexConvention = conventionSource.getConvention(IborIndexConvention.class, convention.getIborIndexConvention());
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final boolean eomLeg = convention.isIsEOM();
        final Period indexTenor = convention.getCompositionTenor().getPeriod();
        final Period paymentTenor = convention.getPaymentTenor().getPeriod();
        final IborIndex iborIndex = indexIbor(indexConvention, indexTenor);
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, iborIndex.getBusinessDayConvention(), calendar, eomLeg);
        final StubType stubLeg = convention.getStubTypeLeg();
        final StubType stubComp = convention.getStubTypeCompound();
        final boolean eom = convention.isIsEOM();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        if (!isPayer && isMarketDataSpread) {
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return AnnuityDefinitionBuilder.couponIborCompoundingSpread(startDate, maturityDate, paymentTenor, 1, spread, iborIndex, stubComp, isPayer,
              iborIndex.getBusinessDayConvention(), eom, calendar, stubLeg);
        }
        return AnnuityDefinitionBuilder.couponIborCompounding(startDate, maturityDate, paymentTenor, 1, iborIndex, stubComp, isPayer,
            iborIndex.getBusinessDayConvention(), eom, calendar, stubLeg);
      }

      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitOISLegConvention(final OISLegConvention convention) {
        final OvernightIndexConvention indexConvention = conventionSource.getConvention(OvernightIndexConvention.class, convention.getOvernightIndexConvention());
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final IndexON indexON = indexON(indexConvention);
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
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
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
        final StubType stub = convention.getStubType();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        return AnnuityDefinitionBuilder.couponFixed(currency, startDate, maturityDate, paymentPeriod, calendar, dayCount, businessDayConvention, eomLeg, 1.0d, rate, isPayer, stub);
      }

      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitVanillaIborLegConvention(final VanillaIborLegConvention convention) {
        final IborIndexConvention indexConvention = conventionSource.getConvention(IborIndexConvention.class, convention.getIborIndexConvention());
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final boolean eomLeg = convention.isIsEOM();
        final Period indexTenor = convention.getResetTenor().getPeriod();
        final IborIndex iborIndex = indexIbor(indexConvention, indexTenor);
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, iborIndex.getBusinessDayConvention(), calendar, eomLeg);
        final StubType stub = convention.getStubType();
        final boolean eom = convention.isIsEOM();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        if (!isPayer && isMarketDataSpread) {
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return AnnuityCouponIborSpreadDefinition.from(startDate, maturityDate, indexTenor, 1, spread, iborIndex, isPayer, iborIndex.getBusinessDayConvention(), eom, 
              iborIndex.getDayCount(), calendar, stub);
        }
        return AnnuityCouponIborDefinition.from(startDate, maturityDate, indexTenor, 1, iborIndex, isPayer, iborIndex.getBusinessDayConvention(), eom, iborIndex.getDayCount(), calendar, stub);
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
      final FinancialConvention legConvention, final ZonedDateTime unadjustedStartDate,
      final int rollDateStartNumber, final int rollDateEndNumber, final RollDateAdjuster adjuster, final RegionSource regionSource, final HolidaySource holidaySource, 
      final ConventionSource conventionSource, final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime, final boolean isPayer,
      final boolean isMarketDataSpread) {
    final FinancialConventionVisitor<AnnuityDefinition<? extends PaymentDefinition>> visitor = new FinancialConventionVisitorAdapter<AnnuityDefinition<? extends PaymentDefinition>>() {

      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitFixedLegRollDateConvention(final FixedLegRollDateConvention convention) {
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, convention.getRegionCalendar());
        final Double rate = marketData.getDataPoint(dataId);
        if (rate == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
        }
        final Currency currency = convention.getCurrency();
        final DayCount dayCount = convention.getDayCount();
        final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(calendar, unadjustedStartDate);
        final StubType stub = convention.getStubType();
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        return AnnuityDefinitionBuilder.couponFixedRollDate(currency, adjustedStartDate, rollDateStartNumber, rollDateEndNumber, adjuster, paymentPeriod, 1, rate, isPayer, dayCount, calendar, stub);
      }
      
      @Override
      public AnnuityDefinition<? extends PaymentDefinition> visitVanillaIborLegRollDateConvention(final VanillaIborLegRollDateConvention convention) {
        final IborIndexConvention indexConvention = conventionSource.getConvention(IborIndexConvention.class, convention.getIborIndexConvention());
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final Period indexTenor = convention.getResetTenor().getPeriod();
        final IborIndex indexIbor = indexIbor(indexConvention, indexTenor);
        final StubType stub = convention.getStubType();
        final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(calendar, unadjustedStartDate);
        if (!isPayer && isMarketDataSpread) { // The spread is on the receiver leg.
          final Double spread = marketData.getDataPoint(dataId);
          if (spread == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + dataId);
          }
          return null;
        }
        return AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(adjustedStartDate, rollDateStartNumber, rollDateEndNumber, adjuster, indexIbor, 1, 
            isPayer, indexIbor.getDayCount(), calendar, stub);
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
      public Triple<? extends SwapLeg, ZonedDateTime, ZonedDateTime> visitCompoundingIborLegConvention(final CompoundingIborLegConvention convention) {
        final IborIndexConvention indexConvention = conventionSource.getConvention(IborIndexConvention.class, convention.getIborIndexConvention());
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
        final OvernightIndexConvention indexConvention = conventionSource.getConvention(OvernightIndexConvention.class, convention.getOvernightIndexConvention());
        final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
        final Currency currency = indexConvention.getCurrency();
        final DayCount dayCount = indexConvention.getDayCount();
//        final int publicationLag = indexConvention.getPublicationLag();
        final int spotLagLeg = convention.getSettlementDays();
        final ZonedDateTime spotDateLeg = ScheduleCalculator.getAdjustedDate(valuationTime, spotLagLeg, calendar);
//        final IndexON indexON = new IndexON(indexConvention.getName(), currency, dayCount, publicationLag);
        final Period paymentPeriod = convention.getPaymentTenor().getPeriod();
        final boolean eomLeg = convention.isIsEOM();
        final BusinessDayConvention businessDayConvention = convention.getBusinessDayConvention();
//        final int paymentLag = convention.getPaymentLag();
        final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDateLeg, startTenor, businessDayConvention, calendar, eomLeg);
//        final StubType stub = convention.getStubType();
        final ZonedDateTime maturityDate = startDate.plus(maturityTenor);
        if (isMarketDataSpread) {
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
                                         FloatingRateType.OIS,
                                         spread), startDate, maturityDate);
        }
        return Triple.of(new FloatingInterestRateLeg(dayCount,
                                           PeriodFrequency.of(paymentPeriod),
                                           indexConvention.getRegionCalendar(),
                                           businessDayConvention,
                                           new InterestRateNotional(currency, 1),
                                           eomLeg,
                                           dataId, FloatingRateType.OIS), startDate, maturityDate);
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
        final IborIndexConvention indexConvention = conventionSource.getConvention(IborIndexConvention.class, convention.getIborIndexConvention());
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
                                         dataId,
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
    final boolean isFloatFloat = (payLegConvention instanceof VanillaIborLegRollDateConvention)
        && (receiveLegConvention instanceof VanillaIborLegRollDateConvention);
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
  
  /**
   * Create an IndexON from the convention.
   * @param indexConvention The overnight index convention.
   * @return The IndexON object.
   */
  public static IndexON indexON(final OvernightIndexConvention indexConvention) {
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final int publicationLag = indexConvention.getPublicationLag();
    final IndexON indexON = new IndexON(indexConvention.getName(), currency, dayCount, publicationLag);
    return indexON;
  }

}
