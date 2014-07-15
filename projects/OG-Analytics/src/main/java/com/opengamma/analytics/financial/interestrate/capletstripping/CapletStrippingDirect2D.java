/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * 
 */
public class CapletStrippingDirect2D {
  private static final double EPS = 1.e-7;

  private final List<CapFloorPricer>[] _capPricers;
  private final CapletNodalSurfaceProvider _nodalSurfaceProvider;
  private final int _nCapsTotal;

  private static final NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty(new CholeskyDecompositionCommons(), MatrixAlgebraFactory.OG_ALGEBRA, 1e-8);
  private static final double LAMBDA_TIME = 12.;
  private static final double LAMBDA_STRIKE = 0.004;
  private static final double ERROR = 1.0e-4;
  private final DoubleMatrix2D _penalty;

  // private final int _totalNodes;

  public CapletStrippingDirect2D(final List<CapFloor>[] caps, final MulticurveProviderInterface curves, final CapletNodalSurfaceProvider nodalSurfaceProvider) {

    ArgumentChecker.noNulls(caps, "caps");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(nodalSurfaceProvider, "nodalSurfaceProvider");

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
    _penalty = _nodalSurfaceProvider.getPenaltyMatrix(LAMBDA_STRIKE, LAMBDA_TIME);
  }

  public LeastSquareResults solveForVol(final double[][] capVols) {

    final DoubleMatrix1D capVolVec = toVector(capVols);
    final int nObs = capVolVec.getNumberOfElements();
    final double[] errors = new double[nObs];
    Arrays.fill(errors, ERROR);
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
      return NLLSWP.solve(capVolVec, new DoubleMatrix1D(errors), _capVols, _capVolsGrad, new DoubleMatrix1D(guess), _penalty, allowed);
    } catch (final Exception e) {
      final NonLinearLeastSquareWithPenalty lqWithSvd = new NonLinearLeastSquareWithPenalty();
      return lqWithSvd.solve(capVolVec, new DoubleMatrix1D(errors), _capVols, _capVolsGrad, new DoubleMatrix1D(guess), _penalty, allowed);
    }
  }

  public LeastSquareResults solveForVol(final double[][] capVols, final double[][] errors, final double[] guess) {

    final DoubleMatrix1D capVolVec = toVector(capVols);
    final DoubleMatrix1D errorsVec = toVector(errors);
    final int nObs = capVolVec.getNumberOfElements();
    ArgumentChecker.isTrue(errorsVec.getNumberOfElements() == nObs, "element numbers mismatch between capVols and errors");
    final int nNodes = _nodalSurfaceProvider.getNumberOfNodes();
    ArgumentChecker.isTrue(errors.length == nNodes, "wrong number of elements: guess values");

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
      return NLLSWP.solve(capVolVec, errorsVec, _capVols, _capVolsGrad, new DoubleMatrix1D(guess), _penalty, allowed);
    } catch (final Exception e) { //try svd if lu fails
      final NonLinearLeastSquareWithPenalty lqWithSvd = new NonLinearLeastSquareWithPenalty();
      return lqWithSvd.solve(capVolVec, errorsVec, _capVols, _capVolsGrad, new DoubleMatrix1D(guess), _penalty, allowed);
    }
  }

  private final Function1D<DoubleMatrix1D, DoubleMatrix1D> _capVols = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

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
          final Double[] capVols = new Double[nCaplets];
          for (int k = 0; k < nCaplets; ++k) {
            capVols[k] = surface.getZValue(i, k);
          }
          res[i][j] = pricer.impliedVol(capVols);
        }
      }
      return toVector(res);
    }
  };

  //  public Function1D<DoubleMatrix1D, DoubleMatrix2D> _capVolsDiff = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
  //
  //    @Override
  //    public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
  //      final int nParams = x.getNumberOfElements();
  //      ArgumentChecker.isTrue(nParams == _nodalSurfaceProvider.getNumberOfNodes(), "wrong parameter length");
  //      final DoubleMatrix1D base = _capVols.evaluate(x);
  //      final int nElements = base.getNumberOfElements();
  //      final double[][] res = new double[nElements][nParams];
  //      for (int j = 0; j < nParams; ++j) {
  //        final double[] params = Arrays.copyOf(x.getData(), nParams);
  //        params[j] += EPS;
  //        final DoubleMatrix1D up = _capVols.evaluate(new DoubleMatrix1D(params));
  //        for (int i = 0; i < nElements; ++i) {
  //          res[i][j] = (up.getData()[i] - base.getData()[i]) / EPS;
  //        }
  //      }
  //      //      System.out.println(new DoubleMatrix2D(res));
  //      return new DoubleMatrix2D(res);
  //    }
  //  };

  private Function1D<DoubleMatrix1D, DoubleMatrix2D> _capVolsGrad = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

    @Override
    public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
      final int nParams = x.getNumberOfElements();
      final int nStrikes = _capPricers.length;
      final int nTimeNodes = _nodalSurfaceProvider.getFixingTimes().length;
      ArgumentChecker.isTrue(nParams == _nodalSurfaceProvider.getNumberOfNodes(), "wrong parameter length");

      final NodalObjectsSurface<Integer, Integer, Double> surface = _nodalSurfaceProvider.evaluate(x);
      final double[][] res = new double[_nCapsTotal][nParams];
      int step = 0;
      for (int i = 0; i < nStrikes; ++i) {
        final int len = _capPricers[i].size();
        for (int j = 0; j < len; ++j) {
          final CapFloorPricer pricer = _capPricers[i].get(j);
          final int nCaplets = pricer.getNumberCaplets();
          final Double[] capVols = new Double[nCaplets];
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

  //  public DoubleMatrix2D makeMatrix(final double[][] array) {
  //    final int nRow = array.length;
  //    int nCol = 0;
  //    for (int i = 0; i < nRow; ++i) {
  //      nCol = Math.max(array[i].length, nCol);
  //    }
  //    final double[][] res = new double[nRow][nCol];
  //    for (int i = 0; i < nRow; ++i) {
  //      Arrays.fill(res[i], 0.);
  //      System.arraycopy(array[i], 0, res[i], 0, array[i].length);
  //    }
  //    return new DoubleMatrix2D(res);
  //  }

  private DoubleMatrix1D toVector(final double[][] doubleArray) {
    final double[] res = new double[_nCapsTotal];
    final int nRow = doubleArray.length;
    int k = 0;
    for (int i = 0; i < nRow; ++i) {
      final int len = doubleArray[i].length;
      for (int j = 0; j < len; ++j) {
        res[k] = doubleArray[i][j];
        ++k;
      }
    }
    return new DoubleMatrix1D(res);
  }

  //  private DoubleMatrix1D toVector(final DoubleMatrix2D matrix) {
  //    final double[] res = new double[matrix.getNumberOfElements()];
  //    final double[][] matrixData = matrix.getData();
  //    final int nRow = matrixData.length;
  //    int k = 0;
  //    for (int i = 0; i < nRow; ++i) {
  //      final int len = matrixData[i].length;
  //      for (int j = 0; j < len; ++j) {
  //        res[k] = matrixData[i][j];
  //        ++k;
  //      }
  //    }
  //    return new DoubleMatrix1D(res);
  //  }
}
