/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

/**
 * A store for binary data for a fully identified {@link ValueSpecification}.
 * It is expected that one of these will be created per iteration per View/Configuration pair.
 */
public interface ValueSpecificationIdentifierBinaryDataStore {
  
  /**
   * Obtain the current data associated with the identifier.
   * This method will return {@code null} if there is no data with
   * the specified identifier.
   * 
   * @param identifier The identifier to obtain data for
   * @return the current data stored with that identifier.
   */
  byte[] get(long identifier);
  
  /**
   * Provide data for the given identifier for this store.
   * 
   * @param identifier The identifier to use as a key
   * @param data The data to store
   */
  void put(long identifier, byte[] data);
  
  /**
   * Remove any underlying resources, and free all memory, relating
   * to this store. Immediately after this method is called the store instance
   * will fall out of scope and be suitable for garbage collection.
   */
  void delete();

}
