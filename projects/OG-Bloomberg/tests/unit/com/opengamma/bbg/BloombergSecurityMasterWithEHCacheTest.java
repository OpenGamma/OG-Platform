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
public class BloombergSecurityMasterWithEHCacheTest extends BloombergSecurityMasterTestCase {
  
  private CachingReferenceDataProvider _refDataProvider = null;
  
  @Override
  protected BloombergSecurityMaster createSecurityMaster() {
    _refDataProvider = BloombergLiveDataServerUtils.getCachingReferenceDataProvider(BloombergSecurityMasterWithEHCacheTest.class);
    
    DefaultExchangeDataProvider exchangeProvider = new DefaultExchangeDataProvider();
    
    return new BloombergSecurityMaster(_refDataProvider, exchangeProvider);
  }

  @Override
  protected void stopSecurityMaster() {
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider(_refDataProvider);
  }

}
