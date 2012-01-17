/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.local;

import java.util.Map;

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
import com.opengamma.financial.model.volatility.smile.fitting.sabr.DoubleQuadraticPiecewiseSABRSurfaceFitter;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.PiecewiseSABRSurfaceFitter1;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.SurfaceArrayUtils;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityMoneynessSurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
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
public class LocalVolatilityForwardPDEGreekCalculator {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final CombinedInterpolatorExtrapolator EXTRAPOLATOR_1D = new CombinedInterpolatorExtrapolator(INTERPOLATOR_1D, new FlatExtrapolator1D());
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(EXTRAPOLATOR_1D, EXTRAPOLATOR_1D);
  private static final DupireLocalVolatilityCalculator DUPIRE = new DupireLocalVolatilityCalculator();

  private final double _theta;
  private final int _timeSteps;
  private final int _spaceSteps;
  private final double _timeGridBunching;
  private final double _spaceGridBunching;
  private final PiecewiseSABRSurfaceFitter1 _surfaceFitter;

  public LocalVolatilityForwardPDEGreekCalculator() {
    this(0.5, 100, 100, 5, 0.05, new DoubleQuadraticPiecewiseSABRSurfaceFitter(true, false, 100)); //TODO
  }

  public LocalVolatilityForwardPDEGreekCalculator(final double theta, final int timeSteps, final int spaceSteps,
      final double timeGridBunching, final double spaceGridBunching, final PiecewiseSABRSurfaceFitter1 surfaceFitter) {
    _theta = theta;
    _timeSteps = timeSteps;
    _spaceSteps = spaceSteps;
    _timeGridBunching = timeGridBunching;
    _spaceGridBunching = spaceGridBunching;
    _surfaceFitter = surfaceFitter;
  }

  public void solve(final SmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    final ForwardCurve forwardCurve = data.getForwardCurve();
    final double[] expiries = data.getExpiries();
    final double[][] strikes = data.getStrikes();
    final double[][] impliedVols = data.getVolatilities();
    final boolean isCall = data.isCallData();
    final BlackVolatilitySurface impVolSurface = _surfaceFitter.getVolatilitySurface(data);
    final LocalVolatilitySurface localVolatility = DUPIRE.getLocalVolatility(impVolSurface, forwardCurve);
    runPDESolver(forwardCurve, localVolatility, expiries, strikes, impliedVols, isCall);
    gridGreeks(forwardCurve, localVolatility, isCall, option);
    bucketedVega(localVolatility, data, option);
  }

  /**
   * Run a forward PDE solver to get model prices (and thus implied vols) and compare these with the market data.
   * Also output the (model) implied volatility as a function of strike for each tenor.
   * @param ps The print stream
   */
  private void runPDESolver(final ForwardCurve forwardCurve, final LocalVolatilitySurface localVolatility, final double[] expiries, final double[][] strikes,
      final double[][] impliedVols, final boolean isCall) {
    final int nExpiries = expiries.length;
    double minK = Double.POSITIVE_INFINITY;
    double maxK = 0.0;
    for (int i = 0; i < nExpiries; i++) {
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

    final double maxT = expiries[nExpiries - 1];
    final double maxMoneyness = 3.5;

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVolatility, isCall, _theta, maxT, maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    //  PDEUtilityTools.printSurface("prices", pdeRes);
    final BlackVolatilitySurface pdeVolSurface = modifiedPriceToVolSurface(forwardCurve, pdeRes, 0, maxT, 0.3, 3.0, isCall);
    //  PDEUtilityTools.printSurface("vol surface", pdeVolSurface.getSurface(), 0, maxT, 0.3, 3.0);
    double chiSq = 0;
    for (int i = 0; i < nExpiries; i++) {
      final int m = strikes[i].length;
      final double t = expiries[i];
      for (int j = 0; j < m; j++) {
        final double k = strikes[i][j];

        final double mrtVol = impliedVols[i][j];
        final double modelVol = pdeVolSurface.getVolatility(t, k);
        //        ps.println(_expiries[i] + "\t" + k + "\t" + mrtVol + "\t" + modelVol);
        chiSq += (mrtVol - modelVol) * (mrtVol - modelVol);
      }
    }
    //    ps.println("chi^2 " + chiSq * 1e6);
    //
    //    ps.print("\n");
    //    ps.println("strike sensitivity");
    //    for (int i = 0; i < _nExpiries; i++) {
    //      ps.print(_expiries[i] + "\t" + "" + "\t");
    //    }
    //    ps.print("\n");
    //    for (int i = 0; i < _nExpiries; i++) {
    //      ps.print("Strike\tImplied Vol\t");
    //    }
    //    ps.print("\n");
    //    for (int j = 0; j < 100; j++) {
    //      for (int i = 0; i < _nExpiries; i++) {
    //        final int m = _strikes[i].length;
    //        final double t = _expiries[i];
    //        final double kLow = _strikes[i][0];
    //        final double kHigh = _strikes[i][m - 1];
    //        final double k = kLow + (kHigh - kLow) * j / 99.;
    //        ps.print(k + "\t" + pdeVolSurface.getVolatility(t, k) + "\t");
    //      }
    //      ps.print("\n");
    //    }
  }

  /**
   * Runs both forward and backwards PDE solvers, and produces delta and gamma (plus the dual - i.e. with respect to strike)
   * values again strike and spot, for the given expiry and strike using the provided local volatility (i.e. override
   * that calculated from the fitted implied volatility surface).
   */
  private void gridGreeks(final ForwardCurve forwardCurve, final LocalVolatilitySurface localVolatility, final boolean isCall, final EuropeanVanillaOption option) {
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);

    final double forwardShift = 5e-2;
    final double volShift = 1e-4;
    final double maxMoneyness = 3.5;

    final LocalVolatilitySurface localVolatilityUp = new LocalVolatilitySurface(SurfaceShiftFunctionFactory.getShiftedSurface(localVolatility.getSurface(), volShift, true));
    final LocalVolatilitySurface localVolatilityDown = new LocalVolatilitySurface(SurfaceShiftFunctionFactory.getShiftedSurface(localVolatilityUp.getSurface(), -volShift, true));

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVolatility, isCall, _theta, expiry, maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardUp = runForwardPDESolver(forwardCurve.withFractionalShift(forwardShift), localVolatility, isCall,
        _theta, expiry, maxMoneyness, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardDown = runForwardPDESolver(forwardCurve.withFractionalShift(-forwardShift), localVolatility, isCall,
        _theta, expiry, maxMoneyness, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResVolUp = runForwardPDESolver(forwardCurve, localVolatilityUp, isCall, _theta, expiry, maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResVolDown = runForwardPDESolver(forwardCurve, localVolatilityDown, isCall, _theta, expiry, maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEResults1D pdeResForwardUpVolUp = runForwardPDESolver(forwardCurve.withFractionalShift(forwardShift), localVolatilityUp, isCall, _theta, expiry, maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardUpVolDown = runForwardPDESolver(forwardCurve.withFractionalShift(forwardShift), localVolatilityDown, isCall, _theta, expiry, maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardDownVolUp = runForwardPDESolver(forwardCurve.withFractionalShift(-forwardShift), localVolatilityUp, isCall, _theta, expiry, maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardDownVolDown = runForwardPDESolver(forwardCurve.withFractionalShift(-forwardShift), localVolatilityDown, isCall, _theta, expiry, maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);

    final int n = pdeRes.getNumberSpaceNodes();

    for (int i = 0; i < n; i++) {
      final double m = pdeRes.getSpaceValue(i);
      if (m > 0.3 && m < 3.0) {
        final double k = m * forward;
        final double mPrice = pdeRes.getFunctionValue(i);
        double impVol = 0;
        try {
          impVol = getBSImpliedVol(mPrice, m, k, expiry, impVol, isCall);
        } catch (final Exception e) {
        }
        final double bsDelta = getBSDelta(forward, k, expiry, impVol, isCall);
        final double bsDualDelta = getBSDualDelta(forward, k, expiry, impVol, isCall);
        final double bsGamma = getBSGamma(forward, k, expiry, impVol);
        final double bsDualGamma = getBSDualGamma(forward, k, expiry, impVol);
        final double bsVega = getBSVega(forward, k, expiry, impVol);
        final double bsVanna = getBSVanna(forward, k, expiry, impVol);
        final double bsVomma = getBSVomma(forward, k, expiry, impVol);

        final double modelDD = getModelDualDelta(pdeRes, i);
        final double fixedSurfaceDelta = getFixedSurfaceDelta(mPrice, m, modelDD);
        final double surfaceDelta = getSurfaceDelta(pdeResForwardUp, pdeResForwardDown, forward, forwardShift, i);
        final double modelDelta = getModelDelta(fixedSurfaceDelta, forward, surfaceDelta);

        final double modelDG = getModelDualGamma(pdeRes, i, forward);
        final double fixedSurfaceGamma = getFixedSurfaceGamma(modelDelta, modelDG);
        final double dSurfaceDMoneyness = getDSurfaceDMoneyness(pdeResForwardUp, pdeResForwardDown, forward, forwardShift, i);
        final double surfaceGamma = getSurfaceGamma(pdeResForwardUp, pdeResForwardDown, pdeResForwardDown, forward, forwardShift, i);
        final double modelGamma = getModelGamma(fixedSurfaceGamma, surfaceDelta, fixedSurfaceGamma, dSurfaceDMoneyness, surfaceGamma);

        final double modelVega = getModelVega(pdeResVolUp, pdeResVolDown, volShift, i);
        final double xVanna = getXVanna(volShift, pdeResVolUp, pdeResVolDown, i, m);
        final double surfaceVanna = getSurfaceVanna(pdeResForwardUpVolUp, pdeResForwardUpVolDown, pdeResForwardDownVolUp, pdeResForwardDownVolDown, volShift, forwardShift, i);
        final double modelVanna = getModelVanna(xVanna, surfaceVanna);
        final double modelVomma = getModelVomma(pdeRes, pdeResVolUp, pdeResVolDown, volShift, i);
      }
    }
  }

  private double getBSImpliedVol(final double mPrice, final double m, final double k, final double expiry, final double impVol, final boolean isCall) {
    return BlackFormulaRepository.impliedVolatility(mPrice, 1.0, m, expiry, isCall);
  }

  private double getBSDelta(final double forward, final double k, final double expiry, final double impVol, final boolean isCall) {
    return BlackFormulaRepository.delta(forward, k, expiry, impVol, isCall);
  }

  private double getBSDualDelta(final double forward, final double k, final double expiry, final double impVol, final boolean isCall) {
    return BlackFormulaRepository.dualDelta(forward, k, expiry, impVol, isCall);
  }

  private double getBSGamma(final double forward, final double k, final double expiry, final double impVol) {
    return BlackFormulaRepository.gamma(forward, k, expiry, impVol);
  }

  private double getBSDualGamma(final double forward, final double k, final double expiry, final double impVol) {
    return BlackFormulaRepository.dualGamma(forward, k, expiry, impVol);
  }

  private double getBSVega(final double forward, final double k, final double expiry, final double impVol) {
    return BlackFormulaRepository.vega(forward, k, expiry, impVol);
  }

  private double getBSVanna(final double forward, final double k, final double expiry, final double impVol) {
    return BlackFormulaRepository.vanna(forward, k, expiry, impVol);
  }

  private double getBSVomma(final double forward, final double k, final double expiry, final double impVol) {
    return BlackFormulaRepository.vomma(forward, k, expiry, impVol);
  }

  private double getFixedSurfaceDelta(final double mPrice, final double m, final double modelDD) {
    return mPrice - m * modelDD; //i.e. the delta if the moneyness parameterised local vol surface was invariant to forward
  }

  private double getSurfaceDelta(final PDEFullResults1D pdeResUp, final PDEFullResults1D pdeResDown, final double forward, final double shift, final int i) {
    return (pdeResUp.getFunctionValue(i) - pdeResDown.getFunctionValue(i)) / 2 / forward / shift;
  }

  private double getModelDelta(final double fixedSurfaceDelta, final double forward, final double surfaceDelta) {
    return fixedSurfaceDelta + forward * surfaceDelta;
  }

  private double getModelDualDelta(final PDEFullResults1D pdeRes, final int i) {
    return pdeRes.getFirstSpatialDerivative(i);
  }

  private double getFixedSurfaceGamma(final double m, final double modelDG) {
    return m * m * modelDG;
  }

  private double getDSurfaceDMoneyness(final PDEFullResults1D pdeResUp, final PDEFullResults1D pdeResDown, final double forward, final double shift, final int i) {
    return (pdeResUp.getFirstSpatialDerivative(i) - pdeResDown.getFirstSpatialDerivative(i)) / 2 / forward / shift;
  }

  private double getSurfaceGamma(final PDEFullResults1D pdeResUp, final PDEFullResults1D pdeResDown, final PDEFullResults1D pdeRes, final double forward, final double shift, final int i) {
    return (pdeResUp.getFunctionValue(i) + pdeResDown.getFunctionValue(i) - 2 * pdeRes.getFunctionValue(i)) / forward / shift / shift;
  }

  private double getModelGamma(final double fixedSurfaceGamma, final double surfaceDelta, final double m, final double surfaceVanna, final double surfaceGamma) {
    return fixedSurfaceGamma + 2 * surfaceDelta - 2 * m * surfaceVanna + surfaceGamma;
  }

  private double getModelDualGamma(final PDEFullResults1D pdeRes, final int i, final double forward) {
    return pdeRes.getSecondSpatialDerivative(i) / forward;
  }

  private double getModelVega(final PDEFullResults1D pdeResUp, final PDEFullResults1D pdeResDown, final double volShift, final int i) {
    return (pdeResUp.getFunctionValue(i) - pdeResDown.getFunctionValue(i)) / 2 / volShift;
  }

  //xVanna is the vanna if the moneyness parameterised local vol surface was invariant to changes in the forward curve
  private double getXVanna(final double volShift, final PDEResults1D pdeResUp, final PDEResults1D pdeResDown, final int i, final double m) {
    return (pdeResUp.getFunctionValue(i) - pdeResDown.getFunctionValue(i)
        - m * (pdeResUp.getFirstSpatialDerivative(i) - pdeResDown.getFirstSpatialDerivative(i))) / 2 / volShift;
  }

  //this is the vanna coming purely from deformation of the local volatility surface
  private double getSurfaceVanna(final PDEResults1D pdeResForwardUpVolUp, final PDEFullResults1D pdeResForwardUpVolDown, final PDEFullResults1D pdeResForwardDownVolUp,
      final PDEFullResults1D pdeResForwardDownVolDown, final double volShift, final double forwardShift, final int i) {
    return (pdeResForwardUpVolUp.getFunctionValue(i) + pdeResForwardDownVolDown.getFunctionValue(i) -
        pdeResForwardUpVolDown.getFunctionValue(i) - pdeResForwardDownVolUp.getFunctionValue(i)) / 4 / forwardShift / volShift;
  }

  private double getModelVanna(final double xVanna, final double surfaceVanna) {
    return xVanna + surfaceVanna;
  }

  private double getModelVomma(final PDEFullResults1D pdeRes, final PDEResults1D pdeResVolUp, final PDEResults1D pdeResVolDown, final double volShift, final int i) {
    return (pdeResVolUp.getFunctionValue(i) + pdeResVolDown.getFunctionValue(i) - 2 * pdeRes.getFunctionValue(i)) / volShift / volShift;
  }

  /**
   * bumped each input volatility by 1bs and record the effect on the representative point by following the chain
   * of refitting the implied volatility surface, the local volatility surface and running the forward PDE solver
   */
  private void bucketedVega(final LocalVolatilitySurface localVolatility, final SmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    final double[] expiries = data.getExpiries();
    final ForwardCurve forwardCurve = data.getForwardCurve();
    final boolean isCall = data.isCallData();
    final double[][] strikes = data.getStrikes();
    final int nExpiries = expiries.length;
    final double forward = forwardCurve.getForward(option.getTimeToExpiry());
    final double maxT = option.getTimeToExpiry();
    final double maxMoneyness = 3.5;
    final double x = option.getStrike() / forward;

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVolatility, isCall, _theta, maxT, maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);

    final double[] xNodes = pdeRes.getGrid().getSpaceNodes();
    int index = SurfaceArrayUtils.getLowerBoundIndex(xNodes, x);
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

    final double[][] res = new double[nExpiries][];

    for (int i = 0; i < nExpiries; i++) {
      final int m = strikes[i].length;
      res[i] = new double[m];
      for (int j = 0; j < m; j++) {
        final SmileSurfaceDataBundle bumpedData = data.withBumpedPoint(i, j, shiftAmount);
        final BlackVolatilitySurface bumpedSurface = _surfaceFitter.getVolatilitySurface(bumpedData);
        final LocalVolatilitySurface bumpedLV = DUPIRE.getLocalVolatility(bumpedSurface, forwardCurve);
        final PDEFullResults1D pdeResBumped = runForwardPDESolver(forwardCurve, bumpedLV, isCall, _theta, maxT,
            maxMoneyness, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
        for (int k = 0; k < 4; k++) {
          vols[k] = BlackFormulaRepository.impliedVolatility(pdeResBumped.getFunctionValue(index + k), 1.0, moneyness[k],
              option.getTimeToExpiry(), option.isCall());
        }
        db = INTERPOLATOR_1D.getDataBundle(moneyness, vols);
        final double vol = INTERPOLATOR_1D.interpolate(db, x);
        res[i][j] = (vol - exampleVol) / shiftAmount;
      }
    }
    //
    //    for (int i = 0; i < nExpiries; i++) {
    //      //  System.out.print(TENORS[i] + "\t");
    //      final int m = _strikes[i].length;
    //      for (int j = 0; j < m; j++) {
    //        ps.print(res[i][j] + "\t");
    //      }
    //      ps.print("\n");
    //    }
    //    ps.print("\n");
  }


  private PDEFullResults1D runForwardPDESolver(final ForwardCurve forwardCurve, final LocalVolatilitySurface localVolatility,
      final boolean isCall, final double theta, final double maxT, final double maxMoneyness, final int
      nTimeSteps, final int nStrikeSteps, final double timeMeshLambda, final double strikeMeshBunching, final double centreMoneyness) {

    final PDEDataBundleProvider provider = new PDEDataBundleProvider();
    final ConvectionDiffusionPDEDataBundle db = provider.getForwardLocalVol(localVolatility, forwardCurve, isCall);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, true);

    BoundaryCondition lower;
    BoundaryCondition upper;
    if (isCall) {
      //call option with strike zero is worth the forward, while a put is worthless
      lower = new DirichletBoundaryCondition(1.0, 0.0);
      upper = new DirichletBoundaryCondition(0.0, maxMoneyness);
    } else {
      lower = new DirichletBoundaryCondition(0.0, 0.0);
      upper = new NeumannBoundaryCondition(1.0, maxMoneyness, false);
    }

    final MeshingFunction timeMesh = new ExponentialMeshing(0.0, maxT, nTimeSteps, timeMeshLambda);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, maxMoneyness, centreMoneyness, nStrikeSteps, strikeMeshBunching);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDEFullResults1D res = (PDEFullResults1D) solver.solve(db, grid, lower, upper);
    return res;
  }

  /**
   * Convert the results of running the forward PDE, which are forward option prices divided by the relevant forward, to an implied volatility
   * surface parameterised by expiry and moneyness (=strike/forward)
   * @param forwardCurve
   * @param prices
   * @param minT
   * @param maxT
   * @param minMoneyness
   * @param maxMoneyness
   * @return
   */
  private BlackVolatilityMoneynessSurface modifiedPriceToVolSurface(final ForwardCurve forwardCurve, final PDEFullResults1D prices,
      final double minT, final double maxT, final double minMoneyness, final double maxMoneyness, final boolean isCall) {

    final Map<DoublesPair, Double> vol = PDEUtilityTools.modifiedPriceToImpliedVol(prices, minT, maxT, minMoneyness, maxMoneyness, isCall);
    final Map<Double, Interpolator1DDataBundle> idb = GRID_INTERPOLATOR2D.getDataBundle(vol);

    final Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        return GRID_INTERPOLATOR2D.interpolate(idb, new DoublesPair(tk[0], tk[1]));
      }
    };

    return new BlackVolatilityMoneynessSurface(FunctionalDoublesSurface.from(func), forwardCurve);
  }
}
