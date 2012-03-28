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
 * Test BloombergSecuritySource.
 */
@Test
public class BloombergSecuritySourceDirectTest extends BloombergSecuritySourceTestCase {
  private BloombergReferenceDataProvider _dataProvider;

  @Override
  protected BloombergSecuritySource createSecuritySource() {
    SessionOptions options = BloombergTestUtils.getSessionOptions();
    BloombergReferenceDataProvider dataProvider = new BloombergReferenceDataProvider(options);
    dataProvider.start();
    _dataProvider = dataProvider;
    
    DefaultExchangeDataProvider exchangeProvider = new DefaultExchangeDataProvider();
    
    return new BloombergSecuritySource(_dataProvider, exchangeProvider);
  }
  
  @Override
  protected void stopSecuritySource() {
    if(_dataProvider != null) {
      BloombergReferenceDataProvider dataProvider = _dataProvider;
      _dataProvider = null;
      dataProvider.stop();
    }
  }
  
}
