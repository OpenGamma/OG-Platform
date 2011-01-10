/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.opengamma.util.ArgumentChecker;

/**
 * Collects and merges updates, releasing them to listeners only when {@link #triggerUpdate()} is called.
 * 
 * @param <T>  the type of the updates
 */
public class MergingUpdateProvider<T> {
  
  private final ReentrantLock _mergerLock = new ReentrantLock();
  private final IncrementalMerger<T> _merger;
  
  private boolean _isPassThrough;
  
  /**
   * The time at which a result was last received.
   */
  private final AtomicLong _lastResultMillis = new AtomicLong(0);

  /**
   * A set of listeners subscribed to updates from this provider. 
   */
  private final Set<MergedUpdateListener<T>> _updateListeners = new CopyOnWriteArraySet<MergedUpdateListener<T>>();
  
  public MergingUpdateProvider(IncrementalMerger<T> merger) {
    ArgumentChecker.notNull(merger, "merger");
    _merger = merger;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets whether incoming updates should be allowed to pass straight through without merging. If this is
   * <code>false</code> then updates will not be released unless {@link #triggerUpdate()} is called.
   *  
   * @return <code>true</code> if updates should be passed straight to listeners without merging, <code>false</code>
   *         otherwise.
   */
  protected boolean isPassThrough() {
    _mergerLock.lock();
    try {
      return _isPassThrough;
    } finally {
      _mergerLock.unlock();
    }
  }
  
  /**
   * Sets whether incoming updates should be allowed to pass straight through without merging. If this is changed to
   * <code>true</code> then an update is first triggered to clear any existing merged updates. Subsequent updates will
   * pass straight through until this is set to <code>false</code>.
   * 
   * @param passThrough  <code>true</code> if incoming updates should be allowed to pass straight through without
   *                     merging, or <code>false</code> to merge updates until {@link #triggerUpdate()} is called.
   */
  protected void setPassThrough(boolean passThrough) {
    _mergerLock.lock();
    try {
      _isPassThrough = passThrough;
      if (passThrough) {
        // Release anything that's been merged while it hasn't been passing updates straight through
        triggerUpdate();
      }
    } finally {
      _mergerLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  /**
   * Forces the current merged update, if any, to be sent to listeners now. This will happen synchronously.
   */
  public void triggerUpdate() {
    _mergerLock.lock();
    try {
      notifyListeners(_merger.consume());
    } finally {
      _mergerLock.unlock();
    }
  }
  
  /**
   * Consumes any update waiting in the merger without notifying the listeners.
   */
  public void resetMerger() {
    _mergerLock.lock();
    try {
      _merger.consume();
    } finally {
      _mergerLock.unlock();
    }
  }
  
  /**
   * Pushes a new result into the merger. This will be handled according to the current state and settings.
   * 
   * @param result  the new result
   */
  public void newResult(T result) {
    _mergerLock.lock();
    try {
      if (isPassThrough()) {
        notifyListeners(result);
      } else {
        _merger.merge(result);
      }
      _lastResultMillis.set(System.currentTimeMillis());
    } finally {
      _mergerLock.unlock();
    }
  }
  
  /**
   * Gets the time at which the last result was received.
   * 
   * @return  the time at which the last result was received, in milliseconds
   */
  protected long getLastResultTimeMillis() {
    return _lastResultMillis.get();
  }
  
  //-------------------------------------------------------------------------
  /**
   * Causes the specified listener to receive updates from this provider.
   * 
   * @param listener  the listener to add, not null
   */
  public void addUpdateListener(MergedUpdateListener<T> listener) {
    ArgumentChecker.notNull(listener, "listener");
    _updateListeners.add(listener);
  }
  
  /**
   * Stops a listener from receiving updates from this provider.
   * 
   * @param listener  the listener to remove, not null
   */
  public void removeUpdateListener(MergedUpdateListener<T> listener) {
    ArgumentChecker.notNull(listener, "listener");
    _updateListeners.remove(listener);
  }
  
  private void notifyListeners(T result) {
    if (result == null) {
      return;
    }
    for (MergedUpdateListener<T> listener : _updateListeners) {
      listener.handleResult(result);
    }
  }

}
