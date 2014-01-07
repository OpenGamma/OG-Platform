/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2013-10-02 -- This was originally going to be named
// InMemoryViewComputationCache, as that's the more consistent name for this class
// given the nomenclature in other contexts.
// However, for consistency its source would have been named InMemoryViewComputationCacheSource,
// which would have conflicted with the existing one that is backed by InMemoryBinaryDataStore,
// which would have meant one of two things:
// 1 - All viewprocessor-spring.xml files would have had to be changed; or
// 2 - All viewprocessor-spring.xml files would have different behavior
// Neither was particularly appealing, and so it has the nomenclature that it has.
/**
 * A simple implementation of {@link ViewComputationCache} that just holds all entries in
 * an in-memory Map. As such, it avoids the serialization behavior (and associated performance
 * penalty) of {@link DefaultViewComputationCache}. Therefore it does not need to be
 * wrapped in a {@link WriteThroughViewComputationCache} for efficient calculation node
 * performance.
 * <p/>
 * Because there is no ability to overflow to off-heap storage in this implementation,
 * it should <strong>only</strong> be used in a case where testing has established
 * that the <em>entire</em> value cache can fit in RAM. Otherwise, an {@code OutOfMemoryException}
 * will be thrown and the JVM will exit.
 * <p/>
 * In addition, this implementation cannot support remote calculation nodes.
 * <p/>
 * This class was originally requested in <a href="http://jira.opengamma.com/browse/PLAT-4786">PLAT-4786</a>.
 */
public class MapBackedInMemoryViewComputationCache extends AbstractViewComputationCache {
  /**
   * The initial capacity of the underlying ConcurrentHashMap.
   * This is set extremely high as in practice cache sizes between 500,000
   * and 1,000,000 are not uncommon.
   */
  private static final int INITIAL_CAPACITY = 100000;
  /**
   * The load factor for the underlying ConcurrentHashMap. This
   * is the same (0.75) as the default load factor. 
   */
  private static final float LOAD_FACTOR = 0.75f;
  /**
   * The concurrency level for the underlying ConcurrentHashMap.
   * This is double the default.
   */
  private static final int CONCURRENCY_LEVEL = 32;
  /**
   * The underlying map for shared values.
   */
  private final ConcurrentMap<ValueSpecification, Object> _sharedValues =
      new ConcurrentHashMap<ValueSpecification, Object>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
  /**
   * The underlying map for private values.
   */
  private final ConcurrentMap<ValueSpecification, Object> _privateValues =
      new ConcurrentHashMap<ValueSpecification, Object>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
  
  public MapBackedInMemoryViewComputationCache() {
  }
  
  public MapBackedInMemoryViewComputationCache(MapBackedInMemoryViewComputationCache existing) {
    ArgumentChecker.notNull(existing, "existing to be cloned");
    _sharedValues.putAll(existing._sharedValues);
    _privateValues.putAll(existing._privateValues);
  }
  
  /**
   * Remove all current elements in the underlying maps.
   */
  public void clear() {
    _sharedValues.clear();
    _privateValues.clear();
  }

  @Override
  public Object getValue(ValueSpecification specification) {
    ArgumentChecker.notNull(specification, "specification");
    Object result = _sharedValues.get(specification);
    if (result == null) {
      result = _privateValues.get(specification);
    }
    return result;
  }

  @Override
  public void putSharedValue(ComputedValue value) {
    ArgumentChecker.notNull(value, "value");
    _sharedValues.put(value.getSpecification(), value.getValue());
  }

  @Override
  public void putPrivateValue(ComputedValue value) {
    ArgumentChecker.notNull(value, "value");
    _privateValues.put(value.getSpecification(), value.getValue());
  }

  @Override
  public Integer estimateValueSize(ComputedValue value) {
    return null;
  }

}
