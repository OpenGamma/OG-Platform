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

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups= {TestGroup.UNIT, "ehcache"})
public class EHValueCachingReferenceDataProviderTest extends AbstractValueCachingReferenceDataProviderTestCase {

  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  @BeforeMethod
  public void setUp() {
    ReferenceDataProvider underlyingProvider = initProviders();
    EHCacheUtils.clear(_cacheManager, EHValueCachingReferenceDataProvider.REFERENCE_DATA_CACHE);
    ReferenceDataProvider provider = new EHValueCachingReferenceDataProvider(
        underlyingProvider, _cacheManager, OpenGammaFudgeContext.getInstance());
    setProvider(provider);
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

}
