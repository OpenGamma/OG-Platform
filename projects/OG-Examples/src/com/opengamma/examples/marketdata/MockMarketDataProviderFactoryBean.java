/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.marketdata;

import java.util.Set;

import javax.time.Duration;
import javax.time.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Mock market data provider to allow the example server to run.
 * <p>
 * For fully-supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters, please contact
 * sales@opengamma.com
 */
public class MockMarketDataProviderFactoryBean extends SingletonFactoryBean<MarketDataProvider> {
  
  private static final String MESSAGE = "\nThis is a placeholder market data component." +
      "\nFor fully-supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters," +
      "\nplease contact sales@opengamma.com";

  @Override
  protected MarketDataProvider createObject() {
    
    return new MarketDataProvider() {
      
      @Override
      public void addListener(MarketDataListener listener) {
        printWarning();
      }
      
      @Override
      public void removeListener(MarketDataListener listener) {
        printWarning();
      }

      //---------------------------------------------------------------------
      @Override
      public void subscribe(UserPrincipal user, ValueRequirement valueRequirement) {
      }

      @Override
      public void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
      }

      @Override
      public void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement) {
      }

      @Override
      public void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
      }

      //---------------------------------------------------------------------
      @Override
      public MarketDataAvailabilityProvider getAvailabilityProvider() {
        printWarning();
        return new MockMarketDataAvailabilityProvider();
      }

      @Override
      public MarketDataPermissionProvider getPermissionProvider() {
        return new PermissiveMarketDataPermissionProvider();
      }

      //---------------------------------------------------------------------
      @Override
      public boolean isCompatible(MarketDataSpecification marketDataSpec) {
        return true;
      }

      @Override
      public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
        return new MockMarketDataSnapshot();
      }

      @Override
      public Duration getRealTimeDuration(Instant fromInstant, Instant toInstant) {
        return Duration.between(fromInstant, toInstant);
      }
    };
  }
  
  /*package*/ static void printWarning() {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

}
