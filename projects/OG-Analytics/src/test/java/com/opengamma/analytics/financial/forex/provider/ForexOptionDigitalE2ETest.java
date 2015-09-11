package com.opengamma.analytics.financial.forex.provider;

import static com.opengamma.util.money.Currency.AUD;
import static com.opengamma.util.money.Currency.EUR;
import static com.opengamma.util.money.Currency.NZD;
import static com.opengamma.util.money.Currency.USD;
import static org.testng.Assert.assertEquals;
import static org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH;
import static org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR;
import static org.threeten.bp.temporal.ChronoField.YEAR;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueCurveSensitivityForexStaticReplicationSmileCalculator;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pairs;

/**
 * End-to-end test for FX digital options.
 */
@Test
@SuppressWarnings("unused")
public class ForexOptionDigitalE2ETest {
  // interpolator for yield curve
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  // interpolator for forward curve, which is used to estimate CNH discount curve
  private static final Interpolator1D CUBIC_FLAT_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.NATURAL_CUBIC_MONOTONE,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final DayCount DAY_COUNT = DayCounts.ACT_365;
  private static final Calendar CALENDAR = new CalendarUSD("USD calendar");
  protected static final BusinessDayConvention MOD_FOLLOWING = BusinessDayConventions.MODIFIED_FOLLOWING;

  /**
   * Spot and forward data.
   */
  private static final LocalDate VALUATION_DATE = LocalDate.of(2015, 8, 6);
  private static final ZonedDateTime VALUATION_DATETIME =
      ZonedDateTime.of(VALUATION_DATE, LocalTime.of(13, 11), ZoneId.of("GMT-4"));
  private static final double SPOT_NZDUSD = 0.6538;
  private static final double SPOT_USDCNH = 6.2191;
  private static final double SPOT_AUDUSD = 0.73805;
  private static final double SPOT_EURUSD = 1.08815;
  private static final double[] FORWARDS_USDCNH = new double[] {6.23396, 6.2481, 6.2606, 6.3101, 6.34985, 6.3991,
    6.5591 }; // Forward used to compute the discounting curve in CNH
  private static final DateTimeFormatter DDMMYYYY = new DateTimeFormatterBuilder().appendValue(DAY_OF_MONTH, 2)
      .appendLiteral("/").appendValue(MONTH_OF_YEAR, 2).appendLiteral("/").appendValue(YEAR, 4).toFormatter();
  private static final LocalDate[] DATES_USDCNH = new LocalDate[] {LocalDate.parse("08/09/2015", DDMMYYYY),
    LocalDate.parse("06/10/2015", DDMMYYYY), LocalDate.parse("06/11/2015", DDMMYYYY),
    LocalDate.parse("16/02/2016", DDMMYYYY), LocalDate.parse("06/05/2016", DDMMYYYY),
    LocalDate.parse("08/08/2016", DDMMYYYY), LocalDate.parse("07/08/2017", DDMMYYYY) };
  private static final Currency CNH = Currency.of("CNH");
  private static final FXMatrix FX_MATRIX;
  static {
    FX_MATRIX = new FXMatrix(EUR, USD, SPOT_EURUSD);
    FX_MATRIX.addCurrency(NZD, USD, SPOT_NZDUSD);
    FX_MATRIX.addCurrency(CNH, USD, 1d / SPOT_USDCNH);
    FX_MATRIX.addCurrency(AUD, USD, SPOT_AUDUSD);
  }
  private static final MulticurveProviderDiscount MULTICURVE = new MulticurveProviderDiscount(FX_MATRIX);

  /**
   * Discount curve data. 
   * CNH curve is estimated from USDCNH forward rates and USD discount curve. 
   */
  private static final String USD_DSC_NAME = "USD Dsc";
  private static final String AUD_DSC_NAME = "AUD Dsc";
  private static final String NZD_DSC_NAME = "NZD Dsc";
  private static final String EUR_DSC_NAME = "EUR Dsc";
  private static final String CNH_DSC_NAME = "CNH Dsc";
  private static final double[] RATES_USD = new double[] {0.00126, 0.001505, 0.001915, 0.0025375, 0.003114, 0.003947,
    0.005536, 0.007271, 0.0092, 0.011275, 0.013347, 0.015062, 0.016723, 0.0128225, 0.0154065, 0.017465, 0.019145,
    0.020495, 0.0215865, 0.022466, 0.023212 }; // Zero coupon rates
  private static final double[] RATES_AUD = new double[] {0.0205, 0.0211, 0.0215, 0.0226, 0.02241, 0.02215, 0.02198,
    0.02247, 0.02275, 0.0237, 0.02495, 0.026463, 0.027913, 0.0291065, 0.030225, 0.031125, 0.031945 };
  private static final double[] RATES_NZD = new double[] {0.03, 0.031, 0.0306, 0.0302, 0.028896, 0.028083, 0.027764,
    0.027939, 0.028723, 0.02895, 0.029725, 0.03075, 0.032, 0.0344, 0.0371 };
  private static final double[] RATES_EUR = new double[] {-0.0018, -0.00133, -0.0008, 0.00047, 0.00046, 0.00047,
    0.0004, 0.000545, 0.00095, 0.00178, 0.002905, 0.004195, 0.00557, 0.00695, 0.008255, 0.00945, 0.010485 };
  private static final LocalDate[] DATES_USD = new LocalDate[] {DateUtils.toLocalDate("20150807"),
    DateUtils.toLocalDate("20150817"), DateUtils.toLocalDate("20150910"), DateUtils.toLocalDate("20151013"),
    DateUtils.toLocalDate("20151110"), DateUtils.toLocalDate("20151216"), DateUtils.toLocalDate("20160316"),
    DateUtils.toLocalDate("20160615"), DateUtils.toLocalDate("20160921"), DateUtils.toLocalDate("20161221"),
    DateUtils.toLocalDate("20170315"), DateUtils.toLocalDate("20170621"), DateUtils.toLocalDate("20170920"),
    DateUtils.toLocalDate("20180810"), DateUtils.toLocalDate("20190810"), DateUtils.toLocalDate("20200810"),
    DateUtils.toLocalDate("20210810"), DateUtils.toLocalDate("20220810"), DateUtils.toLocalDate("20230810"),
    DateUtils.toLocalDate("20240810"), DateUtils.toLocalDate("20250810") };
  private static final LocalDate[] DATES_AUD = new LocalDate[] {DateUtils.toLocalDate("20150907"),
    DateUtils.toLocalDate("20151007"), DateUtils.toLocalDate("20151109"), DateUtils.toLocalDate("20160208"),
    DateUtils.toLocalDate("20160307"), DateUtils.toLocalDate("20160509"), DateUtils.toLocalDate("20160808"),
    DateUtils.toLocalDate("20170207"), DateUtils.toLocalDate("20170807"), DateUtils.toLocalDate("20180807"),
    DateUtils.toLocalDate("20190807"), DateUtils.toLocalDate("20200807"), DateUtils.toLocalDate("20210807"),
    DateUtils.toLocalDate("20220807"), DateUtils.toLocalDate("20230807"), DateUtils.toLocalDate("20240807"),
    DateUtils.toLocalDate("20250807") };
  private static final LocalDate[] DATES_NZD = new LocalDate[] {DateUtils.toLocalDate("20150807"),
    DateUtils.toLocalDate("20150910"), DateUtils.toLocalDate("20151012"), DateUtils.toLocalDate("20151110"),
    DateUtils.toLocalDate("20151218"), DateUtils.toLocalDate("20160318"), DateUtils.toLocalDate("20160617"),
    DateUtils.toLocalDate("20160916"), DateUtils.toLocalDate("20160810"), DateUtils.toLocalDate("20170810"),
    DateUtils.toLocalDate("20180810"), DateUtils.toLocalDate("20190810"), DateUtils.toLocalDate("20200810"),
    DateUtils.toLocalDate("20220810"), DateUtils.toLocalDate("20250810") };
  private static final LocalDate[] DATES_EUR = new LocalDate[] {DateUtils.toLocalDate("20150807"),
    DateUtils.toLocalDate("20150817"), DateUtils.toLocalDate("20150910"), DateUtils.toLocalDate("20160210"),
    DateUtils.toLocalDate("20160310"), DateUtils.toLocalDate("20160411"), DateUtils.toLocalDate("20160510"),
    DateUtils.toLocalDate("20160810"), DateUtils.toLocalDate("20170810"), DateUtils.toLocalDate("20180810"),
    DateUtils.toLocalDate("20190810"), DateUtils.toLocalDate("20200810"), DateUtils.toLocalDate("20210810"),
    DateUtils.toLocalDate("20220810"), DateUtils.toLocalDate("20230810"), DateUtils.toLocalDate("20240810"),
    DateUtils.toLocalDate("20250810") };
  private static final double[] RATES_CNH;
  static {
    // forward curve of USD/CNH, used for estimating CNH discount curve. 
    double[] timeFwd = new double[FORWARDS_USDCNH.length + 1];
    double[] fwd = new double[FORWARDS_USDCNH.length + 1];
    for (int i = 0; i < FORWARDS_USDCNH.length; ++i) {
      timeFwd[i + 1] = DAY_COUNT.getDayCountFraction(VALUATION_DATE, DATES_USDCNH[i]);
      fwd[i + 1] = FORWARDS_USDCNH[i];
    }
    fwd[0] = SPOT_USDCNH;
    Interpolator1DDataBundle bundle = CUBIC_FLAT_LINEAR.getDataBundle(timeFwd, fwd);
    // discount curves
    RATES_CNH = new double[RATES_USD.length];
    String[] names = new String[] {USD_DSC_NAME, AUD_DSC_NAME, NZD_DSC_NAME, EUR_DSC_NAME };
    double[][] rates = new double[][] {RATES_USD, RATES_AUD, RATES_NZD, RATES_EUR };
    LocalDate[][] dates = new LocalDate[][] {DATES_USD, DATES_AUD, DATES_NZD, DATES_EUR };
    YieldCurve[] curves = new YieldCurve[5];
    for (int i = 0; i < 4; ++i) {
      int n = rates[i].length;
      double[] times = new double[n];
      for (int j = 0; j < n; ++j) {
        times[j] = TimeCalculator.getTimeBetween(VALUATION_DATE, dates[i][j]);
        if (i == 0) {
          RATES_CNH[j] = RATES_USD[j]
              + Math.log(CUBIC_FLAT_LINEAR.interpolate(bundle, times[j]) / SPOT_USDCNH) / times[j];
        }
      }
      curves[i] = new YieldCurve(names[i], new InterpolatedDoublesCurve(times, rates[i], LINEAR_FLAT, true, names[i]));
      if (i == 0) {
        curves[4] = new YieldCurve(
            CNH_DSC_NAME, new InterpolatedDoublesCurve(times, RATES_CNH, LINEAR_FLAT, true, CNH_DSC_NAME));
      }
    }
    MULTICURVE.setCurve(USD, curves[0]);
    MULTICURVE.setCurve(AUD, curves[1]);
    MULTICURVE.setCurve(NZD, curves[2]);
    MULTICURVE.setCurve(EUR, curves[3]);
    MULTICURVE.setCurve(CNH, curves[4]);
  }

  /**
   * Vol surface data
   */
  private static final double[] DELTAS = new double[] {0.05, 0.1, 0.25 };
  private static final double[][] VOLS_NZDUSD = new double[][] { {20.28, 19.77, 19.14, 18.69, 18.62, 18.83, 19.07 },
    {15.99, 15.45, 14.79, 14.3, 14.19, 14.36, 14.57 }, {14.72, 14.14, 13.43, 12.88, 12.72, 12.86, 13.05 },
    {14.76, 14.15, 13.4, 12.8, 12.6, 12.69, 12.85 }, {14.92, 14.28, 13.47, 12.8, 12.52, 12.55, 12.67 },
    {15.77, 14.9, 13.83, 12.98, 12.63, 12.7, 12.89 }, {16.17, 15.05, 13.75, 12.78, 12.4, 12.57, 12.89 },
    {16.37, 15.1, 13.67, 12.62, 12.23, 12.46, 12.85 }, {16.64, 15.21, 13.64, 12.51, 12.09, 12.39, 12.87 },
    {16.98, 15.38, 13.66, 12.45, 12.01, 12.38, 12.96 }, {17.52, 15.68, 13.71, 12.32, 11.81, 12.21, 12.85 },
    {17.8, 15.86, 13.79, 12.35, 11.79, 12.19, 12.86 }, {18.71, 16.37, 13.99, 12.5, 11.94, 12.46, 13.33 },
    {18.99, 16.55, 14.11, 12.58, 12.01, 12.56, 13.46 }, {20.11, 17.28, 14.5, 12.88, 12.25, 12.84, 13.86 },
    {20.94, 17.8, 14.84, 13.12, 12.46, 13.2, 14.45 }, {21.71, 18.33, 15.21, 13.42, 12.71, 13.56, 14.98 },
    {21.75, 18.77, 15.48, 13.5, 12.73, 12.84, 13.4 }, {22.56, 19.38, 15.77, 13.55, 12.65, 12.69, 13.22 } };
  private static final double[][] VOLS_USDCNH = new double[][] { {3.01, 2.12, 1.23, 1.5, 2.37, 3.99, 5.1 },
    {2.96, 2.07, 1.22, 1.55, 2.48, 4.09, 5.27 }, {2.96, 2.06, 1.26, 1.65, 2.64, 4.28, 5.55 },
    {2.86, 1.98, 1.23, 1.69, 2.74, 4.36, 5.69 }, {2.74, 1.88, 1.16, 1.7, 2.86, 4.45, 5.82 },
    {3.01, 2.11, 1.38, 2, 3.28, 5.08, 6.64 }, {3.42, 2.47, 1.78, 2.45, 3.83, 5.89, 7.65 },
    {4.03, 3.01, 2.29, 2.98, 4.43, 6.76, 8.72 }, {4.32, 3.28, 2.57, 3.29, 4.8, 7.29, 9.36 },
    {4.5, 3.45, 2.75, 3.5, 5.1, 7.73, 9.89 }, {5.04, 3.89, 3.15, 4, 5.8, 8.72, 11.08 },
    {5.46, 4.24, 3.46, 4.45, 6.51, 9.75, 12.29 }, {6.64, 5.28, 4.44, 5.47, 7.71, 11.39, 14.27 },
    {7.22, 5.78, 4.88, 5.95, 8.38, 12.42, 15.53 } };
  private static final double[][] VOLS_AUDUSD = new double[][] { {14.04, 13.6, 13.06, 12.66, 12.67, 12.89, 13.12 },
    {13.98, 13.52, 12.93, 12.48, 12.42, 12.6, 12.79 }, {13.39, 12.87, 12.21, 11.69, 11.58, 11.72, 11.88 },
    {13.6, 13.04, 12.32, 11.73, 11.56, 11.65, 11.78 }, {13.91, 13.29, 12.49, 11.8, 11.54, 11.56, 11.64 },
    {14.1, 13.41, 12.5, 11.7, 11.34, 11.28, 11.31 }, {14.99, 13.97, 12.72, 11.75, 11.42, 11.54, 11.77 },
    {15.19, 14.08, 12.74, 11.7, 11.33, 11.47, 11.73 }, {15.43, 14.22, 12.79, 11.66, 11.25, 11.41, 11.71 },
    {15.69, 14.39, 12.85, 11.65, 11.2, 11.39, 11.72 }, {16.37, 14.8, 13.01, 11.65, 11.16, 11.4, 11.82 },
    {17.03, 15.21, 13.16, 11.68, 11.16, 11.48, 12.01 }, {17.95, 15.73, 13.39, 11.84, 11.31, 11.78, 12.54 },
    {18.38, 15.97, 13.53, 11.92, 11.38, 11.93, 12.8 }, {19.38, 16.62, 13.9, 12.23, 11.6, 12.19, 13.2 },
    {20.08, 17.15, 14.3, 12.58, 11.9, 12.52, 13.6 }, {20.9, 17.78, 14.72, 12.88, 12.07, 12.66, 13.76 },
    {21.59, 18.48, 15.08, 12.96, 11.93, 12.1, 12.81 }, {23.22, 19.6, 15.52, 13.02, 11.63, 11.68, 12.43 } };
  private static final double[][] VOLS_EURUSD = new double[][] { {12.11, 11.74, 11.29, 10.99, 11.09, 11.39, 11.68 },
    {12.19, 11.8, 11.33, 11, 11.07, 11.37, 11.65 }, {11.72, 11.3, 10.8, 10.44, 10.49, 10.77, 11.04 },
    {11.81, 11.37, 10.84, 10.45, 10.48, 10.74, 11 }, {11.98, 11.51, 10.94, 10.5, 10.49, 10.74, 10.99 },
    {12.36, 11.76, 11.04, 10.45, 10.34, 10.55, 10.79 }, {12.42, 11.71, 10.87, 10.2, 10.02, 10.23, 10.49 },
    {12.6, 11.8, 10.86, 10.12, 9.9, 10.11, 10.39 }, {12.85, 11.93, 10.88, 10.07, 9.79, 10, 10.31 },
    {13.13, 12.09, 10.93, 10.05, 9.73, 9.94, 10.27 }, {13.27, 12.17, 10.91, 9.95, 9.56, 9.71, 10 },
    {13.51, 12.36, 11, 9.95, 9.5, 9.58, 9.82 }, {13.41, 12.21, 10.85, 9.83, 9.4, 9.53, 9.84 },
    {13.39, 12.13, 10.76, 9.77, 9.36, 9.55, 9.92 }, {13.31, 12.07, 10.75, 9.85, 9.5, 9.76, 10.2 },
    {13.08, 11.9, 10.67, 9.85, 9.57, 9.87, 10.34 }, {12.96, 11.84, 10.67, 9.9, 9.67, 9.99, 10.47 },
    {11.94, 11.37, 10.59, 9.95, 9.84, 9.92, 10.01 }, {11.76, 11.19, 10.51, 10.05, 10.11, 10.39, 10.67 } };
  private static final Period[] PERIODS_NZDUSD = new Period[] {Period.ofDays(1), Period.ofDays(7), Period.ofDays(14),
    Period.ofDays(21), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(4),
    Period.ofMonths(5), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofMonths(18),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
  private static final Period[] PERIODS_USDCNH = new Period[] {Period.ofDays(1), Period.ofDays(7), Period.ofDays(14),
    Period.ofDays(21), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(4),
    Period.ofMonths(5), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofMonths(18),
    Period.ofYears(2) };
  private static final Period[] PERIODS_AUDUSD = new Period[] {Period.ofDays(1), Period.ofDays(7), Period.ofDays(14),
    Period.ofDays(21), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(4),
    Period.ofMonths(5), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofMonths(18),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
  private static final Period[] PERIODS_EURUSD = new Period[] {Period.ofDays(1), Period.ofDays(7), Period.ofDays(14),
    Period.ofDays(21), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(4),
    Period.ofMonths(5), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofMonths(18),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
  private static final SmileDeltaTermStructureParametersStrikeInterpolation VOL_SURFACE_NZDUSD;
  private static final SmileDeltaTermStructureParametersStrikeInterpolation VOL_SURFACE_USDCNH;
  private static final SmileDeltaTermStructureParametersStrikeInterpolation VOL_SURFACE_AUDUSD;
  private static final SmileDeltaTermStructureParametersStrikeInterpolation VOL_SURFACE_EURUSD;
  static {
    double[][][] vols = new double[][][] {VOLS_NZDUSD, VOLS_USDCNH, VOLS_AUDUSD, VOLS_EURUSD };
    Period[][] periods = new Period[][] {PERIODS_NZDUSD, PERIODS_USDCNH, PERIODS_AUDUSD, PERIODS_EURUSD };
    SmileDeltaParameters[][] term = new SmileDeltaParameters[4][];
    for (int i = 0; i < 4; ++i) {
      int n = periods[i].length;
      term[i] = new SmileDeltaParameters[n];
      for (int j = 0; j < n; ++j) {
        double time = DAY_COUNT.getDayCountFraction(
            VALUATION_DATE, MOD_FOLLOWING.adjustDate(CALENDAR, VALUATION_DATE.plus(periods[i][j])));
        for (int k = 0; k < vols[i][j].length; ++k) {
          vols[i][j][k] *= 0.01;
        }
        term[i][j] = new SmileDeltaParameters(time, DELTAS, vols[i][j]);
      }
    }
    VOL_SURFACE_NZDUSD = new SmileDeltaTermStructureParametersStrikeInterpolation(term[0]);
    VOL_SURFACE_USDCNH = new SmileDeltaTermStructureParametersStrikeInterpolation(term[1]);
    VOL_SURFACE_AUDUSD = new SmileDeltaTermStructureParametersStrikeInterpolation(term[2]);
    VOL_SURFACE_EURUSD = new SmileDeltaTermStructureParametersStrikeInterpolation(term[3]);
  }

  private static final BlackForexSmileProviderDiscount PROVIDER_NZDUSD =
      new BlackForexSmileProviderDiscount(MULTICURVE, VOL_SURFACE_NZDUSD, Pairs.of(NZD, USD));
  private static final BlackForexSmileProviderDiscount PROVIDER_USDCNH =
      new BlackForexSmileProviderDiscount(MULTICURVE, VOL_SURFACE_USDCNH, Pairs.of(USD, CNH));
  private static final BlackForexSmileProviderDiscount PROVIDER_AUDUSD =
      new BlackForexSmileProviderDiscount(MULTICURVE, VOL_SURFACE_AUDUSD, Pairs.of(AUD, USD));
  private static final BlackForexSmileProviderDiscount PROVIDER_EURUSD =
      new BlackForexSmileProviderDiscount(MULTICURVE, VOL_SURFACE_EURUSD, Pairs.of(EUR, USD));

  /**
   * Instruments.
   */
  private static final double STRIKE_AUDUSD = 0.72;
  private static final double STRIKE_EURUSD = 1.05;
  private static final double STRIKE_NZDUSD = 0.6;
  private static final double STRIKE_USDCNH = 6.2165;
  private static final double SETTLE_CURRENCY_NOTIONAL = 1.0e6;
  // put on AUD/USD, settle in USD
  private static final ZonedDateTime EXPIRY_AUDUSD = ZonedDateTime.of(2015, 11, 10, 10, 0, 0, 0, ZoneId.of("GMT+9"));
  private static final ZonedDateTime SETTLE_AUDUSD = MOD_FOLLOWING.adjustDate(CALENDAR, EXPIRY_AUDUSD.plusDays(2));
  private static final ForexDefinition FX_AUDUSD =
      new ForexDefinition(AUD, USD, SETTLE_AUDUSD, SETTLE_CURRENCY_NOTIONAL / STRIKE_AUDUSD, STRIKE_AUDUSD);
  private static final ForexOptionDigitalDefinition DEFINITION_AUDUSD =
      new ForexOptionDigitalDefinition(FX_AUDUSD, EXPIRY_AUDUSD, false, true, true);
  private static final ForexOptionDigital DERIVATIVE_AUDUSD = DEFINITION_AUDUSD.toDerivative(VALUATION_DATETIME);
  //put on EUR/USD, settle in USD
  private static final ZonedDateTime EXPIRY_EURUSD = ZonedDateTime.of(2015, 8, 13, 10, 0, 0, 0, ZoneId.of("GMT-4"));
  private static final ZonedDateTime SETTLE_EURUSD = MOD_FOLLOWING.adjustDate(CALENDAR, EXPIRY_EURUSD.plusDays(2));
  private static final ForexDefinition FX_EURUSD =
      new ForexDefinition(EUR, USD, SETTLE_EURUSD, SETTLE_CURRENCY_NOTIONAL / STRIKE_EURUSD, STRIKE_EURUSD);
  private static final ForexOptionDigitalDefinition DEFINITION_EURUSD =
      new ForexOptionDigitalDefinition(FX_EURUSD, EXPIRY_EURUSD, false, true, true);
  private static final ForexOptionDigital DERIVATIVE_EURUSD = DEFINITION_EURUSD.toDerivative(VALUATION_DATETIME);
  //put on NZD/USD, settle in USD
  private static final ZonedDateTime EXPIRY_NZDUSD = ZonedDateTime.of(2015, 12, 17, 10, 0, 0, 0, ZoneId.of("GMT-4"));
  private static final ZonedDateTime SETTLE_NZDUSD = MOD_FOLLOWING.adjustDate(CALENDAR, EXPIRY_NZDUSD.plusDays(2));
  private static final ForexDefinition FX_NZDUSD =
      new ForexDefinition(NZD, USD, SETTLE_NZDUSD, SETTLE_CURRENCY_NOTIONAL / STRIKE_NZDUSD, STRIKE_NZDUSD);
  private static final ForexOptionDigitalDefinition DEFINITION_NZDUSD =
      new ForexOptionDigitalDefinition(FX_NZDUSD, EXPIRY_NZDUSD, false, true, true);
  private static final ForexOptionDigital DERIVATIVE_NZDUSD = DEFINITION_NZDUSD.toDerivative(VALUATION_DATETIME);
  //put on USD/CNH, settle in USD
  private static final ZonedDateTime EXPIRY_CNHUSD = ZonedDateTime.of(2015, 8, 27, 15, 0, 0, 0, ZoneId.of("GMT+9"));
  private static final ZonedDateTime SETTLE_CNHUSD = MOD_FOLLOWING.adjustDate(CALENDAR, EXPIRY_CNHUSD.plusDays(2));
  private static final ForexDefinition FX_CNHUSD =
      new ForexDefinition(USD, CNH, SETTLE_CNHUSD, SETTLE_CURRENCY_NOTIONAL, STRIKE_USDCNH);
  private static final ForexOptionDigitalDefinition DEFINITION_USDCNH =
      new ForexOptionDigitalDefinition(FX_CNHUSD, EXPIRY_CNHUSD, false, true, false);
  private static final ForexOptionDigital DERIVATIVE_USDCNH = DEFINITION_USDCNH.toDerivative(VALUATION_DATETIME);

  private static final ForexOptionDigitalCallSpreadBlackSmileMethod METHOD_SPREAD =
      new ForexOptionDigitalCallSpreadBlackSmileMethod();
  private static final PresentValueCurveSensitivityForexStaticReplicationSmileCalculator PVSC =
      PresentValueCurveSensitivityForexStaticReplicationSmileCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<BlackForexSmileProviderInterface> PSC = new
      ParameterSensitivityParameterCalculator<>(PVSC);

  private static final double TOL = 1.0e-8;
  private static final boolean PRINT = false;

  public void testAUDUSD() {
    MultipleCurrencyAmount pv = METHOD_SPREAD.presentValue(DERIVATIVE_AUDUSD, PROVIDER_AUDUSD);
    MultipleCurrencyAmount ce = METHOD_SPREAD.currencyExposure(DERIVATIVE_AUDUSD, PROVIDER_AUDUSD);
    CurrencyAmount delta = METHOD_SPREAD.delta(DERIVATIVE_AUDUSD, PROVIDER_AUDUSD);
    CurrencyAmount gamma = METHOD_SPREAD.gamma(DERIVATIVE_AUDUSD, PROVIDER_AUDUSD);
    MultipleCurrencyMulticurveSensitivity pointSensi =
        METHOD_SPREAD.presentValueCurveSensitivity(DERIVATIVE_AUDUSD, PROVIDER_AUDUSD);
    MultipleCurrencyParameterSensitivity sensi = PSC.pointToParameterSensitivity(pointSensi, PROVIDER_AUDUSD);
    PresentValueForexBlackVolatilitySensitivity vega =
        METHOD_SPREAD.presentValueBlackVolatilitySensitivity(DERIVATIVE_AUDUSD, PROVIDER_AUDUSD);
    PresentValueForexBlackVolatilityNodeSensitivityDataBundle volSensi =
        METHOD_SPREAD.presentValueBlackVolatilityNodeSensitivity(DERIVATIVE_AUDUSD, PROVIDER_AUDUSD);
    assertEquals(pv.getAmount(USD), 342743.8725, TOL * SETTLE_CURRENCY_NOTIONAL);
    assertEquals(delta.getAmount(), -8027937.8774, TOL * SETTLE_CURRENCY_NOTIONAL);
    assertEquals(gamma.getAmount(), 81601543.8955, TOL * SETTLE_CURRENCY_NOTIONAL);

    if (PRINT) {
      System.out.println("PV: " + pv);
      System.out.println("Currency exposure: " + ce);
      System.out.println("PV delta: " + delta);
      System.out.println("PV gamma: " + gamma);
      System.out.println("Bucketed PV01 (zero-rates): " + sensi);
      System.out.println("PV vega: " + vega.getVega());
      System.out.println("Bucketed PV vega:");
      System.out.println("  absolute delta: " + volSensi.getDelta());
      System.out.println("  time to expiry: " + volSensi.getExpiries());
      System.out.println("  " + volSensi.getVega());
    }
  }

  public void testEURUSD() {
    MultipleCurrencyAmount pv = METHOD_SPREAD.presentValue(DERIVATIVE_EURUSD, PROVIDER_EURUSD);
    MultipleCurrencyAmount ce = METHOD_SPREAD.currencyExposure(DERIVATIVE_EURUSD, PROVIDER_EURUSD);
    CurrencyAmount delta = METHOD_SPREAD.delta(DERIVATIVE_EURUSD, PROVIDER_EURUSD);
    CurrencyAmount gamma = METHOD_SPREAD.gamma(DERIVATIVE_EURUSD, PROVIDER_EURUSD);
    MultipleCurrencyMulticurveSensitivity pointSensi =
        METHOD_SPREAD.presentValueCurveSensitivity(DERIVATIVE_EURUSD, PROVIDER_EURUSD);
    MultipleCurrencyParameterSensitivity sensi = PSC.pointToParameterSensitivity(pointSensi, PROVIDER_EURUSD);
    PresentValueForexBlackVolatilitySensitivity vega =
        METHOD_SPREAD.presentValueBlackVolatilitySensitivity(DERIVATIVE_EURUSD, PROVIDER_EURUSD);
    PresentValueForexBlackVolatilityNodeSensitivityDataBundle volSensi =
        METHOD_SPREAD.presentValueBlackVolatilityNodeSensitivity(DERIVATIVE_EURUSD, PROVIDER_EURUSD);
    assertEquals(pv.getAmount(USD), 17397.77618436597, TOL * SETTLE_CURRENCY_NOTIONAL);
    assertEquals(delta.getAmount(), -2340882.319637686, TOL * SETTLE_CURRENCY_NOTIONAL);
    assertEquals(gamma.getAmount(), 2.7112557412446594E8, TOL * SETTLE_CURRENCY_NOTIONAL);

    boolean print = false;
    if (PRINT) {
      System.out.println("PV: " + pv);
      System.out.println("Currency exposure: " + ce);
      System.out.println("PV delta: " + delta);
      System.out.println("PV gamma: " + gamma);
      System.out.println("Bucketed PV01 (zero-rates): " + sensi);
      System.out.println("PV vega: " + vega.getVega());
      System.out.println("Bucketed PV vega:");
      System.out.println("  absolute delta: " + volSensi.getDelta());
      System.out.println("  time to expiry: " + volSensi.getExpiries());
      System.out.println("  " + volSensi.getVega());
    }
  }

  public void testNZDUSD() {
    MultipleCurrencyAmount pv = METHOD_SPREAD.presentValue(DERIVATIVE_NZDUSD, PROVIDER_NZDUSD);
    MultipleCurrencyAmount ce = METHOD_SPREAD.currencyExposure(DERIVATIVE_NZDUSD, PROVIDER_NZDUSD);
    CurrencyAmount delta = METHOD_SPREAD.delta(DERIVATIVE_NZDUSD, PROVIDER_NZDUSD);
    CurrencyAmount gamma = METHOD_SPREAD.gamma(DERIVATIVE_NZDUSD, PROVIDER_NZDUSD);
    MultipleCurrencyMulticurveSensitivity pointSensi =
        METHOD_SPREAD.presentValueCurveSensitivity(DERIVATIVE_NZDUSD, PROVIDER_NZDUSD);
    MultipleCurrencyParameterSensitivity sensi = PSC.pointToParameterSensitivity(pointSensi, PROVIDER_NZDUSD);
    PresentValueForexBlackVolatilitySensitivity vega =
        METHOD_SPREAD.presentValueBlackVolatilitySensitivity(DERIVATIVE_NZDUSD, PROVIDER_NZDUSD);
    PresentValueForexBlackVolatilityNodeSensitivityDataBundle volSensi =
        METHOD_SPREAD.presentValueBlackVolatilityNodeSensitivity(DERIVATIVE_NZDUSD, PROVIDER_NZDUSD);
    assertEquals(pv.getAmount(USD), 157169.3731, TOL * SETTLE_CURRENCY_NOTIONAL);
    assertEquals(delta.getAmount(), -4326631.7014, TOL * SETTLE_CURRENCY_NOTIONAL);
    assertEquals(gamma.getAmount(), 84674218.4853, TOL * SETTLE_CURRENCY_NOTIONAL);

    if (PRINT) {
      System.out.println("PV: " + pv);
      System.out.println("Currency exposure: " + ce);
      System.out.println("Bucketed PV01 (zero-rates): " + sensi);
      System.out.println("PV gamma: " + gamma);
      System.out.println("Bucketed PV01: " + sensi);
      System.out.println("PV vega: " + vega.getVega());
      System.out.println("Bucketed PV vega:");
      System.out.println("  absolute delta: " + volSensi.getDelta());
      System.out.println("  time to expiry: " + volSensi.getExpiries());
      System.out.println("  " + volSensi.getVega());
    }
  }

  public void testUSDCNH() {
    // spread is increased from the default (1.e-4) because of small CNH/USD rate.
    ForexOptionDigitalCallSpreadBlackSmileMethod method =
        new ForexOptionDigitalCallSpreadBlackSmileMethod(0.001);
    MultipleCurrencyAmount pv = method.presentValue(DERIVATIVE_USDCNH, PROVIDER_USDCNH);
    MultipleCurrencyAmount ce = method.currencyExposure(DERIVATIVE_USDCNH, PROVIDER_USDCNH);
    CurrencyAmount delta = method.delta(DERIVATIVE_USDCNH, PROVIDER_USDCNH);
    CurrencyAmount gamma = method.gamma(DERIVATIVE_USDCNH, PROVIDER_USDCNH);
    MultipleCurrencyMulticurveSensitivity pointSensi =
        method.presentValueCurveSensitivity(DERIVATIVE_USDCNH, PROVIDER_USDCNH);
    MultipleCurrencyParameterSensitivity sensi = PSC.pointToParameterSensitivity(pointSensi, PROVIDER_USDCNH);
    PresentValueForexBlackVolatilitySensitivity vega =
        method.presentValueBlackVolatilitySensitivity(DERIVATIVE_USDCNH, PROVIDER_USDCNH);
    PresentValueForexBlackVolatilityNodeSensitivityDataBundle volSensi =
        method.presentValueBlackVolatilityNodeSensitivity(DERIVATIVE_USDCNH, PROVIDER_USDCNH);
    assertEquals(pv.getAmount(USD), 247568.94182499617, TOL * SETTLE_CURRENCY_NOTIONAL);
    assertEquals(delta.getAmount(), -1.4636480454570957E7, TOL * SETTLE_CURRENCY_NOTIONAL);
    assertEquals(gamma.getAmount(), 4.626467186570426E8, TOL * SETTLE_CURRENCY_NOTIONAL);

    if (PRINT) {
      System.out.println("PV: " + pv);
      System.out.println("Currency exposure: " + ce);
      System.out.println("Bucketed PV01 (zero-rates): " + sensi);
      System.out.println("PV gamma: " + gamma);
      System.out.println("Bucketed PV01: " + sensi);
      System.out.println("PV vega: " + vega.getVega());
      System.out.println("Bucketed PV vega:");
      System.out.println("  absolute delta: " + volSensi.getDelta());
      System.out.println("  time to expiry: " + volSensi.getExpiries());
      System.out.println(volSensi.getVega());
    }
  }

  public void volSurfacePrintTest() {
    boolean print = false;
    if (print) {
      double n = 30.0;
      double tMax = 15.0;
      double[] spots = new double[] {STRIKE_AUDUSD, STRIKE_EURUSD, STRIKE_NZDUSD, STRIKE_USDCNH };
      Currency[] currencies = new Currency[] {AUD, EUR, NZD, CNH };
      BlackForexSmileProviderDiscount[] dscs =
          new BlackForexSmileProviderDiscount[] {PROVIDER_AUDUSD, PROVIDER_EURUSD, PROVIDER_NZDUSD, PROVIDER_USDCNH };
      for (int k = 0; k < 4; ++k) {
        System.out.println(currencies[k].toString() + "/" + USD.toString());
        double kMaxP = spots[k] * 0.8;
        for (int j = 0; j < n; ++j) {
          double strike = spots[k] * 0.6 + kMaxP / n * j;
          System.out.print("\t" + strike);
        }
        System.out.print("\n");
        for (int i = 0; i < n; ++i) {
          double time = 1d / 365. + tMax * i / n;
          double forward = spots[k] * MULTICURVE.getDiscountFactor(currencies[k], time)
              / MULTICURVE.getDiscountFactor(USD, time);
          System.out.print(time);
          for (int j = 0; j < n; ++j) {
            double strike = spots[k] * 0.6 + kMaxP / n * j;
            // Note that the USD/CNH is reversed. 
            System.out.print("\t" + dscs[k].getVolatility(currencies[k], USD, time, strike, forward));
          }
          System.out.print("\n");
        }
        System.out.println("\n");
      }
    }
  }
}
