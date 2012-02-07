/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

public class WriteBehindViewComputationCacheTest {

  @DataProvider(name = "cacheHints")
  public static Object[][] data_cacheHints() {
    return new Object[][] {
        {CacheSelectHint.allPrivate()},
        {CacheSelectHint.allShared()},
    };
  }

  //-------------------------------------------------------------------------
  private static class Underlying extends AbstractViewComputationCache {

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
        throw new OpenGammaRuntimeException("woot");
      }
      _putValue = value;
    }

    @Override
    public void putPrivateValue(final ComputedValue value) {
      putValueImpl(value);
    }

    @Override
    public void putSharedValue(final ComputedValue value) {
      putValueImpl(value);
    }

    private void putValuesImpl(final Collection<ComputedValue> values) {
      try {
        _allowPutValues.await(2000L, TimeUnit.MILLISECONDS);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
      if (_throwException) {
        throw new OpenGammaRuntimeException("woot");
      }
      _putValues = new ArrayList<ComputedValue>(values);
    }

    @Override
    public void putPrivateValues(final Collection<ComputedValue> values) {
      putValuesImpl(values);
    }

    @Override
    public void putSharedValues(final Collection<ComputedValue> values) {
      putValuesImpl(values);
    }

    @Override
    public Integer estimateValueSize(final ComputedValue value) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  private static final ValueSpecification s_valueSpec1 = new ValueSpecification(new ValueRequirement("Value 1", new ComputationTargetSpecification(new SimpleSecurity("TEST"))), "Function UID");
  private static final ValueSpecification s_valueSpec2 = new ValueSpecification(new ValueRequirement("Value 2", new ComputationTargetSpecification(new SimpleSecurity("TEST"))), "Function UID");
  private static final ValueSpecification s_valueSpec3 = new ValueSpecification(new ValueRequirement("Value 3", new ComputationTargetSpecification(new SimpleSecurity("TEST"))), "Function UID");

  private final CacheSelectHint _filter;
  private ExecutorService _executorService;
  private Underlying _underlying;
  private WriteBehindViewComputationCache _cache;

  @Factory(dataProvider = "cacheHints")
  public WriteBehindViewComputationCacheTest(final CacheSelectHint filter) {
    _filter = filter;
  }

  @BeforeMethod
  public void init() {
    _executorService = Executors.newCachedThreadPool();
    _underlying = new Underlying();
    _cache = new WriteBehindViewComputationCache(_underlying, _filter, _executorService);
  }

  @AfterMethod
  public void releaseThreads() {
    _underlying._allowPutValue.countDown();
    _underlying._allowPutValues.countDown();
  }

  //-------------------------------------------------------------------------
  @Test
  public void getValueHittingUnderlying() {
    _cache.getValue(s_valueSpec1);
    assertEquals(s_valueSpec1, _underlying._getValue);
  }

  @Test
  public void getValueHittingPending() {
    final ComputedValue value = new ComputedValue(s_valueSpec1, "foo");
    _cache.putValue(value);
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
  public void getValuesOneUnderlying() {
    final Collection<ValueSpecification> valueSpec = Arrays.asList(s_valueSpec1, s_valueSpec2, s_valueSpec3);
    _cache.putValue(new ComputedValue(s_valueSpec1, "foo"));
    _cache.putValue(new ComputedValue(s_valueSpec2, "bar"));
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
    assertNotNull(_underlying._putValues);
    // With nothing pending, we should hit the underlying completely
    _cache.getValues(valueSpec);
    assertEquals(valueSpec, _underlying._getValues);
  }

  @Test
  public void getValuesSomeUnderlying() {
    final Collection<ValueSpecification> valueSpec = Arrays.asList(s_valueSpec1, s_valueSpec2, s_valueSpec3);
    _cache.putValue(new ComputedValue(s_valueSpec1, "foo"));
    final Collection<Pair<ValueSpecification, Object>> values = _cache.getValues(valueSpec);
    assertEquals(1, values.size());
    assertTrue(values.contains(Pair.of(s_valueSpec1, "foo")));
    assertNull(_underlying._putValue);
    assertNull(_underlying._putValues);
    assertEquals(Arrays.asList(s_valueSpec2, s_valueSpec3), _underlying._getValues);
    assertNull(_underlying._getValue);
    _underlying._allowPutValue.countDown();
    _cache.waitForPendingWrites();
    assertNotNull(_underlying._putValue);
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
  public void putValueDirectWrite() {
    _underlying._allowPutValue.countDown();
    final ComputedValue value = new ComputedValue(s_valueSpec1, "foo");
    _cache.putValue(value);
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
  public void putValueCollatedWrite() {
    final ComputedValue value1 = new ComputedValue(s_valueSpec1, "foo");
    final ComputedValue value2 = new ComputedValue(s_valueSpec2, "bar");
    _cache.putValue(value1);
    _cache.putValue(value2);
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
  public void putValuesDirectWrite() {
    _underlying._allowPutValues.countDown();
    final List<ComputedValue> values = Arrays.asList(new ComputedValue(s_valueSpec1, "foo"), new ComputedValue(s_valueSpec2, "bar"));
    _cache.putValues(values);
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
  public void putValuesCollatedWrite() {
    final List<ComputedValue> values1 = Arrays.asList(new ComputedValue(s_valueSpec1, "foo"), new ComputedValue(s_valueSpec2, "bar"));
    final List<ComputedValue> values2 = Arrays.asList(new ComputedValue(s_valueSpec3, "cow"));
    _cache.putValues(values1);
    _cache.putValues(values2);
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
  public void synchronizeCacheNoPending() {
    _underlying._allowPutValue.countDown();
    _cache.putValue(new ComputedValue(s_valueSpec1, "foo"));
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
  public void synchronizeCacheWithPending() {
    _cache.putValue(new ComputedValue(s_valueSpec1, "foo"));
    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
        }
        _underlying._allowPutValue.countDown();
      }
    }.start();
    _cache.waitForPendingWrites();
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void synchronizeCacheWithPendingException() {
    _cache.putValue(new ComputedValue(s_valueSpec1, "foo"));
    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
        }
        _underlying._throwException = true;
        _underlying._allowPutValue.countDown();
      }
    }.start();
    _cache.waitForPendingWrites();
  }

}
