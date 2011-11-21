/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static com.opengamma.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.instrument.Convention;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.YieldCurveFittingSetup;
import com.opengamma.math.rootfinding.YieldCurveFittingTestDataBundle;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InstrumentDefinitionYieldCurveSensitivitiesTest extends YieldCurveFittingSetup {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentDefinitionYieldCurveSensitivitiesTest.class);
  private static final int WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;
  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);
  private static final NewtonVectorRootFinder ROOT_FINDER = new BroydenVectorRootFinder(1e-8, 1e-8, 10000, DecompositionFactory.SV_COLT);
  private static final InstrumentSensitivityCalculator ISC = InstrumentSensitivityCalculator.getInstance();
  private static final PresentValueCouponSensitivityCalculator PVCS = PresentValueCouponSensitivityCalculator.getInstance();
  private static final PresentValueNodeSensitivityCalculator PVNS = PresentValueNodeSensitivityCalculator.getDefaultInstance();
  private static final LastDateCalculator MATURITY_CALCULATOR = LastDateCalculator.getInstance();
  private static final Currency CCY = Currency.USD;
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 1, 3);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final MondayToFridayCalendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Convention CONVENTION = new Convention(0, DAY_COUNT, BUSINESS_DAY, CALENDAR, "CONVENTION");
  private static final IborIndex IBOR = new IborIndex(CCY, Period.ofMonths(3), 2, CALENDAR, DAY_COUNT, BUSINESS_DAY, false);
  private static final double[] SINGLE_CURVE_MARKET_RATES = {0.02, 0.0366, 0.04705, 0.04285, 0.03953, 0.03986, 0.040965, 0.042035, 0.04314, 0.044, 0.046045, 0.048085, 0.048925, 0.049155, 0.049195};
  private static final String SINGLE_CURVE_NAME = "single"; 
  private static final ZonedDateTime SWAP_FIXING_DATE = DateUtils.getUTCDate(2010, 12, 30);
  private static final List<InstrumentDerivative> SINGLE_CURVE_IRD = makeSingleCurveIRD(SINGLE_CURVE_MARKET_RATES);
  private static final YieldCurveFittingTestDataBundle SINGLE_CURVE_PAR_RATE_DATA = getSingleCurveSetup(ParRateCalculator.getInstance(), ParRateCurveSensitivityCalculator.getInstance(), SINGLE_CURVE_MARKET_RATES,
      SINGLE_CURVE_IRD, false);
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> SINGLE_CURVE_PAR_RATE_FUNCTION = new MultipleYieldCurveFinderFunction(SINGLE_CURVE_PAR_RATE_DATA, SINGLE_CURVE_PAR_RATE_DATA.getMarketValueCalculator());
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> SINGLE_CURVE_PAR_RATE_JACOBIAN_FUNCTION;
  private static final DoubleMatrix1D SINGLE_CURVE_PAR_RATE_YIELD_CURVE_NODES;
  private static final DoubleMatrix2D SINGLE_CURVE_PAR_RATE_JACOBIAN;
  private static final YieldCurveBundle SINGLE_CURVE_PAR_RATE_CURVES;
  private static final YieldCurveBundle SINGLE_CURVE_PAR_RATE_ALL_CURVES;
  private static final YieldCurveFittingTestDataBundle SINGLE_CURVE_PV_DATA = getSingleCurveSetup(PresentValueCalculator.getInstance(), PresentValueCurveSensitivityCalculator.getInstance(), SINGLE_CURVE_MARKET_RATES,
      SINGLE_CURVE_IRD, true);
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> SINGLE_CURVE_PV_FUNCTION = new MultipleYieldCurveFinderFunction(SINGLE_CURVE_PV_DATA, SINGLE_CURVE_PV_DATA.getMarketValueCalculator());
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> SINGLE_CURVE_PV_JACOBIAN_FUNCTION;
  private static final DoubleMatrix1D SINGLE_CURVE_PV_YIELD_CURVE_NODES;
  private static final DoubleMatrix2D SINGLE_CURVE_PV_JACOBIAN;
  private static final YieldCurveBundle SINGLE_CURVE_PV_CURVES;
  private static final YieldCurveBundle SINGLE_CURVE_PV_ALL_CURVES;
  private static final DoubleMatrix1D SINGLE_CURVE_PV_COUPON_SENSITIVITY;
  private static final String[] DOUBLE_CURVE_NAMES = new String[]{"Funding", "Libor"};
  private static double[] DOUBLE_CURVE_MARKET_RATES = new double[28];
  private static List<InstrumentDerivative> DOUBLE_CURVE_IRD = new ArrayList<InstrumentDerivative>();
  private static List<double[]> DOUBLE_CURVE_NODES = new ArrayList<double[]>();
  private static final YieldCurveFittingTestDataBundle DOUBLE_CURVE_PAR_RATE_DATA;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> DOUBLE_CURVE_PAR_RATE_FUNCTION;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> DOUBLE_CURVE_PAR_RATE_JACOBIAN_FUNCTION;
  private static final DoubleMatrix1D DOUBLE_CURVE_PAR_RATE_YIELD_CURVE_NODES;
  private static final DoubleMatrix2D DOUBLE_CURVE_PAR_RATE_JACOBIAN;
  private static final YieldCurveBundle DOUBLE_CURVE_PAR_RATE_CURVES;
  private static final YieldCurveBundle DOUBLE_CURVE_PAR_RATE_ALL_CURVES;
  private static final YieldCurveFittingTestDataBundle DOUBLE_CURVE_PV_DATA;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> DOUBLE_CURVE_PV_FUNCTION;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> DOUBLE_CURVE_PV_JACOBIAN_FUNCTION;
  private static final DoubleMatrix1D DOUBLE_CURVE_PV_YIELD_CURVE_NODES;
  private static final DoubleMatrix2D DOUBLE_CURVE_PV_JACOBIAN;
  private static final YieldCurveBundle DOUBLE_CURVE_PV_CURVES;
  private static final YieldCurveBundle DOUBLE_CURVE_PV_ALL_CURVES;
  private static final DoubleMatrix1D DOUBLE_CURVE_PV_COUPON_SENSITIVITY;
  
  static {
    SINGLE_CURVE_PAR_RATE_JACOBIAN_FUNCTION = new MultipleYieldCurveFinderJacobian(SINGLE_CURVE_PAR_RATE_DATA, SINGLE_CURVE_PAR_RATE_DATA.getMarketValueSensitivityCalculator());
    SINGLE_CURVE_PAR_RATE_YIELD_CURVE_NODES = ROOT_FINDER.getRoot(SINGLE_CURVE_PAR_RATE_FUNCTION, SINGLE_CURVE_PAR_RATE_JACOBIAN_FUNCTION, SINGLE_CURVE_PAR_RATE_DATA.getStartPosition());
    SINGLE_CURVE_PAR_RATE_JACOBIAN = SINGLE_CURVE_PAR_RATE_JACOBIAN_FUNCTION.evaluate(SINGLE_CURVE_PAR_RATE_YIELD_CURVE_NODES);
    SINGLE_CURVE_PAR_RATE_CURVES = getYieldCurveMap(SINGLE_CURVE_PAR_RATE_DATA, SINGLE_CURVE_PAR_RATE_YIELD_CURVE_NODES);
    SINGLE_CURVE_PAR_RATE_ALL_CURVES = getAllCurves(SINGLE_CURVE_PAR_RATE_DATA, SINGLE_CURVE_PAR_RATE_CURVES);
    SINGLE_CURVE_PV_JACOBIAN_FUNCTION = new MultipleYieldCurveFinderJacobian(SINGLE_CURVE_PV_DATA, SINGLE_CURVE_PV_DATA.getMarketValueSensitivityCalculator());
    SINGLE_CURVE_PV_YIELD_CURVE_NODES = ROOT_FINDER.getRoot(SINGLE_CURVE_PV_FUNCTION, SINGLE_CURVE_PV_JACOBIAN_FUNCTION, SINGLE_CURVE_PV_DATA.getStartPosition());
    SINGLE_CURVE_PV_JACOBIAN = SINGLE_CURVE_PV_JACOBIAN_FUNCTION.evaluate(SINGLE_CURVE_PV_YIELD_CURVE_NODES);
    SINGLE_CURVE_PV_CURVES = getYieldCurveMap(SINGLE_CURVE_PV_DATA, SINGLE_CURVE_PV_YIELD_CURVE_NODES);
    SINGLE_CURVE_PV_ALL_CURVES = getAllCurves(SINGLE_CURVE_PV_DATA, SINGLE_CURVE_PV_CURVES);
    double[] couponSensitivityArray = new double[SINGLE_CURVE_PV_DATA.getNumInstruments()];
    for (int i = 0; i < SINGLE_CURVE_PV_DATA.getNumInstruments(); i++) {
      couponSensitivityArray[i] = PVCS.visit(SINGLE_CURVE_PV_DATA.getDerivative(i), SINGLE_CURVE_PV_ALL_CURVES);
    }
    SINGLE_CURVE_PV_COUPON_SENSITIVITY = new DoubleMatrix1D(couponSensitivityArray);
    initDoubleCurveMarketCurveData(DOUBLE_CURVE_MARKET_RATES, DOUBLE_CURVE_IRD, DOUBLE_CURVE_NODES);
    DOUBLE_CURVE_PAR_RATE_DATA = getDoubleCurveSetup(ParRateCalculator.getInstance(), ParRateCurveSensitivityCalculator.getInstance(), DOUBLE_CURVE_MARKET_RATES,
        DOUBLE_CURVE_IRD, DOUBLE_CURVE_NODES, false);
    DOUBLE_CURVE_PAR_RATE_FUNCTION = new MultipleYieldCurveFinderFunction(DOUBLE_CURVE_PAR_RATE_DATA, DOUBLE_CURVE_PAR_RATE_DATA.getMarketValueCalculator());
    DOUBLE_CURVE_PAR_RATE_JACOBIAN_FUNCTION = new MultipleYieldCurveFinderJacobian(DOUBLE_CURVE_PAR_RATE_DATA, DOUBLE_CURVE_PAR_RATE_DATA.getMarketValueSensitivityCalculator());
    DOUBLE_CURVE_PAR_RATE_YIELD_CURVE_NODES = ROOT_FINDER.getRoot(DOUBLE_CURVE_PAR_RATE_FUNCTION, DOUBLE_CURVE_PAR_RATE_JACOBIAN_FUNCTION, DOUBLE_CURVE_PAR_RATE_DATA.getStartPosition());
    DOUBLE_CURVE_PAR_RATE_JACOBIAN = DOUBLE_CURVE_PAR_RATE_JACOBIAN_FUNCTION.evaluate(DOUBLE_CURVE_PAR_RATE_YIELD_CURVE_NODES);
    DOUBLE_CURVE_PAR_RATE_CURVES = getYieldCurveMap(DOUBLE_CURVE_PAR_RATE_DATA, DOUBLE_CURVE_PAR_RATE_YIELD_CURVE_NODES);
    DOUBLE_CURVE_PAR_RATE_ALL_CURVES = getAllCurves(DOUBLE_CURVE_PAR_RATE_DATA, DOUBLE_CURVE_PAR_RATE_CURVES);
    DOUBLE_CURVE_PV_DATA = getDoubleCurveSetup(PresentValueCalculator.getInstance(), PresentValueCurveSensitivityCalculator.getInstance(), DOUBLE_CURVE_MARKET_RATES,
        DOUBLE_CURVE_IRD, DOUBLE_CURVE_NODES, true);
    DOUBLE_CURVE_PV_FUNCTION = new MultipleYieldCurveFinderFunction(DOUBLE_CURVE_PV_DATA, DOUBLE_CURVE_PV_DATA.getMarketValueCalculator());
    DOUBLE_CURVE_PV_JACOBIAN_FUNCTION = new MultipleYieldCurveFinderJacobian(DOUBLE_CURVE_PV_DATA, DOUBLE_CURVE_PV_DATA.getMarketValueSensitivityCalculator());
    DOUBLE_CURVE_PV_YIELD_CURVE_NODES = ROOT_FINDER.getRoot(DOUBLE_CURVE_PV_FUNCTION, DOUBLE_CURVE_PV_JACOBIAN_FUNCTION, DOUBLE_CURVE_PV_DATA.getStartPosition());
    DOUBLE_CURVE_PV_JACOBIAN = DOUBLE_CURVE_PV_JACOBIAN_FUNCTION.evaluate(DOUBLE_CURVE_PV_YIELD_CURVE_NODES);
    DOUBLE_CURVE_PV_CURVES = getYieldCurveMap(DOUBLE_CURVE_PV_DATA, DOUBLE_CURVE_PV_YIELD_CURVE_NODES);
    DOUBLE_CURVE_PV_ALL_CURVES = getAllCurves(DOUBLE_CURVE_PV_DATA, DOUBLE_CURVE_PV_CURVES);
    couponSensitivityArray = new double[DOUBLE_CURVE_PV_DATA.getNumInstruments()];
    for (int i = 0; i < DOUBLE_CURVE_PV_DATA.getNumInstruments(); i++) {
      couponSensitivityArray[i] = PVCS.visit(DOUBLE_CURVE_PV_DATA.getDerivative(i), DOUBLE_CURVE_PV_ALL_CURVES);
    }
    DOUBLE_CURVE_PV_COUPON_SENSITIVITY = new DoubleMatrix1D(couponSensitivityArray);
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected int getWarmupCycles() {
    return WARMUP_CYCLES;
  }

  @Override
  protected int getBenchmarkCycles() {
    return BENCHMARK_CYCLES;
  }

  @Test
  public void testSingleCurveWithParRate() {
    for (int i = 0; i < SINGLE_CURVE_PAR_RATE_DATA.getNumInstruments(); i++) {
      final DoubleMatrix1D bucketedDelta = ISC.calculateFromParRate(SINGLE_CURVE_PAR_RATE_DATA.getDerivative(i), SINGLE_CURVE_PAR_RATE_DATA.getKnownCurves(), SINGLE_CURVE_PAR_RATE_CURVES, SINGLE_CURVE_PAR_RATE_JACOBIAN, PVNS);
      final double sensitivity = PVCS.visit(SINGLE_CURVE_PAR_RATE_DATA.getDerivative(i), SINGLE_CURVE_PAR_RATE_ALL_CURVES);
      assertEquals(-sensitivity, bucketedDelta.getEntry(i), 1e-8);
      for (int j = 0; j < SINGLE_CURVE_PAR_RATE_DATA.getNumInstruments(); j++) {
        if (j != i) {
          assertEquals(0.0, bucketedDelta.getEntry(j), 1e-8);
        }
      }
    }
  }

  @Test
  public void testDoubleCurveWithParRate() {
    for (int i = 0; i < DOUBLE_CURVE_PAR_RATE_DATA.getNumInstruments(); i++) {
      final DoubleMatrix1D bucketedDelta = ISC.calculateFromParRate(DOUBLE_CURVE_PAR_RATE_DATA.getDerivative(i), DOUBLE_CURVE_PAR_RATE_DATA.getKnownCurves(), DOUBLE_CURVE_PAR_RATE_CURVES, DOUBLE_CURVE_PAR_RATE_JACOBIAN, PVNS);
      final double sensitivity = PVCS.visit(DOUBLE_CURVE_PAR_RATE_DATA.getDerivative(i), DOUBLE_CURVE_PAR_RATE_ALL_CURVES);
      assertEquals(-sensitivity, bucketedDelta.getEntry(i), 1e-8);
      for (int j = 0; j < DOUBLE_CURVE_PAR_RATE_DATA.getNumInstruments(); j++) {
        if (j != i) {
          assertEquals(0.0, bucketedDelta.getEntry(j), 1e-8);
        }
      }
    }
  }

  @Test
  public void testSingleCurveWithPV() {
    for (int i = 0; i < SINGLE_CURVE_PV_DATA.getNumInstruments(); i++) {
      final DoubleMatrix1D bucketedDelta = ISC.calculateFromPresentValue(SINGLE_CURVE_PV_DATA.getDerivative(i), SINGLE_CURVE_PV_DATA.getKnownCurves(), SINGLE_CURVE_PV_CURVES, SINGLE_CURVE_PV_COUPON_SENSITIVITY, SINGLE_CURVE_PV_JACOBIAN, PVNS);
      assertEquals(-SINGLE_CURVE_PV_COUPON_SENSITIVITY.getEntry(i), bucketedDelta.getEntry(i), 1e-8);
      for (int j = 0; j < SINGLE_CURVE_PV_DATA.getNumInstruments(); j++) {
        if (j != i) {
          assertEquals(0.0, bucketedDelta.getEntry(j), 1e-8);
        }
      }
    }
  }

  @Test
  public void testDoubleCurveWithPV() {
    for (int i = 0; i < DOUBLE_CURVE_PV_DATA.getNumInstruments(); i++) {
      final DoubleMatrix1D bucketedDelta = ISC.calculateFromPresentValue(DOUBLE_CURVE_PV_DATA.getDerivative(i), DOUBLE_CURVE_PV_DATA.getKnownCurves(), DOUBLE_CURVE_PV_CURVES, DOUBLE_CURVE_PV_COUPON_SENSITIVITY, DOUBLE_CURVE_PV_JACOBIAN, PVNS);
      assertEquals(-DOUBLE_CURVE_PV_COUPON_SENSITIVITY.getEntry(i), bucketedDelta.getEntry(i), 1e-8);
      for (int j = 0; j < DOUBLE_CURVE_PV_DATA.getNumInstruments(); j++) {
        if (j != i) {
          assertEquals(0.0, bucketedDelta.getEntry(j), 1e-8);
        }
      }
    }
  }
  
  @Test
  public void testSingleCurveWithBumpedData() {
    final double notional = 1.5e7;
    final double eps = 1e-3;
    final InstrumentDerivative cash = makeCashDefinition(DateUtils.getUTCDate(2013, 6, 1), 0.03445, notional, SINGLE_CURVE_NAME);
    testSingleCurveBumpedDataParRateMethod(cash, eps);
    testSingleCurveBumpedDataPVMethod(cash, eps);
    InstrumentDerivative fra = makeFRADefinition(DateUtils.getUTCDate(2014, 3, 3), DateUtils.getUTCDate(2014, 9, 3), 0.04, notional, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME);
    testSingleCurveBumpedDataParRateMethod(fra, eps);
    testSingleCurveBumpedDataPVMethod(fra, eps);
    InstrumentDerivative swap = makeSwapDefinition(DateUtils.getUTCDate(2020, 4, 2), 0.05, notional, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME);
    testSingleCurveBumpedDataParRateMethod(swap, eps);
    testSingleCurveBumpedDataPVMethod(swap, eps);
  }
  
  @Test
  public void testDoubleCurveWithBumpedData() {
    double notional = 2.3e5;
    double eps = 1e-4;
    InstrumentDerivative cash = makeCashDefinition(DateUtils.getUTCDate(2011, 4, 6), 0.003, notional, DOUBLE_CURVE_NAMES[0]);
    testDoubleCurveBumpedDataParRateMethod(cash, eps);
    InstrumentDerivative libor = makeCashDefinition(DateUtils.getUTCDate(2012, 4, 6), 0.003, notional, DOUBLE_CURVE_NAMES[1]);
    testDoubleCurveBumpedDataParRateMethod(libor, eps);
    InstrumentDerivative fra = makeFRADefinition(DateUtils.getUTCDate(2013, 1, 3), DateUtils.getUTCDate(2013, 10, 3), 0.03, notional, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    testDoubleCurveBumpedDataParRateMethod(fra, eps);
    InstrumentDerivative swap = makeSwapDefinition(DateUtils.getUTCDate(2022, 1, 3), 0.05, notional, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    testDoubleCurveBumpedDataParRateMethod(swap, eps);    
    swap = makeSwapDefinition(DateUtils.getUTCDate(2022, 1, 3), 0.05, notional, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]);
    testDoubleCurveBumpedDataParRateMethod(swap, eps);    
    swap = makeSwapDefinition(DateUtils.getUTCDate(2022, 1, 3), 0.05, notional, DOUBLE_CURVE_NAMES[1], DOUBLE_CURVE_NAMES[1]);
    testDoubleCurveBumpedDataParRateMethod(swap, eps);    
  }

  private void testSingleCurveBumpedDataParRateMethod(final InstrumentDerivative ird, final double eps) {
    final DoubleMatrix1D sensitivities = ISC.calculateFromParRate(ird, null, SINGLE_CURVE_PAR_RATE_CURVES, SINGLE_CURVE_PAR_RATE_JACOBIAN, PVNS);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double pv1 = calculator.visit(ird, SINGLE_CURVE_PAR_RATE_ALL_CURVES);
    for (int i = 0; i < sensitivities.getNumberOfElements(); i++) {
      double[] bumpedData = getSingleCurveBumpedData(i, eps);
      List<InstrumentDerivative> bumpedIRD = makeSingleCurveIRD(bumpedData);
      final YieldCurveFittingTestDataBundle bumpedDataBundle = getSingleCurveSetup(ParRateCalculator.getInstance(), ParRateCurveSensitivityCalculator.getInstance(), bumpedData, bumpedIRD, false);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> f = new MultipleYieldCurveFinderFunction(bumpedDataBundle, bumpedDataBundle.getMarketValueCalculator());
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jf = new MultipleYieldCurveFinderJacobian(bumpedDataBundle, bumpedDataBundle.getMarketValueSensitivityCalculator());
      final DoubleMatrix1D bumpedNodes = ROOT_FINDER.getRoot(f, jf, bumpedDataBundle.getStartPosition());
      final YieldCurveBundle bumpedCurves = getYieldCurveMap(bumpedDataBundle, bumpedNodes);
      final YieldCurveBundle allBumpedCurves = getAllCurves(bumpedDataBundle, bumpedCurves);
      final double pv2 = calculator.visit(ird, allBumpedCurves);
      final double delta = pv2 - pv1;
      if (Math.abs(sensitivities.getEntry(i)) > 1e-3) {
        assertEquals(0, (delta - sensitivities.getEntry(i) * eps) / sensitivities.getEntry(i), eps);
      } else {
        assertEquals(0, sensitivities.getEntry(i), 1e-4);
        assertEquals(0, delta, 1e-4);
      }
    }
  }
  
  private void testSingleCurveBumpedDataPVMethod(final InstrumentDerivative ird, final double eps) {
    final DoubleMatrix1D sensitivities = ISC.calculateFromPresentValue(ird, null, SINGLE_CURVE_PV_CURVES, SINGLE_CURVE_PV_COUPON_SENSITIVITY, SINGLE_CURVE_PV_JACOBIAN, PVNS);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double pv1 = calculator.visit(ird, SINGLE_CURVE_PV_ALL_CURVES);
    for (int i = 0; i < sensitivities.getNumberOfElements(); i++) {
      double[] bumpedData = getSingleCurveBumpedData(i, eps);
      List<InstrumentDerivative> bumpedIRD = makeSingleCurveIRD(bumpedData);
      final YieldCurveFittingTestDataBundle bumpedDataBundle = getSingleCurveSetup(PresentValueCalculator.getInstance(), PresentValueCurveSensitivityCalculator.getInstance(), bumpedData, bumpedIRD, true);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> f = new MultipleYieldCurveFinderFunction(bumpedDataBundle, bumpedDataBundle.getMarketValueCalculator());
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jf = new MultipleYieldCurveFinderJacobian(bumpedDataBundle, bumpedDataBundle.getMarketValueSensitivityCalculator());
      final DoubleMatrix1D bumpedNodes = ROOT_FINDER.getRoot(f, jf, bumpedDataBundle.getStartPosition());
      final YieldCurveBundle bumpedCurves = getYieldCurveMap(bumpedDataBundle, bumpedNodes);
      final YieldCurveBundle allBumpedCurves = getAllCurves(bumpedDataBundle, bumpedCurves);
      final double pv2 = calculator.visit(ird, allBumpedCurves);
      final double delta = pv2 - pv1;
      if (Math.abs(sensitivities.getEntry(i)) > 1e-3) {
        assertEquals(0, (delta - sensitivities.getEntry(i) * eps) / sensitivities.getEntry(i), eps);
      } else {
        assertEquals(0, sensitivities.getEntry(i), 1e-4);
        assertEquals(0, delta, 1e-4);
      }
    }
  }

  private void testDoubleCurveBumpedDataParRateMethod(final InstrumentDerivative ird, final double eps) {
    final DoubleMatrix1D sensitivities = ISC.calculateFromParRate(ird, null, DOUBLE_CURVE_PAR_RATE_CURVES, DOUBLE_CURVE_PAR_RATE_JACOBIAN, PVNS);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double pv1 = calculator.visit(ird, DOUBLE_CURVE_PAR_RATE_ALL_CURVES);
    for (int i = 0; i < sensitivities.getNumberOfElements(); i++) {
      double[] bumpedData = new double[28];
      List<InstrumentDerivative> bumpedIRD = new ArrayList<InstrumentDerivative>();
      initDoubleCurveBumpedCurveData(bumpedData, bumpedIRD, null, i, eps);
      final YieldCurveFittingTestDataBundle bumpedDataBundle = getDoubleCurveSetup(ParRateCalculator.getInstance(), ParRateCurveSensitivityCalculator.getInstance(), bumpedData, bumpedIRD, DOUBLE_CURVE_NODES, false);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> f = new MultipleYieldCurveFinderFunction(bumpedDataBundle, bumpedDataBundle.getMarketValueCalculator());
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jf = new MultipleYieldCurveFinderJacobian(bumpedDataBundle, bumpedDataBundle.getMarketValueSensitivityCalculator());
      final DoubleMatrix1D bumpedNodes = ROOT_FINDER.getRoot(f, jf, bumpedDataBundle.getStartPosition());
      final YieldCurveBundle bumpedCurves = getYieldCurveMap(bumpedDataBundle, bumpedNodes);
      final YieldCurveBundle allBumpedCurves = getAllCurves(bumpedDataBundle, bumpedCurves);
      final double pv2 = calculator.visit(ird, allBumpedCurves);
      final double delta = pv2 - pv1;
      if (Math.abs(sensitivities.getEntry(i)) > 1e-3) {
        assertEquals(0, (delta - sensitivities.getEntry(i) * eps) / sensitivities.getEntry(i), eps);
      } else {
        assertEquals(0, sensitivities.getEntry(i), 1e-3);
        assertEquals(0, delta, 1e-4);
      }
    }
  }

  private static double[] getSingleCurveBumpedData(final int n, final double eps) {
    double[] data = new double[SINGLE_CURVE_MARKET_RATES.length];
    for (int i = 0; i < SINGLE_CURVE_MARKET_RATES.length; i++) {
      if (i == n) {
        data[i] += SINGLE_CURVE_MARKET_RATES[i] + eps;
      } else {
        data[i] = SINGLE_CURVE_MARKET_RATES[i];
      }
    }
    return data;
  }
  
  private static final YieldCurveFittingTestDataBundle getSingleCurveSetup(final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator, double[] marketRates,
      List<InstrumentDerivative> instruments, final boolean isPV) {
    int nNodes = marketRates.length;
    double[] marketValues = new double[nNodes];
    double[] nodes = new double[nNodes];
    for(int i = 0; i < nNodes; i++) {
      marketValues[i] = isPV ? 0 : marketRates[i];
      nodes[i] = MATURITY_CALCULATOR.visit(instruments.get(i));
    }
    List<double[]> curveKnots = Arrays.asList(nodes);
    List<String> curveNames = Arrays.asList(SINGLE_CURVE_NAME);
    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.04;
    }
    rates[0] = 0.02;
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);
    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, curveNames, curveKnots, INTERPOLATOR,
        calculator, sensitivityCalculator, marketValues, startPosition, null, false);
    return data; 
  }
  
  private static final YieldCurveFittingTestDataBundle getDoubleCurveSetup(final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator, double[] marketRates,
      List<InstrumentDerivative> instruments, List<double[]> curveKnots, final boolean isPV) {
    int nNodes = marketRates.length;
    double[] marketValues = new double[nNodes];
    double[] nodes = new double[nNodes];
    for(int i = 0; i < nNodes; i++) {
      marketValues[i] = isPV ? 0 : marketRates[i];
      nodes[i] = MATURITY_CALCULATOR.visit(instruments.get(i));
    }
    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.04;
    }
    rates[0] = 0.02;
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);
    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, Arrays.asList(DOUBLE_CURVE_NAMES), curveKnots, INTERPOLATOR,
        calculator, sensitivityCalculator, marketValues, startPosition, null, false);
    return data; 
  }

  private static YieldCurveBundle getAllCurves(final YieldCurveFittingTestDataBundle data, final YieldCurveBundle curves) {
    final YieldCurveBundle allCurves = new YieldCurveBundle(curves);
    if (data.getKnownCurves() != null) {
      allCurves.addAll(data.getKnownCurves());
    }
    return allCurves;
  }

  private static YieldCurveBundle getYieldCurveMap(final YieldCurveFittingTestDataBundle data, final DoubleMatrix1D yieldCurveNodes) {
    final HashMap<String, double[]> yields = unpackYieldVector(data, yieldCurveNodes);
    final LinkedHashMap<String, YieldAndDiscountCurve> curves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    for (final String name : data.getCurveNames()) {
      final YieldAndDiscountCurve curve = makeYieldCurve(yields.get(name), data.getCurveNodePointsForCurve(name), data.getInterpolatorForCurve(name));
      curves.put(name, curve);
    }
    return new YieldCurveBundle(curves);
  }

  private static Cash makeCashDefinition(ZonedDateTime maturity, double rate, double notional, String curveName) {
    return new CashDefinition(CCY, maturity, notional, rate, CONVENTION).toDerivative(NOW, curveName);
  }
  
  private static Payment makeFRADefinition(ZonedDateTime accrualStart, ZonedDateTime accrualEnd, double rate, double notional, String fundingCurveName, String forwardCurveName) {
    return ForwardRateAgreementDefinition.from(accrualStart, accrualEnd, notional, IBOR, rate).toDerivative(NOW, fundingCurveName, forwardCurveName);
  }
  
  @SuppressWarnings("unchecked")
  private static Swap<?, ?> makeSwapDefinition(ZonedDateTime maturity, double rate, double notional, String fundingCurveName, String forwardCurveName) {
    return new SwapFixedIborDefinition(AnnuityCouponFixedDefinition.from(CCY, DateUtils.getUTCDate(2011, 1, 3), maturity, SimpleFrequency.SEMI_ANNUAL, CALENDAR, DAY_COUNT, BUSINESS_DAY, false, notional, rate, true),
                                       AnnuityCouponIborDefinition.from(DateUtils.getUTCDate(2011, 1, 3), maturity, notional, IBOR, false))
       .toDerivative(NOW, new DoubleTimeSeries[]{new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[]{SWAP_FIXING_DATE}, new double[]{rate})}, fundingCurveName, forwardCurveName);
  }

  private static List<InstrumentDerivative> makeSingleCurveIRD(double[] marketRates) {
    List<InstrumentDerivative> ird = new ArrayList<InstrumentDerivative>();
    ird = new ArrayList<InstrumentDerivative>();
    ird.add(makeCashDefinition(DateUtils.getUTCDate(2011, 4, 3), marketRates[0], 1, SINGLE_CURVE_NAME));
    ird.add(makeFRADefinition(DateUtils.getUTCDate(2011, 4, 3), DateUtils.getUTCDate(2011, 7, 3), marketRates[1], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeFRADefinition(DateUtils.getUTCDate(2011, 7, 3), DateUtils.getUTCDate(2011, 10, 3), marketRates[2], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2012, 1, 3), marketRates[3], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2013, 1, 3), marketRates[4], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2014, 1, 3), marketRates[5], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2015, 1, 3), marketRates[6], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2016, 1, 3), marketRates[7], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2017, 1, 3), marketRates[8], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2018, 1, 3), marketRates[9], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2021, 1, 3), marketRates[10], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2026, 1, 3), marketRates[11], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2031, 1, 3), marketRates[12], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2036, 1, 3), marketRates[13], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    ird.add(makeSwapDefinition(DateUtils.getUTCDate(2041, 1, 3), marketRates[14], 1, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME));
    return ird;
  }
    
  private static void initDoubleCurveMarketCurveData(double[] marketRates, List<InstrumentDerivative> instruments, List<double[]> curveNodes) {
    initDoubleCurveBumpedCurveData(marketRates, instruments, curveNodes, 0, 0);
  }
  
  private static void initDoubleCurveBumpedCurveData(double[] marketRates, List<InstrumentDerivative> instruments, List<double[]> curveNodes, int n, double eps) {
    @SuppressWarnings("synthetic-access")
    Function1D<Double, Double> dummyCurve = new DummyCurve1();
    @SuppressWarnings("synthetic-access")
    Function1D<Double, Double> spreadCurve = new DummySpreadCurve2();
    double[] fundingNodes = new double[10];
    double[] forwardNodes = new double[18];
    InstrumentDerivative ird = makeCashDefinition(DateUtils.getUTCDate(2011, 1, 4), 0, 1, DOUBLE_CURVE_NAMES[0]);
    double t = MATURITY_CALCULATOR.visit(ird);
    marketRates[0] = dummyCurve.evaluate(t) + (n == 0 ? eps : 0);
    fundingNodes[0] = t;
    instruments.add(makeCashDefinition(DateUtils.getUTCDate(2011, 1, 4), marketRates[0], 1, DOUBLE_CURVE_NAMES[0]));
    
    ird = makeCashDefinition(DateUtils.getUTCDate(2011, 1, 10), 0, 1, DOUBLE_CURVE_NAMES[0]);
    t = MATURITY_CALCULATOR.visit(ird);
    marketRates[1] = dummyCurve.evaluate(t) + (n == 1 ? eps : 0);
    fundingNodes[1] = t;
    instruments.add(makeCashDefinition(DateUtils.getUTCDate(2011, 1, 10), marketRates[1], 1, DOUBLE_CURVE_NAMES[0]));
    
    ird = makeCashDefinition(DateUtils.getUTCDate(2011, 1, 17), 0, 1, DOUBLE_CURVE_NAMES[0]);
    t = MATURITY_CALCULATOR.visit(ird);
    fundingNodes[2] = t;
    marketRates[2] = dummyCurve.evaluate(t) + (n == 2 ? eps : 0);
    instruments.add(makeCashDefinition(DateUtils.getUTCDate(2011, 1, 17), marketRates[2], 1, DOUBLE_CURVE_NAMES[0]));
    
    ird = makeSwapDefinition(DateUtils.getUTCDate(2012, 1, 4), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]);
    t = MATURITY_CALCULATOR.visit(ird);
    fundingNodes[3] = t;
    marketRates[3] = dummyCurve.evaluate(t) + (n == 3 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2012, 1, 4), marketRates[3], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]));
    
    ird = makeSwapDefinition(DateUtils.getUTCDate(2013, 1, 4), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]);
    t = MATURITY_CALCULATOR.visit(ird);
    fundingNodes[4] = t;
    marketRates[4] = dummyCurve.evaluate(t) + (n == 4 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2013, 1, 4), marketRates[4], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]));
    
    ird = makeSwapDefinition(DateUtils.getUTCDate(2016, 1, 4), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]);
    t = MATURITY_CALCULATOR.visit(ird);
    fundingNodes[5] = t;
    marketRates[5] = dummyCurve.evaluate(t) + (n == 5 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2016, 1, 4), marketRates[5], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]));
    
    ird = makeSwapDefinition(DateUtils.getUTCDate(2021, 1, 4), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]);
    t = MATURITY_CALCULATOR.visit(ird);
    fundingNodes[6] = t;
    marketRates[6] = dummyCurve.evaluate(t) + (n == 6 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2021, 1, 4), marketRates[6], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2031, 1, 4), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]);
    t = MATURITY_CALCULATOR.visit(ird);
    fundingNodes[7] = t;
    marketRates[7] = dummyCurve.evaluate(t) + (n == 7 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2031, 1, 4), marketRates[7], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]));
    
    ird = makeSwapDefinition(DateUtils.getUTCDate(2041, 1, 4), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]);
    t = MATURITY_CALCULATOR.visit(ird);
    fundingNodes[8] = t;
    marketRates[8] = dummyCurve.evaluate(t) + (n == 8 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2041, 1, 4), marketRates[8], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]));
    
    ird = makeSwapDefinition(DateUtils.getUTCDate(2051, 1, 4), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]);
    t = MATURITY_CALCULATOR.visit(ird);
    fundingNodes[9] = t;
    marketRates[9] = dummyCurve.evaluate(t) + (n == 9 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2051, 1, 4), marketRates[9], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[0]));
    if (curveNodes != null) {
      curveNodes.add(fundingNodes);
    }
    
    ird = makeCashDefinition(DateUtils.getUTCDate(2011, 2, 3), 0, 1, DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[0] = t;
    marketRates[10] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n ==  10 ? eps : 0);
    instruments.add(makeCashDefinition(DateUtils.getUTCDate(2011, 2, 3), marketRates[10], 1, DOUBLE_CURVE_NAMES[1]));

    ird = makeCashDefinition(DateUtils.getUTCDate(2011, 3, 3), 0, 1, DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[1] = t;
    marketRates[11] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 11 ? eps : 0);
    instruments.add(makeCashDefinition(DateUtils.getUTCDate(2011, 3, 3), marketRates[11], 1, DOUBLE_CURVE_NAMES[1]));

    ird = makeCashDefinition(DateUtils.getUTCDate(2011, 4, 3), 0, 1, DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[2] = t;
    marketRates[12] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 12 ? eps : 0);
    instruments.add(makeCashDefinition(DateUtils.getUTCDate(2011, 4, 3), marketRates[12], 1, DOUBLE_CURVE_NAMES[1]));

    ird = makeFRADefinition(DateUtils.getUTCDate(2011, 4, 3), DateUtils.getUTCDate(2011, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[3] = t;
    marketRates[13] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 13 ? eps : 0);
    instruments.add(makeFRADefinition(DateUtils.getUTCDate(2011, 4, 3), DateUtils.getUTCDate(2011, 7, 3), marketRates[13], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    
    ird = makeFRADefinition(DateUtils.getUTCDate(2011, 7, 3), DateUtils.getUTCDate(2011, 10, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[4] = t;
    marketRates[14] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 14 ? eps : 0);
    instruments.add(makeFRADefinition(DateUtils.getUTCDate(2011, 7, 3), DateUtils.getUTCDate(2011, 10, 3), marketRates[14], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2012, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[5] = t;
    marketRates[15] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 15 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2012, 7, 3), marketRates[15], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2013, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[6] = t;
    marketRates[16] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 16 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2013, 7, 3), marketRates[16], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2014, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[7] = t;
    marketRates[17] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 17 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2014, 7, 3), marketRates[17], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2015, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[8] = t;
    marketRates[18] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 18 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2015, 7, 3), marketRates[18], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2016, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[9] = t;
    marketRates[19] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 19 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2016, 7, 3), marketRates[19], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2018, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[10] = t;
    marketRates[20] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 20 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2018, 7, 3), marketRates[20], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2021, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[11] = t;
    marketRates[21] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 21 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2021, 7, 3), marketRates[21], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2026, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[12] = t;
    marketRates[22] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 22 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2026, 7, 3), marketRates[22], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2031, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[13] = t;
    marketRates[23] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 23 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2031, 7, 3), marketRates[23], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2036, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[14] = t;
    marketRates[24] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 24 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2036, 7, 3), marketRates[24], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2041, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[15] = t;
    marketRates[25] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 25 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2041, 7, 3), marketRates[25], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2046, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[16] = t;
    marketRates[26] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 26 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2046, 7, 3), marketRates[26], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));

    ird = makeSwapDefinition(DateUtils.getUTCDate(2061, 7, 3), 0, 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]);
    t = MATURITY_CALCULATOR.visit(ird);
    forwardNodes[17] = t;
    marketRates[27] = spreadCurve.evaluate(t) + dummyCurve.evaluate(t) + (n == 27 ? eps : 0);
    instruments.add(makeSwapDefinition(DateUtils.getUTCDate(2061, 7, 3), marketRates[27], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    if (curveNodes != null) {
      curveNodes.add(forwardNodes);
    }
  }

  private static class DummyCurve1 extends Function1D<Double, Double> {
    private static final double A = 0;
    private static final double B = 0.004148649;
    private static final double C = 0.056397936;
    private static final double D = 0.004457019;
    private static final double E = 0.000429628;
  
    @Override
    public Double evaluate(final Double x) {
      return (A + B * x) * Math.exp(-C * x) + E * x + D;
    }
  }
  
  private static class DummySpreadCurve2 extends Function1D<Double, Double> {
    private static final double A = 0.0025;
    private static final double B = 0.0021;
    private static final double C = 0.2;
  
    @Override
    public Double evaluate(final Double x) {
      return (A + B * x) * Math.exp(-C * x);
    }
  }    

}
