/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.CholeskyDecompositionCommons;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquareWithPenalty;
import com.opengamma.analytics.math.surface.NodalObjectsSurface;
import com.opengamma.util.ArgumentChecker;

/**
 * Global caplet stripping with smoothness penalty across strike-time plane.
 * @deprecated use {@link CapletStripperDirect} instead  
 */
@Deprecated
public class CapletStrippingDirectGlobalWithPenalty {
  private final List<CapFloorPricer>[] _capPricers;
  private final CapletVolatilityNodalSurfaceProvider _nodalSurfaceProvider;
  private final int _nCapsTotal;

  private static final NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty(new CholeskyDecompositionCommons(), MatrixAlgebraFactory.OG_ALGEBRA, 1e-8);
  private static final double DEFAULT_LAMBDA_STRIKE = 0.001;
  private static final double DEFAULT_LAMBDA_TIME = 10.0;
  private static final double DEFAULT_ERROR = 1.0e-4;
  private final DoubleMatrix2D _penalty;

  /**
   * Constructor using default smoothing parameters
   * @param caps Caps as array of lists: each list contains caps with the same strike
   * @param curves The yield curve
   * @param nodalSurfaceProvider Provider for caplet volatilities as a nodal surface
   */
  public CapletStrippingDirectGlobalWithPenalty(final List<CapFloor>[] caps, final MulticurveProviderInterface curves, final CapletVolatilityNodalSurfaceProvider nodalSurfaceProvider) {
    this(caps, curves, nodalSurfaceProvider, DEFAULT_LAMBDA_STRIKE, DEFAULT_LAMBDA_TIME);
  }

  /**
   * Constructor specifying smoothing parameters
   * @param caps Caps as array of lists: each list contains caps with the same strike
   * @param curves The yield curve
   * @param nodalSurfaceProvider Provider for caplet volatilities as a nodal surface
   * @param lambdaStrike Smoothing parameter for strike direction
   * @param lambdaTime Smoothing parameter for time direction
   */
  public CapletStrippingDirectGlobalWithPenalty(final List<CapFloor>[] caps, final MulticurveProviderInterface curves, final CapletVolatilityNodalSurfaceProvider nodalSurfaceProvider,
      final double lambdaStrike, final double lambdaTime) {
    ArgumentChecker.noNulls(caps, "caps");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(nodalSurfaceProvider, "nodalSurfaceProvider");
    ArgumentChecker.isTrue(Doubles.isFinite(lambdaStrike), "lambdaStrike should be finite");
    ArgumentChecker.isTrue(lambdaStrike >= 0, "lambdaStrike should be non-negative");
    ArgumentChecker.isTrue(Doubles.isFinite(lambdaTime), "lambdaTime should be finite");
    ArgumentChecker.isTrue(lambdaTime >= 0, "lambdaTime should be non-negative");

    _nodalSurfaceProvider = nodalSurfaceProvider;

    final int nStrikes = caps.length;
    _capPricers = new List[nStrikes];
    int nCapsTotal = 0;
    for (int i = 0; i < nStrikes; ++i) {
      final int n = caps[i].size();
      nCapsTotal += n;
      _capPricers[i] = new ArrayList<>(n);
      for (final CapFloor cap : caps[i]) {
        _capPricers[i].add(new CapFloorPricer(cap, curves));
      }
    }

    _nCapsTotal = nCapsTotal;
    _penalty = _nodalSurfaceProvider.getPenaltyMatrix(lambdaStrike, lambdaTime);
  }

  /**
   * Solve nonlinear least square with default errors and guess values
   * @param capVols Market cap implied volatilities
   * @return {@link LeastSquareResults}
   */
  public LeastSquareResults solveForVol(final double[][] capVols) {
    ArgumentChecker.noNulls(capVols, "capVols");

    final DoubleMatrix1D capVolVec = toVector(capVols, _nCapsTotal);
    final int nObs = capVolVec.getNumberOfElements();
    final double[] errors = new double[nObs];
    Arrays.fill(errors, DEFAULT_ERROR);
    final int nNodes = _nodalSurfaceProvider.getNumberOfNodes();
    final double[] guess = new double[nNodes];
    Arrays.fill(guess, 0.7);

    final Function1D<DoubleMatrix1D, Boolean> allowed = new Function1D<DoubleMatrix1D, Boolean>() {
      @Override
      public Boolean evaluate(final DoubleMatrix1D x) {
        final double[] temp = x.getData();
        final int m = temp.length;
        for (int i = 0; i < m; i++) {
          if (temp[i] < 0) {
            return false;
          }
        }
        return true;
      }
    };

    try {
      return NLLSWP.solve(capVolVec, new DoubleMatrix1D(errors), getCapVolFunc, getCapVolJacFunc, new DoubleMatrix1D(guess), _penalty, allowed);
    } catch (final Exception e) { //try svd if lu fails
      final NonLinearLeastSquareWithPenalty lqWithSvd = new NonLinearLeastSquareWithPenalty();
      return lqWithSvd.solve(capVolVec, new DoubleMatrix1D(errors), getCapVolFunc, getCapVolJacFunc, new DoubleMatrix1D(guess), _penalty, allowed);
    }
  }

  /**
   * Solve nonlinear least square specifying errors and guess values
   * @param capVols Market cap implied volatilities
   * @param errors The measurement errors
   * @param guess The guess values
   * @return {@link LeastSquareResults}
   */
  public LeastSquareResults solveForVol(final double[][] capVols, final double[][] errors, final double[][] guess) {
    ArgumentChecker.noNulls(capVols, "capVols");
    ArgumentChecker.noNulls(errors, "errors");
    ArgumentChecker.noNulls(guess, "guess");

    final DoubleMatrix1D capVolVec = toVector(capVols, _nCapsTotal);
    final DoubleMatrix1D errorsVec = toVector(errors, _nCapsTotal);
    final DoubleMatrix1D guessVec = toVector(guess, _nodalSurfaceProvider.getNumberOfNodes());

    final Function1D<DoubleMatrix1D, Boolean> allowed = new Function1D<DoubleMatrix1D, Boolean>() {
      @Override
      public Boolean evaluate(final DoubleMatrix1D x) {
        final double[] temp = x.getData();
        final int m = temp.length;
        for (int i = 0; i < m; i++) {
          if (temp[i] < 0) {
            return false;
          }
        }
        return true;
      }
    };

    try {
      return NLLSWP.solve(capVolVec, errorsVec, getCapVolFunc, getCapVolJacFunc, guessVec, _penalty, allowed);
    } catch (final Exception e) { //try svd if lu fails
      final NonLinearLeastSquareWithPenalty lqWithSvd = new NonLinearLeastSquareWithPenalty();
      return lqWithSvd.solve(capVolVec, errorsVec, getCapVolFunc, getCapVolJacFunc, guessVec, _penalty, allowed);
    }
  }

  protected final DoubleMatrix2D getPenaltyMatrix() {
    return _penalty;
  }

  protected final Function1D<DoubleMatrix1D, DoubleMatrix1D> getCapVolFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final NodalObjectsSurface<Integer, Integer, Double> surface = _nodalSurfaceProvider.evaluate(x);
      final int nStrikes = _capPricers.length;

      final double[][] res = new double[nStrikes][];
      for (int i = 0; i < nStrikes; ++i) {
        final int len = _capPricers[i].size();
        res[i] = new double[len];
        for (int j = 0; j < len; ++j) {
          final CapFloorPricer pricer = _capPricers[i].get(j);
          final int nCaplets = pricer.getNumberCaplets();
          final double[] capVols = new double[nCaplets];
          for (int k = 0; k < nCaplets; ++k) {
            capVols[k] = surface.getZValue(i, k);
          }
          res[i][j] = pricer.impliedVol(capVols);
        }
      }
      return toVector(res, _nCapsTotal);
    }
  };

  protected final Function1D<DoubleMatrix1D, DoubleMatrix2D> getCapVolJacFunc = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
    @Override
    public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
      final int nParams = x.getNumberOfElements();
      final int nStrikes = _capPricers.length;
      final int nTimeNodes = _nodalSurfaceProvider.getFixingTimes().length;
      final NodalObjectsSurface<Integer, Integer, Double> surface = _nodalSurfaceProvider.evaluate(x);

      final double[][] res = new double[_nCapsTotal][nParams];
      int step = 0;
      for (int i = 0; i < nStrikes; ++i) {
        final int len = _capPricers[i].size();
        for (int j = 0; j < len; ++j) {
          final CapFloorPricer pricer = _capPricers[i].get(j);
          final int nCaplets = pricer.getNumberCaplets();
          final double[] capVols = new double[nCaplets];
          for (int k = 0; k < nCaplets; ++k) {
            capVols[k] = surface.getZValue(i, k);
          }
          final double vegaInv = 1.0 / pricer.vega(capVols);
          for (int l = 0; l < nCaplets; ++l) {
            final double capletVega = BlackFormulaRepository.vega(_capPricers[i].get(j).getCapletAsOptionData()[l], surface.getZValue(i, l));
            res[step + j][i * nTimeNodes + l] = vegaInv * capletVega;
          }
        }
        step += len;
      }
      return new DoubleMatrix2D(res);
    }
  };

  /*
   * Convert {{a00,a01,a02,...},{a10,a11,...},{a20,...},...} to {a00,a01,a02,....,a10,a11,....,a20,...}
   * Consistency for total number of elements is checked.
   */
  private DoubleMatrix1D toVector(final double[][] doubleArray, final int length) {
    final double[] res = new double[length];
    final int nRow = doubleArray.length;
    int k = 0;
    for (int i = 0; i < nRow; ++i) {
      final int len = doubleArray[i].length;
      System.arraycopy(doubleArray[i], 0, res, k, len);
      k += len;
    }
    ArgumentChecker.isTrue(k == length, "number of elements in input is different form expected vector length");
    return new DoubleMatrix1D(res);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.deepHashCode(_capPricers);
    result = prime * result + _nCapsTotal;
    result = prime * result + ((_nodalSurfaceProvider == null) ? 0 : _nodalSurfaceProvider.hashCode());
    result = prime * result + ((_penalty == null) ? 0 : _penalty.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CapletStrippingDirectGlobalWithPenalty)) {
      return false;
    }
    final CapletStrippingDirectGlobalWithPenalty other = (CapletStrippingDirectGlobalWithPenalty) obj;
    if (_nCapsTotal != other._nCapsTotal) {
      return false;
    }
    if (_nodalSurfaceProvider == null) {
      if (other._nodalSurfaceProvider != null) {
        return false;
      }
    } else if (!_nodalSurfaceProvider.equals(other._nodalSurfaceProvider)) {
      return false;
    }
    if (_penalty == null) {
      if (other._penalty != null) {
        return false;
      }
    } else if (!_penalty.equals(other._penalty)) {
      return false;
    }
    if (!Arrays.equals(_capPricers, other._capPricers)) {
      return false;
    }
    return true;
  }

}
