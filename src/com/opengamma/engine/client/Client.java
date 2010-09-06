/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.client;

import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;

import com.opengamma.engine.client.merging.MergedUpdateListener;
import com.opengamma.engine.client.merging.RateLimitingMergingUpdateProvider;
import com.opengamma.engine.client.merging.ViewComputationResultModelMerger;
import com.opengamma.engine.client.merging.ViewDeltaResultModelMerger;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides client-oriented functionality on top of a {@link View} including:
 * <ul>
 *   <li> Rate-limiting of updates
 *   <li> Pausing updates
 * </ul>
 */
public class Client {
  
  private final ClientManager _manager;
  private final View _view;
  private final UserPrincipal _user;
  
  private ClientState _state = ClientState.STOPPED;
  private ReentrantLock _stateLock = new ReentrantLock();
 
  private final RateLimitingMergingUpdateProvider<ViewComputationResultModel> _computationResultProvider;
  private final RateLimitingMergingUpdateProvider<ViewDeltaResultModel> _deltaResultProvider;
  
  private ComputationResultListener _clientResultListener;
  private DeltaComputationResultListener _clientDeltaListener;
  
  private ComputationResultListener _userResultListener;
  private DeltaComputationResultListener _userDeltaListener;
  
  /**
   * Constructs an instance.
   *
   * @param manager  the manager to which this client belongs
   * @param view  the view from which this client can receive computation results
   * @param user  the user who owns this client
   * @param timer  the timer to use for scheduled tasks
   */
  public Client(ClientManager manager, View view, UserPrincipal user, Timer timer) {
    ArgumentChecker.notNull(manager, "manager");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(timer, "timer");
    
    _manager = manager;
    _view = view;
    _user = user;
    _computationResultProvider = new RateLimitingMergingUpdateProvider<ViewComputationResultModel>(new ViewComputationResultModelMerger(), timer);
    _deltaResultProvider = new RateLimitingMergingUpdateProvider<ViewDeltaResultModel>(new ViewDeltaResultModelMerger(), timer);
    subscribeToMergedUpdates();
    
    // Hide the implementations of ComputationResultListener / DeltaComputationResultListener from public view.
    // These forward the raw output from the view to the merging update providers. We are also subscribed to
    // merged updates and will forward these to user listeners.
    _clientResultListener = new ComputationResultListener() {
      @Override
      public UserPrincipal getUser() {
        return Client.this.getUser();
      }
      
      @Override
      public void computationResultAvailable(ViewComputationResultModel resultModel) {
        _computationResultProvider.newResult(resultModel);
      }
    };
    _clientDeltaListener = new DeltaComputationResultListener() {
      @Override
      public UserPrincipal getUser() {
        return Client.this.getUser();
      }
      
      @Override
      public void deltaResultAvailable(ViewDeltaResultModel deltaModel) {
        _deltaResultProvider.newResult(deltaModel);
      }
    };
  }

  private void subscribeToMergedUpdates() {
    _computationResultProvider.addUpdateListener(new MergedUpdateListener<ViewComputationResultModel>() {
      @Override
      public void handleResult(ViewComputationResultModel result) {
        ComputationResultListener listener = _userResultListener;
        if (listener != null) {
          listener.computationResultAvailable(result);
        }
      }
    });
    _deltaResultProvider.addUpdateListener(new MergedUpdateListener<ViewDeltaResultModel>() {
      @Override
      public void handleResult(ViewDeltaResultModel result) {
        DeltaComputationResultListener listener = _userDeltaListener;
        if (listener != null) {
          listener.deltaResultAvailable(result);
        }
      }
    });
  }
  
  //-------------------------------------------------------------------------
  public void start() {
    _stateLock.lock();
    try {
      checkNotTerminated(_state);
      _computationResultProvider.setPaused(false);
      _deltaResultProvider.setPaused(false);
      configureSubscriptions();
      _state = ClientState.STARTED;
    } finally {
      _stateLock.unlock();
    }
  }

  public void pause() {
    _stateLock.lock();
    try {
      checkNotTerminated(_state);
      _computationResultProvider.setPaused(true);
      _deltaResultProvider.setPaused(true);
      configureSubscriptions();
      _state = ClientState.PAUSED;
    } finally {
      _stateLock.unlock();
    }
  }
  
  public void disconnect() {
    _stateLock.lock();
    try {
      if (_state == ClientState.TERMINATED) {
        return;
      }
      stopSubscriptions();
      _state = ClientState.TERMINATED;
    } finally {
      _stateLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------  
  public View getView() {
    return _view;
  }
  
  public UserPrincipal getUser() {
    return _user;
  }

  //-------------------------------------------------------------------------
  public ViewComputationResultModel getLatestResult() {
    return getView().getLatestResult();
  }
  
  /**
   * Sets the result listener.
   * 
   * @param resultListener  the result listener
   */
  public void setResultListener(ComputationResultListener resultListener) {
    // Need to protect both the state and the listener set from change while the client is configured.
    _stateLock.lock();
    try {
      _userResultListener = resultListener;
      if (_state == ClientState.PAUSED || _state == ClientState.STARTED) {
        configureResultSubscription();
      }  
    } finally {
      _stateLock.unlock();
    }
  }

  /**
   * Sets the delta result listener.
   * 
   * @param deltaResultListener  the listener
   */
  public void setDeltaResultListener(DeltaComputationResultListener deltaResultListener) {
    // Need to protect both the state and the listener from change while the client is configured.
    _stateLock.lock();
    try {
      _userDeltaListener = deltaResultListener;
      if (_state == ClientState.PAUSED || _state == ClientState.STARTED) {
        configureDeltaSubscription();
      }
    } finally {
      _stateLock.unlock();
    }
  }
    
  //-------------------------------------------------------------------------
  /**
   * Must be called while holding the state lock
   */
  private void configureSubscriptions() {
    configureResultSubscription();
    configureDeltaSubscription();
  }

  /**
   * Must be called while holding the state lock
   */
  private void configureDeltaSubscription() {
    if (_userDeltaListener != null) {
      _manager.addDeltaResultListener(this, _clientDeltaListener);
    } else {
      _manager.removeDeltaResultListener(this, _clientDeltaListener);
    }
  }

  /**
   * Must be called while holding the state lock
   */
  private void configureResultSubscription() {
    if (_userResultListener != null) {
      _manager.addResultListener(this, _clientResultListener);
    } else {
      _manager.removeResultListener(this, _clientResultListener);
    }
  }
  
  private void stopSubscriptions() {
    _manager.removeResultListener(this, _clientResultListener);
    _manager.removeDeltaResultListener(this, _clientDeltaListener);
  }
  
  private void checkNotTerminated(ClientState state) {
    if (state == ClientState.TERMINATED) {
      throw new IllegalStateException("The client has been terminated. It is not possible to use a terminated client.");
    }
  }
  
}
