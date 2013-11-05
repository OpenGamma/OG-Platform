/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.AssertJUnit.fail;
import net.sf.ehcache.CacheManager;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link EHCachingConventionSource} class
 */
@Test(groups = {TestGroup.UNIT, "ehcache" })
public class EHCachingConventionSourceTest {

  private static final UniqueId UID = UniqueId.of("Convention", "Test", "1");
  private static final UniqueId UID_MISS = UniqueId.of("Convention", "Miss", "1");
  private static final ObjectId OID = ObjectId.of("Convention", "Test");
  private static final ObjectId OID_MISS = ObjectId.of("Convention", "Miss");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ExternalId.of("Test", "Foo"), ExternalId.of("Test", "Bar"));
  private static final ExternalIdBundle BUNDLE_MISS = ExternalIdBundle.of(ExternalId.of("Test", "Blas"), ExternalId.of("Test", "Bar"));
  private static final VersionCorrection VERSION_CORRECTION = VersionCorrection.of(
      Instant.now().truncatedTo(ChronoUnit.HOURS).minusSeconds(3600), Instant.now().truncatedTo(ChronoUnit.HOURS));

  private CacheManager _cacheManager;
  private Convention _convention;
  private ConventionSource _underlying;
  private EHCachingConventionSource _source;

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
    _convention = new MockConvention("Mock", UID, "Test", BUNDLE);
    _underlying = Mockito.mock(ConventionSource.class);
    _source = new EHCachingConventionSource(_underlying, _cacheManager);
    _source.emptyEHCache();
    _source.emptyFrontCache();
  }

  //-------------------------------------------------------------------------
  public void test_get_byUniqueId_notFound() {
    Mockito.when(_underlying.get(UID_MISS)).thenThrow(new DataNotFoundException("Not found"));
    try {
      assertEquals(_source.get(UID_MISS), null);
      Mockito.verify(_underlying, Mockito.times(1)).get(UID_MISS);
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
  }

  public void test_get_byUniqueId_found() {
    Mockito.when(_underlying.get(UID)).thenReturn(_convention);
    // hit underlying
    assertSame(_source.get(UID), _convention);
    Mockito.verify(_underlying, Mockito.times(1)).get(UID);
    // hit front cache
    assertSame(_source.get(UID), _convention);
    Mockito.verifyNoMoreInteractions(_underlying);
    // hit back cache
    _source.emptyFrontCache();
    assertSame(_source.get(UID), _convention);
    Mockito.verifyNoMoreInteractions(_underlying);
  }

  //-------------------------------------------------------------------------
  public void test_get_byObjectIdVC_notFound() {
    Mockito.when(_underlying.get(OID_MISS, VERSION_CORRECTION)).thenThrow(new DataNotFoundException("Not found"));
    try {
      assertEquals(_source.get(OID_MISS, VERSION_CORRECTION), null);
      Mockito.verify(_underlying, Mockito.times(1)).get(OID_MISS, VERSION_CORRECTION);
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
  }

  public void test_get_byObjectIdVC_found() {
    Mockito.when(_underlying.get(OID, VERSION_CORRECTION)).thenReturn(_convention);
    // hit underlying
    assertSame(_source.get(OID, VERSION_CORRECTION), _convention);
    Mockito.verify(_underlying, Mockito.times(1)).get(OID, VERSION_CORRECTION);
    // hit front cache
    assertSame(_source.get(OID, VERSION_CORRECTION), _convention);
    Mockito.verifyNoMoreInteractions(_underlying);
    // hit back cache
    _source.emptyFrontCache();
    assertSame(_source.get(OID, VERSION_CORRECTION), _convention);
    Mockito.verifyNoMoreInteractions(_underlying);
    // other things cached
    assertSame(_source.get(UID), _convention);
    assertSame(_source.getSingle(BUNDLE, VERSION_CORRECTION), _convention);
    Mockito.verifyNoMoreInteractions(_underlying);
  }

  //-------------------------------------------------------------------------
  public void test_get_byBundleVC_notFound() {
    Mockito.when(_underlying.getSingle(BUNDLE_MISS, VERSION_CORRECTION)).thenThrow(new DataNotFoundException("Not found"));
    try {
      assertEquals(_source.getSingle(BUNDLE_MISS, VERSION_CORRECTION), null);
      Mockito.verify(_underlying, Mockito.times(1)).getSingle(BUNDLE_MISS, VERSION_CORRECTION);
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
  }

  public void test_get_byBundleVC_found() {
    Mockito.when(_underlying.getSingle(BUNDLE, VERSION_CORRECTION)).thenReturn(_convention);
    // hit underlying
    assertSame(_source.getSingle(BUNDLE, VERSION_CORRECTION), _convention);
    Mockito.verify(_underlying, Mockito.times(1)).getSingle(BUNDLE, VERSION_CORRECTION);
    // hit front cache
    assertSame(_source.getSingle(BUNDLE, VERSION_CORRECTION), _convention);
    Mockito.verifyNoMoreInteractions(_underlying);
    // hit back cache
    _source.emptyFrontCache();
    assertSame(_source.getSingle(BUNDLE, VERSION_CORRECTION), _convention);
    Mockito.verifyNoMoreInteractions(_underlying);
    // other things cached
    assertSame(_source.get(UID), _convention);
    assertSame(_source.get(OID, VERSION_CORRECTION), _convention);
    Mockito.verifyNoMoreInteractions(_underlying);
  }

  //-------------------------------------------------------------------------
  public void test_get_byBundleLatest_noCache() {
    Mockito.when(_underlying.getSingle(BUNDLE, VersionCorrection.LATEST)).thenReturn(_convention);
    // hit underlying
    assertSame(_source.getSingle(BUNDLE, VersionCorrection.LATEST), _convention);
    Mockito.verify(_underlying, Mockito.times(1)).getSingle(BUNDLE, VersionCorrection.LATEST);
    // still hit underlying
    assertSame(_source.getSingle(BUNDLE, VersionCorrection.LATEST), _convention);
    Mockito.verify(_underlying, Mockito.times(2)).getSingle(BUNDLE, VersionCorrection.LATEST);
    // other things cached
    assertSame(_source.get(UID), _convention);
    Mockito.verifyNoMoreInteractions(_underlying);
  }

}
