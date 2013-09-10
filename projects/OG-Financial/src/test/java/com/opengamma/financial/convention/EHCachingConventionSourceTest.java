/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import net.sf.ehcache.CacheManager;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link EHCachingConventionSource} class
 */
@Test(groups = {TestGroup.UNIT, "ehcache" })
public class EHCachingConventionSourceTest {

  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  @BeforeMethod
  public void setUp() {
    EHCacheUtils.clear(_cacheManager);
  }

  private Convention convention() {
    final Convention c = new Convention();
    c.setUniqueId(UniqueId.of("Convention", "Test"));
    c.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "Bar")));
    return c;
  }

  public void testGetConventionByExternalId() {
    final ConventionSource underlying = Mockito.mock(ConventionSource.class);
    final EHCachingConventionSource cache = new EHCachingConventionSource(underlying, _cacheManager);
    try {
      final Convention c = convention();
      Mockito.when(underlying.getConvention(ExternalId.of("Test", "Miss"))).thenReturn(null);
      Mockito.when(underlying.getConvention(ExternalId.of("Test", "Foo"))).thenReturn(c);
      // Underlying miss
      assertEquals(cache.getConvention(ExternalId.of("Test", "Miss")), null);
      Mockito.verify(underlying, Mockito.times(1)).getConvention(ExternalId.of("Test", "Miss"));
      // Underlying hit
      assertSame(cache.getConvention(ExternalId.of("Test", "Foo")), c);
      Mockito.verify(underlying, Mockito.times(1)).getConvention(ExternalId.of("Test", "Foo"));
      // Front-cache hit
      assertSame(cache.getConvention(ExternalId.of("Test", "Bar")), c);
      assertSame(cache.getConvention(ExternalId.of("Test", "Foo")), c);
      assertSame(cache.getConvention(ExternalIdBundle.of(ExternalId.of("Test", "A"), ExternalId.of("Test", "Bar"))), c);
      assertSame(cache.getConvention(UniqueId.of("Convention", "Test")), c);
      // Front-cache miss, real cache hit
      cache.emptyFrontCache();
      assertSame(cache.getConvention(ExternalId.of("Test", "Bar")), c);
      assertSame(cache.getConvention(ExternalId.of("Test", "Foo")), c);
      assertSame(cache.getConvention(ExternalIdBundle.of(ExternalId.of("Test", "A"), ExternalId.of("Test", "Bar"))), c);
      assertSame(cache.getConvention(UniqueId.of("Convention", "Test")), c);
      Mockito.verifyNoMoreInteractions(underlying);
      // With class restrictor
      assertSame(cache.getConvention(Convention.class, ExternalId.of("Test", "Miss")), null);
      assertSame(cache.getConvention(Convention.class, ExternalId.of("Test", "Foo")), c);
    } finally {
      cache.shutdown();
    }
  }

  public void testGetConventionByExternalIdBundle() {
    final ConventionSource underlying = Mockito.mock(ConventionSource.class);
    final EHCachingConventionSource cache = new EHCachingConventionSource(underlying, _cacheManager);
    try {
      final Convention c = convention();
      Mockito.when(underlying.getConvention(ExternalIdBundle.of(ExternalId.of("Test", "A"), ExternalId.of("Test", "B")))).thenReturn(null);
      Mockito.when(underlying.getConvention(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "C")))).thenReturn(c);
      // Underlying miss
      assertEquals(cache.getConvention(ExternalIdBundle.of(ExternalId.of("Test", "A"), ExternalId.of("Test", "B"))), null);
      Mockito.verify(underlying, Mockito.times(1)).getConvention(ExternalIdBundle.of(ExternalId.of("Test", "A"), ExternalId.of("Test", "B")));
      // Underlying hit
      assertSame(cache.getConvention(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "C"))), c);
      Mockito.verify(underlying, Mockito.times(1)).getConvention(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "C")));
      // Front-cache hit
      assertSame(cache.getConvention(ExternalIdBundle.of(ExternalId.of("Test", "A"), ExternalId.of("Test", "Bar"))), c);
      assertSame(cache.getConvention(ExternalId.of("Test", "Bar")), c);
      assertSame(cache.getConvention(ExternalId.of("Test", "Foo")), c);
      assertSame(cache.getConvention(UniqueId.of("Convention", "Test")), c);
      // Front-cache miss, real cache hit
      cache.emptyFrontCache();
      assertSame(cache.getConvention(ExternalIdBundle.of(ExternalId.of("Test", "A"), ExternalId.of("Test", "Bar"))), c);
      assertSame(cache.getConvention(ExternalId.of("Test", "Bar")), c);
      assertSame(cache.getConvention(ExternalId.of("Test", "Foo")), c);
      assertSame(cache.getConvention(UniqueId.of("Convention", "Test")), c);
      Mockito.verifyNoMoreInteractions(underlying);
      // With class restrictor
      assertEquals(cache.getConvention(Convention.class, ExternalIdBundle.of(ExternalId.of("Test", "A"), ExternalId.of("Test", "B"))), null);
      assertSame(cache.getConvention(Convention.class, ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "C"))), c);
    } finally {
      cache.shutdown();
    }
  }

  public void testGetConventionByUniqueId() {
    final ConventionSource underlying = Mockito.mock(ConventionSource.class);
    final EHCachingConventionSource cache = new EHCachingConventionSource(underlying, _cacheManager);
    try {
      final Convention c = convention();
      Mockito.when(underlying.getConvention(UniqueId.of("Convention", "Miss"))).thenReturn(null);
      Mockito.when(underlying.getConvention(UniqueId.of("Convention", "Test"))).thenReturn(c);
      // Underlying miss
      assertEquals(cache.getConvention(UniqueId.of("Convention", "Miss")), null);
      Mockito.verify(underlying, Mockito.times(1)).getConvention(UniqueId.of("Convention", "Miss"));
      // Underlying hit
      assertSame(cache.getConvention(UniqueId.of("Convention", "Test")), c);
      Mockito.verify(underlying, Mockito.times(1)).getConvention(UniqueId.of("Convention", "Test"));
      // Front-cache hit
      assertSame(cache.getConvention(UniqueId.of("Convention", "Test")), c);
      assertSame(cache.getConvention(ExternalIdBundle.of(ExternalId.of("Test", "A"), ExternalId.of("Test", "Bar"))), c);
      assertSame(cache.getConvention(ExternalId.of("Test", "Bar")), c);
      assertSame(cache.getConvention(ExternalId.of("Test", "Foo")), c);
      // Front-cache miss, real cache hit
      cache.emptyFrontCache();
      assertSame(cache.getConvention(UniqueId.of("Convention", "Test")), c);
      assertSame(cache.getConvention(ExternalIdBundle.of(ExternalId.of("Test", "A"), ExternalId.of("Test", "Bar"))), c);
      assertSame(cache.getConvention(ExternalId.of("Test", "Bar")), c);
      assertSame(cache.getConvention(ExternalId.of("Test", "Foo")), c);
      Mockito.verifyNoMoreInteractions(underlying);
      // With class restrictor
      assertEquals(cache.getConvention(Convention.class, UniqueId.of("Convention", "Miss")), null);
      assertSame(cache.getConvention(Convention.class, UniqueId.of("Convention", "Test")), c);
    } finally {
      cache.shutdown();
    }
  }

}
