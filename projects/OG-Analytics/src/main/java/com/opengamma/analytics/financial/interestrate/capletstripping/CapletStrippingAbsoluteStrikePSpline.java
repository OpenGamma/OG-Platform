/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructure;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.BasisFunctionGenerator;
import com.opengamma.analytics.math.interpolation.PSplineFitter;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquareWithPenalty;

/**
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class CapletStrippingAbsoluteStrikePSpline extends CapletStrippingAbsoluteStrike {
  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();
  private static final BasisFunctionGenerator GEN = new BasisFunctionGenerator();
  private static final NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty();
  private static final int DIFFERENCE_ORDER = 2;
  private static final double LAMBDA = 100.0;

  private final int _nWeights;
  private final List<Function1D<Double, Double>> _bSplines;
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
  public CapletStrippingAbsoluteStrikePSpline(final List<CapFloor> caps, final YieldCurveBundle yieldCurves) {
    super(caps, yieldCurves);

    // This assigns a unique volatility to each underlying caplet. The resultant volatility curve will not be smooth (it is piecewise linear),
    // however any (higher order) function that hits all the caplet volatilities, will (by construction) also reprice exactly the market caps. The
    // secondary fitting of this smooth curve (for interpolation/extrapolation to seasoned/non-standard caplets) is linear, and thus will be very quick next
    // to the non-linear first step.
    final double[] t = getPricer().getCapletExpiries();
    final int n = t.length;
    _bSplines = GEN.generateSet(t[0], t[n - 1], n, 1);

    _nWeights = _bSplines.size();
    final PSplineFitter psf = new PSplineFitter();
    _penalty = (DoubleMatrix2D) MA.scale(psf.getPenaltyMatrix(_nWeights, DIFFERENCE_ORDER), LAMBDA);
    _volModel = new BasisSplineVolatilityTermStructureProvider(_bSplines);
  }

  //  public CapletStrippingAbsoluteStrikePSpline(List<CapFloor> caps, YieldCurveBundle yieldCurves, final int differenceOrder, final double lambda) {
  //    super(caps, yieldCurves);
  //
  //  }

  @Override
  public CapletStrippingSingleStrikeResult solveForPrices(final double[] capPrices) {
    final double err = 1.0;
    final double[] error = new double[getnCaps()];
    Arrays.fill(error, err);
    return solveForPrices(capPrices, error, true);
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForPrices(final double[] capPrices, final double[] errors, final boolean scaleByVega) {
    checkPrices(capPrices);
    checkErrors(errors);

    final MultiCapFloorPricer pricer = getPricer();
    final double[] capVols = pricer.impliedVols(capPrices);
    final LeastSquareResults lsRes = solveForPrice(capPrices, capVols, errors, scaleByVega);

    final VolatilityTermStructure volCurve = getVolCurve(lsRes.getFitParameters());
    final double[] mPrices = pricer.price(volCurve);

    // least-squares gives chi2 including the penalty, and is calculated via the price difference and vega we just want the fit error
    // TODO maybe the solver should provide this?
    final double chi2 = chiSqr(capPrices, mPrices, errors); // ignore the vega weighting here
    return new CapletStrippingSingleStrikeResult(chi2, lsRes.getFitParameters(), volCurve, new DoubleMatrix1D(mPrices));
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForVol(final double[] capVols) {
    final double err = 0.001; // 10bps
    final double[] error = new double[getnCaps()];
    Arrays.fill(error, err);
    return solveForVol(capVols, error, true);
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForVol(final double[] capVols, final double[] errors, final boolean solveViaPrice) {
    checkVols(capVols);
    checkErrors(errors);

    final MultiCapFloorPricer pricer = getPricer();

    LeastSquareResults lsRes;
    if (solveViaPrice) {
      final double[] capPrices = pricer.price(capVols);
      lsRes = solveForPrice(capPrices, capVols, errors, true);
    } else {
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> modelVolFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
        @SuppressWarnings("synthetic-access")
        @Override
        public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
          final double[] modelVols = pricer.impliedVols(_volModel.evaluate(x));
          return new DoubleMatrix1D(modelVols);
        }
      };
      lsRes = NLLSWP.solve(new DoubleMatrix1D(capVols), new DoubleMatrix1D(errors), modelVolFunc, new DoubleMatrix1D(_nWeights, capVols[capVols.length - 1]), _penalty);
    }

    final VolatilityTermStructure volCurve = getVolCurve(lsRes.getFitParameters());
    final double[] mVols = pricer.impliedVols(volCurve);

    // least-squares gives chi2 including the penalty, and is calculated via the price differecne and vega w we just want the fit error
    // TODO maybe the solver should provide this?
    final double chi2 = chiSqr(capVols, mVols, errors);
    return new CapletStrippingSingleStrikeResult(chi2, lsRes.getFitParameters(), volCurve, new DoubleMatrix1D(mVols));

  }

  private LeastSquareResults solveForPrice(final double[] capPrices, final double[] capVols, final double[] errors, final boolean scaleByVega) {

    final int n = capPrices.length;
    final MultiCapFloorPricer pricer = getPricer();

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

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> modelPriceFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double[] modelPrices = pricer.price(_volModel.evaluate(x));
        return new DoubleMatrix1D(modelPrices);
      }
    };

    DoubleMatrix1D sigma;
    if (scaleByVega) {
      final double[] capVega = pricer.vega(capVols);
      final double[] temp = Arrays.copyOf(errors, n);
      for (int i = 0; i < n; i++) {
        temp[i] *= capVega[i];
      }
      sigma = new DoubleMatrix1D(temp);
    } else {
      sigma = new DoubleMatrix1D(errors);
    }

    return NLLSWP.solve(new DoubleMatrix1D(capPrices), sigma, modelPriceFunc, new DoubleMatrix1D(_nWeights, capVols[n - 1]), _penalty, allowed);
  }

  public VolatilityTermStructure getVolCurve(final DoubleMatrix1D fittedValues) {
    return _volModel.evaluate(fittedValues);
  }

}
