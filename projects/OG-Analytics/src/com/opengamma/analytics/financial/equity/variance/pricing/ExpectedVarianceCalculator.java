/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackImpliedStrikeFromDeltaFunction;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceLogMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculate the expected annualised variance where the underlying is a diffusion (i.e. no jumps) no static replicative of a log-payoff
 */
public class ExpectedVarianceCalculator {
  private static final double SMALL_TIME_CUTOFF = 1e-4;
  private static final double DEFAULT_TOL = 1e-9;
  private static final Integrator1D<Double, Double> DEFAULT_INTEGRAL = new RungeKuttaIntegrator1D();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private final double _tol;

  public ExpectedVarianceCalculator() {
    _tol = DEFAULT_TOL;
  }

  public ExpectedVarianceCalculator(final double tol) {
    _tol = tol;
  }

  public double getAnnualisedVariance(final double forward, final double expiry, final BlackVolatilitySurfaceStrike surface) {

    ArgumentChecker.isTrue(forward > 0.0, "forward is {}", forward);
    ArgumentChecker.isTrue(expiry > 0.0, "expiry is {}", expiry);
    ArgumentChecker.notNull(surface, "null surface");

    final double atmVol = surface.getVolatility(expiry, forward);
    if (expiry < SMALL_TIME_CUTOFF) {
      return atmVol * atmVol;
    }

    final Function1D<Double, Double> integrand = getStrikeIntegrand(forward, expiry, surface);
    final Function1D<Double, Double> remainderFunction = getRemainderFunction(forward, expiry, surface);
    final double rootT = Math.sqrt(expiry);
    final double invNorTol = NORMAL.getInverseCDF(_tol);
    double putPart = DEFAULT_INTEGRAL.integrate(integrand, 0.0, forward);

    double u = forward * Math.exp(-invNorTol * atmVol * rootT); //initial estimate of upper limit
    double callPart = DEFAULT_INTEGRAL.integrate(integrand, forward, u);
    double rem = remainderFunction.evaluate(u);
    double error = rem / callPart;
    while (error > _tol) {
      callPart += DEFAULT_INTEGRAL.integrate(integrand, u, 2 * u);
      u *= 2.0;
      rem = remainderFunction.evaluate(u);
      error = rem / putPart;
    }

    return 2 * (putPart + callPart) / expiry;
  }

  public double getAnnualisedVariance(final double expiry, final BlackVolatilitySurfaceMoneyness surface) {
    ArgumentChecker.isTrue(expiry > 0.0, "expiry is {}", expiry);
    ArgumentChecker.notNull(surface, "null surface");
    final BlackVolatilitySurfaceLogMoneyness logMS = BlackVolatilitySurfaceConverter.toLogMoneynessSurface(surface);
    return getAnnualisedVariance(expiry, logMS);
  }

  public double getAnnualisedVariance(final double expiry, final BlackVolatilitySurfaceLogMoneyness surface) {
    ArgumentChecker.isTrue(expiry > 0.0, "expiry is {}", expiry);
    ArgumentChecker.notNull(surface, "null surface");

    final double atmVol = surface.getVolatilityForLogMoneyness(expiry, 0.0);
    if (expiry < SMALL_TIME_CUTOFF) {
      return atmVol * atmVol;
    }
    final double rootT = Math.sqrt(expiry);
    final double invNorTol = NORMAL.getInverseCDF(_tol);

    final Function1D<Double, Double> integrand = getLogMoneynessIntegrand(expiry, surface);

    final double l = invNorTol * atmVol * rootT; //initial estimate of lower limit
    double putPart = DEFAULT_INTEGRAL.integrate(integrand, l, 0.0);
    double rem = integrand.evaluate(l); //this comes from transforming the strike remainder estimate 
    double error = rem / putPart;
    int step = 1;
    while (error > _tol) {
      putPart += DEFAULT_INTEGRAL.integrate(integrand, (step + 1) * l, step * l);
      step++;
      rem = integrand.evaluate((step + 1) * l);
      error = rem / putPart;
    }
    putPart += rem; //add on the (very small) remainder estimate otherwise we'll always underestimate variance

    final double u = -invNorTol * atmVol * rootT; //initial estimate of upper limit
    double callPart = DEFAULT_INTEGRAL.integrate(integrand, 0.0, u);
    rem = integrand.evaluate(u);
    error = rem / callPart;
    step = 1;
    while (error > _tol) {
      callPart += DEFAULT_INTEGRAL.integrate(integrand, step * u, (1 + step) * u);
      step++;
      rem = integrand.evaluate((1 + step) * u);
      error = rem / putPart;
    }
    //callPart += rem;
    //don't add on the remainder estimate as it is very conservative, and likely too large

    return 2 * (putPart + callPart) / expiry;
  }

  public double getAnnualisedVariance(final double forward, final double expiry, final BlackVolatilitySurfaceDelta surface) {

    ArgumentChecker.isTrue(forward > 0.0, "forward is {}", forward);
    ArgumentChecker.isTrue(expiry > 0.0, "expiry is {}", expiry);
    ArgumentChecker.notNull(surface, "null surface");

    if (expiry < SMALL_TIME_CUTOFF) {
      final double dnsVol = surface.getVolatilityForDelta(expiry, 0.5);
      return dnsVol * dnsVol; //this will be identical to atm-vol for t-> 0
    }

    final Function1D<Double, Double> integrand = getDeltaIntegrand(forward, expiry, surface);
    //find the delta corresponding to the at-the-money-forward (NOTE this is not the DNS of delta = 0.5)
    final double atmfVol = surface.getVolatility(expiry, forward);
    final double atmfDelta = BlackFormulaRepository.delta(forward, forward, expiry, atmfVol, true);

    //Do the call/k^2 integral - split up into the the put integral and the call integral because the function is not smooth at strike = forward
    double callPart = DEFAULT_INTEGRAL.integrate(integrand, _tol, atmfDelta);
    double putPart = DEFAULT_INTEGRAL.integrate(integrand, atmfDelta, 1 - _tol);
    return 2 * (putPart + callPart) / expiry;
  }

  private Function1D<Double, Double> getStrikeIntegrand(final double forward, final double expiry, final BlackVolatilitySurfaceStrike surface) {
    final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike) {
        if (strike == 0) {
          return 0.0;
        }
        final boolean isCall = strike >= forward;
        final double vol = surface.getVolatility(expiry, strike);
        final double otmPrice = BlackFormulaRepository.price(forward, strike, expiry, vol, isCall);
        final double weight = 1.0 / (strike * strike);
        return otmPrice * weight;
      }
    };
    return integrand;
  }

  private Function1D<Double, Double> getLogMoneynessIntegrand(final double expiry, final BlackVolatilitySurfaceLogMoneyness surface) {
    final double rootT = Math.sqrt(expiry);

    final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double logMoneyness) {
        final boolean isCall = logMoneyness >= 0.0;
        final int sign = isCall ? 1 : -1;
        final double vol = surface.getVolatilityForLogMoneyness(expiry, logMoneyness);
        final double sigmaRootT = vol * rootT;

        if (logMoneyness == 0.0) {
          return 2 * NORMAL.getCDF(0.5 * sigmaRootT) - 1.;
        }
        if (sigmaRootT < 1e-12) {
          return 0.0;
        }
        final double d1 = -logMoneyness / sigmaRootT + 0.5 * sigmaRootT;
        final double d2 = d1 - sigmaRootT;
        final double res = sign * (Math.exp(-logMoneyness) * NORMAL.getCDF(sign * d1) - NORMAL.getCDF(sign * d2));
        return res;
      }
    };
    return integrand;
  }

  private Function1D<Double, Double> getDeltaIntegrand(final double forward, final double expiry, final BlackVolatilitySurfaceDelta surface) {
    final double eps = 1e-8; //TODO fairly arbitrary choice of epsilon 
    final double rootT = Math.sqrt(expiry);

    final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double delta) {

        final double vol = surface.getVolatilityForDelta(expiry, delta);
        final double sigmaRootT = vol * rootT;
        //TODO handle sigmaRootT -> 0

        final double strike = BlackImpliedStrikeFromDeltaFunction.impliedStrike(delta, true, forward, expiry, vol);
        final boolean isCall = strike >= forward;
        final int sign = isCall ? 1 : -1;

        //TODO if should be the job of the vol surface to provide derivatives
        double dSigmaDDelta;
        if (delta < eps) {
          final double volUp = surface.getVolatilityForDelta(expiry, delta + eps);
          dSigmaDDelta = (volUp - vol) / eps;
        } else if (delta > 1 - eps) {
          final double volDown = surface.getVolatilityForDelta(expiry, delta - eps);
          dSigmaDDelta = (vol - volDown) / eps;
        } else {
          final double volUp = surface.getVolatilityForDelta(expiry, delta + eps);
          final double volDown = surface.getVolatilityForDelta(expiry, delta - eps);
          dSigmaDDelta = (volUp - volDown) / 2 / eps;
        }

        final double d1 = NORMAL.getInverseCDF(delta);
        final double d2 = d1 - sigmaRootT;
        final double weight = (vol * rootT / NORMAL.getPDF(d1) + dSigmaDDelta * (d1 * rootT - vol * expiry)) / strike;
        final double otmPrice = sign * (forward * (isCall ? delta : 1 - delta) - strike * NORMAL.getCDF(sign * d2));
        return weight * otmPrice;
      }
    };

    return integrand;
  }

  private Function1D<Double, Double> getRemainderFunction(final double forward, final double expiry, final BlackVolatilitySurfaceStrike surface) {
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
        if (strike == 0) {
          return 0.0;
        }
        final boolean isCall = strike >= forward;
        final double vol = surface.getVolatility(expiry, strike);
        final double otmPrice = BlackFormulaRepository.price(forward, strike, expiry, vol, isCall);
        final double res = (isCall ? otmPrice / strike : otmPrice / 2 / strike);
        return res;
      }
    };
  }

}
