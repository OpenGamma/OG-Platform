/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import org.testng.annotations.Test;

import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;

/**
 * Test BloombergSecurityMaster.
 */
@Test
public class BloombergSecurityMasterDirectTest extends BloombergSecurityMasterTestCase {
  private BloombergReferenceDataProvider _dataProvider;

  @Override
  protected BloombergSecurityMaster createSecurityMaster() {
    SessionOptions options = BloombergTestUtils.getSessionOptions();
    BloombergReferenceDataProvider dataProvider = new BloombergReferenceDataProvider(options);
    dataProvider.start();
    _dataProvider = dataProvider;
    
    DefaultExchangeDataProvider exchangeProvider = new DefaultExchangeDataProvider();
    
    return new BloombergSecurityMaster(_dataProvider, exchangeProvider);
  }
  
  @Override
  protected void stopSecurityMaster() {
    if(_dataProvider != null) {
      BloombergReferenceDataProvider dataProvider = _dataProvider;
      _dataProvider = null;
      dataProvider.stop();
    }
  }
  
}
