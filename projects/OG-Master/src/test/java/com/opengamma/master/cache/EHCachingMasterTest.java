/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test EHCaching master behaviour that is common across all masters, using dummy TestDocument and TestMaster classes
 */
@Test(groups = TestGroup.UNIT)
public class EHCachingMasterTest extends AbstractEHCachingMasterTest<CacheTestMaster, CacheTestDocument> {

  {
    // Initialise security documents

    // Document A
    docA100_V1999to2010_Cto2011 = new CacheTestDocument(A100_UID);
    docA200_V2010to = new CacheTestDocument(A200_UID);
    docA300_V1999to2010_C2011to = new CacheTestDocument(A300_UID);

    // Document B
    docB200_V2000to2009 = new CacheTestDocument(B200_UID);
    docB400_V2009to2011 = new CacheTestDocument(B400_UID);
    docB500_V2011to = new CacheTestDocument(B500_UID);

    // Document C
    docC100_Vto2011 = new CacheTestDocument(C100_UID);
    docC300_V2011to = new CacheTestDocument(C300_UID);

    // Document to add
    DOC_TO_ADD = new CacheTestDocument(null);
    DOC_ADDED = new CacheTestDocument(ADDED_UID);
  }

  class EHCachingTestMaster extends AbstractEHCachingMaster<CacheTestDocument> {
    public EHCachingTestMaster(String name, AbstractChangeProvidingMaster<CacheTestDocument> underlying, CacheManager cacheManager) {
      super(name, underlying, cacheManager);
    }
  }

  //-------------------------------------------------------------------------
  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCachingMasterTest.class);
  }

  @BeforeMethod
  public void setUp() {
    EHCacheUtils.clear(_cacheManager);
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  @Test
  public void testGetUidVersioned() {
    CacheTestMaster mockUnderlyingMaster = (CacheTestMaster) populateMockMaster(mock(CacheTestMaster.class));
    EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);

    // Assert returned documents
    assertEquals(docB200_V2000to2009, cachingMaster.get(docB200_V2000to2009.getUniqueId()));
    assertEquals(docA100_V1999to2010_Cto2011, cachingMaster.get(docA100_V1999to2010_Cto2011.getUniqueId()));
    assertEquals(docA100_V1999to2010_Cto2011, cachingMaster.get(docA100_V1999to2010_Cto2011.getUniqueId()));
    assertEquals(docA100_V1999to2010_Cto2011, cachingMaster.get(docA100_V1999to2010_Cto2011.getUniqueId()));
    assertEquals(docB200_V2000to2009, cachingMaster.get(docB200_V2000to2009.getUniqueId()));
    assertEquals(docB200_V2000to2009, cachingMaster.get(docB200_V2000to2009.getUniqueId()));
    assertEquals(docA100_V1999to2010_Cto2011, cachingMaster.get(docA100_V1999to2010_Cto2011.getUniqueId()));
    assertEquals(docB200_V2000to2009, cachingMaster.get(docB200_V2000to2009.getUniqueId()));

    // Assert invocation counts
    verify(mockUnderlyingMaster, times(1)).get(docA100_V1999to2010_Cto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docA200_V2010to.getUniqueId());
    verify(mockUnderlyingMaster, times(1)).get(docB200_V2000to2009.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB400_V2009to2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB500_V2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docC100_Vto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docC300_V2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docA200_V2010to.getObjectId(), VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(0)).get(docB500_V2011to.getObjectId(), VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(0)).get(docC300_V2011to.getObjectId(), VersionCorrection.LATEST);

    cachingMaster.shutdown();
  }

  @Test
  public void testGetUidUnversioned() {
    CacheTestMaster mockUnderlyingMaster = (CacheTestMaster) populateMockMaster(mock(CacheTestMaster.class));
    EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);

    // Assert returned documents
    assertEquals(docB500_V2011to, cachingMaster.get(docB200_V2000to2009.getUniqueId().toLatest()));
    assertEquals(docA200_V2010to, cachingMaster.get(docA100_V1999to2010_Cto2011.getUniqueId().toLatest()));
    assertEquals(docA200_V2010to, cachingMaster.get(docA100_V1999to2010_Cto2011.getUniqueId().toLatest()));
    assertEquals(docA200_V2010to, cachingMaster.get(docA100_V1999to2010_Cto2011.getUniqueId().toLatest()));
    assertEquals(docB500_V2011to, cachingMaster.get(docB200_V2000to2009.getUniqueId().toLatest()));
    assertEquals(docB500_V2011to, cachingMaster.get(docB200_V2000to2009.getUniqueId().toLatest()));
    assertEquals(docA200_V2010to, cachingMaster.get(docA100_V1999to2010_Cto2011.getUniqueId().toLatest()));
    assertEquals(docB500_V2011to, cachingMaster.get(docB200_V2000to2009.getUniqueId().toLatest()));

    // Assert invocation counts
    verify(mockUnderlyingMaster, times(1)).get(A300_UID.toLatest());
    verify(mockUnderlyingMaster, times(1)).get(B500_UID.toLatest());
    verify(mockUnderlyingMaster, times(0)).get(C300_UID.toLatest());
    verify(mockUnderlyingMaster, times(0)).get(docA100_V1999to2010_Cto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docA200_V2010to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB200_V2000to2009.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB400_V2009to2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB500_V2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docC100_Vto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docC300_V2011to.getUniqueId());

    cachingMaster.shutdown();
  }

  @Test
  public void testGetOidLatestVersionCorrection() {
    CacheTestMaster mockUnderlyingMaster = (CacheTestMaster) populateMockMaster(mock(CacheTestMaster.class));
    EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);

    // Assert returned documents
    assertEquals(docB500_V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));
    assertEquals(docA200_V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(docA200_V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(docA200_V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(docB500_V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));
    assertEquals(docB500_V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));
    assertEquals(docA200_V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(docB500_V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));

    // Assert invocation counts
    verify(mockUnderlyingMaster, times(1)).get(A_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(1)).get(B_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(0)).get(C_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(0)).get(docA100_V1999to2010_Cto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docA200_V2010to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB200_V2000to2009.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB400_V2009to2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB500_V2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docC100_Vto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docC300_V2011to.getUniqueId());

    cachingMaster.shutdown();
  }

  @Test
  public void testGetOidMixedVersionCorrection() {
    CacheTestMaster mockUnderlyingMaster = (CacheTestMaster) populateMockMaster(mock(CacheTestMaster.class));
    EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);

    //TODO enhance testing of v/c range border cases

    // Assert returned documents
    assertEquals(docB500_V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));
    assertEquals(docA100_V1999to2010_Cto2011, cachingMaster.get(A_OID,
        VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2009, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(),
                             ZonedDateTime.of(LocalDateTime.of(2010, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant())));
    assertEquals(docA200_V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(docA300_V1999to2010_C2011to, cachingMaster.get(A_OID,
        VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2009, 6, 6, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(), now)));
    assertEquals(docB500_V2011to, cachingMaster.get(B_OID, VersionCorrection.of(now, now)));
    assertEquals(docB500_V2011to, cachingMaster.get(B_OID, VersionCorrection.LATEST));
    assertEquals(docA200_V2010to, cachingMaster.get(A_OID, VersionCorrection.LATEST));
    assertEquals(docB500_V2011to, cachingMaster.get(B_OID,
        VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2011, 6, 6, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(), now)));

    // Assert invocation counts
    verify(mockUnderlyingMaster, times(1)).get(B_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(1)).get(A_OID,
        VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2009, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(),
                             ZonedDateTime.of(LocalDateTime.of(2010, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant()));
    verify(mockUnderlyingMaster, times(1)).get(A_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(1)).get(A_OID,
        VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2009, 6, 6, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(), now));
    verify(mockUnderlyingMaster, times(0)).get(B_OID, VersionCorrection.of(now, now));
    verify(mockUnderlyingMaster, times(0)).get(B_OID,
        VersionCorrection.of(ZonedDateTime.of(LocalDateTime.of(2011, 6, 6, 12, 0, 0, 0), ZoneOffset.UTC).toInstant(), now));
    verify(mockUnderlyingMaster, times(0)).get(C_OID, VersionCorrection.LATEST);
    verify(mockUnderlyingMaster, times(0)).get(docA100_V1999to2010_Cto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docA200_V2010to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docA300_V1999to2010_C2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB200_V2000to2009.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB400_V2009to2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docB500_V2011to.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docC100_Vto2011.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(docC300_V2011to.getUniqueId());

    cachingMaster.shutdown();
  }

  //@Test
  //public void testCachedMiss() {
  //  TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  //  EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  //  //TODO
  //
  //  cachingMaster.shutdown();
  //}
  //
  ////-------------------------------------------------------------------------
  //
  //@Test
  //public void testUpdate() {
  //  TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  //  EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  //  //TODO
  //
  //  cachingMaster.shutdown();
  //}

  @Test
  public void testAdd() {
    CacheTestMaster mockUnderlyingMaster = (CacheTestMaster) populateMockMaster(mock(CacheTestMaster.class));
    EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);

    // Assert returned documents
    assertEquals(DOC_ADDED, cachingMaster.add(DOC_TO_ADD));

    // Assert cache contents
    assertEquals(DOC_ADDED, cachingMaster.get(DOC_ADDED.getUniqueId()));
    assertEquals(DOC_ADDED, cachingMaster.get(DOC_ADDED.getObjectId(), VersionCorrection.LATEST));
    assertEquals(DOC_ADDED, cachingMaster.get(DOC_ADDED.getObjectId(), VersionCorrection.of(now, now)));

    // Assert invocation counts
    verify(mockUnderlyingMaster, times(1)).add(DOC_TO_ADD);
    verify(mockUnderlyingMaster, times(0)).add(DOC_ADDED);
    verify(mockUnderlyingMaster, times(0)).get(DOC_ADDED.getUniqueId());
    verify(mockUnderlyingMaster, times(0)).get(DOC_TO_ADD.getUniqueId());

    cachingMaster.shutdown();
  }

  //@Test
  //public void testRemove() {
  //  TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  //  EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  //  //TODO
  //
  //  cachingMaster.shutdown();
  //}
  //
  //@Test
  //public void testCorrect() { // same as replaceVersion()
  //  TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  //  EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  //  //TODO
  //
  //  cachingMaster.shutdown();
  //}
  //
  //@Test
  //public void testReplaceVersion() {
  //  TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  //  EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  //  //TODO
  //
  //  cachingMaster.shutdown();
  //}
  //
  //@Test
  //public void testReplaceAllVersions() {
  //  TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  //  EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  //  //TODO
  //
  //  cachingMaster.shutdown();
  //}
  //
  //@Test
  //public void testReplaceVersions() {
  //  TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  //  EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  //  //TODO
  //
  //  cachingMaster.shutdown();
  //}
  //
  //@Test
  //public void testRemoveVersion() {
  //  TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  //  EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  //  //TODO
  //
  //  cachingMaster.shutdown();
  //}
  //
  //@Test
  //public void testAddVersion() {
  //  TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  //  EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  //  //TODO
  //
  //  cachingMaster.shutdown();
  //}
  //
  //@Test
  //public void testChangeProvider() {
  //  TestMaster mockUnderlyingMaster = (TestMaster) populateMockMaster(mock(TestMaster.class));
  //  EHCachingTestMaster cachingMaster = new EHCachingTestMaster("test", mockUnderlyingMaster, _cacheManager);
  //
  //  //TODO
  //
  //  cachingMaster.shutdown();
  //}

}
