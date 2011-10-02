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
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory;
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
  private static final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);
  private static final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> INTERPOLATOR_SENSITIVITIES = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
      .getSensitivityCalculator(Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, false);
  private static final NewtonVectorRootFinder ROOT_FINDER = new BroydenVectorRootFinder(1e-8, 1e-8, 1000);
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
  private static final String[] DOUBLE_CURVE_NAMES = new String[]{"Funding", "Libor"};
  private static final ZonedDateTime SWAP_FIXING_DATE = DateUtils.getUTCDate(2010, 12, 30);
  private static final List<InterestRateDerivative> SINGLE_CURVE_IRD = makeSingleCurveIRD(SINGLE_CURVE_MARKET_RATES);
  private static final YieldCurveFittingTestDataBundle SINGLE_CURVE_PAR_RATE_DATA = getSingleCurveSetup(ParRateCalculator.getInstance(), ParRateCurveSensitivityCalculator.getInstance(), SINGLE_CURVE_MARKET_RATES,
      SINGLE_CURVE_IRD, false);
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> SINGLE_CURVE_PAR_RATE_FUNCTION = new MultipleYieldCurveFinderFunction(SINGLE_CURVE_PAR_RATE_DATA, SINGLE_CURVE_PAR_RATE_DATA.getMarketValueCalculator());
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> SINGLE_CURVE_PAR_RATE_JACOBIAN_FUNCTION;
  private static final DoubleMatrix1D SINGLE_CURVE_PAR_RATE_YIELD_CURVE_NODES;
  private static final DoubleMatrix2D SINGLE_CURVE_PAR_RATE_JACOBIAN;
  private static final YieldCurveBundle SINGLE_CURVE_PAR_RATE_CURVES;
  private static final YieldCurveBundle SINGLE_CURVE_PAR_RATE_ALL_CURVES;
  private static final YieldCurveFittingTestDataBundle SINGLE_CURVE_PV_DATA = getSingleCurveSetup(PresentValueCalculator.getInstance(), PresentValueSensitivityCalculator.getInstance(), SINGLE_CURVE_MARKET_RATES,
      SINGLE_CURVE_IRD, true);
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> SINGLE_CURVE_PV_FUNCTION = new MultipleYieldCurveFinderFunction(SINGLE_CURVE_PV_DATA, SINGLE_CURVE_PV_DATA.getMarketValueCalculator());
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> SINGLE_CURVE_PV_JACOBIAN_FUNCTION;
  private static final DoubleMatrix1D SINGLE_CURVE_PV_YIELD_CURVE_NODES;
  private static final DoubleMatrix2D SINGLE_CURVE_PV_JACOBIAN;
  private static final YieldCurveBundle SINGLE_CURVE_PV_CURVES;
  private static final YieldCurveBundle SINGLE_CURVE_PV_ALL_CURVES;
  private static final DoubleMatrix1D SINGLE_CURVE_PV_COUPON_SENSITIVITY;

 
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
    final double[] couponSensitivityArray = new double[SINGLE_CURVE_PV_DATA.getNumInstruments()];
    for (int i = 0; i < SINGLE_CURVE_PV_DATA.getNumInstruments(); i++) {
      couponSensitivityArray[i] = PVCS.visit(SINGLE_CURVE_PV_DATA.getDerivative(i), SINGLE_CURVE_PV_ALL_CURVES);
    }
    SINGLE_CURVE_PV_COUPON_SENSITIVITY = new DoubleMatrix1D(couponSensitivityArray);
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
  public void testWithParRate() {
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
  public void testwithPV() {
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
  public void testBumpedData() {
    final double notional = 10394850;
    final double eps = 1e-3;
    final InterestRateDerivative cash = makeCashDefinition(DateUtils.getUTCDate(2013, 6, 1), 0.03445, notional, SINGLE_CURVE_NAME);
    testBumpedDataParRateMethod(cash, notional, eps);
    testBumpedDataPVMethod(cash, notional, eps);
    InterestRateDerivative fra = makeFRADefinition(DateUtils.getUTCDate(2014, 3, 3), DateUtils.getUTCDate(2014, 9, 3), 0.04, notional, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME);
    testBumpedDataParRateMethod(fra, notional, eps);
    testBumpedDataPVMethod(fra, notional, eps);
    InterestRateDerivative swap = makeSwapDefinition(DateUtils.getUTCDate(2020, 4, 2), 0.05, notional, SINGLE_CURVE_NAME, SINGLE_CURVE_NAME);
    testBumpedDataParRateMethod(swap, notional, eps);
    testBumpedDataPVMethod(swap, notional, eps);
  }
  
  private void testBumpedDataParRateMethod(final InterestRateDerivative ird, final double notional, final double eps) {
    final DoubleMatrix1D sensitivities = ISC.calculateFromParRate(ird, null, SINGLE_CURVE_PAR_RATE_CURVES, SINGLE_CURVE_PAR_RATE_JACOBIAN, PVNS);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double pv1 = calculator.visit(ird, SINGLE_CURVE_PAR_RATE_ALL_CURVES);
    for (int i = 0; i < sensitivities.getNumberOfElements(); i++) {
      double[] bumpedData = getBumpedData(i, eps);
      List<InterestRateDerivative> bumpedIRD = makeSingleCurveIRD(bumpedData);
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
  
  private void testBumpedDataPVMethod(final InterestRateDerivative ird, final double notional, final double eps) {
    final DoubleMatrix1D sensitivities = ISC.calculateFromPresentValue(ird, null, SINGLE_CURVE_PV_CURVES, SINGLE_CURVE_PV_COUPON_SENSITIVITY, SINGLE_CURVE_PV_JACOBIAN, PVNS);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double pv1 = calculator.visit(ird, SINGLE_CURVE_PV_ALL_CURVES);
    for (int i = 0; i < sensitivities.getNumberOfElements(); i++) {
      double[] bumpedData = getBumpedData(i, eps);
      List<InterestRateDerivative> bumpedIRD = makeSingleCurveIRD(bumpedData);
      final YieldCurveFittingTestDataBundle bumpedDataBundle = getSingleCurveSetup(PresentValueCalculator.getInstance(), PresentValueSensitivityCalculator.getInstance(), bumpedData, bumpedIRD, true);
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

  private static double[] getBumpedData(final int n, final double eps) {
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
  
  private static final YieldCurveFittingTestDataBundle getSingleCurveSetup(final InterestRateDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator, double[] marketRates,
      List<InterestRateDerivative> instruments, final boolean isPV) {
    int nNodes = marketRates.length;
    double[] marketValues = new double[nNodes];
    double[] nodes = new double[nNodes];
    for(int i = 0; i < nNodes; i++) {
      marketValues[i] = isPV ? 0 : marketRates[i];
      nodes[i] = MATURITY_CALCULATOR.visit(SINGLE_CURVE_IRD.get(i));
    }
    List<double[]> curveKnots = Arrays.asList(nodes);
    List<String> curveNames = Arrays.asList(SINGLE_CURVE_NAME);
    final double[] rates = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rates[i] = 0.04;
    }
    rates[0] = 0.02;
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);
    final YieldCurveFittingTestDataBundle data = getYieldCurveFittingTestDataBundle(instruments, null, curveNames, curveKnots, INTERPOLATOR, INTERPOLATOR_SENSITIVITIES,
        calculator, sensitivityCalculator, marketValues, startPosition, null);
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

  private static List<InterestRateDerivative> makeSingleCurveIRD(double[] marketRates) {
    List<InterestRateDerivative> ird = new ArrayList<InterestRateDerivative>();
    ird = new ArrayList<InterestRateDerivative>();
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
  
  @SuppressWarnings("unused")
  private static Map<String, List<InterestRateDerivative>> makeDoubleCurveIRD(double[] fundingMarketRates, double[] forwardMarketRates) {
    Map<String, List<InterestRateDerivative>> instruments = new LinkedHashMap<String, List<InterestRateDerivative>>();
    List<InterestRateDerivative> funding = new ArrayList<InterestRateDerivative>();
    funding = new ArrayList<InterestRateDerivative>();
    funding.add(makeCashDefinition(DateUtils.getUTCDate(2011, 4, 3), fundingMarketRates[0], 1, DOUBLE_CURVE_NAMES[0]));
    funding.add(makeFRADefinition(DateUtils.getUTCDate(2011, 4, 3), DateUtils.getUTCDate(2011, 7, 3), fundingMarketRates[1], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeFRADefinition(DateUtils.getUTCDate(2011, 7, 3), DateUtils.getUTCDate(2011, 10, 3), fundingMarketRates[2], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2012, 1, 3), fundingMarketRates[3], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2013, 1, 3), fundingMarketRates[4], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2014, 1, 3), fundingMarketRates[5], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2015, 1, 3), fundingMarketRates[6], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2016, 1, 3), fundingMarketRates[7], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2017, 1, 3), fundingMarketRates[8], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2018, 1, 3), fundingMarketRates[9], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2021, 1, 3), fundingMarketRates[10], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2026, 1, 3), fundingMarketRates[11], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2031, 1, 3), fundingMarketRates[12], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2036, 1, 3), fundingMarketRates[13], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    funding.add(makeSwapDefinition(DateUtils.getUTCDate(2041, 1, 3), fundingMarketRates[14], 1, DOUBLE_CURVE_NAMES[0], DOUBLE_CURVE_NAMES[1]));
    instruments.put(DOUBLE_CURVE_NAMES[0], funding);
    return instruments;
  }
}
