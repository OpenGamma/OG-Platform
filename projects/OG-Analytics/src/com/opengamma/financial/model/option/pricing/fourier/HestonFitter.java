/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import java.util.BitSet;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedVolFormula;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DCubicSplineDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 */
public class HestonFitter {

  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare();
  private static final FFTPricer FFT_PRICER = new FFTPricer();
  private static final Interpolator1D<Interpolator1DCubicSplineDataBundle> INTERPOLATOR = Interpolator1DFactory.NATURAL_CUBIC_SPLINE_INSTANCE;

  public LeastSquareResults solve(final double forward, final double maturity, final double[] strikes, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitSet fixed) {

    double moneynessRange = Math.max(-Math.log(strikes[0] / forward), Math.log(strikes[strikes.length - 1] / forward));
    final double maxDeltaMoneyness = moneynessRange / strikes.length;
    final int nStrikes = 5 * strikes.length;
    final double alpha = -0.5;
    final double tol = 1e-10;
    final double limitSigma = (blackVols[0] + blackVols[blackVols.length - 1]) / 2.0;

    Function1D<DoubleMatrix1D, DoubleMatrix1D> hestonVols = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        final double kappa = x.getEntry(0);
        final double theta = x.getEntry(1);
        final double vol0 = x.getEntry(2);
        final double omega = x.getEntry(3);
        final double rho = x.getEntry(4);
        CharacteristicExponent ce = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho, maturity);
        double[][] strikeNPrice = FFT_PRICER.price(forward, 1.0, true, ce, nStrikes, maxDeltaMoneyness, alpha, tol, limitSigma);
        double[] k = new double[nStrikes];
        double[] vol = new double[nStrikes];
        for (int i = 0; i < nStrikes; i++) {
          k[i] = strikeNPrice[i][0];
          try {
            vol[i] = BlackImpliedVolFormula.impliedVol(strikeNPrice[i][1], forward, k[i], 1.0, maturity, true);
          }
          catch (Exception e) {
            vol[i] = 0.0;
          }
        }
        Interpolator1DCubicSplineDataBundle dataBundle = INTERPOLATOR.getDataBundle(k, vol);
        int n = strikes.length;
        double[] res = new double[n];
        for (int i = 0; i < n; i++) {
          res[i] = INTERPOLATOR.interpolate(dataBundle, strikes[i]);
        }
        return new DoubleMatrix1D(res);
      }
    };

    return SOLVER.solve(new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), hestonVols, new DoubleMatrix1D(initialValues));
  }
}
