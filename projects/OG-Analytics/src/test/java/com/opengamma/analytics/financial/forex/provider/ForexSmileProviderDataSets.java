/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 * Sets of market data used in Forex tests.
 */
public class ForexSmileProviderDataSets {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final Period[] EXPIRY_PERIOD = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1),
      Period.ofYears(2), Period.ofYears(5) };
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final double[] ATM = {0.185, 0.18, 0.17, 0.16, 0.16 };

  private static final double[] DELTA_2 = new double[] {0.10, 0.25 };
  private static final double[][] RISK_REVERSAL_2 = new double[][] {{-0.011, -0.0060 }, {-0.012, -0.0070 }, {-0.013, -0.0080 }, {-0.014, -0.0090 }, {-0.014, -0.0090 } };
  private static final double[][] STRANGLE_2 = new double[][] {{0.0310, 0.0110 }, {0.0320, 0.0120 }, {0.0330, 0.0130 }, {0.0340, 0.0140 }, {0.0340, 0.0140 } };

  private static final double[] DELTA_1 = new double[] {0.25 };
  private static final double[][] RISK_REVERSAL_1 = new double[][] {{-0.0060 }, {-0.0070 }, {-0.0080 }, {-0.0090 }, {-0.0090 } };
  private static final double[][] STRANGLE_1 = new double[][] {{0.0110 }, {0.0120 }, {0.0130 }, {0.0140 }, {0.0140 } };

  private static final double[][] RISK_REVERSAL_FLAT = new double[][] {{0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 } };
  private static final double[][] STRANGLE_FLAT = new double[][] {{0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 } };

  public static SmileDeltaTermStructureParametersStrikeInterpolation smile5points(final ZonedDateTime referenceDate) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, ATM, RISK_REVERSAL_2, STRANGLE_2);
  }

  public static SmileDeltaTermStructureParametersStrikeInterpolation smile5points(final ZonedDateTime referenceDate, final Interpolator1D interpolator) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, ATM, RISK_REVERSAL_2, STRANGLE_2, interpolator);
  }

  public static SmileDeltaTermStructureParametersStrikeInterpolation smile3points(final ZonedDateTime referenceDate, final Interpolator1D interpolator) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_1, ATM, RISK_REVERSAL_1, STRANGLE_1, interpolator);
  }

  public static SmileDeltaTermStructureParametersStrikeInterpolation smile5points(final ZonedDateTime referenceDate, final double shift) {
    final double[] atmShift = ATM.clone();
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      atmShift[loopexp] += shift;
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, atmShift, RISK_REVERSAL_2, STRANGLE_2);
  }

  public static SmileDeltaTermStructureParametersStrikeInterpolation smileFlat(final ZonedDateTime referenceDate) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, ATM, RISK_REVERSAL_FLAT, STRANGLE_FLAT);
  }

}
