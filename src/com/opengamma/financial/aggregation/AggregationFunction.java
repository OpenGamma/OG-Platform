/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.engine.view.FullyPopulatedPosition;

/**
 * 
 * Funciton to classify which bucket to put the position into.  It's generic in case we want to use it for something else.
 * @author jim
 */
public interface AggregationFunction<T> {
  public T classifyPosition(FullyPopulatedPosition position);
  public String getName();
}
