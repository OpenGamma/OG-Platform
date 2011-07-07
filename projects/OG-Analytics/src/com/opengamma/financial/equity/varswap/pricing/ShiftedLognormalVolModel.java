/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.pricing;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.model.volatility.BlackFormula;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.rootfinding.VectorRootFinder;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.util.tuple.DoublesPair;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * 
 */
public class ShiftedLognormalVolModel {
  private double _forward;
  private double _expiry;
  private double _vol;
  private double _shift;
  private double _cutoff;
  private BlackFormula _shiftedBlackOption;

  private static final double DEF_TOL = 1.0E-6;
  private static final int DEF_STEPS = 10000;

  private static final double DEF_GUESS_VOL = 0.2;
  private static final double DEF_GUESS_SHIFT = 0.25; // fwd*(1+shift)
  private static final VectorRootFinder DEF_SOLVER = new BroydenVectorRootFinder(DEF_TOL, DEF_TOL, DEF_STEPS);

  public ShiftedLognormalVolModel(double forward, double expiry, VolatilitySurface volSurface, double cutoffStrike, double secondStrike,
      double volGuess, double shiftGuess, VectorRootFinder solver) {

    _forward = forward;
    _expiry = expiry;
    _cutoff = cutoffStrike;

    final DoubleMatrix1D volShift = fitShiftedLnParams(volSurface, cutoffStrike, secondStrike,
                                                          volGuess, shiftGuess, solver);
    _vol = volShift.getEntry(0);
    _shift = volShift.getEntry(1);

    _shiftedBlackOption = new BlackFormula(_forward * (1 + _shift), _forward * (1 + _shift), _expiry, _vol, null, true);

  }

  public ShiftedLognormalVolModel(double forward, double expiry, VolatilitySurface volSurface, double cutoffStrike, double secondStrike) {
    this(forward, expiry, volSurface, cutoffStrike, secondStrike, DEF_GUESS_VOL, DEF_GUESS_SHIFT, DEF_SOLVER);
  }

  public ShiftedLognormalVolModel from(double forward, double expiry, VolatilitySurface volSurface, double cutoffStrike, double secondStrike) {
    return new ShiftedLognormalVolModel(forward, expiry, volSurface, cutoffStrike, secondStrike, DEF_GUESS_VOL, DEF_GUESS_SHIFT, DEF_SOLVER);
  }

  private DoubleMatrix1D fitShiftedLnParams(
      VolatilitySurface vsurf,
      final double strikeTarget1, final double strikeTarget2,
      double volGuess, double shiftGuess,
      VectorRootFinder solver) {

    Validate.notNull(solver, "solver");
    DoubleMatrix1D volShiftParams; // [vol,shift]
    DoubleMatrix1D guess = new DoubleMatrix1D(new double[] {volGuess, shiftGuess });

    // Targets
    final DoublesPair target1 = DoublesPair.of(_expiry, strikeTarget1 * _forward);
    final double target1Vol = vsurf.getVolatility(target1);
    final double target1Price = new BlackFormula(_forward, strikeTarget1 * _forward, _expiry,
                                        target1Vol, null, strikeTarget1 > 1).computePrice();

    final DoublesPair target2 = DoublesPair.of(_expiry, (strikeTarget2) * _forward);
    final double target2Vol = vsurf.getVolatility(target2);
    final double target2Price = new BlackFormula(_forward, strikeTarget2 * _forward, _expiry,
                                         target2Vol, null, strikeTarget2 > 1).computePrice();

    // Function
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> priceDiffs = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D volShiftPair) {
        double[] diffs = new double[] {100, 100 };
        double vol = Math.max(1e-9, volShiftPair.getEntry(0));
        double shift = volShiftPair.getEntry(1);

        diffs[0] = (target1Price - new BlackFormula(_forward * (1 + shift), _forward * (strikeTarget1 + shift), _expiry, vol, null, strikeTarget1 > 1).computePrice()) * 1.0E+6;
        diffs[1] = (target2Price - new BlackFormula(_forward * (1 + shift), _forward * (strikeTarget2 + shift), _expiry, vol, null, strikeTarget2 > 1).computePrice()) * 1.0E+6;
        // System.err.println("vol = " + vol + "shift = " + shift + "diffs[0] = " + diffs[0] + "diffs[1] = " + diffs[1]);
        return new DoubleMatrix1D(diffs);
      }
    };

    try {
      volShiftParams = solver.getRoot(priceDiffs, guess);
    } catch (Exception e) {
      System.err.println("Failed to find roots to fit a Shifted Lognormal Distribution to your targets. Increase maxSteps, change guess, or change secondTarget.");
      // "Matrix is singular; could not perform LU decomposition" OR "Failed to converge in backtracking, even after a Jacobian recalculation."
      try {
        volShiftParams = solver.getRoot(priceDiffs, new DoubleMatrix1D(new double[] {target2Vol, 0.0 }));
      } catch (Exception e2) {
        throw new OpenGammaRuntimeException(e.getMessage());
      }

    }

    return volShiftParams;
  }

  public double priceFromRelativeStrike(double relStrike) {
    _shiftedBlackOption.setStrike((relStrike + _shift) * _forward);
    _shiftedBlackOption.setIsCall(relStrike > 1.0);
    return _shiftedBlackOption.computePrice();
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
  public final void setForward(double forward) {
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
  public final void setExpiry(double expiry) {
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
  public final void setVol(double vol) {
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
  public final void setShift(double shift) {
    _shift = shift;
  }

  /**
   * Gets the cutoff.
   * @return the cutoff
   */
  public final double getCutoff() {
    return _cutoff;
  }

  /**
   * Sets the cutoff.
   * @param cutoff  the cutoff
   */
  public final void setCutoff(double cutoff) {
    _cutoff = cutoff;
  }

  /**
   * Gets the shiftedBlackOption.
   * @return the shiftedBlackOption
   */
  public final BlackFormula getShiftedBlackOption() {
    return _shiftedBlackOption;
  }

  /**
   * Sets the shiftedBlackOption.
   * @param shiftedBlackOption  the shiftedBlackOption
   */
  public final void setShiftedBlackOption(BlackFormula shiftedBlackOption) {
    _shiftedBlackOption = shiftedBlackOption;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_cutoff);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_expiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_forward);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_shift);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_shiftedBlackOption == null) ? 0 : _shiftedBlackOption.hashCode());
    temp = Double.doubleToLongBits(_vol);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ShiftedLognormalVolModel)) {
      return false;
    }
    ShiftedLognormalVolModel other = (ShiftedLognormalVolModel) obj;
    if (Double.doubleToLongBits(_cutoff) != Double.doubleToLongBits(other._cutoff)) {
      return false;
    }
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
    if (!ObjectUtils.equals(_shiftedBlackOption, other._shiftedBlackOption)) {
      return false;
    }
    return true;
  }

}
