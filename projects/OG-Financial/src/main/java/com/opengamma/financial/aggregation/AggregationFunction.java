/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Comparator;

import com.opengamma.core.position.Position;

/**
 * Function to classify which bucket to put the position into.  It's generic in case we want to use it for something else.
 *
 * @param <T> type of bucket.
 */
public interface AggregationFunction<T> extends Comparator<T> {
  Collection<T> getRequiredEntries();
  T classifyPosition(Position position);
  Comparator<Position> getPositionComparator();
  String getName();
}
