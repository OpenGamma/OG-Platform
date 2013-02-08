/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.ShiftedLogNormalTailExtrapolation;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.rootfinding.VectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * This is a model where the SDE for the forward are $\frac{df}{f+\alpha}=\sigma_{\alpha} dW$, that is, the forward, $f$, plus some displacement, $\alpha$, follow a geometric 
 * Brownian motion (GBM). European options can be priced using the Black formula with forward $f \rightarrow f +\alpha$ and strike $k \rightarrow k + \alpha$ <p>
 * <b> This should not be confused with Shifted Log-Normal</b> (see {@link ShiftedLogNormalTailExtrapolation})
 */
public class DisplacedDiffusionModel {
  /** A logger */
  private static final Logger s_logger = LoggerFactory.getLogger(DisplacedDiffusionModel.class);
  //TODO none of these next fields should be stored in this class
  /** The forward */
  private double _forward;
  /** The expiry */
  private double _expiry;
  /** The volatility */
  private double _vol;
  /** The shift */
  private double _shift;

  /** The default tolerance */
  private static final double DEF_TOL = 1.0E-6;
  /** The default number of steps */
  private static final int DEF_STEPS = 10000;
  /** The default initial volatility */
  private static final double DEF_GUESS_VOL = 0.20;
  /** The default fraction of a forward */
  private static final double DEF_GUESS_SHIFT = 0.1;
  /** The default root-finder */
  private static final VectorRootFinder DEF_SOLVER = new BroydenVectorRootFinder(DEF_TOL, DEF_TOL, DEF_STEPS);
  /** A transform to remove sigma > 0 as a constraint */
  private static final ParameterLimitsTransform TRANSFORM = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);

  /**
   * Build a shifted lognormal volatility model directly from model inputs
   * @param forward absolute level of the forward
   * @param expiry expiry in years
   * @param lognormalVol annual lognormal (black) vol
   * @param shift absolute level of the shift applied to the forward and strike. A positive value shifts distribution left.
   */
  public DisplacedDiffusionModel(final double forward, final double expiry, final double lognormalVol, final double shift) {
    _forward = forward;
    _expiry = expiry;
    _vol = lognormalVol;
    _shift = shift;
  }

  /**
   * Fit a shifted lognormal volatility model to two target points at one expiry.
   * @param forward absolute level of the forward
   * @param expiry expiry in years
   * @param targetStrike1 absolute level of the first target strike
   * @param targetVol1 lognormal volatility at the first target strike
   * @param targetStrike2 absolute level of the second target strike
   * @param targetVol2 lognormal volatility at the second target strike
   * @param volGuess initial guess for the model's annual lognormal volatility
   * @param shiftGuess initial guess for the model's shift, as absolute level
   * @param solver VectorRootFinder
   */
  public DisplacedDiffusionModel(final double forward, final double expiry, final double targetStrike1, final double targetVol1, final double targetStrike2,
      final double targetVol2, final double volGuess, final double shiftGuess, final VectorRootFinder solver) {

    _forward = forward;
    _expiry = expiry;

    // Find target volatilities
    final DoubleMatrix1D volShift = fitShiftedLnParams(targetStrike1, targetVol1, targetStrike2, targetVol2, volGuess, shiftGuess * _forward, solver);
    _vol = volShift.getEntry(0);
    _shift = volShift.getEntry(1);

  }

  /**
   * Fit a Shifted Lognormal Volatility to two target points at one expiry
   * @param forward absolute level of the forward
   * @param expiry expiry in years
   * @param targetStrike1 absolute level of the first target strike
   * @param targetVol1 lognormal vol at the first target strike
   * @param targetStrike2 absolute level of the second target strike
   * @param targetVol2 lognormal vol at the second target strike
   */
  public DisplacedDiffusionModel(final double forward, final double expiry, final double targetStrike1, final double targetVol1, final double targetStrike2,
      final double targetVol2) {
    this(forward, expiry, targetStrike1, targetVol1, targetStrike2, targetVol2, DEF_GUESS_VOL, DEF_GUESS_SHIFT, DEF_SOLVER);
  }

  /**
   * Fit a Shifted Lognormal Volatility to two target points at one expiry
   * @param forward absolute level of the forward
   * @param expiry expiry in years
   * @param targetStrike1 absolute level of the first target strike
   * @param targetVol1 lognormal vol at the first target strike
   * @param targetStrike2 absolute level of the second target strike
   * @param targetVol2 lognormal vol at the second target strike
   * @return a displaced diffusion model
   */
  public DisplacedDiffusionModel from(final double forward, final double expiry, final double targetStrike1, final double targetVol1, final double targetStrike2,
      final double targetVol2) {
    return new DisplacedDiffusionModel(forward, expiry, targetStrike1, targetVol1, targetStrike2, targetVol2, DEF_GUESS_VOL, DEF_GUESS_SHIFT, DEF_SOLVER);
  }

  private DoubleMatrix1D fitShiftedLnParams(final double strikeTarget1, final double volTarget1, final double strikeTarget2, final double volTarget2,
      final double volGuess, final double shiftGuess, final VectorRootFinder solver) {

    ArgumentChecker.notNull(solver, "solver");
    DoubleMatrix1D volShiftParams; // [transform(vol),shift]

    final DoubleMatrix1D guess = new DoubleMatrix1D(new double[] {TRANSFORM.transform(volGuess), shiftGuess });

    // Targets
    final double target1Price = BlackFormulaRepository.price(_forward, strikeTarget1, _expiry, volTarget1, strikeTarget1 > _forward);
    final double target2Price = BlackFormulaRepository.price(_forward, strikeTarget2, _expiry, volTarget2, strikeTarget2 > _forward);

    // Handle trivial case 1: Same Vol ==> 0.0 shift
    if (CompareUtils.closeEquals(volTarget1, volTarget2, DEF_TOL)) {
      return new DoubleMatrix1D(new double[] {volTarget1, 0.0 });
    }

    // Objective function - hit the two vol targets
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> priceDiffs = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D volShiftPair) {
        final double[] diffs = new double[] {100, 100 };
        final double vol = TRANSFORM.inverseTransform(volShiftPair.getEntry(0)); // Math.max(1e-9, volShiftPair.getEntry(0));
        final double shift = volShiftPair.getEntry(1);

        diffs[0] = (target1Price - BlackFormulaRepository.price(_forward + shift, strikeTarget1 + shift, _expiry, vol, strikeTarget1 > _forward)) * 1e6;
        diffs[1] = (target2Price - BlackFormulaRepository.price(_forward + shift, strikeTarget2 + shift, _expiry, vol, strikeTarget2 > _forward)) * 1e6;
        return new DoubleMatrix1D(diffs);
      }
    };

    try {
      volShiftParams = solver.getRoot(priceDiffs, guess);
    } catch (final Exception e) { // Failed on first solver attempt. Doing a second
      try {
        volShiftParams = solver.getRoot(priceDiffs, new DoubleMatrix1D(new double[] {TRANSFORM.transform(volTarget2), 0.0 }));
      } catch (final Exception e2) {
        s_logger.error("Failed to find roots to fit a Shifted Lognormal Distribution to your targets. Increase maxSteps, change guess, or change secondTarget.");
        s_logger.error("K1 = " + strikeTarget1 + ",vol1 = " + volTarget1 + ",price1 = " + target1Price);
        s_logger.error("K2 = " + strikeTarget2 + ",vol2 = " + volTarget2 + ",price2 = " + target2Price);
        throw new OpenGammaRuntimeException(e.getMessage());
      }
    }
    return new DoubleMatrix1D(new double[] {TRANSFORM.inverseTransform(volShiftParams.getEntry(0)), volShiftParams.getEntry(1) });
  }

  /**
   * @param absoluteStrike The absolute strike
   * @return Price of the calibrated model given a fixed (absolute) strike. So if the forward, was 80, an OTM Put might have a strike of 65.
   */
  public double priceFromFixedStrike(final double absoluteStrike) {
    return BlackFormulaRepository.price(_forward + _shift, absoluteStrike + _shift, _expiry, _vol, absoluteStrike > _forward);
  }

  /**
   * Gets the forward.
   * @return the forward
   */
  public final double getForward() {
    return _forward;
  }

  /**
   * Sets the forward.
   * @param forward  the forward
   */
  public final void setForward(final double forward) {
    _forward = forward;
  }

  /**
   * Gets the expiry.
   * @return the expiry
   */
  public final double getExpiry() {
    return _expiry;
  }

  /**
   * Sets the expiry.
   * @param expiry  the expiry
   */
  public final void setExpiry(final double expiry) {
    _expiry = expiry;
  }

  /**
   * Gets the vol.
   * @return the vol
   */
  public final double getVol() {
    return _vol;
  }

  /**
   * Sets the vol.
   * @param vol  the vol
   */
  public final void setVol(final double vol) {
    _vol = vol;
  }

  /**
   * Gets the shift.
   * @return the shift
   */
  public final double getShift() {
    return _shift;
  }

  /**
   * Sets the shift.
   * @param shift  the shift
   */
  public final void setShift(final double shift) {
    _shift = shift;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_expiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_forward);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_shift);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_vol);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DisplacedDiffusionModel)) {
      return false;
    }
    final DisplacedDiffusionModel other = (DisplacedDiffusionModel) obj;

    if (Double.doubleToLongBits(_expiry) != Double.doubleToLongBits(other._expiry)) {
      return false;
    }
    if (Double.doubleToLongBits(_forward) != Double.doubleToLongBits(other._forward)) {
      return false;
    }
    if (Double.doubleToLongBits(_shift) != Double.doubleToLongBits(other._shift)) {
      return false;
    }
    if (Double.doubleToLongBits(_vol) != Double.doubleToLongBits(other._vol)) {
      return false;
    }
    return true;
  }

}
