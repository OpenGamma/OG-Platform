/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.List;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
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
public class CapletStrippingSingleStrikeDirect extends CapletStrippingAbsoluteStrike {

  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();
  private static final Interpolator1D DEFAULT_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty();
  private static final int DIFFERENCE_ORDER = 2;
  private static final double LAMBDA = 100.0;

  private final Interpolator1D _interpolator;
  private final DoubleMatrix2D _penalty;

  public CapletStrippingSingleStrikeDirect(List<CapFloor> caps, YieldCurveBundle yieldCurves) {
    super(caps, yieldCurves);

    PSplineFitter psf = new PSplineFitter();
    _penalty = (DoubleMatrix2D) MA.scale(psf.getPenaltyMatrix(getnCaplets(), DIFFERENCE_ORDER), LAMBDA);
    _interpolator = DEFAULT_INTERPOLATOR;
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForPrices(double[] capPrices) {
    final int n = getnCaps();
    ArgumentChecker.notEmpty(capPrices, "null cap prices");
    ArgumentChecker.isTrue(n == capPrices.length, "cap prices wrong length");

    MultiCapFloorPricer pricer = getPricer();
    double[] capVols = pricer.impliedVols(capPrices);
    double[] vega = pricer.vega(capVols);
    return solveForPrice(new DoubleMatrix1D(capPrices), new DoubleMatrix1D(vega), new DoubleMatrix1D(capVols));
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForPrices(double[] capPrices, double[] errors, boolean scaleByVega) {
    final int n = getnCaps();
    ArgumentChecker.notEmpty(capPrices, "null cap prices");
    ArgumentChecker.notEmpty(errors, "null cap prices");
    ArgumentChecker.isTrue(n == capPrices.length, "cap prices wrong length");
    ArgumentChecker.isTrue(n == errors.length, "errors wrong length");

    MultiCapFloorPricer pricer = getPricer();
    double[] capVols = pricer.impliedVols(capPrices);

    DoubleMatrix1D sigma = null;
    if (scaleByVega) {
      final double[] capVega = pricer.vega(capVols);
      for (int i = 0; i < n; i++) {
        capVega[i] *= errors[i];
      }
      sigma = new DoubleMatrix1D(capVega);
    } else {
      sigma = new DoubleMatrix1D(errors);
    }

    return solveForPrice(new DoubleMatrix1D(capPrices), sigma, new DoubleMatrix1D(capVols));
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForVol(final double[] capVols) {
    final int n = getnCaps();
    ArgumentChecker.notEmpty(capVols, "null cap vols");
    ArgumentChecker.isTrue(n == capVols.length, "cap vols wrong length");

    MultiCapFloorPricer pricer = getPricer();
    final double[] capPrices = pricer.price(capVols);
    final double[] vega = pricer.vega(capVols);
    return solveForPrice(new DoubleMatrix1D(capPrices), new DoubleMatrix1D(vega), new DoubleMatrix1D(capVols));
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForVol(final double[] capVols, double[] errors, boolean solveByPrice) {
    MultiCapFloorPricer pricer = getPricer();
    final int n = getnCaps();
    if (solveByPrice) {
      double[] capPrices = pricer.price(capVols);
      final double[] capVega = pricer.vega(capVols);
      for (int i = 0; i < n; i++) {
        capVega[i] *= errors[i];
      }
      DoubleMatrix1D sigma = new DoubleMatrix1D(capVega);
      return solveForPrice(new DoubleMatrix1D(capPrices), sigma, new DoubleMatrix1D(capVols));
    }
    return solveForVol(new DoubleMatrix1D(capVols), new DoubleMatrix1D(errors), new DoubleMatrix1D(capVols));
  }

  protected CapletStrippingSingleStrikeResult solveForPrice(final DoubleMatrix1D capPrices, final DoubleMatrix1D sigma, DoubleMatrix1D start) {
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> modelPriceFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        double[] modelPrices = getPricer().priceFromCapletVols(x.getData());
        return new DoubleMatrix1D(modelPrices);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> modelPriceJac = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
        return getPricer().vegaFromCapletVols(x.getData());
      }
    };

    LeastSquareResults lsRes = NLLSWP.solve(capPrices, sigma, modelPriceFunc, modelPriceJac, start, _penalty);

    double sum = 0;
    DoubleMatrix1D mPrices = modelPriceFunc.evaluate(lsRes.getFitParameters());
    final int n = getnCaps();
    for (int i = 0; i < n; i++) {
      sum += FunctionUtils.square((capPrices.getEntry(i) - mPrices.getEntry(i)) / sigma.getEntry(i));
    }
    VolatilityCurve vc = new VolatilityCurve(InterpolatedDoublesCurve.from(getPricer().getCapletExpiries(), lsRes.getFitParameters().getData(), _interpolator));

    return new CapletStrippingSingleStrikeResult(sum, lsRes.getFitParameters(), vc, mPrices);
  }

  protected CapletStrippingSingleStrikeResult solveForVol(final DoubleMatrix1D capVols, final DoubleMatrix1D sigma, DoubleMatrix1D start) {
    final int n = getnCaps();
    final int m = getnCaplets();
    final MultiCapFloorPricer pricer = getPricer();

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> modelVolFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        double[] modelPrices = pricer.priceFromCapletVols(x.getData());
        double[] modelVols = pricer.impliedVols(modelPrices);
        return new DoubleMatrix1D(modelVols);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> modelVolJac = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
        double[] modelPrices = pricer.priceFromCapletVols(x.getData());
        double[] modelVols = pricer.impliedVols(modelPrices);
        double[] vega = pricer.vega(modelVols);

        DoubleMatrix2D vegaMatrix = pricer.vegaFromCapletVols(x.getData());
        // scale by inverse of cap vega
        for (int i = 0; i < n; i++) {
          for (int j = 0; j < m; j++) {
            if (vegaMatrix.getData()[i][j] != 0.0) {
              vegaMatrix.getData()[i][j] /= vega[i];
            }
          }
        }
        return vegaMatrix;
      }
    };

    LeastSquareResults lsRes = NLLSWP.solve(capVols, sigma, modelVolFunc, modelVolJac, start, _penalty);

    double sum = 0;
    DoubleMatrix1D mVols = modelVolFunc.evaluate(lsRes.getFitParameters());

    for (int i = 0; i < n; i++) {
      sum += FunctionUtils.square((capVols.getEntry(i) - mVols.getEntry(i)) / sigma.getEntry(i));
    }
    VolatilityCurve vc = new VolatilityCurve(InterpolatedDoublesCurve.from(pricer.getCapletExpiries(), lsRes.getFitParameters().getData(), _interpolator));

    return new CapletStrippingSingleStrikeResult(sum, lsRes.getFitParameters(), vc, mVols);
  }

}
