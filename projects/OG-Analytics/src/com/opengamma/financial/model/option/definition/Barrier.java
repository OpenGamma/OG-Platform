/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;

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
  private final double _level;

  public Barrier(final KnockType knock, final BarrierType barrier, final double level) {
    Validate.notNull(knock, "knock type");
    Validate.notNull(barrier, "barrier type");
    ArgumentChecker.notNegative(level, "barrier level");
    _knock = knock;
    _barrier = barrier;
    _level = level;
  }

  public KnockType getKnockType() {
    return _knock;
  }

  public BarrierType getBarrierType() {
    return _barrier;
  }

  public double getBarrierLevel() {
    return _level;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_barrier == null) ? 0 : _barrier.hashCode());
    result = prime * result + ((_knock == null) ? 0 : _knock.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_level);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Barrier other = (Barrier) obj;
    return ObjectUtils.equals(_barrier, other._barrier) && ObjectUtils.equals(_knock, other._knock) && Double.doubleToLongBits(_level) == Double.doubleToLongBits(other._level);
  }
}
