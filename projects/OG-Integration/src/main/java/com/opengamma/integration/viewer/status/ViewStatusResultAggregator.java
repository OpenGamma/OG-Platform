/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

/**
 * Aggregates view status result
 */
public interface ViewStatusResultAggregator {
  /**
   * Creates a view status result based on given column, row and sub row type.
   * 
   * @param columnType the column type, not null
   * @param rowType the row type, not null
   * @param subRowType the sub row type, not null
   * @return the view status result model, not null
   */
  ViewStatusModel aggregate(ViewAggregationType columnType, ViewAggregationType rowType, ViewAggregationType subRowType);
  /**
   * Put in the result a key/status pair
   * 
   * @see ViewStatusKey 
   * 
   * @param key the key which is a triple of SecurityType, ValueRequirementName and Currency, not null 
   * @param status true if the view can be calculated for the key or false otherwise
   */
  void put(ViewStatusKey key, boolean status);
  /**
   * Get the status for a given key
   * 
   * @param key the key in the key/status pair.
   * @return the status result or null if there is no matching key.
   */
  Boolean get(ViewStatusKey key);
}
