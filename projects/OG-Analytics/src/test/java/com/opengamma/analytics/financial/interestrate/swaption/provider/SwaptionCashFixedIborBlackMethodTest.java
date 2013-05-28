/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.BlackSwaptionSensitivityNodeCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

public class SwaptionCashFixedIborBlackMethodTest {
  // Data
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  //  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final BlackFlatSwaptionParameters BLACK = TestsDataSetsBlack.createBlackSwaptionEUR6();
  private static final BlackSwaptionFlatProvider BLACK_MULTICURVES = new BlackSwaptionFlatProvider(MULTICURVES, BLACK);
  private static final String NOT_USED = "Not used";
  private static final String[] NOT_USED_A = {NOT_USED, NOT_USED, NOT_USED };
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor GENERATOR_EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 10);
  // Swaption
  private static final Period EXPIRY_TENOR = Period.ofMonths(26); // To be between nodes.
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_TENOR, GENERATOR_EUR1YEURIBOR6M.getBusinessDayConvention(), CALENDAR,
      GENERATOR_EUR1YEURIBOR6M.isEndOfMonth());
  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, GENERATOR_EUR1YEURIBOR6M.getSpotLag(), CALENDAR);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final double NOTIONAL = 123456789.0;
  private static final double RATE = 0.02;
  private static final SwapFixedIborDefinition SWAP_DEFINITION_REC = SwapFixedIborDefinition.from(SETTLE_DATE, SWAP_TENOR, GENERATOR_EUR1YEURIBOR6M, NOTIONAL, RATE, false);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_REC = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_REC, true);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_REC = SWAPTION_DEFINITION_LONG_REC.toDerivative(REFERENCE_DATE, NOT_USED_A);
  // Method - calculator
  private static final double TOLERANCE_PRICE = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2;
  //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
  private static final SwaptionCashFixedIborBlackMethod METHOD_BLACK = SwaptionCashFixedIborBlackMethod.getInstance();
  //  private static final PresentValueBlackCalculator PVC_BLACK = PresentValueBlackCalculator.getInstance();
  //  private static final PresentValueCurveSensitivityBlackCalculator PVCSC_BLACK = PresentValueCurveSensitivityBlackCalculator.getInstance();
  //  private static final PresentValueBlackSwaptionSensitivityBlackCalculator PVBSC_BLACK = PresentValueBlackSwaptionSensitivityBlackCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRC = ParRateDiscountingCalculator.getInstance();
  private static final BlackSwaptionSensitivityNodeCalculator BSSNC = new BlackSwaptionSensitivityNodeCalculator();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pvMethod = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    final double forward = SWAPTION_LONG_REC.getUnderlyingSwap().accept(PRC, MULTICURVES);
    final double pvbp = METHOD_SWAP.getAnnuityCash(SWAPTION_LONG_REC.getUnderlyingSwap(), forward);
    final double discountFactorSettle = MULTICURVES.getDiscountFactor(SWAPTION_LONG_REC.getCurrency(), SWAPTION_LONG_REC.getSettlementTime());
    final double volatility = BLACK.getVolatility(SWAPTION_LONG_REC.getTimeToExpiry(), SWAPTION_LONG_REC.getMaturityTime());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, pvbp * discountFactorSettle, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(SWAPTION_LONG_REC);
    final double pvExpected = func.evaluate(dataBlack);
    assertEquals("Swaption Black method: present value", 1, pvMethod.size());
    final CurrencyAmount ca = pvMethod.getCurrencyAmounts()[0];
    assertEquals("Swaption Black method: present value", SWAPTION_LONG_REC.getCurrency(), ca.getCurrency());
    assertEquals("Swaption Black method: present value", pvExpected, ca.getAmount(), TOLERANCE_PRICE);
  }

  //  @Test
  //  /**
  //   * Compare the method figures to the Calculator figures.
  //   */
  //  public void presentValueMethodVsCalculator() {
  //    final CurrencyAmount pvMethod = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, CURVES_BLACK);
  //    final double pvCalculator = PVC_BLACK.visit(SWAPTION_LONG_REC, CURVES_BLACK);
  //    assertEquals("Swaption Black method: present value", pvCalculator, pvMethod.getAmount(), TOLERANCE_PRICE);
  //  }
  //

  @Test
  /**
   * Tests the curve sensitivity for the explicit formula.
   */
  public void presentValueCurveSensitivity() {
    MultipleCurrencyMulticurveSensitivity pvcsSwaption = METHOD_BLACK.presentValueCurveSensitivity(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    pvcsSwaption = pvcsSwaption.cleaned();
    // 1. Forward curve sensitivity
    final DoubleArrayList forwardTime = new DoubleArrayList();
    for (int loopcpn = 0; loopcpn < SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    final double[] nodeTimesForward = forwardTime.toDoubleArray();
    final DoubleArrayList discTime = new DoubleArrayList();
    discTime.add(SWAPTION_LONG_REC.getSettlementTime());
    for (int loopcpn = 0; loopcpn < SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    final double[] nodeTimesDisc = discTime.toDoubleArray();
    //final MulticurveSensitivity sensitivityPvFwd = pvcsSwaption.getSensitivity(SWAPTION_LONG_REC.getCurrency());
    //final MulticurveSensitivity fdSensitivityPvFwd = getDiscountingCurveFiniteDifferenceSensitivities(SWAPTION_LONG_REC, BLACK_MULTICURVES, nodeTimesDisc, TOLERANCE_DELTA);
    //    assertEquals("Swaption Black method: curve sensitivities", sensitivityPvFwd, fdSensitivityPvFwd, TOLERANCE_DELTA);
    //    // 2. Discounting curve sensitivity
    //    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    //    discTime.add(SWAPTION_LONG_REC.getSettlementTime());
    //    for (int loopcpn = 0; loopcpn < SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
    //      final CouponIbor cpn = (CouponIbor) SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
    //      discTime.add(cpn.getPaymentTime());
    //    }
    //    final double[] nodeTimesDisc = discTime.toDoubleArray();
    //    final List<DoublesPair> sensiPvDisc = pvcsSwaption.getSensitivities().get(CURVES_NAME[0]);
    //    final List<DoublesPair> fdSenseDsc = null;//FDCurveSensitivityCalculator.curveSensitvityFDCalculator(SWAPTION_LONG_REC, METHOD_BLACK, CURVES_BLACK, CURVES_NAME[0], nodeTimesDisc, TOLERANCE_DELTA);
    //    assertSensitivityEquals(sensiPvDisc, fdSenseDsc, TOLERANCE_DELTA);
  }

  //  private static MulticurveSensitivity getDiscountingCurveFiniteDifferenceSensitivities(final InstrumentDerivative instrument, final BlackSwaptionProviderInterface blackMulticurves,
  //      final double[] times, final double absTol) {
  //    final double eps = 1e-6;
  //
  //    final List<DoublesPair> res = new ArrayList<DoublesPair>();
  //    for (final double t : times) {
  //      final Function1D<Double, Double> blip = new Function1D<Double, Double>() {
  //        @Override
  //        public Double evaluate(final Double x) {
  //          return (Math.abs(x - t) < 3.0e-6 ? eps : 0.0); //100 second tolerance
  //        }
  //      };
  //      final YieldAndDiscountCurve blipCurve = YieldCurve.from(new FunctionalDoublesCurve(blip));
  //      final MulticurveProviderDiscount multicurveProvider = (MulticurveProviderDiscount) blackMulticurves.getMulticurveProvider();
  //      final YieldAndDiscountCurve originalCurve = multicurveProvider.getCurve(Currency.EUR);
  //      final YieldAndDiscountCurve upCurve = new YieldAndDiscountAddZeroSpreadCurve("UpCurve", false, originalCurve, blipCurve);
  //      final YieldAndDiscountCurve downCurve = new YieldAndDiscountAddZeroSpreadCurve("DownCurve", true, originalCurve, blipCurve);
  //      final MulticurveProviderDiscount upCurves = multicurveProvider.withDiscountFactor(Currency.EUR, upCurve);
  //      final MulticurveProviderDiscount downCurves = multicurveProvider.withDiscountFactor(Currency.EUR, downCurve);
  //      final BlackSwaptionProviderInterface upData = new BlackSwaptionProviderDiscount(upCurves, blackMulticurves.getBlackParameters());
  //      final BlackSwaptionProviderInterface downData = new BlackSwaptionProviderDiscount(downCurves, blackMulticurves.getBlackParameters());
  //      final MultipleCurrencyAmount upPV = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, upData);
  //      final MultipleCurrencyAmount downPV = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, downData);
  //      assertEquals(1, upPV.size());
  //      assertEquals(1, downPV.size());
  //      final double up = upPV.getAmount(Currency.EUR);
  //      final double down = downPV.getAmount(Currency.EUR);
  //      final double dPV = (up - down) / 2 / eps;
  //      if (Math.abs(dPV) > absTol) {
  //        res.add(new DoublesPair(t, dPV));
  //      }
  //    }
  //    return MulticurveSensitivity.ofYieldDiscounting(res);
  //    return null;
  //  }
  //
  //  @Test
  //  /**
  //   * Compare the method figures to the Calculator figures.
  //   */
  //  public void presentValueCurveSensitivityMethodVsCalculator() {
  //    final InterestRateCurveSensitivity pvcsMethod = METHOD_BLACK.presentValueCurveSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
  //    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(PVCSC_BLACK.visit(SWAPTION_LONG_REC, CURVES_BLACK));
  //    AssertSensivityObjects.assertEquals("Swaption Black method: present value", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  //  }
  //
  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackSensitivity() {
    final double shift = 1.0E-6;
    final PresentValueBlackSwaptionSensitivity pvbvs = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    final BlackFlatSwaptionParameters blackP = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(shift);
    final BlackSwaptionFlatProvider curvesBlackP = new BlackSwaptionFlatProvider(MULTICURVES, blackP);
    final MultipleCurrencyAmount pvP = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackP);
    final BlackFlatSwaptionParameters blackM = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(-shift);
    final BlackSwaptionFlatProvider curvesBlackM = new BlackSwaptionFlatProvider(MULTICURVES, blackM);
    final MultipleCurrencyAmount pvM = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackM);
    final DoublesPair point = new DoublesPair(SWAPTION_LONG_REC.getTimeToExpiry(), SWAPTION_LONG_REC.getMaturityTime());
    final Double volatilitySensitivity = pvbvs.getSensitivity().getMap().get(point);
    assertEquals("Swaption Black method: present value volatility sensitivity", (pvP.getCurrencyAmounts()[0].getAmount() - pvM.getCurrencyAmounts()[0].getAmount()) / (2 * shift),
        volatilitySensitivity, TOLERANCE_DELTA);
  }

  //  @Test
  //  /**
  //   * Tests the Black volatility sensitivity (vega).
  //   */
  //  public void presentValueBlackSensitivityMethodVsCalculator() {
  //    final PresentValueBlackSwaptionSensitivity pvbsMethod = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
  //    final PresentValueBlackSwaptionSensitivity pvbsCalculator = PVBSC_BLACK.visit(SWAPTION_LONG_REC, CURVES_BLACK);
  //    assertEquals("Swaption Black method: present value", pvbsMethod, pvbsCalculator);
  //  }

  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackNodeSensitivity() {
    final double shift = 1.0E-6;
    final PresentValueBlackSwaptionSensitivity pvbvs = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    final PresentValueBlackSwaptionSensitivity pvbns = BSSNC.calculateNodeSensitivities(pvbvs, BLACK);
    final double[] x = ((InterpolatedDoublesSurface) BLACK.getVolatilitySurface()).getXDataAsPrimitive();
    final double[] y = ((InterpolatedDoublesSurface) BLACK.getVolatilitySurface()).getYDataAsPrimitive();
    for (int loopindex = 0; loopindex < x.length; loopindex++) {
      final BlackFlatSwaptionParameters blackP = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(loopindex, shift);
      final BlackSwaptionFlatProvider curvesBlackP = new BlackSwaptionFlatProvider(MULTICURVES, blackP);
      final MultipleCurrencyAmount pvP = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackP);
      final BlackFlatSwaptionParameters blackM = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(loopindex, -shift);
      final BlackSwaptionFlatProvider curvesBlackM = new BlackSwaptionFlatProvider(MULTICURVES, blackM);
      final MultipleCurrencyAmount pvM = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackM);
      assertEquals("Swaption Black method: present value volatility sensitivity", (pvP.getCurrencyAmounts()[0].getAmount() - pvM.getCurrencyAmounts()[0].getAmount()) / (2 * shift), pvbns
          .getSensitivity().getMap().get(new DoublesPair(x[loopindex], y[loopindex])), TOLERANCE_DELTA);
    }
  }

}
