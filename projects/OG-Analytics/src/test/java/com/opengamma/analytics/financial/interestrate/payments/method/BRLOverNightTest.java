/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class BRLOverNightTest {

  private static final ZonedDateTime[] DATES = new ZonedDateTime[] {
    DateUtils.getUTCDate(2013, 8, 16),
    DateUtils.getUTCDate(2013, 8, 19), DateUtils.getUTCDate(2013, 8, 20), DateUtils.getUTCDate(2013, 8, 21), DateUtils.getUTCDate(2013, 8, 22), DateUtils.getUTCDate(2013, 8, 23),
    DateUtils.getUTCDate(2013, 8, 26), DateUtils.getUTCDate(2013, 8, 27), DateUtils.getUTCDate(2013, 8, 28), DateUtils.getUTCDate(2013, 8, 29), DateUtils.getUTCDate(2013, 8, 30),
    DateUtils.getUTCDate(2013, 9, 2), DateUtils.getUTCDate(2013, 9, 3), DateUtils.getUTCDate(2013, 9, 4), DateUtils.getUTCDate(2013, 9, 5), DateUtils.getUTCDate(2013, 9, 6),
    DateUtils.getUTCDate(2013, 9, 9), DateUtils.getUTCDate(2013, 9, 10), DateUtils.getUTCDate(2013, 9, 11), DateUtils.getUTCDate(2013, 9, 12), DateUtils.getUTCDate(2013, 9, 13),
    DateUtils.getUTCDate(2013, 9, 16), DateUtils.getUTCDate(2013, 9, 17), DateUtils.getUTCDate(2013, 9, 18), DateUtils.getUTCDate(2013, 9, 19), DateUtils.getUTCDate(2013, 9, 20),
    DateUtils.getUTCDate(2013, 9, 23), DateUtils.getUTCDate(2013, 9, 24), DateUtils.getUTCDate(2013, 9, 25), DateUtils.getUTCDate(2013, 9, 26), DateUtils.getUTCDate(2013, 9, 27),
    DateUtils.getUTCDate(2013, 9, 30), DateUtils.getUTCDate(2013, 10, 1), DateUtils.getUTCDate(2013, 10, 2), DateUtils.getUTCDate(2013, 10, 3), DateUtils.getUTCDate(2013, 10, 4),
    DateUtils.getUTCDate(2013, 10, 7), DateUtils.getUTCDate(2013, 10, 8), DateUtils.getUTCDate(2013, 10, 9), DateUtils.getUTCDate(2013, 10, 10), DateUtils.getUTCDate(2013, 10, 11) };
  private static final double[] CDI = new double[] {
    0.0822,
    0.0822, 0.0822, 0.0822, 0.0822, 0.0822,
    0.0822, 0.0822, 0.0822, 0.0872, 0.0872,
    0.0872, 0.0872, 0.0872, 0.0872, 0.0872,
    0.0872, 0.0872, 0.0872, 0.0872, 0.0872,
    0.0872, 0.0872, 0.0872, 0.0871, 0.0870,
    0.0870, 0.0870, 0.0881, 0.0872, 0.0871,
    0.0871, 0.0871, 0.0870, 0.0870, 0.0883,
    0.0881, 0.0881, 0.0885, 0.0936, 0.0932
  };
  private static final ZonedDateTimeDoubleTimeSeries TS = ImmutableZonedDateTimeDoubleTimeSeries.of(DATES, CDI, ZoneOffset.UTC);
  private static final DayCount DC = DayCountFactory.INSTANCE.getDayCount("Business/252");
  private static final BusinessDayConvention BDC = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final Calendar CALENDAR = new MyCalendar();
  private static final IndexON INDEX = new IndexON("", Currency.BRL, DC, 0);
  private static final double FX = 0.46;
  private static final GeneratorSwapFixedCompoundedONCompounded GENERATOR = new GeneratorSwapFixedCompoundedONCompounded("", INDEX, DC, BDC, true, 0, 0, CALENDAR);
  private static final ZonedDateTime EFFECTIVE_DATE1 = DateUtils.getUTCDate(2013, 10, 4);
  private static final ZonedDateTime MATURITY_DATE1 = DateUtils.getUTCDate(2015, 1, 2);
  private static final double NOTIONAL1 = 53682248.01 + 329762380.66;
  private static final SwapFixedCompoundedONCompoundedDefinition SWAP1 = SwapFixedCompoundedONCompoundedDefinition.from(EFFECTIVE_DATE1, MATURITY_DATE1, NOTIONAL1, GENERATOR, 0.1011, true);
  private static final ZonedDateTime EFFECTIVE_DATE2 = DateUtils.getUTCDate(2013, 8, 16);
  private static final ZonedDateTime MATURITY_DATE2 = DateUtils.getUTCDate(2016, 1, 4);
  private static final double NOTIONAL2 = 7651967.4728 + 14757365.84 + 90652390.16 + 47004943.0472;
  private static final SwapFixedCompoundedONCompoundedDefinition SWAP2 = SwapFixedCompoundedONCompoundedDefinition.from(EFFECTIVE_DATE2, MATURITY_DATE2, NOTIONAL2, GENERATOR, 0.1097, false);
  private static final ZonedDateTime EFFECTIVE_DATE3 = DateUtils.getUTCDate(2015, 1, 2);
  private static final ZonedDateTime MATURITY_DATE3 = DateUtils.getUTCDate(2016, 1, 4);
  private static final double NOTIONAL3 = 43984170.9062 + 270188478.4238 + 246257498.897 + 40088430.053;
  private static final SwapFixedCompoundedONCompoundedDefinition SWAP3 = SwapFixedCompoundedONCompoundedDefinition.from(EFFECTIVE_DATE3, MATURITY_DATE3, NOTIONAL3, GENERATOR, 0.115, false);
  private static final ZonedDateTime EFFECTIVE_DATE4 = DateUtils.getUTCDate(2013, 10, 10);
  private static final ZonedDateTime MATURITY_DATE4 = DateUtils.getUTCDate(2016, 1, 4);
  private static final double NOTIONAL4 = 97154115.4534 + 15815786.2366;
  private static final SwapFixedCompoundedONCompoundedDefinition SWAP4 = SwapFixedCompoundedONCompoundedDefinition.from(EFFECTIVE_DATE4, MATURITY_DATE4, NOTIONAL4, GENERATOR, 0.1084, false);
  private static final double[] T = new double[] {0.0575, 0.1425, 0.2274, 0.3151, 0.4712, 0.7205, 0.9726, 1.2274, 1.4712, 1.7205, 1.9726, 2.2329, 2.4733, 2.7219, 2.9788, 3.2274, 3.4767, 3.7260,
    3.9753, 4.2274 };
  private static final double[] TBUS252 = new double[] {0.0595, 0.1389, 0.2222, 0.3095, 0.4643, 0.7063, 0.9682, 1.2262, 1.4683, 1.7103, 1.9683, 2.2183, 2.4603, 2.7103, 2.9683, 3.2143, 3.4643, 3.7063,
    3.9603, 4.2024 };
  private static final double[] YIELD = new double[] {0.089338, 0.089585, 0.091088, 0.092346, 0.093940, 0.095895, 0.097620, 0.098345, 0.100155, 0.102008, 0.103045, 0.104037, 0.104847, 0.105567,
    0.105792, 0.106017, 0.106287, 0.106781, 0.107185, 0.107634 };
  private static final double[] T2 = new double[] {0.0595, 0.1389, 0.2222, 0.3095, 0.4643, 0.7063, 0.9682, 1.2262, 1.4683, 1.7103, 1.9683, 2.2183, 2.4603, 2.7103, 2.9683, 3.2143, 3.4643, 3.7063,
    3.9603, 4.2024 };
  private static final double[] YIELD2 = new double[] {0.093450, 0.093720, 0.095365, 0.096745, 0.098494, 0.100644, 0.102543, 0.103343, 0.105343, 0.107392, 0.108542, 0.109641, 0.110541, 0.111341,
    0.111591, 0.111841, 0.112141, 0.112690, 0.113140, 0.113640 };
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final YieldAndDiscountCurve CURVE;
  private static final YieldCurveBundle BUNDLE = new YieldCurveBundle();
  private static final String CURVE_NAME = "Curve";
  private static final double EXPECTED1 = 275279;
  private static final double EXPECTED2 = 187142;
  private static final double EXPECTED3 = -189555;
  private static final double EXPECTED4 = -5195;
  private static final double OG1 = -653316;
  private static final double OG2 = 1124980;
  private static final double OG3 = 891140;
  private static final double OG4 = 478887;

  static {
    final double[] yield = new double[YIELD.length];
    for (int i = 0; i < YIELD.length; i++) {
      yield[i] = YIELD[i] + 8. / 10000;
      System.out.println(YIELD2[i] - YIELD[i] * TBUS252[i] / T[i]);
    }
    CURVE = new YieldCurve("", InterpolatedDoublesCurve.from(T, yield, INTERPOLATOR));
    BUNDLE.setCurve(CURVE_NAME, CURVE);
  }

  @Test
  public void test1() {
    final ZonedDateTime date = DateUtils.getUTCDate(2013, 10, 11);
    final Swap<CouponFixedAccruedCompounding, ? extends Payment> derivative = SWAP1.toDerivative(date, new ZonedDateTimeDoubleTimeSeries[] {TS }, CURVE_NAME, CURVE_NAME);
    final double pv = derivative.accept(PresentValueCalculator.getInstance(), BUNDLE);
    System.err.println(EXPECTED1 + ":\t" + OG1 + ":\t" + pv * FX);
  }

  @Test
  public void test2() {
    final ZonedDateTime date = DateUtils.getUTCDate(2013, 10, 11);
    final Swap<CouponFixedAccruedCompounding, ? extends Payment> derivative = SWAP2.toDerivative(date, new ZonedDateTimeDoubleTimeSeries[] {TS }, CURVE_NAME, CURVE_NAME);
    final double pv = derivative.accept(PresentValueCalculator.getInstance(), BUNDLE);
    System.err.println(EXPECTED2 + ":\t" + OG2 + ":\t" + pv * FX);
  }

  @Test
  public void test3() {
    final ZonedDateTime date = DateUtils.getUTCDate(2013, 10, 11);
    final Swap<CouponFixedAccruedCompounding, ? extends Payment> derivative = SWAP3.toDerivative(date, new ZonedDateTimeDoubleTimeSeries[] {TS }, CURVE_NAME, CURVE_NAME);
    final double pv = derivative.accept(PresentValueCalculator.getInstance(), BUNDLE);
    System.err.println(EXPECTED3 + ":\t" + OG3 + ":\t" + pv * FX);
  }

  @Test
  public void test4() {
    final ZonedDateTime date = DateUtils.getUTCDate(2013, 10, 11);
    final Swap<CouponFixedAccruedCompounding, ? extends Payment> derivative = SWAP4.toDerivative(date, new ZonedDateTimeDoubleTimeSeries[] {TS }, CURVE_NAME, CURVE_NAME);
    final double pv = derivative.accept(PresentValueCalculator.getInstance(), BUNDLE);
    System.err.println(EXPECTED4 + ":\t" + OG4 + ":\t" + pv * FX);
  }

  private static class MyCalendar implements Calendar {
    private static final Calendar WEEKENDS = new MondayToFridayCalendar("");
    private static final List<LocalDate> HOLIDAYS = Arrays.asList(LocalDate.of(2013, 11, 15), LocalDate.of(2013, 12, 25),
        LocalDate.of(2014, 1, 1), LocalDate.of(2014, 3, 3), LocalDate.of(2014, 3, 4), LocalDate.of(2014, 4, 18), LocalDate.of(2014, 4, 21),
        LocalDate.of(2014, 5, 1), LocalDate.of(2014, 6, 19), LocalDate.of(2014, 12, 25), LocalDate.of(2015, 1, 1), LocalDate.of(2015, 2, 16), LocalDate.of(2015, 2, 17),
        LocalDate.of(2015, 4, 3), LocalDate.of(2015, 4, 21), LocalDate.of(2015, 5, 1), LocalDate.of(2015, 6, 4), LocalDate.of(2015, 9, 7),
        LocalDate.of(2015, 10, 12), LocalDate.of(2015, 11, 2), LocalDate.of(2015, 12, 25), LocalDate.of(2016, 1, 1));

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      if (!WEEKENDS.isWorkingDay(date)) {
        return false;
      }
      return !HOLIDAYS.contains(date);
    }

    @Override
    public String getConventionName() {
      return "";
    }

  }

}
