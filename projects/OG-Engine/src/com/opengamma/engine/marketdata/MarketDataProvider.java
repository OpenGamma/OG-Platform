/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;

import javax.time.Duration;
import javax.time.Instant;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicSPI;

/**
 * Represents a source of market data from which the engine can obtain a consistent snapshot and receive notifications
 * of changes.
 */
@PublicSPI
public interface MarketDataProvider {
  
  /**
   * Adds a listener which will receive notifications of certain events. The events could be related to any
   * subscriptions made through this snapshot provider.
   * 
   * @param listener  the listener to add.
   */
  void addListener(MarketDataListener listener);
  
  /**
   * Removes a listener.
   * 
   * @param listener  the listener to remove
   */
  void removeListener(MarketDataListener listener);
  
  //-------------------------------------------------------------------------
  /**
   * Attempts to subscribe a user to a piece of market data. All listeners will be notified of the result
   * asynchronously. The existence of a subscription might notify the provider that the value should be included in
   * snapshots.
   * 
   * @param user  the user making the subscription, not null
   * @param valueRequirement  the market data requirement, not null
   */
  void subscribe(UserPrincipal user, ValueRequirement valueRequirement);
  
  /**
   * Attempts to subscribe a user to a set of market data. All listeners will be notified of the result
   * asynchronously. The existence of a subscription might notify the provider that the value should be included in
   * snapshots.
   *
   * @param user  the user making the subscription, not null
   * @param valueRequirements  the market data requirements, not null
   */
  void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements);
  
  /**
   * Unsubscribes a user from a piece of market data.
   * 
   * @param user  the user who made the subscription, not null
   * @param valueRequirement  the market data requirement, not null
   */
  void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement);
  
  /**
   * Unsubscribes a user from a set of market data.
   * 
   * @param user  the user who made the subscription, not null
   * @param valueRequirements  the market data requirements, not null
   */
  void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements);
  
  //-------------------------------------------------------------------------
  /**
   * Gets the availability provider for this market data provider. It is expected that obtaining an accurate
   * availability provider could be a heavy operation. This method is called every time a view definition is compiled,
   * in order to build the dependency graphs, and the result on each occasion is cached and reused throughout that
   * compilation. 
   * 
   * @return the availability provider, not null
   */
  MarketDataAvailabilityProvider getAvailabilityProvider();
  
  /**
   * Gets the permission provider, not null
   * 
   * @return  the permission provider, not null
   */
  MarketDataPermissionProvider getPermissionProvider();

  //-------------------------------------------------------------------------
  /**
   * Gets whether a market data specification is compatible with this market data provider. It does not necessarily
   * indicate that the specification can be satisfied, only whether the market data provider knows how to make the best
   * attempt to satisfy it.
   * 
   * @param marketDataSpec  describes the market data, not null
   * @return true if the specification is compatible with this provider
   */
  boolean isCompatible(MarketDataSpecification marketDataSpec);
  
  /**
   * Obtains access to a snapshot of market data.
   * 
   * @param marketDataSpec  describes the market data to obtain, not null
   * @return  the snapshot, not null
   */
  MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec);
  
  /**
   * Gets the real-time duration between two instants on the market data time-line. If the market data provider is
   * replaying data at a different rate from normal then this will not correspond to the actual duration between the
   * two instants.
   * 
   * @param fromInstant  the instant from which the duration begins, not null
   * @param toInstant  the instant at which the duration ends, not null
   * @return the real-time duration, null if the market data provider is not able to tell
   */
  Duration getRealTimeDuration(Instant fromInstant, Instant toInstant);
  
}
