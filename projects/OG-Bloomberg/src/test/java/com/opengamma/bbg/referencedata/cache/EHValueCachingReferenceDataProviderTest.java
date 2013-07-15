/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.MockReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups= {TestGroup.UNIT, "ehcache"}, singleThreaded = true)
public class EHValueCachingReferenceDataProviderTest extends AbstractValueCachingReferenceDataProviderTestCase {

  private CacheManager _cacheManager;
  private MockReferenceDataProvider _underlyingProvider;
  private UnitTestingReferenceDataProvider _unitProvider;
  private ReferenceDataProvider _provider;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  @BeforeMethod
  public void setUp() {
    _underlyingProvider = new MockReferenceDataProvider();
    _unitProvider = new UnitTestingReferenceDataProvider(_underlyingProvider);
    EHCacheUtils.clear(_cacheManager, EHValueCachingReferenceDataProvider.REFERENCE_DATA_CACHE);
    _provider = new EHValueCachingReferenceDataProvider(
        _underlyingProvider, _cacheManager, OpenGammaFudgeContext.getInstance());
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
