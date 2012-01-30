/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.util.ArgumentChecker;

/**
 * A subscription handle is kept by the client while a subscription is being established.
 * After the subscription has been established, it is no longer needed.
 * 
 * @author kirk
 */
public class SubscriptionHandle {
  
  private static final Logger s_logger = LoggerFactory.getLogger(SubscriptionHandle.class);
  
  private final UserPrincipal _user;
  private final SubscriptionType _subscriptionType;
  private final LiveDataSpecification _requestedSpecification;
  private final LiveDataListener _listener;
  private final List<LiveDataValueUpdateBean> _ticksOnHold = new ArrayList<LiveDataValueUpdateBean>();
  private LiveDataValueUpdateBean _snapshotOnHold; // = null;
  
  public SubscriptionHandle(
      UserPrincipal user,
      SubscriptionType subscriptionType,
      LiveDataSpecification requestedSpecification,
      LiveDataListener listener) {
    ArgumentChecker.notNull(user, "User credentials");
    ArgumentChecker.notNull(subscriptionType, "Subscription type");
    ArgumentChecker.notNull(requestedSpecification, "Requested Specification");
    ArgumentChecker.notNull(listener, "Live Data Listener");
    _user = user;
    _subscriptionType = subscriptionType;
    _requestedSpecification = requestedSpecification;
    _listener = listener;
  }

  /**
   * @return the user principal
   */
  public UserPrincipal getUser() {
    return _user;
  }
  
  public SubscriptionType getSubscriptionType() {
    return _subscriptionType;
  }

  /**
   * @return the requestedSpecification
   */
  public LiveDataSpecification getRequestedSpecification() {
    return _requestedSpecification;
  }

  /**
   * @return the listener
   */
  public LiveDataListener getListener() {
    return _listener;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  /**
   * Informs the client listener about the response received from the server 
   * 
   * @param response Response received, not null
   */
  public void subscriptionResultReceived(LiveDataSubscriptionResponse response) {
    
    if (_subscriptionType == SubscriptionType.SNAPSHOT) {
      if (response.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
        s_logger.debug("Got snapshot {}", getRequestedSpecification());
      } else {
        s_logger.debug("Failed to snapshot {}. Result was {}, msg = {}", 
            new Object[] {getRequestedSpecification(), response.getSubscriptionResult(), response.getUserMessage()});
      }
    } else {
      if (response.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
        s_logger.debug("Established subscription to {}", getRequestedSpecification());
      } else {
        s_logger.debug("Failed to establish subscription to {}. Result was {}, msg = {}", 
            new Object[] {getRequestedSpecification(), response.getSubscriptionResult(), response.getUserMessage()});
      }
    }
    
    getListener().subscriptionResultReceived(response);
  }
  
  /**
   * In a two-phase subscription procedure (see LIV-18), after a subscription is established, 
   * the client needs to get a snapshot from the server.
   * Between establishing the subscription and getting the snapshot, all ticks
   * must be kept in memory and only released after the snapshot is received.
   * 
   * @param tick Tick to add to temporary memory store
   */
  public synchronized void addTickOnHold(LiveDataValueUpdateBean tick) {
    _ticksOnHold.add(tick);
  }
  
  /**
   * In a two-phase subscription procedure (see LIV-18), after a subscription is established, 
   * the client needs to get a snapshot from the server. This method is used 
   * to store that snapshot.
   * 
   * @param snapshot The snapshot to be placed on hold
   */
  public synchronized void addSnapshotOnHold(LiveDataValueUpdateBean snapshot) {
    if (_snapshotOnHold != null) {
      throw new IllegalStateException("Snapshot has already been set");
    }
    
    _snapshotOnHold = snapshot;
  }
  
  /**
   * Releases the snapshot and ticks stored in memory. 
   * For an explanation of why we need to do this, see LIV-18.
   * The method copes with server restarts during the subscription process
   * by assuming that the server sends a full image to the client
   * when it restarts.
   */
  public synchronized void releaseTicksOnHold() {
    if (_snapshotOnHold == null) {
      // this will happen if the snapshot failed.
      s_logger.debug("No ticks to send to {}. {}", getListener(), getRequestedSpecification());
      return; 
    }
    
    long snapshotSequenceNo = _snapshotOnHold.getSequenceNumber();
    
    // Find the LAST reset (in theory, there could be multiple resets although
    // this is a highly theoretical case)
    Integer resetIndex = null;
    for (int i = 0; i < _ticksOnHold.size(); i++) {
      LiveDataValueUpdateBean tick = _ticksOnHold.get(i);
      if (tick.getSequenceNumber() == LiveDataValueUpdate.SEQUENCE_START) {
        resetIndex = i;                
      }
    }
    
    if (resetIndex == null) {
      s_logger.debug("{}: Sending snapshot and {} ticks on hold to {}", 
          new Object[] {getRequestedSpecification(), _ticksOnHold.size(), getListener()});
      
      // No resets. This is the normal case. Use the snapshot
      // and any subsequent ticks. The subsequent ticks
      // are not sorted, but are played back in the order received,
      // which hopefully should be the sequence number order (i.e., no sorting necessary). 
      _listener.valueUpdate(_snapshotOnHold);
      
      for (LiveDataValueUpdateBean tick : _ticksOnHold) {
        if (tick.getSequenceNumber() > snapshotSequenceNo) {
          _listener.valueUpdate(tick);
        }
      }
    } else {
      s_logger.debug("{}: Reset detected. Sending {} ticks on hold to {}", 
          new Object[] {getRequestedSpecification(), _ticksOnHold.size() - resetIndex, getListener()});
      
      // This happens when the server is reset (rebooted/migrated) while subscribing.
      // We assume that the tick with sequence number = 0
      // is a full update (as LiveDataValueUpdate.getSequenceNumber() specifies).
      // Using this assumption, we first use the tick with sequence number = 0, which
      // acts as the snapshot, and then simply send any subsequent ticks in order.
      for (int i = resetIndex; i < _ticksOnHold.size(); i++) {
        LiveDataValueUpdateBean tick = _ticksOnHold.get(i);
        _listener.valueUpdate(tick);
      }
    }
      
    _ticksOnHold.clear();
    _snapshotOnHold = null;
  }

}
