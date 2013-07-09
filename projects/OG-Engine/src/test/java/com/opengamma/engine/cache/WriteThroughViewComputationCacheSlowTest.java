/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the {@link WriteThroughViewComputationCache} class.
 */
@Test(groups = TestGroup.UNIT_SLOW)
public class WriteThroughViewComputationCacheSlowTest {

  private class MockViewComputationCache extends AbstractViewComputationCache {

    private boolean _block;

    public synchronized void unblock() {
      _block = false;
      notifyAll();
    }

    public synchronized void block() {
      _block = true;
    }

    private synchronized void waitIfBlocked() {
      try {
        while (_block) {
          wait();
        }
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
    }

    @Override
    public Object getValue(ValueSpecification specification) {
      waitIfBlocked();
      if (specification == _value2) {
        return null;
      } else {
        return new Object();
      }
    }

    @Override
    public void putSharedValue(ComputedValue value) {
      fail();
    }

    @Override
    public void putPrivateValue(ComputedValue value) {
      fail();
    }

    @Override
    public Integer estimateValueSize(ComputedValue value) {
      fail();
      return null;
    }

  }

  private class MockWriteThrough extends WriteThroughViewComputationCache {

    private boolean _block;

    public MockWriteThrough(final ViewComputationCache underlying) {
      super(underlying);
    }

    public synchronized void unblock() {
      _block = false;
      notifyAll();
    }

    public synchronized void block() {
      _block = true;
    }

    @Override
    protected synchronized Pending waitFor(final ValueSpecification specification) {
      try {
        while (_block) {
          wait();
        }
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
      return super.waitFor(specification);
    }

  }

  private final ValueSpecification _value1 = Mockito.mock(ValueSpecification.class);
  private final ValueSpecification _value2 = Mockito.mock(ValueSpecification.class);
  private final ValueSpecification _value3 = Mockito.mock(ValueSpecification.class);
  private final ValueSpecification _value4 = Mockito.mock(ValueSpecification.class);

  public void testGetValue() throws InterruptedException {
    final ExecutorService spawn = Executors.newCachedThreadPool();
    try {
      final MockViewComputationCache underlying = new MockViewComputationCache();
      final MockWriteThrough cache = new MockWriteThrough(underlying);
      // read-through, return value
      Object v = cache.getValue(_value1);
      assertNotNull(v);
      // hit value
      assertSame(cache.getValue(_value1), v);
      // read-through, return null
      v = cache.getValue(_value2);
      assertNull(v);
      // hit null
      assertSame(cache.getValue(_value2), v);
      // block second reader until first completes
      underlying.block();
      final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();
      for (int i = 0; i < 2; i++) {
        spawn.submit(new Runnable() {
          @Override
          public void run() {
            values.add(cache.getValue(_value3));
          }
        });
      }
      Thread.sleep(Timeout.standardTimeoutMillis());
      underlying.unblock();
      v = values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertNotNull(v);
      assertSame(values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS), v);
      // nearly block second reader
      underlying.block();
      cache.block();
      for (int i = 0; i < 2; i++) {
        spawn.submit(new Runnable() {
          @Override
          public void run() {
            values.add(cache.getValue(_value4));
          }
        });
      }
      Thread.sleep(Timeout.standardTimeoutMillis());
      underlying.unblock();
      v = values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertNotNull(v);
      cache.unblock();
      assertSame(values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS), v);
    } finally {
      spawn.shutdownNow();
    }
  }

  public void testGetValueFilter() throws InterruptedException {
    final ExecutorService spawn = Executors.newCachedThreadPool();
    try {
      final MockViewComputationCache underlying = new MockViewComputationCache();
      final MockWriteThrough cache = new MockWriteThrough(underlying);
      // read-through, return value
      Object v = cache.getValue(_value1, CacheSelectHint.allPrivate());
      assertNotNull(v);
      // hit value
      assertSame(cache.getValue(_value1, CacheSelectHint.allPrivate()), v);
      // read-through, return null
      v = cache.getValue(_value2, CacheSelectHint.allPrivate());
      assertNull(v);
      // hit null
      assertSame(cache.getValue(_value2, CacheSelectHint.allPrivate()), v);
      // block second reader until first completes
      underlying.block();
      final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();
      for (int i = 0; i < 2; i++) {
        spawn.submit(new Runnable() {
          @Override
          public void run() {
            values.add(cache.getValue(_value3, CacheSelectHint.allPrivate()));
          }
        });
      }
      Thread.sleep(Timeout.standardTimeoutMillis());
      underlying.unblock();
      v = values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertNotNull(v);
      assertSame(values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS), v);
      // nearly block second reader
      underlying.block();
      cache.block();
      for (int i = 0; i < 2; i++) {
        spawn.submit(new Runnable() {
          @Override
          public void run() {
            values.add(cache.getValue(_value4, CacheSelectHint.allPrivate()));
          }
        });
      }
      Thread.sleep(Timeout.standardTimeoutMillis());
      underlying.unblock();
      v = values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertNotNull(v);
      cache.unblock();
      assertSame(values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS), v);
    } finally {
      spawn.shutdownNow();
    }
  }

  public void testGetValues() throws InterruptedException {
    final ExecutorService spawn = Executors.newCachedThreadPool();
    try {
      final MockViewComputationCache underlying = new MockViewComputationCache();
      final MockWriteThrough cache = new MockWriteThrough(underlying);
      // read-through, return value
      Collection<Pair<ValueSpecification, Object>> vs = cache.getValues(Collections.singleton(_value1));
      assertNotNull(vs);
      assertEquals(vs.size(), 1);
      Pair<ValueSpecification, Object> v = vs.iterator().next();
      assertSame(v.getFirst(), _value1);
      Object vo = v.getSecond();
      assertNotNull(vo);
      // hit value
      assertSame(cache.getValues(Collections.singleton(_value1)).iterator().next().getSecond(), vo);
      // read-through, return null
      vs = cache.getValues(Collections.singleton(_value2));
      vo = vs.iterator().next().getSecond();
      assertNull(vo);
      // hit null
      assertSame(cache.getValues(Collections.singleton(_value2)).iterator().next().getSecond(), vo);
      // block second reader until first completes
      underlying.block();
      final BlockingQueue<Pair<ValueSpecification, Object>> values = new LinkedBlockingQueue<Pair<ValueSpecification, Object>>();
      for (int i = 0; i < 2; i++) {
        spawn.submit(new Runnable() {
          @Override
          public void run() {
            values.addAll(cache.getValues(Collections.singleton(_value3)));
          }
        });
      }
      Thread.sleep(Timeout.standardTimeoutMillis());
      underlying.unblock();
      v = values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertNotNull(v);
      assertSame(values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS).getSecond(), v.getSecond());
      // nearly block second reader
      underlying.block();
      cache.block();
      for (int i = 0; i < 2; i++) {
        spawn.submit(new Runnable() {
          @Override
          public void run() {
            values.addAll(cache.getValues(Collections.singleton(_value4)));
          }
        });
      }
      Thread.sleep(Timeout.standardTimeoutMillis());
      underlying.unblock();
      v = values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertNotNull(v);
      cache.unblock();
      assertSame(values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS).getSecond(), v.getSecond());
    } finally {
      spawn.shutdownNow();
    }
  }

  public void testGetValuesFilter() throws InterruptedException {
    final ExecutorService spawn = Executors.newCachedThreadPool();
    try {
      final MockViewComputationCache underlying = new MockViewComputationCache();
      final MockWriteThrough cache = new MockWriteThrough(underlying);
      // read-through, return value
      Collection<Pair<ValueSpecification, Object>> vs = cache.getValues(Collections.singleton(_value1), CacheSelectHint.allShared());
      assertNotNull(vs);
      assertEquals(vs.size(), 1);
      Pair<ValueSpecification, Object> v = vs.iterator().next();
      assertSame(v.getFirst(), _value1);
      Object vo = v.getSecond();
      assertNotNull(vo);
      // hit value
      assertSame(cache.getValues(Collections.singleton(_value1), CacheSelectHint.allShared()).iterator().next().getSecond(), vo);
      // read-through, return null
      vs = cache.getValues(Collections.singleton(_value2), CacheSelectHint.allShared());
      vo = vs.iterator().next().getSecond();
      assertNull(vo);
      // hit null
      assertSame(cache.getValues(Collections.singleton(_value2), CacheSelectHint.allShared()).iterator().next().getSecond(), vo);
      // block second reader until first completes
      underlying.block();
      final BlockingQueue<Pair<ValueSpecification, Object>> values = new LinkedBlockingQueue<Pair<ValueSpecification, Object>>();
      for (int i = 0; i < 2; i++) {
        spawn.submit(new Runnable() {
          @Override
          public void run() {
            values.addAll(cache.getValues(Collections.singleton(_value3), CacheSelectHint.allShared()));
          }
        });
      }
      Thread.sleep(Timeout.standardTimeoutMillis());
      underlying.unblock();
      v = values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertNotNull(v);
      assertSame(values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS).getSecond(), v.getSecond());
      // nearly block second reader
      underlying.block();
      cache.block();
      for (int i = 0; i < 2; i++) {
        spawn.submit(new Runnable() {
          @Override
          public void run() {
            values.addAll(cache.getValues(Collections.singleton(_value4), CacheSelectHint.allShared()));
          }
        });
      }
      Thread.sleep(Timeout.standardTimeoutMillis());
      underlying.unblock();
      v = values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertNotNull(v);
      cache.unblock();
      assertSame(values.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS).getSecond(), v.getSecond());
    } finally {
      spawn.shutdownNow();
    }
  }

}
