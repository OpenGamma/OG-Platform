/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;

/**
 * 
 */
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
