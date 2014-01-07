/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import org.mockito.Mockito;
import org.testng.annotations.Test;

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

}
