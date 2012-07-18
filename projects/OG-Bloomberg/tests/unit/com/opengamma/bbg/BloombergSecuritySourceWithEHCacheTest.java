/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import org.testng.annotations.Test;

import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;

/**
 * Test.
 */
@Test(groups = "integration")
public class BloombergSecuritySourceWithEHCacheTest extends BloombergSecuritySourceTestCase {

  private CachingReferenceDataProvider _refDataProvider = null;

  @Override
  protected BloombergSecuritySource createSecuritySource() {
    _refDataProvider = BloombergLiveDataServerUtils.getCachingReferenceDataProvider(BloombergSecuritySourceWithEHCacheTest.class);
    
    DefaultExchangeDataProvider exchangeProvider = new DefaultExchangeDataProvider();
    
    return new BloombergSecuritySource(_refDataProvider, exchangeProvider);
  }

  @Override
  protected void stopSecuritySource() {
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider(_refDataProvider);
  }

}
