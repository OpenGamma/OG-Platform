/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.bbg.referencedata.statistics.NullBloombergReferenceDataStatistics;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link BloombergConnector}
 */
@Test(groups = TestGroup.UNIT)
public class BloombergConnectorTest {

  public void testAvailabilityListeners() {
    final BloombergConnector connector = new BloombergConnector("Test", NullBloombergReferenceDataStatistics.INSTANCE);
    try {
      final BloombergConnector.AvailabilityListener mock1 = Mockito.mock(BloombergConnector.AvailabilityListener.class);
      final BloombergConnector.AvailabilityListener mock2 = Mockito.mock(BloombergConnector.AvailabilityListener.class);
      // Test with none
      connector.notifyAvailabilityListeners();
      // Add mocks
      connector.addAvailabilityListener(mock1);
      connector.addAvailabilityListener(mock2);
      // Test with both
      connector.notifyAvailabilityListeners();
      Mockito.verify(mock1, Mockito.times(1)).bloombergAvailable();
      Mockito.verify(mock2, Mockito.times(1)).bloombergAvailable();
      // Remove first & notify again
      connector.removeAvailabilityListener(mock1);
      connector.notifyAvailabilityListeners();
      Mockito.verify(mock1, Mockito.times(1)).bloombergAvailable();
      Mockito.verify(mock2, Mockito.times(2)).bloombergAvailable();
    } finally {
      connector.close();
    }
  }

  public void getNullApplicationName() {
    SessionOptions sessionOptions = new SessionOptions();
    final BloombergConnector connector = new BloombergConnector("Test", sessionOptions);
    try {
      String applicationName = connector.getApplicationName();
      assertNull(applicationName);
    } finally {
      connector.close();
    }
  }

  public void getApplicationName() {
    BloombergConnectorFactoryBean factoryBean = new BloombergConnectorFactoryBean("Test", "127.0.0.1", 8417, "TestAppName");
    BloombergConnector connector = factoryBean.createObject();
    try {
      assertEquals("TestAppName", connector.getApplicationName());
    } finally {
      connector.close();
    }
  }

}
