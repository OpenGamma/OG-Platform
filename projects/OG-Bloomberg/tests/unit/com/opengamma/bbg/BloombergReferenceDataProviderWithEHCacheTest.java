/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import org.testng.annotations.Test;

import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
@Test
public class BloombergReferenceDataProviderWithEHCacheTest  extends BloombergReferenceDataProviderTestCase {

  @Override
  protected ReferenceDataProvider createReferenceDataProvider(Class<?> c) {
    EHCacheUtils.clearAll();
    CachingReferenceDataProvider underlying = BloombergLiveDataServerUtils.getCachingReferenceDataProvider(c);
    return new EHCachingReferenceDataProvider(
        underlying, 
        EHCacheUtils.createCacheManager(), 
        OpenGammaFudgeContext.getInstance());
  }

  @Override
  protected void stopProvider() {
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider((CachingReferenceDataProvider) getReferenceDataProvider());
  }

}
