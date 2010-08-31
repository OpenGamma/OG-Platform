/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

public class WriteBehindViewComputationCacheTest {

  private static class Underlying implements ViewComputationCache {

    private ValueSpecification _getValue;
    private Collection<ValueSpecification> _getValues;
    private ComputedValue _putValue;
    private Collection<ComputedValue> _putValues;
    
    private volatile boolean _throwException;

    private final CountDownLatch _allowPutValue = new CountDownLatch(1);
    private final CountDownLatch _allowPutValues = new CountDownLatch(1);

    @Override
    public Object getValue(ValueSpecification specification) {
      _getValue = specification;
      return null;
    }

    @Override
    public Collection<Pair<ValueSpecification, Object>> getValues(Collection<ValueSpecification> specifications) {
      _getValues = specifications;
      return Collections.emptyList();
    }

    private void putValueImpl(final ComputedValue value) {
      try {
        _allowPutValue.await(2000L, TimeUnit.MILLISECONDS);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
      if (_throwException) {
        throw new OpenGammaRuntimeException ("woot");
      }
      _putValue = value;
    }
    
    @Override
    public void putPrivateValue (final ComputedValue value) {
      putValueImpl (value);
    }
    
    @Override
    public void putSharedValue (final ComputedValue value) {
      putValueImpl (value);
    }

    private void putValuesImpl(final Collection<ComputedValue> values) {
      try {
        _allowPutValues.await(2000L, TimeUnit.MILLISECONDS);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
      if (_throwException) {
        throw new OpenGammaRuntimeException ("woot");
      }
      _putValues = new ArrayList<ComputedValue>(values);
    }
    
    @Override
    public void putPrivateValues (final Collection<ComputedValue> values) {
      putValuesImpl (values);
    }
    
    @Override
    public void putSharedValues (final Collection<ComputedValue> values) {
      putValuesImpl (values);
    }

  }

  private static final ValueSpecification s_valueSpec1 = new ValueSpecification(new ValueRequirement("Value 1", new ComputationTargetSpecification(new DefaultSecurity("TEST"))), "Function UID");
  private static final ValueSpecification s_valueSpec2 = new ValueSpecification(new ValueRequirement("Value 2", new ComputationTargetSpecification(new DefaultSecurity("TEST"))), "Function UID");
  private static final ValueSpecification s_valueSpec3 = new ValueSpecification(new ValueRequirement("Value 3", new ComputationTargetSpecification(new DefaultSecurity("TEST"))), "Function UID");

  private ExecutorService _executorService;
  private Underlying _underlying;
  private WriteBehindViewComputationCache _cache;

  @Before
  public void init() {
    _executorService = Executors.newCachedThreadPool();
    _underlying = new Underlying();
    _cache = new WriteBehindViewComputationCache(_underlying, _executorService);
  }

  @After
  public void releaseThreads() {
    _underlying._allowPutValue.countDown();
    _underlying._allowPutValues.countDown();
  }

  @Test
  public void getValueHittingUnderlying() {
    _cache.getValue(s_valueSpec1);
    assertEquals(s_valueSpec1, _underlying._getValue);
  }

  @Test
  public void getValueHittingPendingPrivate() {
    final ComputedValue value = new ComputedValue(s_valueSpec1, "foo");
    _cache.putPrivateValue(value);
    assertEquals("foo", _cache.getValue(s_valueSpec1));
    assertNull(_underlying._putValue);
    assertNull(_underlying._getValue);
  }

  @Test
  public void getValueHittingPendingShared() {
    final ComputedValue value = new ComputedValue(s_valueSpec1, "foo");
    _cache.putSharedValue(value);
    assertEquals("foo", _cache.getValue(s_valueSpec1));
    assertNull(_underlying._putValue);
    assertNull(_underlying._getValue);
  }

  @Test
  public void getValuesHittingUnderlying() {
    final Collection<ValueSpecification> valueSpec = Arrays.asList(s_valueSpec1, s_valueSpec2, s_valueSpec3);
    _cache.getValues(valueSpec);
    assertEquals(valueSpec, _underlying._getValues);
  }

  @Test
  public void getValuesOneUnderlyingPrivate() {
    final Collection<ValueSpecification> valueSpec = Arrays.asList(s_valueSpec1, s_valueSpec2, s_valueSpec3);
    _cache.putPrivateValue(new ComputedValue(s_valueSpec1, "foo"));
    _cache.putPrivateValue(new ComputedValue(s_valueSpec2, "bar"));
    final Collection<Pair<ValueSpecification, Object>> values = _cache.getValues(valueSpec);
    assertEquals(valueSpec.size(), values.size());
    assertTrue(values.contains(Pair.of(s_valueSpec1, "foo")));
    assertTrue(values.contains(Pair.of(s_valueSpec2, "bar")));
    assertNull(_underlying._putValue);
    assertNull(_underlying._putValues);
    assertNull(_underlying._getValues);
    assertEquals(s_valueSpec3, _underlying._getValue);
    _underlying._allowPutValues.countDown();
    _cache.waitForPendingWrites();
    assertNotNull (_underlying._putValues);
    // With nothing pending, we should hit the underlying completely
    _cache.getValues(valueSpec);
    assertEquals(valueSpec, _underlying._getValues);
  }

  @Test
  public void getValuesOneUnderlyingShared() {
    final Collection<ValueSpecification> valueSpec = Arrays.asList(s_valueSpec1, s_valueSpec2, s_valueSpec3);
    _cache.putSharedValue(new ComputedValue(s_valueSpec1, "foo"));
    _cache.putSharedValue(new ComputedValue(s_valueSpec2, "bar"));
    final Collection<Pair<ValueSpecification, Object>> values = _cache.getValues(valueSpec);
    assertEquals(valueSpec.size(), values.size());
    assertTrue(values.contains(Pair.of(s_valueSpec1, "foo")));
    assertTrue(values.contains(Pair.of(s_valueSpec2, "bar")));
    assertNull(_underlying._putValue);
    assertNull(_underlying._putValues);
    assertNull(_underlying._getValues);
    assertEquals(s_valueSpec3, _underlying._getValue);
    _underlying._allowPutValues.countDown();
    _cache.waitForPendingWrites();
    assertNotNull (_underlying._putValues);
    // With nothing pending, we should hit the underlying completely
    _cache.getValues(valueSpec);
    assertEquals(valueSpec, _underlying._getValues);
  }

  @Test
  public void getValuesSomeUnderlyingPrivate() {
    final Collection<ValueSpecification> valueSpec = Arrays.asList(s_valueSpec1, s_valueSpec2, s_valueSpec3);
    _cache.putPrivateValue(new ComputedValue(s_valueSpec1, "foo"));
    final Collection<Pair<ValueSpecification, Object>> values = _cache.getValues(valueSpec);
    assertEquals(1, values.size());
    assertTrue(values.contains(Pair.of(s_valueSpec1, "foo")));
    assertNull(_underlying._putValue);
    assertNull(_underlying._putValues);
    assertEquals(Arrays.asList(s_valueSpec2, s_valueSpec3), _underlying._getValues);
    assertNull(_underlying._getValue);
    _underlying._allowPutValue.countDown();
    _cache.waitForPendingWrites();
    assertNotNull (_underlying._putValue);
    // With nothing pending, we should hit the underlying completely
    _cache.getValues(valueSpec);
    assertEquals(valueSpec, _underlying._getValues);
  }

  @Test
  public void getValuesSomeUnderlyingShared() {
    final Collection<ValueSpecification> valueSpec = Arrays.asList(s_valueSpec1, s_valueSpec2, s_valueSpec3);
    _cache.putSharedValue(new ComputedValue(s_valueSpec1, "foo"));
    final Collection<Pair<ValueSpecification, Object>> values = _cache.getValues(valueSpec);
    assertEquals(1, values.size());
    assertTrue(values.contains(Pair.of(s_valueSpec1, "foo")));
    assertNull(_underlying._putValue);
    assertNull(_underlying._putValues);
    assertEquals(Arrays.asList(s_valueSpec2, s_valueSpec3), _underlying._getValues);
    assertNull(_underlying._getValue);
    _underlying._allowPutValue.countDown();
    _cache.waitForPendingWrites();
    assertNotNull (_underlying._putValue);
    // With nothing pending, we should hit the underlying completely
    _cache.getValues(valueSpec);
    assertEquals(valueSpec, _underlying._getValues);
  }

  private void pause() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
    }
  }

  @Test
  public void putValueDirectWritePrivate() {
    _underlying._allowPutValue.countDown();
    final ComputedValue value = new ComputedValue(s_valueSpec1, "foo");
    _cache.putPrivateValue(value);
    for (int i = 0; i < 100; i++) {
      if (_underlying._putValue != null) {
        break;
      }
      pause();
    }
    assertEquals(value, _underlying._putValue);
    assertNull(_underlying._putValues);
  }

  @Test
  public void putValueDirectWriteShared() {
    _underlying._allowPutValue.countDown();
    final ComputedValue value = new ComputedValue(s_valueSpec1, "foo");
    _cache.putSharedValue(value);
    for (int i = 0; i < 100; i++) {
      if (_underlying._putValue != null) {
        break;
      }
      pause();
    }
    assertEquals(value, _underlying._putValue);
    assertNull(_underlying._putValues);
  }

  @Test
  public void putValueCollatedWritePrivate() {
    final ComputedValue value1 = new ComputedValue(s_valueSpec1, "foo");
    final ComputedValue value2 = new ComputedValue(s_valueSpec2, "bar");
    _cache.putPrivateValue(value1);
    _cache.putPrivateValue(value2);
    _underlying._allowPutValues.countDown();
    for (int i = 0; i < 100; i++) {
      if (_underlying._putValues != null) {
        break;
      }
      pause();
    }
    assertNull(_underlying._putValue);
    assertEquals(Arrays.asList(value1, value2), _underlying._putValues);
  }

  @Test
  public void putValueCollatedWriteShared() {
    final ComputedValue value1 = new ComputedValue(s_valueSpec1, "foo");
    final ComputedValue value2 = new ComputedValue(s_valueSpec2, "bar");
    _cache.putSharedValue(value1);
    _cache.putSharedValue(value2);
    _underlying._allowPutValues.countDown();
    for (int i = 0; i < 100; i++) {
      if (_underlying._putValues != null) {
        break;
      }
      pause();
    }
    assertNull(_underlying._putValue);
    assertEquals(Arrays.asList(value1, value2), _underlying._putValues);
  }

  @Test
  public void putValuesDirectWritePrivate() {
    _underlying._allowPutValues.countDown();
    final List<ComputedValue> values = Arrays.asList(new ComputedValue(s_valueSpec1, "foo"), new ComputedValue(s_valueSpec2, "bar"));
    _cache.putPrivateValues(values);
    for (int i = 0; i < 100; i++) {
      if (_underlying._putValues != null) {
        break;
      }
      pause();
    }
    assertEquals(values, _underlying._putValues);
    assertNull(_underlying._putValue);
  }

  @Test
  public void putValuesDirectWriteShared() {
    _underlying._allowPutValues.countDown();
    final List<ComputedValue> values = Arrays.asList(new ComputedValue(s_valueSpec1, "foo"), new ComputedValue(s_valueSpec2, "bar"));
    _cache.putSharedValues(values);
    for (int i = 0; i < 100; i++) {
      if (_underlying._putValues != null) {
        break;
      }
      pause();
    }
    assertEquals(values, _underlying._putValues);
    assertNull(_underlying._putValue);
  }

  @Test
  public void putValuesCollatedWritePrivate() {
    final List<ComputedValue> values1 = Arrays.asList(new ComputedValue(s_valueSpec1, "foo"), new ComputedValue(s_valueSpec2, "bar"));
    final List<ComputedValue> values2 = Arrays.asList(new ComputedValue(s_valueSpec3, "cow"));
    _cache.putPrivateValues(values1);
    _cache.putPrivateValues(values2);
    _underlying._allowPutValues.countDown();
    for (int i = 0; i < 100; i++) {
      if (_underlying._putValues != null) {
        break;
      }
      pause();
    }
    Collection<ComputedValue> values = _underlying._putValues;
    assertEquals(3, values.size());
    assertTrue(values.containsAll(values1));
    assertTrue(values.containsAll(values2));
    assertNull(_underlying._putValue);
  }

  @Test
  public void putValuesCollatedWriteShared() {
    final List<ComputedValue> values1 = Arrays.asList(new ComputedValue(s_valueSpec1, "foo"), new ComputedValue(s_valueSpec2, "bar"));
    final List<ComputedValue> values2 = Arrays.asList(new ComputedValue(s_valueSpec3, "cow"));
    _cache.putSharedValues(values1);
    _cache.putSharedValues(values2);
    _underlying._allowPutValues.countDown();
    for (int i = 0; i < 100; i++) {
      if (_underlying._putValues != null) {
        break;
      }
      pause();
    }
    Collection<ComputedValue> values = _underlying._putValues;
    assertEquals(3, values.size());
    assertTrue(values.containsAll(values1));
    assertTrue(values.containsAll(values2));
    assertNull(_underlying._putValue);
  }

  @Test
  public void synchronizeCacheNoPendingPrivate() {
    _underlying._allowPutValue.countDown();
    _cache.putPrivateValue(new ComputedValue(s_valueSpec1, "foo"));
    for (int i = 0; i < 100; i++) {
      if (_underlying._putValue != null) {
        break;
      }
      pause();
    }
    assertNotNull(_underlying._putValue);
    _cache.waitForPendingWrites();
  }

  @Test
  public void synchronizeCacheNoPendingShared() {
    _underlying._allowPutValue.countDown();
    _cache.putSharedValue(new ComputedValue(s_valueSpec1, "foo"));
    for (int i = 0; i < 100; i++) {
      if (_underlying._putValue != null) {
        break;
      }
      pause();
    }
    assertNotNull(_underlying._putValue);
    _cache.waitForPendingWrites();
  }

  @Test
  public void synchronizeCacheWithPendingPrivate() {
    _cache.putPrivateValue(new ComputedValue(s_valueSpec1, "foo"));
    new Thread () {
      @Override
      public void run () {
        try {
          Thread.sleep (1000L);
        } catch (InterruptedException e) {
        }
        _underlying._allowPutValue.countDown ();
      }
    }.start ();
    _cache.waitForPendingWrites();
  }

  @Test
  public void synchronizeCacheWithPendingShared() {
    _cache.putSharedValue(new ComputedValue(s_valueSpec1, "foo"));
    new Thread () {
      @Override
      public void run () {
        try {
          Thread.sleep (1000L);
        } catch (InterruptedException e) {
        }
        _underlying._allowPutValue.countDown ();
      }
    }.start ();
    _cache.waitForPendingWrites();
  }

  @Test(expected=OpenGammaRuntimeException.class)
  public void synchronizeCacheWithPendingExceptionPrivate() {
    _cache.putPrivateValue(new ComputedValue(s_valueSpec1, "foo"));
    new Thread () {
      @Override
      public void run () {
        try {
          Thread.sleep (1000L);
        } catch (InterruptedException e) {
        }
        _underlying._throwException = true;
        _underlying._allowPutValue.countDown ();
      }
    }.start ();
    _cache.waitForPendingWrites();
  }

  @Test(expected=OpenGammaRuntimeException.class)
  public void synchronizeCacheWithPendingExceptionShared() {
    _cache.putSharedValue(new ComputedValue(s_valueSpec1, "foo"));
    new Thread () {
      @Override
      public void run () {
        try {
          Thread.sleep (1000L);
        } catch (InterruptedException e) {
        }
        _underlying._throwException = true;
        _underlying._allowPutValue.countDown ();
      }
    }.start ();
    _cache.waitForPendingWrites();
  }

}
