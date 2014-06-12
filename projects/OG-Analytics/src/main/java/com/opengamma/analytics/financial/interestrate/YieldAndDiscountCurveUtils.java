/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Utils function related to YieldAndDiscount curves and MulticurveProvider.
 */
public class YieldAndDiscountCurveUtils {

  /**
   * Computes the forward rate for a given index in a given curve 
   * @param curve The curve.
   * @param curveDate The curve date.
   * @param fixingDate The forward rate fixing date.
   * @param index The Ibor index.
   * @param cal Calendar used to compute the fixing period of the index.
   * @return The forward.
   */
  public static double forwardRateFromCurve(final YieldAndDiscountCurve curve, final ZonedDateTime curveDate, final ZonedDateTime fixingDate, final IborIndex index, final Calendar cal) {
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.notNull(curveDate, "curveDate");
    ArgumentChecker.notNull(fixingDate, "fixingDate");
    ArgumentChecker.notNull(index, "iborIndex");
    ArgumentChecker.notNull(cal, "calendar");
    final ZonedDateTime fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, index.getSpotLag(), cal);
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(curveDate, fixingPeriodStartDate);
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingDate, index, cal);
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(curveDate, fixingPeriodEndDate);
    final double accrualFixing = index.getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate);
    final double dfStart = curve.getDiscountFactor(fixingPeriodStartTime);
    final double dfEnd = curve.getDiscountFactor(fixingPeriodEndTime);
    final double forwardRate = (dfStart / dfEnd - 1.0d) / accrualFixing;
    return forwardRate;
  }

  /**
   * Computes the forward rate for a given index in a multicurve provider. The curve used to compute the forward is the curve associated to the index. 
   * @param multicurve The curve provider. Should contain the curve related to the index for which the forward rate is requested.
   * @param curveDate The curve date.
   * @param fixingDate The forward rate fixing date.
   * @param index The Ibor index.
   * @param cal Calendar used to compute the fixing period of the index.
   * @return The forward.
   */
  public static double forwardRateFromProvider(final MulticurveProviderInterface multicurve, final ZonedDateTime curveDate, final ZonedDateTime fixingDate,
      final IborIndex index, final Calendar cal) {
    ArgumentChecker.notNull(multicurve, "multicurve provider");
    ArgumentChecker.notNull(curveDate, "curveDate");
    ArgumentChecker.notNull(fixingDate, "fixingDate");
    ArgumentChecker.notNull(index, "iborIndex");
    ArgumentChecker.notNull(cal, "calendar");
    final ZonedDateTime fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, index.getSpotLag(), cal);
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(curveDate, fixingPeriodStartDate);
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingDate, index, cal);
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(curveDate, fixingPeriodEndDate);
    final double accrualFixing = index.getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate);
    final double forwardRate = multicurve.getSimplyCompoundForwardRate(index, fixingPeriodStartTime, fixingPeriodEndTime, accrualFixing);
    return forwardRate;
  }

  /**
   * Compute the zero coupon for a given payment date.
   * @param curve The curve.
   * @param curveDate The curve date.
   * @param payDate The payment date.
   * @param dc The day count convention.
   * @param paymentPerYear Number of payment per year for a period payment. If paymentPerYear<=0, a continuously compounded rate is computed.
   * @return The rate
   */
  public static double zeroCouponRate(final YieldAndDiscountCurve curve, final ZonedDateTime curveDate, final ZonedDateTime payDate, final DayCount dc, final int paymentPerYear) {
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.notNull(curveDate, "curveDate");
    ArgumentChecker.notNull(payDate, "payDate");
    ArgumentChecker.notNull(dc, "dayCount");
    final double timeCurve = TimeCalculator.getTimeBetween(curveDate, payDate);
    final double df = curve.getDiscountFactor(timeCurve);
    final double timeDc = dc.getDayCountFraction(curveDate, payDate);
    if (paymentPerYear > 0) {
      final double rate = paymentPerYear * (Math.pow(df, -1.0 / (paymentPerYear * timeDc)) - 1.0);
      return rate;
    }
    final double rate = -Math.log(df) / timeDc;
    return rate;
  }
  
  /**
   * Compute the zero coupon for a given payment date.
   * @param curve The curve.
   * @param curveDate The curve date.
   * @param payDate The payment date.
   * @return The rate
   */
  public static double discountFactor(final YieldAndDiscountCurve curve, final ZonedDateTime curveDate, final ZonedDateTime payDate) {
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.notNull(curveDate, "curveDate");
    ArgumentChecker.notNull(payDate, "payDate");
    final double timeCurve = TimeCalculator.getTimeBetween(curveDate, payDate);
    final double df = curve.getDiscountFactor(timeCurve);
    return df;
  }

}
