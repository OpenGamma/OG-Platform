/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.finitedifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.financial.model.finitedifference.CoupledPDEDataBundle;
import com.opengamma.financial.model.finitedifference.ExtendedCoupledPDEDataBundle;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.volatility.surface.AbsoluteLocalVolatilitySurface;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class PDEDataBundleProvider {

  /**
   * Sets up a standard Black-Scholes PDE 
   * @param vol The volatility
   * @param rate The rate
   * @param strike The strike
   * @param isCall is a call
   * @return a convection data bundle
   */
  public ConvectionDiffusionPDEDataBundle getBackwardsBlackScholes(final double vol, final double rate, final double strike, final boolean isCall) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];
        final double temp = s * vol;
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];
        return -s * rate;
      }
    };

    return new ConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(rate), new EuropeanPayoff(strike, isCall));
  }

  public ConvectionDiffusionPDEDataBundle getBackwardsLogBlackScholes(final double vol, final double rate, final double strike, final boolean isCall) {

    final double a = -vol * vol / 2;
    final double b = -a - rate;

    final Function1D<Double, Double> payoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        final double s = Math.exp(x);
        if (isCall) {
          return Math.max(s - strike, 0);
        }
        return Math.max(strike - s, 0);

      }
    };

    return new ConvectionDiffusionPDEDataBundle(ConstantDoublesSurface.from(a), ConstantDoublesSurface.from(b), ConstantDoublesSurface.from(rate), payoff);
  }

  /**
   * When the rate is zero this models the process df = sigma*f^beta dw (i.e. f is the forward value and a Martingale) so the call price will need to be
   * multiplied by a suitable numeraire (e.g. the price of a zero coupon bond).  When rates are non zero the process is for the spot 
   * ds = rsdt + sigma_mod*s^beta dw, where sigma_mod = sigma * exp(rt*(beta-1)) and no correction is made to the price  
   * @param vol The volatility
   * @param rate The rate
   * @param strike The strike
   * @param beta The beta
   * @param isCall Is a call
   * @return A convection data bundle
   */
  public ConvectionDiffusionPDEDataBundle getBackwardsCEV(final double vol, final double rate, final double strike, final double beta, final boolean isCall) {
    Validate.isTrue(beta >= 0.0, "Need beta >=0");

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        double temp = vol * Math.pow(s, beta);
        if (rate != 0.0) {
          temp *= Math.exp(rate * (beta - 1) * t);
        }
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];
        return -s * rate;
      }
    };

    return new ConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(rate), new EuropeanPayoff(strike, isCall));
  }

  public CoupledPDEDataBundle[] getCoupledForwardPair(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data) {
    final AbsoluteLocalVolatilitySurface localVolOverlay = new AbsoluteLocalVolatilitySurface(ConstantDoublesSurface.from(1.0));
    return getCoupledForwardPair(forward, data, localVolOverlay);
  }

  public CoupledPDEDataBundle[] getCoupledForwardPair(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data, final AbsoluteLocalVolatilitySurface localVolOverlay) {
    Validate.notNull(forward, "null forward");
    Validate.notNull(data, "null data");
    Validate.notNull(localVolOverlay, "null localVolOverlay");

    final CoupledPDEDataBundle[] res = new CoupledPDEDataBundle[2];
    res[0] = getCoupledForwardPDE(forward, data.getVol1(), data.getLambda12(), data.getLambda21(), data.getP0(), data.getBeta1(), localVolOverlay);
    res[1] = getCoupledForwardPDE(forward, data.getVol2(), data.getLambda21(), data.getLambda12(), 1.0 - data.getP0(), data.getBeta2(), localVolOverlay);
    return res;
  }

  public ExtendedCoupledPDEDataBundle[] getCoupledFokkerPlankPair(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data) {
    final AbsoluteLocalVolatilitySurface localVolOverlay = new AbsoluteLocalVolatilitySurface(ConstantDoublesSurface.from(1.0));
    return getCoupledFokkerPlankPair(forward, data, localVolOverlay);
  }

  public ExtendedCoupledPDEDataBundle[] getCoupledFokkerPlankPair(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data, final AbsoluteLocalVolatilitySurface localVolOverlay) {
    Validate.notNull(forward, "null forward");
    Validate.notNull(data, "null data");
    Validate.notNull(localVolOverlay, "null localVolOverlay");

    final ExtendedCoupledPDEDataBundle[] res = new ExtendedCoupledPDEDataBundle[2];
    res[0] = getCoupledFokkerPlank(forward, data.getVol1(), data.getLambda12(), data.getLambda21(), data.getP0(), data.getBeta1(), localVolOverlay);
    res[1] = getCoupledFokkerPlank(forward, data.getVol2(), data.getLambda21(), data.getLambda12(), 1.0 - data.getP0(), data.getBeta2(), localVolOverlay);

    return res;
  }

  private CoupledPDEDataBundle getCoupledForwardPDE(final ForwardCurve forward, final double vol, final double lambda1, final double lambda2, final double initialProb, final double beta,
      final AbsoluteLocalVolatilitySurface localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double t = tk[0];
        final double k = tk[1];
        final double temp = vol * Math.pow(k, beta) * localVol.getVolatility(t, k);
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double k = tk[1];
        return k * forward.getDrift(beta);
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return lambda1;
      }
    };

    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double k) {
        return initialProb * Math.max(0, forward.getSpot() - k);
      }
    };

    return new CoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), -lambda2, initialCondition);
  }

  private ExtendedCoupledPDEDataBundle getCoupledFokkerPlank(final ForwardCurve forward, final double vol, final double lambda1, final double lambda2, final double initialProb, final double beta,
      final AbsoluteLocalVolatilitySurface localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return -1.0;
      }
    };

    final Function<Double, Double> aStar = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double temp = localVol.getVolatility(t, s) * vol * Math.pow(s, beta);

        return 0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        return s * forward.getDrift(t);
      }
    };

    final Function<Double, Double> bStar = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return 1.0;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];

        return forward.getDrift(t) + lambda1;
      }
    };

    //using a log-normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {
      private final double _volRootTOffset = 0.01;

      @Override
      public Double evaluate(final Double s) {
        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward.getSpot());
        final NormalDistribution dist = new NormalDistribution(0, _volRootTOffset);
        return initialProb * dist.getPDF(x) / s;
      }
    };

    return new ExtendedCoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), FunctionalDoublesSurface.from(aStar),
        FunctionalDoublesSurface.from(bStar), -lambda2, initialCondition);

  }

  private class EuropeanPayoff extends Function1D<Double, Double> {
    private final double _k;
    private final boolean _isCall;

    public EuropeanPayoff(final double strike, final boolean isCall) {
      _k = strike;
      _isCall = isCall;
    }

    @Override
    public Double evaluate(final Double x) {
      if (_isCall) {
        return Math.max(0, x - _k);
      }
      return Math.max(0, _k - x);
    }
  }

}
