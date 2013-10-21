/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

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
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.async.AsynchronousOperation;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

@Test(groups = TestGroup.UNIT_SLOW)
public class WriteBehindViewComputationCacheTest {

  @DataProvider(name = "cacheHints")
  public static Object[][] data_cacheHints() {
    return new Object[][] {
        {CacheSelectHint.allPrivate() },
        {CacheSelectHint.allShared() },
    };
  }

  //-------------------------------------------------------------------------
  private static class Underlying extends AbstractViewComputationCache {

    private ValueSpecification _getValue;
    private Collection<ValueSpecification> _getValues;
    private volatile ComputedValue _putValue;
    private volatile Collection<ComputedValue> _putValues;

    private volatile boolean _throwException;

    private final CountDownLatch _allowPutValue = new CountDownLatch(1);
    private final CountDownLatch _allowPutValues = new CountDownLatch(1);

    @Override
    public Object getValue(final ValueSpecification specification) {
      _getValue = specification;
      return null;
    }

    @Override
    public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
      _getValues = specifications;
      return Collections.emptyList();
    }

    private void putValueImpl(final ComputedValue value) {
      try {
        _allowPutValue.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      } catch (final InterruptedException ex) {
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

    private void putValuesImpl(final Collection<? extends ComputedValue> values) {
      try {
        _allowPutValues.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      } catch (final InterruptedException ex) {
        ex.printStackTrace();
      }
      if (_throwException) {
        throw new OpenGammaRuntimeException("woot");
      }
      _putValues = new ArrayList<ComputedValue>(values);
    }

    @Override
    public void putPrivateValues(final Collection<? extends ComputedValue> values) {
      putValuesImpl(values);
    }

    @Override
    public void putSharedValues(final Collection<? extends ComputedValue> values) {
      putValuesImpl(values);
    }

    @Override
    public Integer estimateValueSize(final ComputedValue value) {
      return null;
    }
  }

  private static final ComputationTargetSpecification s_targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("TEST", "SECURITY"));
  private static final ValueProperties s_properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "Function UID").get();
  private static final ValueSpecification s_valueSpec1 = new ValueSpecification("Value 1", s_targetSpec, s_properties);
  private static final ValueSpecification s_valueSpec2 = new ValueSpecification("Value 2", s_targetSpec, s_properties);
  private static final ValueSpecification s_valueSpec3 = new ValueSpecification("Value 3", s_targetSpec, s_properties);
  private static final ValueSpecification s_valueSpec4 = new ValueSpecification("Value 4", s_targetSpec, s_properties);

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
    _cache = new WriteBehindViewComputationCache(_underlying, _executorService, true, true);
  }

  @AfterMethod
  public void releaseThreads() {
    _underlying._allowPutValue.countDown();
    _underlying._allowPutValues.countDown();
  }

  private void flush(final WriteBehindViewComputationCache cache) {
    try {
      cache.flush();
    } catch (final AsynchronousExecution e) {
      AsynchronousOperation.getResult(e);
    }
    cache.clearBuffer();
  }

  @Test
  public void getValueHittingUnderlying() {
    _cache.getValue(s_valueSpec1);
    assertEquals(s_valueSpec1, _underlying._getValue);
  }

  @Test
  public void getValueHittingPending() {
    final ComputedValue value = new ComputedValue(s_valueSpec1, "foo");
    _cache.putValue(value, _filter);
    assertEquals("foo", _cache.getValue(s_valueSpec1));
    assertNull(_underlying._putValue);
    assertNull(_underlying._getValue);
  }

  @Test
  public void getValuesHittingUnderlying() {
    final Collection<ValueSpecification> valueSpec = Arrays.asList(s_valueSpec1, s_valueSpec2, s_valueSpec3, s_valueSpec4);
    _cache.getValues(valueSpec);
    assertEquals(valueSpec, _underlying._getValues);
  }

  @Test
  public void getValuesOneUnderlying() {
    final Collection<ValueSpecification> valueSpec = Arrays.asList(s_valueSpec1, s_valueSpec2, s_valueSpec3, s_valueSpec4);
    _cache.putValue(new ComputedValue(s_valueSpec1, "foo"), _filter);
    _cache.putValue(new ComputedValue(s_valueSpec2, "bar"), _filter);
    _cache.putValue(new ComputedValue(s_valueSpec3, "cow"), _filter);
    final Collection<Pair<ValueSpecification, Object>> values = _cache.getValues(valueSpec);
    assertEquals(valueSpec.size(), values.size());
    assertTrue(values.contains(Pairs.of(s_valueSpec1, "foo")));
    assertTrue(values.contains(Pairs.of(s_valueSpec2, "bar")));
    assertTrue(values.contains(Pairs.of(s_valueSpec3, "cow")));
    assertNull(_underlying._putValue);
    assertNull(_underlying._putValues);
    assertNull(_underlying._getValues);
    assertEquals(s_valueSpec4, _underlying._getValue);
    _underlying._allowPutValue.countDown();
    _underlying._allowPutValues.countDown();
    flush(_cache);
    assertNotNull(_underlying._putValues);
    if (_underlying._putValue != null) {
      assertEquals(2, _underlying._putValues.size());
    } else {
      assertEquals(3, _underlying._putValues.size());
    }
    // With nothing pending, we should hit the underlying completely
    _cache.getValues(valueSpec);
    assertEquals(valueSpec, _underlying._getValues);
  }

  @Test
  public void getValuesSomeUnderlying() {
    final Collection<ValueSpecification> valueSpec = Arrays.asList(s_valueSpec1, s_valueSpec2, s_valueSpec3);
    _cache.putValue(new ComputedValue(s_valueSpec1, "foo"), _filter);
    final Collection<Pair<ValueSpecification, Object>> values = _cache.getValues(valueSpec);
    assertEquals(1, values.size());
    assertTrue(values.contains(Pairs.of(s_valueSpec1, "foo")));
    assertNull(_underlying._putValue);
    assertNull(_underlying._putValues);
    assertEquals(Arrays.asList(s_valueSpec2, s_valueSpec3), _underlying._getValues);
    assertNull(_underlying._getValue);
    _underlying._allowPutValue.countDown();
    flush(_cache);
    assertNotNull(_underlying._putValue);
    // With nothing pending, we should hit the underlying completely
    _cache.getValues(valueSpec);
    assertEquals(valueSpec, _underlying._getValues);
  }

  private static final long PAUSE = 10;

  @Test
  public void putValueDirectWrite() throws InterruptedException {
    _underlying._allowPutValue.countDown();
    final ComputedValue value = new ComputedValue(s_valueSpec1, "foo");
    _cache.putValue(value, _filter);
    for (long i = Timeout.standardTimeoutMillis(); i > 0; i -= PAUSE) {
      if (_underlying._putValue != null) {
        break;
      }
      Thread.sleep(PAUSE);
    }
    assertEquals(value, _underlying._putValue);
    assertNull(_underlying._putValues);
  }

  @Test
  public void putValueCollatedWrite() throws InterruptedException {
    final ComputedValue value0 = new ComputedValue(s_valueSpec4, "null");
    final ComputedValue value1 = new ComputedValue(s_valueSpec1, "foo");
    final ComputedValue value2 = new ComputedValue(s_valueSpec2, "bar");
    _cache.putValue(value0, _filter);
    Thread.sleep(1000);
    _cache.putValue(value1, _filter);
    _cache.putValue(value2, _filter);
    _underlying._allowPutValues.countDown();
    for (long i = Timeout.standardTimeoutMillis(); i > 0; i -= PAUSE) {
      if (_underlying._putValues != null) {
        break;
      }
      Thread.sleep(PAUSE);
    }
    assertEquals(value0, _underlying._putValue);
    assertEquals(Arrays.asList(value1, value2), _underlying._putValues);
  }

  @Test
  public void putValuesDirectWrite() throws InterruptedException {
    _underlying._allowPutValues.countDown();
    final List<ComputedValue> values = Arrays.asList(new ComputedValue(s_valueSpec1, "foo"), new ComputedValue(s_valueSpec2, "bar"));
    _cache.putValues(values, _filter);
    for (long i = Timeout.standardTimeoutMillis(); i > 0; i -= PAUSE) {
      if (_underlying._putValues != null) {
        break;
      }
      Thread.sleep(PAUSE);
    }
    assertEquals(values, _underlying._putValues);
    assertNull(_underlying._putValue);
  }

  @Test
  public void putValuesCollatedWrite() throws InterruptedException {
    final ComputedValue value0 = new ComputedValue(s_valueSpec4, "null");
    final List<ComputedValue> values1 = Arrays.asList(new ComputedValue(s_valueSpec1, "foo"), new ComputedValue(s_valueSpec2, "bar"));
    final List<ComputedValue> values2 = Arrays.asList(new ComputedValue(s_valueSpec3, "cow"));
    _cache.putValue(value0, _filter);
    Thread.sleep(1000);
    _cache.putValues(values1, _filter);
    _cache.putValues(values2, _filter);
    _underlying._allowPutValues.countDown();
    for (long i = Timeout.standardTimeoutMillis(); i > 0; i -= PAUSE) {
      if (_underlying._putValues != null) {
        break;
      }
      Thread.sleep(PAUSE);
    }
    final Collection<ComputedValue> values = _underlying._putValues;
    assertEquals(3, values.size());
    assertTrue(values.containsAll(values1));
    assertTrue(values.containsAll(values2));
  }

  @Test
  public void synchronizeCacheNoPending() throws InterruptedException {
    _underlying._allowPutValue.countDown();
    _cache.putValue(new ComputedValue(s_valueSpec1, "foo"), _filter);
    for (long i = Timeout.standardTimeoutMillis(); i > 0; i -= PAUSE) {
      if (_underlying._putValue != null) {
        break;
      }
      Thread.sleep(PAUSE);
    }
    assertNotNull(_underlying._putValue);
    flush(_cache);
  }

  @Test
  public void synchronizeCacheWithPending() {
    _cache.putValue(new ComputedValue(s_valueSpec1, "foo"), _filter);
    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(Timeout.standardTimeoutMillis() / 4);
        } catch (final InterruptedException e) {
        }
        _underlying._allowPutValue.countDown();
      }
    }.start();
    flush(_cache);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void synchronizeCacheWithPendingException() {
    _cache.putValue(new ComputedValue(s_valueSpec1, "foo"), _filter);
    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(Timeout.standardTimeoutMillis() / 4);
        } catch (final InterruptedException e) {
        }
        _underlying._throwException = true;
        _underlying._allowPutValue.countDown();
      }
    }.start();
    flush(_cache);
  }

}
