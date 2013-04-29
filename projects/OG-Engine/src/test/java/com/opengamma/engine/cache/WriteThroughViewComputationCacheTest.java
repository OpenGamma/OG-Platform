/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link WriteThroughViewComputationCache} class.
 */
@Test(groups = TestGroup.UNIT)
public class WriteThroughViewComputationCacheTest {

  private final ValueSpecification _value1 = Mockito.mock(ValueSpecification.class);

  public void testPutShared() {
    final ComputedValue value = new ComputedValue(_value1, new Object());
    final ViewComputationCache underlying = Mockito.mock(ViewComputationCache.class);
    final WriteThroughViewComputationCache cache = new WriteThroughViewComputationCache(underlying);
    cache.putSharedValue(value);
    Mockito.verify(underlying).putSharedValue(value);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testPutPrivate() {
    final ComputedValue value = new ComputedValue(_value1, new Object());
    final ViewComputationCache underlying = Mockito.mock(ViewComputationCache.class);
    final WriteThroughViewComputationCache cache = new WriteThroughViewComputationCache(underlying);
    cache.putPrivateValue(value);
    Mockito.verify(underlying).putPrivateValue(value);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testPutFilter() {
    final ComputedValue value = new ComputedValue(_value1, new Object());
    final ViewComputationCache underlying = Mockito.mock(ViewComputationCache.class);
    final WriteThroughViewComputationCache cache = new WriteThroughViewComputationCache(underlying);
    cache.putValue(value, CacheSelectHint.allPrivate());
    Mockito.verify(underlying).putValue(value, CacheSelectHint.allPrivate());
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testPutSharedValues() {
    final ComputedValue value = new ComputedValue(_value1, new Object());
    final ViewComputationCache underlying = Mockito.mock(ViewComputationCache.class);
    final WriteThroughViewComputationCache cache = new WriteThroughViewComputationCache(underlying);
    cache.putSharedValues(Collections.singleton(value));
    Mockito.verify(underlying).putSharedValues(Collections.singleton(value));
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testPutPrivateValues() {
    final ComputedValue value = new ComputedValue(_value1, new Object());
    final ViewComputationCache underlying = Mockito.mock(ViewComputationCache.class);
    final WriteThroughViewComputationCache cache = new WriteThroughViewComputationCache(underlying);
    cache.putPrivateValues(Collections.singleton(value));
    Mockito.verify(underlying).putPrivateValues(Collections.singleton(value));
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testPutFilterValues() {
    final ComputedValue value = new ComputedValue(_value1, new Object());
    final ViewComputationCache underlying = Mockito.mock(ViewComputationCache.class);
    final WriteThroughViewComputationCache cache = new WriteThroughViewComputationCache(underlying);
    cache.putValues(Collections.singleton(value), CacheSelectHint.allShared());
    Mockito.verify(underlying).putValues(Collections.singleton(value), CacheSelectHint.allShared());
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testEstimateValueSize() {
    final ComputedValue value = new ComputedValue(_value1, new Object());
    final ViewComputationCache underlying = Mockito.mock(ViewComputationCache.class);
    Mockito.when(underlying.estimateValueSize(value)).thenReturn(42);
    final WriteThroughViewComputationCache cache = new WriteThroughViewComputationCache(underlying);
    assertEquals(cache.estimateValueSize(value), (Integer) 42);
    Mockito.verify(underlying).estimateValueSize(value);
    Mockito.verifyNoMoreInteractions(underlying);
  }

}
