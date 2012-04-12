/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.BitSet;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.NonLinearParameterTransforms;
import com.opengamma.analytics.math.minimization.NullTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.UncoupledParameterTransforms;
import com.opengamma.analytics.math.rootfinding.VectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;

/**
 * 
 */
public class ShiftedLogNormalTailExtrapolationFitter {

  private static final ScalarFirstOrderDifferentiator DIFFERENTIATOR = new ScalarFirstOrderDifferentiator();
  private static final VectorRootFinder ROOTFINDER = new BroydenVectorRootFinder();
  private static final NonLinearParameterTransforms TRANSFORMS;

  static {
    final ParameterLimitsTransform a = new NullTransform();
    final ParameterLimitsTransform b = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    TRANSFORMS = new UncoupledParameterTransforms(new DoubleMatrix1D(0.0, 1.0), new ParameterLimitsTransform[] {a, b }, new BitSet());
  }

  public double[] fitTwoPrices(final double forward, final double[] strikes, final double[] prices, final double timeToExpiry, final boolean isCall) {

    double vol1 = BlackFormulaRepository.impliedVolatility(prices[0], forward, strikes[0], timeToExpiry, isCall);
    double vol2 = BlackFormulaRepository.impliedVolatility(prices[1], forward, strikes[1], timeToExpiry, isCall);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getPriceDifferenceFunc(forward, strikes, prices, timeToExpiry, isCall);
    return TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(func, TRANSFORMS.transform(new DoubleMatrix1D(0.0, (vol1 + vol2) / 2.0)))).getData();
  }

  public double[] fitTwoVolatilities(final double forward, final double[] strikes, final double[] vols, final double timeToExpiry) {

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getVolDifferenceFunc(forward, strikes, vols, timeToExpiry);
    return TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(func, TRANSFORMS.transform(new DoubleMatrix1D(0.0, (vols[0] + vols[1]) / 2.0)))).getData();
  }

  public double[] fitVolatilityAndGrad(final double forward, final double strike, final double vol, final double volGrad, final double timeToExpiry) {

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getVolGradDifferenceFunc(forward, strike, vol, volGrad, timeToExpiry);
    return TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(func, TRANSFORMS.transform(new DoubleMatrix1D(0.0, vol)))).getData();
  }

  public double[] fitVolatilityAndGrad(final double forward, final double strike, final Function1D<Double, Double> smile, final double timeToExpiry) {
    final double vol = smile.evaluate(strike);
    Function1D<Double, Double> smileGrad = DIFFERENTIATOR.differentiate(smile);
    final double dVol = smileGrad.evaluate(strike);
    return fitVolatilityAndGrad(forward, strike, vol, dVol, timeToExpiry);
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getPriceDifferenceFunc(final double forward, final double[] strike, final double[] prices, final double timeToExpiry, final boolean isCall) {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        DoubleMatrix1D y = TRANSFORMS.inverseTransform(x);
        double mu = y.getEntry(0);
        double theta = y.getEntry(1);

        double p1 = ShiftedLogNormalTailExtrapolation.price(forward, strike[0], timeToExpiry, isCall, mu, theta);
        double p2 = ShiftedLogNormalTailExtrapolation.price(forward, strike[1], timeToExpiry, isCall, mu, theta);
        return new DoubleMatrix1D((prices[0] - p1) / prices[0], (prices[1] - p2) / prices[1]);
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getVolDifferenceFunc(final double forward, final double[] strike, final double[] vols, final double timeToExpiry) {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        DoubleMatrix1D y = TRANSFORMS.inverseTransform(x);
        double mu = y.getEntry(0);
        double theta = y.getEntry(1);

        double v1 = ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, strike[0], timeToExpiry, mu, theta);
        double v2 = ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, strike[1], timeToExpiry, mu, theta);
        return new DoubleMatrix1D((vols[0] - v1), (vols[1] - v2));
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getVolGradDifferenceFunc(final double forward, final double strike, final double targetVol, final double targetDvol, final double expiry) {
    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        DoubleMatrix1D y = TRANSFORMS.inverseTransform(x);
        double mu = y.getEntry(0);
        double theta = y.getEntry(1);
        double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, strike, expiry, mu, theta);
        double dvol = ShiftedLogNormalTailExtrapolation.dVdK(forward, strike, expiry, mu, theta, vol);
        return new DoubleMatrix1D(vol - targetVol, forward * (dvol - targetDvol));
      }
    };
  }

}
