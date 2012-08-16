/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import org.testng.annotations.Test;

import com.opengamma.bbg.security.BloombergSecurityProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;

/**
 * Test.
 */
@Test(groups = "integration")
public class BloombergSecuritySourceDirectTest extends BloombergSecuritySourceTestCase {

  private BloombergReferenceDataProvider _dataProvider;

  @Override
  protected BloombergSecuritySource createSecuritySource() {
    BloombergConnector connector = BloombergTestUtils.getBloombergConnector();
    BloombergReferenceDataProvider refDataProvider = new BloombergReferenceDataProvider(connector);
    refDataProvider.start();
    _dataProvider = refDataProvider;
    DefaultExchangeDataProvider exchangeProvider = new DefaultExchangeDataProvider();
    BloombergSecurityProvider secProvider = new BloombergSecurityProvider(refDataProvider, exchangeProvider);
    return new BloombergSecuritySource(secProvider);
  }

  @Override
  protected void stopSecuritySource() {
    if (_dataProvider != null) {
      BloombergReferenceDataProvider dataProvider = _dataProvider;
      _dataProvider = null;
      dataProvider.stop();
    }
  }

}
