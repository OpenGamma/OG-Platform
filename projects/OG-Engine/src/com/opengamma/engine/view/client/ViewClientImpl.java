/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.engine.view.client.merging.MergedUpdateListener;
import com.opengamma.engine.view.client.merging.RateLimitingMergingUpdateProvider;
import com.opengamma.engine.view.client.merging.ViewComputationResultModelMerger;
import com.opengamma.engine.view.client.merging.ViewDeltaResultModelMerger;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides client-oriented functionality on top of a {@link View} including:
 * <ul>
 *   <li> Rate-limiting of updates
 *   <li> Pausing updates
 * </ul>
 */
public class ViewClientImpl implements ViewClient {
  
  private static final Logger s_logger = LoggerFactory.getLogger(ViewClientImpl.class);
  
  private final UniqueIdentifier _id;
  private final ViewImpl _view;
  private final UserPrincipal _user;
  
  private ViewClientState _state = ViewClientState.STOPPED;
  private ReentrantLock _clientLock = new ReentrantLock();
  
  private final AtomicReference<ViewComputationResultModel> _latestResult = new AtomicReference<ViewComputationResultModel>();
  private final RateLimitingMergingUpdateProvider<ViewComputationResultModel> _liveComputationResultProvider;
  private final RateLimitingMergingUpdateProvider<ViewDeltaResultModel> _liveDeltaResultProvider;
  
  private ComputationResultListener _liveResultListener;
  private DeltaComputationResultListener _liveDeltaListener;
  
  private ComputationResultListener _userResultListener;
  private DeltaComputationResultListener _userDeltaListener;
  
  /**
   * Constructs an instance.
   *
   * @param id  the unique identifier assigned to this view client
   * @param view  the view from which this client can receive computation results
   * @param user  the user who owns this client
   * @param timer  the timer to use for scheduled tasks
   */
  public ViewClientImpl(UniqueIdentifier id, ViewImpl view, UserPrincipal user, Timer timer) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(timer, "timer");
    
    _id = id;
    _view = view;
    _user = user;
    _liveComputationResultProvider = new RateLimitingMergingUpdateProvider<ViewComputationResultModel>(new ViewComputationResultModelMerger(), timer);
    _liveDeltaResultProvider = new RateLimitingMergingUpdateProvider<ViewDeltaResultModel>(new ViewDeltaResultModelMerger(), timer);
    subscribeToMergedUpdates();
    
    // Hide the implementations of ComputationResultListener / DeltaComputationResultListener from public view.
    // These forward the raw output from the view to the merging update providers. We are also subscribed to
    // merged updates and will forward these to user listeners.
    _liveResultListener = new ComputationResultListener() {
      @Override
      public UserPrincipal getUser() {
        return ViewClientImpl.this.getUser();
      }
      
      @Override
      public void computationResultAvailable(ViewComputationResultModel resultModel) {
        _liveComputationResultProvider.newResult(resultModel);
      }
    };
    _liveDeltaListener = new DeltaComputationResultListener() {
      @Override
      public UserPrincipal getUser() {
        return ViewClientImpl.this.getUser();
      }
      
      @Override
      public void deltaResultAvailable(ViewDeltaResultModel deltaModel) {
        _liveDeltaResultProvider.newResult(deltaModel);
      }
    };
  }

  private void subscribeToMergedUpdates() {
    _liveComputationResultProvider.addUpdateListener(new MergedUpdateListener<ViewComputationResultModel>() {
      @Override
      public void handleResult(ViewComputationResultModel result) {
        updateLatestResult(result);
        ComputationResultListener listener = _userResultListener;
        if (listener != null) {
          listener.computationResultAvailable(result);
        }
      }
    });
    _liveDeltaResultProvider.addUpdateListener(new MergedUpdateListener<ViewDeltaResultModel>() {
      @Override
      public void handleResult(ViewDeltaResultModel result) {
        DeltaComputationResultListener listener = _userDeltaListener;
        if (listener != null) {
          listener.deltaResultAvailable(result);
        }
      }
    });
  }
  
  @Override
  public View getView() {
    return _view;
  }
  
  @Override
  public UniqueIdentifier getUniqueId() {
    return _id;
  }

  //-------------------------------------------------------------------------
  @Override
  public UserPrincipal getUser() {
    return _user;
  }
  
  @Override
  public boolean isResultAvailable() {
    return _latestResult.get() != null;
  }
  
  @Override
  public ViewComputationResultModel getLatestResult() {
    return _latestResult.get();
  }
  
  @Override
  public void setResultListener(ComputationResultListener resultListener) {
    _clientLock.lock();
    try {
      _userResultListener = resultListener;
      if (_state == ViewClientState.PAUSED || _state == ViewClientState.STARTED) {
        configureViewResultSubscription();
      }  
    } finally {
      _clientLock.unlock();
    }
  }

  @Override
  public void setDeltaResultListener(DeltaComputationResultListener deltaResultListener) {
    _clientLock.lock();
    try {
      _userDeltaListener = deltaResultListener;
      if (_state == ViewClientState.PAUSED || _state == ViewClientState.STARTED) {
        configureViewDeltaSubscription();
      }
    } finally {
      _clientLock.unlock();
    }
  }
  
  @Override
  public ViewClientState getState() {
    return _state;
  }
  
  @Override
  public void setLiveUpdatePeriod(long periodMillis) {
    _liveComputationResultProvider.setMinimumUpdatePeriodMillis(periodMillis);
    _liveDeltaResultProvider.setMinimumUpdatePeriodMillis(periodMillis);
  }
  
  @Override
  public void startLive() {
    _clientLock.lock();
    try {
      checkNotTerminated(_state);
      _liveComputationResultProvider.setPaused(false);
      _liveDeltaResultProvider.setPaused(false);
      configureViewSubscriptions();
      _view.addLiveComputationClient(this);
      _state = ViewClientState.STARTED;
    } finally {
      _clientLock.unlock();
    }
  }

  @Override
  public void pauseLive() {
    _clientLock.lock();
    try {
      checkNotTerminated(_state);
      _liveComputationResultProvider.setPaused(true);
      _liveDeltaResultProvider.setPaused(true);
      configureViewSubscriptions();
      _view.addLiveComputationClient(this);
      _state = ViewClientState.PAUSED;
    } finally {
      _clientLock.unlock();
    }
  }
  
  public void stopLive() {
    _clientLock.lock();
    try {
      checkNotTerminated(_state);
      _view.removeLiveComputationClient(this);
      stopViewSubscriptions();
      stopProvider(_liveComputationResultProvider);
      stopProvider(_liveDeltaResultProvider);
      _state = ViewClientState.STOPPED;
    } finally {
      _clientLock.unlock();
    }    
  }
  
  private class RunOneCycleListener implements ComputationResultListener {
    private volatile ViewComputationResultModel _returnValue;
    
    @Override
    public UserPrincipal getUser() {
      return ViewClientImpl.this.getUser();
    }
    
    @Override
    public void computationResultAvailable(ViewComputationResultModel resultModel) {
      _returnValue = resultModel;
    }

  };
  
  @Override
  public ViewComputationResultModel runOneCycle(final long valuationTime) {
    final RunOneCycleListener listener = new RunOneCycleListener();

    _clientLock.lock();
    try {
      checkNotTerminated(_state);
      
      // don't really care if client is terminated while cycle is running
      // so don't hold onto the lock. This also allows multiple
      // one-off cycles to be run in parallel.

    } finally {
      _clientLock.unlock();
    }
    
    try {
      _view.runOneCycle(valuationTime, _view.getLiveDataSnapshotProvider(), listener);
    } catch (Exception e) {
      s_logger.error("Run one cycle failed", e);
      return null;
    }
    
    return listener._returnValue;
  }
  
  @Override
  public void shutdown() {
    _clientLock.lock();
    try {
      if (_state == ViewClientState.TERMINATED) {
        return;
      }
      _view.removeLiveComputationClient(this);
      stopViewSubscriptions();
      _liveComputationResultProvider.shutdown();
      _liveDeltaResultProvider.shutdown();
      _state = ViewClientState.TERMINATED;
    } finally {
      _clientLock.unlock();
    }
  }
    
  //-------------------------------------------------------------------------
  private void configureViewSubscriptions() {
    configureViewResultSubscription();
    configureViewDeltaSubscription();
  }

  private void configureViewDeltaSubscription() {
    if (_userDeltaListener != null) {
      _view.addDeltaResultListener(_liveDeltaListener);
    } else {
      _view.removeDeltaResultListener(_liveDeltaListener);
    }
  }

  private void configureViewResultSubscription() {
    // Always want to listen to the full results so that getLatestResult works correctly
    _view.addResultListener(_liveResultListener);
  }
  
  private void stopViewSubscriptions() {
    _view.removeResultListener(_liveResultListener);
    _view.removeDeltaResultListener(_liveDeltaListener);
  }
  
  private void checkNotTerminated(ViewClientState state) {
    if (state == ViewClientState.TERMINATED) {
      throw new IllegalStateException("The client has been terminated. It is not possible to use a terminated client.");
    }
  }
  
  private void updateLatestResult(ViewComputationResultModel result) {
    _latestResult.set(result);
  }
  
  private void stopProvider(RateLimitingMergingUpdateProvider<?> provider) {
    // Leave the provider paused to stop the (potential) flow of outbound updates
    provider.setPaused(true);
    
    // Reset the merger to remove any update waiting to be sent to listeners
    provider.resetMerger();
  }
  
}
