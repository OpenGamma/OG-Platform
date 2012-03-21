/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import java.io.PrintStream;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.financial.greeks.PDEResultCollection;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.financial.model.volatility.local.LocalVolatilityForwardPDEGreekCalculator;
import com.opengamma.financial.model.volatility.local.LocalVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurfaceMoneyness;
import com.opengamma.financial.model.volatility.surface.Moneyness;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PiecewiseSABRSurfaceFitter1Test {
  private static final DupireLocalVolatilityCalculator DUPIRE = new DupireLocalVolatilityCalculator();
  private static final CombinedInterpolatorExtrapolator NATURAL_CUBIC_SPLINE = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final CombinedInterpolatorExtrapolator DOUBLE_QUADRATIC = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  //Instrument used for Vega/Greeks Reports
  private static final double EXAMPLE_EXPIRY = 0.5;
  private static final double EXAMPLE_STRIKE = 1.4;

  private static final double[] DELTAS = new double[] {0.15, 0.25 };
  private static final double[] FORWARDS = new double[] {1.34, 1.35, 1.36, 1.38, 1.4, 1.43, 1.45, 1.48, 1.5, 1.52 };
  private static final ForwardCurve FORWARD_CURVE;
  private static final double[] EXPIRIES = new double[] {7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 10 };
  private static final double[] ATM = new double[] {0.17045, 0.1688, 0.167425, 0.1697, 0.1641, 0.1642, 0.1641, 0.1642, 0.138, 0.12515 };
  private static final double[][] RR = new double[][] { {-0.0168, -0.02935, -0.039125, -0.047325, -0.058325, -0.06055, -0.0621, -0.063, -0.032775, -0.023925 },
    {-0.012025, -0.02015, -0.026, -0.0314, -0.0377, -0.03905, -0.0396, -0.0402, -0.02085, -0.015175 } };
  private static final double[][] BUTT = new double[][] { {0.00665, 0.00725, 0.00835, 0.009075, 0.013175, 0.01505, 0.01565, 0.0163, 0.009275, 0.007075, },
    {0.002725, 0.00335, 0.0038, 0.004, 0.0056, 0.0061, 0.00615, 0.00635, 0.00385, 0.002575 } };
  private static final double[][] STRIKES;
  private static final double[][] VOLS;
  private static final int N;
  private static final double THETA = 0.5;
  private static final int TIME_STEPS = 100;
  private static final int SPACE_STEPS = 100;
  private static final double TIME_GRID_BUNCHING = 5;
  private static final double SPACE_GRID_BUNCHING = 0.05;
  private static final double MAX_MONEYNESS = 3.5;

  private static final ForexSmileDeltaSurfaceDataBundle FOREX_DELTA_DATA;
  private static final MoneynessPiecewiseSABRSurfaceFitter MONEYNESS_SURFACE_FITTER;

  static {
    N = EXPIRIES.length;
    STRIKES = new double[N][];
    VOLS = new double[N][];
    for (int i = 0; i < N; i++) {
      final SmileDeltaParameter cal = new SmileDeltaParameter(EXPIRIES[i], ATM[i], DELTAS,
          new double[] {RR[0][i], RR[1][i] }, new double[] {BUTT[0][i], BUTT[1][i] });
      STRIKES[i] = cal.getStrike(FORWARDS[i]);
      VOLS[i] = cal.getVolatility();
    }
    FORWARD_CURVE = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, DOUBLE_QUADRATIC));
    FOREX_DELTA_DATA = new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, RR, BUTT, true);
    MONEYNESS_SURFACE_FITTER = new MoneynessPiecewiseSABRSurfaceFitter(true, true, true);
  }

  //For each expiry, print the expiry and the strikes and implied volatilities
  @Test
  (enabled = false)
  public void printMarketData() {

    for (int i = 0; i < N; i++) {
      System.out.println(EXPIRIES[i]);
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      final int m = STRIKES[i].length;
      for (int j = 0; j < m; j++) {
        System.out.print(STRIKES[i][j] + "\t");
      }
      System.out.print("\n");
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      final int m = STRIKES[i].length;
      for (int j = 0; j < m; j++) {
        System.out.print(VOLS[i][j] + "\t");
      }
      System.out.print("\n");
    }
  }

  //  //Fit the market data at each time slice and print the smiles and a functions of both strike and delta
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  (enabled = false)
  public void fitMarketData() {
    final PiecewiseSABRFitter fitter = new PiecewiseSABRFitter();
    final Function1D[] fitters = new Function1D[N];
    System.out.println("Fitted smiles by strike");
    System.out.print("\t");
    for (int j = 0; j < 100; j++) {
      final double k = 0.5 + j * 4.5 / 99.;
      System.out.print(k + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      fitters[i] = fitter.getVolatilityFunction(FORWARDS[i], STRIKES[i], EXPIRIES[i], VOLS[i]);
      System.out.print(EXPIRIES[i] + "\t");
      for (int j = 0; j < 100; j++) {
        final double k = 0.5 + j * 4.5 / 99.;
        final double vol = (Double) fitters[i].evaluate(k);
        System.out.print(vol + "\t");
      }
      System.out.print("\n");
    }
    System.out.print("\n");
    System.out.println("Fitted smiles by delta");
    System.out.print("\t");
    for (int j = 0; j < 100; j++) {
      final double d = 0.001 + j * 0.998 / 99.;
      System.out.print(d + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      System.out.print(EXPIRIES[i] + "\t");
      for (int j = 0; j < 100; j++) {
        final double d = 0.05 + j * 0.9 / 99.;
        final Function1D<Double, Double> func = getStrikeForDeltaFunction(FORWARDS[i], EXPIRIES[i], true, fitters[i]);
        final double vol = (Double) fitters[i].evaluate(func.evaluate(d));
        System.out.print(vol + "\t");
      }
      System.out.print("\n");
    }

  }

  /**
   * Print the fitted implied vol surface and the derived implied vol
   */
  @Test
  (enabled = false)
  public void printSurface() {
    final BlackVolatilitySurfaceMoneyness surfaceM = MONEYNESS_SURFACE_FITTER.getVolatilitySurface(FOREX_DELTA_DATA);
    PDEUtilityTools.printSurface("vol surface", surfaceM.getSurface(), 0, 10, 0.2, 3.0, 200, 100);
    final LocalVolatilitySurfaceMoneyness lv = DUPIRE.getLocalVolatility(surfaceM);
    PDEUtilityTools.printSurface("LV surface", lv.getSurface(), 0, 10, 0.2, 3.0, 200, 100);
  }

  /**
   * Print the fitted implied vol surface and the derived implied vol as a function of moneyness m = log(k/f)/(1+lambda*sqrt(t))
   */
  @Test
  (enabled = false)
  public void printDeltaProxySurface() {
    final ForwardCurve forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, NATURAL_CUBIC_SPLINE));
    final ForexSmileDeltaSurfaceDataBundle forexDeltaData = new ForexSmileDeltaSurfaceDataBundle(forwardCurve, EXPIRIES, DELTAS, ATM, RR, BUTT, true);
    final MoneynessPiecewiseSABRSurfaceFitter surfaceFitter = new MoneynessPiecewiseSABRSurfaceFitter(true, true, true);
    final double xMin = -0.5;
    final double xMax = 0.5;

    final BlackVolatilitySurfaceMoneyness surface = surfaceFitter.getVolatilitySurface(forexDeltaData);
    final Surface<Double, Double, Double> moneynessSurface = toDeltaProxySurface(surface);
    PDEUtilityTools.printSurface("moneyness surface", moneynessSurface, 0, 10, xMin, xMax, 200, 100);

    final LocalVolatilitySurfaceMoneyness lv = DUPIRE.getLocalVolatility(surface);

    final Surface<Double, Double, Double> lvMoneynessSurface = toDeltaProxySurface(lv);
    PDEUtilityTools.printSurface("LV moneyness surface", lvMoneynessSurface, 0.0001, 10, xMin, xMax, 200, 100);
  }

  @Test
  (enabled = false)
  public void runPDESolver() {
    final PrintStream ps = System.out;
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final ForwardCurve forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, interpolator));
    final ForexSmileDeltaSurfaceDataBundle forexDeltaData = new ForexSmileDeltaSurfaceDataBundle(forwardCurve, EXPIRIES, DELTAS, ATM, RR, BUTT, true);
    final MoneynessPiecewiseSABRSurfaceFitter surfaceFitter = new MoneynessPiecewiseSABRSurfaceFitter(true, true, true);
    final LocalVolatilityForwardPDEGreekCalculator<Moneyness> pdeCalculator =
        new LocalVolatilityForwardPDEGreekCalculator<Moneyness>(THETA, TIME_STEPS, SPACE_STEPS, TIME_GRID_BUNCHING, SPACE_GRID_BUNCHING, surfaceFitter, DUPIRE, MAX_MONEYNESS);
    final BlackVolatilitySurface<?> impVolSurface = surfaceFitter.getVolatilitySurface(forexDeltaData);
    final LocalVolatilitySurface<?> localVolatility = DUPIRE.getLocalVolatilitySurface(impVolSurface, forwardCurve);
    final PDEFullResults1D result = pdeCalculator.solve(forexDeltaData, localVolatility);
    final double[][] strikes = forexDeltaData.getStrikes();
    final double[] expiries = forexDeltaData.getExpiries();
    final double[][] impliedVols = forexDeltaData.getVolatilities();
    double minK = Double.POSITIVE_INFINITY;
    double maxK = 0.0;
    for (int i = 0; i < N; i++) {
      final int m = strikes[i].length;
      for (int j = 0; j < m; j++) {
        final double k = strikes[i][j];
        if (k < minK) {
          minK = k;
        }
        if (k > maxK) {
          maxK = k;
        }
      }
    }
    minK /= 2;
    maxK *= 1.5;

    final double maxT = expiries[N - 1];


    final BlackVolatilitySurfaceMoneyness pdeVolSurface = modifiedPriceToVolSurface(forwardCurve, result, 0, maxT, 0.3, 3.0, true);
    //PDEUtilityTools.printSurface("vol surface", pdeVolSurface.getSurface(), 0, maxT, 0.3, 3.0);

    double chiSq = 0;
    for (int i = 0; i < N; i++) {
      final int m = strikes[i].length;
      final double t = expiries[i];
      for (int j = 0; j < m; j++) {
        final double k = strikes[i][j];

        final double mrtVol = impliedVols[i][j];
        final double modelVol = pdeVolSurface.getVolatility(t, k);
        ps.println(expiries[i] + "\t" + k + "\t" + mrtVol + "\t" + modelVol);
        chiSq += (mrtVol - modelVol) * (mrtVol - modelVol);
      }
    }
    ps.println("chi^2 " + chiSq * 1e6);

    ps.print("\n");
    ps.println("strike sensitivity");
    for (int i = 0; i < N; i++) {
      ps.print(expiries[i] + "\t" + "" + "\t");
    }
    ps.print("\n");
    for (int i = 0; i < N; i++) {
      ps.print("Strike\tImplied Vol\t");
    }
    ps.print("\n");
    for (int j = 0; j < 100; j++) {
      for (int i = 0; i < N; i++) {
        final int m = strikes[i].length;
        final double t = expiries[i];
        final double kLow = strikes[i][0];
        final double kHigh = strikes[i][m - 1];
        final double k = kLow + (kHigh - kLow) * j / 99.;
        ps.print(k + "\t" + pdeVolSurface.getVolatility(t, k) + "\t");
      }
      ps.print("\n");
    }

  }

  //  @Test
  //  (enabled = false)
  //  public void runBackwardsPDESolver() {
  //    final PrintStream ps = System.out;
  //    CAL.runBackwardsPDESolver(ps, EXAMPLE_EXPIRY, EXAMPLE_STRIKE);
  //    final int n = res.getGrid().getNumSpaceNodes();
  //    for (int i = 0; i < n; i++) {
  //      final double price = res.getFunctionValue(i);
  //      final double f = res.getSpaceValue(i);
  //      try {
  //        final double vol = BlackFormulaRepository.impliedVolatility(price, f, strike, expiry, _isCall);
  //        ps.println(f + "\t" + price + "\t" + vol);
  //      } catch (final Exception e) {
  //
  //      }
  //    }
  //  }
  //
  @Test
  (enabled = false)
  public void bucketedVega() {
    final EuropeanVanillaOption option = new EuropeanVanillaOption(EXAMPLE_STRIKE, EXAMPLE_EXPIRY, true);
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final ForwardCurve forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, interpolator));
    final ForexSmileDeltaSurfaceDataBundle forexDeltaData = new ForexSmileDeltaSurfaceDataBundle(forwardCurve, EXPIRIES, DELTAS, ATM, RR, BUTT, true);
    final Interpolator1D interpolator1 = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final ForwardCurve forwardCurve1 = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, interpolator1));
    final MoneynessPiecewiseSABRSurfaceFitter surfaceFitter = new MoneynessPiecewiseSABRSurfaceFitter(true, true, true);
    final LocalVolatilityForwardPDEGreekCalculator<Moneyness> pdeCalculator =
        new LocalVolatilityForwardPDEGreekCalculator<Moneyness>(THETA, TIME_STEPS, SPACE_STEPS, TIME_GRID_BUNCHING, SPACE_GRID_BUNCHING, surfaceFitter, DUPIRE, MAX_MONEYNESS);
    final BlackVolatilitySurface<?> impVolSurface = surfaceFitter.getVolatilitySurface(forexDeltaData);
    final LocalVolatilitySurface<?> localVolatility = DUPIRE.getLocalVolatilitySurface(impVolSurface, forwardCurve1);
    final BucketedGreekResultCollection bucketedVega = pdeCalculator.getBucketedVega(forexDeltaData, localVolatility, option);
    final double[][] vega = bucketedVega.getBucketedGreeks(BucketedGreekResultCollection.BUCKETED_VEGA);
    for (final double[] element : vega) {
      String temp = "";
      for (final double element2 : element) {
        temp += element2 + "\t";
      }
      System.out.println(temp);
    }
  }

  @Test
  (enabled = false)
  public void deltaAndGamma() {
    final PrintStream ps = System.out;
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final ForwardCurve forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, interpolator));
    final ForexSmileDeltaSurfaceDataBundle forexDeltaData = new ForexSmileDeltaSurfaceDataBundle(forwardCurve, EXPIRIES, DELTAS, ATM, RR, BUTT, true);
    final Interpolator1D interpolator1 = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final ForwardCurve forwardCurve1 = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, interpolator1));
    final ForexSmileDeltaSurfaceDataBundle forexDeltaData1 = new ForexSmileDeltaSurfaceDataBundle(forwardCurve1, EXPIRIES, DELTAS, ATM, RR, BUTT, true);
    final MoneynessPiecewiseSABRSurfaceFitter surfaceFitter = new MoneynessPiecewiseSABRSurfaceFitter(true, true, true);
    final LocalVolatilityForwardPDEGreekCalculator<Moneyness> pdeCalculator =
        new LocalVolatilityForwardPDEGreekCalculator<Moneyness>(THETA, TIME_STEPS, SPACE_STEPS, TIME_GRID_BUNCHING, SPACE_GRID_BUNCHING, surfaceFitter, DUPIRE, MAX_MONEYNESS);
    final BlackVolatilitySurface<?> impVolSurface = surfaceFitter.getVolatilitySurface(forexDeltaData);
    final LocalVolatilitySurface<?> localVolatility = DUPIRE.getLocalVolatilitySurface(impVolSurface, forwardCurve);
    final PDEResultCollection greeks = pdeCalculator.getGridGreeks(forexDeltaData1, localVolatility, new EuropeanVanillaOption(EXAMPLE_STRIKE, EXAMPLE_EXPIRY, true));
    final double[] strikes = greeks.getStrikes();
    final double[] bsDelta = greeks.getGridGreeks(PDEResultCollection.GRID_BLACK_DELTA);
    final double[] modelDelta = greeks.getGridGreeks(PDEResultCollection.GRID_DELTA);
    final double[] bsDualDelta = greeks.getGridGreeks(PDEResultCollection.GRID_BLACK_DUAL_DELTA);
    final double[] modelDD = greeks.getGridGreeks(PDEResultCollection.GRID_DUAL_DELTA);
    final double[] bsGamma = greeks.getGridGreeks(PDEResultCollection.GRID_BLACK_GAMMA);
    final double[] modelGamma = greeks.getGridGreeks(PDEResultCollection.GRID_GAMMA);
    final double[] bsDualGamma = greeks.getGridGreeks(PDEResultCollection.GRID_BLACK_DUAL_GAMMA);
    final double[] modelDG = greeks.getGridGreeks(PDEResultCollection.GRID_DUAL_GAMMA);
    for (int i = 0; i < strikes.length; i++) {
      ps.println(strikes[i] + "\t 0" + "\t" + bsDelta[i] + "\t" + modelDelta[i] + "\t" + bsDualDelta[i] + "\t" + modelDD[i]
          + "\t" + bsGamma[i] + "\t" + modelGamma[i] + "\t" + bsDualGamma[i] + "\t" + modelDG[i]);
    }
  }

  /**
   * print out vega based greeks
   */
  @Test
  (enabled = false)
  public void vega() {
    final PrintStream ps = System.out;
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final ForwardCurve forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, interpolator));
    final ForexSmileDeltaSurfaceDataBundle forexDeltaData = new ForexSmileDeltaSurfaceDataBundle(forwardCurve, EXPIRIES, DELTAS, ATM, RR, BUTT, true);
    final Interpolator1D interpolator1 = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final ForwardCurve forwardCurve1 = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, interpolator1));
    final ForexSmileDeltaSurfaceDataBundle forexDeltaData1 = new ForexSmileDeltaSurfaceDataBundle(forwardCurve1, EXPIRIES, DELTAS, ATM, RR, BUTT, true);
    final MoneynessPiecewiseSABRSurfaceFitter surfaceFitter = new MoneynessPiecewiseSABRSurfaceFitter(true, true, true);
    final LocalVolatilityForwardPDEGreekCalculator<Moneyness> pdeCalculator =
        new LocalVolatilityForwardPDEGreekCalculator<Moneyness>(THETA, TIME_STEPS, SPACE_STEPS, TIME_GRID_BUNCHING, SPACE_GRID_BUNCHING, surfaceFitter, DUPIRE, MAX_MONEYNESS);
    final BlackVolatilitySurface<?> impVolSurface = surfaceFitter.getVolatilitySurface(forexDeltaData);
    final LocalVolatilitySurface<?> localVolatility = DUPIRE.getLocalVolatilitySurface(impVolSurface, forwardCurve);
    final PDEResultCollection greeks = pdeCalculator.getGridGreeks(forexDeltaData1, localVolatility, new EuropeanVanillaOption(EXAMPLE_STRIKE, EXAMPLE_EXPIRY, true));
    final double[] strikes = greeks.getStrikes();
    final double[] bsVega = greeks.getGridGreeks(PDEResultCollection.GRID_BLACK_VEGA);
    final double[] bsVanna = greeks.getGridGreeks(PDEResultCollection.GRID_BLACK_VANNA);
    final double[] bsVomma = greeks.getGridGreeks(PDEResultCollection.GRID_BLACK_VOMMA);
    final double[] modelVega = greeks.getGridGreeks(PDEResultCollection.GRID_VEGA);
    final double[] modelVanna = greeks.getGridGreeks(PDEResultCollection.GRID_VANNA);
    final double[] modelVomma = greeks.getGridGreeks(PDEResultCollection.GRID_VOMMA);
    for (int i = 0; i < strikes.length; i++) {
      ps.println(strikes[i] + "\t" + bsVega[i] + "\t" + modelVega[i] + "\t" + bsVanna[i] + "\t" + modelVanna[i] + "\t" + bsVomma[i] + "\t" + modelVomma[i]);
    }
  }

  private Function1D<Double, Double> getStrikeForDeltaFunction(final double forward, final double expiry, final boolean isCall,
      final Function1D<Double, Double> volFunc) {
    final BracketRoot bracketer = new BracketRoot();
    final BisectionSingleRootFinder rootFinder = new BisectionSingleRootFinder(1e-8);

    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double delta) {

        final Function1D<Double, Double> deltaFunc = new Function1D<Double, Double>() {

          @Override
          public Double evaluate(final Double strike) {
            final double vol = volFunc.evaluate(strike);
            final double deltaTry = BlackFormulaRepository.delta(forward, strike, expiry, vol, isCall);
            return deltaTry - delta;
          }
        };

        final double[] brackets = bracketer.getBracketedPoints(deltaFunc, 0.5, 1.5, 0, 5);
        return rootFinder.getRoot(deltaFunc, brackets[0], brackets[1]);
      }
    };
  }

  private Surface<Double, Double, Double> toDeltaProxySurface(final BlackVolatilitySurface<?> lv) {

    final Function<Double, Double> func = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final double f = FORWARD_CURVE.getForward(t);
        final double k = f * Math.exp(-x * Math.sqrt(t));
        return lv.getVolatility(t, k);
      }
    };

    return FunctionalDoublesSurface.from(func);
  }

  private Surface<Double, Double, Double> toDeltaProxySurface(final LocalVolatilitySurface<?> lv) {

    final Function<Double, Double> func = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final double f = FORWARD_CURVE.getForward(t);
        final double k = f * Math.exp(-x * Math.sqrt(t));
        return lv.getVolatility(t, k);
      }
    };

    return FunctionalDoublesSurface.from(func);
  }

  private BlackVolatilitySurfaceMoneyness modifiedPriceToVolSurface(final ForwardCurve forwardCurve, final PDEFullResults1D prices,
      final double minT, final double maxT, final double minMoneyness, final double maxMoneyness, final boolean isCall) {
    final CombinedInterpolatorExtrapolator extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final GridInterpolator2D interpolator2D = new GridInterpolator2D(extrapolator, extrapolator);

    final Map<DoublesPair, Double> vol = PDEUtilityTools.modifiedPriceToImpliedVol(prices, minT, maxT, minMoneyness, maxMoneyness, isCall);
    final Map<Double, Interpolator1DDataBundle> idb = interpolator2D.getDataBundle(vol);

    final Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        return interpolator2D.interpolate(idb, new DoublesPair(tk[0], tk[1]));
      }
    };

    return new BlackVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(func), forwardCurve);
  }
}
