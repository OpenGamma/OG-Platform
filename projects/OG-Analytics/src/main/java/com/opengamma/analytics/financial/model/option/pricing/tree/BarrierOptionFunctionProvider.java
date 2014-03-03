/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * Option function provider for in(out) barrier option
 * In(Out) option comes into existence (becomes worthless) if the asset price hits the barrier
 */
public abstract class BarrierOptionFunctionProvider extends OptionFunctionProvider1D {

  /**
   * Use these strings to specify barrier option type.
   * DownAndIn and UpAndIn MUST be computed via in-out parity if the option is European
   */
  public static enum BarrierTypes {
    /**
     * Down-and-out option
     */
    DownAndOut,

    /**
     * Up-and-out option
     */
    UpAndOut,

    /**
     * Down-and-in option, not implemented
     */
    DownAndIn,

    /**
     * Up-and-in option, not implemented
     */
    UpAndIn,

    /**
     * Up-and-Out-Down-and-Out
     */
    DoubleKnockOut,

    /**
     * Up-and-In-Down-and-In, not implemented
     * Knock-in type should be priced by another model
     */
    DoubleKnockIn
  }

  private double _barrier;
  private CrossBarrierChecker _checker;

  /**
   * Constructor
   * @param strike The strike price
   * @param timeToExpiry Time to expiry
   * @param steps The number of steps
   * @param isCall True if call option, false if put option
   * @param barrier The barrier price
   * @param typeName Type of barrier option
   */
  public BarrierOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double barrier, final BarrierTypes typeName) {
    super(strike, timeToExpiry, steps, isCall);
    ArgumentChecker.isTrue(barrier > 0., "barrier should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(barrier), "barrier should be finite");
    _barrier = barrier;

    switch (typeName) {
      case DownAndOut:
        _checker = new CrossLowerBarrier();
        break;
      case UpAndOut:
        _checker = new CrossUpperBarrier();
        break;
      default:
        throw new NotImplementedException();
    }

  }

  /**
   * Access cross barrier checker
   * @return _checker
   */
  public CrossBarrierChecker getChecker() {
    return _checker;
  }

  /**
   * Access barrier
   * @return _barrier
   */
  public double getBarrier() {
    return _barrier;
  }

  /**
   * Barrier type
   * @return DownAndOut or UpAndOut
   */
  public BarrierTypes getBarrierType() {
    return _checker instanceof CrossLowerBarrier ? BarrierTypes.DownAndOut : BarrierTypes.UpAndOut;
  }

  /**
   * The protected class checks barrier crossing
   */
  protected abstract class CrossBarrierChecker {
    /**
     * @param priceTmp The asset price
     * @return True if asset price crosses the barrier
     */
    public abstract boolean checkOut(final double priceTmp);

    /**
     * When strike is behind the barrier, payoff will never be in-the-money, depending on Call or Put
     * @return True if option price is trivially 0
     */
    public abstract boolean checkStrikeBehindBarrier();
  }

  /**
   * The inherited class checks lower barrier crossing for down-and-out option
   */
  @SuppressWarnings("synthetic-access")
  protected class CrossLowerBarrier extends CrossBarrierChecker {
    @Override
    public boolean checkOut(final double priceTmp) {
      return (priceTmp <= _barrier);
    }

    @Override
    public boolean checkStrikeBehindBarrier() {
      return getSign() == 1. ? false : (_barrier >= getStrike());
    }

    @Override
    public int hashCode() {
      return 1;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof CrossLowerBarrier)) {
        return false;
      }
      return true;
    }
  }

  /**
   * The inherited class checks upper barrier crossing for up-and-out option
   */
  @SuppressWarnings("synthetic-access")
  protected class CrossUpperBarrier extends CrossBarrierChecker {
    @Override
    public boolean checkOut(final double priceTmp) {
      return priceTmp >= _barrier;
    }

    @Override
    public boolean checkStrikeBehindBarrier() {
      return getSign() == 1. ? (_barrier <= getStrike()) : false;
    }

    @Override
    public int hashCode() {
      return 2;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof CrossUpperBarrier)) {
        return false;
      }
      return true;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_barrier);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_checker == null) ? 0 : _checker.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {

    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof BarrierOptionFunctionProvider)) {
      return false;
    }
    BarrierOptionFunctionProvider other = (BarrierOptionFunctionProvider) obj;
    if (Double.doubleToLongBits(_barrier) != Double.doubleToLongBits(other._barrier)) {
      return false;
    }
    if (!_checker.equals(other._checker)) {
      return false;
    }
    return true;
  }

}
