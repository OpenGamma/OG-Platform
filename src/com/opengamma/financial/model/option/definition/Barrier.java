package com.opengamma.financial.model.option.definition;

/**
 *
 */

public class Barrier {

  /**
   * Knock type
   */
  public enum KnockType {
    /**
     * Knock-in
     */
    IN,
    /**
     * Knock-out
     */
    OUT
  }

  /**
   * Barrier type
   */
  public enum BarrierType {
    /**
     * Down
     */
    DOWN,
    /**
     * Up
     */
    UP
  }

  private final KnockType _knock;
  private final BarrierType _barrier;
  private final double _value;

  public Barrier(final KnockType knock, final BarrierType barrier, final double value) {
    _knock = knock;
    _barrier = barrier;
    _value = value;
  }

  public KnockType getKnockType() {
    return _knock;
  }

  public BarrierType getBarrierType() {
    return _barrier;
  }

  public double getBarrierValue() {
    return _value;
  }
}
