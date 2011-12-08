/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.RateReplacingInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.interestrate.swap.definition.CrossCurrencySwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.ForexForward;
import com.opengamma.financial.interestrate.swap.definition.OISSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.differentiation.FiniteDifferenceType;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.YieldCurveFittingTestDataBundle.TestType;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public abstract class YieldCurveFittingSetup {
  /** Random number generator */
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  /** Replaces rates */
  protected static final RateReplacingInterestRateDerivativeVisitor REPLACE_RATE = RateReplacingInterestRateDerivativeVisitor.getInstance();
  private static final Currency DUMMY_CUR = Currency.USD;
  private static final IborIndex DUMMY_INDEX = new IborIndex(DUMMY_CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
  private static final IndexON DUMMY_OIS_INDEX = new IndexON("OIS", DUMMY_CUR, DayCountFactory.INSTANCE.getDayCount("Actual/365"), 0, new MondayToFridayCalendar("A"));

  /** Accuracy */
  protected static final double EPS = 1e-8;
  /** Number of steps */
  protected static final int STEPS = 100;

  protected abstract Logger getLogger();

  protected abstract int getWarmupCycles();

  protected abstract int getBenchmarkCycles();

  protected static YieldCurveFittingTestDataBundle getYieldCurveFittingTestDataBundle(final List<InstrumentDerivative> instruments, final YieldCurveBundle knownCurves,
      final List<String> curveNames, final List<double[]> curvesKnots, final Interpolator1D extrapolator, final InstrumentDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator,
      final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator, final double[] marketRates, final DoubleMatrix1D startPosition,
      final List<double[]> curveYields) {
    return getYieldCurveFittingTestDataBundle(instruments, knownCurves, curveNames, curvesKnots, extrapolator, marketValueCalculator, marketValueSensitivityCalculator, marketRates, startPosition,
        curveYields, false);
  }

  protected static YieldCurveFittingTestDataBundle getYieldCurveFittingTestDataBundle(final List<InstrumentDerivative> instruments, final YieldCurveBundle knownCurves,
      final List<String> curveNames, final List<double[]> curvesKnots, final Interpolator1D extrapolator, final InstrumentDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator,
      final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator, final double[] marketRates, final DoubleMatrix1D startPosition,
      final List<double[]> curveYields, boolean useFiniteDifferenceByDefault) {

    Validate.notNull(curveNames);
    Validate.notNull(curvesKnots);
    Validate.notNull(instruments);
    Validate.notNull(extrapolator);

    final int n = curveNames.size();
    Validate.isTrue(n == curvesKnots.size());
    int count = 0;
    for (int i = 0; i < n; i++) {
      Validate.notNull(curvesKnots.get(i));
      count += curvesKnots.get(i).length;
    }
    Validate.isTrue(count <= instruments.size(), "more nodes than instruments");

    final LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators = new LinkedHashMap<String, Interpolator1D>();
    final LinkedHashMap<String, double[]> unknownCurveNodes = new LinkedHashMap<String, double[]>();

    for (int i = 0; i < n; i++) {
      unknownCurveInterpolators.put(curveNames.get(i), extrapolator);
      unknownCurveNodes.put(curveNames.get(i), curvesKnots.get(i));
    }
    if (curveYields == null) {
      return new YieldCurveFittingTestDataBundle(instruments, knownCurves, unknownCurveNodes, unknownCurveInterpolators, marketValueCalculator, marketValueSensitivityCalculator, marketRates,
          startPosition, useFiniteDifferenceByDefault);
    }

    Validate.isTrue(curveYields.size() == n, "wrong number of true yields");
    final HashMap<String, double[]> yields = new HashMap<String, double[]>();
    for (int i = 0; i < n; i++) {
      yields.put(curveNames.get(i), curveYields.get(i));
    }
    return new YieldCurveFittingTestDataBundle(instruments, knownCurves, unknownCurveNodes, unknownCurveInterpolators, marketValueCalculator, marketValueSensitivityCalculator, marketRates,
        startPosition, yields, useFiniteDifferenceByDefault);
  }

  public void doHotSpot(final NewtonVectorRootFinder rootFinder, final YieldCurveFittingTestDataBundle data, final String name) {
    for (int i = 0; i < getWarmupCycles(); i++) {
      doTestForCurveFinding(rootFinder, data);

    }
    if (getBenchmarkCycles() > 0) {
      final OperationTimer timer = new OperationTimer(getLogger(), "processing {} cycles on " + name, getBenchmarkCycles());
      for (int i = 0; i < getBenchmarkCycles(); i++) {
        doTestForCurveFinding(rootFinder, data);
      }
      timer.finished();
    }
  }

  private void doTestForCurveFinding(final NewtonVectorRootFinder rootFinder, final YieldCurveFittingTestDataBundle data) {

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new MultipleYieldCurveFinderFunction(data, data.getMarketValueCalculator());
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = null;

    if (data.getTestType() == TestType.ANALYTIC_JACOBIAN) {
      jac = new MultipleYieldCurveFinderJacobian(data, data.getMarketValueSensitivityCalculator());
    } else if (data.getTestType() == TestType.FD_JACOBIAN) {
      final VectorFieldFirstOrderDifferentiator fdJacCalculator = new VectorFieldFirstOrderDifferentiator();
      jac = fdJacCalculator.differentiate(func);
    } else {
      throw new IllegalArgumentException("unknown TestType " + data.getTestType());

    }

    //DoubleMatrix2D startJac = jac.evaluate(data.getStartPosition());
    //    System.out.print(startJac);

    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(func, jac, data.getStartPosition());
    final DoubleMatrix1D modelMarketValueDiff = func.evaluate(yieldCurveNodes);

    for (int i = 0; i < modelMarketValueDiff.getNumberOfElements(); i++) {
      assertEquals(0.0, modelMarketValueDiff.getEntry(i), EPS);
    }

    checkResult(yieldCurveNodes, data);

    //   DoubleMatrix2D endJac = jac.evaluate(yieldCurveNodes);

    //    System.out.print("\n");
    //    System.out.print(endJac);
  }

  protected void checkResult(final DoubleMatrix1D yieldCurveNodes, final YieldCurveFittingTestDataBundle data) {
    final HashMap<String, double[]> yields = unpackYieldVector(data, yieldCurveNodes);

    final YieldCurveBundle bundle = new YieldCurveBundle();
    for (final String name : data.getCurveNames()) {
      final YieldAndDiscountCurve curve = makeYieldCurve(yields.get(name), data.getCurveNodePointsForCurve(name), data.getInterpolatorForCurve(name));
      bundle.setCurve(name, curve);
    }
    if (data.getKnownCurves() != null) {
      bundle.addAll(data.getKnownCurves());
    }

    // this is possibly a redundant test, as the very fact that
    // the root finder converged (and modelMarketValueDiff are within EPS of 0)
    // means this will also pass
    for (int i = 0; i < data.getMarketRates().length; i++) {
      assertEquals(data.getMarketRates()[i], data.getMarketValueCalculator().visit(data.getDerivative(i), bundle), EPS);
    }

    // this test cannot be performed when we don't know what the true yield
    // curves are - i.e. we start from market data
    if (data.getCurveYields() != null) {
      for (final String name : data.getCurveNames()) {
        final double[] trueYields = data.getCurveYields().get(name);
        final double[] fittedYields = yields.get(name);
        for (int i = 0; i < trueYields.length; i++) {
          assertEquals(trueYields[i], fittedYields[i], EPS);
        }
      }
    }
  }

  protected static HashMap<String, double[]> unpackYieldVector(final YieldCurveFittingTestDataBundle data, final DoubleMatrix1D yieldCurveNodes) {

    final HashMap<String, double[]> res = new HashMap<String, double[]>();
    int start = 0;
    int end = 0;
    for (final String name : data.getCurveNames()) {
      end += data.getCurveNodePointsForCurve(name).length;
      final double[] temp = Arrays.copyOfRange(yieldCurveNodes.getData(), start, end);
      res.put(name, temp);
      start = end;
    }

    return res;
  }

  protected void assertJacobian(final YieldCurveFittingTestDataBundle data) {
    final MultipleYieldCurveFinderFunction func = new MultipleYieldCurveFinderFunction(data, data.getMarketValueCalculator());
    final MultipleYieldCurveFinderJacobian jac = new MultipleYieldCurveFinderJacobian(data, data.getMarketValueSensitivityCalculator());
    final VectorFieldFirstOrderDifferentiator fdCal = new VectorFieldFirstOrderDifferentiator(FiniteDifferenceType.CENTRAL, 1.0E-6);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFD = fdCal.differentiate(func);
    final DoubleMatrix2D jacExact = jac.evaluate(data.getStartPosition());
    final DoubleMatrix2D jacFD = jacobianFD.evaluate(data.getStartPosition());
    assertMatrixEquals(jacExact, jacFD, 1e-5);

  }

  protected static YieldAndDiscountCurve makeYieldCurve(final double[] yields, final double[] times, final Interpolator1D interpolator) {
    final int n = yields.length;
    if (n != times.length) {
      throw new IllegalArgumentException("rates and times different lengths");
    }
    return new YieldCurve(InterpolatedDoublesCurve.from(times, yields, interpolator));
  }

  protected static MultipleYieldCurveFinderDataBundle updateInstruments(final MultipleYieldCurveFinderDataBundle old, final List<InstrumentDerivative> instruments, final double[] marketRates) {
    Validate.isTrue(instruments.size() == marketRates.length);
    return new MultipleYieldCurveFinderDataBundle(instruments, marketRates, old.getKnownCurves(), old.getUnknownCurveNodePoints(), old.getUnknownCurveInterpolators(),
        old.useFiniteDifferenceForNodeSensitivities());
  }

  protected static InstrumentDerivative makeSingleCurrencyIRD(final String type, final double maturity, final SimpleFrequency paymentFreq, final String fundCurveName, final String indexCurveName,
      final double rate, final double notional) {
    if ("cash".equals(type)) {
      return makeCash(maturity, fundCurveName, rate, notional);
    } else if ("libor".equals(type)) {
      return makeLibor(maturity, indexCurveName, rate, notional);
    } else if ("fra".equals(type)) {
      return makeFRA(maturity, paymentFreq, fundCurveName, indexCurveName, rate, notional);
    } else if ("future".equals(type)) {
      return makeFuture(maturity, paymentFreq, fundCurveName, indexCurveName, rate, (int) notional);
    } else if ("swap".equals(type)) {
      return makeSwap(maturity, paymentFreq, fundCurveName, indexCurveName, rate, notional);
    } else if ("OIS".equals(type)) {
      return makeOISSwap(maturity, paymentFreq, fundCurveName, fundCurveName, rate, notional); //the OIS curve is the funding curve    
    } else if ("basisSwap".equals(type)) { //TODO remove basis swap from here  
      return makeBasisSwap(maturity, fundCurveName, indexCurveName, rate, notional);
    }
    throw new IllegalArgumentException("unknown IRD type " + type);
  }

  protected static InstrumentDerivative makeCash(final double time, final String fundCurveName, final double rate, final double notional) {
    return new Cash(DUMMY_CUR, time, notional, rate, fundCurveName);
  }

  protected static InstrumentDerivative makeLibor(final double time, final String indexCurveName, final double rate, final double notional) {
    return new Cash(DUMMY_CUR, time, notional, rate, indexCurveName);
  }

  /**
   * makes a very simple FRA with  payment time, fixing time and fixing period start being identical and an amount tau before fixing period end. The payment and fixing year fractions are
   * Identically equal to tau.   
   * @param time The fixing period end (the last relevant date for the FRA)
   * @param paymentFreq for a 3M FRA the payment freq is quarterly 
   * @param fundCurveName Name of funding curve
   * @param indexCurveName Name of index curve 
   * @param rate The FRA rate
   * @param notional the notional amount 
   * @return A FRA
   */
  protected static InstrumentDerivative makeFRA(final double time, final SimpleFrequency paymentFreq, final String fundCurveName, final String indexCurveName, final double rate,
      final double notional) {
    double tau = 1. / paymentFreq.getPeriodsPerYear();
    return new ForwardRateAgreement(DUMMY_CUR, time - tau, fundCurveName, tau, notional, DUMMY_INDEX, time - tau, time - tau, time, tau, rate, indexCurveName);
  }

  protected static InstrumentDerivative makeFuture(final double time, final SimpleFrequency paymentFreq, final String fundCurveName, final String indexCurveName, final double rate,
      final int contracts) {
    final double referencePrice = 0.0; // TODO CASE - Future refactor - Confirm referencePrice
    double tau = 1. / paymentFreq.getPeriodsPerYear();
    final InterestRateFuture underlyingFuture = new InterestRateFuture(time, DUMMY_INDEX, time, time + tau, tau, referencePrice, 1, tau, "N", fundCurveName, indexCurveName);

    return underlyingFuture; // TODO CASE - Future Refactor - Check whether rate is required here. It may well be. *Shrug*
  }

  protected static TenorSwap<CouponIbor> makeBasisSwap(final double time, final String fundCurveName, final String liborCurveName, final double rate, final double notional) {

    final int index = (int) Math.round(4 * time);
    final double[] paymentTimes = new double[index];

    final double[] spreads = new double[index];
    final double[] indexFixing = new double[index];
    final double[] indexMaturity = new double[index];
    final double[] yearFracs = new double[index];
    for (int i = 0; i < index; i++) {
      indexFixing[i] = 0.25 * i;
      paymentTimes[i] = 0.25 * (i + 1);
      indexMaturity[i] = paymentTimes[i];
      spreads[i] = rate;
      yearFracs[i] = 0.25;
    }
    final GenericAnnuity<CouponIbor> payLeg = new AnnuityCouponIbor(DUMMY_CUR, paymentTimes, DUMMY_INDEX, notional, fundCurveName, fundCurveName, true);
    final GenericAnnuity<CouponIbor> receiveLeg = new AnnuityCouponIbor(DUMMY_CUR, paymentTimes, indexFixing, DUMMY_INDEX, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, notional,
        fundCurveName, liborCurveName, false);
    return new TenorSwap<CouponIbor>(payLeg, receiveLeg);
  }

  protected static OISSwap makeOISSwap(final double time, SimpleFrequency paymentFreq, final String fundingCurveName, final String indexCurveName, final double rate, final double notional) {

    if (time < 1.0) {
      return makeSinglePaymentOISSwap(time, fundingCurveName, indexCurveName, rate, notional);
    }

    paymentFreq = SimpleFrequency.ANNUAL; //TODO even though this is only a test, this is a bad idea.

    final int index = (int) (time * paymentFreq.getPeriodsPerYear());
    final double[] paymentTimes = new double[index];
    double tau = 1. / paymentFreq.getPeriodsPerYear();

    final CouponOIS[] oisCoupons = new CouponOIS[index];
    for (int i = 0; i < index; i++) {
      paymentTimes[i] = tau * (i + 1);
      oisCoupons[i] = new CouponOIS(DUMMY_CUR, paymentTimes[i], fundingCurveName, tau, notional, DUMMY_OIS_INDEX, paymentTimes[i] - tau, paymentTimes[i], tau, notional, indexCurveName);
    }

    final AnnuityCouponFixed fixedLeg = new AnnuityCouponFixed(DUMMY_CUR, paymentTimes, notional, rate, fundingCurveName, true);
    final GenericAnnuity<CouponOIS> payLeg = new GenericAnnuity<CouponOIS>(oisCoupons);

    return new OISSwap(fixedLeg, payLeg);
  }

  protected static OISSwap makeSinglePaymentOISSwap(final double time, final String fundingCurveName, final String indexCurveName, final double rate, final double notional) {

    CouponOIS oisCoupon = new CouponOIS(DUMMY_CUR, time, fundingCurveName, time, notional, DUMMY_OIS_INDEX, 0, time, time, notional, indexCurveName);

    CouponFixed fixedCoupon = new CouponFixed(DUMMY_CUR, time, fundingCurveName, time, -notional, rate);

    AnnuityCouponFixed fixedLeg = new AnnuityCouponFixed(new CouponFixed[] {fixedCoupon});
    return new OISSwap(fixedLeg, new GenericAnnuity<CouponOIS>(new CouponOIS[] {oisCoupon}));
  }

  protected static FixedFloatSwap makeSwap(final double time, final SimpleFrequency floatLegFreq, final String fundingCurveName, final String liborCurveName, final double rate, final double notional) {

    int floatPayments = (int) (time * floatLegFreq.getPeriodsPerYear());
    Validate.isTrue(floatPayments % 2 == 0, "need even number of float payments as fixed payments at half frequency");
    int fixedPayments = floatPayments / 2;

    double tauFloat = 1. / floatLegFreq.getPeriodsPerYear();
    double tauFixed = 2 * tauFloat;

    final double[] fixed = new double[fixedPayments];
    final double[] floating = new double[floatPayments];
    final double[] indexFixing = new double[floatPayments];
    final double[] indexMaturity = new double[floatPayments];
    final double[] yearFrac = new double[floatPayments];

    //turn on to randomised fixing/reset/payment dates 
    final double sigma = 0.0 / 365.0;

    for (int i = 0; i < fixedPayments; i++) {
      fixed[i] = tauFixed * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    for (int i = 0; i < floatPayments; i++) {
      floating[i] = tauFloat * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      yearFrac[i] = tauFloat + sigma * (RANDOM.nextDouble() - 0.5);
      indexFixing[i] = tauFloat * i + sigma * (i == 0 ? RANDOM.nextDouble() / 2 : (RANDOM.nextDouble() - 0.5));
      indexMaturity[i] = tauFloat * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    final AnnuityCouponFixed fixedLeg = new AnnuityCouponFixed(DUMMY_CUR, fixed, notional, rate, fundingCurveName, true);

    final AnnuityCouponIbor floatingLeg = new AnnuityCouponIbor(DUMMY_CUR, floating, indexFixing, DUMMY_INDEX, indexMaturity, yearFrac, notional, fundingCurveName, liborCurveName, false);
    return new FixedFloatSwap(fixedLeg, floatingLeg);
  }

  @SuppressWarnings("unused")
  private static FixedFloatSwap makeSwap(final double time, final SimpleFrequency floatLegFreq, final SimpleFrequency fixedLegFreq, final String fundingCurveName, final String liborCurveName,
      final double rate, final double notional) {

    int floatPayments = (int) (time * floatLegFreq.getPeriodsPerYear());
    int fixedPayments = (int) (time * fixedLegFreq.getPeriodsPerYear());

    double tauFloat = 1. / floatLegFreq.getPeriodsPerYear();
    double tauFixed = 1. / fixedLegFreq.getPeriodsPerYear();

    Validate.isTrue(tauFloat * floatPayments == time, "float payments will not finish on time");
    Validate.isTrue(tauFixed * fixedPayments == time, "fixed payments will not finish on time");

    final double[] fixed = new double[fixedPayments];
    final double[] floating = new double[floatPayments];
    final double[] indexFixing = new double[floatPayments];
    final double[] indexMaturity = new double[floatPayments];
    final double[] yearFrac = new double[floatPayments];

    //turn on to randomised fixing/reset/payment dates 
    final double sigma = 0.0 / 365.0;

    for (int i = 0; i < fixedPayments; i++) {
      fixed[i] = tauFixed * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    for (int i = 0; i < floatPayments; i++) {
      floating[i] = tauFloat * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      yearFrac[i] = tauFloat + sigma * (RANDOM.nextDouble() - 0.5);
      indexFixing[i] = tauFloat * i + sigma * (i == 0 ? RANDOM.nextDouble() / 2 : (RANDOM.nextDouble() - 0.5));
      indexMaturity[i] = tauFloat * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    final AnnuityCouponFixed fixedLeg = new AnnuityCouponFixed(DUMMY_CUR, fixed, notional, rate, fundingCurveName, true);

    final AnnuityCouponIbor floatingLeg = new AnnuityCouponIbor(DUMMY_CUR, floating, indexFixing, DUMMY_INDEX, indexMaturity, yearFrac, notional, fundingCurveName, liborCurveName, false);
    return new FixedFloatSwap(fixedLeg, floatingLeg);
  }

  /**
   * Sets up a simple Floating rate note to test the analytics 
   * @param notional An amount in a currency
   * @param nYears time to maturity in years
   * @param freq Frequency of payments 
   * @param discountCurve Name of discount curve
   * @param indexCurve Name of index curve
   * @param spread the spread paid
   * @return A FRN
   */
  protected static FloatingRateNote makeFRN(final CurrencyAmount notional, final int nYears, SimpleFrequency freq, final String discountCurve, final String indexCurve, final double spread) {

    int payments = (int) (nYears * freq.getPeriodsPerYear());
    final double[] floatingPayments = new double[payments];
    final double[] indexFixing = new double[payments];
    final double[] indexMaturity = new double[payments];
    final double[] yearFrac = new double[payments];

    for (int i = 0; i < payments; i++) {
      indexFixing[i] = i / freq.getPeriodsPerYear();
      indexMaturity[i] = (i + 1) / freq.getPeriodsPerYear();
      floatingPayments[i] = indexMaturity[i];
      yearFrac[i] = 1 / freq.getPeriodsPerYear();
    }
    final AnnuityCouponIbor floatingLeg = new AnnuityCouponIbor(notional.getCurrency(), floatingPayments, indexFixing, DUMMY_INDEX, indexMaturity, yearFrac, notional.getAmount(), discountCurve,
        indexCurve, notional.getAmount() < 0.0).withSpread(spread);

    PaymentFixed initialPayment = new PaymentFixed(notional.getCurrency(), 2.0 / 365, -notional.getAmount(), discountCurve);
    PaymentFixed finalPayment = new PaymentFixed(notional.getCurrency(), nYears, notional.getAmount(), discountCurve);

    return new FloatingRateNote(floatingLeg, initialPayment, finalPayment);
  }

  protected static CrossCurrencySwap makeCrossCurrencySwap(final CurrencyAmount domesticNotional, final CurrencyAmount foreignNotional, final int swapLength, SimpleFrequency domesticPaymentFreq,
      SimpleFrequency foreignPaymentFreq, final String domesticDiscountCurve, final String domesticIndexCurve, final String foreignDiscountCurve, final String foreignIndexCurve, final double spread) {

    FloatingRateNote domesticFRN = makeFRN(domesticNotional, swapLength, domesticPaymentFreq, domesticDiscountCurve, domesticIndexCurve, 0.0);
    FloatingRateNote foreignFRN = makeFRN(foreignNotional, swapLength, foreignPaymentFreq, foreignDiscountCurve, foreignIndexCurve, spread);

    double spotFX = domesticNotional.getAmount() / foreignNotional.getAmount(); //assume the initial exchange of notionals cancels 
    return new CrossCurrencySwap(domesticFRN, foreignFRN, spotFX);
  }

  protected static ForexForward makeForexForward(final CurrencyAmount domesticNotional, final CurrencyAmount foreignNotional, final double paymentTime, final double spotFX,
      final String domesticDiscountCurve, final String foreignDiscountCurve) {
    PaymentFixed p1 = new PaymentFixed(domesticNotional.getCurrency(), paymentTime, domesticNotional.getAmount(), domesticDiscountCurve);
    PaymentFixed p2 = new PaymentFixed(foreignNotional.getCurrency(), paymentTime, foreignNotional.getAmount(), foreignDiscountCurve);
    return new ForexForward(p1, p2, spotFX);
  }

  protected double[] catMap(final HashMap<String, double[]> map) {
    int nNodes = 0;
    for (final double[] temp : map.values()) {
      nNodes += temp.length;
    }

    final double[] temp = new double[nNodes];
    int index = 0;
    for (final double[] times : map.values()) {
      for (final double t : times) {
        temp[index++] = t;
      }
    }
    Arrays.sort(temp);
    return temp;
  }

  protected void assertMatrixEquals(final DoubleMatrix2D m1, final DoubleMatrix2D m2, final double eps) {
    final int m = m1.getNumberOfRows();
    final int n = m1.getNumberOfColumns();
    assertEquals(m2.getNumberOfRows(), m);
    assertEquals(m2.getNumberOfColumns(), n);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < m; j++) {
        assertEquals("Entry " + i + " - " + j, m1.getEntry(i, j), m2.getEntry(i, j), eps);
      }
    }
  }

}
