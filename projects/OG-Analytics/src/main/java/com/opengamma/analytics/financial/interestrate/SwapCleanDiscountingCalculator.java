/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Computes par rate excluding accrued interest. <br> 
 * <b>Use {@link ParRateDiscountingCalculator} for standard definition of par rate.<b>
 */
public class SwapCleanDiscountingCalculator {
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes par rate excluding accrued interest.
   * @param swapDefinition Fixed vs Ibor swap definition
   * @param fixedLegDayCount Day count for fixed leg 
   * @param iborLegDayCount Day count for Ibor leg
   * @param calendar The calendar
   * @param valuationDate The valuation date
   * @param indexTimeSeries Index fixing time series 
   * @param multicurves The multi-curve
   * @return The par rate
   */
  public Double parRate(SwapFixedIborDefinition swapDefinition, DayCount fixedLegDayCount,
      DayCount iborLegDayCount, Calendar calendar, ZonedDateTime valuationDate,
      ZonedDateTimeDoubleTimeSeries indexTimeSeries, MulticurveProviderDiscount multicurves) {
    ArgumentChecker.notNull(swapDefinition, "swapDefinition");
    ArgumentChecker.notNull(fixedLegDayCount, "fixedLegDayCount");
    ArgumentChecker.notNull(iborLegDayCount, "iborLegDayCount");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(valuationDate, "valuationDate");
    ArgumentChecker.notNull(indexTimeSeries, "indexTimeSeries");
    ArgumentChecker.notNull(multicurves, "multicurves");

    //    AnnuityCouponIborDefinition iborLegDefinition = (AnnuityCouponIborDefinition) trimAnnuity(
    //        swapDefinition.getIborLeg(), calendar, valuationDate);
    //    Annuity<? extends Coupon> iborLeg = iborLegDefinition.toDerivative(valuationDate, indexTimeSeries);
    Annuity<? extends Coupon> iborLeg = swapDefinition.getIborLeg().toDerivative(valuationDate, indexTimeSeries);
    double dirtyIborLegPV = iborLeg.accept(PVDC, multicurves).getAmount(iborLeg.getCurrency()) *
        Math.signum(iborLeg.getNthPayment(0).getNotional());
    //    CouponFixed firstCoupon = (CouponFixed) iborLeg.getNthPayment(0);
    //    double iborLegAccruedInterest = getAccrued(iborLegDayCount, calendar, valuationDate,
    //        iborLegDefinition.getNthPayment(0)) * firstCoupon.getFixedRate();
    double iborLegAccruedInterest = getAccrued(iborLegDayCount, calendar, valuationDate,
        swapDefinition.getIborLeg(), indexTimeSeries);
    double cleanFloatingPV = dirtyIborLegPV - iborLegAccruedInterest;
    
    //    AnnuityCouponFixedDefinition fixedLegDefinition = (AnnuityCouponFixedDefinition) trimAnnuity(
    //        swapDefinition.getFixedLeg(), calendar, valuationDate);
    //    AnnuityCouponFixed fixedLeg = fixedLegDefinition.toDerivative(valuationDate);
    //    double accruedAmount = getAccrued(fixedLegDayCount, calendar, valuationDate, fixedLegDefinition.getNthPayment(0));
    double accruedAmount = getAccrued(fixedLegDayCount, calendar, valuationDate, swapDefinition.getFixedLeg(),
        indexTimeSeries);
    AnnuityCouponFixed fixedLeg = swapDefinition.getFixedLeg().toDerivative(valuationDate);
    double dirtyAnnuity = METHOD_SWAP.presentValueBasisPoint(new SwapFixedCoupon<>(fixedLeg, iborLeg),
        multicurves);
    double cleanAnnuity = dirtyAnnuity - accruedAmount;

    return cleanFloatingPV / cleanAnnuity;
  }

  /**
   * Computes accrued interest
   * @param swapDefinition Fixed vs Ibor swap definition
   * @param fixedLegDayCount Day count for fixed leg 
   * @param iborLegDayCount Day count for Ibor leg
   * @param calendar The calendar
   * @param valuationDate The valuation date
   * @param indexTimeSeries Index fixing time series 
   * @param multicurves The multi-curve
   * @return The accrued interest
   */
  public MultipleCurrencyAmount accruedInterest(SwapFixedIborDefinition swapDefinition, DayCount fixedLegDayCount,
      DayCount iborLegDayCount, Calendar calendar, ZonedDateTime valuationDate,
      ZonedDateTimeDoubleTimeSeries indexTimeSeries, MulticurveProviderDiscount multicurves) {
    ArgumentChecker.notNull(swapDefinition, "swapDefinition");
    ArgumentChecker.notNull(fixedLegDayCount, "fixedLegDayCount");
    ArgumentChecker.notNull(iborLegDayCount, "iborLegDayCount");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(valuationDate, "valuationDate");
    ArgumentChecker.notNull(indexTimeSeries, "indexTimeSeries");
    ArgumentChecker.notNull(multicurves, "multicurves");

    Annuity<? extends Coupon> iborLeg = swapDefinition.getIborLeg().toDerivative(valuationDate, indexTimeSeries);
    //    CouponFixed firstCoupon = (CouponFixed) iborLeg.getNthPayment(0);
    //    double iborLegAccruedInterest = getAccrued(iborLegDayCount, calendar, valuationDate,
    //        iborLegDefinition.getNthPayment(0)) * firstCoupon.getFixedRate();
    double iborLegAccruedInterest = getAccrued(iborLegDayCount, calendar, valuationDate,
        swapDefinition.getIborLeg(), indexTimeSeries) * Math.signum(iborLeg.getNthPayment(0).getNotional());

    //    AnnuityCouponFixedDefinition fixedLegDefinition = (AnnuityCouponFixedDefinition) trimAnnuity(
    //        swapDefinition.getFixedLeg(), calendar, valuationDate);
    //    AnnuityCouponFixed fixedLeg = fixedLegDefinition.toDerivative(valuationDate);
    //    double accruedAmount = getAccrued(fixedLegDayCount, calendar, valuationDate, fixedLegDefinition.getNthPayment(0));
    CouponFixedDefinition refFixed = swapDefinition.getFixedLeg().getNthPayment(0);
    double fixedLegAccruedInterest = getAccrued(fixedLegDayCount, calendar, valuationDate,
        swapDefinition.getFixedLeg(),
        indexTimeSeries) * Math.signum(refFixed.getNotional()) * refFixed.getRate();

    //    AnnuityCouponIborDefinition iborLegDefinition = (AnnuityCouponIborDefinition) trimAnnuity(
    //        swapDefinition.getIborLeg(), calendar, valuationDate);
    //    CouponFixed firstCoupon = (CouponFixed) iborLegDefinition.getNthPayment(0).toDerivative(valuationDate,
    //        indexTimeSeries);
    //    double iborLegAccruedInterest = getAccrued(iborLegDayCount, calendar, valuationDate,
    //        iborLegDefinition.getNthPayment(0)) * firstCoupon.getFixedRate() * firstCoupon.getNotional();
    //
    //    AnnuityCouponFixedDefinition fixedLegDefinition = (AnnuityCouponFixedDefinition) trimAnnuity(
    //        swapDefinition.getFixedLeg(), calendar, valuationDate);
    //    CouponFixedDefinition firstCouponDDefinition = fixedLegDefinition.getNthPayment(0);
    //    double fixedLegAccruedInterest = getAccrued(fixedLegDayCount, calendar, valuationDate, firstCouponDDefinition) *
    //        firstCouponDDefinition.getRate() * firstCouponDDefinition.getNotional();

    return MultipleCurrencyAmount.of(swapDefinition.getCurrency(), iborLegAccruedInterest + fixedLegAccruedInterest);
  }

  //  private double getCleanPresentValue(AnnuityCouponIborDefinition floatingLegDefnition, DayCount floatingLegDayCount,
  //      Calendar calendar, ZonedDateTime valuationDate, ZonedDateTimeDoubleTimeSeries indexTimeSeries,
  //      MulticurveProviderDiscount multicurves) {
  //    IborIndex index = floatingLegDefnition.getIborIndex();
  //    CouponIborDefinition[] paymentsFloating = floatingLegDefnition.getPayments();
  //    List<CouponIborDefinition> listFloating = new ArrayList<>();
  //    for (CouponIborDefinition payment : paymentsFloating) {
  //      if (!payment.getPaymentDate().isBefore(valuationDate)) {
  //        listFloating.add(payment);
  //      }
  //    }
  //    AnnuityCouponIborDefinition trimedFloatingLeg = new AnnuityCouponIborDefinition(
  //        listFloating.toArray(new CouponIborDefinition[listFloating.size()]), index, calendar);
  //    Annuity<? extends Coupon> floatingLegDerivative = trimedFloatingLeg.toDerivative(valuationDate, indexTimeSeries);
  //    double accruedYearFractionFloating = floatingLegDayCount.getDayCountFraction(trimedFloatingLeg.getNthPayment(0)
  //        .getAccrualStartDate(), valuationDate, calendar);
  //    double dirtyFloatingPV = floatingLegDerivative.accept(PVDC, multicurves).getAmount(
  //        floatingLegDerivative.getCurrency()) * Math.signum(floatingLegDerivative.getNthPayment(0).getNotional());
  //    /* Cast is possible for standard instruments because fixing date is before accrual start date */
  //    ArgumentChecker.isTrue(floatingLegDerivative.getNthPayment(0) instanceof CouponFixed,
  //        "ibor rate for the first payment is not fixed yet, thus accrued interst is not computed");
  //    CouponFixed firstCoupon = (CouponFixed) floatingLegDerivative.getNthPayment(0);
  //    double accruedInterestFloating = firstCoupon.getFixedRate() * accruedYearFractionFloating *
  //        firstCoupon.getNotional();
  //    return dirtyFloatingPV - accruedInterestFloating;
  //  }
  
  //  private double getAccruedIbor(DayCount dayCount, Calendar calendar, ZonedDateTime valuationDate,
  //      AnnuityCouponIborDefinition annuity, ZonedDateTimeDoubleTimeSeries indexTimeSeries) {
  //    double res = 0.0;
  //    CouponIborDefinition[] payments = annuity.getPayments();
  //    for (CouponIborDefinition payment : payments) {
  //      if (payment.getAccrualStartDate().isBefore(valuationDate) && !payment.getPaymentDate().isBefore(valuationDate)) {
  //        CouponFixed coupon = (CouponFixed) payment.toDerivative(valuationDate, indexTimeSeries);
  //        res += getAccrued(dayCount, calendar, valuationDate, payment) * coupon.getFixedRate();
  //      }
  //    }
  //    return res;
  //  }
  //
  //  private double getAccruedFixed(DayCount dayCount, Calendar calendar, ZonedDateTime valuationDate,
  //      AnnuityCouponFixedDefinition annuity) {
  //    double res = 0.0;
  //    CouponFixedDefinition[] payments = annuity.getPayments();
  //    for (CouponFixedDefinition payment : payments) {
  //      if (payment.getAccrualStartDate().isBefore(valuationDate) && !payment.getPaymentDate().isBefore(valuationDate)) {
  //        res += getAccrued(dayCount, calendar, valuationDate, payment);
  //      }
  //    }
  //    return res;
  //  }

  private double getAccrued(DayCount dayCount, Calendar calendar, ZonedDateTime valuationDate,
      AnnuityDefinition<? extends CouponDefinition> annuity, ZonedDateTimeDoubleTimeSeries indexTimeSeries) {
    double res = 0.0;
    CouponDefinition[] payments = annuity.getPayments();
    for (CouponDefinition payment : payments) {
      if (payment.getAccrualStartDate().isBefore(valuationDate) && !payment.getPaymentDate().isBefore(valuationDate)) {
        double rate;
        if (payment instanceof CouponIborDefinition) {
          CouponIborDefinition casted = (CouponIborDefinition) payment;
          CouponFixed coupon = (CouponFixed) casted.toDerivative(valuationDate, indexTimeSeries);
          rate = coupon.getFixedRate();
        } else if (payment instanceof CouponFixedDefinition) {
          rate = 1.0;
        } else {
          throw new IllegalArgumentException("This annuity type is not supported");
        }
        res += getAccrued(dayCount, calendar, valuationDate, payment) * rate;
      }
    }
    return res;
  }

  private double getAccrued(DayCount dayCount, Calendar calendar, ZonedDateTime valuationDate, CouponDefinition coupon) {
    double accruedYearFraction = dayCount.getDayCountFraction(coupon.getAccrualStartDate(), valuationDate, calendar);
    return accruedYearFraction * Math.abs(coupon.getNotional());
  }
  
  //  private AnnuityDefinition<?> trimAnnuity(AnnuityDefinition<?> annuity, Calendar calendar,
  //      ZonedDateTime valuationDate) {
  //    PaymentDefinition[] payments = annuity.getPayments();
  //    List<PaymentDefinition> list = new ArrayList<>();
  //    for (PaymentDefinition payment : payments) {
  //      if (!payment.getPaymentDate().isBefore(valuationDate)) {
  //        list.add(payment);
  //      }
  //    }
  //    if (annuity instanceof AnnuityCouponIborDefinition) {
  //      AnnuityCouponIborDefinition casted = (AnnuityCouponIborDefinition) annuity;
  //      return new AnnuityCouponIborDefinition(list.toArray(new CouponIborDefinition[list.size()]),
  //          casted.getIborIndex(), calendar);
  //    } else if (annuity instanceof AnnuityCouponFixedDefinition) {
  //      return new AnnuityCouponFixedDefinition(list.toArray(new CouponFixedDefinition[list.size()]), calendar);
  //    }
  //    throw new IllegalArgumentException("This annuity type is not supported");
  //  }
}
