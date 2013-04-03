/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import com.opengamma.analytics.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.analytics.financial.greeks.PDEResultCollection;
import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
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
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.StrikeType;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <T> The strike type parameterization to be used
 * @deprecated Do not use
 */
@Deprecated
public class LocalVolatilityForwardPDEGreekCalculator1<T extends StrikeType> {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private final double _theta;
  private final int _timeSteps;
  private final int _spaceSteps;
  private final double _timeGridBunching;
  private final double _spaceGridBunching;
  private final VolatilitySurfaceInterpolator _surfaceFitter;
  // private final PiecewiseSABRSurfaceFitter1<T> _surfaceFitter;
  private final DupireLocalVolatilityCalculator _localVolatilityCalculator;
  private final double _maxAbsProxyDelta;

  //TODO remove surface fitter and local volatility calculator from here and put in a bundle with the local volatility surface
  public LocalVolatilityForwardPDEGreekCalculator1(final double theta, final int timeSteps, final int spaceSteps,
      final double timeGridBunching, final double spaceGridBunching, final VolatilitySurfaceInterpolator surfaceFitter, /*final PiecewiseSABRSurfaceFitter1<T> surfaceFitter,*/
      final DupireLocalVolatilityCalculator localVolatilityCalculator, final double maxAbsProxyDelta) {
    ArgumentChecker.isTrue(theta >= 0 && theta <= 1, "Theta must be >= 0 and <= 1; have {}", theta);
    ArgumentChecker.isTrue(timeSteps > 0, "Number of time steps must be greater than 0; have {}", timeSteps);
    ArgumentChecker.isTrue(spaceSteps > 0, "Number of space steps must be greater than 0; have {}", spaceSteps);
    ArgumentChecker.isTrue(spaceGridBunching > 0, "Space grid bunching must be greater than 0; have {}", spaceGridBunching);
    ArgumentChecker.notNull(surfaceFitter, "surface fitter");
    ArgumentChecker.notNull(localVolatilityCalculator, "local volatility calculator");
    ArgumentChecker.isTrue(maxAbsProxyDelta > 0, "max abs proxy-delta  must be greater than 0; have {}", maxAbsProxyDelta);
    _theta = theta;
    _timeSteps = timeSteps;
    _spaceSteps = spaceSteps;
    _timeGridBunching = timeGridBunching;
    _spaceGridBunching = spaceGridBunching;
    _surfaceFitter = surfaceFitter;
    _localVolatilityCalculator = localVolatilityCalculator;
    _maxAbsProxyDelta = maxAbsProxyDelta;
    //   _maxMoneyness = maxMoneyness;
  }

  public PDEFullResults1D solve(final SmileSurfaceDataBundle data, final LocalVolatilitySurface<?> localVolatility) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(localVolatility, "local volatility surface");
    final ForwardCurve forwardCurve = data.getForwardCurve();
    final double[] expiries = data.getExpiries();
    //    final double[][] strikes = data.getStrikes();
    //    final double[][] impliedVols = data.getVolatilities();
    final boolean isCall = true; //TODO have this as an option  data.isCallData();
    return runPDESolver(forwardCurve, localVolatility, expiries, isCall);
  }

  public PDEResultCollection getGridGreeks(final SmileSurfaceDataBundle data, final LocalVolatilitySurface<?> localVolatility, final EuropeanVanillaOption option) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(localVolatility, "local volatility surface");
    final ForwardCurve forwardCurve = data.getForwardCurve();
    final boolean isCall = true; //TODO have this as an option  data.isCallData();
    LocalVolatilitySurfaceStrike strikeLocalVolatility;
    if (localVolatility instanceof LocalVolatilitySurfaceStrike) {
      strikeLocalVolatility = (LocalVolatilitySurfaceStrike) localVolatility;
    } else if (localVolatility instanceof LocalVolatilitySurfaceMoneyness) {
      strikeLocalVolatility = LocalVolatilitySurfaceConverter.toStrikeSurface(((LocalVolatilitySurfaceMoneyness) localVolatility));
    } else {
      throw new IllegalArgumentException("Cannot handle surface of type " + localVolatility.getClass());
    }
    return gridGreeks(forwardCurve, strikeLocalVolatility, isCall, option);
  }

  public BucketedGreekResultCollection getBucketedVega(final SmileSurfaceDataBundle data, final LocalVolatilitySurface<?> localVolatility,
      final EuropeanVanillaOption option) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(localVolatility, "local volatility surface");
    LocalVolatilitySurfaceStrike strikeLocalVolatility;
    if (localVolatility instanceof LocalVolatilitySurfaceStrike) {
      strikeLocalVolatility = (LocalVolatilitySurfaceStrike) localVolatility;
    } else if (localVolatility instanceof LocalVolatilitySurfaceMoneyness) {
      strikeLocalVolatility = LocalVolatilitySurfaceConverter.toStrikeSurface(((LocalVolatilitySurfaceMoneyness) localVolatility));
    } else {
      throw new IllegalArgumentException("Cannot handle surface of type " + localVolatility.getClass());
    }
    return bucketedVega(strikeLocalVolatility, data, option);
  }

  /**
   * Run a forward PDE solver to get model prices (and thus implied vols) and compare these with the market data.
   * Also output the (model) implied volatility as a function of strike for each tenor.
   * @param ps The print stream
   */
  private PDEFullResults1D runPDESolver(final ForwardCurve forwardCurve, final LocalVolatilitySurface<?> localVolatility, final double[] expiries, final boolean isCall) {
    final int nExpiries = expiries.length;
    final double maxT = expiries[nExpiries - 1];
    //TODO check type of local vol surface
    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVolatility, isCall, _theta, maxT, _maxAbsProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    return pdeRes;
  }

  private PDEResultCollection gridGreeks(final ForwardCurve forwardCurve, final LocalVolatilitySurfaceStrike localVolatility, final boolean isCall, final EuropeanVanillaOption option) {
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);

    final double forwardShift = 5e-2;
    final double volShift = 1e-4;

    final LocalVolatilitySurfaceStrike localVolatilityUp = new LocalVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(localVolatility.getSurface(), volShift, true));
    final LocalVolatilitySurfaceStrike localVolatilityDown = new LocalVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(localVolatility.getSurface(), -volShift, true));

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVolatility, isCall, _theta, expiry, _maxAbsProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardUp = runForwardPDESolver(forwardCurve.withFractionalShift(forwardShift), localVolatility, isCall,
        _theta, expiry, _maxAbsProxyDelta, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardDown = runForwardPDESolver(forwardCurve.withFractionalShift(-forwardShift), localVolatility, isCall,
        _theta, expiry, _maxAbsProxyDelta, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResVolUp = runForwardPDESolver(forwardCurve, localVolatilityUp, isCall, _theta, expiry, _maxAbsProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResVolDown = runForwardPDESolver(forwardCurve, localVolatilityDown, isCall, _theta, expiry, _maxAbsProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEResults1D pdeResForwardUpVolUp = runForwardPDESolver(forwardCurve.withFractionalShift(forwardShift), localVolatilityUp, isCall, _theta, expiry, _maxAbsProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardUpVolDown = runForwardPDESolver(forwardCurve.withFractionalShift(forwardShift), localVolatilityDown, isCall, _theta, expiry, _maxAbsProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardDownVolUp = runForwardPDESolver(forwardCurve.withFractionalShift(-forwardShift), localVolatilityUp, isCall, _theta, expiry, _maxAbsProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
    final PDEFullResults1D pdeResForwardDownVolDown = runForwardPDESolver(forwardCurve.withFractionalShift(-forwardShift), localVolatilityDown, isCall, _theta, expiry, _maxAbsProxyDelta,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);

    final double[] timeNodes = pdeRes.getGrid().getTimeNodes();
    final double[] spaceNodes = pdeRes.getGrid().getSpaceNodes();
    final int n = pdeRes.getNumberSpaceNodes();
    final DoubleArrayList strikes = new DoubleArrayList();
    final DoubleArrayList impliedVolatilities = new DoubleArrayList();
    final DoubleArrayList prices = new DoubleArrayList();
    final DoubleArrayList blackPrices = new DoubleArrayList();
    final DoubleArrayList absoluteDomesticPrice = new DoubleArrayList();
    //final DoubleArrayList absoluteForeignPrice = new DoubleArrayList();
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
    for (int i = 0; i < n - 1; i++) {
      final double moneyness = pdeRes.getSpaceValue(i);
      final double k = moneyness * forward;
      final double mPrice = pdeRes.getFunctionValue(i);
      double impVol = 0;
      try {
        impVol = getBSImpliedVol(mPrice, moneyness, expiry, isCall);
      } catch (final Exception e) {
        continue;
      }

      final int timeIndex = SurfaceArrayUtils.getLowerBoundIndex(timeNodes, expiry);
      final int spaceIndex = SurfaceArrayUtils.getLowerBoundIndex(spaceNodes, moneyness);
      final double value1 = forward * pdeRes.getFunctionValue(spaceIndex, timeIndex);
      final double value2 = forward * pdeRes.getFunctionValue(spaceIndex + 1, timeIndex);
      final double m1 = pdeRes.getSpaceValue(spaceIndex);
      final double m2 = pdeRes.getSpaceValue(spaceIndex + 1);
      //review R White 9/3/2012 This is pointless as moneyness == m1 (it is just the value at space index i) - what we should be doing
      //is finding the price (and all other greeks) for a given moneyness (corresponding to an option) that is not on the grid
      //So price and blackPrice should be the same (within the round trip to implied volatility)
      final double price = ((m2 - moneyness) * value1 + (moneyness - m1) * value2) / (m2 - m1);
      final double blackPrice = BlackFormulaRepository.price(forward, k, expiry, impVol, isCall);
      strikes.add(k);
      impliedVolatilities.add(impVol);
      prices.add(price);
      blackPrices.add(blackPrice);
      bsDelta.add(getBSDelta(forward, k, expiry, impVol, isCall));
      bsDualDelta.add(getBSDualDelta(forward, k, expiry, impVol, isCall));
      bsGamma.add(getBSGamma(forward, k, expiry, impVol));
      bsDualGamma.add(getBSDualGamma(forward, k, expiry, impVol));
      bsVega.add(getBSVega(forward, k, expiry, impVol));
      bsVanna.add(getBSVanna(forward, k, expiry, impVol));
      bsVomma.add(getBSVomma(forward, k, expiry, impVol));
      absoluteDomesticPrice.add(Math.PI); //DEBUG - just trying to get a number through the system

      final double modelDD = getModelDualDelta(pdeRes, i);
      modelDualDelta.add(modelDD);
      final double fixedSurfaceDelta = getFixedSurfaceDelta(mPrice, moneyness, modelDD);
      final double surfaceDelta = getSurfaceDelta(pdeResForwardUp, pdeResForwardDown, forward, forwardShift, i);
      final double modelD = getModelDelta(fixedSurfaceDelta, forward, surfaceDelta);
      modelDelta.add(modelD);

      final double modelDG = getModelDualGamma(pdeRes, i, forward);
      modelDualGamma.add(modelDG);
      final double fixedSurfaceGamma = getFixedSurfaceGamma(moneyness, modelDG);
      final double dSurfaceDMoneyness = getDSurfaceDMoneyness(pdeResForwardUp, pdeResForwardDown, forward, forwardShift, i);
      final double surfaceGamma = getSurfaceGamma(pdeResForwardUp, pdeResForwardDown, pdeRes, forward, forwardShift, i);
      modelGamma.add(getModelGamma(fixedSurfaceGamma, surfaceDelta, moneyness, dSurfaceDMoneyness, surfaceGamma));

      modelVega.add(getModelVega(pdeResVolUp, pdeResVolDown, volShift, i));
      final double xVanna = getXVanna(volShift, pdeResVolUp, pdeResVolDown, i, moneyness);
      final double surfaceVanna = getSurfaceVanna(pdeResForwardUpVolUp, pdeResForwardUpVolDown, pdeResForwardDownVolUp, pdeResForwardDownVolDown, volShift, forwardShift, i);
      modelVanna.add(getModelVanna(xVanna, surfaceVanna));
      modelVomma.add(getModelVomma(pdeRes, pdeResVolUp, pdeResVolDown, volShift, i));
    }
    final PDEResultCollection result = new PDEResultCollection(strikes.toDoubleArray());
    result.put(PDEResultCollection.GRID_PRICE, prices.toDoubleArray());
    result.put(PDEResultCollection.GRID_BLACK_PRICE, blackPrices.toDoubleArray());
    result.put(PDEResultCollection.GRID_IMPLIED_VOL, impliedVolatilities.toDoubleArray());
    result.put(PDEResultCollection.GRID_BLACK_DELTA, bsDelta.toDoubleArray());
    result.put(PDEResultCollection.GRID_BLACK_DUAL_DELTA, bsDualDelta.toDoubleArray());
    result.put(PDEResultCollection.GRID_BLACK_GAMMA, bsGamma.toDoubleArray());
    result.put(PDEResultCollection.GRID_BLACK_DUAL_GAMMA, bsDualGamma.toDoubleArray());
    result.put(PDEResultCollection.GRID_BLACK_VEGA, bsVega.toDoubleArray());
    result.put(PDEResultCollection.GRID_BLACK_VANNA, bsVanna.toDoubleArray());
    result.put(PDEResultCollection.GRID_BLACK_VOMMA, bsVomma.toDoubleArray());
    result.put(PDEResultCollection.GRID_DELTA, modelDelta.toDoubleArray());
    result.put(PDEResultCollection.GRID_DUAL_DELTA, modelDualDelta.toDoubleArray());
    result.put(PDEResultCollection.GRID_GAMMA, modelGamma.toDoubleArray());
    result.put(PDEResultCollection.GRID_DUAL_GAMMA, modelDualGamma.toDoubleArray());
    result.put(PDEResultCollection.GRID_VEGA, modelVega.toDoubleArray());
    result.put(PDEResultCollection.GRID_VANNA, modelVanna.toDoubleArray());
    result.put(PDEResultCollection.GRID_VOMMA, modelVomma.toDoubleArray());
    result.put(PDEResultCollection.GRID_DOMESTIC_PV_QUOTE, absoluteDomesticPrice.toDoubleArray());
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
    final boolean isCall = true; //TODO have this as an option  data.isCallData();

    final double[][] strikes = data.getStrikes();
    final int nExpiries = expiries.length;
    final double forward = forwardCurve.getForward(option.getTimeToExpiry());
    final double maxT = option.getTimeToExpiry();
    final double x = option.getStrike() / forward;

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVolatility, isCall, _theta, maxT, _maxAbsProxyDelta,
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
            _maxAbsProxyDelta, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, 1.0);
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
      final boolean isCall, final double theta, final double maxT, final double maxAbsProxyDelta, final int
      nTimeSteps, final int nStrikeSteps, final double timeMeshLambda, final double strikeMeshBunching, final double centreMoneyness) {

    final PDE1DCoefficientsProvider provider = new PDE1DCoefficientsProvider();
    ConvectionDiffusionPDE1DCoefficients pde;
    if (localVolatility instanceof LocalVolatilitySurfaceStrike) {
      pde = provider.getForwardLocalVol(forwardCurve, (LocalVolatilitySurfaceStrike) localVolatility);
    } else if (localVolatility instanceof LocalVolatilitySurfaceMoneyness) {
      pde = provider.getForwardLocalVol((LocalVolatilitySurfaceMoneyness) localVolatility);
    } else {
      throw new IllegalArgumentException();
    }
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
    final Function1D<Double, Double> initialCond = (new InitialConditionsProvider()).getForwardCallPut(isCall);
    final PDEFullResults1D res = (PDEFullResults1D) solver.solve(new PDE1DDataBundle<>(pde, initialCond, lower, upper, grid));
    return res;
  }
}
