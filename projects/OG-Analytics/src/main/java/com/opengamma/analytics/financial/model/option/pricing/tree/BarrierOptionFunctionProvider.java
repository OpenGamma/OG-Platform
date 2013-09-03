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
 *
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
    UpAndIn
  }

  private final double _barrier;
  private CrossBarrierChecker _checker;

  /**
   * Constructor
   * @param strike The strike price
   * @param steps The number of steps
   * @param isCall True if call option, false if put option
   * @param barrier The barrier price
   * @param typeName Type of barrier option
   */
  public BarrierOptionFunctionProvider(final double strike, final int steps, final boolean isCall, final double barrier, final BarrierTypes typeName) {
    super(strike, steps, isCall);
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
      case DownAndIn:
        throw new NotImplementedException();
      default: //i.e., UpAndIn
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
  }

  /**
   * The inherited class checks lower barrier crossing for up-and-out option
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
  }
}
