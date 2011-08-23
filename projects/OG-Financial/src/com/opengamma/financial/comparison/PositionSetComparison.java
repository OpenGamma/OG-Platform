/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.core.position.Position;
import com.opengamma.util.tuple.Pair;

/**
 * Represents the result of a position set comparison operation, providing:
 * <ul>
 * <li>The intersection of identical positions</li>
 * <li>The positions only present in the first set</li>
 * <li>The positions only present in the second set</li>
 * <li>The positions that exist in both but have changed from the first to the second</li>
 * </ul>
 */
public class PositionSetComparison {

  /**
   * Positions that exist in both but have been amended. Each element of the collection
   * is a pair of the initial position and the amended position.
   */
  private final Collection<Pair<Position, Position>> _diff;

  /**
   * Positions that existed only in the first set.
   */
  private final Collection<Position> _left;

  /**
   * Positions that existed only in the second set.
   */
  private final Collection<Position> _right;

  /**
   * Positions that were identical in both sets (the intersection).
   */
  private final Collection<Position> _intersection;

  protected PositionSetComparison(final Collection<Pair<Position, Position>> diff, final Collection<Position> left, final Collection<Position> right, final Collection<Position> intersection) {
    _diff = Collections.unmodifiableCollection(diff);
    _left = Collections.unmodifiableCollection(left);
    _right = Collections.unmodifiableCollection(right);
    _intersection = Collections.unmodifiableCollection(intersection);
  }

  protected PositionSetComparison(final PositionSetComparison copyFrom) {
    _diff = copyFrom.getChanged();
    _left = copyFrom.getOnlyInFirst();
    _right = copyFrom.getOnlyInSecond();
    _intersection = copyFrom.getIdentical();
  }

  public final Collection<Pair<Position, Position>> getChanged() {
    return _diff;
  }

  public final Collection<Position> getOnlyInFirst() {
    return _left;
  }

  public final Collection<Position> getOnlyInSecond() {
    return _right;
  }

  public final Collection<Position> getIdentical() {
    return _intersection;
  }

  /**
   * Returns true if both sets of positions contained identical values.
   * 
   * @return true if the positions were identical
   */
  public boolean isEqual() {
    return getChanged().isEmpty() && getOnlyInFirst().isEmpty() && getOnlyInSecond().isEmpty();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
