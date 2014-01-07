/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponInflationYearOnYearInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationYearOnYear;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a year on year inflation swap.
 */
public class SwapFixedInflationYearOnYearDefinition extends SwapDefinition {

  /**
   * Year on year inflation swap constructor for the fixed and inflation coupons.
   * @param fixedLeg The swap fixed leg.
   * @param inflationLeg The swap inflation leg.
   */
  public SwapFixedInflationYearOnYearDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityDefinition<? extends PaymentDefinition> inflationLeg) {
    super(fixedLeg, inflationLeg);
    ArgumentChecker.isTrue(fixedLeg.getCurrency().equals(inflationLeg.getCurrency()), "legs should have the same currency");
  }

  /**
   * Constructor of the fixed- Year on year swap from its two legs. (with monthly values of price index)
   * @param fixedLeg The fixed leg.
   * @param inflationLeg The inflation leg. (the inflation coupons contains monthly price index values)
   */
  public SwapFixedInflationYearOnYearDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityCouponInflationYearOnYearMonthlyDefinition inflationLeg) {
    super(fixedLeg, inflationLeg);
    ArgumentChecker.isTrue(fixedLeg.getCurrency().equals(inflationLeg.getCurrency()), "legs should have the same currency");
  }

  /**
   * Constructor of the fixed- Year on year swap from its two legs. (with interpolated values of price index)
   * @param fixedLeg The fixed leg.
   * @param inflationLeg The inflation leg. (the inflation coupons contains interpolated price index values)
   */
  public SwapFixedInflationYearOnYearDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityCouponInflationYearOnYearInterpolationDefinition inflationLeg) {
    super(fixedLeg, inflationLeg);
    ArgumentChecker.isTrue(fixedLeg.getCurrency().equals(inflationLeg.getCurrency()), "legs should have the same currency");
  }

  /**
   * Builder from financial details and the time series of exiting price index values.
   * @param priceIndex The price index.
   * @param settlementDate The swap settlement date.
   * @param paymentPeriod the period between each payment.
   * @param tenor The swap tenor in years.
   * @param fixedRate The swap fixed rate (annual compounding). The fixed payment is (1+fixedRate)^tenor-1.
   * @param notional The swap notional.
   * @param isPayer The flag
   * @param businessDayConvention The business day convention used to compute the payment date.
   * @param calendar The calendar used to compute the payment date.
   * @param endOfMonth The end-of-month convention used to compute the payment date.
   * @param fixedLegDayCount The day counter of the fixed leg.
   * @param conventionalMonthLag The month lag
   * @param monthLag The price index fixing lag in months.
   * @param payNotional flag if the notional is paid or not (false is the standard case).
   * @return The year on year inflation swap.
   */
  public static SwapFixedInflationYearOnYearDefinition fromMonthly(final IndexPrice priceIndex, final ZonedDateTime settlementDate, final Period paymentPeriod, final int tenor,
      final double fixedRate, final double notional, final boolean isPayer, final BusinessDayConvention businessDayConvention, final Calendar calendar, final boolean endOfMonth,
      final DayCount fixedLegDayCount, final int conventionalMonthLag, final int monthLag, final boolean payNotional) {
    ArgumentChecker.notNull(priceIndex, "Price index");
    ArgumentChecker.notNull(settlementDate, "Settlement date");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(calendar, "Calendar");
    final AnnuityCouponFixedDefinition fixedLeg = AnnuityCouponFixedDefinition.from(priceIndex.getCurrency(), settlementDate, Period.ofYears(tenor), paymentPeriod, calendar,
        fixedLegDayCount, businessDayConvention, endOfMonth, notional, fixedRate, isPayer);
    final AnnuityCouponInflationYearOnYearMonthlyDefinition inflationLeg = AnnuityCouponInflationYearOnYearMonthlyDefinition.from(priceIndex, settlementDate, notional, Period.ofYears(tenor),
        paymentPeriod, businessDayConvention, calendar, endOfMonth, conventionalMonthLag, monthLag, payNotional);
    return new SwapFixedInflationYearOnYearDefinition(fixedLeg, inflationLeg);
  }

  /**
   * Builder from financial details and the time series of exiting price index values and a generator of swap.
   * @param settlementDate The swap settlement date.
   * @param fixedRate The swap fixed rate (annual compounding). The fixed payment is (1+fixedRate)^tenor-1.
   * @param notional The swap notional.
   * @param tenor the tenor of the instrument
   * @param generator the generator of swap
   * @param isPayer The flag
   * @return The year on year inflation swap.
   */
  public static SwapFixedInflationYearOnYearDefinition fromGeneratorMonthly(final ZonedDateTime settlementDate, final double fixedRate, final double notional, final Period tenor,
      final GeneratorSwapFixedInflationYearOnYear generator, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "Settlement date");
    ArgumentChecker.notNull(generator, "generator");
    ArgumentChecker.notNull(tenor, "tenor");
    final AnnuityCouponFixedDefinition fixedLeg = AnnuityCouponFixedDefinition.from(generator.getIndexPrice().getCurrency(), settlementDate, tenor, generator.getFixedLegPeriod(),
        generator.getCalendar(), generator.getFixedLegDayCount(), generator.getBusinessDayConvention(), generator.isEndOfMonth(), notional, fixedRate, isPayer);
    final AnnuityCouponInflationYearOnYearMonthlyDefinition inflationLeg = AnnuityCouponInflationYearOnYearMonthlyDefinition.from(generator.getIndexPrice(), settlementDate, notional,
        tenor, generator.getFixedLegPeriod(), generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth(), generator.getMonthLag(), generator.getMonthLag(),
        generator.payNotional());
    return new SwapFixedInflationYearOnYearDefinition(fixedLeg, inflationLeg);
  }

  /**
   * Builder from financial details and the time series of exiting price index values.
   * @param priceIndex The price index.
   * @param settlementDate The swap settlement date.
   * @param paymentPeriod the period between each payment.
   * @param tenor The swap tenor in years.
   * @param fixedRate The swap fixed rate (annual compounding). The fixed payment is (1+fixedRate)^tenor-1.
   * @param notional The swap notional.
   * @param isPayer The flag
   * @param businessDayConvention The business day convention used to compute the payment date.
   * @param calendar The calendar used to compute the payment date.
   * @param endOfMonth The end-of-month convention used to compute the payment date.
   * @param fixedLegDayCount The day counter of the fixed leg.
   * @param conventionalMonthLag The month lag
   * @param monthLag The price index fixing lag in months.
   * @param payNotional flag if the notional is paid or not (false is the standard case).
   * @return The year on year inflation swap.
   */
  public static SwapFixedInflationYearOnYearDefinition fromInterpolation(final IndexPrice priceIndex, final ZonedDateTime settlementDate, final Period paymentPeriod, final Period tenor,
      final double fixedRate, final double notional, final boolean isPayer, final BusinessDayConvention businessDayConvention, final Calendar calendar, final boolean endOfMonth,
      final DayCount fixedLegDayCount, final int conventionalMonthLag, final int monthLag, final boolean payNotional) {
    ArgumentChecker.notNull(priceIndex, "Price index");
    ArgumentChecker.notNull(settlementDate, "Settlement date");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(calendar, "Calendar");
    final AnnuityCouponFixedDefinition fixedLeg = AnnuityCouponFixedDefinition.from(priceIndex.getCurrency(), settlementDate, tenor, paymentPeriod, calendar,
        fixedLegDayCount, businessDayConvention, endOfMonth, notional, fixedRate, isPayer);
    final AnnuityCouponInflationYearOnYearInterpolationDefinition inflationLeg = AnnuityCouponInflationYearOnYearInterpolationDefinition.from(priceIndex, settlementDate, notional,
        tenor, paymentPeriod, businessDayConvention, calendar, endOfMonth, conventionalMonthLag, monthLag, payNotional);
    return new SwapFixedInflationYearOnYearDefinition(fixedLeg, inflationLeg);
  }

  /**
   * Builder from financial details and the time series of exiting price index values and a generator of swap.
   * @param settlementDate The swap settlement date.
   * @param fixedRate The swap fixed rate (annual compounding). The fixed payment is (1+fixedRate)^tenor-1.
   * @param notional The swap notional.
   * @param tenor the tenor of the instrument.
   * @param generator The swap generator.
   * @param isPayer The flag
   * @return The year on year inflation swap.
   */
  public static SwapFixedInflationYearOnYearDefinition fromGeneratorInterpolation(final ZonedDateTime settlementDate, final double fixedRate, final double notional, final Period tenor,
      final GeneratorSwapFixedInflationYearOnYear generator, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "Settlement date");
    ArgumentChecker.notNull(generator, "generator");
    ArgumentChecker.notNull(tenor, "tenor");
    final AnnuityCouponFixedDefinition fixedLeg = AnnuityCouponFixedDefinition.from(generator.getIndexPrice().getCurrency(), settlementDate, tenor, generator.getFixedLegPeriod(),
        generator.getCalendar(), generator.getFixedLegDayCount(), generator.getBusinessDayConvention(), generator.isEndOfMonth(), notional, fixedRate, isPayer);
    final AnnuityCouponInflationYearOnYearInterpolationDefinition inflationLeg = AnnuityCouponInflationYearOnYearInterpolationDefinition.from(generator.getIndexPrice(), settlementDate,
        notional, tenor, generator.getFixedLegPeriod(), generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth(), generator.getMonthLag(), generator.getMonthLag(),
        generator.payNotional());
    return new SwapFixedInflationYearOnYearDefinition(fixedLeg, inflationLeg);
  }

}
