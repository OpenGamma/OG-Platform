/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;

/**
 * Function to classify which bucket to put the position into.  It's generic in case we want to use it for something else.
 *
 * @param <T> type of bucket.
 */
public interface AggregationFunction<T> {
  T classifyPosition(Position position);
  String getName();
}
