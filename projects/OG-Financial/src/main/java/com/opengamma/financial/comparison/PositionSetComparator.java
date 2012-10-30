/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;

import com.google.common.collect.Maps;
import com.opengamma.core.position.Position;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Provides comparison operations between sets of {@link Position} objects.
 */
public class PositionSetComparator extends AbstractComparator {

  public PositionSetComparator(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  private static Set<PositionInfo> createPositionInfoSet(final ComparisonContext context, final Iterable<Position> positions, final int sizeHint) {
    final Map<PositionInfo, PositionInfo> map = Maps.newHashMapWithExpectedSize(sizeHint);
    for (Position position : positions) {
      final PositionInfo info = new PositionInfo(context, position);
      final PositionInfo previous = map.get(info);
      if (previous != null) {
        previous.associateAdditionalUnderlying(position);
      } else {
        map.put(info, info);
      }
    }
    return new HashSet<PositionInfo>(map.keySet());
  }

  /**
   * Produces a comparison of two sets of positions.
   * 
   * @param first first set of positions, not null
   * @param second second set of positions, not null
   * @return the comparison result
   */
  public PositionSetComparison compare(final Iterable<Position> first, final Iterable<Position> second) {
    ArgumentChecker.notNull(first, "first");
    ArgumentChecker.notNull(second, "second");
    final ComparisonContext context = createContext();
    return compare(createPositionInfoSet(context, first, 32), createPositionInfoSet(context, second, 32));
  }

  /**
   * Produces a comparison of two sets of positions.
   * 
   * @param first first set of positions, not null
   * @param second second set of positions, not null
   * @return the comparison result
   */
  public PositionSetComparison compare(final Collection<Position> first, final Collection<Position> second) {
    ArgumentChecker.notNull(first, "first");
    ArgumentChecker.notNull(second, "second");
    final ComparisonContext context = createContext();
    return compare(createPositionInfoSet(context, first, first.size()), createPositionInfoSet(context, second, second.size()));
  }

  private static Collection<Position> getUnderlyingPositions(final Collection<PositionInfo> positions) {
    final Collection<Position> result = new ArrayList<Position>(positions.size());
    for (PositionInfo position : positions) {
      position.addUnderlyingToCollection(result);
    }
    return result;
  }

  private PositionSetComparison compare(final Set<PositionInfo> first, final Set<PositionInfo> second) {
    final Iterator<PositionInfo> itrFirst = first.iterator();
    final Collection<Position> intersection = new ArrayList<Position>();
    // Remove the identical positions first to lower the size of the sets
    while (itrFirst.hasNext()) {
      final PositionInfo position = itrFirst.next();
      if (second.remove(position)) {
        // Second portfolio contained an equivalent position
        position.addUnderlyingToCollection(intersection);
        itrFirst.remove();
      }
    }
    final Collection<Pair<Position, Position>> diff = new ArrayList<Pair<Position, Position>>();
    // TODO: identify changes
    // Anything left in either set is unique to that set
    return new PositionSetComparison(diff, getUnderlyingPositions(first), getUnderlyingPositions(second), intersection);
  }

}
