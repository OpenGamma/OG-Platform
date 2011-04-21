/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.livedata;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Mock live data availability provider to get the example server running
 * 
 * For fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters, please contact sales@opengamma.com
 */
public class MockLiveDataAvailabilityProviderFactoryBean extends SingletonFactoryBean<LiveDataAvailabilityProvider> {
  
  private static final String MESSAGE = "\nThis is a placeholder live data snapshot provider." +
      "\nFor fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters," +
      "\nPlease contact sales@opengamma.com.\n";
  
  @Override
  protected LiveDataAvailabilityProvider createObject() {
    return new LiveDataAvailabilityProvider() {
      
      @Override
      public boolean isAvailable(ValueRequirement requirement) {
        printWarning();
        return false;
      }
    };
  }
  
  private void printWarning() {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

}
