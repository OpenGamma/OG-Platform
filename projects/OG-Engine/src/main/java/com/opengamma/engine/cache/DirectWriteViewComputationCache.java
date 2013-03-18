/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.tuple.Pair;

/**
 * An implementation of {@link DeferredViewComputationCache} that always writes to the underlying immediately.
 */
public class DirectWriteViewComputationCache implements DeferredViewComputationCache {

  private final ViewComputationCache _underlying;

  public DirectWriteViewComputationCache(final ViewComputationCache underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected ViewComputationCache getUnderlying() {
    return _underlying;
  }

  @Override
  public Object getValue(final ValueSpecification specification) {
    return getUnderlying().getValue(specification);
  }

  @Override
  public Object getValue(final ValueSpecification specification, final CacheSelectHint filter) {
    return getUnderlying().getValue(specification, filter);
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
    return getUnderlying().getValues(specifications);
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications, final CacheSelectHint filter) {
    return getUnderlying().getValues(specifications, filter);
  }

  @Override
  public void putSharedValue(final ComputedValue value) {
    getUnderlying().putSharedValue(value);
  }

  @Override
  public void putPrivateValue(final ComputedValue value) {
    getUnderlying().putPrivateValue(value);
  }

  @Override
  public void putValue(final ComputedValue value, final CacheSelectHint filter) {
    getUnderlying().putValue(value, filter);
  }

  @Override
  public void putSharedValues(final Collection<? extends ComputedValue> values) {
    getUnderlying().putSharedValues(values);
  }

  @Override
  public void putPrivateValues(final Collection<? extends ComputedValue> values) {
    getUnderlying().putPrivateValues(values);
  }

  @Override
  public void putValues(final Collection<? extends ComputedValue> values, final CacheSelectHint filter) {
    getUnderlying().putValues(values, filter);
  }

  @Override
  public Integer estimateValueSize(final ComputedValue value) {
    return getUnderlying().estimateValueSize(value);
  }

  protected void invocationStatistics(final ComputedValue value, final DeferredStatistics statistics) {
    statistics.reportEstimatedSize(value, estimateValueSize(value));
  }

  @Override
  public void putSharedValue(final ComputedValue value, final DeferredStatistics statistics) {
    putSharedValue(value);
    invocationStatistics(value, statistics);
  }

  @Override
  public void putPrivateValue(final ComputedValue value, final DeferredStatistics statistics) {
    putPrivateValue(value);
    invocationStatistics(value, statistics);
  }

  @Override
  public void putValue(final ComputedValue value, final CacheSelectHint filter, final DeferredStatistics statistics) {
    putValue(value, filter);
    invocationStatistics(value, statistics);
  }

  protected void statistics(final Collection<? extends ComputedValue> values, final DeferredStatistics statistics) {
    for (ComputedValue value : values) {
      statistics.reportEstimatedSize(value, estimateValueSize(value));
    }
  }

  @Override
  public void putSharedValues(final Collection<? extends ComputedValue> values, final DeferredStatistics statistics) {
    putSharedValues(values);
    statistics(values, statistics);
  }

  @Override
  public void putPrivateValues(final Collection<? extends ComputedValue> values, final DeferredStatistics statistics) {
    putPrivateValues(values);
    statistics(values, statistics);
  }

  @Override
  public void putValues(final Collection<? extends ComputedValue> values, final CacheSelectHint filter, final DeferredStatistics statistics) {
    putValues(values, filter);
    statistics(values, statistics);
  }

  @Override
  public void flush() throws AsynchronousExecution {
    // No-op; already written to underlying
  }

}
