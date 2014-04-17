/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import net.sf.ehcache.CacheManager;

import org.mockito.Mockito;
import org.springframework.context.Lifecycle;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link EHCachingTempTargetRepository} class.
 */
@Test(groups = TestGroup.UNIT)
public class EHCachingTempTargetRepositoryTest {

  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCachingTempTargetRepository.class);
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  public void testGet() {
    final TempTargetRepository underlying = Mockito.mock(TempTargetRepository.class);
    Mockito.when(underlying.get(UniqueId.of("Foo", "Bar"))).thenReturn(new MockTempTarget("X"));
    final TempTargetRepository cache = new EHCachingTempTargetRepository(underlying, _cacheManager);
    final TempTarget result = cache.get(UniqueId.of("Foo", "Bar"));
    assertEquals(result, new MockTempTarget("X"));
    assertSame(cache.get(UniqueId.of("Foo", "Bar")), result);
    Mockito.verify(underlying, Mockito.times(1)).get(UniqueId.of("Foo", "Bar"));
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testCacheManager() {
    final TempTargetRepository underlying = Mockito.mock(TempTargetRepository.class);
    final ChangeManager changeManager = Mockito.mock(ChangeManager.class);
    Mockito.when(underlying.changeManager()).thenReturn(changeManager);
    final TempTargetRepository cache = new EHCachingTempTargetRepository(underlying, _cacheManager);
    assertSame(cache.changeManager(), changeManager);
    Mockito.verify(underlying, Mockito.only()).changeManager();
  }

  public void testLocateOrStore() {
    final TempTargetRepository underlying = Mockito.mock(TempTargetRepository.class);
    final TempTarget target = new MockTempTarget("X");
    Mockito.when(underlying.locateOrStore(target)).thenReturn(UniqueId.of("Foo", "Bar"));
    final TempTargetRepository cache = new EHCachingTempTargetRepository(underlying, _cacheManager);
    assertEquals(cache.locateOrStore(target), UniqueId.of("Foo", "Bar"));
    assertEquals(cache.locateOrStore(target), UniqueId.of("Foo", "Bar"));
    Mockito.verify(underlying, Mockito.times(1)).locateOrStore(target);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testStartStop_noPassthrough() {
    final TempTargetRepository underlying = Mockito.mock(TempTargetRepository.class);
    final EHCachingTempTargetRepository cache = new EHCachingTempTargetRepository(underlying, _cacheManager);
    assertFalse(cache.isRunning());
    cache.start();
    assertTrue(cache.isRunning());
    cache.stop();
    assertFalse(cache.isRunning());
    Mockito.verifyZeroInteractions(underlying);
  }

  private static class LifecycleTempTargetRepository implements TempTargetRepository, Lifecycle {

    private final Lifecycle _lifecycle;

    public LifecycleTempTargetRepository(final Lifecycle lifecycle) {
      _lifecycle = lifecycle;
    }

    // TempTargetRepository

    @Override
    public TempTarget get(UniqueId identifier) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ChangeManager changeManager() {
      throw new UnsupportedOperationException();
    }

    @Override
    public UniqueId locateOrStore(TempTarget target) {
      throw new UnsupportedOperationException();
    }

    // Lifecycle

    @Override
    public void start() {
      _lifecycle.start();
    }

    @Override
    public void stop() {
      _lifecycle.stop();
    }

    @Override
    public boolean isRunning() {
      return _lifecycle.isRunning();
    }

  }

  public void testStartStop_passThrough() {
    final Lifecycle lifecycle = Mockito.mock(Lifecycle.class);
    Mockito.when(lifecycle.isRunning()).thenReturn(true);
    final TempTargetRepository underlying = new LifecycleTempTargetRepository(lifecycle);
    final EHCachingTempTargetRepository cache = new EHCachingTempTargetRepository(underlying, _cacheManager);
    Mockito.verify(lifecycle, Mockito.times(0)).start();
    Mockito.verify(lifecycle, Mockito.times(0)).stop();
    Mockito.verify(lifecycle, Mockito.times(0)).isRunning();
    assertFalse(cache.isRunning());
    cache.start();
    Mockito.verify(lifecycle, Mockito.times(1)).start();
    assertTrue(cache.isRunning());
    Mockito.verify(lifecycle, Mockito.times(1)).isRunning();
    cache.stop();
    Mockito.verify(lifecycle, Mockito.times(1)).stop();
    assertFalse(cache.isRunning());
    Mockito.verify(lifecycle, Mockito.times(1)).start();
    Mockito.verify(lifecycle, Mockito.times(1)).isRunning();
    Mockito.verify(lifecycle, Mockito.times(1)).stop();
  }

}
