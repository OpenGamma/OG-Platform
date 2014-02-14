/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertSame;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link EHCachingLegalEntitySource}.
 */
@Test(groups = {TestGroup.UNIT, "ehcache" })
public class EHCachingLegalEntitySourceTest {

  private static final UniqueId UID = UniqueId.of("A", "B", "123");
  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final VersionCorrection VC = VersionCorrection.of(Instant.now(), Instant.now());

  private LegalEntitySource _underlyingSource;
  private EHCachingLegalEntitySource _cachingSource;
  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCachingLegalEntitySourceTest.class);
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  @BeforeMethod
  public void setUp() {
    _underlyingSource = mock(LegalEntitySource.class);
    _cachingSource = new EHCachingLegalEntitySource(_underlyingSource, _cacheManager);
  }

  @AfterMethod
  public void tearDown() {
    _cachingSource.shutdown();
  }

  //-------------------------------------------------------------------------
  public void getLegalEntity_uniqueId() {
    final SimpleLegalEntity h = new SimpleLegalEntity();
    h.setUniqueId(UID);
    when(_underlyingSource.get(UID)).thenReturn(h);
    assertSame(_cachingSource.get(UID), h);
    assertSame(_cachingSource.get(UID), h);
    verify(_underlyingSource, times(1)).get(UID);
  }

  public void getLegalEntity_objectId() {
    final SimpleLegalEntity h = new SimpleLegalEntity();
    h.setUniqueId(UID);
    when(_underlyingSource.get(OID, VC)).thenReturn(h);
    assertSame(_cachingSource.get(OID, VC), h);
    assertSame(_cachingSource.get(OID, VC), h);
    verify(_underlyingSource, times(1)).get(OID, VC);
  }

}
