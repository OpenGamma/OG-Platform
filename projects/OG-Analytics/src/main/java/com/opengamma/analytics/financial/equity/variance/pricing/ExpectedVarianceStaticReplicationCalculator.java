/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.ShiftedLogNormalTailExtrapolation;
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
 * Calculate the expected annualised variance where the underlying is a diffusion (i.e. no jumps) using static replication of a log-payoff. This implicitly assumes continuous
 * monitoring of the realised variance, while in practice the daily squared returns are used (the difference is normally negligible). 
 * <p>
 * Situations where the underlying contains jumps (other than equity dividend payments) are not currently handled.
 */
public class ExpectedVarianceStaticReplicationCalculator {
  /** The cutoff time */
  private static final double SMALL_TIME_CUTOFF = 1e-4;
  /** The default tolerance */
  private static final double DEFAULT_TOL = 1e-9;
  /** The minimum permissible tolerance */
  private static final double MIN_TOL = 1e-15;
  /** The maximum permissible tolerance */
  private static final double MAX_TOL = 0.1;
  /** The default integrator */
  private static final Integrator1D<Double, Double> DEFAULT_INTEGRATOR = new RungeKuttaIntegrator1D();
  /** A normal distribution */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /** The tolerance */
  private final double _tol;
  /** The integrator */
  private final Integrator1D<Double, Double> _integrator;

  /**
   * Constructor setting the tolerance and the integrator to default values (1e-9 and Runge-Kutta respectively).
   */
  public ExpectedVarianceStaticReplicationCalculator() {
    _tol = DEFAULT_TOL;
    _integrator = DEFAULT_INTEGRATOR;
  }

  /**
   * Constructor taking a value for the tolerance and using the default integrator (Runge-Kutta)
   * @param tol The tolerance, must be greater than 1e-15 and less than 1e-1
   */
  public ExpectedVarianceStaticReplicationCalculator(final double tol) {
    ArgumentChecker.isTrue(tol > MIN_TOL && tol < MAX_TOL, "tol must be in range {} to {} exclusive. Value given is {}", MIN_TOL, MAX_TOL, tol);
    _tol = tol;
    _integrator = DEFAULT_INTEGRATOR;
  }

  /**
   * Constructor taking an integrator and setting a value for the tolerance (1e-9)
   * @param integrator1d The integrator, not null
   */
  public ExpectedVarianceStaticReplicationCalculator(final Integrator1D<Double, Double> integrator1d) {
    ArgumentChecker.notNull(integrator1d, "null integrator1d");
    _tol = DEFAULT_TOL;
    _integrator = integrator1d;
  }

  /**
   * Constructor taking an integrator and tolerance
   * @param integrator1d The integrator, not null
   * @param tol The tolerance, must be greater than 1e-15 and less than 1e-1
   */
  public ExpectedVarianceStaticReplicationCalculator(final Integrator1D<Double, Double> integrator1d, final double tol) {
    ArgumentChecker.notNull(integrator1d, "null integrator1d");
    ArgumentChecker.isTrue(tol > MIN_TOL && tol < MAX_TOL, "tol must be in range {} to {} exclusive. Value given is {}", MIN_TOL, MAX_TOL, tol);
    _tol = tol;
    _integrator = integrator1d;
  }

  /**
   * Calculate the expected annualised variance using static replication of a log payoff, where the underlying is a diffusion (i.e. no jumps) and the Black volatility
   * surface is parameterised by strike.
   * <p>
   * Note: the Black volatility surface must be fitted externally and be well defined for strikes down to zero ({@link ShiftedLogNormalTailExtrapolation} can be useful for this)
   * @param forward The forward value of the underlying at expiry, must be greater than 0
   * @param expiry The expiry - expected variance is calculated from now (time zero) to expiry, must be greater than 0
   * @param surface A BlackVolatilitySurfaceStrike which is usually fitted from market option prices (on the same underlying as the variance is measured), not null
   * @return The annualised expected variance
   */
  public double getAnnualizedVariance(final double forward, final double expiry, final BlackVolatilitySurfaceStrike surface) {

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
    final double putPart = _integrator.integrate(integrand, 0.0, forward);

    double u = forward * Math.exp(-invNorTol * atmVol * rootT); //initial estimate of upper limit
    double callPart = _integrator.integrate(integrand, forward, u);
    double rem = remainderFunction.evaluate(u);
    double error = rem / callPart;
    while (error > _tol) {
      callPart += _integrator.integrate(integrand, u, 2 * u);
      u *= 2.0;
      rem = remainderFunction.evaluate(u);
      error = rem / putPart;
    }

    return 2 * (putPart + callPart) / expiry;
  }

  /**
   * Calculate the expected annualised variance using static replication of a log payoff, where the underlying is a diffusion (i.e. no jumps) and the Black volatility
   * surface is parameterised by moneyness.
   * <p>
   * Note: the Black volatility surface must be fitted externally and be well defined for strikes down to zero ({@link ShiftedLogNormalTailExtrapolation} can be useful for this)
   * @param expiry The expiry - expected variance is calculated from now (time zero) to expiry, must be greater than zero
   * @param surface A BlackVolatilitySurfaceMoneyness which is usually fitted from market option prices (on the same underlying as the variance is measured), not null
   * @return The annualised expected variance
   */
  public double getAnnualizedVariance(final double expiry, final BlackVolatilitySurfaceMoneyness surface) {
    ArgumentChecker.isTrue(expiry > 0.0, "expiry is {}", expiry);
    ArgumentChecker.notNull(surface, "null surface");
    final BlackVolatilitySurfaceLogMoneyness logMS = BlackVolatilitySurfaceConverter.toLogMoneynessSurface(surface);
    return getAnnualizedVariance(expiry, logMS);
  }

  /**
   * Calculate the expected annualised variance using static replication of a log payoff, where the underlying is a diffusion (i.e. no jumps) and the Black volatility
   * surface is parameterised by log-moneyness.
   * <p>
   * Note: the Black volatility surface must be fitted externally and be well defined for strikes down to zero ({@link ShiftedLogNormalTailExtrapolation} can be useful for this)
   * @param expiry The expiry - expected variance is calculated from now (time zero) to expiry, must be greater than zero
   * @param surface A BlackVolatilitySurfaceLogMoneyness which is usually fitted from market option prices (on the same underlying as the variance is measured), not null
   * @return The annualised expected variance
   */
  public double getAnnualizedVariance(final double expiry, final BlackVolatilitySurfaceLogMoneyness surface) {
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
    double putPart = _integrator.integrate(integrand, l, 0.0);
    double rem = integrand.evaluate(l); //this comes from transforming the strike remainder estimate
    double error = rem / putPart;
    int step = 1;
    while (error > _tol) {
      putPart += _integrator.integrate(integrand, (step + 1) * l, step * l);
      step++;
      rem = integrand.evaluate((step + 1) * l);
      error = rem / putPart;
    }
    putPart += rem; //add on the (very small) remainder estimate otherwise we'll always underestimate variance

    final double u = -invNorTol * atmVol * rootT; //initial estimate of upper limit
    double callPart = _integrator.integrate(integrand, 0.0, u);
    rem = integrand.evaluate(u);
    error = rem / callPart;
    step = 1;
    while (error > _tol) {
      callPart += _integrator.integrate(integrand, step * u, (1 + step) * u);
      step++;
      rem = integrand.evaluate((1 + step) * u);
      error = rem / putPart;
    }

    return 2 * (putPart + callPart) / expiry;
  }

  /**
   * Calculate the expected annualised variance using static replication of a log payoff, where the underlying is a diffusion (i.e. no jumps) and the Black volatility
   * surface is parameterised by delta.
   * <p>
   * Note: the Black volatility surface must be fitted externally and be well defined across the full range of delta (i.e. 0 to 1)
   * @param forward The forward value of the underlying at expiry, must be greater than zero
   * @param expiry The expiry - expected variance is calculated from now (time zero) to expiry, must be greater than zero
   * @param surface A BlackVolatilitySurfaceDelta which is usually fitted from market option prices (on the same underlying as the variance is measured), not null
   * @return The annualised expected variance
   */
  public double getAnnualizedVariance(final double forward, final double expiry, final BlackVolatilitySurfaceDelta surface) {

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
    final double callPart = _integrator.integrate(integrand, _tol, atmfDelta);
    final double putPart = _integrator.integrate(integrand, atmfDelta, 1 - _tol);
    return 2 * (putPart + callPart) / expiry;
  }

  private Function1D<Double, Double> getStrikeIntegrand(final double forward, final double expiry, final BlackVolatilitySurfaceStrike surface) {
    final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
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

        final double strike = BlackFormulaRepository.impliedStrike(delta, true, forward, expiry, vol);
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
