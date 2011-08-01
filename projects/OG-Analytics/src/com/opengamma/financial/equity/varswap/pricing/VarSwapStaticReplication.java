/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.pricing;

import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.financial.model.volatility.BlackFormula;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityDeltaSurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityFixedStrikeSurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.CompareUtils;

import org.apache.commons.lang.Validate;

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
public class VarSwapStaticReplication {

  // TODO CASE Review: Current treatment of forward vol attempts to disallow 'short' periods that may confuse intention of traders.
  // If the entire observation period is less than A_FEW_WEEKS, an error will be thrown.
  // If timeToFirstObs < A_FEW_WEEKS, the pricer will consider the volatility to be from now until timeToLastObs 
  private static final double A_FEW_WEEKS = 0.05;

  // Vol Extrapolation 
  private final StrikeParameterisation _cutoffType; // Whether strike targets are specified as Absolute Strike or Spot Delta levels
  private final Double _cutoffLevel; // Lowest interpolated strike. ShiftedLognormal hits Put(_deltaCutoff)
  private final Double _cutoffSpread; // Match derivative near cutoff by also fitting to Put(_deltaCutoff + _deltaSpread)
  private final boolean _cutoffProvided; // False if the above are null
  private static final double EPS = 1.0e-12;

  /**
   * Strike prices in Volatility surfaces may be parameterised in a number of ways. To clarify, we introduce this enum.
   * Types available: STRIKE, DELTA
   */
  public enum StrikeParameterisation {
    /** Strike prices, absolute levels */
    STRIKE,
    /** Implied spot delta  */
    DELTA
  };

  // Integration parameters
  private final double _lowerBound; // ~ zero
  private final double _upperBound; // ~infinity in strike space, ~1.0 in delta
  private final Integrator1D<Double, Double> _integrator;

  /**
   * Default constructor with sensible inputs.
   * A shiftedLognormal distribution is fit to extrapolate below 0.25*forward. It matches the 0.25*F and 0.3*F prices, representing a measure of level and slope 
   * @param strikeType TODO
   */

  /**
   * Default constructor relies on extrapolation of data bundle's BlackVolatilitySurface to handle low strikes.
   */
  public VarSwapStaticReplication() {

    _lowerBound = EPS;
    _upperBound = 20.0;
    _integrator = new RungeKuttaIntegrator1D();

    _cutoffType = null;
    _cutoffLevel = null;
    _cutoffSpread = null;
    _cutoffProvided = false;
  }

  public VarSwapStaticReplication(StrikeParameterisation strikeType) {
    Validate.notNull(strikeType, "Please provide a StrikeParameterization. You may find this from your BlackVolatilitySurface.getStrikeParameterisation()");
    switch (strikeType) {

      case DELTA:
        _lowerBound = 0.0 + EPS; // TODO Confirm this doesn't fall over
        _upperBound = 1.0 - EPS;
        _integrator = new RungeKuttaIntegrator1D();

        _cutoffType = strikeType;
        _cutoffLevel = 0.1;
        _cutoffSpread = 0.001;
        _cutoffProvided = true;
        break;

      case STRIKE:
        _lowerBound = 0.0 + EPS;
        _upperBound = 10.0; // Multiple of forward. Integrand falls off quickly.
        _integrator = new RungeKuttaIntegrator1D();

        _cutoffType = strikeType;
        _cutoffLevel = 0.25;
        _cutoffSpread = 0.05;
        _cutoffProvided = true;
        break;

      default:
        throw new IllegalArgumentException("Unhandled StrikeParameterisation.");
    }
  }

  /**
   * TODO WRITE THIS UP
   * @param lowerBound
   * @param upperBound
   * @param integrator
   * @param cutoffType
   * @param cutoffLevel
   * @param cutoffSpread
   */
  public VarSwapStaticReplication(final double lowerBound, final double upperBound, final Integrator1D<Double, Double> integrator,
        final StrikeParameterisation cutoffType, final Double cutoffLevel, final Double cutoffSpread) {

    _lowerBound = lowerBound;
    _upperBound = upperBound;
    _integrator = integrator;

    _cutoffType = cutoffType;
    _cutoffLevel = cutoffLevel;
    _cutoffSpread = cutoffSpread;
    if (_cutoffType == null || _cutoffLevel == null || _cutoffSpread == null) {
      Validate.isTrue(_cutoffType == null && _cutoffLevel == null && _cutoffSpread == null,
          "To specify a ShiftedLognormal for left tail extrapolation, all three of a cutoff type, a cutoff level and a spread must be provided.");
      _cutoffProvided = false;
    } else {
      _cutoffProvided = true;
    }
  }

  public double presentValue(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
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
  public double impliedVariance(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
    Validate.notNull(deriv, "VarianceSwap deriv");
    Validate.notNull(market, "VarianceSwapDataBundle market");

    final double timeToLastObs = deriv.getTimeToObsEnd();
    final double timeToFirstObs = deriv.getTimeToObsStart();

    Validate.isTrue(timeToFirstObs + A_FEW_WEEKS < timeToLastObs, "timeToLastObs is not sufficiently longer than timeToFirstObs. "
        + "This method is not intended to handle very short periods of volatility.");

    // Compute Variance from spot until last observation
    final double varianceSpotEnd = impliedVarianceFromSpot(timeToLastObs, market);

    // If timeToFirstObs < A_FEW_WEEKS, the pricer will consider the volatility to be from now until timeToLastObs
    final boolean forwardStarting = timeToFirstObs > A_FEW_WEEKS;
    if (!forwardStarting) {
      return varianceSpotEnd;
    } else {
      final double varianceSpotStart = impliedVarianceFromSpot(timeToFirstObs, market);
      return (varianceSpotEnd * timeToLastObs - varianceSpotStart * timeToFirstObs) / (timeToLastObs - timeToFirstObs);
    }
  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterized in 'variance' terms,
   * It is quoted as an annual variance value, hence 1/T * integral(0,T) {sigmaSquared dt} <p>
   * 
   * @param expiry Time from spot until last observation
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap. 
   */
  private double impliedVarianceFromSpot(final double expiry, final VarianceSwapDataBundle market) {

    final ProbabilityDistribution<Double> ndist = new NormalDistribution(0, 1);

    // 1. Unpack Market data 
    final double fwd = market.getForwardUnderlying();
    final BlackVolatilitySurface volSurf = market.getVolatilitySurface();

    if (_cutoffType != null) {
      if (volSurf instanceof BlackVolatilityDeltaSurface) {
        Validate.isTrue(_cutoffType == StrikeParameterisation.DELTA,
            "Left Tail extrapolation type is not consistent with Vol Surface, BlackVolatilityDeltaSurface. The cutoff must be of type DELTA.");
      } else if (volSurf instanceof BlackVolatilityFixedStrikeSurface) {
        Validate.isTrue(_cutoffType == StrikeParameterisation.STRIKE,
            "Left Tail extrapolation type is not consistent with Vol Surface, BlackVolatilityFixedStrikeSurface. The cutoff must be of type STRIKE.");
      } else {
        throw new IllegalArgumentException("The BlackVolatilitySurface in the VarianceSwapDataBundle must be of type BlackVolatilityFixedStrikeSurface or BlackVolatilityDeltaSurface.");
      }

    }
    if (expiry < 1E-4) { // If expiry occurs in less than an hour or so, return 0
      return 0;
    }

    /******* Handle strike parameterisation cases separately *******/

    /******* Case 1: BlackVolatilityFixedStrikeSurface *******/
    if (volSurf instanceof BlackVolatilityFixedStrikeSurface) {

      // 2. Fit the leftExtrapolator to the two target strikes, if provided
      final ShiftedLognormalVolModel leftExtrapolator;
      final double cutoffStrike = _cutoffProvided ? _cutoffLevel * fwd : 0.0;
      final Double lowerBoundOfExtrapolator;
      final Double lowerBoundValue;

      if (_cutoffProvided) {
        final double secondStrike = (_cutoffLevel + _cutoffSpread) * fwd;
        final double cutoffVol = volSurf.getVolatility(expiry, cutoffStrike);
        final double secondVol = volSurf.getVolatility(expiry, secondStrike);

        // Check for trivial case where cutoff is so low that there's no effective value in the option 
        double cutoffPrice = volSurf.getForwardPrice(expiry, cutoffStrike, fwd, cutoffStrike > fwd);
        if (CompareUtils.closeEquals(cutoffPrice, 0)) {
          leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, 0.0, -1.0e6); // Model will price every strike at zero
          lowerBoundOfExtrapolator = 0.0;
          lowerBoundValue = 0.0;
        } else { // The typical case
          leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, cutoffStrike, cutoffVol, secondStrike, secondVol);

          // Now, handle behaviour near zero strike. ShiftedLognormalVolModel has non-zero put price for zero strike. 
          // What we do is to find the strike, k_min, at which p(k)/k^2 begins to blow up, and fit a quadratic, p(k) = p(k_min) * k^2 / k_min^2
          // This ensures the implied volatilit and the integrand are well behaved in the limit k -> 0.
          final Function1D<Double, Double> shiftedLnIntegrand = new Function1D<Double, Double>() {
            @Override
            public Double evaluate(Double strike) {
              return 2.0 * leftExtrapolator.priceFromFixedStrike(strike) / (strike * strike);
            }
          };
          lowerBoundOfExtrapolator = new BrentMinimizer1D().minimize(shiftedLnIntegrand, EPS, EPS, cutoffStrike);
          lowerBoundValue = shiftedLnIntegrand.evaluate(lowerBoundOfExtrapolator);

          System.err.println("Minimum of otmOptionAndWeight = " + lowerBoundValue); // TODO Remove
          System.err.println("Strike at min of otmOptionAndWeight = " + lowerBoundOfExtrapolator); // TODO Remove

        }

      } else {
        leftExtrapolator = null;
        lowerBoundOfExtrapolator = 0.0;
        lowerBoundValue = 0.0;
      }

      // 3. Define the hedging portfolio: The position to hold in each otmOption(k) = 2 / strike^2,
      //                                       where otmOption is a call if k > fwd and a put otherwise
      final Function1D<Double, Double> otmOptionAndWeight = new Function1D<Double, Double>() {
        @Override
        public Double evaluate(final Double strike) {

          final boolean isCall = strike > fwd;
          final double weight = 2 / (strike * strike);
          if (_cutoffProvided && strike < cutoffStrike) { // Extrapolate with ShiftedLognormal
            if (strike >= lowerBoundOfExtrapolator) {
              System.err.println(strike + "," + leftExtrapolator.priceFromFixedStrike(strike) * weight); // TODO
              return leftExtrapolator.priceFromFixedStrike(strike) * weight;
            } else {
              System.err.println(strike + "," + lowerBoundValue);
              return lowerBoundValue;
            }
          } else { // Interp/Extrap directly on volSurf
            final double vol = volSurf.getVolatility(expiry, strike);
            final double otmPrice = new BlackFormula(fwd, strike, expiry, vol, null, isCall).computePrice();
            System.err.println(strike + "," + weight * otmPrice);
            return otmPrice * weight;
          }

        }
      };

      /***********  TESTING TODO REMOVE *********
      final Function1D<Double, Double> IntNeg = new Function1D<Double, Double>() {
        @Override
        public Double evaluate(Double strike) {
          return -1 * otmOptionAndWeight.evaluate(strike);
        }
      };
      BrentMinimizer1D MIN = new BrentMinimizer1D();
      double otmKMin = new BrentMinimizer1D().minimize(otmOptionAndWeight, EPS, EPS, fwd);
      System.err.println("Minimum of otmOptionAndWeight = " + otmKMin);
      System.err.println("Value at Minimum = " + otmOptionAndWeight.evaluate(new Double(otmKMin)));
      System.err.println("Maximum of otmOptionAndWeight = " + new BrentMinimizer1D().minimize(IntNeg, EPS, EPS, 500));

      System.err.println("Strike,ImpliedVol");
      for (int i = 0; i <= 15; i++) {
        double k;
        if (i <= 10) {
          k = EPS + i;
        } else {
          k = 10.0 + 10 * (i - 10);
        }
        Double weighted = otmOptionAndWeight.evaluate(new Double(k));
        double pShift = weighted * k * k * 0.5;
        double lnImpVol = new BlackFormula(fwd, k, expiry, null, pShift, false).computeImpliedVolatility();
        double bsVol = volSurf.getVolatility(expiry, k);
        double weightedBS = new BlackFormula(fwd, k, expiry, bsVol, null, false).computePrice() / (k * k * 0.5);
        System.err.println(k + "," + lnImpVol + "," + weighted + "," + bsVol + "," + weightedBS);

      }
      *******************************************/

      // 4. Compute variance hedge by integrating positions over all strikes
      double variance = _integrator.integrate(otmOptionAndWeight, _lowerBound * fwd, _upperBound * fwd);
      return variance / expiry;

      /******* Case 2: BlackVolatilityDeltaSurface *******/
    } else if (volSurf instanceof BlackVolatilityDeltaSurface) {

      final boolean axisOfCalls = ((BlackVolatilityDeltaSurface) volSurf).strikeAxisRepresentsCalls();

      final ShiftedLognormalVolModel leftExtrapolator;
      final Double lowerBoundOfExtrapolator;
      final Double lowerBoundValue;

      if (_cutoffProvided) {
        // 2. Fit the leftExtrapolator to the two target deltas, if provided

        final double cutoffVol = volSurf.getVolatility(expiry, _cutoffLevel);
        final BlackFormula black = new BlackFormula(fwd, fwd, expiry, cutoffVol, null, axisOfCalls);
        final double cutoffStrike = black.computeStrikeImpliedByForwardDelta(_cutoffLevel, axisOfCalls);

        final double secondVol = volSurf.getVolatility(expiry, _cutoffLevel + _cutoffSpread);
        black.setLognormalVol(secondVol);
        final double secondStrike = black.computeStrikeImpliedByForwardDelta(_cutoffLevel + _cutoffSpread, axisOfCalls);

        //****************************************
        final double[] deltas = new double[] {0.01, .1, .25, .5, .75, .99 };
        final double[] strikes = new double[6];
        for (int i = 0; i < 6; i++) {
          final double vol = volSurf.getVolatility(expiry, deltas[i]);
          final BlackFormula bs = new BlackFormula(fwd, fwd, expiry, vol, null, axisOfCalls);
          strikes[i] = bs.computeStrikeImpliedByForwardDelta(deltas[i], axisOfCalls);
          System.err.println(strikes[i]);
        }
        //****************************************

        // Check for trivial case where cutoff is so low that there's no effective value in the option 
        double cutoffPrice = volSurf.getForwardPrice(expiry, _cutoffLevel, fwd, cutoffStrike > fwd);
        if (CompareUtils.closeEquals(cutoffPrice, 0)) {
          leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, 0.0, -1.0e6); // Model will price every strike at zero
        } else {
          // Typical case
          leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, cutoffStrike, cutoffVol, secondStrike, secondVol);

          /*******
          // Now, handle behaviour near zero strike. ShiftedLognormalVolModel has non-zero put price for zero strike. 
          // What we do is to find the strike, k_min, at which p(k)/k^2 begins to blow up, and fit a quadratic, p(k) = p(k_min) * k^2 / k_min^2
          // This ensures the implied volatilit and the integrand are well behaved in the limit k -> 0.
          final Function1D<Double, Double> shiftedLnIntegrand = new Function1D<Double, Double>() {
            @Override
            public Double evaluate(Double delta) {
              final double vol = volSurf.getVolatility(expiry, delta);
              final BlackFormula black = new BlackFormula(fwd, fwd, expiry, vol, null, axisOfCalls);

              final double strike = black.computeStrikeImpliedByForwardDelta(delta, axisOfCalls);
              
              return 2.0 * leftExtrapolator.priceFromFixedStrike(strike) / (strike * strike);
            }
          };
          
          lowerBoundOfExtrapolator = new BrentMinimizer1D().minimize(shiftedLnIntegrand, EPS, EPS, cutoffStrike);
          lowerBoundValue = shiftedLnIntegrand.evaluate(lowerBoundOfExtrapolator);

          System.err.println("Minimum of otmOptionAndWeight = " + lowerBoundValue); // TODO Remove
          System.err.println("Strike at min of otmOptionAndWeight = " + lowerBoundOfExtrapolator); // TODO Remove
          ***********/
        }
      } else {
        leftExtrapolator = null;
      }
      // 3. Define the hedging portfolio : The position to hold in each otmOption(k) = 2 / strike^2, 
      //                                    where otmOption is a call if k > fwd and a put otherwise
      final Function1D<Double, Double> otmOptionAndWeight = new Function1D<Double, Double>() {
        @Override
        public Double evaluate(final Double delta) {

          final double vol = volSurf.getVolatility(expiry, delta);
          final BlackFormula black = new BlackFormula(fwd, fwd, expiry, vol, null, axisOfCalls);

          final double strike = black.computeStrikeImpliedByForwardDelta(delta, axisOfCalls);

          black.setStrike(strike);
          black.setIsCall(strike > fwd);

          // 2/k^2 => the following when we switch integration variable to delta 
          double weight = 2 * vol * Math.sqrt(expiry) / strike;
          weight /= ndist.getPDF(ndist.getInverseCDF(delta));
          double otmPrice;
          if (_cutoffProvided && delta < _cutoffLevel) { // Extrapolate with ShiftedLognormal
            otmPrice = leftExtrapolator.priceFromFixedStrike(strike); // TODO Confirm that you're pricing the right option, whether call or put
          } else {
            otmPrice = black.computePrice();
          }
          System.err.println(delta + "," + strike + "," + weight * otmPrice);
          return weight * otmPrice;
        }
      };

      // 4. Compute variance hedge by integrating positions over all strikes
      double variance = _integrator.integrate(otmOptionAndWeight, Math.min(_lowerBound, 1.0 - EPS), _upperBound); // FIXME
      return variance / expiry;

    } else {
      throw new IllegalArgumentException("Unexpected cutoffType. Allowed values of StrikeParameterisation are STRIKE and DELTA.");
    }
  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterized in vol/vega terms.
   * This is an estimate of annual Lognormal (Black) volatility 
   * 
   * @param deriv VarianceSwap derivative to be priced
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap. 
   */
  public double impliedVolatility(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
    final double sigmaSquared = impliedVariance(deriv, market);
    return Math.sqrt(sigmaSquared);
  }
}
