/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearWithConvexityProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationZeroCouponWithConvexityProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.InflationConvexityAdjustmentProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute the convexity adjustment between two times for year on year coupons and for zero coupons (this adjustment is also used for the computation of the forward in optional inflation instruments)
 */
public class InflationMarketModelConvexityAdjustmentForCapFloor {
  /**
   * Computes the convexity adjustment for year on year inflation swap with a monthly index.
   * @param coupon The year on year coupon.
   * @param inflationConvexity The inflation provider.
   * @return The convexity adjustment.
   */
  public double getYearOnYearConvexityAdjustment(final CapFloorInflationYearOnYearMonthly coupon, final BlackSmileCapInflationYearOnYearWithConvexityProviderInterface inflationConvexity) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflationConvexity, "Inflation");

    final double firstFixingTime = coupon.getReferenceStartTime();
    final double secondFixingTime = coupon.getReferenceEndTime();
    final double firstNaturalPaymentTime = coupon.getNaturalPaymentStartTime();
    final double secondNaturalPaymentTime = coupon.getNaturalPaymentEndTime();
    final double paymentTime = coupon.getPaymentTime();
    final double volatilityStart = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[0];
    final double volatilityEnd = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[1];
    final double correlationInflation = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexCorrelation().getZValue(firstFixingTime, secondFixingTime);
    final double correlationInflationRateStart = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(firstFixingTime);
    final double correlationInflationRateEnd = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(secondFixingTime);
    final double volBondForwardStart = getVolBondForward(firstNaturalPaymentTime, paymentTime, inflationConvexity);
    final double volBondForwardEnd = getVolBondForward(secondNaturalPaymentTime, paymentTime, inflationConvexity);
    final double adjustment = volatilityStart * (volatilityStart - volatilityEnd * correlationInflation - volBondForwardStart * correlationInflationRateStart) * firstNaturalPaymentTime
        + volatilityEnd * volBondForwardEnd * correlationInflationRateEnd * secondNaturalPaymentTime;
    return Math.exp(adjustment);

  }

  /**
   * Computes the convexity adjustment for year on year inflation swap with an interpolated index.
   * @param cap The year on year coupon.
   * @param inflationConvexity The inflation provider.
   * @return The convexity adjustment.
   */
  public double getYearOnYearConvexityAdjustment(final CapFloorInflationYearOnYearInterpolation cap, final BlackSmileCapInflationYearOnYearWithConvexityProviderInterface inflationConvexity) {
    ArgumentChecker.notNull(cap, "Coupon");
    ArgumentChecker.notNull(inflationConvexity, "Inflation");

    final double firstFixingTime = cap.getWeightStart() * cap.getReferenceStartTime()[0] + (1 - cap.getWeightStart()) * cap.getReferenceStartTime()[1];
    final double secondFixingTime = cap.getWeightEnd() * cap.getReferenceEndTime()[0] + (1 - cap.getWeightEnd()) * cap.getReferenceEndTime()[1];
    final double firstNaturalPaymentTime = cap.getNaturalPaymentStartTime();
    final double secondNaturalPaymentTime = cap.getNaturalPaymentEndTime();
    final double paymentTime = cap.getPaymentTime();
    final double volatilityStart = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[0];
    final double volatilityEnd = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[1];
    final double correlationInflation = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexCorrelation().getZValue(firstFixingTime, secondFixingTime);
    final double correlationInflationRateStart = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(firstFixingTime);
    final double correlationInflationRateEnd = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(secondFixingTime);
    final double volBondForwardStart = getVolBondForward(firstNaturalPaymentTime, paymentTime, inflationConvexity);
    final double volBondForwardEnd = getVolBondForward(secondNaturalPaymentTime, paymentTime, inflationConvexity);
    final double adjustment = volatilityStart * (volatilityStart - volatilityEnd * correlationInflation - volBondForwardStart * correlationInflationRateStart) * firstNaturalPaymentTime
        + volatilityEnd * volBondForwardEnd * correlationInflationRateEnd * secondNaturalPaymentTime;
    return Math.exp(adjustment);

  }

  /**
   * Computes the convexity adjustment for zero coupon inflation swap with a monthly index.
  * @param coupon The zero-coupon payment.
   * @param inflationConvexity The inflation provider.
   * @return The convexity adjustment.
   */
  public double getZeroCouponConvexityAdjustment(final CapFloorInflationZeroCouponMonthly coupon, final BlackSmileCapInflationZeroCouponWithConvexityProviderInterface inflationConvexity) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflationConvexity, "Inflation");

    final double fixingTime = coupon.getReferenceEndTime();
    final double naturalPaymentTime = coupon.getNaturalPaymentTime();
    final double paymentTime = coupon.getPaymentTime();

    final double volatility = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[0];
    final double correlationInflationRate = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(fixingTime);
    final double volBondForward = getVolBondForward(naturalPaymentTime, paymentTime, inflationConvexity);
    final double adjustment = volatility * volBondForward * correlationInflationRate * naturalPaymentTime;
    return Math.exp(adjustment);
  }

  /**
   * Computes the convexity adjustment for zero coupon inflation swap with an interpolated index.
  * @param coupon The zero-coupon payment.
   * @param inflationConvexity The inflation provider.
   * @return The convexity adjustment.
   */
  public double getZeroCouponConvexityAdjustment(final CapFloorInflationZeroCouponInterpolation coupon, final BlackSmileCapInflationZeroCouponWithConvexityProviderInterface inflationConvexity) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflationConvexity, "Inflation");

    final double fixingTime = coupon.getWeight() * coupon.getReferenceEndTime()[0] + (1 - coupon.getWeight()) * coupon.getReferenceEndTime()[1];
    final double naturalPaymentTime = coupon.getNaturalPaymentTime();
    final double paymentTime = coupon.getPaymentTime();

    final double volatility = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[0];
    final double correlationInflationRate = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(fixingTime);
    final double volBondForward = getVolBondForward(naturalPaymentTime, paymentTime, inflationConvexity);
    final double adjustment = volatility * volBondForward * correlationInflationRate * naturalPaymentTime;
    return Math.exp(adjustment);
  }

  /**
   * Computes the volatility of a bond forward, a bond forward is defined by his start time and his end time.
   * @param startTime The
   * @param endTime The
   * @param inflationConvexity The
   * @return The convexity adjustment.
   */
  public double getVolBondForward(final double startTime, final double endTime, final InflationConvexityAdjustmentProviderInterface inflationConvexity) {
    ArgumentChecker.isTrue(startTime <= endTime, null);
    if (startTime == endTime) {
      return 0.0;
    }
    final IborIndex iborIndex = inflationConvexity.getBlackSmileIborCapParameters().getIndex();
    final int liborTenorInMonth = iborIndex.getTenor().getMonths();
    final double lenghtOfInterval = liborTenorInMonth / 12.0;
    final int numberOfInterval = (int) Math.round((endTime - startTime) / lenghtOfInterval);

    if (numberOfInterval == 0) {
      double volBondForward = ((endTime - startTime) / lenghtOfInterval) * inflationConvexity.getMulticurveProvider().getSimplyCompoundForwardRate(iborIndex, startTime, endTime, 1.0);
      volBondForward = volBondForward / (1 + volBondForward) * inflationConvexity.getBlackSmileIborCapParameters().getVolatility(endTime);
      return volBondForward;
    }

    // generate the schedule
    final double[] scheduleTimes = new double[numberOfInterval + 2];
    scheduleTimes[numberOfInterval + 1] = endTime;
    for (int i = 0; i < numberOfInterval + 1; i++) {
      scheduleTimes[i] = startTime + i * lenghtOfInterval;
    }

    final double[] volatilityComponents = new double[numberOfInterval + 1];

    double varBondForward = 0.0;

    // implementation note : double sum for the 
    for (int i = 0; i < numberOfInterval + 1; i++) {

      // Implementation note : breaktrough for the last period where the accrued calculation is different. 
      if (i == numberOfInterval + 1) {
        volatilityComponents[i] = (scheduleTimes[i + 1] - scheduleTimes[i]) / lenghtOfInterval *
            inflationConvexity.getMulticurveProvider().getSimplyCompoundForwardRate(iborIndex, scheduleTimes[i], scheduleTimes[i + 1], 1.0);
      } else {
        volatilityComponents[i] = inflationConvexity.getMulticurveProvider().getSimplyCompoundForwardRate(iborIndex, scheduleTimes[i], scheduleTimes[i + 1], 1.0);
      }

      volatilityComponents[i] = volatilityComponents[i] / (1 + volatilityComponents[i]) * inflationConvexity.getBlackSmileIborCapParameters().getVolatility(scheduleTimes[i + 1]);
      varBondForward = varBondForward + volatilityComponents[i] * volatilityComponents[i] * scheduleTimes[i + 1];
      for (int j = 0; j < i; j++) {
        varBondForward = varBondForward + 2 * volatilityComponents[i] * volatilityComponents[j] * scheduleTimes[j + 1] *
            inflationConvexity.getInflationConvexityAdjustmentParameters().getLiborCorrelation().getZValue(scheduleTimes[i], scheduleTimes[j]);
      }
    }
    return Math.sqrt(varBondForward) / endTime;
  }
}
