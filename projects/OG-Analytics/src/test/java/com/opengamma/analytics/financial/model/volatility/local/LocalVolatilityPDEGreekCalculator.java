/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.DoubleExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.applications.InitialConditionsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public class LocalVolatilityPDEGreekCalculator {

  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final CombinedInterpolatorExtrapolator EXTRAPOLATOR_1D = new CombinedInterpolatorExtrapolator(INTERPOLATOR_1D, new FlatExtrapolator1D());
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(EXTRAPOLATOR_1D, EXTRAPOLATOR_1D);
  private static final DupireLocalVolatilityCalculator DUPIRE = new DupireLocalVolatilityCalculator();

  private final VolatilitySurfaceInterpolator _surfaceFitter = new VolatilitySurfaceInterpolator(new SmileInterpolatorSpline());
  private final LocalVolatilitySurfaceStrike _localVolatilityStrike;
  private final LocalVolatilitySurfaceMoneyness _localVolatilityMoneyness;
  private final SmileSurfaceDataBundle _marketData;

  private final boolean _isCall;

  private final double _theta;
  private final int _timeSteps;
  private final int _spaceSteps;
  private final double _timeGridBunching;
  private final double _spaceGridBunching;

  public LocalVolatilityPDEGreekCalculator(final ForwardCurve forwardCurve, final double[] expiries, final double[][] strikes, final double[][] impliedVols,
      final boolean isCall) {

    _marketData = new StandardSmileSurfaceDataBundle(forwardCurve, expiries, strikes, impliedVols);

    Validate.notNull(forwardCurve, "null forward curve");
    Validate.notNull(expiries, "null expiries");
    Validate.notNull(strikes, "null strikes");
    Validate.notNull(impliedVols, "null impliedVols");

    _isCall = isCall;

    _theta = 0.5; //0.55;
    _timeSteps = 100;
    _spaceSteps = 100;
    _timeGridBunching = 5.0;
    _spaceGridBunching = 0.05;

    final BlackVolatilitySurfaceMoneyness impVolSurface = _surfaceFitter.getVolatilitySurface(_marketData);
    _localVolatilityMoneyness = DUPIRE.getLocalVolatility(impVolSurface);
    _localVolatilityStrike = LocalVolatilitySurfaceConverter.toStrikeSurface(_localVolatilityMoneyness);
  }

  /**
   * Run  a backwards PDE solver for the given option (i.e. expiry and strike), and print out the values of initial forward (F(0,T)), forward price,
   * and implied volatility
   * @param ps a print stream
   * @param expiry the expiry of the option
   * @param strike the option's strike
   */
  public void runBackwardsPDESolver(final PrintStream ps, final double expiry, final double strike) {

    final double forward = _marketData.getForwardCurve().getForward(expiry);
    final double maxSpot = 3.5 * forward;

    final PDEResults1D res = runBackwardsPDESolver(strike, _localVolatilityStrike, _isCall, _theta, expiry, maxSpot,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, forward);
    final int n = res.getGrid().getNumSpaceNodes();
    for (int i = 0; i < n; i++) {
      final double price = res.getFunctionValue(i);
      final double f = res.getSpaceValue(i);
      try {
        final double vol = BlackFormulaRepository.impliedVolatility(price, f, strike, expiry, _isCall);
        ps.println(f + "\t" + price + "\t" + vol);
      } catch (final Exception e) {

      }
    }
  }

  /**
   * Run a forward PDE solver to get model prices (and thus implied vols) and compare these with the market data.
   * Also output the (model) implied volatility as a function of strike for each tenor.
   * @param ps The print stream
   */
  public void runPDESolver(final PrintStream ps) {
    final int n = _marketData.getNumExpiries();
    final double[] expiries = _marketData.getExpiries();
    final double[][] strikes = _marketData.getStrikes();
    final double[][] vols = _marketData.getVolatilities();
    final double maxT = expiries[n - 1];
    final double maxProxyDelta = 0.4;

    final PDEFullResults1D pdeRes = runForwardPDESolver(_localVolatilityMoneyness, _isCall, _theta, maxT, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final BlackVolatilitySurfaceMoneyness pdeVolSurface = modifiedPriceToVolSurface(_marketData.getForwardCurve(), pdeRes, 0, maxT, 0.3, 3.0, _isCall);
    double chiSq = 0;
    for (int i = 0; i < n; i++) {
      final int m = strikes[i].length;
      final double t = expiries[i];
      for (int j = 0; j < m; j++) {
        final double k = strikes[i][j];
        final double mrtVol = vols[i][j];
        final double modelVol = pdeVolSurface.getVolatility(t, k);
        ps.println(expiries[i] + "\t" + k + "\t" + mrtVol + "\t" + modelVol);
        chiSq += (mrtVol - modelVol) * (mrtVol - modelVol);
      }
    }
    ps.println("chi^2 " + chiSq * 1e6);

    ps.print("\n");
    ps.println("strike sensitivity");
    for (int i = 0; i < n; i++) {
      ps.print(expiries[i] + "\t" + "" + "\t");
    }
    ps.print("\n");
    for (int i = 0; i < n; i++) {
      ps.print("Strike\tImplied Vol\t");
    }
    ps.print("\n");
    for (int j = 0; j < 100; j++) {
      for (int i = 0; i < n; i++) {
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

  public void prices(final PrintStream ps, final double expiry, final double[] strikes) {
    prices(ps, expiry, strikes, _localVolatilityStrike);
  }

  public void prices(final PrintStream ps, final double expiry, final double[] strikes,
      final LocalVolatilitySurfaceStrike localVol) {
    final ForwardCurve forwardCurve = _marketData.getForwardCurve();
    final double forward = forwardCurve.getForward(expiry);

    double maxProxyDelta = 1.5;
    final int nStrikes = strikes.length;
    for (int i = 0; i < nStrikes; i++) {
      final double d = Math.abs(Math.log(strikes[i] / forward) / Math.sqrt(expiry));
      if (d > maxProxyDelta) {
        maxProxyDelta = d;
      }
    }

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVol, _isCall, _theta, expiry, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final int n = pdeRes.getNumberSpaceNodes();
    double[] logM = new double[n];
    double[] impVol = new double[n];
    int count = 0;

    for (int i = 0; i < n; i++) {
      final double m = pdeRes.getSpaceValue(i);
      if (m > 0.3 && m < 3.0) {
        final double mPrice = pdeRes.getFunctionValue(i);
        try {
          impVol[count] = BlackFormulaRepository.impliedVolatility(mPrice, 1.0, m, expiry, _isCall);
          logM[count] = Math.log(m);
          count++;
        } catch (final Exception e) {
        }
      }
    }

    logM = Arrays.copyOfRange(logM, 0, count);
    impVol = Arrays.copyOfRange(impVol, 0, count);
    final Interpolator1DDoubleQuadraticDataBundle data = INTERPOLATOR_1D.getDataBundle(logM, impVol);
    for (int i = 0; i < nStrikes; i++) {
      final double vol = INTERPOLATOR_1D.interpolate(data, Math.log(strikes[i] / forward));
      final double price = BlackFormulaRepository.price(forward, strikes[i], expiry, vol, _isCall);
      ps.println(strikes[i] + "\t" + vol + "\t" + price);
    }

  }

  /**
   * Runs both forward and backwards PDE solvers, and produces delta and gamma (plus the dual - i.e. with respect to strike)
   * values again strike and spot, for the given expiry and strike using the calculated local volatility
   * @param ps Print Stream
   * @param expiry the expiry of test option
   * @param strike the strike of test option
   */
  public void deltaAndGamma(final PrintStream ps, final double expiry, final double strike) {
    deltaAndGamma(ps, expiry, strike, _localVolatilityStrike);
  }

  /**
   * Runs both forward and backwards PDE solvers, and produces delta and gamma (plus the dual - i.e. with respect to strike)
   * values again strike and spot, for the given expiry and strike using the provided local volatility (i.e. override
   * that calculated from the fitted implied volatility surface).
   * @param ps Print Stream
   * @param expiry the expiry of test option
   * @param strike the strike of test option
   * @param localVol The local volatility
   */
  public void deltaAndGamma(final PrintStream ps, final double expiry, final double strike,
      final LocalVolatilitySurfaceStrike localVol) {

    final ForwardCurve forwardCurve = _marketData.getForwardCurve();
    final double forward = forwardCurve.getForward(expiry);

    final double shift = 1e-2;

    final double maxForward = 3.5 * forward;
    final double maxProxyDelta = 1.5;

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVol, _isCall, _theta, expiry, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResUp = runForwardPDESolver(forwardCurve.withFractionalShift(shift), localVol, _isCall, _theta, expiry, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResDown = runForwardPDESolver(forwardCurve.withFractionalShift(-shift), localVol, _isCall, _theta, expiry, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);

    final int n = pdeRes.getNumberSpaceNodes();

    ps.println("Result of running Forward PDE solver - this gives you a grid of prices at expiries and strikes for a spot " +
        "and forward curve. Dual delta and gamma are calculated by finite difference on the PDE grid. Spot delta and " +
        "gamma are calculated by ");
    ps.println("Strike\tVol\tBS Delta\tDelta\tBS Dual Delta\tDual Delta\tBS Gamma\tGamma\tBS Dual Gamma\tDual Gamma");
    //\tsurface delta\tsurface gamma\t surface cross gamma\tmodel dg");

    final double minM = Math.exp(-1.0 * Math.sqrt(expiry));
    final double maxM = 1.0 / minM;
    for (int i = 0; i < n; i++) {
      final double m = pdeRes.getSpaceValue(i);
      if (m > minM && maxM < 3.0) {
        final double k = m * forward;

        final double mPrice = pdeRes.getFunctionValue(i);
        double impVol = 0;
        try {
          impVol = BlackFormulaRepository.impliedVolatility(mPrice, 1.0, m, expiry, _isCall);
        } catch (final Exception e) {
        }

        final double bsDelta = BlackFormulaRepository.delta(forward, k, expiry, impVol, _isCall);
        final double bsDualDelta = BlackFormulaRepository.dualDelta(forward, k, expiry, impVol, _isCall);
        final double bsGamma = BlackFormulaRepository.gamma(forward, k, expiry, impVol);
        final double bsDualGamma = BlackFormulaRepository.dualGamma(forward, k, expiry, impVol);

        final double modelDD = pdeRes.getFirstSpatialDerivative(i);
        final double fixedSurfaceDelta = mPrice - m * modelDD; //i.e. the delta if the moneyness parameterised local vol surface was invariant to forward
        final double surfaceDelta = (pdeResUp.getFunctionValue(i) - pdeResDown.getFunctionValue(i)) / 2 / forward / shift;
        final double modelDelta = fixedSurfaceDelta + forward * surfaceDelta;

        final double modelDG = pdeRes.getSecondSpatialDerivative(i) / forward;
        final double crossGamma = (pdeResUp.getFirstSpatialDerivative(i) - pdeResDown.getFirstSpatialDerivative(i)) / 2 / forward / shift;
        final double surfaceGamma = (pdeResUp.getFunctionValue(i) + pdeResDown.getFunctionValue(i) - 2 * pdeRes.getFunctionValue(i)) / forward / shift / shift;
        final double modelGamma = 2 * surfaceDelta + surfaceGamma - 2 * m * crossGamma + m * m * modelDG;

        ps.println(k + "\t" + impVol + "\t" + bsDelta + "\t" + modelDelta + "\t" + bsDualDelta + "\t" + modelDD
            + "\t" + bsGamma + "\t" + modelGamma + "\t" + bsDualGamma + "\t" + modelDG);
        //  + "\t" + 2 * surfaceDelta + "\t" + surfaceGamma + "\t" + -2 * m * crossGamma + "\t" + m * m *  modelDG);
      }
    }
    ps.print("\n");

    //Now run the backwards solver and get delta and gamma off the grid
    ps.println("Result of running backwards PDE solver - this gives you a set of prices at different spot levels for a" +
        " single expiry and strike. Delta and gamma are calculated by finite difference on the grid");
    ps.println("Spot\tVol\tBS Delta\tDelta\tBS Gamma\tGamma");

    PDEResults1D res = runBackwardsPDESolver(strike, localVol, _isCall, _theta, expiry, maxForward,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, forward);

    for (int i = 0; i < n; i++) {
      final double price = res.getFunctionValue(i);
      final double fwd = res.getGrid().getSpaceNode(i);
      double impVol = 0;
      try {
        impVol = BlackFormulaRepository.impliedVolatility(price, fwd, strike, expiry, _isCall);
      } catch (final Exception e) {
      }
      final double bsDelta = BlackFormulaRepository.delta(fwd, strike, expiry, impVol, _isCall);
      final double bsGamma = BlackFormulaRepository.gamma(fwd, strike, expiry, impVol);

      final double modelDelta = res.getFirstSpatialDerivative(i);
      final double modelGamma = res.getSecondSpatialDerivative(i);

      ps.println(fwd + "\t" + impVol + "\t" + bsDelta + "\t" + modelDelta + "\t" + bsGamma + "\t" + modelGamma);
    }
    ps.print("\n");

    //finally run the backwards PDE solver 100 times with different strikes,  interpolating to get vol, delta and gamma at the forward
    final int xIndex = res.getGrid().getLowerBoundIndexForSpace(forward);
    final double actForward = res.getSpaceValue(xIndex);
    final double f1 = res.getSpaceValue(xIndex);
    final double f2 = res.getSpaceValue(xIndex + 1);
    final double w = (f2 - forward) / (f2 - f1);
    ps.println("True forward: " + forward + ", grid forward: " + actForward);
    ps.println("Result of running 100 backwards PDE solvers all with different strikes. Delta and gamma for each strike" +
        " is calculated from finite difference on the grid");
    ps.println("Strike\tVol\tDelta\tGamma");
    for (int i = 0; i < 100; i++) {
      final double k = forward * (0.3 + 2.7 * i / 99.0);
      res = runBackwardsPDESolver(k, localVol, _isCall, _theta, expiry, maxForward,
          _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, forward);

      double vol = 0;
      try {
        final double vol1 = BlackFormulaRepository.impliedVolatility(res.getFunctionValue(xIndex), f1, k, expiry, _isCall);
        final double vol2 = BlackFormulaRepository.impliedVolatility(res.getFunctionValue(xIndex + 1), f2, k, expiry, _isCall);
        vol = w * vol1 + (1 - w) * vol2;
      } catch (final Exception e) {
      }
      final double modelDelta = w * res.getFirstSpatialDerivative(xIndex) + (1 - w) * res.getFirstSpatialDerivative(xIndex + 1);
      final double modelGamma = w * res.getSecondSpatialDerivative(xIndex) + (1 - w) * res.getSecondSpatialDerivative(xIndex + 1);
      ps.println(k + "\t" + vol + "\t" + modelDelta + "\t" + modelGamma);
    }
  }

  /**
   * Dumped out the smile (from forward and backwards PDEs), when the local volatility (parameterised by strike) is held constant and the the forward curve is shift by some amount
   * @param ps Print Stream
   * @param expiry the expiry of test option
   * @param shift the fraction shift to the forward
   */
  public void smileDynamic(final PrintStream ps, final double expiry, final double shift) {
    final ForwardCurve forwardCurve = _marketData.getForwardCurve();
    smileDynamic(ps, expiry, _localVolatilityStrike, forwardCurve);
    smileDynamic(ps, expiry, _localVolatilityStrike, forwardCurve.withFractionalShift(shift));
  }

  public void smileDynamic(final PrintStream ps, final double expiry,
      final LocalVolatilitySurfaceStrike localVol, final ForwardCurve forwardCurve) {

    final double forward = forwardCurve.getForward(expiry);

    final double maxProxyDelta = 0.4;

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVol, _isCall, _theta, expiry, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);

    final int n = pdeRes.getNumberSpaceNodes();

    ps.println("Strike\tVol");

    for (int i = 0; i < n; i++) {
      final double m = pdeRes.getSpaceValue(i);
      if (m > 0.3 && m < 3.0) {
        final double k = m * forward;

        final double mPrice = pdeRes.getFunctionValue(i);
        double impVol = 0;
        try {
          impVol = BlackFormulaRepository.impliedVolatility(mPrice, 1.0, m, expiry, _isCall);
        } catch (final Exception e) {
        }
        ps.println(k + "\t" + impVol);
      }
    }
    ps.print("\n");

    //finally run the backwards PDE solver 100 times with different strikes,  interpolating to get vol
    final double maxForward = 3.5 * forward;
    PDEResults1D res = runBackwardsPDESolver(forward, localVol, _isCall, _theta, expiry, maxForward,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, forward);
    final int xIndex = res.getGrid().getLowerBoundIndexForSpace(forward);
    final double f1 = res.getSpaceValue(xIndex);
    final double f2 = res.getSpaceValue(xIndex + 1);
    final double w = (f2 - forward) / (f2 - f1);

    ps.println("Result of running 100 backwards PDE solvers all with different strikes. Delta and gamma for each strike" +
        " is calculated from finite difference on the grid");
    ps.println("Strike\tVol\tDelta\tGamma");
    for (int i = 0; i < 100; i++) {
      final double k = forward * (0.3 + 2.7 * i / 99.0);
      res = runBackwardsPDESolver(k, localVol, _isCall, _theta, expiry, maxForward,
          _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, forward);

      double vol = 0;
      try {
        final double vol1 = BlackFormulaRepository.impliedVolatility(res.getFunctionValue(xIndex), f1, k, expiry, _isCall);
        final double vol2 = BlackFormulaRepository.impliedVolatility(res.getFunctionValue(xIndex + 1), f2, k, expiry, _isCall);
        vol = w * vol1 + (1 - w) * vol2;
      } catch (final Exception e) {
      }

      ps.println(k + "\t" + vol);
    }
    ps.println("\n");

    //    //debug
    //    PDEFullResults1D pdeResDebug = runForwardPDESolverDebug(forward, localVol, _isCall, _theta, expiry, maxForward, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, forward);
    //    for (int i = 0; i < n; i++) {
    //      double k = pdeResDebug.getSpaceValue(i);
    //      double price = pdeResDebug.getFunctionValue(i);
    //      double impVol = 0;
    //      try {
    //        impVol = BlackFormulaRepository.impliedVolatility(price, forward, k, expiry, _isCall);
    //      } catch (Exception e) {
    //      }
    //      double priceShift = pdeResDebugShift.getFunctionValue(i);
    //      double impVolShift = 0;
    //      try {
    //        impVolShift = BlackFormulaRepository.impliedVolatility(priceShift, forwardShift, k, expiry, _isCall);
    //      } catch (Exception e) {
    //      }
    //      ps.println(k + "\t" + impVol + "\t" + impVolShift);
    //    }
  }

  /**
   * bumped each input volatility by 1bs and record the effect on the representative point by following the chain
   * of refitting the implied volatility surface, the local volatility surface and running the forward PDE solver
   * @param ps Print Stream
   * @param option test option
   */
  public void bucketedVegaForwardPDE(final PrintStream ps, final EuropeanVanillaOption option) {

    final int n = _marketData.getNumExpiries();
    final double[][] strikes = _marketData.getStrikes();
    final double forward = _marketData.getForwardCurve().getForward(option.getTimeToExpiry());
    final double maxT = option.getTimeToExpiry();
    final double maxProxyDelta = 0.4;
    final double x = option.getStrike() / forward;

    final PDEFullResults1D pdeRes = runForwardPDESolver(_localVolatilityMoneyness, _isCall, _theta, maxT, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);

    final double[] xNodes = pdeRes.getGrid().getSpaceNodes();
    int index = getLowerBoundIndex(xNodes, x);
    if (index >= 1) {
      index--;
    }
    if (index >= _spaceSteps - 1) {
      index--;
      if (index >= _spaceSteps - 1) {
        index--;
      }
    }
    final double[] vols = new double[4];
    final double[] moneyness = new double[4];
    System.arraycopy(xNodes, index, moneyness, 0, 4);
    for (int i = 0; i < 4; i++) {
      vols[i] = BlackFormulaRepository.impliedVolatility(pdeRes.getFunctionValue(index + i), 1.0, moneyness[i],
          option.getTimeToExpiry(), option.isCall());
    }
    Interpolator1DDoubleQuadraticDataBundle db = INTERPOLATOR_1D.getDataBundle(moneyness, vols);
    final double exampleVol = INTERPOLATOR_1D.interpolate(db, x);

    final double shiftAmount = 1e-4; //1bps

    final double[][] res = new double[n][];

    for (int i = 0; i < n; i++) {
      final int m = strikes[i].length;
      res[i] = new double[m];
      for (int j = 0; j < m; j++) {
        final BlackVolatilitySurfaceMoneyness bumpedSurface = _surfaceFitter.getBumpedVolatilitySurface(_marketData, i, j, shiftAmount);
        final LocalVolatilitySurfaceMoneyness bumpedLV = DUPIRE.getLocalVolatility(bumpedSurface);
        final PDEFullResults1D pdeResBumped = runForwardPDESolver(bumpedLV, _isCall, _theta, maxT, maxProxyDelta,
            _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
        for (int k = 0; k < 4; k++) {
          vols[k] = BlackFormulaRepository.impliedVolatility(pdeResBumped.getFunctionValue(index + k), 1.0, moneyness[k],
              option.getTimeToExpiry(), option.isCall());
        }
        db = INTERPOLATOR_1D.getDataBundle(moneyness, vols);
        final double vol = INTERPOLATOR_1D.interpolate(db, x);
        res[i][j] = (vol - exampleVol) / shiftAmount;
      }
    }

    for (int i = 0; i < n; i++) {
      //  System.out.print(TENORS[i] + "\t");
      final int m = strikes[i].length;
      for (int j = 0; j < m; j++) {
        ps.print(res[i][j] + "\t");
      }
      ps.print("\n");
    }
    ps.print("\n");
  }

  /**
   * bumped each input volatility by 1bs and record the effect on the representative point by following the chain
   * of refitting the implied volatility surface, the local volatility surface and running the backwards PDE solver
   * @param ps Print Stream
   * @param option test option
   */
  public void bucketedVegaBackwardsPDE(final PrintStream ps, final EuropeanVanillaOption option) {

    final int n = _marketData.getNumExpiries();
    final double[][] strikes = _marketData.getStrikes();
    final double forward = _marketData.getForwardCurve().getForward(option.getTimeToExpiry());
    final double maxFwd = 3.5 * Math.max(option.getStrike(), forward);
    final PDEResults1D pdeRes = runBackwardsPDESolver(option.getStrike(), _localVolatilityStrike, option.isCall(), _theta, option.getTimeToExpiry(),
        maxFwd, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());

    final double exampleVol;
    final double[] fwdNodes = pdeRes.getGrid().getSpaceNodes();
    int index = getLowerBoundIndex(fwdNodes, forward);
    if (index >= 1) {
      index--;
    }
    if (index >= _spaceSteps - 1) {
      index--;
      if (index >= _spaceSteps - 1) {
        index--;
      }
    }
    final double[] vols = new double[4];
    final double[] fwds = new double[4];
    System.arraycopy(fwdNodes, index, fwds, 0, 4);
    for (int i = 0; i < 4; i++) {
      vols[i] = BlackFormulaRepository.impliedVolatility(pdeRes.getFunctionValue(index + i), fwds[i], option.getStrike(), option.getTimeToExpiry(), option.isCall());
    }
    Interpolator1DDoubleQuadraticDataBundle db = INTERPOLATOR_1D.getDataBundle(fwds, vols);
    exampleVol = INTERPOLATOR_1D.interpolate(db, forward);

    final double shiftAmount = 1e-4; //1bps

    final double[][] res = new double[n][];

    for (int i = 0; i < n; i++) {
      final int m = strikes[i].length;
      res[i] = new double[m];
      for (int j = 0; j < m; j++) {
        final BlackVolatilitySurfaceMoneyness bumpedSurface = _surfaceFitter.getBumpedVolatilitySurface(_marketData, i, j, shiftAmount);
        final LocalVolatilitySurfaceStrike bumpedLV = LocalVolatilitySurfaceConverter.toStrikeSurface(DUPIRE.getLocalVolatility(bumpedSurface));
        final PDEResults1D pdeResBumped = runBackwardsPDESolver(option.getStrike(), bumpedLV, option.isCall(), _theta, option.getTimeToExpiry(),
            maxFwd, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());
        for (int k = 0; k < 4; k++) {
          vols[k] = BlackFormulaRepository.impliedVolatility(pdeResBumped.getFunctionValue(index + k), fwds[k], option.getStrike(), option.getTimeToExpiry(), option.isCall());
        }
        db = INTERPOLATOR_1D.getDataBundle(fwds, vols);
        final double vol = INTERPOLATOR_1D.interpolate(db, forward);
        res[i][j] = (vol - exampleVol) / shiftAmount;
      }
    }

    for (int i = 0; i < n; i++) {
      //     System.out.print(TENORS[i] + "\t");
      final int m = strikes[i].length;
      for (int j = 0; j < m; j++) {
        ps.print(res[i][j] + "\t");
      }
      ps.print("\n");
    }
    ps.print("\n");
  }

  /**
   * Get the volatility based Greeks (vega, vanna & vomma) for the provided option, by parallel bumping of calculated
   * local volatility surface and bumping of the spot rate. The option prices are calculated by running a forward PDE
   * solver
   * @param ps Print Stream
   * @param option test option
   */
  public void vega(final PrintStream ps, final EuropeanVanillaOption option) {
    vega(ps, option, _localVolatilityStrike);
  }

  /**
   * Get the volatility based Greeks (vega, vanna & vomma) for the provided option, by parallel bumping of supplied
   * local volatility surface and bumping of the spot rate. The option prices are calculated by running a forward PDE
   * solver
   * @param ps Print Stream
   * @param option test option
   * @param localVol the local volatility
   */
  public void vega(final PrintStream ps, final EuropeanVanillaOption option, final LocalVolatilitySurfaceStrike localVol) {
    final ForwardCurve forwardCurve = _marketData.getForwardCurve();
    final double forward = forwardCurve.getForward(option.getTimeToExpiry());
    final double maxT = option.getTimeToExpiry();
    final double maxProxyDelta = 1.5;
    final double volShift = 1e-3;
    //    final double fracShift = 5e-4;
    final double fwdShift = 5e-2;

    //parallel shift the strike parameterised local vol surface
    final LocalVolatilitySurfaceStrike lvUp = new LocalVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(localVol.getSurface(), volShift, true));
    final LocalVolatilitySurfaceStrike lvDown = new LocalVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(localVol.getSurface(), -volShift, true));

    //first order shifts
    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVol, _isCall, _theta, maxT, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEResults1D pdeResUp = runForwardPDESolver(forwardCurve, lvUp, _isCall, _theta, maxT, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEResults1D pdeResDown = runForwardPDESolver(forwardCurve, lvDown, _isCall, _theta, maxT, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);

    //second order shifts
    final PDEResults1D pdeResUpUp = runForwardPDESolver(forwardCurve.withFractionalShift(fwdShift), lvUp, _isCall, _theta, maxT, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResUpDown = runForwardPDESolver(forwardCurve.withFractionalShift(fwdShift), lvDown, _isCall, _theta, maxT, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResDownUp = runForwardPDESolver(forwardCurve.withFractionalShift(-fwdShift), lvUp, _isCall, _theta, maxT, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResDownDown = runForwardPDESolver(forwardCurve.withFractionalShift(-fwdShift), lvDown, _isCall, _theta, maxT, maxProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);

    ps.println("Strike\tBS Vega\tVega\tBS Vanna\tVanna\tBS Vomma\tVomma");
    final int n = pdeRes.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      final double x = pdeRes.getSpaceValue(i);
      final double k = x * forward;
      final double mPrice = pdeRes.getFunctionValue(i);
      try {
        final double bsVol = BlackFormulaRepository.impliedVolatility(mPrice, 1.0, x, maxT, _isCall);
        final double bsVega = BlackFormulaRepository.vega(forward, k, maxT, bsVol);
        final double bsVanna = BlackFormulaRepository.vanna(forward, k, maxT, bsVol);
        final double bsVomma = BlackFormulaRepository.vomma(forward, k, maxT, bsVol);
        final double modelVega = forward * (pdeResUp.getFunctionValue(i) - pdeResDown.getFunctionValue(i)) / 2 / volShift;

        //xVanna is the vanna if the moneyness parameterised local vol surface was invariant to changes in the forward curve
        final double xVanna = (pdeResUp.getFunctionValue(i) - pdeResDown.getFunctionValue(i)
            - x * (pdeResUp.getFirstSpatialDerivative(i) - pdeResDown.getFirstSpatialDerivative(i))) / 2 / volShift;
        //this is the vanna coming purely from deformation of the local volatility surface
        final double surfaceVanna = (pdeResUpUp.getFunctionValue(i) + pdeResDownDown.getFunctionValue(i) -
            pdeResUpDown.getFunctionValue(i) - pdeResDownUp.getFunctionValue(i)) / 4 / fwdShift / volShift;
        final double modelVanna = xVanna + surfaceVanna;
        final double modelVomma = forward * (pdeResUp.getFunctionValue(i) + pdeResDown.getFunctionValue(i)
            - 2 * pdeRes.getFunctionValue(i)) / volShift / volShift;
        ps.println(k + "\t" + bsVega + "\t" + modelVega + "\t" + bsVanna + "\t" + modelVanna + "\t" + bsVomma + "\t" + modelVomma);
      } catch (final Exception e) {
      }
    }

  }

  /**
   *
   * @param spot
   * @param localVolatility
   * @param isCall
   * @param theta The theta parameters of the PDE solver
   * @param maxT
   * @param maxMoneyness
   * @param nTimeSteps
   * @param nStrikeSteps
   * @param timeMeshLambda
   * @param strikeMeshBunching
   * @return
   */
  private PDEFullResults1D runForwardPDESolver(final LocalVolatilitySurfaceMoneyness localVolatility, final boolean isCall,
      final double theta, final double maxT, final double maxAbsProxyDelta, final int nTimeSteps, final int nStrikeSteps,
      final double timeMeshLambda, final double strikeMeshBunching, final double centreMoneyness) {

    final PDE1DCoefficientsProvider provider = new PDE1DCoefficientsProvider();
    final ConvectionDiffusionPDE1DCoefficients pde = provider.getForwardLocalVol(localVolatility);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, true);

    final double minMoneyness = Math.exp(-maxAbsProxyDelta * Math.sqrt(maxT));
    final double maxMoneyness = 1.0 / minMoneyness;
    ArgumentChecker.isTrue(minMoneyness < centreMoneyness, "min moneyness of {} greater than centreMoneyness of {}. Increase maxAbsProxydelta", minMoneyness, centreMoneyness);
    ArgumentChecker.isTrue(maxMoneyness > centreMoneyness, "max moneyness of {} less than centreMoneyness of {}. Increase maxAbsProxydelta", maxMoneyness, centreMoneyness);

    BoundaryCondition lower;
    BoundaryCondition upper;
    if (isCall) {
      //call option with low strike  is worth the forward - strike, while a put is worthless
      lower = new DirichletBoundaryCondition((1.0 - minMoneyness), minMoneyness);
      upper = new DirichletBoundaryCondition(0.0, maxMoneyness);
    } else {
      lower = new DirichletBoundaryCondition(0.0, minMoneyness);
      upper = new NeumannBoundaryCondition(1.0, maxMoneyness, false);
    }

    final MeshingFunction timeMesh = new ExponentialMeshing(0.0, maxT, nTimeSteps, timeMeshLambda);

    final MeshingFunction spaceMesh = new HyperbolicMeshing(minMoneyness, maxMoneyness, centreMoneyness, nStrikeSteps, strikeMeshBunching);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final Function1D<Double, Double> intCond = (new InitialConditionsProvider()).getForwardCallPut(isCall);
    final PDEFullResults1D res = (PDEFullResults1D) solver.solve(new PDE1DDataBundle<>(pde, intCond, lower, upper, grid));
    return res;
  }

  private PDEFullResults1D runForwardPDESolver(final ForwardCurve forwardCurve, final LocalVolatilitySurfaceStrike localVolatility,
      final boolean isCall, final double theta, final double maxT, final double maxAbsProxyDelta, final int nTimeSteps, final int nStrikeSteps,
      final double timeMeshLambda, final double strikeMeshBunching, final double centreMoneyness) {
    final LocalVolatilitySurfaceMoneyness ms = LocalVolatilitySurfaceConverter.toMoneynessSurface(localVolatility, forwardCurve);
    return runForwardPDESolver(ms, isCall, theta, maxT, maxAbsProxyDelta, nTimeSteps, nStrikeSteps, timeMeshLambda, strikeMeshBunching, centreMoneyness);
  }

  /**
   * Runs a backwards PDE (i.e the initial condition is at t = expiry, and the final solution is a t = 0) on a grid of time and forward, F(t,T), points
   * for an option with a given strike under a local volatility model
   * @param strike The strike of the option
   * @param localVolatility Local volatility surface
   * @param isCall true for call
   * @param theta Balance between fully explicit (theta = 0) and fully implicit (theta = 1) time marching schemes. Theta = 0.5 is Crank-Nicolson
   * @param expiry The time-to-expiry of the option
   * @param maxFwd The maximum forward in the grid (should be a few times larger than the forward F(0,T) and/or the strike)
   * @param nTimeNodes Number of nodes in the time direction
   * @param nFwdNodes Number of nodes in the forward (space) direction
   * @param timeMeshLambda for lambda > 0 the point are bunched around tau = 0
   * @param spotMeshBunching as this -> 0 points bunched around fwdNodeCentre
   * @param fwdNodeCentre The point with the highest concentration of space nodes
   * @return PDEResults1D which contains the forward (i.e. non-discounted) option prices and different initial levels of the forward F(0,T)
   */
  private PDEResults1D runBackwardsPDESolver(final double strike, final LocalVolatilitySurfaceStrike localVolatility, final boolean isCall,
      final double theta, final double expiry, final double maxFwd, final int
      nTimeNodes, final int nFwdNodes, final double timeMeshLambda, final double spotMeshBunching, final double fwdNodeCentre) {
    final ForwardCurve forwardCurve = _marketData.getForwardCurve();

    final PDE1DCoefficientsProvider pdeProvider = new PDE1DCoefficientsProvider();
    final InitialConditionsProvider intProvider = new InitialConditionsProvider();
    final ConvectionDiffusionPDE1DCoefficients pde = pdeProvider.getBackwardsLocalVol(forwardCurve, expiry, localVolatility);
    final Function1D<Double, Double> payoff = intProvider.getEuropeanPayoff(strike, isCall);
    // final ZZConvectionDiffusionPDEDataBundle db = provider.getBackwardsLocalVol(strike, expiry, isCall, localVolatility, forwardCurve);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower;
    BoundaryCondition upper;
    if (isCall) {
      lower = new DirichletBoundaryCondition(0.0, 0.0); //call option with strike zero is worth 0
      upper = new NeumannBoundaryCondition(1.0, maxFwd, false);
    } else {
      lower = new DirichletBoundaryCondition(strike, 0.0);
      upper = new NeumannBoundaryCondition(0.0, maxFwd, false);
    }

    // MeshingFunction timeMesh = new ExponentialMeshing(0.0, expiry, nTimeNodes, timeMeshLambda);
    final MeshingFunction timeMesh = new DoubleExponentialMeshing(0, expiry, expiry / 2, nTimeNodes, timeMeshLambda, -timeMeshLambda);
    //keep the grid the same regardless of spot (useful for finite-difference)
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, maxFwd, fwdNodeCentre, nFwdNodes, spotMeshBunching);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<>(pde, payoff, lower, upper, grid);
    final PDEResults1D res = solver.solve(db);
    return res;
  }

  /**
   * Convert the results of running the forward PDE, which are forward option prices divided by the relevant forward, to an implied volatility
   * surface parameterised by expiry and moneyness (=strike/forward)
   * @param forwardCurve forward curve
   * @param prices grid of (modified) prices
   * @param minT min time
   * @param maxT max time
   * @param minMoneyness the minimum moneyness
   * @param maxMoneyness the max moneyness
   * @param isCall true for call
   * @return BlackVolatilitySurfaceMoneyness
   */
  public static BlackVolatilitySurfaceMoneyness modifiedPriceToVolSurface(final ForwardCurve forwardCurve, final PDEFullResults1D prices,
      final double minT, final double maxT, final double minMoneyness, final double maxMoneyness, final boolean isCall) {

    final Map<DoublesPair, Double> vol = PDEUtilityTools.modifiedPriceToImpliedVol(prices, minT, maxT, minMoneyness, maxMoneyness, isCall);
    final Map<Double, Interpolator1DDataBundle> idb = GRID_INTERPOLATOR2D.getDataBundle(vol);

    final Function<Double, Double> func = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tk) {
        return GRID_INTERPOLATOR2D.interpolate(idb, DoublesPair.of(tk[0].doubleValue(), tk[1].doubleValue()));
      }
    };

    return new BlackVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(func), forwardCurve);
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

}
