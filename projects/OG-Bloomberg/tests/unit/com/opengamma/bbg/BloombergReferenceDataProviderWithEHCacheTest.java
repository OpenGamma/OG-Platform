/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.Test;

import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Test.
 */
@Test(groups = "integration")
public class BloombergReferenceDataProviderWithEHCacheTest  extends BloombergReferenceDataProviderTestCase {

  @Override
  protected ReferenceDataProvider createReferenceDataProvider(Class<?> c) {
    // REVIEW jonathan 2012-09-04 -- a reference data provider wrapped in Mongo, wrapped again in EHCache... seriously?
    CachingReferenceDataProvider underlying = BloombergLiveDataServerUtils.getCachingReferenceDataProvider(c);
    CacheManager cm = EHCacheUtils.createCacheManager();
    EHCacheUtils.clear(cm, EHCachingReferenceDataProvider.REFERENCE_DATA_CACHE);
    return new EHCachingReferenceDataProvider(
        underlying, 
        cm, 
        OpenGammaFudgeContext.getInstance());
  }

  @Override
  protected void stopProvider() {
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider((CachingReferenceDataProvider) getReferenceDataProvider());
  }

}
