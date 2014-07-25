/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.InterpolatedVolatilityTermStructureProvider;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.MultiCapFloorPricer;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.VolatilitySurfaceProvider;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
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

/**
 * 
 */
public class CapletStrippingSingleStrikeDirect extends CapletStrippingAbsoluteStrike {

  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();
  private static final Interpolator1D DEFAULT_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty();
  private static final int DIFFERENCE_ORDER = 2;
  private static final double LAMBDA = 1000;

  private final Interpolator1D _interpolator;
  private final DoubleMatrix2D _penalty;
  private final VolatilitySurfaceProvider _volSurfaceProvider;

  public CapletStrippingSingleStrikeDirect(final List<CapFloor> caps, final MulticurveProviderInterface curves) {
    super(caps, curves);

    final PSplineFitter psf = new PSplineFitter();
    _penalty = (DoubleMatrix2D) MA.scale(psf.getPenaltyMatrix(getnCaplets(), DIFFERENCE_ORDER), LAMBDA);
    _interpolator = DEFAULT_INTERPOLATOR;
    _volSurfaceProvider = new InterpolatedVolatilityTermStructureProvider(getPricer().getCapletExpiries(), _interpolator);
    // _altCapletStripper = new CapletStrippingAbsoluteStrikeInterpolation(caps, yieldCurves);
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForPrices(final double[] capPrices) {
    checkPrices(capPrices);

    final MultiCapFloorPricer pricer = getPricer();
    final double[] capVols = pricer.impliedVols(capPrices);
    final double[] vega = pricer.vega(capVols);
    final double[] start = new double[getnCaplets()];
    Arrays.fill(start, capVols[capVols.length - 1]);

    final LeastSquareResults lsRes = solveForPrice(new DoubleMatrix1D(capPrices), new DoubleMatrix1D(vega), new DoubleMatrix1D(start));

    final double[] mPrices = pricer.priceFromCapletVols(lsRes.getFitParameters().getData());
    final double chiSqr = chiSqr(capPrices, mPrices);
    final VolatilitySurface vc = getVolSurface(lsRes.getFitParameters().getData());
    return new CapletStrippingSingleStrikeResult(chiSqr, lsRes.getFitParameters(), vc, new DoubleMatrix1D(mPrices));
  }

  /**
   * Produce a continuous volatility surface from the discrete caplet vols 
   * @param capletVols the caplet vols
   * @return a volatility surface
   */
  private VolatilitySurface getVolSurface(final double[] capletVols) {
    return _volSurfaceProvider.getVolSurface(new DoubleMatrix1D(capletVols));
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForPrices(final double[] capPrices, final double[] errors, final boolean scaleByVega) {
    checkPrices(capPrices);
    checkErrors(errors);

    final MultiCapFloorPricer pricer = getPricer();
    final double[] capVols = pricer.impliedVols(capPrices);

    DoubleMatrix1D sigma = null;
    if (scaleByVega) {
      final double[] capVega = pricer.vega(capVols);
      final int n = getnCaps();
      for (int i = 0; i < n; i++) {
        capVega[i] *= errors[i];
      }
      sigma = new DoubleMatrix1D(capVega);
    } else {
      sigma = new DoubleMatrix1D(errors);
    }

    final double[] start = new double[getnCaplets()];
    Arrays.fill(start, capVols[capVols.length - 1]);
    final LeastSquareResults lsRes = solveForPrice(new DoubleMatrix1D(capPrices), sigma, new DoubleMatrix1D(start));

    final double[] mPrices = pricer.priceFromCapletVols(lsRes.getFitParameters().getData());
    final double chiSqr = chiSqr(capPrices, mPrices, errors);
    final VolatilitySurface vc = getVolSurface(lsRes.getFitParameters().getData());
    return new CapletStrippingSingleStrikeResult(chiSqr, lsRes.getFitParameters(), vc, new DoubleMatrix1D(mPrices));
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForVol(final double[] capVols) {
    final double err = 0.001; // 10bps
    final double[] errors = new double[getnCaps()];
    Arrays.fill(errors, err);
    return solveForVol(capVols, errors, true);
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForVol(final double[] capVols, final double[] errors, final boolean solveByPrice) {
    checkVols(capVols);
    checkErrors(errors);

    final MultiCapFloorPricer pricer = getPricer();

    final double[] start = new double[getnCaplets()];
    Arrays.fill(start, capVols[capVols.length - 1]);

    // we are using CapletStrippingAbsoluteStrikeInterpolation to give use a starting position
    // CapletStrippingSingleStrikeResult altRes = _altCapletStripper.solveForVol(capVols);
    // VolatilityTermStructure altVC = altRes.getVolatilityCurve();
    // double[] t = pricer.getCapletExpiries();
    // final int nCaplets = getnCaplets();
    // for (int i = 0; i < nCaplets; i++) {
    // start[i] = altVC.getVolatility(t[i]);
    // }

    final int n = getnCaps();
    LeastSquareResults lsRes;
    if (solveByPrice) {
      final double[] capPrices = pricer.price(capVols);
      final double[] capVega = pricer.vega(capVols);
      for (int i = 0; i < n; i++) {
        capVega[i] *= errors[i];
      }
      final DoubleMatrix1D sigma = new DoubleMatrix1D(capVega);
      lsRes = solveForPrice(new DoubleMatrix1D(capPrices), sigma, new DoubleMatrix1D(start));
    } else {
      lsRes = solveForVol(new DoubleMatrix1D(capVols), new DoubleMatrix1D(errors), new DoubleMatrix1D(start));
    }

    final double[] mVols = pricer.impliedVols(pricer.priceFromCapletVols(lsRes.getFitParameters().getData()));
    final double chiSqr = chiSqr(capVols, mVols, errors);
    final VolatilitySurface vc = getVolSurface(lsRes.getFitParameters().getData());
    return new CapletStrippingSingleStrikeResult(chiSqr, lsRes.getFitParameters(), vc, new DoubleMatrix1D(mVols));
  }

  private LeastSquareResults solveForPrice(final DoubleMatrix1D capPrices, final DoubleMatrix1D sigma, final DoubleMatrix1D start) {

    final Function1D<DoubleMatrix1D, Boolean> allowed = new Function1D<DoubleMatrix1D, Boolean>() {
      @Override
      public Boolean evaluate(final DoubleMatrix1D x) {
        final double[] temp = x.getData();
        final int n = temp.length;
        for (int i = 0; i < n; i++) {
          if (temp[i] < 0) {
            return false;
          }
        }
        return true;
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> modelPriceFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double[] modelPrices = getPricer().priceFromCapletVols(x.getData());
        return new DoubleMatrix1D(modelPrices);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> modelPriceJac = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        return getPricer().vegaFromCapletVols(x.getData());
      }
    };

    return NLLSWP.solve(capPrices, sigma, modelPriceFunc, modelPriceJac, start, _penalty, allowed);
  }

  private LeastSquareResults solveForVol(final DoubleMatrix1D capVols, final DoubleMatrix1D sigma, final DoubleMatrix1D start) {
    final int n = getnCaps();
    final int m = getnCaplets();
    final MultiCapFloorPricer pricer = getPricer();

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> modelVolFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double[] modelPrices = pricer.priceFromCapletVols(x.getData());
        final double[] modelVols = pricer.impliedVols(modelPrices);
        return new DoubleMatrix1D(modelVols);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> modelVolJac = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        final double[] modelPrices = pricer.priceFromCapletVols(x.getData());
        final double[] modelVols = pricer.impliedVols(modelPrices);
        final double[] vega = pricer.vega(modelVols);

        final DoubleMatrix2D vegaMatrix = pricer.vegaFromCapletVols(x.getData());
        // scale by inverse of cap vega
        for (int i = 0; i < n; i++) {
          for (int j = 0; j < m; j++) {
            if (vegaMatrix.getData()[i][j] != 0.0) {
              vegaMatrix.getData()[i][j] /= vega[i]; //CSIGNORE
            }
          }
        }
        return vegaMatrix;
      }
    };

    return NLLSWP.solve(capVols, sigma, modelVolFunc, modelVolJac, start, _penalty);
  }

}
