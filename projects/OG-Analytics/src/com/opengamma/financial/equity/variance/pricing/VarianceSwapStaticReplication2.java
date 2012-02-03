/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance.pricing;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.equity.variance.VarianceSwapDataBundle2;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.model.volatility.surface.StrikeAlgebra;
import com.opengamma.financial.model.volatility.surface.StrikeType;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;

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
  protected final T _cutoffLevel; // Lowest interpolated 'strike', whether fixed value, call or put delta. ShiftedLognormal hits Put(_deltaCutoff)
  protected final T _cutoffSpread; // Match derivative near cutoff by also fitting to Put(_deltaCutoff + _deltaSpread)
  protected final boolean _cutoffProvided; // False if the above are null
  protected static final double EPS = 1.0e-12;

  // Integration parameters
  protected final double _lowerBound; // ~ zero
  protected final double _upperBound; // ~infinity in strike space, ~1.0 in delta
  protected final Integrator1D<Double, Double> _integrator;

  /**
   * Default constructor with sensible inputs.
   * A shiftedLognormal distribution is fit to extrapolate below 0.25*forward. It matches the 0.25*F and 0.3*F prices, representing a measure of level and slope
   * @param strikeType TODO
   */

  /**
   * Default constructor relies on extrapolation of data bundle's BlackVolatilitySurface to handle low strikes.
   */
  public VarianceSwapStaticReplication2(final double lowerBound, final double upperBound) {

    Validate.isTrue(upperBound>lowerBound, "need upperBound > lowerBound");
    _lowerBound = lowerBound;
    _upperBound = upperBound;
    _integrator = new RungeKuttaIntegrator1D();

    _cutoffLevel = null;
    _cutoffSpread = null;
    _cutoffProvided = false;
  }

  //  public VarianceSwapStaticReplication2() {
  //    Validate.notNull(strikeType, "Please provide a StrikeParameterization. You may find this from your BlackVolatilitySurface.getStrikeParameterisation()");
  //    switch (strikeType) {
  //
  //      case STRIKE:
  //        _lowerBound = 0.0 + EPS;
  //        _upperBound = 10.0; // Multiple of forward. Integrand falls off quickly.
  //        _integrator = new RungeKuttaIntegrator1D();
  //
  //        _cutoffType = strikeType;
  //        _cutoffLevel = 0.25;
  //        _cutoffSpread = 0.05;
  //        _cutoffProvided = true;
  //        break;
  //
  //      case PUTDELTA:
  //        _lowerBound = 0.0 + EPS; // TODO Confirm this doesn't fall over
  //        _upperBound = 1.0 - EPS;
  //        _integrator = new RungeKuttaIntegrator1D();
  //
  //        _cutoffType = strikeType;
  //        _cutoffLevel = 0.1;
  //        _cutoffSpread = 0.001;
  //        _cutoffProvided = true;
  //        break;
  //
  //      case CALLDELTA:
  //        //Validate.isTrue(false, "Finish CALLDELTA Constructor");
  //        _lowerBound = 0.0 + EPS; // TODO Confirm this doesn't fall over
  //        _upperBound = 1.0 - EPS;
  //        _integrator = new RungeKuttaIntegrator1D();
  //
  //        _cutoffType = strikeType;
  //        _cutoffLevel = 0.9;
  //        _cutoffSpread = -0.001;
  //        _cutoffProvided = true;
  //        break;
  //
  //      default:
  //        throw new IllegalArgumentException("Unhandled StrikeParameterisation.");
  //    }
  //  }

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
  public VarianceSwapStaticReplication2(final double lowerBound, final double upperBound, final Integrator1D<Double, Double> integrator,
      final T cutoffLevel, final T cutoffSpread) {

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
  protected abstract double impliedVarianceFromSpot(final double expiry, final VarianceSwapDataBundle2<T> market);

  //
  //    final ProbabilityDistribution<Double> ndist = new NormalDistribution(0, 1);
  //
  //    // 1. Unpack Market data
  //    final double fwd = market.getForwardCurve().getForward(expiry);
  //    final BlackVolatilitySurface<T> volSurf = market.getVolatilitySurface();
  //
  //    if (expiry < 1E-4) { // If expiry occurs in less than an hour or so, return 0
  //      return 0;
  //    }
  //
  //    /******* Handle strike parameterisation cases separately *******/
  //
  //    /******* Case 1: BlackVolatilityFixedStrikeSurface *******/
  //
  //    // 2. Fit the leftExtrapolator to the two target strikes, if provided
  //    final ShiftedLognormalVolModel leftExtrapolator;
  //    final double cutoffStrike = _cutoffProvided ? volSurf.getAbsoluteStrike(expiry, _cutoffLevel) : 0.0;
  //    final Double lowerBoundOfExtrapolator;
  //    final Double lowerBoundValue;
  //
  //    if (_cutoffProvided) {
  //      T temp = _sa.add(_cutoffSpread, _cutoffLevel);
  //      final double secondStrike = volSurf.getAbsoluteStrike(expiry, temp);
  //      final double cutoffVol = volSurf.getVolatility(expiry, _cutoffLevel);
  //      final double secondVol = volSurf.getVolatility(expiry, temp);
  //
  //      // Check for trivial case where cutoff is so low that there's no effective value in the option
  //      double cutoffPrice = BlackFormulaRepository.price(fwd, cutoffStrike, expiry, cutoffVol, cutoffStrike > fwd);
  //      if (CompareUtils.closeEquals(cutoffPrice, 0)) {
  //        leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, 0.0, -1.0e6); // Model will price every strike at zero
  //        lowerBoundOfExtrapolator = 0.0;
  //        lowerBoundValue = 0.0;
  //      } else { // The typical case
  //        leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, cutoffStrike, cutoffVol, secondStrike, secondVol);
  //
  //        // Now, handle behaviour near zero strike. ShiftedLognormalVolModel has non-zero put price for zero strike.
  //        // What we do is to find the strike, k_min, at which p(k)/k^2 begins to blow up, and fit a quadratic, p(k) = p(k_min) * k^2 / k_min^2
  //        // This ensures the implied volatility and the integrand are well behaved in the limit k -> 0.
  //        final Function1D<Double, Double> shiftedLnIntegrand = new Function1D<Double, Double>() {
  //          @Override
  //          public Double evaluate(Double strike) {
  //            return 2.0 * leftExtrapolator.priceFromFixedStrike(strike) / (strike * strike);
  //          }
  //        };
  //        lowerBoundOfExtrapolator = new BrentMinimizer1D().minimize(shiftedLnIntegrand, EPS, EPS, cutoffStrike);
  //        lowerBoundValue = shiftedLnIntegrand.evaluate(lowerBoundOfExtrapolator);
  //      }
  //    } else {
  //      leftExtrapolator = null;
  //      lowerBoundOfExtrapolator = 0.0;
  //      lowerBoundValue = 0.0;
  //    }
  //
  //    // 3. Define the hedging portfolio: The position to hold in each otmOption(k) = 2 / strike^2,
  //    //                                       where otmOption is a call if k > fwd and a put otherwise
  //    final Function1D<Double, Double> otmOptionAndWeight = new Function1D<Double, Double>() {
  //      @SuppressWarnings("synthetic-access")
  //      @Override
  //      public Double evaluate(final Double strike) {
  //        final boolean isCall = strike > fwd;
  //        final double weight = 2 / (strike * strike);
  //        if (_cutoffProvided && strike < cutoffStrike) { // Extrapolate with ShiftedLognormal
  //          if (strike >= lowerBoundOfExtrapolator) {
  //            return leftExtrapolator.priceFromFixedStrike(strike) * weight;
  //          }
  //          return lowerBoundValue;
  //        } // Interp/Extrap directly on volSurf
  //        final double vol = volSurf.getVolatility(expiry, strike);
  //        final double otmPrice = BlackFormulaRepository.price(fwd, strike, expiry, vol, isCall);
  //        return otmPrice * weight;
  //      }
  //    };
  //
  //    //      // 4. Compute variance hedge by integrating positions over all strikes
  //    double variance = _integrator.integrate(otmOptionAndWeight, _lowerBound * fwd, _upperBound * fwd);
  //    return variance / expiry;
  //
  //      /******* Case 2: BlackVolatilityDeltaSurface *******/
  //    } else if (volSurf.getStrikeParameterisation() == StrikeParameterization.CALLDELTA || volSurf.getStrikeParameterisation() == StrikeParameterization.PUTDELTA) {
  //
  //      final boolean axisOfCalls = ((BlackVolatilityDeltaSurface) volSurf).strikeAxisRepresentsCalls();
  //      final ShiftedLognormalVolModel leftExtrapolator;
  //      final double cutoffStrike;
  //
  //      if (_cutoffProvided) {
  //        // 2. Fit the leftExtrapolator to the two target deltas, if provided
  //
  //        final double cutoffVol = volSurf.getVolatility(expiry, _cutoffLevel);
  //        final BlackFormula black = new BlackFormula(fwd, fwd, expiry, cutoffVol, null, axisOfCalls);
  //        cutoffStrike = black.computeStrikeImpliedByForwardDelta(_cutoffLevel, axisOfCalls);
  //
  //        final double secondVol = volSurf.getVolatility(expiry, _cutoffLevel + _cutoffSpread);
  //        black.setLognormalVol(secondVol);
  //        final double secondStrike = black.computeStrikeImpliedByForwardDelta(_cutoffLevel + _cutoffSpread, axisOfCalls);
  //
  //        // Check for trivial case where cutoff is so low that there's no effective value in the option
  //        double cutoffPrice = volSurf.getForwardPrice(expiry, _cutoffLevel, fwd, cutoffStrike > fwd);
  //        if (CompareUtils.closeEquals(cutoffPrice, 0)) {
  //          leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, 0.0, -1.0e6); // Model will price every strike at zero
  //        } else {
  //          // Typical case
  //          leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, cutoffStrike, cutoffVol, secondStrike, secondVol);
  //        }
  //      } else {
  //        cutoffStrike = 0.0;
  //        leftExtrapolator = null;
  //      }
  //      // 3. Define the hedging portfolio : The position to hold in each otmOption(k) = 2 / strike^2,
  //      //                                    where otmOption is a call if k > fwd and a put otherwise
  //          final Function1D<Double, Double> otmOptionAndWeight = new Function1D<Double, Double>() {
  //            @SuppressWarnings("synthetic-access")
  //            @Override
  //            public Double evaluate(final Double delta) {
  //
  //              final double vol = volSurf.getVolatility(expiry, delta);
  //              final BlackFormula black = new BlackFormula(fwd, fwd, expiry, vol, null, axisOfCalls);
  //
  //              final double strike = black.computeStrikeImpliedByForwardDelta(delta, axisOfCalls);
  //
  //              black.setStrike(strike);
  //              black.setIsCall(strike > fwd);
  //
  //              // 2/k^2 => the following when we switch integration variable to delta
  //              double weight = 2 * vol * Math.sqrt(expiry) / strike;
  //              weight /= ndist.getPDF(ndist.getInverseCDF(delta));
  //              double otmPrice;
  //              if (_cutoffProvided && strike < cutoffStrike) { // Extrapolate with ShiftedLognormal
  //                otmPrice = leftExtrapolator.priceFromFixedStrike(strike);
  //              } else {
  //                otmPrice = black.computePrice();
  //              }
  //              return weight * otmPrice;
  //            }
  //          };
  //
  //      // 4. Compute variance hedge by integrating positions over all strikes
  //      double variance = _integrator.integrate(otmOptionAndWeight, _lowerBound, _upperBound);
  //      return variance / expiry;
  //
  //    } else {
  //      throw new IllegalArgumentException("Unexpected cutoffType. Allowed values of StrikeParameterisation are STRIKE and DELTA.");
  //    }
  //}

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
