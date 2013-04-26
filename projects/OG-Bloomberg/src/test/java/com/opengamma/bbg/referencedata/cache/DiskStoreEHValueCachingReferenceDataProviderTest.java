/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.io.InputStream;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DiskStoreEHValueCachingReferenceDataProviderTest extends AbstractValueCachingReferenceDataProviderTestCase {

  @BeforeMethod
  public void setUp() {
    ReferenceDataProvider underlyingProvider = initProviders();
    InputStream inputStream = DiskStoreEHValueCachingReferenceDataProviderTest.class.getResourceAsStream("diskstore-ehcache.xml");
    CacheManager.create(inputStream).clearAll();
    
    CacheManager cacheManager = CacheManager.create(inputStream);
    ReferenceDataProvider provider = new DiskStoreEHValueCachingReferenceDataProvider(underlyingProvider, cacheManager);
    setProvider(provider);
  }

}
