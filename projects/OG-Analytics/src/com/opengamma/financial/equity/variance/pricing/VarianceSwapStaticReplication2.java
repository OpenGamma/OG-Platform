/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance.pricing;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.equity.variance.VarianceSwapDataBundle2;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.StrikeAlgebra;
import com.opengamma.financial.model.volatility.surface.StrikeType;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.Pair;

/**
 * We construct a model independent method to price variance as a static replication
 * of an (in)finite sum of call and put option prices on the underlying.
 * We assume the existence of a smooth function of these option prices / Implied volatilities.
 * The portfolio weighting is 1/k^2. As such, this method is especially sensitive to strike near zero,
 * so we allow the caller to override the Volatilities below a cutoff point (defined as a fraction of the forward rate).
 * We then fit a ShiftedLognormal model to the price of the linear (call) and digital (call spread) at the cutoff.
 * <p>
 * Note: This is not intended to handle large payment delays between last observation date and payment. No convexity adjustment has been applied.<p>
 * Note: Forward variance (forward starting observations) is intended to consider periods beginning more than A_FEW_WEEKS from trade inception
 */
public abstract class VarianceSwapStaticReplication2<T extends StrikeType> {

  protected final StrikeAlgebra<T> _sa = new StrikeAlgebra<T>();

  // TODO CASE Review: Current treatment of forward vol attempts to disallow 'short' periods that may confuse intention of traders.
  // If the entire observation period is less than A_FEW_WEEKS, an error will be thrown.
  // If timeToFirstObs < A_FEW_WEEKS, the pricer will consider the volatility to be from now until timeToLastObs
  private static final double A_FEW_WEEKS = 0.05;

  // Vol Extrapolation
  private final T _cutoffLevel; // Lowest interpolated 'strike', whether fixed value, call or put delta. ShiftedLognormal hits Put(_deltaCutoff)
  private final T _cutoffSpread; // Match derivative near cutoff by also fitting to Put(_deltaCutoff + _deltaSpread)
  private final boolean _cutoffProvided; // False if the above are null
  protected static final double EPS = 1.0e-12;



  // Integration parameters
  private final T _lowerBound;
  private final T _upperBound;
  private final Integrator1D<Double, Double> _integrator;

  /**
   * Default constructor with sensible inputs.
   * A shiftedLognormal distribution is fit to extrapolate below 0.25*forward. It matches the 0.25*F and 0.3*F prices, representing a measure of level and slope
   * @param strikeType TODO
   */



  /**
   * Gets the cutoffProvided.
   * @return the cutoffProvided
   */
  protected boolean isCutoffProvided() {
    return _cutoffProvided;
  }

  /**
   * Gets the cutoffSpread.
   * @return the cutoffSpread
   */
  protected T getCutoffSpread() {
    return _cutoffSpread;
  }

  /**
   * Gets the cutoffLevel.
   * @return the cutoffSpread
   */
  protected T getCutoffLevel() {
    return _cutoffLevel;
  }


  /**
   * Gets the lowerBound.
   * @return the lowerBound
   */
  protected T getLowerBound() {
    return _lowerBound;
  }

  /**
   * Gets the upperBound.
   * @return the upperBound
   */
  protected T getUpperBound() {
    return _upperBound;
  }

  /**
   * Default constructor relies on extrapolation of data bundle's BlackVolatilitySurface to handle low strikes.
   */
  public VarianceSwapStaticReplication2(final T lowerBound, final T upperBound) {
    Validate.notNull(lowerBound, "null lower bound");
    Validate.notNull(upperBound, "null upper bound");
    Validate.isTrue(upperBound.value() > lowerBound.value(), "need upperBound > lowerBound");
    _lowerBound = lowerBound;
    _upperBound = upperBound;
    _integrator = new RungeKuttaIntegrator1D();

    _cutoffLevel = null;
    _cutoffSpread = null;
    _cutoffProvided = false;
  }



  /**
   * Construct a model independent method to price variance as infinite sum of call and put option prices on the underlying.
   * When the cutoff parameters are provided, a shifted lognormal is fit to the two target vols, and used to extrapolate to low strikes.
   * 
   * @param lowerBound Lowest strike / delta in integral. Near zero.
   * @param upperBound Highest strike / delta in integral. Big => just shy of 1.0 in  delta space, multiples of the forward in fixed strike space.
   * @param integrator Integration method
   * @param cutoffType Whether the cutoff is parameterized as STRIKE, CALLDELTA or PUTDELTA
   * @param cutoffLevel First target of shifted lognormal model. Below this, the fit model will extrapolate to produce prices
   * @param cutoffSpread Second target is cutoffLevel + cutoffSpread. Given as fraction of the forward (if STRIKE) else delta value
   */
  public VarianceSwapStaticReplication2(final T lowerBound, final T upperBound, final Integrator1D<Double, Double> integrator,
      final T cutoffLevel, final T cutoffSpread) {
    Validate.notNull(lowerBound, "null lower bound");
    Validate.notNull(upperBound, "null upper bound");
    Validate.isTrue(upperBound.value() > lowerBound.value(), "need upperBound > lowerBound");
    _lowerBound = lowerBound;
    _upperBound = upperBound;
    _integrator = integrator;

    _cutoffLevel = cutoffLevel;
    _cutoffSpread = cutoffSpread;
    if (_cutoffLevel == null || _cutoffSpread == null) {
      Validate.isTrue(_cutoffLevel == null && _cutoffSpread == null,
      "To specify a ShiftedLognormal for left tail extrapolation, a cutoff level and a spread must be provided.");
      _cutoffProvided = false;
    } else {
      _cutoffProvided = true;
    }
  }

  public double presentValue(final VarianceSwap deriv, final VarianceSwapDataBundle2<T> market) {
    Validate.notNull(deriv, "VarianceSwap deriv");
    Validate.notNull(market, "VarianceSwapDataBundle market");

    if (deriv.getTimeToSettlement() < 0) {
      return 0.0; // All payments have been settled
    }

    // Compute contribution from past realizations
    double realizedVar = new RealizedVariance().evaluate(deriv); // Realized variance of log returns already observed
    // Compute contribution from future realizations
    double remainingVar = impliedVariance(deriv, market); // Remaining variance implied by option prices

    // Compute weighting
    double nObsExpected = deriv.getObsExpected(); // Expected number as of trade inception
    double nObsDisrupted = deriv.getObsDisrupted(); // Number of observations missed due to market disruption
    double nObsActual = 0;

    if (deriv.getTimeToObsStart() <= 0) {
      Validate.isTrue(deriv.getObservations().length > 0, "presentValue requested after first observation date, yet no observations have been provided.");
      nObsActual = deriv.getObservations().length - 1; // From observation start until valuation
    }

    double totalVar = realizedVar * (nObsActual / nObsExpected) + remainingVar * (nObsExpected - nObsActual - nObsDisrupted) / nObsExpected;
    double finalPayment = deriv.getVarNotional() * (totalVar - deriv.getVarStrike());

    final double df = market.getDiscountCurve().getDiscountFactor(deriv.getTimeToSettlement());
    return df * finalPayment;

  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterized in 'variance' terms,
   * It is quoted as an annual variance value, hence 1/T * integral(0,T) {sigmaSquared dt} <p>
   * 
   * @param deriv VarianceSwap derivative to be priced
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  public double impliedVariance(final VarianceSwap deriv, final VarianceSwapDataBundle2<T> market) {
    Validate.notNull(deriv, "VarianceSwap deriv");
    Validate.notNull(market, "VarianceSwapDataBundle market");

    final double timeToLastObs = deriv.getTimeToObsEnd();
    final double timeToFirstObs = deriv.getTimeToObsStart();

    Validate.isTrue(timeToFirstObs + A_FEW_WEEKS < timeToLastObs, "timeToLastObs is not sufficiently longer than timeToFirstObs. "
        + "This method is not intended to handle very short periods of volatility." + (timeToLastObs - timeToFirstObs));

    // Compute Variance from spot until last observation
    final double varianceSpotEnd = impliedVarianceFromSpot(timeToLastObs, market);

    // If timeToFirstObs < A_FEW_WEEKS, the pricer will consider the volatility to be from now until timeToLastObs
    final boolean forwardStarting = timeToFirstObs > A_FEW_WEEKS;
    if (!forwardStarting) {
      return varianceSpotEnd;
    }
    final double varianceSpotStart = impliedVarianceFromSpot(timeToFirstObs, market);
    return (varianceSpotEnd * timeToLastObs - varianceSpotStart * timeToFirstObs) / (timeToLastObs - timeToFirstObs);
  }


  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterized in 'variance' terms,
   * It is quoted as an annual variance value, hence 1/T * integral(0,T) {sigmaSquared dt} <p>
   * 
   * @param expiry Time from spot until last observation
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  protected double impliedVarianceFromSpot(final double expiry, final VarianceSwapDataBundle2<T> market) {
    // 1. Unpack Market data
    final double fwd = market.getForwardCurve().getForward(expiry);
    final BlackVolatilitySurface<T> volSurf = market.getVolatilitySurface();

    if (expiry < 1E-4) { // If expiry occurs in less than an hour or so, return 0
      return 0;
    }

    //do the main part of the integral by pricing off the volatility surface
    final Function1D<Double, Double> integrand = getMainIntegrand(expiry, fwd, volSurf);
    Pair<T, T> limits = getIntegralLimits();
    double variance = _integrator.integrate(integrand, limits.getFirst().value(), limits.getSecond().value());

    //if a left tail extrapolation is provided, compute the contribution to the integral from zero to cutOffLevel.
    //If the lowerBound is greater than
    if (_cutoffProvided) {
      Pair<double[], double[]> pars = getTailExtrapolationParameters(fwd, expiry, volSurf);
      double[] ks = pars.getFirst();
      double[] vols = pars.getSecond();
      final double res = getResidual(fwd, expiry, ks, vols);
      variance += res;
    }

    return 2 * variance / expiry;
  }

  protected abstract Pair<T, T> getIntegralLimits();

  protected abstract Pair<double[], double[]> getTailExtrapolationParameters(final double fwd, final double expiry, final BlackVolatilitySurface<T> volSurf);

  protected double getResidual(final double fwd, final double expiry, final double[] ks, final double[] vols) {

    double res;

    // Check for trivial case where cutoff is so low that there's no effective value in the option
    double cutoffPrice = BlackFormulaRepository.price(fwd, ks[0], expiry, vols[0], ks[0] > fwd);
    if (CompareUtils.closeEquals(cutoffPrice, 0)) {
      return 0.0; //i.e. the tail function is never used
    }
    // The typical case - fit a  ShiftedLognormal to the two strike-vol pairs
    final ShiftedLognormalVolModel leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, ks[0], vols[0], ks[1], vols[1]);

    // Now, handle behaviour near zero strike. ShiftedLognormalVolModel has non-zero put price for zero strike.
    // What we do is to find the strike, k_min, at which f(k) = p(k)/k^2 begins to blow up, by finding the minimum of this function, k_min
    // then setting f(k) = f(k_min) for k < k_min. This ensures the implied volatility and the integrand are well behaved in the limit k -> 0.
    final Function1D<Double, Double> shiftedLnIntegrand = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double strike) {
        return leftExtrapolator.priceFromFixedStrike(strike) / (strike * strike);
      }
    };
    final double kMin = new BrentMinimizer1D().minimize(shiftedLnIntegrand, EPS, EPS, ks[0]);
    final double fMin = shiftedLnIntegrand.evaluate(kMin);
    res = fMin * kMin; //the (hopefully) very small rectangular bit between zero and kMin

    res += _integrator.integrate(shiftedLnIntegrand, kMin, ks[0]);

    return res;
  }

  protected abstract Function1D<Double, Double> getMainIntegrand(final double expiry, final double fwd, final BlackVolatilitySurface<T> volSurf);

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterised in vol/vega terms.
   * This is an estimate of annual Lognormal (Black) volatility
   * 
   * @param deriv VarianceSwap derivative to be priced
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  public double impliedVolatility(final VarianceSwap deriv, final VarianceSwapDataBundle2<T> market) {
    final double sigmaSquared = impliedVariance(deriv, market);
    return Math.sqrt(sigmaSquared);
  }

}
