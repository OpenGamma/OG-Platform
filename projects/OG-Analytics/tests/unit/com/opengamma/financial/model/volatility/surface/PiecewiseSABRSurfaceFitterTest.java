/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.financial.model.finitedifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.financial.model.finitedifference.MeshingFunction;
import com.opengamma.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.model.finitedifference.PDEResults1D;
import com.opengamma.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.financial.model.finitedifference.applications.PDEDataBundleProvider;
import com.opengamma.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.smile.fitting.PiecewiseSABRFitter;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.curve.CurveShiftFunctionFactory;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;
import com.opengamma.math.surface.SurfaceShiftFunctionFactory;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PiecewiseSABRSurfaceFitterTest {
  DupireLocalVolatilityCalculator DUPIRE = new DupireLocalVolatilityCalculator();

  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  //private static final CombinedInterpolatorExtrapolatorFactory FACTORY = new CIE;
  private static final CombinedInterpolatorExtrapolator EXTRAPOLATOR_1D = new CombinedInterpolatorExtrapolator(INTERPOLATOR_1D, new FlatExtrapolator1D());
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(EXTRAPOLATOR_1D, EXTRAPOLATOR_1D);

  //Instrument used for Vega/Greeks Reports
  private static final double EXAMPLE_EXPIRY = 0.5;
  private static final double EXAMPLE_STRIKE = 1.4;

  private static final double[] DELTAS = new double[] {0.15, 0.25 };
  private static final double SPOT = 1.34;
  private static final double[] FORWARDS = new double[] {1.34, 1.34, 1.34, 1.34, 1.34, 1.34, 1.34, 1.34, 1.34, 1.34 };
  private static final ForwardCurve FORWARD_CURVE;
  private static final String[] TENORS = new String[] {"1W", "2W", "3W", "1M", "3M", "6M", "9M", "1Y", "5Y", "10Y" };
  private static final double[] EXPIRIES = new double[] {7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 10 };
  private static final double[] ATM = new double[] {0.17045, 0.1688, 0.167425, 0.1697, 0.1641, 0.1642, 0.1641, 0.1642, 0.138, 0.12515 };
  private static final double[][] RR = new double[][] { {-0.0168, -0.02935, -0.039125, -0.047325, -0.058325, -0.06055, -0.0621, -0.063, -0.032775, -0.023925 },
    {-0.012025, -0.02015, -0.026, -0.0314, -0.0377, -0.03905, -0.0396, -0.0402, -0.02085, -0.015175 } };
  private static final double[][] BUTT = new double[][] { {0.00665, 0.00725, 0.00835, 0.009075, 0.013175, 0.01505, 0.01565, 0.0163, 0.009275, 0.007075, },
    {0.002725, 0.00335, 0.0038, 0.004, 0.0056, 0.0061, 0.00615, 0.00635, 0.00385, 0.002575 } };
  private static final double[][] STRIKES;
  private static final double[][] VOLS;
  private static final int N;

  private static final PiecewiseSABRSurfaceFitter SURFACE_FITTER;

  static {

    N = FORWARDS.length;
    STRIKES = new double[N][];
    VOLS = new double[N][];
    for (int i = 0; i < N; i++) {
      SmileDeltaParameter cal = new SmileDeltaParameter(EXPIRIES[i], ATM[i], DELTAS,
          new double[] {RR[0][i], RR[1][i] }, new double[] {BUTT[0][i], BUTT[1][i] });
      STRIKES[i] = cal.getStrike(FORWARDS[i]);
      VOLS[i] = cal.getVolatility();
    }

    FORWARD_CURVE = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, EXTRAPOLATOR_1D));
    SURFACE_FITTER = new PiecewiseSABRSurfaceFitter(DELTAS, FORWARDS, EXPIRIES, ATM, RR, BUTT);
  }

  //For each expiry, print the expiry and the strikes and implied volatilities
  @Test(enabled = false)
  public void printMarketData() {

    for (int i = 0; i < N; i++) {
      System.out.println(EXPIRIES[i]);
      final int m = STRIKES[i].length;
      for (int j = 0; j < m; j++) {
        System.out.print(STRIKES[i][j] + "\t");
      }
      System.out.print("\n");
      for (int j = 0; j < m; j++) {
        System.out.print(VOLS[i][j] + "\t");
      }
      System.out.print("\n");
    }
  }

  //Fit the market data at each time slice and print the smiles and a functions of both strike and delta
  @Test(enabled = false)
  public void fitMarketData() {
    PiecewiseSABRFitter[] fitters = new PiecewiseSABRFitter[N];
    System.out.println("Fitted smiles by strike");
    System.out.print("\t");
    for (int j = 0; j < 100; j++) {
      double k = 0.5 + j * 4.5 / 99.;
      System.out.print(k + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      fitters[i] = new PiecewiseSABRFitter(FORWARDS[i], STRIKES[i], EXPIRIES[i], VOLS[i]);
      System.out.print(EXPIRIES[i] + "\t");
      for (int j = 0; j < 100; j++) {
        double k = 0.5 + j * 4.5 / 99.;
        double vol = fitters[i].getVol(k);
        System.out.print(vol + "\t");
      }
      System.out.print("\n");
    }
    System.out.print("\n");
    System.out.println("Fitted smiles by delta");
    System.out.print("\t");
    for (int j = 0; j < 100; j++) {
      double d = 0.001 + j * 0.998 / 99.;
      System.out.print(d + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      System.out.print(EXPIRIES[i] + "\t");
      for (int j = 0; j < 100; j++) {
        double d = 0.05 + j * 0.9 / 99.;
        Function1D<Double, Double> func = getStrikeForDeltaFunction(FORWARDS[i], EXPIRIES[i], true, fitters[i].getVolFunction());
        double vol = fitters[i].getVol(func.evaluate(d));
        System.out.print(vol + "\t");
      }
      System.out.print("\n");
    }

  }

  /**
   * Print the fitted implied vol surface and the derived implied vol
   */
  @Test(enabled = false)
  public void printSurface() {
    BlackVolatilitySurface surface = SURFACE_FITTER.getImpliedVolatilitySurface();
    PDEUtilityTools.printSurface("vol surface", surface.getSurface(), 0, 10, 0.3, 3.0, 200, 100);
    LocalVolatilitySurface lv = DUPIRE.getLocalVolatility(surface, FORWARD_CURVE);
    PDEUtilityTools.printSurface("LV surface", lv.getSurface(), 0, 10, 0.3, 3.0, 200, 100);
  }

  /**
   * Print the fitted implied vol surface and the derived implied vol as a function of moneyness m = log(k/f)/sqrt(t)
   */
  @Test(enabled = false)
  public void printMoneylessSurface() {
    final double xMin = 4 / Math.sqrt(EXPIRIES[0]) * Math.log(STRIKES[0][0] / FORWARDS[0]);
    final double xMax = 4 / Math.sqrt(EXPIRIES[0]) * Math.log(STRIKES[0][STRIKES[0].length - 1] / FORWARDS[0]);

    BlackVolatilitySurface surface = SURFACE_FITTER.getImpliedVolatilitySurface();
    Surface<Double, Double, Double> moneyNessSurface = toMoneynessSurface(surface.getSurface());
    PDEUtilityTools.printSurface("moneyness surface", moneyNessSurface, 0, 10, xMin, xMax, 200, 100);

    LocalVolatilitySurface lv = DUPIRE.getLocalVolatility(surface, FORWARD_CURVE);

    Surface<Double, Double, Double> lvMoneyNessSurface = toMoneynessSurface(lv.getSurface());
    PDEUtilityTools.printSurface("LV moneyness surface", lvMoneyNessSurface, 0, 10, xMin, xMax, 200, 100);
  }

  /**
   * outputs the model prices at the original expiries/strikes and the smiles for a range of strikes
   */
  @Test(enabled = false)
  public void runPDESolver() {
    BlackVolatilitySurface surface = SURFACE_FITTER.getImpliedVolatilitySurface();
    LocalVolatilitySurface lv = DUPIRE.getLocalVolatility(surface, FORWARD_CURVE);
    PDEFullResults1D pdeRes = runForwardPDESolver(SPOT, FORWARD_CURVE, lv);
    BlackVolatilitySurface pdeVolSurface = priceToVolSurface(FORWARD_CURVE, pdeRes);

    double chiSq = 0;
    for (int i = 0; i < N; i++) {
      int m = STRIKES[i].length;
      double t = EXPIRIES[i];
      for (int j = 0; j < m; j++) {
        double k = STRIKES[i][j];

        double mrtVol = VOLS[i][j];
        double modelVol = pdeVolSurface.getVolatility(t, k);
        System.out.println(TENORS[i] + "\t" + k + "\t" + mrtVol + "\t" + modelVol);
        chiSq += (mrtVol - modelVol) * (mrtVol - modelVol);
      }
    }
    System.out.println("chi^2 " + chiSq * 1e6);

    System.out.print("\n");
    System.out.println("strike sensitivity");
    for (int i = 0; i < N; i++) {
      System.out.print(TENORS[i] + "\t" + "" + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      System.out.print("Strike\tImplied Vol\t");
    }
    System.out.print("\n");
    for (int j = 0; j < 100; j++) {
      for (int i = 0; i < N; i++) {
        int m = STRIKES[i].length;
        double t = EXPIRIES[i];
        double kLow = STRIKES[i][0];
        double kHigh = STRIKES[i][m - 1];
        double k = kLow + (kHigh - kLow) * j / 99.;
        System.out.print(k + "\t" + pdeVolSurface.getVolatility(t, k) + "\t");
      }
      System.out.print("\n");
    }
    System.out.print("\n");

  }

  /**
   * bumped each input volatility by 1bs and record the effect on the representative point
   */
  @Test(enabled = false)
  public void bucketedVega() {
    BlackVolatilitySurface surface = SURFACE_FITTER.getImpliedVolatilitySurface();
    LocalVolatilitySurface lv = DUPIRE.getLocalVolatility(surface, FORWARD_CURVE);
    PDEFullResults1D pdeRes = runForwardPDESolver(SPOT, FORWARD_CURVE, lv);

    BlackVolatilitySurface modelSurface = priceToVolSurface(FORWARD_CURVE, pdeRes);

    double exampleVol = modelSurface.getVolatility(EXAMPLE_EXPIRY, EXAMPLE_STRIKE);
    double shiftAmount = 1e-4; //1bps

    double[][] res = new double[N][];

    for (int i = 0; i < N; i++) {
      final int m = STRIKES[i].length;
      res[i] = new double[m];
      for (int j = 0; j < m; j++) {
        PiecewiseSABRSurfaceFitter fitter = SURFACE_FITTER.withBumpedPoint(i, j, shiftAmount);
        BlackVolatilitySurface bumpedSurface = fitter.getImpliedVolatilitySurface();
        LocalVolatilitySurface bumpedLV = DUPIRE.getLocalVolatility(bumpedSurface, FORWARD_CURVE);
        PDEFullResults1D pdeResBumped = runForwardPDESolver(SPOT, FORWARD_CURVE, bumpedLV);
        BlackVolatilitySurface bumpedModelSurface = priceToVolSurface(FORWARD_CURVE, pdeResBumped);
        double vol = bumpedModelSurface.getVolatility(EXAMPLE_EXPIRY, EXAMPLE_STRIKE);
        res[i][j] = (vol - exampleVol) / shiftAmount;
      }
    }
    for (int i = 0; i < N; i++) {
      System.out.print(TENORS[i] + "\t");
      final int m = STRIKES[i].length;
      for (int j = 0; j < m; j++) {
        System.out.print(res[i][j] + "\t");
      }
      System.out.print("\n");
    }
  }

  @Test(enabled = false)
  public void smoothLocalVol() {
    //   final double vol = 0.3;
    //
    //    Function<Double, Double> lvFunc = new Function<Double, Double>() {
    //
    //      @Override
    //      public Double evaluate(Double... tk) {
    //        double k = tk[1];
    //        return ((k - 1.2) * (k - 1.2) * 0.4 + vol);
    //      }
    //    };
    //
    //    LocalVolatilitySurface lv = new LocalVolatilitySurface(FunctionalDoublesSurface.from(lvFunc));

    BlackVolatilitySurface surface = SURFACE_FITTER.getImpliedVolatilitySurface();

    LocalVolatilitySurface lv = DUPIRE.getLocalVolatility(surface, FORWARD_CURVE);

    final double shift = 5e-2 * SPOT;
    PDEFullResults1D pdeRes = runForwardPDESolver(SPOT, FORWARD_CURVE, lv);
    PDEFullResults1D pdeResUp = runForwardPDESolver(SPOT + shift,
        new ForwardCurve(CurveShiftFunctionFactory.getShiftedCurve(FORWARD_CURVE.getForwardCurve(), shift)), lv);
    PDEFullResults1D pdeResDown = runForwardPDESolver(SPOT - shift,
        new ForwardCurve(CurveShiftFunctionFactory.getShiftedCurve(FORWARD_CURVE.getForwardCurve(), -shift)), lv);

    BlackVolatilitySurface modelSurface = priceToVolSurface(FORWARD_CURVE, pdeRes);
    double[] timeNodes = pdeRes.getGrid().getTimeNodes();
    int tIndex = getLowerBoundIndex(timeNodes, EXAMPLE_EXPIRY);

    final int n = pdeRes.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      double k = pdeRes.getSpaceValue(i);
      double bsVol = modelSurface.getVolatility(EXAMPLE_EXPIRY, k);
      double bsDelta = BlackFormulaRepository.delta(FORWARDS[0], k, EXAMPLE_EXPIRY, bsVol, true);
      double bsDualDelta = BlackFormulaRepository.dualDelta(FORWARDS[0], k, EXAMPLE_EXPIRY, bsVol, true);
      double bsGamma = BlackFormulaRepository.gamma(FORWARDS[0], k, EXAMPLE_EXPIRY, bsVol);
      double bsDualGamma = BlackFormulaRepository.dualGamma(FORWARDS[0], k, EXAMPLE_EXPIRY, bsVol);
      double modelDelta = (pdeResUp.getFunctionValue(i, tIndex) - pdeResDown.getFunctionValue(i, tIndex)) / 2 / shift;
      double modelDD = pdeRes.getFirstSpatialDerivative(i, tIndex);
      double modelGamma = (pdeResUp.getFunctionValue(i, tIndex) + pdeResDown.getFunctionValue(i, tIndex) -
          2 * pdeRes.getFunctionValue(i, tIndex)) / shift / shift;
      double modelDG = pdeRes.getSecondSpatialDerivative(i, tIndex);
      System.out.println(k + "\t" + bsVol + "\t" + bsDelta + "\t" + modelDelta + "\t" + bsDualDelta + "\t" + modelDD
          + "\t" + bsGamma + "\t" + modelGamma + "\t" + bsDualGamma + "\t" + modelDG);
    }

    PDEGrid1D grid = pdeRes.getGrid();
    double[] spaceNodes = grid.getSpaceNodes();

    PDEResults1D res = runBackwardsPDESolver(EXAMPLE_STRIKE, lv);

    double[] impVol = new double[n];
    for (int i = 0; i < n; i++) {
      double price = res.getFunctionValue(i);
      double spot = res.getGrid().getSpaceNode(i);
      try {
        impVol[i] = BlackFormulaRepository.impliedVolatility(price, spot, EXAMPLE_STRIKE, EXAMPLE_EXPIRY, true);
      } catch (Exception e) {

      }
    }

    for (int i = 0; i < n; i++) {
      double spot = res.getGrid().getSpaceNode(i);
      double bsDelta = BlackFormulaRepository.delta(spot, EXAMPLE_STRIKE, EXAMPLE_EXPIRY, impVol[i], true);
      double bsVega = BlackFormulaRepository.vega(spot, EXAMPLE_STRIKE, EXAMPLE_EXPIRY, impVol[i]);
      double[] coeff;
      int offset = 0;
      if (i == 0) {
        coeff = grid.getFirstDerivativeForwardCoefficients(i);
      } else if (i == n - 1) {
        coeff = grid.getFirstDerivativeBackwardCoefficients(i);
        offset = -coeff.length + 1;
      } else {
        coeff = grid.getFirstDerivativeCoefficients(i);
        offset = -(coeff.length - 1) / 2;
      }
      double volSense = 0;
      for (int j = 0; j < coeff.length; j++) {
        volSense += coeff[j] * impVol[i + j + offset];
      }

      double modelDelta = res.getFirstSpatialDerivative(i);
      double calDelta = bsDelta + bsVega * volSense;
      System.out.println(spot + "\t" + impVol[i] + "\t" + bsDelta + "\t" + modelDelta + "\t" + calDelta
          + "\t" + bsVega + "\t" + volSense);
    }

    final int xIndex = getLowerBoundIndex(spaceNodes, SPOT);
    double spot = res.getGrid().getSpaceNode(xIndex);
    for (int i = 0; i < 100; i++) {
      double k = 0.4 + 2.0 * i / 99.;
      res = runBackwardsPDESolver(k, lv);
      double price = res.getFunctionValue(xIndex);

      double bsVol = BlackFormulaRepository.impliedVolatility(price, spot, k, EXAMPLE_EXPIRY, true);
      double modelDelta = res.getFirstSpatialDerivative(xIndex);
      double modelGamma = res.getSecondSpatialDerivative(xIndex);

      System.out.println(k + "\t" + bsVol + "\t" + modelDelta + "\t" + modelGamma);
    }

  }

  @Test(enabled = false)
  public void deltaAndGamma() {
    final double shift = 5e-2 * SPOT;
    //   BlackVolatilitySurface surface = SURFACE_FITTER.getImpliedVolatilitySurface();
    //   LocalVolatilitySurface lv = DUPIRE.getLocalVolatility(surface, FORWARD_CURVE);

    Function<Double, Double> lvFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        //       double k = tk[1];
        return 0.2 + 0.2 * Math.exp(-0.3 * t);
        //        if (t > 3.0) {
        //          return 0.27;
        //        }
        //        return (((k - 1.2) * (k - 1.2) * 10 + 0.05) * Math.exp(-2.0 * t) + 0.3);
      }
    };

    LocalVolatilitySurface lv = new LocalVolatilitySurface(FunctionalDoublesSurface.from(lvFunc));
    // LocalVolatilitySurface lv = new LocalVolatilitySurface(ConstantDoublesSurface.from(0.3));

    PDEFullResults1D pdeRes = runForwardPDESolver(SPOT, FORWARD_CURVE, lv);
    PDEFullResults1D pdeResUp = runForwardPDESolver(SPOT + shift,
        new ForwardCurve(CurveShiftFunctionFactory.getShiftedCurve(FORWARD_CURVE.getForwardCurve(), shift)), lv);
    PDEFullResults1D pdeResDown = runForwardPDESolver(SPOT - shift,
        new ForwardCurve(CurveShiftFunctionFactory.getShiftedCurve(FORWARD_CURVE.getForwardCurve(), -shift)), lv);

    BlackVolatilitySurface modelSurface = priceToVolSurface(FORWARD_CURVE, pdeRes);
    double[] timeNodes = pdeRes.getGrid().getTimeNodes();

    int tIndex = getLowerBoundIndex(timeNodes, EXAMPLE_EXPIRY);

    System.out.println("times " + EXAMPLE_EXPIRY + " " + timeNodes[tIndex]);

    //get dual delta & gamma by finite difference on grid, and (normal) delta and gamma by fd on separate grids, for
    // a range of strikes (i.e. the spot is fixed0
    final int n = pdeRes.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      double k = pdeRes.getSpaceValue(i);
      double bsVol = modelSurface.getVolatility(EXAMPLE_EXPIRY, k);
      double bsDelta = BlackFormulaRepository.delta(FORWARDS[0], k, EXAMPLE_EXPIRY, bsVol, true);
      double bsDualDelta = BlackFormulaRepository.dualDelta(FORWARDS[0], k, EXAMPLE_EXPIRY, bsVol, true);
      double bsGamma = BlackFormulaRepository.gamma(FORWARDS[0], k, EXAMPLE_EXPIRY, bsVol);
      double bsDualGamma = BlackFormulaRepository.dualGamma(FORWARDS[0], k, EXAMPLE_EXPIRY, bsVol);
      double modelDelta = (pdeResUp.getFunctionValue(i, tIndex) - pdeResDown.getFunctionValue(i, tIndex)) / 2 / shift;
      double modelDD = pdeRes.getFirstSpatialDerivative(i, tIndex);
      double modelGamma = (pdeResUp.getFunctionValue(i, tIndex) + pdeResDown.getFunctionValue(i, tIndex) -
          2 * pdeRes.getFunctionValue(i, tIndex)) / shift / shift;
      double modelDG = pdeRes.getSecondSpatialDerivative(i, tIndex);
      System.out.println(k + "\t" + bsVol + "\t" + bsDelta + "\t" + modelDelta + "\t" + bsDualDelta + "\t" + modelDD
          + "\t" + bsGamma + "\t" + modelGamma + "\t" + bsDualGamma + "\t" + modelDG);
    }

    //Now run thr backwards solver and get delta and gamma off grid
    PDEResults1D res = runBackwardsPDESolver(EXAMPLE_STRIKE, lv);

    double[] impVol = new double[n];
    for (int i = 0; i < n; i++) {
      double price = res.getFunctionValue(i);
      double forward = res.getGrid().getSpaceNode(i);
      try {
        impVol[i] = BlackFormulaRepository.impliedVolatility(price, forward, EXAMPLE_STRIKE, EXAMPLE_EXPIRY, true);
      } catch (Exception e) {
      }
    }

    for (int i = 0; i < n; i++) {
      double spot = res.getGrid().getSpaceNode(i);
      double bsDelta = BlackFormulaRepository.delta(spot, EXAMPLE_STRIKE, EXAMPLE_EXPIRY, impVol[i], true);
      double bsGamma = BlackFormulaRepository.gamma(spot, EXAMPLE_STRIKE, EXAMPLE_EXPIRY, impVol[i]);
      //double bsVega = BlackFormulaRepository.vega(spot, EXAMPLE_STRIKE, EXAMPLE_EXPIRY, impVol[i]);

      double modelDelta = res.getFirstSpatialDerivative(i);
      double modelGamma = res.getSecondSpatialDerivative(i);

      System.out.println(spot + "\t" + impVol[i] + "\t" + bsDelta + "\t" + modelDelta + "\t" + bsGamma + "\t" + modelGamma);
    }

    final int xIndex = res.getGrid().getLowerBoundIndexForSpace(SPOT);
    double spot = res.getSpaceValue(xIndex);
    System.out.println("True Spot: " + SPOT + ", grid spot: " + res.getSpaceValue(xIndex));
    for (int i = 0; i < 100; i++) {
      double k = 0.5 + 1.5 * i / 99.0;
      res = runBackwardsPDESolver(k, lv);
      double price = res.getFunctionValue(xIndex);
      double vol = 0;
      try {
        vol = BlackFormulaRepository.impliedVolatility(price, spot, k, EXAMPLE_EXPIRY, false);
      } catch (Exception e) {
      }
      //   double modelDelta = res.getFirstSpatialDerivative(xIndex);
      double modelGamma = res.getSecondSpatialDerivative(xIndex);
      System.out.println(k + "\t" + vol + "\t" + price + "\t" + modelGamma);
    }

  }

  /**
   * print out vega based greeks
   */
  @Test(enabled = false)
  public void vega() {
    final double volShift = 1e-4;
    final double spotShift = 5e-2 * SPOT;
    // BlackVolatilitySurface surface = SURFACE_FITTER.getImpliedVolatilitySurface();
    //  LocalVolatilitySurface lv = DUPIRE.getLocalVolatility(surface, FORWARD_CURVE);
    LocalVolatilitySurface lv = new LocalVolatilitySurface(ConstantDoublesSurface.from(0.3));

    lv.getSurface();
    LocalVolatilitySurface lvUp = new LocalVolatilitySurface(SurfaceShiftFunctionFactory.getShiftedSurface(lv.getSurface(), volShift, true));
    LocalVolatilitySurface lvDown = new LocalVolatilitySurface(SurfaceShiftFunctionFactory.getShiftedSurface(lv.getSurface(), -volShift, true));

    PDEFullResults1D pdeRes = runForwardPDESolver(SPOT, FORWARD_CURVE, lv);
    PDEFullResults1D pdeResUp = runForwardPDESolver(SPOT, FORWARD_CURVE, lvUp);
    PDEFullResults1D pdeResDown = runForwardPDESolver(SPOT, FORWARD_CURVE, lvDown);

    PDEFullResults1D pdeResUpUp = runForwardPDESolver(SPOT + spotShift,
        new ForwardCurve(CurveShiftFunctionFactory.getShiftedCurve(FORWARD_CURVE.getForwardCurve(), spotShift)), lvUp);
    PDEFullResults1D pdeResUpDown = runForwardPDESolver(SPOT + spotShift,
        new ForwardCurve(CurveShiftFunctionFactory.getShiftedCurve(FORWARD_CURVE.getForwardCurve(), spotShift)), lvDown);
    PDEFullResults1D pdeResDownUp = runForwardPDESolver(SPOT - spotShift,
        new ForwardCurve(CurveShiftFunctionFactory.getShiftedCurve(FORWARD_CURVE.getForwardCurve(), -spotShift)), lvUp);
    PDEFullResults1D pdeResDownDown = runForwardPDESolver(SPOT - spotShift,
        new ForwardCurve(CurveShiftFunctionFactory.getShiftedCurve(FORWARD_CURVE.getForwardCurve(), -spotShift)), lvDown);

    BlackVolatilitySurface modelSurface = priceToVolSurface(FORWARD_CURVE, pdeRes);

    int tIndex = getLowerBoundIndex(pdeRes.getGrid().getTimeNodes(), EXAMPLE_EXPIRY);
    double forward = FORWARD_CURVE.getForward(EXAMPLE_EXPIRY);
    final int n = pdeRes.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      double k = pdeRes.getSpaceValue(i);
      double bsVol = modelSurface.getVolatility(EXAMPLE_EXPIRY, k);
      double bsVega = BlackFormulaRepository.vega(forward, k, EXAMPLE_EXPIRY, bsVol);
      double bsVanna = BlackFormulaRepository.vanna(forward, k, EXAMPLE_EXPIRY, bsVol);
      double bsVomma = BlackFormulaRepository.vomma(forward, k, EXAMPLE_EXPIRY, bsVol);
      double modelVega = (pdeResUp.getFunctionValue(i, tIndex) - pdeResDown.getFunctionValue(i, tIndex)) / 2 / volShift;
      double modelVanna = (pdeResUpUp.getFunctionValue(i, tIndex) + pdeResDownDown.getFunctionValue(i, tIndex) -
          pdeResUpDown.getFunctionValue(i, tIndex) - pdeResDownUp.getFunctionValue(i, tIndex)) / 4 / spotShift / volShift;
      double modelVomma = (pdeResUp.getFunctionValue(i, tIndex) + pdeResDown.getFunctionValue(i, tIndex)
          - 2 * pdeRes.getFunctionValue(i, tIndex)) / volShift / volShift;
      System.out.println(k + "\t" + bsVega + "\t" + modelVega + "\t" + bsVanna + "\t" + modelVanna + "\t" + bsVomma + "\t" + modelVomma);
    }

  }

  @Test
  //TODO useful test - move somewhere else
  public void sideTest() {
    double eps = 1e-3;
    double f = 1.2;
    double k = 1.4;
    double t = 5.0;
    double alpha = 0.3;
    double beta = 0.6;
    double rho = -0.4;
    double nu = 0.4;
    SABRFormulaData sabrData = new SABRFormulaData(alpha, beta, rho, nu);

    SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    double[] vol = sabr.getVolatilityAdjoint(new EuropeanVanillaOption(k, t, true), f, sabrData);
    double bsDelta = BlackFormulaRepository.delta(f, k, t, vol[0], true);
    double bsVega = BlackFormulaRepository.vega(f, k, t, vol[0]);
    double volForwardSense = vol[1];
    double delta = bsDelta + bsVega * volForwardSense;

    double volUp = sabr.getVolatility(f + eps, k, t, alpha, beta, rho, nu);
    double volDown = sabr.getVolatility(f - eps, k, t, alpha, beta, rho, nu);
    double priceUp = BlackFormulaRepository.price(f + eps, k, t, volUp, true);
    double price = BlackFormulaRepository.price(f, k, t, vol[0], true);
    double priceDown = BlackFormulaRepository.price(f - eps, k, t, volDown, true);
    double fdDelta = (priceUp - priceDown) / 2 / eps;
    assertEquals(fdDelta, delta, 1e-6);

    double bsVanna = BlackFormulaRepository.vanna(f, k, t, vol[0]);
    double bsGamma = BlackFormulaRepository.gamma(f, k, t, vol[0]);

    double[] volD1 = new double[5];
    double[][] volD2 = new double[2][2];
    sabr.getVolatilityAdjoint2(new EuropeanVanillaOption(k, t, true), f, sabrData, volD1, volD2);
    double d2Sigmad2Fwd = volD2[0][0];
    double gamma = bsGamma + 2 * bsVanna * vol[1] + bsVega * d2Sigmad2Fwd;
    double fdGamma = (priceUp + priceDown - 2 * price) / eps / eps;

    double d2Sigmad2FwdFD = (volUp + volDown - 2 * vol[0]) / eps / eps;
    assertEquals(d2Sigmad2FwdFD, d2Sigmad2Fwd, 1e-4);

    assertEquals(fdGamma, gamma, 1e-2);
  }

  @SuppressWarnings("unused")
  private double[] bumpArray(final double[] from, final double amount) {
    int n = from.length;
    double[] to = new double[n];
    for (int i = 0; i < n; i++) {
      to[i] = from[i] + amount;
    }
    return to;
  }

  @SuppressWarnings("unused")
  private PDEFullResults1D runForwardPDESolver(final double spot, AbsoluteLocalVolatilitySurface lv) {

    PDEDataBundleProvider provider = new PDEDataBundleProvider();
    ForwardCurve forwardCurve = new ForwardCurve(spot);
    ConvectionDiffusionPDEDataBundle db = provider.getForwardLocalVol(forwardCurve, 0.0, true, lv);
    ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, true);

    final double upperBoundary = 3.5; //Arbitrary choice
    final double finalT = EXPIRIES[N - 1];

    BoundaryCondition lower = new DirichletBoundaryCondition(spot, 0.0); //call option with strike zero is worth the forward
    BoundaryCondition upper = new DirichletBoundaryCondition(0, upperBoundary);
    //BoundaryCondition upper = new NeumannBoundaryCondition(0, upperBoundary, false);
    MeshingFunction timeMesh = new ExponentialMeshing(0.0, finalT, 100, 5.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, upperBoundary, FORWARDS[0], 100, 0.05);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDEFullResults1D res = (PDEFullResults1D) solver.solve(db, grid, lower, upper);
    return res;
  }

  private PDEFullResults1D runForwardPDESolver(final double spot, final ForwardCurve forwardCurve, final LocalVolatilitySurface lv) {

    PDEDataBundleProvider provider = new PDEDataBundleProvider();
    ConvectionDiffusionPDEDataBundle db = provider.getForwardLocalVol(spot, true, lv);
    ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.65, true);

    final double upperBoundary = 3.5; //Arbitrary choice
    final double finalT = EXPIRIES[N - 1];

    BoundaryCondition lower = new DirichletBoundaryCondition(forwardCurve.getForwardCurve().toFunction1D(), 0.0); //call option with strike zero is worth the forward
    BoundaryCondition upper = new NeumannBoundaryCondition(0, upperBoundary, false);
    MeshingFunction timeMesh = new ExponentialMeshing(0.0, finalT, 100, 5.0);
    //keep the grid the same regardless of spot (useful for finite-difference)
    MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, upperBoundary, SPOT, 100, 0.05);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDEFullResults1D res = (PDEFullResults1D) solver.solve(db, grid, lower, upper);
    return res;
  }

  private PDEResults1D runBackwardsPDESolver(final double strike, LocalVolatilitySurface lv) {

    PDEDataBundleProvider provider = new PDEDataBundleProvider();
    ConvectionDiffusionPDEDataBundle db = provider.getBackwardsLocalVol(strike, EXAMPLE_EXPIRY, false, lv);
    ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.65, false);

    final double upperBoundary = 3.5;
    //    BoundaryCondition lower = new DirichletBoundaryCondition(0, 0.0); //call option with strike zero is worth 0
    //    BoundaryCondition upper = new NeumannBoundaryCondition(1.0, upperBoundary, false);
    BoundaryCondition lower = new DirichletBoundaryCondition(strike, 0.0); //call option with strike zero is worth 0
    BoundaryCondition upper = new NeumannBoundaryCondition(0.0, upperBoundary, false);
    MeshingFunction timeMesh = new ExponentialMeshing(0.0, EXAMPLE_EXPIRY, 100, 5.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, upperBoundary, SPOT, 100, 0.05);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDEResults1D res = solver.solve(db, grid, lower, upper);
    return res;
  }

  //  private BlackVolatilitySurface getModelSurface(final double spot, AbsoluteLocalVolatilitySurface lv) {
  //    PDEFullResults1D res = runForwardPDESolver(spot, lv);
  //
  //    return priceToVolSurface(spot, res);
  //  }

  private BlackVolatilitySurface priceToVolSurface(final ForwardCurve forwardCurve, PDEFullResults1D prices) {

    Map<DoublesPair, Double> vol = PDEUtilityTools.priceToImpliedVol(forwardCurve, prices, 0, 10, 0.3, 4.0);
    final Map<Double, Interpolator1DDataBundle> idb = GRID_INTERPOLATOR2D.getDataBundle(vol);

    Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tk) {
        return GRID_INTERPOLATOR2D.interpolate(idb, new DoublesPair(tk[0], tk[1]));
      }
    };

    return new BlackVolatilitySurface(FunctionalDoublesSurface.from(func));
  }

  private int getLowerBoundIndex(final double[] array, final double t) {
    final int n = array.length;
    if (t < array[0]) {
      return 0;
    }
    if (t > array[n - 1]) {
      return n - 1;
    }

    int index = Arrays.binarySearch(array, t);
    if (index >= 0) {
      // Fast break out if it's an exact match.
      return index;
    }
    if (index < 0) {
      index = -(index + 1);
      index--;
    }
    return index;
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
          public Double evaluate(Double strike) {
            double vol = volFunc.evaluate(strike);
            double deltaTry = BlackFormulaRepository.delta(forward, strike, expiry, vol, isCall);
            return deltaTry - delta;
          }
        };

        double[] brackets = bracketer.getBracketedPoints(deltaFunc, 0.5, 1.5, 0, 5);
        return rootFinder.getRoot(deltaFunc, brackets[0], brackets[1]);
      }
    };
  }

  private Surface<Double, Double, Double> toMoneynessSurface(final Surface<Double, Double, Double> surface) {

    final double eps = 1e-2;

    Function<Double, Double> func = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final double f = FORWARD_CURVE.getForward(t);
        final double k = f * Math.exp(Math.sqrt(t + eps) * x);
        return surface.getZValue(t, k);
      }
    };

    return FunctionalDoublesSurface.from(func);

  }
}
