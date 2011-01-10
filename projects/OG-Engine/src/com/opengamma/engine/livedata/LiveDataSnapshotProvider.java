/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.Set;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicSPI;

/**
 * Allows a view to create a snapshot of financial data, and subsequently query that snapshot. Snapshots must be
 * explicitly released through the snapshot provider once they are no longer required.
 */
@PublicSPI
public interface LiveDataSnapshotProvider {
  
  /**
   * Adds a listener which will receive notifications of certain events. The events could be related to any
   * subscriptions made through this snapshot provider.
   * 
   * @param listener  the listener to add.
   */
  void addListener(LiveDataSnapshotListener listener);

  /**
   * Notifies the snapshot provider that the specified user is interested in a particular piece of live data. The
   * snapshot provider will attempt to subscribe to this piece of live data, if required, and attached listeners will
   * be notified of the outcome asynchronously.
   * 
   * @param user  the user wishing to make the subscription, for entitlement purposes.
   * @param valueRequirement  describes the live data required.
   */
  void addSubscription(UserPrincipal user, ValueRequirement valueRequirement);
  
  /**
   * Notifies the snapshot provider that the specified user is interested in a particular set of live data. The
   * snapshot provider will attempt to subscribe to this piece of live data, if required, and attached listeners will
   * be notified of the outcome asynchronously. 
   * 
   * @param user  the user wishing to make the subscription, for entitlement purposes.
   * @param valueRequirements  describes the set of live data required.
   */
  void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements);
  
  /**
   * Takes a new snapshot.
   * 
   * @return  the time at which the snapshot was taken (in milliseconds, as returned by
   *          {@link System#currentTimeMillis()}). This must be used subsequently to operate on the snapshot.
   */
  long snapshot();
  
  /**
   * Takes a new snapshot, associating it with a specific snapshot time.  This allows for snapshot providers that don't run in real time.
   * 
   * @param snapshot  the snapshot time with which to associate the new snapshot
   * @return  the time at which the snapshot was taken (provided).  This must be used subsequently to operate on the snapshot.
   */
  long snapshot(long snapshot);

  /**
   * Queries an existing snapshot for a particular piece of data.
   * 
   * @param snapshot  the time of the snapshot. 
   * @param requirement  describes the value required from the snapshot.
   * @return  the value found in the snapshot, or <code>null</code> if the snapshot does not exist or no such value was
   *          found in the snapshot.
   */
  Object querySnapshot(long snapshot, ValueRequirement requirement); 
  
  /**
   * Indicates that a particular snapshot is no longer required and should be deleted. Following a call to this method,
   * any attempts to query the snapshot will fail, resulting in <code>null</code> values being returned. 
   * 
   * @param snapshot  the time of the snapshot.
   */
  void releaseSnapshot(long snapshot);
}
