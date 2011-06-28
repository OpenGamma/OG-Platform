/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSnapshotSpecification;
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
   * @param user  the user making the subscription, not {@code null}
   * @param valueRequirement  the market data requirement, not {@code null}
   */
  void subscribe(UserPrincipal user, ValueRequirement valueRequirement);
  
  /**
   * Attempts to subscribe a user to a set of market data. All listeners will be notified of the result
   * asynchronously. The existence of a subscription might notify the provider that the value should be included in
   * snapshots.
   *
   * @param user  the user making the subscription, not {@code null}
   * @param valueRequirements  the market data requirements, not {@code null}
   */
  void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements);
  
  /**
   * Unsubscribes a user from a piece of market data.
   * 
   * @param user  the user who made the subscription, not {@code null}
   * @param valueRequirement  the market data requirement, not {@code null}
   */
  void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement);
  
  /**
   * Unsubscribes a user from a set of market data.
   * 
   * @param user  the user who made the subscription, not {@code null}
   * @param valueRequirements  the market data requirements, not {@code null}
   */
  void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements);
  
  //-------------------------------------------------------------------------
  /**
   * Gets the availability provider for this market data provider.
   * 
   * @return the availability provider, not {@code null}
   */
  MarketDataAvailabilityProvider getAvailabilityProvider();
  
  /**
   * Gets the permission provider, not {@code null}
   * 
   * @return  the permission provider, not {@code null}
   */
  MarketDataPermissionProvider getPermissionProvider();

  //-------------------------------------------------------------------------
  /**
   * Gets whether a snapshot specification is compatible with this market data provider. It does not necessarily
   * indicate that the snapshot specification can be satisfied, only whether the market data provider knows how to make
   * the best attempt to satisfy it.
   * 
   * @param snapshotSpec  describes the snapshot, not {@code null}
   * @return {@code true} if the snapshot specification is compatible with this provider, {@code false} otherwise
   */
  boolean isCompatible(MarketDataSnapshotSpecification snapshotSpec);
  
  /**
   * Obtains access to a snapshot of market data. Depending on the implementation, this may involve creating a
   * snapshot, or simply making an existing snapshot available, for example by loading it into memory.
   * 
   * @param snapshotSpec  describes the snapshot to obtain, not {@code null}
   * @return  a description of the snapshot obtained, not {@code null}
   */
  MarketDataSnapshot snapshot(MarketDataSnapshotSpecification snapshotSpec);
    
}
