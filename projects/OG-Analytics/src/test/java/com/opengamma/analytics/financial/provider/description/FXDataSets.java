/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import static com.opengamma.util.money.Currency.EUR;
import static com.opengamma.util.money.Currency.GBP;
import static com.opengamma.util.money.Currency.USD;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;

/**
 * Sets of market data used in Forex tests.
 */
public class FXDataSets {
  private static final Currency KRW = Currency.of("KRW");
  private static final String DISCOUNTING_EUR = "Discounting EUR";
  private static final String DISCOUNTING_USD = "Discounting USD";
  private static final String DISCOUNTING_GBP = "Discounting GBP";
  private static final String DISCOUNTING_KRW = "Discounting KRW";
  private static final double EUR_USD = 1.40;
  private static final double KRW_USD = 1111.11;
  private static final double GBP_USD = 0.6;
  private static final FXMatrix FX_MATRIX;

  static {
    FX_MATRIX = new FXMatrix(EUR, USD, EUR_USD);
    FX_MATRIX.addCurrency(KRW, USD, KRW_USD);
    FX_MATRIX.addCurrency(GBP, USD, GBP_USD);
  }

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] USD_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0 };
  private static final double[] USD_DSC_RATE = new double[] {0.0100, 0.0120, 0.0120, 0.0140, 0.0140 };
  private static final String USD_DSC_NAME = "USD Dsc";
  private static final YieldAndDiscountCurve USD_DSC = new YieldCurve(USD_DSC_NAME, new InterpolatedDoublesCurve(USD_DSC_TIME, USD_DSC_RATE, LINEAR_FLAT, true, USD_DSC_NAME));

  private static final double[] EUR_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0 };
  private static final double[] EUR_DSC_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150 };
  private static final String EUR_DSC_NAME = "EUR Dsc";
  private static final YieldAndDiscountCurve EUR_DSC = new YieldCurve(EUR_DSC_NAME, new InterpolatedDoublesCurve(EUR_DSC_TIME, EUR_DSC_RATE, LINEAR_FLAT, true, EUR_DSC_NAME));

  private static final double[] GBP_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0 };
  private static final double[] GBP_DSC_RATE = new double[] {0.0160, 0.0135, 0.0160, 0.0185, 0.0160 };
  private static final String GBP_DSC_NAME = "GBP Dsc";
  private static final YieldAndDiscountCurve GBP_DSC = new YieldCurve(GBP_DSC_NAME, new InterpolatedDoublesCurve(GBP_DSC_TIME, GBP_DSC_RATE, LINEAR_FLAT, true, GBP_DSC_NAME));

  private static final double[] KRW_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0 };
  private static final double[] KRW_DSC_RATE = new double[] {0.0350, 0.0325, 0.0350, 0.0375, 0.0350 };
  private static final String KRW_DSC_NAME = "KRW Dsc";
  private static final YieldAndDiscountCurve KRW_DSC = new YieldCurve(KRW_DSC_NAME, new InterpolatedDoublesCurve(KRW_DSC_TIME, KRW_DSC_RATE, LINEAR_FLAT, true, KRW_DSC_NAME));

  public static MulticurveProviderDiscount createCurvesForex() {
    final YieldAndDiscountCurve CURVE_EUR = EUR_DSC; // YieldCurve.from(ConstantDoublesCurve.from(0.0250));
    final YieldAndDiscountCurve CURVE_USD = USD_DSC; // YieldCurve.from(ConstantDoublesCurve.from(0.0100));
    final YieldAndDiscountCurve CURVE_GBP = GBP_DSC; // YieldCurve.from(ConstantDoublesCurve.from(0.0200));
    final YieldAndDiscountCurve CURVE_KRW = KRW_DSC; // YieldCurve.from(ConstantDoublesCurve.from(0.0321));
    final MulticurveProviderDiscount curves = new MulticurveProviderDiscount(FX_MATRIX);
    curves.setCurve(Currency.EUR, CURVE_EUR);
    curves.setCurve(Currency.USD, CURVE_USD);
    curves.setCurve(Currency.GBP, CURVE_GBP);
    curves.setCurve(Currency.of("KRW"), CURVE_KRW);
    return curves;
  }

  public static MulticurveProviderDiscount createCurvesForex(final Currency ccy1, final Currency ccy2, final double exchangeRate) {
    final YieldAndDiscountCurve curve1 = YieldCurve.from(ConstantDoublesCurve.from(0.0100));
    final YieldAndDiscountCurve curve2 = YieldCurve.from(ConstantDoublesCurve.from(0.0200));
    final FXMatrix fxMatrix = new FXMatrix(ccy1, ccy2, exchangeRate);
    final MulticurveProviderDiscount curves = new MulticurveProviderDiscount(fxMatrix);
    curves.setCurve(ccy1, curve1);
    curves.setCurve(ccy2, curve2);
    return curves;
  }

  /**
   * Create a yield curve bundle with three curves. One called "Discounting EUR" with a constant rate of 2.50%, one called "Discounting USD" with a constant rate of 1.00%
   * and one called "Discounting GBP" with a constant rate of 2.00%; "Discounting KRW" with a constant rate of 3.21%;
   * @return The yield curve bundle.
   */
  public static MulticurveProviderDiscount createCurvesForex2() {
    final YieldAndDiscountCurve CURVE_EUR = YieldCurve.from(ConstantDoublesCurve.from(0.0250));
    final YieldAndDiscountCurve CURVE_USD = YieldCurve.from(ConstantDoublesCurve.from(0.0100));
    final YieldAndDiscountCurve CURVE_GBP = YieldCurve.from(ConstantDoublesCurve.from(0.0200));
    final YieldAndDiscountCurve CURVE_KRW = YieldCurve.from(ConstantDoublesCurve.from(0.0321));
    final MulticurveProviderDiscount curves = new MulticurveProviderDiscount(FX_MATRIX);
    curves.setCurve(Currency.EUR, CURVE_EUR);
    curves.setCurve(Currency.USD, CURVE_USD);
    curves.setCurve(Currency.GBP, CURVE_GBP);
    curves.setCurve(Currency.of("KRW"), CURVE_KRW);
    return curves;
  }

  public static String[] curveNames() {
    return new String[] {DISCOUNTING_EUR, DISCOUNTING_USD, DISCOUNTING_GBP, DISCOUNTING_KRW };
  }

  public static Map<String, Currency> curveCurrency() {
    final Map<String, Currency> map = new HashMap<>();
    map.put(DISCOUNTING_EUR, EUR);
    map.put(DISCOUNTING_USD, USD);
    map.put(DISCOUNTING_GBP, Currency.GBP);
    map.put(DISCOUNTING_KRW, Currency.of("KRW"));
    return map;
  }

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  //  private static final int SETTLEMENT_DAYS = 2;
  private static final Period[] EXPIRY_PERIOD = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(5) };
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final double[] ATM = {0.185, 0.18, 0.17, 0.16, 0.16 };

  private static final double[] DELTA_2 = new double[] {0.10, 0.25 };
  private static final double[][] RISK_REVERSAL_2 = new double[][] { {-0.011, -0.0060 }, {-0.012, -0.0070 }, {-0.013, -0.0080 }, {-0.014, -0.0090 }, {-0.014, -0.0090 } };
  private static final double[][] STRANGLE_2 = new double[][] { {0.0310, 0.0110 }, {0.0320, 0.0120 }, {0.0330, 0.0130 }, {0.0340, 0.0140 }, {0.0340, 0.0140 } };

  private static final double[] DELTA_1 = new double[] {0.25 };
  private static final double[][] RISK_REVERSAL_1 = new double[][] { {-0.0060 }, {-0.0070 }, {-0.0080 }, {-0.0090 }, {-0.0090 } };
  private static final double[][] STRANGLE_1 = new double[][] { {0.0110 }, {0.0120 }, {0.0130 }, {0.0140 }, {0.0140 } };

  private static final double[][] RISK_REVERSAL_FLAT = new double[][] { {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 } };
  private static final double[][] STRANGLE_FLAT = new double[][] { {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 } };

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

  public static SmileDeltaTermStructureParametersStrikeInterpolation smileFlat(final ZonedDateTime referenceDate, final double volatility) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    final double[] atmFlat = new double[ATM.length];
    Arrays.fill(atmFlat, volatility);
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, atmFlat, RISK_REVERSAL_FLAT, STRANGLE_FLAT);
  }

  public static FXMatrix fxMatrix() {
    return FX_MATRIX;
  }

}
