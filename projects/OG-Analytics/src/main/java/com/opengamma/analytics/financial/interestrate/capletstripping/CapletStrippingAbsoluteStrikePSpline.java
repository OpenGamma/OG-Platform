/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.financial.model.volatility.VolatilityModelProvider;
import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructure;
import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructureProvider;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.BasisFunctionAggregation;
import com.opengamma.analytics.math.interpolation.BasisFunctionGenerator;
import com.opengamma.analytics.math.interpolation.PSplineFitter;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquareWithPenalty;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CapletStrippingAbsoluteStrikePSpline {
  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();
  private static final BasisFunctionGenerator GEN = new BasisFunctionGenerator();
  private static final NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty();
  private static final int DIFFERENCE_ORDER = 2;
  private static final double LAMBDA = 100.0;

  private final MultiCapFloorPricer _pricer;
  private final int _nCaps;
  private final int _nWeights;
  // private final double[] _capStartTimes;
  // private final double[] _capEndTimes;

  private final List<Function1D<Double, Double>> _bSplines;
  // B_SPLINES = GEN.generateSet(0.0, CAP_EXPIRIES[CAP_EXPIRIES.length - 1], N_KNOTS, DEGREE);
  private final BasisSplineVolatilityTermStructureProvider _volModel;
  private final DoubleMatrix2D _penalty;

  /**
   * caplet stripping for a set of caps with the <b>same</b> (absolute) strike. The interpolator is double-quadratic with a linear extrapolator and a 
   * transformation so it remains everywhere positive. For co-starting caps the knots are the first caplet expiry, then the end time of all the caps expect the
   * final one (i.e. all but the first unique caplets in the longest cap see volatilities from the extrapolated part of the curve). If the caps are not co-starting
   * it is not possible to auto-generate the knots and these should be supplied.
   * @param caps List of caps with identical strikes 
   * @param yieldCurves The yield curves (should include the discount and relevant Ibor projection curve)
   */
  public CapletStrippingAbsoluteStrikePSpline(List<CapFloor> caps, YieldCurveBundle yieldCurves) {
    ArgumentChecker.noNulls(caps, "caps null");
    ArgumentChecker.notNull(yieldCurves, "null yield curves");

    _nCaps = caps.size();
    _pricer = new MultiCapFloorPricer(caps, yieldCurves);

    double[][] startAndEndTimes = getTimes(caps);
    double[] temp = new double[2 * _nCaps];
    System.arraycopy(startAndEndTimes[0], 0, temp, 0, _nCaps);
    System.arraycopy(startAndEndTimes[1], 0, temp, _nCaps, _nCaps);
    double[] times = FunctionUtils.unique(temp);
    ArgumentChecker.isTrue(times.length > _nCaps, "Please check caps - one or more not unique");

    final int nCaplets = _pricer.getTotalNumberOfCaplets();
    final double[] expTimes = _pricer.getCapletExpiries();

    // This assigns a unique volatility to each underlying caplet. The resultant volatility curve will not be smooth (it is piecewise linear),
    // however any (higher order) function that hits all the caplet volatilities, will (by construction) also reprice exactly the market caps. The
    // secondary fitting of this smooth curve (for interpolation/extrapolation to seasoned/non-standard caplets) is linear, and thus will be very quick next
    // to the non-linear first step.
    _bSplines = GEN.generateSet(expTimes[0], expTimes[nCaplets - 1], nCaplets, 1);

    _nWeights = _bSplines.size();
    PSplineFitter psf = new PSplineFitter();
    _penalty = (DoubleMatrix2D) MA.scale(psf.getPenaltyMatrix(_nWeights, DIFFERENCE_ORDER), LAMBDA);
    _volModel = new BasisSplineVolatilityTermStructureProvider(_bSplines);
  }

  public LeastSquareResults solveForVolViaPrice(final double[] capVols) {
    final double[] capPrices = _pricer.price(capVols);
    final double[] error = new double[_nCaps];
    Arrays.fill(error, 1.0);
    return solveForPrice(capPrices, capVols, error, true);
  }

  public LeastSquareResults solveForVolViaPrice2(final double[] capVols) {
    final double[] capPrices = _pricer.price(capVols);
    final double[] error = new double[_nCaps];
    Arrays.fill(error, 1.0);
    return solveForPrice2(capPrices, capVols, error, true);
  }

  private LeastSquareResults solveForPrice(final double[] capPrices, final double[] capVols, final double[] errors, final boolean scaleByVega) {

    final int n = capPrices.length;
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> modelPriceFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        VolatilityTermStructure vol = _volModel.evaluate(x);
        double[] modelPrices = _pricer.price(vol);
        return new DoubleMatrix1D(modelPrices);
      }
    };

    DoubleMatrix1D sigma;
    if (scaleByVega) {
      final double[] capVega = _pricer.vega(capVols);
      double[] temp = Arrays.copyOf(errors, n);
      for (int i = 0; i < n; i++) {
        temp[i] *= capVega[i];
      }
      sigma = new DoubleMatrix1D(temp);
    } else {
      sigma = new DoubleMatrix1D(errors);
    }

    return NLLSWP.solve(new DoubleMatrix1D(capPrices), sigma, modelPriceFunc, new DoubleMatrix1D(_nWeights, capVols[n - 1]), _penalty);
  }

  private LeastSquareResults solveForPrice2(final double[] capPrices, final double[] capVols, final double[] errors, final boolean scaleByVega) {

    final int n = capPrices.length;

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> modelPriceFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        double[] modelPrices = _pricer.priceFromCapletVols(x.getData());
        return new DoubleMatrix1D(modelPrices);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> modelPriceJac = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
        return _pricer.vegaFromCapletVols(x.getData());
      }
    };

    DoubleMatrix1D sigma;
    if (scaleByVega) {
      final double[] capVega = _pricer.vega(capVols);
      double[] temp = Arrays.copyOf(errors, n);
      for (int i = 0; i < n; i++) {
        temp[i] *= capVega[i];
      }
      sigma = new DoubleMatrix1D(temp);
    } else {
      sigma = new DoubleMatrix1D(errors);
    }

    return NLLSWP.solve(new DoubleMatrix1D(capPrices), sigma, modelPriceFunc, modelPriceJac, new DoubleMatrix1D(_nWeights, capVols[n - 1]), _penalty);
  }

  public LeastSquareResults solveForVol(final double[] capVols) {
    // TODO keep a version of this for testing, but the main solveForVol should get prices then call solveForPrice (since this roots finds, the answer should
    // not change)
    ArgumentChecker.notEmpty(capVols, "null vols");
    final int n = capVols.length;
    ArgumentChecker.isTrue(n == _nCaps, "wrong number of cap vols");

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> modelVolFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        VolatilityTermStructure vol = _volModel.evaluate(x);
        double[] modelVols = _pricer.impliedVols(vol);
        return new DoubleMatrix1D(modelVols);
      }
    };

    LeastSquareResults lsRes = NLLSWP.solve(new DoubleMatrix1D(capVols), modelVolFunc, new DoubleMatrix1D(_nWeights, capVols[n - 1]), _penalty);
    return lsRes;
  }

  public VolatilityTermStructure getVolCurve(DoubleMatrix1D fittedValues) {
    return _volModel.evaluate(fittedValues);
  }

  // TODO pull out
  private static VolatilityModelProvider getVolModel(final List<Function1D<Double, Double>> bSplines) {
    return new VolatilityModelProvider() {

      @Override
      public VolatilityModel1D evaluate(DoubleMatrix1D x) {
        final Function1D<Double, Double> fittedVol = new BasisFunctionAggregation<>(bSplines, x.getData());
        return new VolatilityModel1D() {

          @Override
          public Double getVolatility(double[] t) {
            return fittedVol.evaluate(t[2]);
          }

          @Override
          public double getVolatility(SimpleOptionData option) {
            return fittedVol.evaluate(option.getTimeToExpiry());
          }

          @Override
          public double getVolatility(double forward, double strike, double timeToExpiry) {
            return fittedVol.evaluate(timeToExpiry);
          }
        };
      }
    };
  }

  // TODO this is copied from the interpolation version - should be in one place
  private double[][] getTimes(final List<CapFloor> caps) {
    final int n = caps.size();

    final double[] capStartTimes = new double[n];
    final double[] capEndTimes = new double[n];
    final Iterator<CapFloor> iter = caps.iterator();
    CapFloor cap = iter.next();
    final double strike = cap.getStrike();
    capStartTimes[0] = cap.getStartTime();
    capEndTimes[0] = cap.getEndTime();
    int ii = 1;
    while (iter.hasNext()) {
      cap = iter.next();
      ArgumentChecker.isTrue(cap.getStrike() == strike, "All caps are requied to have the same strike");
      capStartTimes[ii] = cap.getStartTime();
      capEndTimes[ii] = cap.getEndTime();
      ii++;
    }
    return new double[][] {capStartTimes, capEndTimes};
  }

}
