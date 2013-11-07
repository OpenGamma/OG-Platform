/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.ObjectId;

/**
 * Listens for changes to a known set of target resolutions. Changes to these resolutions may trigger recompilation of a view definition as the dependency graph based on the old resolutions may no
 * longer be valid.
 */
public abstract class TargetResolverChangeListener implements ChangeListener {

  private static final Logger s_logger = LoggerFactory.getLogger(TargetResolverChangeListener.class);

  /**
   * Marker for the state of watched targets.
   */
  private static enum TargetState {
    /**
     * Notification of changes to the target are required, but it must be checked for any changes between when it was last queried and this state was stored. After such a check, the state may be
     * changed to {@link #WAITING}.
     */
    REQUIRED,
    /**
     * Notification of changes to the target are required, none have been received, and it will not be checked unless notified. After a change is received, the state may be changed to {@link #CHANGED}
     * .
     */
    WAITING,
    /**
     * Notification of changes to the target are required, at least one is pending, and it must now be checked. Before the check is made, the state may be changed to {@link #WAITING}.
     */
    CHANGED
  }

  /**
   * Map of target object identifiers to be monitored, to the monitoring state (see {@link TargetState} members).
   */
  private final ConcurrentMap<ObjectId, TargetState> _targets = new ConcurrentHashMap<ObjectId, TargetState>();
  private boolean _hasRequired;

  public void watch(final ObjectId identifier) {
    _targets.putIfAbsent(identifier, TargetState.REQUIRED);
    _hasRequired = true;
  }

  /**
   * Tests if a change has been seen for an object, or if the object has not been monitored and a manual check is necessary. The state is cleared.
   * 
   * @param identifier the object identifier to test, not null
   * @return true if the object has changed, or was not being monitored, false otherwise
   */
  public boolean isChanged(final ObjectId identifier) {
    TargetState state = _targets.get(identifier);
    if (state == TargetState.WAITING) {
      s_logger.debug("No change to {}", identifier);
      return false;
    }
    // Either new, or has changed; set to WAITING and return TRUE
    s_logger.debug("New or changed identifier {} ({})", identifier, state);
    _targets.put(identifier, TargetState.WAITING);
    return true;
  }

  /**
   * Prunes the watch list to only include the given identifiers.
   * 
   * @param identifiers the identifiers to keep watching, not null and not containing null
   */
  public void watchOnly(final Set<ObjectId> identifiers) {
    _targets.keySet().retainAll(identifiers);
  }

  /**
   * Indicates whether there are any objects that must be checked for updates.
   * <p>
   * Note that this is based on the identifiers that are initially added to the watch-list. Change notifications and {@link #isChanged} calls may mean this is no longer true.
   * 
   * @return true if there are, false otherwise
   */
  public boolean hasChecksPending() {
    return _hasRequired;
  }

  /**
   * Tests if an identifier must be checked for initial updates.
   * 
   * @param identifier the identifier to check, not null
   * @return true if is needs checking, false otherwise
   */
  public boolean isPending(final ObjectId identifier) {
    return _targets.get(identifier) == TargetState.REQUIRED;
  }

  /**
   * Clears the initial update check state for an identifier. If a change notification has not been received it will be marked as "changed" or "not-changed" and no longer returned as pending.
   * 
   * @param identifier the identifier to clear, not null
   * @param changed true if {@link #isChanged} should now return true, false otherwise
   */
  public void clearCheckPending(final ObjectId identifier, final boolean changed) {
    _targets.replace(identifier, TargetState.REQUIRED, changed ? TargetState.CHANGED : TargetState.WAITING);
  }

  /**
   * Clears the flag that {@link #hasChecksPending} returns. Call this if there was an indication of pending checks but none were found.
   */
  public void clearChecksPending() {
    _hasRequired = false;
  }

  protected abstract void onChanged();

  // ChangeListener

  @Override
  public void entityChanged(final ChangeEvent event) {
    final ObjectId oid = event.getObjectId();
    TargetState state = _targets.get(oid);
    if (state == null) {
      return;
    }
    if ((state == TargetState.WAITING) || (state == TargetState.REQUIRED)) {
      if (_targets.replace(oid, state, TargetState.CHANGED)) {
        // If the state changed to anything else, we either don't need the notification or another change message overtook
        // this one and a cycle has already been triggered.
        s_logger.info("Received change notification for {}", oid);
        onChanged();
      }
    }
  }

}
