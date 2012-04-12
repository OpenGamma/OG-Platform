/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.MathException;
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
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ShiftedLogNormalTailExtrapolationFitter {

  private static final Logger LOG = LoggerFactory.getLogger(ShiftedLogNormalTailExtrapolationFitter.class);
  private static final ScalarFirstOrderDifferentiator DIFFERENTIATOR = new ScalarFirstOrderDifferentiator();
  private static final VectorRootFinder ROOTFINDER = new BroydenVectorRootFinder();
  private static final NonLinearParameterTransforms TRANSFORMS;

  static {
    final ParameterLimitsTransform a = new NullTransform();
    final ParameterLimitsTransform b = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    TRANSFORMS = new UncoupledParameterTransforms(new DoubleMatrix1D(0.0, 1.0), new ParameterLimitsTransform[] {a, b }, new BitSet());
  }

  public double[] fitTwoPrices(final double forward, final double[] strikes, final double[] prices, final double timeToExpiry, final boolean isCall) {
    ArgumentChecker.isTrue(strikes[0] < strikes[1], "strikes must be in assending order");
    ArgumentChecker.isTrue(strikes[1] < forward || strikes[0] > forward, "strikes cannot be either side of forward");
    if (isCall) {
      ArgumentChecker.isTrue(prices[0] > prices[1], "Call prices are not decreasing with strike. Either the input data is wrong or there is an arbitrage");
    } else {
      ArgumentChecker.isTrue(prices[0] < prices[1], "Put prices are not incresing with strike. Either the input data is wrong or there is an arbitrage");
    }

    double vol1 = BlackFormulaRepository.impliedVolatility(prices[0], forward, strikes[0], timeToExpiry, isCall);
    double vol2 = BlackFormulaRepository.impliedVolatility(prices[1], forward, strikes[1], timeToExpiry, isCall);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getPriceDifferenceFunc(forward, strikes, prices, timeToExpiry, isCall);
    return TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(func, TRANSFORMS.transform(new DoubleMatrix1D(0.0, (vol1 + vol2) / 2.0)))).getData();
  }

  public double[] fitTwoVolatilities(final double forward, final double[] strikes, final double[] vols, final double timeToExpiry) {
    ArgumentChecker.isTrue(strikes[0] < strikes[1], "strikes must be in assending order");
    ArgumentChecker.isTrue(strikes[1] < forward || strikes[0] > forward, "strikes cannot be either side of forward");

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getVolDifferenceFunc(forward, strikes, vols, timeToExpiry);
    try {
      return TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(func, TRANSFORMS.transform(new DoubleMatrix1D(0.0, (vols[0] + vols[1]) / 2.0)))).getData();
    } catch (MathException e) {
      LOG.info("Failed to match volatilities. Trying to match on price");
      boolean call = strikes[0] >= forward;
      double p1 = BlackFormulaRepository.price(forward, strikes[0], timeToExpiry, vols[0], call);
      double p2 = BlackFormulaRepository.price(forward, strikes[1], timeToExpiry, vols[1], call);
      return fitTwoPrices(forward, strikes, new double[] {p1, p2 }, timeToExpiry, call);
    }
  }

  public double[] fitPriceAndGrad(final double forward, final double strike, final double price, final double priceGrad, final double timeToExpiry, final boolean isCall) {
    if (isCall && priceGrad >= 0.0) {
      throw new IllegalArgumentException("Call prices must decrease with strike, but priceGrad is " + priceGrad);
    }
    if (!isCall && priceGrad <= 0.0) {
      throw new IllegalArgumentException("Put prices must increase with strike, but priceGrad is " + priceGrad);
    }

    double vol = BlackFormulaRepository.impliedVolatility(price, forward, strike, timeToExpiry, isCall);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getPriceGradDifferenceFunc(forward, strike, price, priceGrad, timeToExpiry, isCall);
    return TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(func, TRANSFORMS.transform(new DoubleMatrix1D(0.0, vol)))).getData();
  }

  public double[] fitVolatilityAndGrad(final double forward, final double strike, final double vol, final double volGrad, final double timeToExpiry) {

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getVolGradDifferenceFunc(forward, strike, vol, volGrad, timeToExpiry);
    try {
      return TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(func, TRANSFORMS.transform(new DoubleMatrix1D(0.0, vol)))).getData();
    } catch (Exception e) {
      //only if the fit fails, check inputs 
      boolean isCall = strike >= forward;
      double dd = BlackFormulaRepository.dualDelta(forward, strike, timeToExpiry, vol, isCall);
      double vega = BlackFormulaRepository.vega(forward, strike, timeToExpiry, vol);
      double grad = -dd / vega;
      if (isCall && volGrad >= grad) {
        throw new IllegalArgumentException("Smile gradient of " + volGrad + " is too large. Maximum allowed to avoid arbitrage is " + grad);
      }
      if (!isCall && volGrad <= grad) {
        throw new IllegalArgumentException("Smile gradient of " + volGrad + " is too small. minimum allowed to avoid arbitrage is " + grad);
      }
      //if the gradient is OK, then it failed for some other reason, so try fitting on price;
      LOG.info("Failed to match volatility and smile gradient. Trying to match on price and dual delta");
      double price = BlackFormulaRepository.price(forward, strike, timeToExpiry, vol, isCall);
      double dPrice = dd + vega * volGrad;
      return fitPriceAndGrad(forward, strike, price, dPrice, timeToExpiry, isCall);
    }
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

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getPriceGradDifferenceFunc(final double forward, final double strike, final double targetPrice, final double targetDPrice, final double expiry,
      final boolean isCall) {
    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        DoubleMatrix1D y = TRANSFORMS.inverseTransform(x);
        double mu = y.getEntry(0);
        double theta = y.getEntry(1);
        double price = ShiftedLogNormalTailExtrapolation.price(forward, strike, expiry, isCall, mu, theta);
        double dPrice = ShiftedLogNormalTailExtrapolation.dualDelta(forward, strike, expiry, isCall, mu, theta);
        return new DoubleMatrix1D((price - targetPrice) / targetPrice, (dPrice - targetDPrice) / targetDPrice);
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
