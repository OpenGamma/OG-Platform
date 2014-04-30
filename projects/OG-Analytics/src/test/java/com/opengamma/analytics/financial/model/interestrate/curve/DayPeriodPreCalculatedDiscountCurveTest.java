/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DayPeriodPreCalculatedDiscountCurveTest {

  private static double TOLERANCE = 1e-10;
  // note the time periods passed in must be a whole number divided by 365.25, otherwise the pre calculated factors
  // will not line up
  private static double[] x = new double[] {0 / DateUtils.DAYS_PER_YEAR, 1 / DateUtils.DAYS_PER_YEAR,
    2 / DateUtils.DAYS_PER_YEAR, 100 / DateUtils.DAYS_PER_YEAR, 293 / DateUtils.DAYS_PER_YEAR,
    309 / DateUtils.DAYS_PER_YEAR, 428 / DateUtils.DAYS_PER_YEAR, 567 / DateUtils.DAYS_PER_YEAR,
    5634 / DateUtils.DAYS_PER_YEAR };
  private static double[] y = new double[] {1.0, 0.75, 0.5, 0.25, 0.15, 0.12, 0.10, 0.9, 0.85 };
  private static InterpolatedDoublesCurve DOUBLES_CURVE = InterpolatedDoublesCurve.from(x, y, Interpolator1DFactory.LINEAR_INSTANCE);
  private static final DiscountCurve EXISTING_CURVE = DiscountCurve.from(DOUBLES_CURVE);

  @Test(groups = TestGroup.UNIT)
  public void testGetDiscountFactor() throws Exception {
    final DayPeriodPreCalculatedDiscountCurve curve = new DayPeriodPreCalculatedDiscountCurve("test", DOUBLES_CURVE, DateUtils.DAYS_PER_YEAR);
    curve.preCalculateDiscountFactors(15);
    for (int i = 0; i < x[x.length - 1]; i++) {
      final double t = i / DateUtils.DAYS_PER_YEAR;
      Assert.assertEquals(EXISTING_CURVE.getDiscountFactor(t), curve.getDiscountFactor(t), TOLERANCE);
    }
  }

  // Data for testing the curve on a swap
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 11, 5);
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();

  private static final MulticurveProviderDiscount MULTICURVES_WITH_PRECALCULATED_DISCOUNT = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = INDEX_LIST[0];
  private static final IborIndex EURIBOR6M = INDEX_LIST[1];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2013, 9, 9);
  private static final ZonedDateTime END_DATE_3 = ScheduleCalculator.getAdjustedDate(START_DATE, EURIBOR3M.getTenor(), EURIBOR3M, CALENDAR);
  private static final Period TOTAL_TENOR = EURIBOR3M.getTenor().plus(EURIBOR6M.getTenor());
  private static final ZonedDateTime END_DATE_6 = ScheduleCalculator.getAdjustedDate(START_DATE, TOTAL_TENOR, EURIBOR3M, CALENDAR);
  // Definitions
  private static final double NOTIONAL = 100000.0; // 100m
  private static final double RATE = 0.0250; // 2.5%
  private static final CouponIborDefinition CPN_IBOR_3_DEFINITION = CouponIborDefinition.from(START_DATE, END_DATE_3, NOTIONAL, EURIBOR3M, CALENDAR);
  private static final CouponIborDefinition CPN_IBOR_6_DEFINITION = CouponIborDefinition.from(END_DATE_3, END_DATE_6, NOTIONAL, EURIBOR6M, CALENDAR);
  private static final AnnuityDefinition<CouponDefinition> ANNUITY_IBOR_DEFINITION = new AnnuityDefinition<CouponDefinition>(
      new CouponIborDefinition[] {CPN_IBOR_3_DEFINITION, CPN_IBOR_6_DEFINITION }, CALENDAR);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final AnnuityCouponFixedDefinition ANNUITY_FIXED_DEFINITION = AnnuityCouponFixedDefinition.from(START_DATE, TOTAL_TENOR, EUR1YEURIBOR6M, NOTIONAL, RATE, true);
  private static final SwapDefinition SWAP_DEFINITION = new SwapDefinition(ANNUITY_FIXED_DEFINITION, ANNUITY_IBOR_DEFINITION);
  // Derivatives
  private static final CouponIbor CPN_IBOR_3 = (CouponIbor) CPN_IBOR_3_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponIbor CPN_IBOR_6 = (CouponIbor) CPN_IBOR_6_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final Annuity<? extends Payment> ANNUITY_IBOR = ANNUITY_IBOR_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final Annuity<? extends Payment> ANNUITY_FIXED = ANNUITY_FIXED_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final Swap<? extends Payment, ? extends Payment> SWAP = SWAP_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] EUR_DSC_TIME = new double[] {0.0, 182 / 365.25, 365 / 365.25, 731 / 365.25, 1826 / 365.25, 3652 / 365.25 };

  private static final double[] EUR_DSC_DISCOUNT_FACTOR = new double[] {1.0, 0.99, .98, .97, 1.0, .99 };
  private static final String EUR_DSC_NAME = "EUR Dsc";
  /*  private static final YieldAndDiscountCurve EUR_DSC = new YieldCurve(EUR_DSC_NAME, new InterpolatedDoublesCurve(EUR_DSC_TIME, EUR_DSC_RATE, LINEAR_FLAT, true, EUR_DSC_NAME));*/
  private static final YieldAndDiscountCurve EUR_DSC = DiscountCurve.from(new InterpolatedDoublesCurve(EUR_DSC_TIME, EUR_DSC_DISCOUNT_FACTOR, LINEAR_FLAT, true, EUR_DSC_NAME));
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-10;

  @Test(enabled = false)
  /**
   * Tests the present value for a swap with the Ibor leg having different Ibor indexes (EURIBOR3M and EURIBOR6M).
   * this test passed onbly if the MODEL_DAYCOUNT in TimeCalculator.properties is set at Actul / 365.25
   */
  public void testWithSwap() {

    MULTICURVES.replaceCurve(EUR, EUR_DSC);
    final DayPeriodPreCalculatedDiscountCurve curve = new DayPeriodPreCalculatedDiscountCurve("test", new InterpolatedDoublesCurve(EUR_DSC_TIME, EUR_DSC_DISCOUNT_FACTOR, LINEAR_FLAT, true,
        EUR_DSC_NAME), 365.25);
    curve.preCalculateDiscountFactors(50);
    final MulticurveProviderDiscount MULTICURVES_WITH_PRECALCULATED_DISCOUNT = MULTICURVES.withDiscountFactor(EUR, curve);
    final MultipleCurrencyAmount pvCalcCpn3 = CPN_IBOR_3.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount pvMethCpn3 = METHOD_CPN_IBOR.presentValue(CPN_IBOR_3, MULTICURVES);
    final MultipleCurrencyAmount pvCalcCpn6 = CPN_IBOR_6.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount pvMethCpn6 = METHOD_CPN_IBOR.presentValue(CPN_IBOR_6, MULTICURVES);
    final MultipleCurrencyAmount pvCalcAnnIbor = ANNUITY_IBOR.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount pvCalcAnnFixed = ANNUITY_FIXED.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount pvCalcSwap = SWAP.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount pvCalcCpn3_WITH_PRECALCULATED_DISCOUNT = CPN_IBOR_3.accept(PVDC, MULTICURVES_WITH_PRECALCULATED_DISCOUNT);
    final MultipleCurrencyAmount pvMethCpn3_WITH_PRECALCULATED_DISCOUNT = METHOD_CPN_IBOR.presentValue(CPN_IBOR_3, MULTICURVES_WITH_PRECALCULATED_DISCOUNT);
    final MultipleCurrencyAmount pvCalcCpn6_WITH_PRECALCULATED_DISCOUNT = CPN_IBOR_6.accept(PVDC, MULTICURVES_WITH_PRECALCULATED_DISCOUNT);
    final MultipleCurrencyAmount pvMethCpn6_WITH_PRECALCULATED_DISCOUNT = METHOD_CPN_IBOR.presentValue(CPN_IBOR_6, MULTICURVES_WITH_PRECALCULATED_DISCOUNT);
    final MultipleCurrencyAmount pvCalcAnnIbor_WITH_PRECALCULATED_DISCOUNT = ANNUITY_IBOR.accept(PVDC, MULTICURVES_WITH_PRECALCULATED_DISCOUNT);
    final MultipleCurrencyAmount pvCalcAnnFixed_WITH_PRECALCULATED_DISCOUNT = ANNUITY_FIXED.accept(PVDC, MULTICURVES_WITH_PRECALCULATED_DISCOUNT);
    final MultipleCurrencyAmount pvCalcSwap_WITH_PRECALCULATED_DISCOUNT = SWAP.accept(PVDC, MULTICURVES_WITH_PRECALCULATED_DISCOUNT);
    assertEquals("PresentValueDiscountingProviderCalculator: multiple Ibor index", pvCalcCpn3_WITH_PRECALCULATED_DISCOUNT.getAmount(EUR), pvCalcCpn3.getAmount(EUR), TOLERANCE_PV);
    assertEquals("PresentValueDiscountingProviderCalculator: multiple Ibor index", pvCalcCpn6_WITH_PRECALCULATED_DISCOUNT.getAmount(EUR), pvCalcCpn6.getAmount(EUR), TOLERANCE_PV);
    assertEquals("PresentValueDiscountingProviderCalculator: multiple Ibor index", pvCalcAnnIbor_WITH_PRECALCULATED_DISCOUNT.getAmount(EUR), pvCalcAnnIbor.getAmount(EUR), TOLERANCE_PV);
    assertEquals("PresentValueDiscountingProviderCalculator: multiple Ibor index", pvCalcAnnFixed.getAmount(EUR), pvCalcAnnFixed_WITH_PRECALCULATED_DISCOUNT.getAmount(EUR), TOLERANCE_PV);
    assertEquals("PresentValueDiscountingProviderCalculator: multiple Ibor index", pvMethCpn3.getAmount(EUR), pvMethCpn3_WITH_PRECALCULATED_DISCOUNT.getAmount(EUR), TOLERANCE_PV);
    assertEquals("PresentValueDiscountingProviderCalculator: multiple Ibor index", pvMethCpn6.getAmount(EUR), pvMethCpn6_WITH_PRECALCULATED_DISCOUNT.getAmount(EUR), TOLERANCE_PV);
    assertEquals("PresentValueDiscountingProviderCalculator: multiple Ibor index", pvCalcSwap_WITH_PRECALCULATED_DISCOUNT.getAmount(EUR), pvCalcSwap.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * 
   */
  public void testPreCalculatedVsOutput() {
    final String preiInterpolatedCurveName = "preiInterpolated Discount curve";
    final Interpolator1D logNaturalinterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC_MONOTONE,
        Interpolator1DFactory.QUADRATIC_LEFT_EXTRAPOLATOR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final double[] dscCurveTime = new double[] {0.0, 1 / 365.25, 2 / 365.25, 3 / 365.25, 91 / 365.25, 183 / 365.25, 274 / 365.25, 365 / 365.25, 457 / 365.25, 548 / 365.25, 639 / 365.25, 731 / 365.25,
      1096 / 365.25, 1461 / 365.25, 1826 / 365.25, 2192 / 365.25, 2557 / 365.25, 2922 / 365.25, 3287 / 365.25, 3653 / 365.25, 4383 / 365.25, 5479 / 365.25, 7305 / 365.25, 9131 / 365.25,
      10958 / 365.25, 14610 / 365.25, 18263 / 365.25 };

    final double[] dscCurveDiscountFactor = new double[] {1.0, 0.999995414, 0.999990827, 0.999986241, 0.999583194, 0.999167209, 0.998770249, 0.998379852, 0.997966181, 0.997459857, 0.996773166,
      0.995843348, 0.989078497, 0.976557279, 0.959377269, 0.938363477, 0.91453858, 0.888747447, 0.861693639, 0.83391463, 0.778517911, 0.700537471, 0.596392269, 0.516411342, 0.451196242, 0.3436152,
      0.259911234 };

    final DayPeriodPreCalculatedDiscountCurve periodPreCalculatedCurve = new DayPeriodPreCalculatedDiscountCurve("test", new InterpolatedDoublesCurve(dscCurveTime, dscCurveDiscountFactor,
        logNaturalinterpolator, true, preiInterpolatedCurveName), 365.25);
    periodPreCalculatedCurve.preCalculateDiscountFactors(50);
    final double[] outputTime = new double[] {0.0, 1 / 365.25, 2 / 365.25, 3 / 365.25, 4 / 365.25, 7 / 365.25, 8 / 365.25, 9 / 365.25, 17 / 365.25, 77 / 365.25, 91 / 365.25, 140 / 365.25,
      183 / 365.25, 219 / 365.25, 274 / 365.25, 301 / 365.25, 365 / 365.25, 672 / 365.25, 1001 / 365.25, 2034 / 365.25, 9879 / 365.25, 13374 / 365.25, 16180 / 365.25 };

    final double[] outputDiscountFactor = new double[] {1.0, 0.999995414, 0.999990827, 0.999986241, 0.999981654, 0.999967895, 0.999963309, 0.999958723, 0.999922035, 0.999647188, 0.999583194,
      0.999360197, 0.999167209, 0.999008575, 0.998770249, 0.998654304, 0.998379852, 0.996469188, 0.991376737, 0.947845026, 0.488392208, 0.377151958, 0.304915694 };
    for (int i = 0; i < outputDiscountFactor.length; i++) {
      assertEquals("DayPeriodPreCalculatedDiscountCurve: discount factor value" + outputTime[i] * 365.25, outputDiscountFactor[i], periodPreCalculatedCurve.getDiscountFactor(outputTime[i]),
          1.0E-6);
    }
  }

  public void testPreCalculatedVsOriginalCurve() {
    final String curveName = "Discount curve";
    final String preiInterpolatedCurveName = "preiInterpolated Discount curve";
    final Interpolator1D logNaturalinterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC_MONOTONE,
        Interpolator1DFactory.QUADRATIC_LEFT_EXTRAPOLATOR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final double[] dscCurveTime = new double[] {0.0, 1 / 365.25, 2 / 365.25, 3 / 365.25, 91 / 365.25, 183 / 365.25, 274 / 365.25, 365 / 365.25, 457 / 365.25, 548 / 365.25, 639 / 365.25, 731 / 365.25,
      1096 / 365.25, 1461 / 365.25, 1826 / 365.25, 2192 / 365.25, 2557 / 365.25, 2922 / 365.25, 3287 / 365.25, 3653 / 365.25, 4383 / 365.25, 5479 / 365.25, 7305 / 365.25, 9131 / 365.25,
      10958 / 365.25, 14610 / 365.25, 18263 / 365.25 };

    final double[] dscCurveDiscountFactor = new double[] {1.0, 0.999995414, 0.999990827, 0.999986241, 0.999583194, 0.999167209, 0.998770249, 0.998379852, 0.997966181, 0.997459857, 0.996773166,
      0.995843348, 0.989078497, 0.976557279, 0.959377269, 0.938363477, 0.91453858, 0.888747447, 0.861693639, 0.83391463, 0.778517911, 0.700537471, 0.596392269, 0.516411342, 0.451196242, 0.3436152,
      0.259911234 };

    final double[] dscCurveYields = new double[dscCurveDiscountFactor.length];
    dscCurveYields[0] = 1.0;
    for (int i = 1; i < dscCurveYields.length; i++) {
      dscCurveYields[i] = -Math.log(dscCurveDiscountFactor[i]) / dscCurveTime[i];
    }
    final YieldAndDiscountCurve discountCurve = new DiscountCurve(curveName, new InterpolatedDoublesCurve(dscCurveTime, dscCurveDiscountFactor,
        logNaturalinterpolator, true, preiInterpolatedCurveName));
    final DayPeriodPreCalculatedDiscountCurve periodPreCalculatedCurve = new DayPeriodPreCalculatedDiscountCurve("test", new InterpolatedDoublesCurve(dscCurveTime, dscCurveDiscountFactor,
        logNaturalinterpolator, true, preiInterpolatedCurveName), 365.25);
    periodPreCalculatedCurve.preCalculateDiscountFactors(50);
    final double[] outputTime = new double[] {0.0, 1 / 365.25, 2 / 365.25, 3 / 365.25, 4 / 365.25, 7 / 365.25, 8 / 365.25, 9 / 365.25, 17 / 365.25, 77 / 365.25, 91 / 365.25, 140 / 365.25,
      183 / 365.25, 219 / 365.25, 274 / 365.25, 301 / 365.25, 365 / 365.25, 672 / 365.25, 1001 / 365.25, 2034 / 365.25, 9879 / 365.25, 13374 / 365.25, 16180 / 365.25 };

    final double[] outputDiscountFactor = new double[] {1.0, 0.999995414, 0.999990827, 0.999986241, 0.999981654, 0.999967895, 0.999963309, 0.999958723, 0.999922035, 0.999647188, 0.999583194,
      0.999360197, 0.999167209, 0.999008575, 0.998770249, 0.998654304, 0.998379852, 0.996469188, 0.991376737, 0.947845026, 0.488392208, 0.377151958, 0.304915694 };
    for (int i = 0; i < outputDiscountFactor.length; i++) {
      assertEquals("DayPeriodPreCalculatedDiscountCurve: discount factor value" + outputTime[i] * 365.25, discountCurve.getDiscountFactor(outputTime[i]),
          periodPreCalculatedCurve.getDiscountFactor(outputTime[i]), 1.0E-14);
    }
  }
}
