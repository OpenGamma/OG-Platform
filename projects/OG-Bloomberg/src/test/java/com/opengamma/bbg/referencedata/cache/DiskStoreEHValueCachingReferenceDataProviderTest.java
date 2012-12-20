/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.io.InputStream;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;

/**
 * Test.
 */
@Test(groups = "unit")
public class DiskStoreEHValueCachingReferenceDataProviderTest extends AbstractValueCachingReferenceDataProviderTestCase {

  @Override
  protected ReferenceDataProvider createCachingProvider() {
    InputStream inputStream = DiskStoreEHValueCachingReferenceDataProviderTest.class.getResourceAsStream("diskstore-ehcache.xml");
    CacheManager.create(inputStream).clearAll();
    
    CacheManager cacheManager = CacheManager.create(inputStream);
    return new DiskStoreEHValueCachingReferenceDataProvider(getUnderlyingProvider(), cacheManager);
  }

}
