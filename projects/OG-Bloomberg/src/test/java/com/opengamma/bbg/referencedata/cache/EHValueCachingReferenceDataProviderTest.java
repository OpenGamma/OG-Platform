/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ReferenceDataProvider createCachingProvider() {
    EHCacheUtils.clear(_cacheManager, EHValueCachingReferenceDataProvider.REFERENCE_DATA_CACHE);
    return new EHValueCachingReferenceDataProvider(getUnderlyingProvider(), _cacheManager, OpenGammaFudgeContext.getInstance());
  }

}
