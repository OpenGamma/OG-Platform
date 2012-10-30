/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * The shared cache through which various elements in view recalculation will
 * store and retrieve values.
 */
public interface ViewComputationCache {

  /**
   * Retrieves a value from the cache. The private data store should be checked first, falling back to
   * the shared data store if the value isn't available locally.
   * 
   * @param specification the value to look up, not null.
   * @return the value from the cache, or null if not found.
   */
  Object getValue(ValueSpecification specification);

  /**
   * Retrieves a value from the cache using the {@link CacheSelectHint} to identify the private or
   * shared data stores.
   * 
   * @param specification the value to look up, not null.
   * @param filter identifies the shared or private data stores, not null.
   * @return the value from the cache, or null if not found.
   */
  Object getValue(ValueSpecification specification, CacheSelectHint filter);

  /**
   * Retrieves a set of values from the cache. The private data store should be checked first, falling
   * back to the shared data store for anything not found.
   * 
   * @param specifications the values to look up, not null.
   * @return the values from the cache, never null.
   */
  Collection<Pair<ValueSpecification, Object>> getValues(Collection<ValueSpecification> specifications);

  /**
   * Retrieves a set of values from the cache using the {@link CacheSelectHint} to identify the private
   * or shared data stores.
   * 
   * @param specifications the values to look up, not null.
   * @param filter identifies the shared or private data stores, not null.
   * @return the values from the cache, never null.
   */
  Collection<Pair<ValueSpecification, Object>> getValues(Collection<ValueSpecification> specifications, CacheSelectHint filter);

  /**
   * Puts a value into the shared data store.
   * 
   * @param value value to store, not null.
   */
  void putSharedValue(ComputedValue value);

  /**
   * Puts a value into the private data store.
   * 
   * @param value value to store, not null.
   */
  void putPrivateValue(ComputedValue value);

  /**
   * Puts a value into either the shared or private data stores using the {@link CacheSelectHint} to identify which.
   * 
   * @param value value to store, not null.
   * @param filter identifies the shared or private data stores, not null.
   */
  void putValue(ComputedValue value, CacheSelectHint filter);

  /**
   * Puts a set of values into the shared data store.
   * 
   * @param values values to store, not null.
   */
  void putSharedValues(Collection<ComputedValue> values);

  /**
   * Puts a set of values into the private data store.
   * 
   * @param values values to store, not null.
   */
  void putPrivateValues(Collection<ComputedValue> values);

  /**
   * Puts a set of values into the shared or private data stores using the {@link CacheSelectHint} to identify which.
   * 
   * @param values values to store, not null.
   * @param filter identifies the shared or private data stores, not null.
   */
  void putValues(Collection<ComputedValue> values, CacheSelectHint filter);

  /**
   * Estimates the size of a value in bytes. If the value has been recently processed, the actual byte count of the
   * Fudge encoding should be used. Other possibilities could be the average size of objects of that class.
   * 
   * @param value the value to estimate
   * @return the size in bytes, or null if no meaningful estimate is available
   */
  Integer estimateValueSize(ComputedValue value);

}
