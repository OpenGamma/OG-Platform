/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.NonLinearParameterTransforms;
import com.opengamma.analytics.math.minimization.NonLinearTransformFunction;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.UncoupledParameterTransforms;
import com.opengamma.analytics.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * Fit a shifted log-normal model to two pieces of information from the tail of the smile (i.e. two prices/vols or a price and gradient)
 */
public class ShiftedLogNormalTailExtrapolationFitter {

  private static final Logger LOG = LoggerFactory.getLogger(ShiftedLogNormalTailExtrapolationFitter.class);
  private static final ScalarFirstOrderDifferentiator DIFFERENTIATOR = new ScalarFirstOrderDifferentiator();
  //Review R White - this was changed from   BroydenVectorRootFinder (with default parameters to get test roundTripTest to work for all values)
  private static final NewtonVectorRootFinder ROOTFINDER = new NewtonDefaultVectorRootFinder(1e-8, 1e-8, 500);

  private static final NonLinearParameterTransforms TRANSFORMS;

  static {
    final ParameterLimitsTransform b = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    DoubleRangeLimitTransform a = new DoubleRangeLimitTransform(-40.0, 40.0);
    TRANSFORMS = new UncoupledParameterTransforms(new DoubleMatrix1D(0.0, 1.0), new ParameterLimitsTransform[] {a, b}, new BitSet());
  }

  /**
   * Fit a shifted log-normal model to two option prices at different strikes
   * @param forward The forward value of the underlying at expiry
   * @param strikes The <b>two</b> strikes. These must be in ascending order and NOT either side of the forward
   * @param prices The <b>two</b> prices of the two options
   * @param timeToExpiry time-to-expiry
   * @param isCall true for call
   * @return double array containing the exponential shift of the forward, $mu$, such that the effective forward is $f \exp(\mu)$ and the volatility, $\sigma$
   */
  public double[] fitTwoPrices(final double forward, final double[] strikes, final double[] prices, final double timeToExpiry, final boolean isCall) {
    ArgumentChecker.isTrue(strikes[0] < strikes[1], "strikes must be in ascending order");
    ArgumentChecker.isTrue(strikes[1] < forward || strikes[0] > forward, "strikes cannot be either side of forward");
    if (isCall) {
      ArgumentChecker.isTrue(prices[0] > prices[1], "Call prices are not decreasing with strike. Either the input data is wrong or there is an arbitrage");
    } else {
      ArgumentChecker.isTrue(prices[0] < prices[1], "Put prices are not increasing with strike. Either the input data is wrong or there is an arbitrage");
    }

    final double vol1 = BlackFormulaRepository.impliedVolatility(prices[0], forward, strikes[0], timeToExpiry, isCall);
    final double vol2 = BlackFormulaRepository.impliedVolatility(prices[1], forward, strikes[1], timeToExpiry, isCall);
    final double kAv = (strikes[0] + strikes[1]) / 2.0;
    final double sigmaAv = (vol1 + vol2) / 2.0;
    final double pAv = BlackFormulaRepository.price(forward, kAv, timeToExpiry, sigmaAv, isCall);
    final double sigmaGrad = (vol1 - vol2) / (strikes[0] - strikes[1]);
    final double pGrad = BlackFormulaRepository.dualDelta(forward, kAv, timeToExpiry, sigmaAv, isCall) + BlackFormulaRepository.vega(forward, kAv, timeToExpiry, sigmaAv)
        * sigmaGrad;

    //This often fails to converge, thus the model is first fitted using price and grad, which given a point very close to the solution to use as the starting point
    final double[] temp = fitPriceAndGrad(forward, kAv, pAv, pGrad, timeToExpiry, isCall);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getPriceDifferenceFunc(forward, strikes, prices, timeToExpiry, isCall);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = getPriceDifferenceJac(forward, strikes, prices, timeToExpiry, isCall);
    final NonLinearTransformFunction nltf = new NonLinearTransformFunction(func, jac, TRANSFORMS);

    final DoubleMatrix1D start = TRANSFORMS.transform(new DoubleMatrix1D(temp));
    return TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(nltf.getFittingFunction(), nltf.getFittingJacobian(), start)).getData();
  }

  /**
   * Fit a shifted log-normal model to an option's price and dual delta (price sensitivity to strike)  at a single strike
   * @param forward The forward value of the underlying at expiry
   * @param strike The strike
   * @param price The option's price
   * @param priceGrad The option's dual delta
   * @param timeToExpiry time-to-expiry
   * @param isCall true for call
   * @return double array containing the exponential shift of the forward, $mu$, such that the effective forward is $f \exp(\mu)$ and the volatility, $\sigma$
   */
  public double[] fitPriceAndGrad(final double forward, final double strike, final double price, final double priceGrad, final double timeToExpiry, final boolean isCall) {

    ArgumentChecker.isTrue(forward > 0, "Forward must be greater that zero. value given is {}", forward);
    ArgumentChecker.isTrue(strike >= 0, "Strike must be greater that or equal to zero. value given is {}", strike);
    ArgumentChecker.isTrue(price > 0, "Price must be greater that zero. value given is {}", price);
    ArgumentChecker.isTrue(timeToExpiry > 0, "timeToExpiry must be greater that zero. value given is {}", timeToExpiry);
    if (isCall) {
      ArgumentChecker.isTrue(price < forward, "call price must be less than forward. Price  is {} and foward is {} ", price, forward);
      ArgumentChecker.isTrue(priceGrad < 0.0, "Call prices must decrease with strike, but priceGrad is {}", priceGrad);
      final double approxATMPrice = price + (forward - strike) * priceGrad;
      if (approxATMPrice >= forward) {
        throw new IllegalArgumentException("inputs imply an ATM price of >  " + approxATMPrice + " which is greater than the forward of " + forward);
      }
    } else {
      ArgumentChecker.isTrue(price < strike, "put price must be less than strike. Price is {} and strike is  {}", price, strike);
      ArgumentChecker.isTrue(priceGrad > 0.0, "Put prices must increase with strike, but priceGrad is {}", priceGrad);
    }

    final double vol = BlackFormulaRepository.impliedVolatility(price, forward, strike, timeToExpiry, isCall);
    final DoubleMatrix1D start = TRANSFORMS.transform(new DoubleMatrix1D(0.0, vol));

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getPriceGradDifferenceFunc(forward, strike, price, priceGrad, timeToExpiry, isCall);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = getPriceGradJac(forward, strike, price, priceGrad, timeToExpiry, isCall);
    final NonLinearTransformFunction nltf = new NonLinearTransformFunction(func, jac, TRANSFORMS);
    return TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(nltf.getFittingFunction(), nltf.getFittingJacobian(), start)).getData();
  }

  /**
   * Fit a shifted log-normal model to two option implied volatilities at different strikes
   * @param forward The forward value of the underlying at expiry
   * @param strikes The <b>two</b> strikes. These must be in ascending order and NOT either side of the forward
   * @param vols The <b>two</b> implied of the two options
   * @param timeToExpiry time-to-expiry
   * @return double array containing the exponential shift of the forward, $mu$, such that the effective forward is $f \exp(\mu)$ and the volatility, $\sigma$
   */
  public double[] fitTwoVolatilities(final double forward, final double[] strikes, final double[] vols, final double timeToExpiry) {
    ArgumentChecker.isTrue(strikes[0] < strikes[1], "strikes must be in assending order");
    ArgumentChecker.isTrue(strikes[1] < forward || strikes[0] > forward, "strikes cannot be either side of forward");

    final double kAv = (strikes[0] + strikes[1]) / 2.0;
    final double volsAv = (vols[0] + vols[1]) / 2.0;
    final double volGrad = (vols[1] - vols[0]) / (strikes[1] - strikes[0]);

    final double[] temp = fitVolatilityAndGrad(forward, kAv, volsAv, volGrad, timeToExpiry);
    final DoubleMatrix1D start = TRANSFORMS.transform(new DoubleMatrix1D(temp));

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getVolDifferenceFunc(forward, strikes, vols, timeToExpiry);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = getVolDifferenceJac(forward, strikes, timeToExpiry);
    final NonLinearTransformFunction nltf = new NonLinearTransformFunction(func, jac, TRANSFORMS);
    return TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(nltf.getFittingFunction(), nltf.getFittingJacobian(), start)).getData();
  }

  /**
   * Fit a shifted log-normal model to an option's implied volatility and implied volatility sensitivity to strike (i.e. the gradient of the smile)  at a single strike
   * @param forward The forward value of the underlying at expiry
   * @param strike The strike
   * @param vol The option's implied volatility
   * @param volGrad The gradient of the smile at the strike
   * @param timeToExpiry time-to-expiry
   * @return double array containing the exponential shift of the forward, $mu$, such that the effective forward is $f \exp(\mu)$ and the volatility, $\sigma$
   */
  public double[] fitVolatilityAndGrad(final double forward, final double strike, final double vol, final double volGrad, final double timeToExpiry) {

    //check the inputs make sense
    final boolean isCall = strike >= forward;
    final double blackDD = BlackFormulaRepository.dualDelta(forward, strike, timeToExpiry, vol, isCall);
    final double blackVega = BlackFormulaRepository.vega(forward, strike, timeToExpiry, vol);

    final double minGrad = (isCall ? -(1 + blackDD) : -blackDD) / blackVega;
    final double maxGrad = (isCall ? -blackDD : 1 - blackDD) / blackVega;

    if (volGrad >= maxGrad || volGrad <= minGrad) {
      LOG.info("Extrapolation - Expiry = " + timeToExpiry + "- failed to fit tail to strike, " + strike + ". volGrad, " + volGrad + ", out of bounds. minGrad = "
          + minGrad + ", maxGrad = " + maxGrad);
      throw new IllegalArgumentException("Volatility smile gradient must be in range " + minGrad + " to " + maxGrad + ", but value is " + volGrad);
    }

    // The shifted log-normal model does not guarantee that call prices are below the forward and hence that the implied volatility exists.
    // The root finding can fail (even when a genuine solution does exist) because the parameters have wandered into a region where the implied volatility does not exist.
    // The remedy is to fit for price and dual delta, which will give the correct answer (prices above the forward, while not economically possible, do not bother the root finder)
    final double price = BlackFormulaRepository.price(forward, strike, timeToExpiry, vol, isCall);
    final double dd = blackDD + blackVega * volGrad;
    return fitPriceAndGrad(forward, strike, price, dd, timeToExpiry, isCall);
  }

  /**
   * Fit a shifted log-normal model to an option's implied volatility and implied volatility sensitivity to strike (i.e. the gradient of the smile)  at a single strike
   * @param forward The forward value of the underlying at expiry
   * @param strike The strike
   * @param smile A functional form of the volatility smile (must be differentiable at the strike)
   * @param timeToExpiry time-to-expiry
   * @return double array containing the exponential shift of the forward, $mu$, such that the effective forward is $f \exp(\mu)$ and the volatility, $\sigma$
   */
  public double[] fitVolatilityAndGrad(final double forward, final double strike, final Function1D<Double, Double> smile, final double timeToExpiry) {
    final double vol = smile.evaluate(strike);
    final Function1D<Double, Double> smileGrad = DIFFERENTIATOR.differentiate(smile);
    final double dVol = smileGrad.evaluate(strike);
    return fitVolatilityAndGrad(forward, strike, vol, dVol, timeToExpiry);
  }

  /**
   * Calls fitVolatilityAndGrad. If this fails, it will retry from the nearest strike within the domain, and continue to do this until success is found
   * @param forward forward
   * @param strikes array of strikes
   * @param vols array of vols at strikes
   * @param dSigmaDx Function1D<Double, Double> that produces the vol gradient at given strike
   * @param expiry option expiry
   * @param lowTail True if fitting extrapolation model to low strikes, false if fitting to high strike tail.
   * @return 3-element array containing: [0] mu = ln(shiftedForward / originalForward) [1] theta = new ln volatility to use [2] new extrapolation boundary
   */
  public ArrayList<Double> fitVolatilityAndGradRecursivelyByTossingPoints(final double forward, final double[] strikes, final double[] vols,
      final Function1D<Double, Double> dSigmaDx, final double expiry, final boolean lowTail) {
    final int n = strikes.length;
    ArgumentChecker.isTrue(vols.length == n, "Lengths of strikes and vols unexpectedly differ!");
    double[] shiftAndVol;
    final int endIdx = lowTail ? 0 : n - 1;
    try {
      shiftAndVol = fitVolatilityAndGrad(forward, strikes[endIdx], vols[endIdx], dSigmaDx.evaluate(strikes[endIdx]), expiry);
    } catch (final Exception e) {
      LOG.info("Extrapolation - Expiry = " + expiry + "- failed to fit tail to " + strikes[endIdx] + ". Trying next strike. Caught " + e);
      if (lowTail) {
        return fitVolatilityAndGradRecursivelyByTossingPoints(forward, Arrays.copyOfRange(strikes, 1, n), Arrays.copyOfRange(vols, 1, n), dSigmaDx, expiry, lowTail);
      }
      return fitVolatilityAndGradRecursivelyByTossingPoints(forward, Arrays.copyOfRange(strikes, 0, n - 1), Arrays.copyOfRange(vols, 0, n - 1), dSigmaDx, expiry, lowTail);
    }
    LOG.info("Extrapolating from strike, " + strikes[endIdx] + ", with shifted forward, " + forward * Math.exp(shiftAndVol[0]) + ", and vol, " + shiftAndVol[1]);
    final ArrayList<Double> listShiftVolStrike = new ArrayList<>();
    listShiftVolStrike.add(0, shiftAndVol[0]); // mu = ln(shiftedForward / originalForward)
    listShiftVolStrike.add(1, shiftAndVol[1]); // theta = new ln volatility to use
    listShiftVolStrike.add(2, strikes[endIdx]); // new extapolation boundary
    return listShiftVolStrike;
  }

  /**
   * Calls fitVolatilityAndGrad. If this fails, it will retry from the nearest strike within the domain, and continue to do this until success is found
   * @param forward forward
   * @param strikes array of strikes
   * @param vols array of vols at strikes
   * @param dSigmaDx array of vol gradients at strikes
   * @param expiry option expiry
   * @param lowTail True if fitting extrapolation model to low strikes, false if fitting to high strike tail.
   * @return 3-element array containing: [0] mu = ln(shiftedForward / originalForward) [1] theta = new ln volatility to use [2] new extapolation boundary
   */
  public ArrayList<Double> fitVolatilityAndGradRecursivelyByTossingPoints(final double forward, final double[] strikes, final double[] vols, final double[] dSigmaDx,
      final double expiry, final boolean lowTail) {
    final int n = strikes.length;
    ArgumentChecker.isTrue(vols.length == n, "Lengths of strikes and vols unexpectedly differ!");
    ArgumentChecker.isTrue(dSigmaDx.length == n, "Lengths of slopes and vols unexpectedly differ!");

    double[] shiftAndVol;
    final int endIdx = lowTail ? 0 : n - 1;
    try {
      shiftAndVol = fitVolatilityAndGrad(forward, strikes[endIdx], vols[endIdx], dSigmaDx[endIdx], expiry);
    } catch (final Exception e) {
      LOG.info("Extrapolation - Expiry = " + expiry + "- failed to fit tail to " + strikes[endIdx] + ". Trying next strike. Caught " + e);
      if (lowTail) {
        return fitVolatilityAndGradRecursivelyByTossingPoints(forward, Arrays.copyOfRange(strikes, 1, n), Arrays.copyOfRange(vols, 1, n),
            Arrays.copyOfRange(dSigmaDx, 1, n), expiry, lowTail);
      }
      return fitVolatilityAndGradRecursivelyByTossingPoints(forward, Arrays.copyOfRange(strikes, 0, n - 1), Arrays.copyOfRange(vols, 0, n - 1),
          Arrays.copyOfRange(dSigmaDx, 0, n - 1), expiry, lowTail);
    }
    LOG.info("Extrapolating from strike, " + strikes[endIdx] + ", with shifted forward, " + forward * Math.exp(shiftAndVol[0]) + ", and vol, " + shiftAndVol[1]);
    final ArrayList<Double> listShiftVolStrike = new ArrayList<>();
    listShiftVolStrike.add(0, shiftAndVol[0]); // mu = ln(shiftedForward / originalForward)
    listShiftVolStrike.add(1, shiftAndVol[1]); // theta = new ln volatility to use
    listShiftVolStrike.add(2, strikes[endIdx]); // new extrapolation boundary
    return listShiftVolStrike;
  }

  /**
   * Calls fitVolatilityAndGrad. If this fails, it will retry from the nearest strike within the domain, then continue to reduce smile until success is found
   * @param forward forward
   * @param strike strike at which to begin extrapolation
   * @param vol lognormal implied volatility at strike
   * @param volGrad the vol gradient at given strike
   * @param expiry option expiry
   * @return 2-element array containing: [0] mu = ln(shiftedForward / originalForward) [1] sigma = new lognormal volatility to use
   */
  public double[] fitVolatilityAndGradRecursivelyByReducingSmile(final double forward, final double strike, final double vol, final double volGrad, final double expiry) {
    double[] shiftAndVol;
    try {
      shiftAndVol = fitVolatilityAndGrad(forward, strike, vol, volGrad, expiry);
    } catch (final Exception e) {

      double newVolGrad;

      // Find the bounds in which a solution should exist
      final boolean isCall = strike >= forward;
      final double blackDD = BlackFormulaRepository.dualDelta(forward, strike, expiry, vol, isCall);
      final double blackVega = BlackFormulaRepository.vega(forward, strike, expiry, vol);
      final double minGrad = (isCall ? -(1 + blackDD) : -blackDD) / blackVega;
      final double maxGrad = (isCall ? -blackDD : 1 - blackDD) / blackVega;
      // Adjust gradient dVol/dStrike to just within these bounds
      if (volGrad >= maxGrad) {
        newVolGrad = (maxGrad > 0.0 ? 0.99 : 1.01) * maxGrad;
      } else if (volGrad <= minGrad) {
        newVolGrad = (minGrad < 0.0 ? 0.99 : 1.01) * minGrad;
      } else {

        // Although within bounds, the root finder is failing to find a solution. Reducing smile further works, but it is not optimal. A solution exists, but the starting point isn't close enough..
        // TODO Review this fail-over behaviour
        LOG.info("Extrapolation - Expiry = " + expiry + "- failed to fit tail to strike, " + strike + ", and DVolDStrike, " + volGrad
            + ", though within bounds. Lowering smile.");
        newVolGrad = 0.95 * volGrad;
      }
      return fitVolatilityAndGradRecursivelyByReducingSmile(forward, strike, vol, newVolGrad, expiry);
    }
    return shiftAndVol;
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getPriceDifferenceFunc(final double forward, final double[] strike, final double[] prices,
      final double timeToExpiry, final boolean isCall) {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D y) {
        final double mu = y.getEntry(0);
        final double theta = y.getEntry(1);

        final double p1 = ShiftedLogNormalTailExtrapolation.price(forward, strike[0], timeToExpiry, isCall, mu, theta);
        final double p2 = ShiftedLogNormalTailExtrapolation.price(forward, strike[1], timeToExpiry, isCall, mu, theta);
        return new DoubleMatrix1D((p1 - prices[0]) / prices[0], (p2 - prices[1]) / prices[1]);
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix2D> getPriceDifferenceJac(final double forward, final double[] strike, final double[] prices, final double timeToExpiry,
      final boolean isCall) {

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D y) {
        final double mu = y.getEntry(0);
        final double theta = y.getEntry(1);
        final double fStar = forward * Math.exp(mu);
        final double j11 = BlackFormulaRepository.delta(fStar, strike[0], timeToExpiry, theta, isCall) * fStar / prices[0];
        final double j12 = BlackFormulaRepository.vega(fStar, strike[0], timeToExpiry, theta) / prices[0];
        final double j21 = BlackFormulaRepository.delta(fStar, strike[1], timeToExpiry, theta, isCall) * fStar / prices[1];
        final double j22 = BlackFormulaRepository.vega(fStar, strike[1], timeToExpiry, theta) / prices[1];

        final DoubleMatrix2D modelParmJac = new DoubleMatrix2D(new double[][] {{j11, j12}, {j21, j22}});
        return modelParmJac;
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getVolDifferenceFunc(final double forward, final double[] strike, final double[] vols, final double timeToExpiry) {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D y) {
        final double mu = y.getEntry(0);
        final double theta = y.getEntry(1);

        final double v1 = ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, strike[0], timeToExpiry, mu, theta);
        final double v2 = ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, strike[1], timeToExpiry, mu, theta);
        return new DoubleMatrix1D((v1 - vols[0]), (v2 - vols[1]));
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix2D> getVolDifferenceJac(final double forward, final double[] strike, final double timeToExpiry) {

    final boolean isCall = strike[0] > forward;

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D y) {
        final double mu = y.getEntry(0);
        final double theta = y.getEntry(1);

        final double fStar = forward * Math.exp(mu);

        final double p1 = BlackFormulaRepository.price(fStar, strike[0], timeToExpiry, theta, isCall);
        final double p2 = BlackFormulaRepository.price(fStar, strike[1], timeToExpiry, theta, isCall);
        final double vol1 = BlackFormulaRepository.impliedVolatility(p1, forward, strike[0], timeToExpiry, isCall);
        final double vol2 = BlackFormulaRepository.impliedVolatility(p2, forward, strike[1], timeToExpiry, isCall);
        final double vega1 = BlackFormulaRepository.vega(forward, strike[0], timeToExpiry, vol1);
        final double vega2 = BlackFormulaRepository.vega(forward, strike[1], timeToExpiry, vol2);

        final double j11 = BlackFormulaRepository.delta(fStar, strike[0], timeToExpiry, theta, isCall) * fStar / vega1;
        final double j12 = BlackFormulaRepository.vega(fStar, strike[0], timeToExpiry, theta) / vega1;
        final double j21 = BlackFormulaRepository.delta(fStar, strike[1], timeToExpiry, theta, isCall) * fStar / vega2;
        final double j22 = BlackFormulaRepository.vega(fStar, strike[1], timeToExpiry, theta) / vega2;

        return new DoubleMatrix2D(new double[][] {{j11, j12}, {j21, j22}});
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getPriceGradDifferenceFunc(final double forward, final double strike, final double targetPrice,
      final double targetDPrice, final double expiry, final boolean isCall) {

    final double scale1 = 1.0 / targetPrice;
    final double scale2 = 1.0 / targetDPrice;

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D y) {
        final double mu = y.getEntry(0);
        final double theta = y.getEntry(1);
        final double price = scale1 * ShiftedLogNormalTailExtrapolation.price(forward, strike, expiry, isCall, mu, theta);
        final double dPrice = scale2 * ShiftedLogNormalTailExtrapolation.dualDelta(forward, strike, expiry, isCall, mu, theta);
        return new DoubleMatrix1D(price - 1.0, dPrice - 1.0);
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix2D> getPriceGradJac(final double forward, final double strike, final double targetPrice, final double targetDPrice,
      final double expiry, final boolean isCall) {
    final double scale1 = 1.0 / targetPrice;
    final double scale2 = 1.0 / targetDPrice;

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D y) {
        final double mu = y.getEntry(0);
        final double theta = y.getEntry(1);
        final double fStar = forward * Math.exp(mu);
        final double j11 = scale1 * BlackFormulaRepository.delta(fStar, strike, expiry, theta, isCall) * fStar;
        final double j12 = scale1 * BlackFormulaRepository.vega(fStar, strike, expiry, theta);
        final double j21 = scale2 * BlackFormulaRepository.crossGamma(fStar, strike, expiry, theta) * fStar;
        final double j22 = scale2 * BlackFormulaRepository.dualVanna(fStar, strike, expiry, theta);

        return new DoubleMatrix2D(new double[][] {{j11, j12}, {j21, j22}});
      }
    };

  }

}
