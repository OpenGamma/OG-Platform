/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import java.util.Set;

/**
 * Aggregates view status result
 */
public interface ViewStatusResultAggregator {
  /**
   * Creates a view status result based on given column, row and sub row type.
   * 
   * @param aggregateType the view aggregation type, not null
   * @return the view status result model, not null
   */
  ViewStatusModel aggregate(AggregateType aggregateType);
  /**
   * Put in the result a key/status pair
   * 
   * @see ViewStatusKey 
   * 
   * @param key the key which is a triple of SecurityType, ValueRequirementName and Currency, not null 
   * @param status the status of the view calculation, i.e VALUE, NO_VALUE or GRAPH_FAIL.
   */
  void putStatus(ViewStatusKey key, ViewStatus status);
  /**
   * Get the status for a given key
   * 
   * @param key the key in the key/status pair.
   * @return the status result or null if there is no matching key.
   */
  ViewStatus getStatus(ViewStatusKey key);
  /**
   * Get the underlying keys
   * 
   * @return the set of view status keys, not-null.
   */
  Set<ViewStatusKey> keySet();
}
