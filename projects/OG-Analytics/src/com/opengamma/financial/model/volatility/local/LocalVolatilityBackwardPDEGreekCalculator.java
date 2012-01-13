/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.local;

import java.util.Arrays;

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
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.PiecewiseSABRSurfaceFitter;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;

/**
 * 
 */
public class LocalVolatilityBackwardPDEGreekCalculator {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final DupireLocalVolatilityCalculator DUPIRE = new DupireLocalVolatilityCalculator();

  private final double _modMoneynessParameter;

  private final double _theta;
  private final int _timeSteps;
  private final int _spaceSteps;
  private final double _timeGridBunching;
  private final double _spaceGridBunching;

  public LocalVolatilityBackwardPDEGreekCalculator() {
    this(100, 0.55, 100, 100, 5, 0.05);
  }

  public LocalVolatilityBackwardPDEGreekCalculator(final double modifiedMoneynessParameter, final double theta, final int timeSteps, final int spaceSteps,
      final double timeGridBunching, final double spaceGridBunching) {
    _modMoneynessParameter = modifiedMoneynessParameter;
    _theta = theta;
    _timeSteps = timeSteps;
    _spaceSteps = spaceSteps;
    _timeGridBunching = timeGridBunching;
    _spaceGridBunching = spaceGridBunching;
  }

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
    final double maxT = expiries[nExpiries - 1];
    final double maxSpot = 3.5 * Math.max(strike, forwardCurve.getForward(maxT));
    final PiecewiseSABRSurfaceFitter surfaceFitter = new PiecewiseSABRSurfaceFitter(forwards, expiries, strikes, impliedVols);
    final BlackVolatilitySurface impVolSurface = surfaceFitter.getImpliedVolatilitySurface(true, false, _modMoneynessParameter);
    final LocalVolatilitySurface localVolatilitySurface = DUPIRE.getLocalVolatility(impVolSurface, forwardCurve);
    PDEFullResults1D pdeRes = runBackwardsPDESolver(strike, localVolatilitySurface, isCall, _theta, maxT, maxSpot,
        _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);

    //adjust the expiry to the nearest grid point
    //TODO should be able to adjust the grid instead
    final double[] timeNodes = pdeRes.getGrid().getTimeNodes();
    final double[] spotNodes = pdeRes.getGrid().getSpaceNodes();
    int tIndex = getLowerBoundIndex(timeNodes, expiry);
    if (tIndex < timeNodes.length - 1) {
      final double dT1 = (expiry - timeNodes[tIndex]);
      final double dT2 = (timeNodes[tIndex + 1] - expiry);
      if (dT1 > dT2) {
        tIndex++;
      }
    }
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
    final double actExpiry = timeNodes[tIndex];
    final int n = pdeRes.getNumberSpaceNodes();
    final int xIndex = pdeRes.getGrid().getLowerBoundIndexForSpace(spot);
    final double[] bsDelta = new double[n];
    final double[] bsGamma = new double[n];
    final double[] modelDelta = new double[n];
    final double[] modelGamma = new double[n];
    final double actSpot = pdeRes.getSpaceValue(xIndex);
    for (int i = 0; i < n; i++) {
      final double k = minK + (maxK - minK) * i / (n - 1);
      pdeRes = runBackwardsPDESolver(k, localVolatilitySurface, isCall, _theta, actExpiry, maxSpot,
          _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, spot);
      final double price = pdeRes.getFunctionValue(xIndex);
      double vol = 0;
      try {
        vol = BlackFormulaRepository.impliedVolatility(price, actSpot, k, actExpiry, isCall);
      } catch (final Exception e) {
      }
      bsDelta[i] = BlackFormulaRepository.delta(actSpot, strike, actExpiry, vol, isCall);
      bsGamma[i] = BlackFormulaRepository.gamma(actSpot, strike, actExpiry, vol);
      modelDelta[i] = pdeRes.getFirstSpatialDerivative(xIndex);
      modelGamma[i] = pdeRes.getSecondSpatialDerivative(xIndex);
    }
    final double[][] bucketedVega = new double[nExpiries][];
    final double[] vols = new double[4];
    final double[] spots = new double[4];
    System.arraycopy(spotNodes, index, spots, 0, 4);
    for (int i = 0; i < 4; i++) {
      vols[i] = BlackFormulaRepository.impliedVolatility(pdeRes.getFunctionValue(index + i), spots[i], strike, actExpiry, isCall);
    }
    Interpolator1DDoubleQuadraticDataBundle db = INTERPOLATOR_1D.getDataBundle(spots, vols);
    final double exampleVol = INTERPOLATOR_1D.interpolate(db, spot);
    final double volShift = 1e-4;
    for (int i = 0; i < nExpiries; i++) {
      final int m = strikes[i].length;
      bucketedVega[i] = new double[m];
      for (int j = 0; j < m; j++) {
        final PiecewiseSABRSurfaceFitter fitter = surfaceFitter.withBumpedPoint(i, j, volShift);
        final BlackVolatilitySurface bumpedSurface = fitter.getImpliedVolatilitySurface(true, false, _modMoneynessParameter);
        final LocalVolatilitySurface bumpedLV = DUPIRE.getLocalVolatility(bumpedSurface, forwardCurve);
        final PDEResults1D pdeResBumped = runBackwardsPDESolver(strike, bumpedLV, isCall, _theta, actExpiry,
            maxSpot, _timeSteps, _spaceSteps, _timeGridBunching, _spaceGridBunching, strike);
        for (int k = 0; k < 4; k++) {
          vols[k] = BlackFormulaRepository.impliedVolatility(pdeResBumped.getFunctionValue(index + k), spots[k], strike, actExpiry, isCall);
        }
        db = INTERPOLATOR_1D.getDataBundle(spots, vols);
        final double vol = INTERPOLATOR_1D.interpolate(db, spot);
        bucketedVega[i][j] = (vol - exampleVol) / volShift;
      }
    }
  }

  private PDEFullResults1D runBackwardsPDESolver(final double strike, final LocalVolatilitySurface localVolatility, final boolean isCall,
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
    final PDEFullResults1D res = (PDEFullResults1D) solver.solve(db, grid, lower, upper);
    return res;
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
