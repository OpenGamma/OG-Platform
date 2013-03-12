/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;

/**
 * Represents a source of market data from which the engine can obtain a consistent snapshot and receive notifications of changes.
 */
@PublicSPI
public interface MarketDataProvider {

  /**
   * Adds a listener which will receive notifications of certain events. The events could be related to any subscriptions made through this snapshot provider.
   * 
   * @param listener the listener to add.
   */
  void addListener(MarketDataListener listener);

  /**
   * Removes a listener.
   * 
   * @param listener the listener to remove
   */
  void removeListener(MarketDataListener listener);

  //-------------------------------------------------------------------------
  /**
   * Attempts to subscribe a user to a piece of market data. All listeners will be notified of the result asynchronously. The existence of a subscription might notify the provider that the value
   * should be included in snapshots.
   * 
   * @param valueSpecification the market data specification as indicated by a {@link MarketDataAvailabilityProvider}, not null
   */
  void subscribe(ValueSpecification valueSpecification);

  /**
   * Attempts to subscribe a user to a set of market data. All listeners will be notified of the result asynchronously. The existence of a subscription might notify the provider that the value should
   * be included in snapshots.
   * 
   * @param valueSpecifications the market data specifications as indicated by a {@link MarketDataAvailabilityProvider}, not null
   */
  void subscribe(Set<ValueSpecification> valueSpecifications);

  /**
   * Unsubscribes a user from a piece of market data.
   * 
   * @param valueSpecification the market data specification as passed to an earlier call to {@link #subscribe(ValueSpecification)} or {@link #subscribe(Set)}, not null
   */
  void unsubscribe(ValueSpecification valueSpecification);

  /**
   * Unsubscribes a user from a set of market data.
   * 
   * @param valueSpecifications the market data specifications as passed to earlier calls to {@link #subscribe(ValueSpecification)} or {@link #subscribe(Set)}, not null
   */
  void unsubscribe(Set<ValueSpecification> valueSpecifications);

  //-------------------------------------------------------------------------
  /**
   * Gets the availability provider for this market data provider. It is expected that obtaining an accurate availability provider could be a heavy operation. This method is called every time a view
   * definition is compiled, in order to build the dependency graphs, and the result on each occasion is cached and reused throughout that compilation.
   * 
   * @param marketDataSpec described the market data to check availability for, not null
   * @return the availability provider, not null
   */
  MarketDataAvailabilityProvider getAvailabilityProvider(MarketDataSpecification marketDataSpec);

  /**
   * Gets the permission provider, not null
   * 
   * @return the permission provider, not null
   */
  MarketDataPermissionProvider getPermissionProvider();

  //-------------------------------------------------------------------------
  /**
   * Gets whether a market data specification is compatible with this market data provider. It does not necessarily indicate that the specification can be satisfied, only whether the market data
   * provider knows how to make the best attempt to satisfy it.
   * 
   * @param marketDataSpec describes the market data, not null
   * @return true if the specification is compatible with this provider
   */
  boolean isCompatible(MarketDataSpecification marketDataSpec);

  /**
   * Obtains access to a snapshot of market data.
   * 
   * @param marketDataSpec describes the market data to obtain, not null
   * @return the snapshot, not null
   */
  MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec);

  /**
   * Gets the real-time duration between two instants on the market data time-line. If the market data provider is replaying data at a different rate from normal then this will not correspond to the
   * actual duration between the two instants.
   * 
   * @param fromInstant the instant from which the duration begins, not null
   * @param toInstant the instant at which the duration ends, not null
   * @return the real-time duration, null if the market data provider is not able to tell
   */
  Duration getRealTimeDuration(Instant fromInstant, Instant toInstant);

}
