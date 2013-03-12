/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A {@link ViewComputationCache} composed with a {@link CacheSelectHint}.
 */
public class FilteredViewComputationCache {

  private final ViewComputationCache _cache;
  private final CacheSelectHint _filter;

  public FilteredViewComputationCache(final ViewComputationCache cache, final CacheSelectHint filter) {
    ArgumentChecker.notNull(cache, "cache");
    ArgumentChecker.notNull(filter, "filter");
    _cache = cache;
    _filter = filter;
  }

  protected ViewComputationCache getCache() {
    return _cache;
  }

  public CacheSelectHint getFilter() {
    return _filter;
  }

  public Object getValue(ValueSpecification specification) {
    return getCache().getValue(specification, getFilter());
  }

  public Collection<Pair<ValueSpecification, Object>> getValues(Collection<ValueSpecification> specifications) {
    return getCache().getValues(specifications, getFilter());
  }

  public Integer estimateValueSize(final ComputedValue value) {
    return getCache().estimateValueSize(value);
  }

  public void putValue(final ComputedValue value) {
    getCache().putValue(value, getFilter());
  }

  public void putValues(final Collection<ComputedValue> values) {
    getCache().putValues(values, getFilter());
  }

}
