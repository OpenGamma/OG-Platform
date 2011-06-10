/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.livedata;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.engine.livedata.LiveDataSnapshotListener;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Mock live data snapshot provider to get the example server running
 * 
 * For fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters, please contact sales@opengamma.com
 */
public class MockLiveDataSnapshotProviderFactoryBean extends SingletonFactoryBean<LiveDataSnapshotProvider> {
  
  private static final String MESSAGE = "\nThis is a placeholder live data snapshot provider." +
      "\nFor fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters," +
      "\nPlease contact sales@opengamma.com.";

  @Override
  protected LiveDataSnapshotProvider createObject() {
    
    return new LiveDataSnapshotProvider() {
      
      @Override
      public long snapshot(long snapshot) {
        printWarning();
        return 0;
      }

      @Override
      public long snapshot() {
        printWarning();
        return 0;
      }
      
      @Override
      public void removeListener(LiveDataSnapshotListener listener) {
        printWarning();
      }
      
      @Override
      public void releaseSnapshot(long snapshot) {
        printWarning();
      }
      
      @Override
      public Object querySnapshot(long snapshot, ValueRequirement requirement) {
        printWarning();
        return null;
      }
      
      @Override
      public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
        printWarning();
      }
      
      @Override
      public void addSubscription(UserPrincipal user, ValueRequirement valueRequirement) {
        printWarning();
      }
      
      @Override
      public void addListener(LiveDataSnapshotListener listener) {
        printWarning();
      }

      @Override
      public boolean hasStructuredData() {
        return false;
      }

      @Override
      public SnapshotDataBundle querySnapshot(long snapshot, StructuredMarketDataKey marketDataKey) {
        return null;
      }
    };
  }
  
  private void printWarning() {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

}
