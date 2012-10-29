/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Test.
 */
@Test(groups = "unit")
public class EHValueCachingReferenceDataProviderTest extends AbstractValueCachingReferenceDataProviderTestCase {

  @Override
  protected ReferenceDataProvider createCachingProvider() {
    CacheManager cm = EHCacheUtils.createCacheManager();
    EHCacheUtils.clear(cm, EHValueCachingReferenceDataProvider.REFERENCE_DATA_CACHE);
    return new EHValueCachingReferenceDataProvider(getUnderlyingProvider(), cm, OpenGammaFudgeContext.getInstance());
  }

}
