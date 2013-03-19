/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

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
 * Test.
 */
@Test(groups = {TestGroup.UNIT, "ehcache"}, singleThreaded = true)
public class EHCachingConventionBundleSourceTest {

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

  //-------------------------------------------------------------------------
  private ConventionBundle createBundle() {
    final ConventionBundle bundle = Mockito.mock(ConventionBundle.class);
    Mockito.when(bundle.getUniqueId()).thenReturn(UniqueId.of("Mock", "0"));
    Mockito.when(bundle.getIdentifiers()).thenReturn(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "Bar")));
    return bundle;
  }

  public void testByIdentifierUnderlyingMiss() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      Mockito.when(mock.getConventionBundle(ExternalId.of("Test", "Foo"))).thenReturn(null);
      assertSame(cache.getConventionBundle(ExternalId.of("Test", "Foo")), null);
      Mockito.verify(mock).getConventionBundle(ExternalId.of("Test", "Foo"));
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByIdentifierUnderlyingHit() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      final ConventionBundle bundle = createBundle();
      Mockito.when(mock.getConventionBundle(ExternalId.of("Test", "Foo"))).thenReturn(bundle);
      assertSame(cache.getConventionBundle(ExternalId.of("Test", "Foo")), bundle);
      Mockito.verify(mock).getConventionBundle(ExternalId.of("Test", "Foo"));
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByIdentifierFrontCacheHit() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      final ConventionBundle bundle = createBundle();
      Mockito.when(mock.getConventionBundle(ExternalId.of("Test", "Foo"))).thenReturn(bundle);
      assertSame(cache.getConventionBundle(ExternalId.of("Test", "Foo")), bundle);
      Mockito.verify(mock).getConventionBundle(ExternalId.of("Test", "Foo"));
      cache.emptyEHCache();
      assertSame(cache.getConventionBundle(ExternalId.of("Test", "Foo")), bundle);
      Mockito.verify(mock).getConventionBundle(ExternalId.of("Test", "Foo"));
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByIdentifierEHCacheHit() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      final ConventionBundle bundle = createBundle();
      Mockito.when(mock.getConventionBundle(ExternalId.of("Test", "Foo"))).thenReturn(bundle);
      assertSame(cache.getConventionBundle(ExternalId.of("Test", "Foo")), bundle);
      Mockito.verify(mock).getConventionBundle(ExternalId.of("Test", "Foo"));
      cache.emptyFrontCache();
      assertSame(cache.getConventionBundle(ExternalId.of("Test", "Foo")), bundle);
      Mockito.verify(mock).getConventionBundle(ExternalId.of("Test", "Foo"));
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByBundleUnderlyingMiss() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      Mockito.when(mock.getConventionBundle(ExternalId.of("Test", "Foo").toBundle())).thenReturn(null);
      assertSame(cache.getConventionBundle(ExternalId.of("Test", "Foo").toBundle()), null);
      Mockito.verify(mock).getConventionBundle(ExternalId.of("Test", "Foo").toBundle());
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByBundleUnderlyingHit() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      final ConventionBundle bundle = createBundle();
      Mockito.when(mock.getConventionBundle(ExternalId.of("Test", "Foo").toBundle())).thenReturn(bundle);
      assertSame(cache.getConventionBundle(ExternalId.of("Test", "Foo").toBundle()), bundle);
      Mockito.verify(mock).getConventionBundle(ExternalId.of("Test", "Foo").toBundle());
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByBundleFrontCacheHit() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      final ConventionBundle bundle = createBundle();
      Mockito.when(mock.getConventionBundle(UniqueId.of("Mock", "0"))).thenReturn(bundle);
      cache.getConventionBundle(UniqueId.of("Mock", "0"));
      cache.emptyEHCache();
      assertSame(cache.getConventionBundle(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "Bar"))), bundle);
      Mockito.verify(mock).getConventionBundle(UniqueId.of("Mock", "0"));
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByBundleEHCacheHit() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      final ConventionBundle bundle = createBundle();
      Mockito.when(mock.getConventionBundle(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "Bar")))).thenReturn(bundle);
      assertSame(cache.getConventionBundle(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "Bar"))), bundle);
      Mockito.verify(mock).getConventionBundle(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "Bar")));
      cache.emptyFrontCache();
      assertSame(cache.getConventionBundle(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "Bar"))), bundle);
      Mockito.verify(mock).getConventionBundle(ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "Bar")));
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByUniqueIdUnderlyingMiss() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      Mockito.when(mock.getConventionBundle(UniqueId.of("Mock", "-1"))).thenReturn(null);
      assertSame(cache.getConventionBundle(UniqueId.of("Mock", "-1")), null);
      Mockito.verify(mock).getConventionBundle(UniqueId.of("Mock", "-1"));
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByUniqueIdUnderlyingHit() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      final ConventionBundle bundle = createBundle();
      Mockito.when(mock.getConventionBundle(UniqueId.of("Mock", "0"))).thenReturn(bundle);
      assertSame(cache.getConventionBundle(UniqueId.of("Mock", "0")), bundle);
      Mockito.verify(mock).getConventionBundle(UniqueId.of("Mock", "0"));
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByUniqueIdFrontCacheHit() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      final ConventionBundle bundle = createBundle();
      Mockito.when(mock.getConventionBundle(UniqueId.of("Mock", "0"))).thenReturn(bundle);
      assertSame(cache.getConventionBundle(UniqueId.of("Mock", "0")), bundle);
      Mockito.verify(mock).getConventionBundle(UniqueId.of("Mock", "0"));
      cache.emptyEHCache();
      assertSame(cache.getConventionBundle(UniqueId.of("Mock", "0")), bundle);
      Mockito.verify(mock).getConventionBundle(UniqueId.of("Mock", "0"));
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

  public void testByUniqueIdEHCacheHit() {
    final ConventionBundleSource mock = Mockito.mock(ConventionBundleSource.class);
    final EHCachingConventionBundleSource cache = new EHCachingConventionBundleSource(mock, _cacheManager);
    try {
      final ConventionBundle bundle = createBundle();
      Mockito.when(mock.getConventionBundle(UniqueId.of("Mock", "0"))).thenReturn(bundle);
      assertSame(cache.getConventionBundle(UniqueId.of("Mock", "0")), bundle);
      Mockito.verify(mock).getConventionBundle(UniqueId.of("Mock", "0"));
      cache.emptyFrontCache();
      assertSame(cache.getConventionBundle(UniqueId.of("Mock", "0")), bundle);
      Mockito.verify(mock).getConventionBundle(UniqueId.of("Mock", "0"));
      Mockito.verifyNoMoreInteractions(mock);
    } finally {
      cache.shutdown();
    }
  }

}
