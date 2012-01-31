/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.local;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import com.opengamma.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.financial.greeks.PDEGreekResultCollection;
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
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.PiecewiseSABRSurfaceFitter1;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.SurfaceArrayUtils;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurfaceConverter;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurfaceMoneyness;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurfaceStrike;
import com.opengamma.financial.model.volatility.surface.StrikeType;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.math.surface.SurfaceShiftFunctionFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <T> The strike type parameterization to be used
 */
public class LocalVolatilityForwardPDEGreekCalculator<T extends StrikeType> {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private final double _theta;
  private final int _timeSteps;
  private final int _spaceSteps;
  private final double _timeGridBunching;
  private final double _spaceGridBunching;
  private final PiecewiseSABRSurfaceFitter1<T> _surfaceFitter;
  private final DupireLocalVolatilityCalculator _localVolatilityCalculator;
  private final double _maxMoneyness;

  //TODO remove surface fitter and local volatility calculator from here and put in a bundle with the local volatility surface
  public LocalVolatilityForwardPDEGreekCalculator(final double theta, final int timeSteps, final int spaceSteps,
      final double timeGridBunching, final double spaceGridBunching, final PiecewiseSABRSurfaceFitter1<T> surfaceFitter,
      final DupireLocalVolatilityCalculator localVolatilityCalculator, final double maxMoneyness) {
    ArgumentChecker.isTrue(theta >= 0 && theta <= 1, "Theta must be >= 0 and <= 1; have {}", theta);
    ArgumentChecker.isTrue(timeSteps > 0, "Number of time steps must be greater than 0; have {}", timeSteps);
    ArgumentChecker.isTrue(spaceSteps > 0, "Number of space steps must be greater than 0; have {}", spaceSteps);
    ArgumentChecker.isTrue(spaceGridBunching > 0, "Space grid bunching must be greater than 0; have {}", spaceGridBunching);
    ArgumentChecker.notNull(surfaceFitter, "surface fitter");
    ArgumentChecker.notNull(localVolatilityCalculator, "local volatility calculator");
    _theta = theta;
    _timeSteps = timeSteps;
    _spaceSteps = spaceSteps;
    _timeGridBunching = timeGridBunching;
    _spaceGridBunching = spaceGridBunching;
    _surfaceFitter = surfaceFitter;
    _localVolatilityCalculator = localVolatilityCalculator;
    _maxMoneyness = maxMoneyness;
  }

  public PDEFullResults1D solve(final SmileSurfaceDataBundle data, final LocalVolatilitySurface<?> localVolatility, final EuropeanVanillaOption option) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(option, "option");
    final ForwardCurve forwardCurve = data.getForwardCurve();
    final double[] expiries = data.getExpiries();
    final double[][] strikes = data.getStrikes();
    final double[][] impliedVols = data.getVolatilities();
    final boolean isCall = data.isCallData();
    return runPDESolver(forwardCurve, localVolatility, expiries, strikes, impliedVols, isCall);
  }

  public PDEGreekResultCollection getGridGreeks(final SmileSurfaceDataBundle data, final LocalVolatilitySurface<?> localVolatility,
      final EuropeanVanillaOption option) {
    final ForwardCurve forwardCurve = data.getForwardCurve();
    final boolean isCall = data.isCallData();
    LocalVolatilitySurfaceStrike strikeLocalVolatility;
    if (localVolatility instanceof LocalVolatilitySurfaceStrike) {
      strikeLocalVolatility = (LocalVolatilitySurfaceStrike) localVolatility;
    } else {
      strikeLocalVolatility = LocalVolatilitySurfaceConverter.toStrikeSurface(((LocalVolatilitySurfaceMoneyness) localVolatility));
    }
    return gridGreeks(forwardCurve, strikeLocalVolatility, isCall, option);
  }

  public BucketedGreekResultCollection getBucketedVega(final SmileSurfaceDataBundle data, final LocalVolatilitySurface<?> localVolatility,
      final EuropeanVanillaOption option) {
    LocalVolatilitySurfaceStrike strikeLocalVolatility;
    if (localVolatility instanceof LocalVolatilitySurfaceStrike) {
      strikeLocalVolatility = (LocalVolatilitySurfaceStrike) localVolatility;
    } else {
      strikeLocalVolatility = LocalVolatilitySurfaceConverter.toStrikeSurface(((LocalVolatilitySurfaceMoneyness) localVolatility));
    }
    return bucketedVega(strikeLocalVolatility, data, option);
  }

  /**
   * Run a forward PDE solver to get model prices (and thus implied vols) and compare these with the market data.
   * Also output the (model) implied volatility as a function of strike for each tenor.
   * @param ps The print stream
   */
  private PDEFullResults1D runPDESolver(final ForwardCurve forwardCurve, final LocalVolatilitySurface<?> localVolatility, final double[] expiries, final double[][] strikes,
      final double[][] impliedVols, final boolean isCall) {
    final int nExpiries = expiries.length;
    final double maxT = expiries[nExpiries - 1];
    //TODO check type of local vol surface
    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVolatility, isCall, _theta, maxT, _maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    return pdeRes;
  }

  /**
   * Runs both forward and backwards PDE solvers, and produces delta and gamma (plus the dual - i.e. with respect to strike)
   * values again strike and spot, for the given expiry and strike using the provided local volatility (i.e. override
   * that calculated from the fitted implied volatility surface).
   */
  private PDEGreekResultCollection gridGreeks(final ForwardCurve forwardCurve, final LocalVolatilitySurfaceStrike localVolatility, final boolean isCall, final EuropeanVanillaOption option) {
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);

    final double forwardShift = 5e-2;
    final double volShift = 1e-4;

    final LocalVolatilitySurfaceStrike localVolatilityUp = new LocalVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(localVolatility.getSurface(), volShift, true));
    final LocalVolatilitySurfaceStrike localVolatilityDown = new LocalVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(localVolatility.getSurface(), -volShift, true));

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVolatility, isCall, _theta, expiry, _maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardUp = runForwardPDESolver(forwardCurve.withFractionalShift(forwardShift), localVolatility, isCall,
        _theta, expiry, _maxMoneyness, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardDown = runForwardPDESolver(forwardCurve.withFractionalShift(-forwardShift), localVolatility, isCall,
        _theta, expiry, _maxMoneyness, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResVolUp = runForwardPDESolver(forwardCurve, localVolatilityUp, isCall, _theta, expiry, _maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResVolDown = runForwardPDESolver(forwardCurve, localVolatilityDown, isCall, _theta, expiry, _maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEResults1D pdeResForwardUpVolUp = runForwardPDESolver(forwardCurve.withFractionalShift(forwardShift), localVolatilityUp, isCall, _theta, expiry, _maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardUpVolDown = runForwardPDESolver(forwardCurve.withFractionalShift(forwardShift), localVolatilityDown, isCall, _theta, expiry, _maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardDownVolUp = runForwardPDESolver(forwardCurve.withFractionalShift(-forwardShift), localVolatilityUp, isCall, _theta, expiry, _maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardDownVolDown = runForwardPDESolver(forwardCurve.withFractionalShift(-forwardShift), localVolatilityDown, isCall, _theta, expiry, _maxMoneyness,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);

    final int n = pdeRes.getNumberSpaceNodes();
    final DoubleArrayList strikes = new DoubleArrayList();
    final DoubleArrayList impliedVolatilities = new DoubleArrayList();
    final DoubleArrayList bsDelta = new DoubleArrayList();
    final DoubleArrayList bsDualDelta = new DoubleArrayList();
    final DoubleArrayList bsGamma = new DoubleArrayList();
    final DoubleArrayList bsDualGamma = new DoubleArrayList();
    final DoubleArrayList bsVega = new DoubleArrayList();
    final DoubleArrayList bsVanna = new DoubleArrayList();
    final DoubleArrayList bsVomma = new DoubleArrayList();
    final DoubleArrayList modelDelta = new DoubleArrayList();
    final DoubleArrayList modelDualDelta = new DoubleArrayList();
    final DoubleArrayList modelGamma = new DoubleArrayList();
    final DoubleArrayList modelDualGamma = new DoubleArrayList();
    final DoubleArrayList modelVega = new DoubleArrayList();
    final DoubleArrayList modelVanna = new DoubleArrayList();
    final DoubleArrayList modelVomma = new DoubleArrayList();
    for (int i = 0; i < n; i++) {
      final double m = pdeRes.getSpaceValue(i);
      final double k = m * forward;
      final double mPrice = pdeRes.getFunctionValue(i);
      double impVol = 0;
      try {
        impVol = getBSImpliedVol(mPrice, m, expiry, isCall);
      } catch (final Exception e) {
        continue;
      }
      strikes.add(k);
      impliedVolatilities.add(impVol);
      bsDelta.add(getBSDelta(forward, k, expiry, impVol, isCall));
      bsDualDelta.add(getBSDualDelta(forward, k, expiry, impVol, isCall));
      bsGamma.add(getBSGamma(forward, k, expiry, impVol));
      bsDualGamma.add(getBSDualGamma(forward, k, expiry, impVol));
      bsVega.add(getBSVega(forward, k, expiry, impVol));
      bsVanna.add(getBSVanna(forward, k, expiry, impVol));
      bsVomma.add(getBSVomma(forward, k, expiry, impVol));

      final double modelDD = getModelDualDelta(pdeRes, i);
      modelDualDelta.add(modelDD);
      final double fixedSurfaceDelta = getFixedSurfaceDelta(mPrice, m, modelDD);
      final double surfaceDelta = getSurfaceDelta(pdeResForwardUp, pdeResForwardDown, forward, forwardShift, i);
      final double modelD = getModelDelta(fixedSurfaceDelta, forward, surfaceDelta);
      modelDelta.add(modelD);

      final double modelDG = getModelDualGamma(pdeRes, i, forward);
      modelDualGamma.add(modelDG);
      final double fixedSurfaceGamma = getFixedSurfaceGamma(m, modelDG);
      final double dSurfaceDMoneyness = getDSurfaceDMoneyness(pdeResForwardUp, pdeResForwardDown, forward, forwardShift, i);
      final double surfaceGamma = getSurfaceGamma(pdeResForwardUp, pdeResForwardDown, pdeRes, forward, forwardShift, i);
      modelGamma.add(getModelGamma(fixedSurfaceGamma, surfaceDelta, m, dSurfaceDMoneyness, surfaceGamma));

      modelVega.add(getModelVega(pdeResVolUp, pdeResVolDown, volShift, i));
      final double xVanna = getXVanna(volShift, pdeResVolUp, pdeResVolDown, i, m);
      final double surfaceVanna = getSurfaceVanna(pdeResForwardUpVolUp, pdeResForwardUpVolDown, pdeResForwardDownVolUp, pdeResForwardDownVolDown, volShift, forwardShift, i);
      modelVanna.add(getModelVanna(xVanna, surfaceVanna));
      modelVomma.add(getModelVomma(pdeRes, pdeResVolUp, pdeResVolDown, volShift, i));
    }
    final PDEGreekResultCollection result = new PDEGreekResultCollection(strikes.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_BLACK_DELTA, bsDelta.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_BLACK_DUAL_DELTA, bsDualDelta.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_BLACK_GAMMA, bsGamma.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_BLACK_DUAL_GAMMA, bsDualGamma.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_BLACK_VEGA, bsVega.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_BLACK_VANNA, bsVanna.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_BLACK_VOMMA, bsVomma.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_DELTA, modelDelta.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_DUAL_DELTA, modelDualDelta.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_GAMMA, modelGamma.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_DUAL_GAMMA, modelDualGamma.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_VEGA, modelVega.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_VANNA, modelVanna.toDoubleArray());
    result.put(PDEGreekResultCollection.GRID_VOMMA, modelVomma.toDoubleArray());
    return result;
  }

  private double getBSImpliedVol(final double mPrice, final double m, final double expiry, final boolean isCall) {
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

  private double getModelGamma(final double fixedSurfaceGamma, final double surfaceDelta, final double m, final double dSurfaceDMoneyness, final double surfaceGamma) {
    return fixedSurfaceGamma + 2 * surfaceDelta - 2 * m * dSurfaceDMoneyness + surfaceGamma;
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
  private BucketedGreekResultCollection bucketedVega(final LocalVolatilitySurfaceStrike localVolatility, final SmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    final double[] expiries = data.getExpiries();
    final ForwardCurve forwardCurve = data.getForwardCurve();
    final boolean isCall = data.isCallData();

    final double[][] strikes = data.getStrikes();
    final int nExpiries = expiries.length;
    final double forward = forwardCurve.getForward(option.getTimeToExpiry());
    final double maxT = option.getTimeToExpiry();
    final double x = option.getStrike() / forward;

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVolatility, isCall, _theta, maxT, _maxMoneyness,
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

    final double[][] bucketedVega = new double[nExpiries][];

    for (int i = 0; i < nExpiries; i++) {
      final int m = strikes[i].length;
      bucketedVega[i] = new double[m];
      for (int j = 0; j < m; j++) {
        final SmileSurfaceDataBundle bumpedData = data.withBumpedPoint(i, j, shiftAmount);
        final BlackVolatilitySurface<?> bumpedSurface = _surfaceFitter.getVolatilitySurface(bumpedData);
        final LocalVolatilitySurface<?> bumpedLV = _localVolatilityCalculator.getLocalVolatilitySurface(bumpedSurface, forwardCurve);
        final LocalVolatilitySurfaceStrike bumpedLVStrike;
        if (bumpedLV instanceof LocalVolatilitySurfaceStrike) {
          bumpedLVStrike = (LocalVolatilitySurfaceStrike) bumpedLV;
        } else {
          bumpedLVStrike = LocalVolatilitySurfaceConverter.toStrikeSurface(((LocalVolatilitySurfaceMoneyness) bumpedLV));
        }
        final PDEFullResults1D pdeResBumped = runForwardPDESolver(forwardCurve, bumpedLVStrike, isCall, _theta, maxT,
            _maxMoneyness, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
        for (int k = 0; k < 4; k++) {
          vols[k] = BlackFormulaRepository.impliedVolatility(pdeResBumped.getFunctionValue(index + k), 1.0, moneyness[k],
              option.getTimeToExpiry(), option.isCall());
        }
        db = INTERPOLATOR_1D.getDataBundle(moneyness, vols);
        final double vol = INTERPOLATOR_1D.interpolate(db, x);
        bucketedVega[i][j] = (vol - exampleVol) / shiftAmount;
      }
    }
    final BucketedGreekResultCollection result = new BucketedGreekResultCollection(expiries, strikes);
    result.put(BucketedGreekResultCollection.BUCKETED_VEGA, bucketedVega);
    return result;
  }


  private PDEFullResults1D runForwardPDESolver(final ForwardCurve forwardCurve, final LocalVolatilitySurface<?> localVolatility,
      final boolean isCall, final double theta, final double maxT, final double maxMoneyness, final int
      nTimeSteps, final int nStrikeSteps, final double timeMeshLambda, final double strikeMeshBunching, final double centreMoneyness) {

    final PDEDataBundleProvider provider = new PDEDataBundleProvider();
    ConvectionDiffusionPDEDataBundle db;
    if (localVolatility instanceof LocalVolatilitySurfaceStrike) {
      db = provider.getForwardLocalVol((LocalVolatilitySurfaceStrike) localVolatility, forwardCurve, isCall);
    } else if (localVolatility instanceof LocalVolatilitySurfaceMoneyness) {
      db = provider.getForwardLocalVol((LocalVolatilitySurfaceMoneyness) localVolatility, isCall);
    } else {
      throw new IllegalArgumentException();
    }
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
}
