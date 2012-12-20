/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;

/**
 * A store for binary data for a fully identified {@link ValueSpecification}.
 * It is expected that one of these will be created per iteration per View/Configuration pair.
 */
public interface BinaryDataStore {

  /**
   * Obtain the current data associated with the identifier.
   * This method will return null if there is no data with
   * the specified identifier.
   * 
   * @param identifier The identifier to obtain data for
   * @return the current data stored with that identifier.
   */
  byte[] get(long identifier);

  /**
   * Potentially more efficient form of {@link #get} for multiple lookups.
   * 
   * @param identifiers identifiers to query
   * @return map of results. If there is no data for an identifier it will be missing from the map. 
   */
  Map<Long, byte[]> get(Collection<Long> identifiers);

  /**
   * Provide data for the given identifier for this store.
   * 
   * @param identifier The identifier to use as a key
   * @param data The data to store
   */
  void put(long identifier, byte[] data);

  /**
   * Potentially more efficient form of {@link #put} for multiple puts.
   * 
   * @param data map of identifier to data values to store
   */
  void put(Map<Long, byte[]> data);

  /**
   * Remove any underlying resources, and free all memory, relating
   * to this store. Immediately after this method is called the store instance
   * will fall out of scope and be suitable for garbage collection.
   */
  void delete();

}
