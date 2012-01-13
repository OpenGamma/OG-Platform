/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.local;

import java.util.Arrays;
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
import com.opengamma.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.financial.model.finitedifference.applications.PDEDataBundleProvider;
import com.opengamma.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.PiecewiseSABRSurfaceFitter;
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

  private final double _modMoneynessParameter;

  private final double _theta;
  private final int _timeSteps;
  private final int _spaceSteps;
  private final double _timeGridBunching;
  private final double _spaceGridBunching;

  public LocalVolatilityForwardPDEGreekCalculator() {
    this(100, 0.55, 100, 100, 5, 0.05);
  }

  public LocalVolatilityForwardPDEGreekCalculator(final double modifiedMoneynessParameter, final double theta, final int timeSteps, final int spaceSteps,
      final double timeGridBunching, final double spaceGridBunching) {
    _modMoneynessParameter = modifiedMoneynessParameter;
    _theta = theta;
    _timeSteps = timeSteps;
    _spaceSteps = spaceSteps;
    _timeGridBunching = timeGridBunching;
    _spaceGridBunching = spaceGridBunching;
  }

  //greeks = forward price wrt relevant forward
  public void getGreeks(final double expiry, final double strike, final boolean isCall, final double[] expiries, final double[][] strikes,
      final double[][] impliedVols, final ForwardCurve forwardCurve) {
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

    final double[] forwards = new double[nExpiries];
    for (int i = 0; i < nExpiries; i++) {
      forwards[i] = forwardCurve.getForward(expiries[i]);
    }
    final double spot = forwardCurve.getSpot();
    final double forward = forwardCurve.getForward(expiry);
    final double spotShift = 5e-2 * spot;
    final double volShift = 1e-4;
    final double maxT = expiries[nExpiries - 1];
    final double maxStrike = 3.5 * forwardCurve.getForward(maxT);

    final PiecewiseSABRSurfaceFitter surfaceFitter = new PiecewiseSABRSurfaceFitter(forwards, expiries, strikes, impliedVols);
    final BlackVolatilitySurface impVolSurface = surfaceFitter.getImpliedVolatilitySurface(true, false, _modMoneynessParameter);
    final LocalVolatilitySurface localVolatilitySurface = DUPIRE.getLocalVolatility(impVolSurface, forwardCurve);
    final ForwardCurve forwardCurveUp = forwardCurve.withShiftedSpot(spotShift);
    final ForwardCurve forwardCurveDown = forwardCurve.withShiftedSpot(-spotShift);
    final LocalVolatilitySurface localVolatilitySurfaceUp = new LocalVolatilitySurface(SurfaceShiftFunctionFactory.getShiftedSurface(localVolatilitySurface.getSurface(), volShift, true));
    final LocalVolatilitySurface localVolatilitySurfaceDown = new LocalVolatilitySurface(SurfaceShiftFunctionFactory.getShiftedSurface(localVolatilitySurface.getSurface(), -volShift, true));

    final PDEFullResults1D pdeRes = runForwardPDESolver(forwardCurve, localVolatilitySurface, isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final PDEFullResults1D pdeResSpotUp = runForwardPDESolver(forwardCurveUp, localVolatilitySurface, isCall,
        _theta, maxT, maxStrike, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final PDEFullResults1D pdeResSpotDown = runForwardPDESolver(forwardCurveDown, localVolatilitySurface, isCall,
        _theta, maxT, maxStrike, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final PDEFullResults1D pdeResLocalVolUp = runForwardPDESolver(forwardCurve, localVolatilitySurfaceUp, isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final PDEFullResults1D pdeResLocalVolDown = runForwardPDESolver(forwardCurve, localVolatilitySurfaceDown, isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final PDEFullResults1D pdeResSpotUpLocalVolUp = runForwardPDESolver(forwardCurveUp, localVolatilitySurfaceUp, isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final PDEFullResults1D pdeResSpotUpLocalVolDown = runForwardPDESolver(forwardCurveUp, localVolatilitySurfaceDown, isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final PDEFullResults1D pdeResSpotDownLocalVolUp = runForwardPDESolver(forwardCurveDown, localVolatilitySurfaceUp, isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
    final PDEFullResults1D pdeResSpotDownLocalVolDown = runForwardPDESolver(forwardCurveDown, localVolatilitySurfaceDown, isCall, _theta, maxT, maxStrike,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);

    final BlackVolatilitySurface modelSurface = priceToVolSurface(forwardCurve, pdeRes, 0.0, maxT, 0, maxStrike, isCall);
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

    //get dual delta & gamma by finite difference on grid, and (normal) delta and gamma by fd on separate grids, for
    // a range of strikes (i.e. the spot is fixed)
    final int n = pdeRes.getNumberSpaceNodes();
    final double[] k = getStrikes(n, pdeRes);
    final double[] prices = getPrices(n, pdeRes);
    final double[] bsVol = getBlackEquivalentVolatilityForStrikes(n, k, actExpiry, modelSurface);
    final double[] bsVol1 = getBlackEquivalentVolatilityForStrikes(n, k, actExpiry, forward, isCall, prices);
    final double[] bsDelta = getBlackEquivalentDeltaForStrikes(n, k, actExpiry, forward, isCall, bsVol);
    final double[] bsGamma = getBlackEquivalentGammaForStrikes(n, k, actExpiry, forward, bsVol);
    final double[] bsDualDelta = getBlackEquivalentDualDeltaForStrikes(n, k, actExpiry, forward, isCall, bsVol);
    final double[] bsDualGamma = getBlackEquivalentDualGammaForStrikes(n, k, actExpiry, forward, isCall, bsVol);
    final double[] modelDelta = getModelDeltaForStrikes(n, pdeResSpotUp, pdeResSpotDown, tIndex, spotShift, forward, isCall);
    final double[] modelGamma = getModelGammaForStrikes(n, pdeRes, pdeResSpotUp, pdeResSpotDown, tIndex, spotShift, forward, isCall);
    final double[] modelDualDelta = getModelDualDeltaForStrikes(n, pdeRes, tIndex, forward, isCall);
    final double[] modelDualGamma = getModelDualGammaForStrikes(n, pdeRes, tIndex, forward, isCall);
    final double[] bsVega = getBlackEquivalentVegaForStrikes(n, k, actExpiry, forward, bsVol1);
    final double[] bsVanna = getBlackEquivalentVannaForStrikes(n, k, actExpiry, forward, bsVol1);
    final double[] bsVomma = getBlackEquivalentVommaForStrikes(n, k, actExpiry, forward, bsVol1);
    final double[] modelVega = getModelVegaForStrikes(n, pdeResLocalVolUp, pdeResLocalVolDown, volShift);
    final double[] modelVanna = getModelVannaForStrikes(n, pdeResSpotUpLocalVolUp, pdeResSpotUpLocalVolDown, pdeResSpotDownLocalVolUp, pdeResSpotDownLocalVolDown, spotShift, volShift);
    final double[] modelVomma = getModelVommaForStrikes(n, pdeResSpotDownLocalVolDown, pdeResLocalVolUp, pdeResLocalVolDown, volShift);
    final double[][] bucketedVega = getBucketedVega(pdeRes, actExpiry, strike, forward, nExpiries, strikes, forwardCurve, maxT, maxStrike, isCall, forwards, expiries, impliedVols);
  }

  private double[] getStrikes(final int n, final PDEFullResults1D pdeRes) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = pdeRes.getSpaceValue(i);
    }
    return result;
  }

  private double[] getBlackEquivalentVolatilityForStrikes(final int n, final double[] k, final double actExpiry, final BlackVolatilitySurface modelSurface) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = modelSurface.getVolatility(actExpiry, k[i]);
    }
    return result;
  }

  private double[] getBlackEquivalentDeltaForStrikes(final int n, final double[] k, final double actExpiry, final double forward, final boolean isCall, final double[] bsVol) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = BlackFormulaRepository.delta(forward, k[i], actExpiry, bsVol[i], isCall);
    }
    return result;
  }

  private double[] getBlackEquivalentDualDeltaForStrikes(final int n, final double[] k, final double actExpiry,
      final double forward, final boolean isCall, final double[] bsVol) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = BlackFormulaRepository.dualDelta(forward, k[i], actExpiry, bsVol[i], isCall);
    }
    return result;
  }

  private double[] getBlackEquivalentGammaForStrikes(final int n, final double[] k, final double actExpiry, final double forward, final double[] bsVol) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = BlackFormulaRepository.gamma(forward, k[i], actExpiry, bsVol[i]);
    }
    return result;
  }

  private double[] getBlackEquivalentDualGammaForStrikes(final int n, final double[] k, final double actExpiry, final double forward, final boolean isCall, final double[] bsVol) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = BlackFormulaRepository.dualGamma(forward, k[i], actExpiry, bsVol[i]);
    }
    return result;
  }

  private double[] getPrices(final int n, final PDEFullResults1D pdeRes) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = pdeRes.getFunctionValue(i);
    }
    return result;
  }

  private double[] getBlackEquivalentVolatilityForStrikes(final int n, final double[] k, final double actExpiry, final double forward, final boolean isCall, final double[] prices) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = BlackFormulaRepository.impliedVolatility(prices[i], forward, k[i], actExpiry, isCall);
    }
    return result;
  }

  private double[] getBlackEquivalentVegaForStrikes(final int n, final double[] k, final double actExpiry, final double forward, final double[] bsVol) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = BlackFormulaRepository.vega(forward, k[i], actExpiry, bsVol[i]);
    }
    return result;
  }

  private double[] getBlackEquivalentVannaForStrikes(final int n, final double[] k, final double actExpiry, final double forward, final double[] bsVol) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = BlackFormulaRepository.vanna(forward, k[i], actExpiry, bsVol[i]);
    }
    return result;
  }

  private double[] getBlackEquivalentVommaForStrikes(final int n, final double[] k, final double actExpiry, final double forward, final double[] bsVol) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = BlackFormulaRepository.vomma(forward, k[i], actExpiry, bsVol[i]);
    }
    return result;
  }

  private double[] getModelDeltaForStrikes(final int n, final PDEFullResults1D pdeResSpotUp, final PDEFullResults1D pdeResSpotDown, final int tIndex, final double spotShift,
      final double forward, final boolean isCall) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = (pdeResSpotUp.getFunctionValue(i, tIndex) - pdeResSpotDown.getFunctionValue(i, tIndex)) / 2 / spotShift;
    }
    return result;
  }

  private double[] getModelDualDeltaForStrikes(final int n, final PDEFullResults1D pdeRes, final int tIndex, final double forward, final boolean isCall) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = pdeRes.getFirstSpatialDerivative(i, tIndex);
    }
    return result;
  }

  private double[] getModelGammaForStrikes(final int n, final PDEFullResults1D pdeRes, final PDEFullResults1D pdeResSpotUp, final PDEFullResults1D pdeResSpotDown, final int tIndex,
      final double spotShift, final double forward, final boolean isCall) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = (pdeResSpotUp.getFunctionValue(i, tIndex) + pdeResSpotDown.getFunctionValue(i, tIndex) - 2 * pdeRes.getFunctionValue(i, tIndex)) / spotShift / spotShift;
    }
    return result;
  }

  private double[] getModelDualGammaForStrikes(final int n, final PDEFullResults1D pdeRes, final int tIndex, final double forward, final boolean isCall) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = pdeRes.getSecondSpatialDerivative(i, tIndex);
    }
    return result;
  }

  private double[] getModelVegaForStrikes(final int n, final PDEFullResults1D pdeResLocalVolUp, final PDEFullResults1D pdeResLocalVolDown, final double volShift) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = (pdeResLocalVolUp.getFunctionValue(i) - pdeResLocalVolDown.getFunctionValue(i)) / 2 / volShift;
    }
    return result;
  }

  private double[] getModelVannaForStrikes(final int n, final PDEFullResults1D pdeResSpotUpLocalVolUp, final PDEFullResults1D pdeResSpotUpLocalVolDown, final PDEFullResults1D pdeResSpotDownLocalVolUp,
      final PDEFullResults1D pdeResSpotDownLocalVolDown, final double spotShift, final double volShift) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = (pdeResSpotUpLocalVolUp.getFunctionValue(i) + pdeResSpotDownLocalVolDown.getFunctionValue(i) -
          pdeResSpotUpLocalVolDown.getFunctionValue(i) - pdeResSpotDownLocalVolUp.getFunctionValue(i)) / 4 / spotShift / volShift;
    }
    return result;
  }

  private double[] getModelVommaForStrikes(final int n, final PDEFullResults1D pdeRes, final PDEFullResults1D pdeResLocalVolUp, final PDEFullResults1D pdeResLocalVolDown, final double volShift) {
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = (pdeResLocalVolUp.getFunctionValue(i) + pdeResLocalVolDown.getFunctionValue(i) - 2 * pdeRes.getFunctionValue(i)) / volShift / volShift;
    }
    return result;
  }

  private double[][] getBucketedVega(final PDEFullResults1D pdeRes, final double actExpiry, final double strike, final double forward, final int nExpiries, final double[][] strikes,
      final ForwardCurve forwardCurve, final double maxT, final double maxStrike, final boolean isCall, final double[] forwards, final double[] expiries, final double[][] impliedVols) {
    final PiecewiseSABRSurfaceFitter surfaceFitter = new PiecewiseSABRSurfaceFitter(forwards, expiries, strikes, impliedVols);
    final double[] strikeNodes = pdeRes.getGrid().getSpaceNodes();
    int index = getLowerBoundIndex(strikeNodes, strike);
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
    final double[] nearStrikes = new double[4];
    System.arraycopy(strikeNodes, index, strikes, 0, 4);
    for (int i = 0; i < 4; i++) {
      vols[i] = BlackFormulaRepository.impliedVolatility(pdeRes.getFunctionValue(index + i), forward, nearStrikes[i],
          actExpiry, isCall);
    }
    Interpolator1DDoubleQuadraticDataBundle db = INTERPOLATOR_1D.getDataBundle(nearStrikes, vols);
    final double exampleVol = INTERPOLATOR_1D.interpolate(db, strike);

    final double shiftAmount = 1e-4; //1bps

    final double[][] result = new double[nExpiries][];

    for (int i = 0; i < nExpiries; i++) {
      final int m = strikes[i].length;
      result[i] = new double[m];
      for (int j = 0; j < m; j++) {
        final PiecewiseSABRSurfaceFitter fitter = surfaceFitter.withBumpedPoint(i, j, shiftAmount);
        final BlackVolatilitySurface bumpedSurface = fitter.getImpliedVolatilitySurface(true, false, _modMoneynessParameter);
        final LocalVolatilitySurface bumpedLV = DUPIRE.getLocalVolatility(bumpedSurface, forwardCurve);
        final PDEFullResults1D pdeResBumped = runForwardPDESolver(forwardCurve, bumpedLV, isCall, _theta, maxT,
            maxStrike, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, strike);
        for (int k = 0; k < 4; k++) {
          vols[k] = BlackFormulaRepository.impliedVolatility(pdeResBumped.getFunctionValue(index + k), forward, nearStrikes[k],
              actExpiry, isCall);
        }
        db = INTERPOLATOR_1D.getDataBundle(nearStrikes, vols);
        final double vol = INTERPOLATOR_1D.interpolate(db, strike);
        result[i][j] = (vol - exampleVol) / shiftAmount;
      }
    }
    return result;
  }

  private PDEFullResults1D runForwardPDESolver(final ForwardCurve forwardCurve, final LocalVolatilitySurface localVolatility, final boolean isCall,
      final double theta, final double maxT, final double maxStrike, final int nTimeSteps, final int nStrikeSteps, final double timeMeshLambda,
      final double strikeMeshBunching, final double centreStrike) {

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

  private BlackVolatilitySurface priceToVolSurface(final ForwardCurve forwardCurve, final PDEFullResults1D prices,
      final double minT, final double maxT, final double minStrike, final double maxStrike, final boolean isCall) {

    final Map<DoublesPair, Double> vol = PDEUtilityTools.priceToImpliedVol(forwardCurve, prices, minT, maxT, minStrike, maxStrike, isCall);
    final Map<Double, Interpolator1DDataBundle> idb = GRID_INTERPOLATOR2D.getDataBundle(vol);

    final Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        return GRID_INTERPOLATOR2D.interpolate(idb, new DoublesPair(tk[0], tk[1]));
      }
    };

    return new BlackVolatilitySurface(FunctionalDoublesSurface.from(func));
  }

  //TODO there is a method in Find that does this
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
