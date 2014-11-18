/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.env.AnalyticsEnvironment;
import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponFloatingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * JPY swap pricing example
 */
public class JPYSwapExampleTest {
  private static final double NOTIONAL = 1e10;
  private static final double SPREAD = 5.625e-4;
  private static final Calendar baseCalendar = new CalendarGBP("GBP");
  private static final DayCount ACT360 = DayCounts.ACT_360;
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2014, 9, 25);
  private static final ZonedDateTime SPOT_DATE =  DateUtils.getUTCDate(2012, 5, 28);
  //This just ensures the the swap does start on the spot date
  private static final ZonedDateTime REF_DATE =   ScheduleCalculator.getAdjustedDate(SPOT_DATE, -2, baseCalendar);
  private static final double[] JPY_LIBOR_KNOT_TIMES = new double[] {0.002739726027397, 0.019178082191781, 0.082191780821918, 0.164383561643836, 0.249315068493151, 0.498630136986301,
    0.747945205479452, 1, 1.4986301369863, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30, 40 };
  private static final double[] JPY_LIBOR_ZERO_RATES = new double[] {0.0003983567270378, 0.00042993388819736, 0.00067505895387277, 0.00098218263696675, 0.00113130668887412, 0.00165679051768223,
    0.00168257001572176, 0.00168506355236911, 0.00169161137040334, 0.00169823165591862, 0.00181755633568928, 0.00211702455993688, 0.00258936207292861, 0.00321085530122632, 0.00394155442314442,
    0.00473508721069115, 0.00556448628047427, 0.00642596697808084, 0.00824342986342501, 0.0109670505368749, 0.0145690757463316, 0.0165410883373974, 0.0176675111617229, 0.0193229312672143 };
  private static final double[] JPY_TONA_KNOT_TIMES = new double[] {0.002739726027397, 0.010958904109589, 0.03013698630137, 0.052054794520548, 0.068493150684932, 0.093150684931507, 0.175342465753425,
    0.26027397260274, 0.345205479452055, 0.424657534246575, 0.50958904109589, 0.594520547945205, 0.673972602739726, 0.758904109589041, 0.841095890410959, 0.931506849315068, 1.01095890410959,
    1.50958904109589, 2.01369863013699, 3.01369863013699, 4.01095890410959, 5.01643835616438, 6.01643835616438, 7.01643835616438, 8.01643835616438, 9.01643835616438, 10.0219178082192,
    12.0191780821918, 15.0191780821918, 20.0246575342466, 25.027397260274, 30.0328767123288, 40.0383561643836 };
  private static final double[] JPY_TONA_ZERO_RATES = new double[] {0.00072999927002197, 0.0007299981750037, 0.00066314695313092, 0.00064706696417918, 0.00065227308829961, 0.00064219125327023,
    0.00063715670784559, 0.0006245856060934, 0.00061520134618019, 0.00061107386102671, 0.00061053345407946, 0.00060574710125102, 0.00060356977766275, 0.00059386362023272, 0.00059168152420429,
    0.00060136570133023, 0.00060123121671593, 0.00061419067541201, 0.00062173581024485, 0.00063094292287806, 0.00083093896837371, 0.00121193817346258, 0.00167523291256022, 0.00220255228407544,
    0.00277575634756025, 0.00340181606947232, 0.00412035000329854, 0.00569398049447993, 0.00831519889255234, 0.0118449175150898, 0.0137208249673008, 0.0147877697459616, 0.0163278079744461 };
  private static final double[] JPY_LIBOR1M_KNOT_TIMES = new double[] {0.093150684931507, 0.175342465753425, 0.26027397260274, 0.50958904109589, 0.758904109589041, 1.01095890410959, 2.01369863013699,
    3.01369863013699, 4.01095890410959, 5.01643835616438, 6.01643835616438, 7.01643835616438, 8.01643835616438, 9.01643835616438, 10.0219178082192, 12.0191780821918, 15.0191780821918,
    20.0246575342466, 25.027397260274, 30.0328767123288, 40.0383561643836 };
  private static final double[] JPY_LIBOR1M_ZERO_RATES = new double[] {0.00076039290463677, 0.00085541975703918, 0.00085746558022983, 0.00084703351338364, 0.00086660627173753, 0.00088558915472231,
    0.00094250789602142, 0.00103628104751685, 0.00129852708371005, 0.00174873977892968, 0.00230710677319014, 0.00297319240390701, 0.0036869560813269, 0.00443427512954406, 0.0052289115325722,
    0.0069071518209529, 0.00963132272163178, 0.0132127380932934, 0.0151274377392894, 0.0162307228355892, 0.0178372504710364 };
  private static final double[] JPY_LIBOR3M_KNOT_TIMES = new double[] {0.26027397260274, 0.345205479452055, 0.424657534246575, 0.50958904109589, 0.594520547945205, 0.673972602739726,
    0.758904109589041, 0.841095890410959, 0.931506849315068, 1.01095890410959, 2.01369863013699, 3.01369863013699, 4.01095890410959, 5.01643835616438, 6.01643835616438, 7.01643835616438,
    8.01643835616438, 9.01643835616438, 10.0219178082192, 12.0191780821918, 15.0191780821918, 20.0246575342466, 25.027397260274, 30.0328767123288, 40.0383561643836 };
  private static final double[] JPY_LIBOR3M_ZERO_RATES = new double[] {0.00118029715571636, 0.00121615239802092, 0.00121091360452696, 0.00121039784037458, 0.0012308486011696, 0.00122522063625026,
    0.00122894767585858, 0.00123597797522836, 0.00123298977684211, 0.00123847455076616, 0.00125551354486698, 0.00132417586532975, 0.001598731796945, 0.00202382426358414, 0.00259507579856051,
    0.00324843884416917, 0.00396209177364413, 0.00470918765974003, 0.00551604005478606, 0.00726879530751577, 0.00988817116221545, 0.0134557814886542, 0.0153555390663917, 0.0164277010011057,
    0.0180361268910519 };

  private static final YieldAndDiscountCurve JPY_LIBOR_CURVE;
  private static final YieldAndDiscountCurve JPY_TONA_CURVE;
  private static final YieldAndDiscountCurve JPY_LIBOR1M_CURVE;
  private static final YieldAndDiscountCurve JPY_LIBOR3M_CURVE;

  private static final MulticurveProviderDiscount SINGLE_CURVE;
  private static final MulticurveProviderDiscount TRIPLE_CURVE;
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex JPYLIBOR1M = MASTER_IBOR.getIndex("JPYLIBOR1M");
  private static final IborIndex JPYLIBOR3M = MASTER_IBOR.getIndex("JPYLIBOR3M");
  private static final Currency CCY = JPYLIBOR1M.getCurrency();

  private static final ZonedDateTimeDoubleTimeSeries TS_JPYLIBOR1M = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 8, 26), DateUtils.getUTCDate(2014, 8, 27), DateUtils.getUTCDate(2014, 8, 28), DateUtils.getUTCDate(2014, 9, 23) },
      new double[] {0.0009, 0.0009143, 0.0009, 0.00075});
  private static final ZonedDateTimeDoubleTimeSeries TS_JPYLIBOR3M = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 8, 26), DateUtils.getUTCDate(2014, 8, 27), DateUtils.getUTCDate(2014, 8, 28), DateUtils.getUTCDate(2014, 9, 23) },
      new double[] {0.0012786, 0.0012786, 0.0012786, 0.0011929 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_JPYLIBOR1M_JPYLIBOR3M = new ZonedDateTimeDoubleTimeSeries[] {TS_JPYLIBOR1M, TS_JPYLIBOR3M };

  static {
    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    JPY_LIBOR_CURVE = new YieldCurve("JPY Libor", new InterpolatedDoublesCurve(JPY_LIBOR_KNOT_TIMES, JPY_LIBOR_ZERO_RATES, interpolator, true));
    JPY_TONA_CURVE = new YieldCurve("JPY Tona", new InterpolatedDoublesCurve(JPY_TONA_KNOT_TIMES, JPY_TONA_ZERO_RATES, interpolator, true));
    JPY_LIBOR1M_CURVE = new YieldCurve("JPY Libor 1M", new InterpolatedDoublesCurve(JPY_LIBOR1M_KNOT_TIMES, JPY_LIBOR1M_ZERO_RATES, interpolator, true));
    JPY_LIBOR3M_CURVE = new YieldCurve("JPY Libor 3M", new InterpolatedDoublesCurve(JPY_LIBOR3M_KNOT_TIMES, JPY_LIBOR3M_ZERO_RATES, interpolator, true));

    SINGLE_CURVE = new MulticurveProviderDiscount();
    SINGLE_CURVE.setOrReplaceCurve(CCY, JPY_LIBOR_CURVE);
    SINGLE_CURVE.setOrReplaceCurve(JPYLIBOR1M, JPY_LIBOR_CURVE);
    SINGLE_CURVE.setOrReplaceCurve(JPYLIBOR3M, JPY_LIBOR_CURVE);
    TRIPLE_CURVE = new MulticurveProviderDiscount();
    TRIPLE_CURVE.setOrReplaceCurve(CCY, JPY_TONA_CURVE);
    TRIPLE_CURVE.setOrReplaceCurve(JPYLIBOR1M, JPY_LIBOR1M_CURVE);
    TRIPLE_CURVE.setOrReplaceCurve(JPYLIBOR3M, JPY_LIBOR3M_CURVE);
  }

  private static final double EPS = 1.0e-13; //TODO need to be relaxed, before push

  private void assertRelative(String message, double expected, double obtained, double relTol) {
    double ref = Math.max(Math.abs(obtained), 1.0);
    assertEquals(message, expected, obtained, ref * relTol);
  }

  /**
   * Test swap definition, pv and principal
   */
  @Test
  public void testSwap() {
    boolean print = false;

    /* Build swap */
    Period swapTenor = Period.ofYears(5);
    AnalyticsEnvironment.setInstance(AnalyticsEnvironment.getInstance().toBuilder().modelDayCount(DayCounts.ACT_365)
        .build());
    GeneratorAttributeIR att = new GeneratorAttributeIR(swapTenor);
    GeneratorLegIbor leg1Gen = new GeneratorLegIbor("1M", CCY, JPYLIBOR1M, JPYLIBOR1M.getTenor(),
        JPYLIBOR1M.getSpotLag(), 0, JPYLIBOR1M.getBusinessDayConvention(), JPYLIBOR1M.isEndOfMonth(),
        StubType.SHORT_START, false, baseCalendar, baseCalendar);
    GeneratorLegIbor leg2Gen = new GeneratorLegIbor("3M", CCY, JPYLIBOR3M, JPYLIBOR3M.getTenor(),
        JPYLIBOR3M.getSpotLag(), 0, JPYLIBOR3M.getBusinessDayConvention(), JPYLIBOR3M.isEndOfMonth(),
        StubType.SHORT_START, false, baseCalendar, baseCalendar);
    AnnuityDefinition<?> leg1 = leg1Gen.generateInstrument(REF_DATE, SPREAD, -NOTIONAL, att);
    AnnuityDefinition<?> leg2 = leg2Gen.generateInstrument(REF_DATE, 0, NOTIONAL, att);
    SwapDefinition swapDef = new SwapDefinition(leg1, leg2);
    if (print) {
      System.out.println(swapDef);
    }

    /* Check swap definition */
    AnnuityDefinition<? extends PaymentDefinition> firstLeg = swapDef.getFirstLeg();
    int nFirst = firstLeg.getNumberOfPayments();
    double[] expectedFirstFixingPeriod = new double[] {0.08611111111111111, 0.08888888888888889, 0.08611111111111111,
        0.08611111111111111, 0.08611111111111111, 0.08611111111111111, 0.08333333333333333, 0.08611111111111111,
        0.08611111111111111, 0.07777777777777778, 0.08888888888888889, 0.08333333333333333, 0.08611111111111111,
        0.08611111111111111, 0.08611111111111111, 0.09166666666666666, 0.08333333333333333, 0.08611111111111111,
        0.08888888888888889, 0.08611111111111111, 0.08611111111111111, 0.07777777777777778, 0.08611111111111111,
        0.08333333333333333, 0.09166666666666666, 0.08333333333333333, 0.08611111111111111, 0.08888888888888889,
        0.08333333333333333, 0.08611111111111111, 0.08611111111111111, 0.08611111111111111, 0.08333333333333333,
        0.07777777777777778, 0.08611111111111111, 0.08333333333333333, 0.08888888888888889, 0.08333333333333333,
        0.08611111111111111, 0.08611111111111111, 0.08333333333333333, 0.09166666666666666, 0.08333333333333333,
        0.08611111111111111, 0.08888888888888889, 0.08055555555555556, 0.08611111111111111, 0.09166666666666666,
        0.08333333333333333, 0.08333333333333333, 0.09166666666666666, 0.08611111111111111, 0.08333333333333333,
        0.08611111111111111, 0.08333333333333333, 0.09166666666666666, 0.08055555555555556, 0.07777777777777778,
        0.08611111111111111, 0.08888888888888889 };
    double[] expectedFirstPaymentPeriod = new double[] {0.08611111111111111, 0.08888888888888889, 0.08055555555555556,
        0.08611111111111111, 0.08611111111111111, 0.08333333333333333, 0.08333333333333333, 0.08611111111111111,
        0.08611111111111111, 0.07777777777777778, 0.08888888888888889, 0.08055555555555556, 0.08611111111111111,
        0.08611111111111111, 0.08333333333333333, 0.09166666666666666, 0.07777777777777778, 0.08611111111111111,
        0.08888888888888889, 0.08055555555555556, 0.08611111111111111, 0.07777777777777778, 0.08611111111111111,
        0.08333333333333333, 0.09166666666666666, 0.07777777777777778, 0.08611111111111111, 0.08888888888888889,
        0.08055555555555556, 0.08611111111111111, 0.08611111111111111, 0.08333333333333333, 0.08333333333333333,
        0.08611111111111111, 0.08055555555555556, 0.08333333333333333, 0.08888888888888889, 0.08055555555555556,
        0.08611111111111111, 0.08611111111111111, 0.08333333333333333, 0.09166666666666666, 0.08055555555555556,
        0.08333333333333333, 0.08888888888888889, 0.08055555555555556, 0.08333333333333333, 0.09166666666666666,
        0.07777777777777778, 0.08333333333333333, 0.09166666666666666, 0.08055555555555556, 0.08333333333333333,
        0.08611111111111111, 0.08333333333333333, 0.09166666666666666, 0.08055555555555556, 0.07777777777777778,
        0.08611111111111111, 0.08888888888888889 };
    double[] expectedFirstSpreadAmounts = new double[] {-484375, -500000, -453125, -484375, -484375, -468750, -468750,
        -484375, -484375, -437500, -500000, -453125, -484375, -484375, -468750, -515625, -437500, -484375, -500000,
        -453125, -484375, -437500, -484375, -468750, -515625, -437500, -484375, -500000, -453125, -484375, -484375,
        -468750, -468750, -484375, -453125, -468750, -500000, -453125, -484375, -484375, -468750, -515625, -453125,
        -468750, -500000, -453125, -468750, -515625, -437500, -468750, -515625, -453125, -468750, -484375, -468750,
        -515625, -453125, -437500, -484375, -500000 };
    for (int i = 0; i < nFirst; ++i) {
      assertTrue(firstLeg.getNthPayment(i) instanceof CouponIborSpreadDefinition);
      CouponIborSpreadDefinition coupon = (CouponIborSpreadDefinition) firstLeg.getNthPayment(i);
      assertRelative("testSwap, first leg", expectedFirstFixingPeriod[i], coupon.getFixingPeriodAccrualFactor(), EPS);
      assertRelative("testSwap, first leg", expectedFirstPaymentPeriod[i], coupon.getPaymentYearFraction(), EPS);
      assertRelative("testSwap, first leg", expectedFirstSpreadAmounts[i], coupon.getSpreadAmount(), EPS);
    }
    AnnuityDefinition<? extends PaymentDefinition> secondLeg = swapDef.getSecondLeg();
    int nSecond = secondLeg.getNumberOfPayments();
    double[] expectedSecondFixingPeriod = new double[] {0.25555555555555554, 0.25555555555555554, 0.25555555555555554,
        0.24722222222222223, 0.25555555555555554, 0.25555555555555554, 0.25555555555555554, 0.24722222222222223,
        0.25555555555555554, 0.25555555555555554, 0.25277777777777777, 0.24722222222222223, 0.25555555555555554,
        0.2611111111111111, 0.25277777777777777, 0.25555555555555554, 0.25555555555555554, 0.25555555555555554,
        0.25555555555555554, 0.25277777777777777 };
    double[] expectedSecondPaymentPeriod = new double[] {0.25555555555555554, 0.25555555555555554, 0.25555555555555554,
        0.24722222222222223, 0.25555555555555554, 0.25555555555555554, 0.25555555555555554, 0.24722222222222223,
        0.25555555555555554, 0.25555555555555554, 0.25277777777777777, 0.25, 0.25555555555555554, 0.2611111111111111,
        0.25277777777777777, 0.25555555555555554, 0.25277777777777777, 0.25, 0.25555555555555554, 0.25277777777777777 };
    double expectedSecondSpreadAmounts = 0.0;
    for (int i = 0; i < nSecond; ++i) {
      assertTrue(secondLeg.getNthPayment(i) instanceof CouponIborSpreadDefinition);
      CouponIborSpreadDefinition coupon = (CouponIborSpreadDefinition) secondLeg.getNthPayment(i);
      assertRelative("testSwap, second leg", expectedSecondFixingPeriod[i], coupon.getFixingPeriodAccrualFactor(), EPS);
      assertRelative("testSwap, second leg", expectedSecondPaymentPeriod[i], coupon.getPaymentYearFraction(), EPS);
      assertRelative("testSwap, second leg", expectedSecondSpreadAmounts, coupon.getSpreadAmount(), EPS);
    }

    /* Check PV and principal */
    SwapDefinition swapMd = removeUnclearedCoupon(swapDef, TRADE_DATE, baseCalendar, baseCalendar);
    Swap<? extends Payment, ? extends Payment> swap = swapMd.toDerivative(TRADE_DATE, TS_ARRAY_JPYLIBOR1M_JPYLIBOR3M);
    MultipleCurrencyAmount pv1 = swap.accept(PVDC, SINGLE_CURVE);
    assertRelative("testSwap, single curve PV", -1.370494358316157E7, pv1.getAmount(CCY), EPS);
    if (print) {
      System.out.println("single curve PV: " + pv1);
    }
    MultipleCurrencyAmount pv2 = swap.accept(PVDC, TRIPLE_CURVE);
    assertRelative("testSwap, Multi-curve PV", -5988005.581088446, pv2.getAmount(CCY), EPS);
    if (print) {
      System.out.println("Multi-curve PV: " + pv2);
    }
    ZonedDateTime fixingDate1M = getLastFixing(leg1, TRADE_DATE);
    ZonedDateTime fixingDate3M = getLastFixing(leg2, TRADE_DATE);
    if (fixingDate1M.isBefore(TRADE_DATE) && fixingDate3M.isBefore(TRADE_DATE)) {
      double fixed1M = TS_JPYLIBOR1M.getValue(fixingDate1M);
      double fixed3M = TS_JPYLIBOR3M.getValue(fixingDate3M);
      double accrued1M = -ACT360.getDayCountFraction(fixingDate1M, TRADE_DATE) * NOTIONAL * (fixed1M + SPREAD);
      double accrued3M = ACT360.getDayCountFraction(fixingDate3M, TRADE_DATE) * NOTIONAL * (fixed3M);
      if (print) {
        System.out.println("accrued1M: " + accrued1M);
        System.out.println("accrued3M: " + accrued3M);
      }
      double pricipal = pv2.getAmount(CCY) - (accrued1M + accrued3M);
      if (print) {
        System.out.println("pricipal: " + pricipal);
      }
      assertRelative("testSwap, Principal", -5834755.581088446, pricipal, EPS);
    }
    AnalyticsEnvironment.setInstance(AnalyticsEnvironment.DEFAULT);
  }

  private SwapDefinition removeUnclearedCoupon(SwapDefinition swap, ZonedDateTime data, Calendar firstLegCalendar,
      Calendar secondLegCalendar) {
    AnnuityDefinition<? extends PaymentDefinition> firstLeg = removeUnclearedCoupon(swap.getFirstLeg(), data,
        firstLegCalendar);
    AnnuityDefinition<? extends PaymentDefinition> secondLeg = removeUnclearedCoupon(swap.getSecondLeg(), data,
        secondLegCalendar);
    return new SwapDefinition(firstLeg, secondLeg);
  }

  private AnnuityDefinition<PaymentDefinition> removeUnclearedCoupon(
      AnnuityDefinition<? extends PaymentDefinition> annuity, ZonedDateTime date, Calendar calendar) {
    PaymentDefinition[] payments = annuity.getPayments();
    final List<PaymentDefinition> resultList = new ArrayList<>();
    ZonedDateTime adjDate = ScheduleCalculator.getAdjustedDate(date, 3, calendar);
    for (final PaymentDefinition payment : payments) {
      if (!adjDate.isAfter(payment.getPaymentDate())) {
        resultList.add(payment);
      }
    }
    return new AnnuityDefinition<>(resultList.toArray(new PaymentDefinition[resultList.size()]), calendar);
  }

  /**
   * Print multicurve test, for debugging
   */
  @Test(enabled = false)
  public void printMulticurve() {
    System.out.println(SINGLE_CURVE);
    System.out.println(CCY);
    System.out.println(JPYLIBOR1M.getDayCount() + "\t" + JPYLIBOR1M.getTenor() + "\t" + JPYLIBOR1M.getSpotLag());
    System.out.println(JPYLIBOR3M.getDayCount() + "\t" + JPYLIBOR3M.getTenor() + "\t" + JPYLIBOR3M.getSpotLag());
    System.out.println(JPYLIBOR3M);
  }

  private ZonedDateTime getLastFixing(AnnuityDefinition<?> leg, ZonedDateTime now) {
    for (PaymentDefinition p : leg.getPayments()) {
      if (p.getPaymentDate().isAfter(now)) {
        return ((CouponFloatingDefinition) p).getFixingDate();
      }
    }
    throw new IllegalArgumentException("all payments in past");
  }

}
