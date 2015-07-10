/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceLogMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceVisitor;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * We construct a model independent method to price variance as a static replication
 * of an (in)finite sum of call and put option prices on the underlying.
 * We assume the existence of a smooth function of these option prices / implied volatilities.
 * The portfolio weighting is 1/k^2. As such, this method is especially sensitive to strike near zero.
 * <p>
 * Note: This is not intended to handle large payment delays between last observation date and payment. No convexity adjustment has been applied.
 */
public class VarianceSwapStaticReplication {
  /** Calculates the expected annualised variance of an instrument with a log payoff */
  private final ExpectedVarianceStaticReplicationCalculator _cal;

  /**
   * // * Constructor that uses the default values for expected variance calculations.
   * //
   */
  public VarianceSwapStaticReplication() {
    _cal = new ExpectedVarianceStaticReplicationCalculator();
  }

  /**
   * @param tolerance The tolerance of the expected variance calculations
   */
  public VarianceSwapStaticReplication(final double tolerance) {
    _cal = new ExpectedVarianceStaticReplicationCalculator(tolerance);
  }

  /**
   * @param integrator The integrator to be used in expected variance calculations, not null
   */
  public VarianceSwapStaticReplication(final Integrator1D<Double, Double> integrator) {
    _cal = new ExpectedVarianceStaticReplicationCalculator(integrator);
  }

  /**
   * @param integrator The integrator to be used in expected variance calculations, not null
   * @param tolerance The tolerance of the expected variance calculations
   */
  public VarianceSwapStaticReplication(final Integrator1D<Double, Double> integrator, final double tolerance) {
    _cal = new ExpectedVarianceStaticReplicationCalculator(integrator, tolerance);
  }

  /**
   * Calculates the present value of a variance swap using static replication
   * @param deriv The variance swap, not null
   * @param market Bundle containing market data, not null
   * @return The present value
   */
  public double presentValue(final VarianceSwap deriv, final StaticReplicationDataBundle market) {
    ArgumentChecker.notNull(deriv, "VarianceSwap deriv");
    ArgumentChecker.notNull(market, "EquityOptionDataBundle market");

    if (deriv.getTimeToSettlement() < 0) {
      return 0.0; // All payments have been settled
    }

    // Compute contribution from past realizations
    final double realizedVar = new RealizedVariance().evaluate(deriv); // Realized variance of log returns already observed
    // Compute contribution from future realizations
    final double remainingVar = expectedVariance(deriv, market); // Remaining variance implied by option prices

    // Compute weighting
    int nObsExpected = deriv.getObsExpected(); // Expected number of observed as of trade inception
    int nObsDisrupted = deriv.getObsDisrupted(); // Number of observations missed due to market disruption

    double totalVar = 0.0;
    if (deriv.getTimeToObsStart() > 0) { // no observations have been made
      totalVar = remainingVar;
    } else {
      ArgumentChecker.isTrue(deriv.getObservations().length > 0, "presentValue requested after first observation date, yet no observations have been provided.");
      int nObsActual = deriv.getObservations().length; // From observation start until valuation
      totalVar = (realizedVar * (nObsActual - 1) + remainingVar * (nObsExpected - nObsActual - nObsDisrupted)) / (nObsExpected - 1);
    }

    final double finalPayment = deriv.getVarNotional() * (totalVar - deriv.getVarStrike());

    final double df = market.getDiscountCurve().getDiscountFactor(deriv.getTimeToSettlement());
    return df * finalPayment;

  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterised in 'variance' terms,
   * It is quoted as an annual variance value, hence 1/T * integral(0,T) {sigmaSquared dt}
   * <p>
   * 
   * @param deriv VarianceSwap derivative to be priced
   * @param market EquityOptionDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  public double expectedVariance(final VarianceSwap deriv, final StaticReplicationDataBundle market) {

    validateData(deriv, market);

    final double timeToLastObs = deriv.getTimeToObsEnd();
    if (timeToLastObs <= 0) { // expired swap returns 0 variance
      return 0.0;
    }

    final double timeToFirstObs = deriv.getTimeToObsStart();

    // Compute Variance from spot until last observation
    final double varianceSpotEnd = expectedVarianceFromSpot(timeToLastObs, market);

    // If timeToFirstObs= 0.0, the pricer will consider the volatility to be from now until timeToLastObs
    final boolean forwardStarting = timeToFirstObs > 0.0;
    if (!forwardStarting) {
      return varianceSpotEnd;
    }
    final double varianceSpotStart = expectedVarianceFromSpot(timeToFirstObs, market);
    return (varianceSpotEnd * timeToLastObs - varianceSpotStart * timeToFirstObs) / (timeToLastObs - timeToFirstObs);
  }

  private void validateData(final VarianceSwap deriv, final StaticReplicationDataBundle market) {
    ArgumentChecker.notNull(deriv, "VarianceSwap deriv");
    ArgumentChecker.notNull(market, "EquityOptionDataBundle market");

    final double timeToLastObs = deriv.getTimeToObsEnd();
    final double timeToFirstObs = deriv.getTimeToObsStart();

    ArgumentChecker.isTrue(timeToFirstObs < timeToLastObs, "timeToLastObs is not sufficiently longer than timeToFirstObs");
  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterized in 'variance' terms,
   * It is quoted as an annual variance value, hence 1/T * integral(0,T) {sigmaSquared dt}
   * <p>
   * 
   * @param expiry Time from spot until last observation
   * @param market EquityOptionDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  protected double expectedVarianceFromSpot(final double expiry, final StaticReplicationDataBundle market) {
    // 1. Unpack Market data
    final double fwd = market.getForwardCurve().getForward(expiry);
    final BlackVolatilitySurface<?> volSurf = market.getVolatilitySurface();

    final VarianceCalculator varCal = new VarianceCalculator(fwd, expiry);
    return varCal.getVariance(volSurf);
  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterised in vol/vega terms.
   * This is an estimate of annual Lognormal (Black) volatility
   * 
   * @param deriv VarianceSwap derivative to be priced
   * @param market EquityOptionDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  public double expectedVolatility(final VarianceSwap deriv, final StaticReplicationDataBundle market) {
    final double sigmaSquared = expectedVariance(deriv, market);
    return Math.sqrt(sigmaSquared);
  }

  /**
   * This is just a wrapper around ExpectedVarianceCalculator which uses a visitor pattern to farm out the calculation to the correct method of ExpectedVarianceCalculator
   * depending on the type of BlackVolatilitySurface
   */
  private class VarianceCalculator implements BlackVolatilitySurfaceVisitor<DoublesPair, Double> {
    /** The time to expiry */
    private final double _t;
    /** The forward */
    private final double _f;

    public VarianceCalculator(final double forward, final double expiry) {
      _f = forward;
      _t = expiry;
    }

    public double getVariance(final BlackVolatilitySurface<?> surf) {
      return surf.accept(this);
    }

    // ********************************************
    // strike surfaces
    // ********************************************

    @Override
    public Double visitStrike(final BlackVolatilitySurfaceStrike surface, final DoublesPair data) {
      throw new NotImplementedException();
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Double visitStrike(final BlackVolatilitySurfaceStrike surface) {
      return _cal.getAnnualizedVariance(_f, _t, surface);
    }

    // ********************************************
    // delta surfaces
    // ********************************************

    @Override
    public Double visitDelta(final BlackVolatilitySurfaceDelta surface, final DoublesPair data) {
      throw new NotImplementedException();
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Double visitDelta(final BlackVolatilitySurfaceDelta surface) {
      return _cal.getAnnualizedVariance(_f, _t, surface);
    }

    // ********************************************
    // moneyness surfaces
    // ********************************************

    @Override
    public Double visitMoneyness(final BlackVolatilitySurfaceMoneyness surface, final DoublesPair data) {
      throw new NotImplementedException();
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Double visitMoneyness(final BlackVolatilitySurfaceMoneyness surface) {
      return _cal.getAnnualizedVariance(_t, surface);
    }

    // ********************************************
    // log-moneyness surfaces
    // ********************************************

    /**
     * Only use if the integral limits have been calculated elsewhere, or you need the contribution from a specific range
     */
    @Override
    public Double visitLogMoneyness(final BlackVolatilitySurfaceLogMoneyness surface, final DoublesPair data) {
      throw new NotImplementedException();
    }

    /**
     * General method when you wish to compute the expected variance from a log-moneyness parametrised surface to within a certain tolerance
     * @param surface log-moneyness parametrised volatility surface
     * @return expected variance
     */
    @SuppressWarnings("synthetic-access")
    @Override
    public Double visitLogMoneyness(final BlackVolatilitySurfaceLogMoneyness surface) {
      return _cal.getAnnualizedVariance(_t, surface);
    }

  }

}
