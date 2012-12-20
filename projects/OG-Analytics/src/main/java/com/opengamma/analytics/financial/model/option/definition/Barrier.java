/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.Validate;

/**
 *
 */

public class Barrier {
  /** Knock type */
  public enum KnockType {
    /** Knock-in */
    IN,
    /** Knock-out */
    OUT
  }

  /** Barrier type */
  public enum BarrierType {
    /** Down */
    DOWN,
    /** Up */
    UP
  }

  //TODO probably will need something more useful 
  /** Observation type */
  public enum ObservationType {
    /** Continuous */
    CONTINUOUS,
    /** Close */
    CLOSE
  }

  private final KnockType _knock;
  private final BarrierType _barrier;
  private final ObservationType _observation;
  private final double _level;

  public Barrier(final KnockType knock, final BarrierType barrier, final ObservationType observation, final double level) {
    Validate.notNull(knock, "knock type");
    Validate.notNull(barrier, "barrier type");
    Validate.notNull(observation, "observation type");
    Validate.isTrue(level > 0, "barrier level must be > 0");
    _knock = knock;
    _barrier = barrier;
    _observation = observation;
    _level = level;
  }

  public KnockType getKnockType() {
    return _knock;
  }

  public BarrierType getBarrierType() {
    return _barrier;
  }

  public ObservationType getObservationType() {
    return _observation;
  }

  public double getBarrierLevel() {
    return _level;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _barrier.hashCode();
    result = prime * result + _knock.hashCode();
    result = prime * result + _observation.hashCode();
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
    if (!(obj instanceof Barrier)) {
      return false;
    }
    final Barrier other = (Barrier) obj;
    return _barrier == other._barrier &&
           _knock == other._knock &&
           _observation == other._observation &&
           Double.doubleToLongBits(_level) == Double.doubleToLongBits(other._level);
  }
}
