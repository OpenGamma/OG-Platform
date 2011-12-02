/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.fourier.MartingaleCharacteristicExponent;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.minimization.UncoupledParameterTransforms;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.LeastSquareResultsWithTransform;

/**
 * 
 */
public class HestonFFTPriceFitter extends HestonFFTSmileFitter {

  private static final BlackPriceFunction BLACK_PRICE_FUNCTION = new BlackPriceFunction();

  /**
   * @param fixVol0 True if initial value of vol the same as mean reversion level
   */
  public HestonFFTPriceFitter(boolean fixVol0) {
    super(fixVol0);
  }

  @Override
  public LeastSquareResultsWithTransform getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] errors, final double[] initialFitParameters,
      final BitSet fixed) {
    testData(options, data, errors, initialFitParameters, fixed, getnParams());
    final int n = options.length;
    final double[] strikes = new double[n];
    final double[] prices = new double[n];
    final double maturity = options[0].getTimeToExpiry();
    final double forward = data[0].getForward();
    final double df = data[0].getDiscountFactor();
    final boolean isCall = options[0].isCall();

    for (int i = 0; i < n; i++) {

      strikes[i] = options[i].getStrike();
      prices[i] = BLACK_PRICE_FUNCTION.getPriceFunction(options[i]).evaluate(data[i]);
    }
    final UncoupledParameterTransforms transforms = new UncoupledParameterTransforms(new DoubleMatrix1D(initialFitParameters), getTransforms(), fixed);
    final double lowStrike = strikes[0];
    final double highStrike = strikes[n - 1];
    final double limitSigma = (data[0].getBlackVolatility() + data[n - 1].getBlackVolatility()) / 2.0;
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> hestonVols = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D fp) {
        final MartingaleCharacteristicExponent ce = getCharacteristicExponent(transforms, fp);
        final double[][] strikeNPrice = FFT_PRICER.price(forward, df, maturity, isCall, ce, lowStrike, highStrike, n, limitSigma, getAlpha(), getLimitTolerance());
        final int nStrikes = strikeNPrice.length;
        final double[] k = new double[nStrikes];
        final double[] price = new double[nStrikes];
        for (int i = 0; i < nStrikes; i++) {
          k[i] = strikeNPrice[i][0];
          price[i] = strikeNPrice[i][1];
        }
        final Interpolator1DDataBundle dataBundle = getInterpolator().getDataBundle(k, price);
        final int m = strikes.length;
        final double[] res = new double[m];
        for (int i = 0; i < m; i++) {
          res[i] = getInterpolator().interpolate(dataBundle, strikes[i]);
        }
        return new DoubleMatrix1D(res);
      }
    };
    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialFitParameters));
    final LeastSquareResults results = errors == null ? SOLVER.solve(new DoubleMatrix1D(prices), hestonVols, fp) : SOLVER.solve(new DoubleMatrix1D(prices), new DoubleMatrix1D(errors), hestonVols, fp);

    return new LeastSquareResultsWithTransform(results, transforms);
    //return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getFitParameters()), new DoubleMatrix2D(new double[getnParams()][getnParams()]));
  }

}
