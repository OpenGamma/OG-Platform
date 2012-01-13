/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.Validate;

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
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.financial.model.volatility.local.LocalVolatilitySurface;
import com.opengamma.math.function.Function;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.SurfaceShiftFunctionFactory;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class LocalVolatilityPDEGreekCalculator {

  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final CombinedInterpolatorExtrapolator EXTRAPOLATOR_1D = new CombinedInterpolatorExtrapolator(INTERPOLATOR_1D, new FlatExtrapolator1D());
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(EXTRAPOLATOR_1D, EXTRAPOLATOR_1D);
  private static final DupireLocalVolatilityCalculator DUPIRE = new DupireLocalVolatilityCalculator();

  private final PiecewiseSABRSurfaceFitter _surfaceFitter;
  private final LocalVolatilitySurface _localVolatility;
  private final ForwardCurve _forwardCurve;
  private final double[] _expiries;
  private final double[][] _strikes;
  private final double[][] _impliedVols;
  private final int _nExpiries;

  private final boolean _isCall;

  private final double _modMoneynessParameter;

  private final double _theta;
  private final int _timeSteps;
  private final int _spaceSteps;
  private final double _timeGridBunching;
  private final double _spaceGridBunching;

  public LocalVolatilityPDEGreekCalculator(final ForwardCurve forwardCurve, final double[] expiries, final double[][] strikes, final double[][] impliedVols,
      final boolean isCall) {

    Validate.notNull(forwardCurve, "null forward curve");
    Validate.notNull(expiries, "null expiries");
    Validate.notNull(strikes, "null strikes");
    Validate.notNull(impliedVols, "null impliedVols");
    // Validate.notNull(localVolatility, "null local vol");

    _nExpiries = expiries.length;
    Validate.isTrue(_nExpiries == strikes.length, "wrong number of strike sets");
    Validate.isTrue(_nExpiries == impliedVols.length, "wrong number of implied vol sets");

    _forwardCurve = forwardCurve;
    _expiries = expiries;
    _strikes = strikes;
    _impliedVols = impliedVols;
    _isCall = isCall;

    _modMoneynessParameter = 100.0;

    _theta = 0.55;
    _timeSteps = 100;
    _spaceSteps = 100;
    _timeGridBunching = 5.0;
    _spaceGridBunching = 0.05;

    final double[] forwards = new double[_nExpiries];
    for (int i = 0; i < _nExpiries; i++) {
      forwards[i] = forwardCurve.getForward(_expiries[i]);
    }

    _surfaceFitter = new PiecewiseSABRSurfaceFitter(forwards, expiries, strikes, impliedVols);
    final BlackVolatilitySurface impVolSurface = _surfaceFitter.getImpliedVolatilitySurface(true, false, _modMoneynessParameter);
    _localVolatility = DUPIRE.getLocalVolatility(impVolSurface, forwardCurve);
  }

  /**
   * Run a forward PDE solver to get model prices (and thus implied vols) and compare these with the market data.
   * Also output the (model) implied volatility as a function of strike for each tenor.
   * @param ps The print stream
   */
  public void runPDESolver(final PrintStream ps) {

    double minK = Double.POSITIVE_INFINITY;
    double maxK = 0.0;
    for (int i = 0; i < _nExpiries; i++) {
      final int m = _strikes[i].length;
      for (int j = 0; j < m; j++) {
        final double k = _strikes[i][j];
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

    final double spot = _forwardCurve.getSpot();
    final double maxT = _expiries[_nExpiries - 1];
    final double maxStrike = 3.5 * _forwardCurve.getForward(maxT);

    final PDEFullResults1D pdeRes = runForwardPDESolver(_forwardCurve, _localVolatility, _isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final BlackVolatilitySurface pdeVolSurface = priceToVolSurface(_forwardCurve, pdeRes, 0, maxT, minK, maxK);

    double chiSq = 0;
    for (int i = 0; i < _nExpiries; i++) {
      final int m = _strikes[i].length;
      final double t = _expiries[i];
      for (int j = 0; j < m; j++) {
        final double k = _strikes[i][j];

        final double mrtVol = _impliedVols[i][j];
        final double modelVol = pdeVolSurface.getVolatility(t, k);
        System.out.println(_expiries[i] + "\t" + k + "\t" + mrtVol + "\t" + modelVol);
        chiSq += (mrtVol - modelVol) * (mrtVol - modelVol);
      }
    }
    ps.println("chi^2 " + chiSq * 1e6);

    ps.print("\n");
    ps.println("strike sensitivity");
    for (int i = 0; i < _nExpiries; i++) {
      ps.print(_expiries[i] + "\t" + "" + "\t");
    }
    ps.print("\n");
    for (int i = 0; i < _nExpiries; i++) {
      ps.print("Strike\tImplied Vol\t");
    }
    ps.print("\n");
    for (int j = 0; j < 100; j++) {
      for (int i = 0; i < _nExpiries; i++) {
        final int m = _strikes[i].length;
        final double t = _expiries[i];
        final double kLow = _strikes[i][0];
        final double kHigh = _strikes[i][m - 1];
        final double k = kLow + (kHigh - kLow) * j / 99.;
        ps.print(k + "\t" + pdeVolSurface.getVolatility(t, k) + "\t");
      }
      ps.print("\n");
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
    deltaAndGamma(ps, expiry, strike, _localVolatility);
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
  public void deltaAndGamma(final PrintStream ps, final double expiry, final double strike, final LocalVolatilitySurface localVol) {

    double minK = Double.POSITIVE_INFINITY;
    double maxK = 0.0;
    for (int i = 0; i < _nExpiries; i++) {
      final int m = _strikes[i].length;
      for (int j = 0; j < m; j++) {
        final double k = _strikes[i][j];
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

    final double spot = _forwardCurve.getSpot();
    final double forward = _forwardCurve.getForward(expiry);
    final double shift = 5e-2 * spot;
    final double maxT = _expiries[_nExpiries - 1];
    final double maxStrike = 3.5 * _forwardCurve.getForward(maxT);
    final double maxSpot = 3.5 * Math.max(strike, _forwardCurve.getForward(maxT));

    final PDEFullResults1D pdeRes = runForwardPDESolver(_forwardCurve, localVol, _isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final PDEFullResults1D pdeResUp = runForwardPDESolver(_forwardCurve.withShiftedSpot(shift), localVol, _isCall,
        _theta, maxT, maxStrike, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final PDEFullResults1D pdeResDown = runForwardPDESolver(_forwardCurve.withShiftedSpot(-shift), localVol, _isCall,
        _theta, maxT, maxStrike, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);

    final BlackVolatilitySurface modelSurface = priceToVolSurface(_forwardCurve, pdeRes, 0.0, maxT, 0, maxStrike);
    final double[] timeNodes = pdeRes.getGrid().getTimeNodes();

    //adjust the expiry to the nearest grid point
    //TODO should be able to adjust the grid instead
    int tIndex = getLowerBoundIndex(timeNodes, expiry);
    if (tIndex < timeNodes.length - 1) {
      final double dT1 = (expiry - timeNodes[tIndex]);
      final double dT2 = (timeNodes[tIndex + 1] - expiry);
      if (dT1 > dT2) {
        tIndex++;
      }
    }
    final double actExpiry = timeNodes[tIndex];
    ps.println("Requied expiry: " + expiry + " Actual grid time: " + actExpiry);

    //get dual delta & gamma by finite difference on grid, and (normal) delta and gamma by fd on separate grids, for
    // a range of strikes (i.e. the spot is fixed0
    final int n = pdeRes.getNumberSpaceNodes();
    ps.println("Result of running Forward PDE solver - this gives you a grid of prices at expiries and strikes for a spot " +
        "and forward curve. Dual delta and gamma are calculated by finite difference on the PDE grid. Spot delta and " +
        "gamma are calculated by ");
    ps.println("Strike\tVol\tBS Delta\tDelta\tBS Dual Delta\tDual Delta\tBS Gamma\tGamma\tBS Dual Gamma\tDual Gamma");
    for (int i = 0; i < n; i++) {
      final double k = pdeRes.getSpaceValue(i);
      final double bsVol = modelSurface.getVolatility(actExpiry, k);
      final double bsDelta = BlackFormulaRepository.delta(forward, k, actExpiry, bsVol, _isCall);
      final double bsDualDelta = BlackFormulaRepository.dualDelta(forward, k, actExpiry, bsVol, _isCall);
      final double bsGamma = BlackFormulaRepository.gamma(forward, k, actExpiry, bsVol);
      final double bsDualGamma = BlackFormulaRepository.dualGamma(forward, k, actExpiry, bsVol);
      final double modelDelta = (pdeResUp.getFunctionValue(i, tIndex) - pdeResDown.getFunctionValue(i, tIndex)) / 2 / shift;
      final double modelDD = pdeRes.getFirstSpatialDerivative(i, tIndex);
      final double modelGamma = (pdeResUp.getFunctionValue(i, tIndex) + pdeResDown.getFunctionValue(i, tIndex) -
          2 * pdeRes.getFunctionValue(i, tIndex)) / shift / shift;
      final double modelDG = pdeRes.getSecondSpatialDerivative(i, tIndex);
      //      ps.println("debug\t" + k + "\t" + modelDelta + "\t" + pdeResUp.getFunctionValue(i, tIndex) +
      //          "\t" + pdeResDown.getFunctionValue(i, tIndex) + "\t" + pdeRes.getFunctionValue(i, tIndex));
      ps.println(k + "\t" + bsVol + "\t" + bsDelta + "\t" + modelDelta + "\t" + bsDualDelta + "\t" + modelDD
          + "\t" + bsGamma + "\t" + modelGamma + "\t" + bsDualGamma + "\t" + modelDG);
    }
    ps.print("\n");

    //Now run the backwards solver and get delta and gamma off the grid
    ps.println("Result of running backwards PDE solver - this gives you a set of prices at different spot levels for a" +
        " single expiry and strike. Delta and gamma are calculated by finite difference on the grid");
    ps.println("Spot\tVol\tBS Delta\tDelta\tBS Gamma\tGamma");

    PDEResults1D res = runBackwardsPDESolver(strike, localVol, _isCall, _theta, actExpiry, maxSpot,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final double[] impVol = new double[n];
    for (int i = 0; i < n; i++) {
      final double price = res.getFunctionValue(i);
      final double gridSpot = res.getGrid().getSpaceNode(i);
      try {
        impVol[i] = BlackFormulaRepository.impliedVolatility(price, gridSpot, strike, actExpiry, _isCall);
      } catch (final Exception e) {
      }
      final double bsDelta = BlackFormulaRepository.delta(gridSpot, strike, actExpiry, impVol[i], _isCall);
      final double bsGamma = BlackFormulaRepository.gamma(gridSpot, strike, actExpiry, impVol[i]);

      final double modelDelta = res.getFirstSpatialDerivative(i);
      final double modelGamma = res.getSecondSpatialDerivative(i);

      ps.println(gridSpot + "\t" + impVol[i] + "\t" + bsDelta + "\t" + modelDelta + "\t" + bsGamma + "\t" + modelGamma);
    }
    ps.print("\n");

    //finally run the backwards PDE solver 100 times with different strikes
    final int xIndex = res.getGrid().getLowerBoundIndexForSpace(spot);
    final double actSpot = res.getSpaceValue(xIndex);
    ps.println("True Spot: " + spot + ", grid spot: " + actSpot);
    ps.println("Result of running 100 backwards PDE solvers all with different strikes. Delta and gamma for each strike" +
        " is calculated from finite difference on the grid");
    ps.println("Strike\tVol\tDelta\tGamma");
    for (int i = 0; i < 100; i++) {
      final double k = minK + (maxK - minK) * i / 99.0;
      res = runBackwardsPDESolver(k, localVol, _isCall, _theta, actExpiry, maxSpot,
          _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
      final double price = res.getFunctionValue(xIndex);
      double vol = 0;
      try {
        vol = BlackFormulaRepository.impliedVolatility(price, actSpot, k, actExpiry, _isCall);
      } catch (final Exception e) {
      }
      final double modelDelta = res.getFirstSpatialDerivative(xIndex);
      final double modelGamma = res.getSecondSpatialDerivative(xIndex);
      ps.println(k + "\t" + vol + "\t" + modelDelta + "\t" + modelGamma);
    }
  }

  /**
   * bumped each input volatility by 1bs and record the effect on the representative point by following the chain
   * of refitting the implied volatility surface, the local volatility surface and running the forward PDE solver
   * @param ps Print Stream
   * @param option test option
   */
  public void bucketedVegaForwardPDE(final PrintStream ps, final EuropeanVanillaOption option) {

    final double forward = _forwardCurve.getForward(option.getTimeToExpiry());
    final double maxT = option.getTimeToExpiry();
    final double maxStrike = 3.5 * _forwardCurve.getForward(maxT);

    final PDEFullResults1D pdeRes = runForwardPDESolver(_forwardCurve, _localVolatility, _isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());
    //    final PDEFullResults1D pdeRes = runForwardPDESolver(_forwardCurve, localVol, _isCall, _theta, maxT, maxStrike,
    //        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final double[] strikeNodes = pdeRes.getGrid().getSpaceNodes();
    int index = getLowerBoundIndex(strikeNodes, option.getStrike());
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
    final double[] strikes = new double[4];
    System.arraycopy(strikeNodes, index, strikes, 0, 4);
    for (int i = 0; i < 4; i++) {
      vols[i] = BlackFormulaRepository.impliedVolatility(pdeRes.getFunctionValue(index + i), forward, strikes[i],
          option.getTimeToExpiry(), option.isCall());
    }
    Interpolator1DDoubleQuadraticDataBundle db = INTERPOLATOR_1D.getDataBundle(strikes, vols);
    final double exampleVol = INTERPOLATOR_1D.interpolate(db, option.getStrike());

    final double shiftAmount = 1e-4; //1bps

    final double[][] res = new double[_nExpiries][];

    for (int i = 0; i < _nExpiries; i++) {
      final int m = _strikes[i].length;
      res[i] = new double[m];
      for (int j = 0; j < m; j++) {
        final PiecewiseSABRSurfaceFitter fitter = _surfaceFitter.withBumpedPoint(i, j, shiftAmount);
        final BlackVolatilitySurface bumpedSurface = fitter.getImpliedVolatilitySurface(true, false, _modMoneynessParameter);
        final LocalVolatilitySurface bumpedLV = DUPIRE.getLocalVolatility(bumpedSurface, _forwardCurve);
        final PDEFullResults1D pdeResBumped = runForwardPDESolver(_forwardCurve, bumpedLV, _isCall, _theta, maxT,
            maxStrike, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());
        for (int k = 0; k < 4; k++) {
          vols[k] = BlackFormulaRepository.impliedVolatility(pdeResBumped.getFunctionValue(index + k), forward, strikes[k],
              option.getTimeToExpiry(), option.isCall());
        }
        db = INTERPOLATOR_1D.getDataBundle(strikes, vols);
        final double vol = INTERPOLATOR_1D.interpolate(db, option.getStrike());
        res[i][j] = (vol - exampleVol) / shiftAmount;
      }
    }

    for (int i = 0; i < _nExpiries; i++) {
      //  System.out.print(TENORS[i] + "\t");
      final int m = _strikes[i].length;
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
    final double spot = _forwardCurve.getSpot();
    final double forward = _forwardCurve.getForward(option.getTimeToExpiry());
    final double maxSpot = 3.5 * Math.max(option.getStrike(), forward);
    final PDEResults1D pdeRes = runBackwardsPDESolver(option.getStrike(), _localVolatility, option.isCall(), _theta, option.getTimeToExpiry(),
        maxSpot, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());

    double exampleVol;
    final double[] spotNodes = pdeRes.getGrid().getSpaceNodes();
    int index = getLowerBoundIndex(spotNodes, spot);
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
    final double[] spots = new double[4];
    System.arraycopy(spotNodes, index, spots, 0, 4);
    for (int i = 0; i < 4; i++) {
      vols[i] = BlackFormulaRepository.impliedVolatility(pdeRes.getFunctionValue(index + i), spots[i], option.getStrike(), option.getTimeToExpiry(), option.isCall());
    }
    Interpolator1DDoubleQuadraticDataBundle db = INTERPOLATOR_1D.getDataBundle(spots, vols);
    exampleVol = INTERPOLATOR_1D.interpolate(db, spot);

    final double shiftAmount = 1e-4; //1bps

    final double[][] res = new double[_nExpiries][];

    for (int i = 0; i < _nExpiries; i++) {
      final int m = _strikes[i].length;
      res[i] = new double[m];
      for (int j = 0; j < m; j++) {
        final PiecewiseSABRSurfaceFitter fitter = _surfaceFitter.withBumpedPoint(i, j, shiftAmount);
        final BlackVolatilitySurface bumpedSurface = fitter.getImpliedVolatilitySurface(true, false, _modMoneynessParameter);
        final LocalVolatilitySurface bumpedLV = DUPIRE.getLocalVolatility(bumpedSurface, _forwardCurve);
        final PDEResults1D pdeResBumped = runBackwardsPDESolver(option.getStrike(), bumpedLV, option.isCall(), _theta, option.getTimeToExpiry(),
            maxSpot, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());
        for (int k = 0; k < 4; k++) {
          vols[k] = BlackFormulaRepository.impliedVolatility(pdeResBumped.getFunctionValue(index + k), spots[k], option.getStrike(), option.getTimeToExpiry(), option.isCall());
        }
        db = INTERPOLATOR_1D.getDataBundle(spots, vols);
        final double vol = INTERPOLATOR_1D.interpolate(db, spot);
        res[i][j] = (vol - exampleVol) / shiftAmount;
      }
    }

    for (int i = 0; i < _nExpiries; i++) {
      //     System.out.print(TENORS[i] + "\t");
      final int m = _strikes[i].length;
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
    vega(ps, option, _localVolatility);
  }

  /**
   * Get the volatility based Greeks (vega, vanna & vomma) for the provided option, by parallel bumping of supplied
   * local volatility surface and bumping of the spot rate. The option prices are calculated by running a forward PDE
   * solver
   * @param ps Print Stream
   * @param option test option
   * @param localVol the local volatility
   */
  public void vega(final PrintStream ps, final EuropeanVanillaOption option, final LocalVolatilitySurface localVol) {
    final double spot = _forwardCurve.getSpot();
    final double forward = _forwardCurve.getForward(option.getTimeToExpiry());
    final double maxT = option.getTimeToExpiry();
    final double maxStrike = 3.5 * _forwardCurve.getForward(maxT);
    final double volShift = 1e-4;
    final double spotShift = 5e-2 * spot;

    final LocalVolatilitySurface lvUp = new LocalVolatilitySurface(SurfaceShiftFunctionFactory.getShiftedSurface(localVol.getSurface(), volShift, true));
    final LocalVolatilitySurface lvDown = new LocalVolatilitySurface(SurfaceShiftFunctionFactory.getShiftedSurface(localVol.getSurface(), -volShift, true));

    //first order shifts
    final PDEFullResults1D pdeRes = runForwardPDESolver(_forwardCurve, localVol, _isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());
    final PDEResults1D pdeResUp = runForwardPDESolver(_forwardCurve, lvUp, _isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());
    final PDEResults1D pdeResDown = runForwardPDESolver(_forwardCurve, lvDown, _isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());

    //second order shifts
    final PDEResults1D pdeResUpUp = runForwardPDESolver(_forwardCurve.withShiftedSpot(spotShift), lvUp, _isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());
    final PDEFullResults1D pdeResUpDown = runForwardPDESolver(_forwardCurve.withShiftedSpot(spotShift), lvDown, _isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());
    final PDEFullResults1D pdeResDownUp = runForwardPDESolver(_forwardCurve.withShiftedSpot(-spotShift), lvUp, _isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());
    final PDEFullResults1D pdeResDownDown = runForwardPDESolver(_forwardCurve.withShiftedSpot(-spotShift), lvDown, _isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, option.getStrike());

    ps.println("Strike\tBS Vega\tVega\tBS Vanna\tVanna\tBS Vomma\tVomma");
    final int n = pdeRes.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      final double k = pdeRes.getSpaceValue(i);
      final double price = pdeRes.getFunctionValue(i);
      try {
        final double bsVol = BlackFormulaRepository.impliedVolatility(price, forward, k, maxT, _isCall);
        final double bsVega = BlackFormulaRepository.vega(forward, k, maxT, bsVol);
        final double bsVanna = BlackFormulaRepository.vanna(forward, k, maxT, bsVol);
        final double bsVomma = BlackFormulaRepository.vomma(forward, k, maxT, bsVol);
        final double modelVega = (pdeResUp.getFunctionValue(i) - pdeResDown.getFunctionValue(i)) / 2 / volShift;
        final double modelVanna = (pdeResUpUp.getFunctionValue(i) + pdeResDownDown.getFunctionValue(i) -
            pdeResUpDown.getFunctionValue(i) - pdeResDownUp.getFunctionValue(i)) / 4 / spotShift / volShift;
        final double modelVomma = (pdeResUp.getFunctionValue(i) + pdeResDown.getFunctionValue(i)
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
   * @param maxStrike
   * @param nTimeSteps
   * @param nStrikeSteps
   * @param timeMeshLambda
   * @param strikeMeshBunching
   * @return
   */
  private PDEFullResults1D runForwardPDESolver(final ForwardCurve forwardCurve, final LocalVolatilitySurface localVolatility, final boolean isCall,
      final double theta, final double maxT, final double maxStrike, final int
      nTimeSteps, final int nStrikeSteps, final double timeMeshLambda, final double strikeMeshBunching, final double centreStrike) {

    final PDEDataBundleProvider provider = new PDEDataBundleProvider();
    final ConvectionDiffusionPDEDataBundle db = provider.getForwardLocalVol(forwardCurve.getSpot(), isCall, localVolatility);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, true);

    BoundaryCondition lower;
    BoundaryCondition upper;
    if (isCall) {
      //call option with strike zero is worth the forward, while a put is worthless
      lower = new DirichletBoundaryCondition(forwardCurve.getForwardCurve().toFunction1D(), 0.0);
      upper = new DirichletBoundaryCondition(0.0, maxStrike);
      //upper = new NeumannBoundaryCondition(0, maxStrike, false);
      //upper = new FixedSecondDerivativeBoundaryCondition(0, maxStrike, false);
    } else {
      lower = new DirichletBoundaryCondition(0.0, 0.0);
      upper = new NeumannBoundaryCondition(1.0, maxStrike, false);
    }

    final MeshingFunction timeMesh = new ExponentialMeshing(0.0, maxT, nTimeSteps, timeMeshLambda);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, maxStrike, centreStrike, nStrikeSteps, strikeMeshBunching);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDEFullResults1D res = (PDEFullResults1D) solver.solve(db, grid, lower, upper);
    return res;
  }

  private PDEResults1D runBackwardsPDESolver(final double strike, final LocalVolatilitySurface localVolatility, final boolean isCall,
      final double theta, final double expiry, final double maxSpot, final int
      nTimeSteps, final int nSpotSteps, final double timeMeshLambda, final double spotMeshBunching, final double centreSpot) {

    final PDEDataBundleProvider provider = new PDEDataBundleProvider();
    final ConvectionDiffusionPDEDataBundle db = provider.getBackwardsLocalVol(strike, expiry, isCall, localVolatility);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower;
    BoundaryCondition upper;
    if (isCall) {
      lower = new DirichletBoundaryCondition(0.0, 0.0); //call option with strike zero is worth 0
      upper = new NeumannBoundaryCondition(1.0, maxSpot, false);
    } else {
      lower = new DirichletBoundaryCondition(strike, 0.0);
      upper = new NeumannBoundaryCondition(0.0, maxSpot, false);
    }

    final MeshingFunction timeMesh = new ExponentialMeshing(0.0, expiry, nTimeSteps, timeMeshLambda);
    //keep the grid the same regardless of spot (useful for finite-difference)
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, maxSpot, centreSpot, nSpotSteps, spotMeshBunching);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDEResults1D res = solver.solve(db, grid, lower, upper);
    return res;
  }

  private BlackVolatilitySurface priceToVolSurface(final ForwardCurve forwardCurve, final PDEFullResults1D prices,
      final double minT, final double maxT, final double minStrike, final double maxStrike) {

    final Map<DoublesPair, Double> vol = PDEUtilityTools.priceToImpliedVol(forwardCurve, prices, minT, maxT, minStrike, maxStrike, _isCall);
    final Map<Double, Interpolator1DDataBundle> idb = GRID_INTERPOLATOR2D.getDataBundle(vol);

    final Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
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

}
