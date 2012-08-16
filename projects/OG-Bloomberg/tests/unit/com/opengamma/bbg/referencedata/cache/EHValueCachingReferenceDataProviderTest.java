/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Test.
 */
@Test(groups = "unit")
public class EHValueCachingReferenceDataProviderTest extends AbstractValueCachingReferenceDataProviderTestCase {

  @Override
  protected ReferenceDataProvider createCachingProvider() {
    EHCacheUtils.clearAll();
    
    return new EHValueCachingReferenceDataProvider(getUnderlyingProvider(), EHCacheUtils.createCacheManager());
  }

}
