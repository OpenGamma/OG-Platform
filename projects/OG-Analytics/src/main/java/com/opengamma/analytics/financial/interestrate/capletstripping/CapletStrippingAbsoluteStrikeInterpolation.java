/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructure;
import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructureProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * Caplet stripping for a set of caps with the <b>same</b> (absolute) strike. The model assumes that all the forwards (that underly the caplets) 'see' a volatility
 * that is a function of their initial time-to-expiry only (i.e. this model is not time homogeneous - the 2y forward in one years time, with have a different volatility
 * that the current 1y forward.). This volatility curve is modelled as an interpolated curve with suitably chosen knots (the number of knots equals the number of caps).
 * Provided the market cap prices are arbitrage free (i.e. the prices of two caps do not imply the price of a forward starting cap that is below its intrinsic value)
 * it is always possible to root find for the ordinates of the knots such that every cap is exactly repriced by the model. In this way the volatility (and hence the
 * price) of every caplet (of the fixed strike) can be inferred from the curve - this result will be highly dependent on the choice of interpolator and knot positions.
 */
public class CapletStrippingAbsoluteStrikeInterpolation extends CapletStrippingAbsoluteStrike {
  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();
  // TODO option on root finder
  private static final NewtonVectorRootFinder ROOTFINDER = new NewtonDefaultVectorRootFinder();  // new BroydenVectorRootFinder();

  private static final String DEFAULT_INTERPOLATOR = Interpolator1DFactory.DOUBLE_QUADRATIC;
  private static final String DEFAULT_EXTRAPOLATOR = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private static final ParameterLimitsTransform TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);

  private final VolatilityTermStructureProvider<DoubleMatrix1D> _volModel;
  private final Interpolator1D _interpolator;
  private final double[] _knots;

  /**
   * caplet stripping for a set of caps with the <b>same</b> (absolute) strike. The interpolator is double-quadratic with a linear extrapolator and a
   * transformation so it remains everywhere positive. For co-starting caps the knots are the first caplet expiry, then the end time of all the caps expect the
   * final one (i.e. all but the first unique caplets in the longest cap see volatilities from the extrapolated part of the curve). If the caps are not co-starting
   * it is not possible to auto-generate the knots and these should be supplied.
   * @param caps List of caps with identical strikes
   * @param curves The yield curves (should include the discount and relevant Ibor projection curve)
   */
  public CapletStrippingAbsoluteStrikeInterpolation(final List<CapFloor> caps, final MulticurveProviderInterface curves) {

    super(caps, curves);
    final CombinedInterpolatorExtrapolator baseInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(DEFAULT_INTERPOLATOR, DEFAULT_EXTRAPOLATOR);
    _interpolator = new TransformedInterpolator1D(baseInterpolator, TRANSFORM);
    _knots = getKnots();
    // actually need direct access to interpolator
    _volModel = new InterpolatedVolatilityTermStructureProvider(_knots, _interpolator);
  }

  /**
   * caplet stripping for a set of caps with the <b>same</b> (absolute) strike.   The interpolator is double-quadratic with a linear extrapolator and a
   * transformation so it remains everywhere positive.
   * @param caps List of caps with identical strikes
   * @param curves The yield curves (should include the discount and relevant Ibor projection curve)
   * @param knots knot positions (must equal the number of caps)
   */
  public CapletStrippingAbsoluteStrikeInterpolation(final List<CapFloor> caps, final MulticurveProviderInterface curves, final double[] knots) {
    this(caps, curves, CombinedInterpolatorExtrapolatorFactory.getInterpolator(DEFAULT_INTERPOLATOR, DEFAULT_EXTRAPOLATOR), knots);
  }

  /**
   * caplet stripping for a set of caps with the <b>same</b> (absolute) strike.
   * @param caps List of caps with identical strikes
   * @param curves List of caps with identical strikes
   * @param interpolator the combined interpolator/extrapolator used to define the vol curve. <b>It is recommended</b> that a strictly positive interpolator
   *  is used
   * @param knots  knot positions (must equal the number of caps)
   */
  public CapletStrippingAbsoluteStrikeInterpolation(final List<CapFloor> caps, final MulticurveProviderInterface curves, final CombinedInterpolatorExtrapolator interpolator, final double[] knots) {
    super(caps, curves);
    ArgumentChecker.notNull(interpolator, "null interpolator");
    ArgumentChecker.notEmpty(knots, "null knots");
    ArgumentChecker.isTrue(getnCaps() == knots.length, "must have {} knots", getnCaps());

    _volModel = new InterpolatedVolatilityTermStructureProvider(knots, interpolator);
    _interpolator = interpolator;
    _knots = knots;
  }

  /**
   * Stripe from market cap prices.
   * @param capPrices The cap prices (in the same order that the caps we given)
   * @return vector of ordinates of the knots of the interpolated vol curve. Call getVolCurve with this result
   * @see #solveForVol
   */
  @Override
  public CapletStrippingSingleStrikeResult solveForPrices(final double[] capPrices) {
    checkPrices(capPrices);

    final MultiCapFloorPricer pricer = getPricer();
    final double[] capVols = pricer.impliedVols(capPrices);
    final DoubleMatrix1D fitValues = solveForPrice(capPrices, new DoubleMatrix1D(capVols));
    return new CapletStrippingSingleStrikeResult(0.0, fitValues, _volModel.evaluate(fitValues), new DoubleMatrix1D(capPrices));
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForPrices(final double[] capPrices, final double[] errors, final boolean scaleByVega) {
    return solveForPrices(capPrices);
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForVol(final double[] capVols) {
    checkVols(capVols);
    final MultiCapFloorPricer pricer = getPricer();
    final double[] capPrices = pricer.price(capVols);
    final DoubleMatrix1D fitValues = solveForPrice(capPrices, new DoubleMatrix1D(capVols));

    return new CapletStrippingSingleStrikeResult(0.0, fitValues, _volModel.evaluate(fitValues), new DoubleMatrix1D(capVols));
  }

  @Override
  public CapletStrippingSingleStrikeResult solveForVol(final double[] capVols, final double[] errors, final boolean solveViaPrice) {
    if (solveViaPrice) {
      return solveForVol(capVols);
    }
    final DoubleMatrix1D fitValues = solveForVolDirect(capVols);
    return new CapletStrippingSingleStrikeResult(0.0, fitValues, _volModel.evaluate(fitValues), new DoubleMatrix1D(capVols));
  }

  /**
   * Stripe from market cap implied volatilities. <b>Note:</b> this will be considerably slower than using the cap prices
   * @param capVols The cap implied volatilities (in the same order that the caps we given)
   * @return vector of ordinates of the knots of the interpolated vol curve. Call getVolCurve with this result
   * @see #solveForPrice
   * @see #solveForVol
   */
  protected DoubleMatrix1D solveForVolDirect(final double[] capVols) {
    // TODO keep a version of this for testing, but the main solveForVol should get prices then call solveForPrice (since this roots finds, the answer should
    // not change)
    ArgumentChecker.notEmpty(capVols, "null vols");
    final int nCaps = getnCaps();
    ArgumentChecker.isTrue(capVols.length == nCaps, "wrong number of cap vols");
    final MultiCapFloorPricer pricer = getPricer();

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> volDiffFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final VolatilityTermStructure vol = _volModel.evaluate(x);
        final double[] modelVols = pricer.impliedVols(vol);
        final double[] res = new double[nCaps];
        for (int i = 0; i < nCaps; i++) {
          res[i] = (modelVols[i] - capVols[i]);
        }
        return new DoubleMatrix1D(res);
      }
    };

    final DoubleMatrix1D root = ROOTFINDER.getRoot(volDiffFunc, new DoubleMatrix1D(capVols));
    return root;
  }

  private DoubleMatrix1D solveForPrice(final double[] capPrices, final DoubleMatrix1D start) {

    final double minPrice = 1e-13;

    final MultiCapFloorPricer pricer = getPricer();

    final double[] capletExp = pricer.getCapletExpiries();
    final int nCaps = getnCaps();
    final int nCaplets = getnCaplets();

    final double[] scale = new double[nCaps];
    for (int i = 0; i < nCaps; i++) {
      scale[i] = 1.0 / (Math.max(minPrice, capPrices[i]));
    }
    final DoubleMatrix2D mScale = DoubleMatrixUtils.getTwoDimensionalDiagonalMatrix(scale);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> weightedPriceFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {

        final VolatilityTermStructure vol = _volModel.evaluate(x);
        final double[] modelPrices = pricer.price(vol);

        final double[] res = new double[nCaps];
        for (int i = 0; i < nCaps; i++) {
          res[i] = (modelPrices[i] - capPrices[i]) * scale[i];
        }
        return new DoubleMatrix1D(res);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> priceJac = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {

        final Interpolator1DDataBundle db = _interpolator.getDataBundle(_knots, x.getData());

        final double[] capletVols = new double[nCaplets];
        final double[][] sense = new double[nCaplets][];
        for (int i = 0; i < nCaplets; i++) {
          capletVols[i] = _interpolator.interpolate(db, capletExp[i]);
          sense[i] = _interpolator.getNodeSensitivitiesForValue(db, capletExp[i]);
        }
        final DoubleMatrix2D curveSensitivity = new DoubleMatrix2D(sense);
        final DoubleMatrix2D vega = pricer.vegaFromCapletVols(capletVols);
        final DoubleMatrix2D jac = (DoubleMatrix2D) MA.multiply(vega, curveSensitivity);
        // must now scale
        return (DoubleMatrix2D) MA.multiply(mScale, jac);
      }
    };

    return ROOTFINDER.getRoot(weightedPriceFunc, priceJac, start);
  }

  private double[] getKnots() {

    final double[] s = getCapStartTimes();
    final double[] e = getCapEndTimes();
    final int n = s.length;
    final double[] temp = new double[2 * n];
    System.arraycopy(s, 0, temp, 0, n);
    System.arraycopy(e, 0, temp, n, n);
    final double[] times = FunctionUtils.unique(temp);
    ArgumentChecker.isTrue(times.length > n, "Please check caps - one or more not unique");

    // there will be at least one more time than there are caps. For co-starting caps, our experience is that it is better to use this start time, but
    // NOT the last end time to form the knots.
    final double[] knots = new double[n];
    if (times.length == (n + 1)) {
      System.arraycopy(times, 0, knots, 0, n);
    } else {
      // TODO some logic to auto generate knots
      throw new NotImplementedException("Please supply knots");
    }
    return knots;
  }

}
