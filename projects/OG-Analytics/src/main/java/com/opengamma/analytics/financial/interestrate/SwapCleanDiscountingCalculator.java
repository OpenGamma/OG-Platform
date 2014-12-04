/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
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
 * Computes par rate (excluding accrued interests) and accrued interest. <br> 
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
    checkNotionalAndFixedRate(swapDefinition);

    Annuity<? extends Coupon> iborLeg = swapDefinition.getIborLeg().toDerivative(valuationDate, indexTimeSeries);
    double dirtyIborLegPV = iborLeg.accept(PVDC, multicurves).getAmount(iborLeg.getCurrency());
    double iborLegAccruedInterest = getAccrued(iborLegDayCount, calendar, valuationDate,
        swapDefinition.getIborLeg(), indexTimeSeries);
    double cleanFloatingPV = (dirtyIborLegPV - iborLegAccruedInterest) *
        Math.signum(iborLeg.getNthPayment(0).getNotional());
    
    AnnuityCouponFixed fixedLeg = swapDefinition.getFixedLeg().toDerivative(valuationDate);
    double accruedAmount = getAccrued(fixedLegDayCount, calendar, valuationDate, swapDefinition.getFixedLeg(),
        indexTimeSeries) * Math.signum(fixedLeg.getNthPayment(0).getNotional());
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
    checkNotionalAndFixedRate(swapDefinition);

    double iborLegAccruedInterest = getAccrued(iborLegDayCount, calendar, valuationDate, swapDefinition.getIborLeg(),
        indexTimeSeries);

    CouponFixedDefinition refFixed = swapDefinition.getFixedLeg().getNthPayment(0);
    double fixedLegAccruedInterest = getAccrued(fixedLegDayCount, calendar, valuationDate,
        swapDefinition.getFixedLeg(), indexTimeSeries) * refFixed.getRate();

    return MultipleCurrencyAmount.of(swapDefinition.getCurrency(), iborLegAccruedInterest + fixedLegAccruedInterest);
  }

  private double getAccrued(DayCount dayCount, Calendar calendar, ZonedDateTime valuationDate,
      AnnuityDefinition<? extends CouponDefinition> annuity, ZonedDateTimeDoubleTimeSeries indexTimeSeries) {
    LocalDate date = valuationDate.toLocalDate();
    double res = 0.0;
    CouponDefinition[] payments = annuity.getPayments();
    for (CouponDefinition payment : payments) {
      if (payment.getAccrualStartDate().toLocalDate().isBefore(date) &&
          !payment.getPaymentDate().toLocalDate().isBefore(date)) {
        double rate;
        if (payment instanceof CouponIborDefinition) {
          CouponIborDefinition casted = (CouponIborDefinition) payment;
          Coupon coupon = casted.toDerivative(valuationDate, indexTimeSeries);
          ArgumentChecker.isTrue(coupon instanceof CouponFixed,
              "index should be fixed before accrual starts for standard vanilla swap");
          CouponFixed couponFixed = (CouponFixed) coupon;
          rate = couponFixed.getFixedRate();
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
    return accruedYearFraction * coupon.getNotional();
  }

  private void checkNotionalAndFixedRate(SwapFixedIborDefinition swapDefinition) {
    AnnuityCouponIborDefinition iborLeg = swapDefinition.getIborLeg();
    int nIbor = iborLeg.getNumberOfPayments();
    double notioanl = iborLeg.getNthPayment(0).getNotional();
    for (int i = 1; i < nIbor; ++i) {
      ArgumentChecker.isTrue(notioanl == iborLeg.getNthPayment(i).getNotional(),
          "Notional should be constant in both the legs");
    }
    AnnuityCouponFixedDefinition fixedLeg = swapDefinition.getFixedLeg();
    int nFixed = fixedLeg.getNumberOfPayments();
    double rate = fixedLeg.getNthPayment(0).getRate();
    notioanl *= -1.0; // payer/receiver conversion
    ArgumentChecker.isTrue(notioanl == fixedLeg.getNthPayment(0).getNotional(),
        "Notional should be constant in both the legs");
    for (int i = 1; i < nFixed; ++i) {
      ArgumentChecker.isTrue(rate == fixedLeg.getNthPayment(i).getRate(), "Fixed rate should be constant");
      ArgumentChecker.isTrue(notioanl == fixedLeg.getNthPayment(i).getNotional(),
          "Notional should be constant in both the legs");
    }
  }
}
