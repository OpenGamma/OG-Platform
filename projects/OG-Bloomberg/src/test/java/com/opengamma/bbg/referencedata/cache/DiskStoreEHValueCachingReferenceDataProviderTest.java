/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.io.InputStream;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.MockReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT, singleThreaded = true)
public class DiskStoreEHValueCachingReferenceDataProviderTest extends AbstractValueCachingReferenceDataProviderTestCase {

  private CacheManager _cacheManager;
  private MockReferenceDataProvider _underlyingProvider;
  private UnitTestingReferenceDataProvider _unitProvider;
  private ReferenceDataProvider _provider;

  @BeforeClass
  public void setUpClass() {
    InputStream inputStream = DiskStoreEHValueCachingReferenceDataProviderTest.class.getResourceAsStream("diskstore-ehcache.xml");
    _cacheManager = CacheManager.create(inputStream);
    _cacheManager.clearAll();
  }

  @BeforeMethod
  public void setUp() {
    _underlyingProvider = new MockReferenceDataProvider();
    _unitProvider = new UnitTestingReferenceDataProvider(_underlyingProvider);
    _provider = new DiskStoreEHValueCachingReferenceDataProvider(_underlyingProvider, _cacheManager);
    EHCacheUtils.clear(_cacheManager, EHValueCachingReferenceDataProvider.REFERENCE_DATA_CACHE);
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  //-------------------------------------------------------------------------
  @Override
  protected MockReferenceDataProvider getUnderlyingProvider() {
    return _underlyingProvider;
  }

  @Override
  protected UnitTestingReferenceDataProvider getUnitProvider() {
    return _unitProvider;
  }

  @Override
  protected ReferenceDataProvider getProvider() {
    return _provider;
  }

  //-------------------------------------------------------------------------
  @Test(groups= {TestGroup.UNIT_DB, "mongodb"})
  public void numberOfReturnedFields() {
    super.numberOfReturnedFields();
  }

  @Test(groups= {TestGroup.UNIT_DB, "mongodb"})
  public void singleSecurityEscalatingFields() {
    super.numberOfReturnedFields();
  }

  @Test(groups= {TestGroup.UNIT_DB, "mongodb"})
  public void fieldNotAvailable() {
    super.numberOfReturnedFields();
  }

  @Test(groups= {TestGroup.UNIT_DB, "mongodb"})
  public void securityNotAvailable() {
    super.numberOfReturnedFields();
  }

  @Test(groups= {TestGroup.UNIT_DB, "mongodb"})
  public void multipleSecuritiesSameEscalatingFields() {
    super.numberOfReturnedFields();
  }

}
