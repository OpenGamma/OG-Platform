/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessListener;
import com.opengamma.engine.view.calc.ViewCycleManager;
import com.opengamma.engine.view.calc.ViewCycleRetainer;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Collects and merges view process updates, releasing them only when {@link #drain()} is called. Also ensures that
 * different update types are passed to the underlying listener in the correct order when drained.
 */
public class MergingViewProcessListener implements ViewProcessListener {
  
  private final ReentrantLock _mergerLock = new ReentrantLock();
  private final ViewProcessListener _underlying;
  
  private boolean _isPassThrough = true;
  private boolean _isLatestResultCycleRetained;
  private ViewCycleRetainer _cycleRetainer;
  
  /**
   * The time at which an update was last received.
   */
  private final AtomicLong _lastUpdateMillis = new AtomicLong(0);
  
  // REVIEW jonathan 2011-03-25 -- currently the view process listener interface is simple enough that calls can be
  // interpreted and stored here for later draining. If it gets much worse then a collapsing queue of objects modelling
  // the calls would be a better approach.
  
  private ViewComputationResultModel _latestFullResult;
  private final IncrementalMerger<ViewDeltaResultModel> _deltaResultMerger = new ViewDeltaResultModelMerger();
  
  private ViewEvaluationModel _preResultCompilation;
  private ViewEvaluationModel _postResultCompilation;
  
  private boolean _processCompleted;
  private boolean _shutdown;
  
  public MergingViewProcessListener(ViewProcessListener underlying, ViewCycleManager cycleManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
    _cycleRetainer = new ViewCycleRetainer(cycleManager);
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
        drain();
      }
    } finally {
      _mergerLock.unlock();
    }
  }
  
  /**
   * Gets the time at which the last update was received.
   * 
   * @return  the time at which the last udpate was received, in milliseconds
   */
  protected long getLastUpdateTimeMillis() {
    return _lastUpdateMillis.get();
  }
  
  //-------------------------------------------------------------------------
  public boolean isLatestResultCycleRetained() {
    return _isLatestResultCycleRetained;
  }
  
  public void setLatestResultCycleRetained(boolean isLatestResultCycleRetained) {
    _mergerLock.lock();
    try {
      _isLatestResultCycleRetained = isLatestResultCycleRetained;
      if (!isLatestResultCycleRetained) {
        getCycleRetainer().replaceRetainedCycle(null);
      }
    } finally {
      _mergerLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  @Override
  public boolean isDeltaResultRequired() {
    return getUnderlying().isDeltaResultRequired();
  }

  @Override
  public void compiled(ViewEvaluationModel viewEvaluationModel) {
    _mergerLock.lock();
    try {
      if (isPassThrough()) {
        getUnderlying().compiled(viewEvaluationModel);
      } else {
        _postResultCompilation = viewEvaluationModel;
      }
      _lastUpdateMillis.set(System.currentTimeMillis());
    } finally {
      _mergerLock.unlock();
    }
  }

  @Override
  public void result(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _mergerLock.lock();
    try {
      if (isLatestResultCycleRetained() && fullResult != null) {
        getCycleRetainer().replaceRetainedCycle(fullResult.getViewCycleId());
      }
      if (isPassThrough()) {
        getUnderlying().result(fullResult, deltaResult);
      } else {
        if (_postResultCompilation != null) {
          // The current post-result compilation happened before the new result
          // Any old pre-result compilation is irrelevant because it corresponds to a result that we're about to replace
          _preResultCompilation = _postResultCompilation;
          _postResultCompilation = null;
        }
        _latestFullResult = fullResult;
        if (isDeltaResultRequired() && deltaResult != null) {
          _deltaResultMerger.merge(deltaResult);
        }
      }
      _lastUpdateMillis.set(System.currentTimeMillis());
    } finally {
      _mergerLock.unlock();
    }
  }
  
  @Override
  public void processCompleted() {
    _mergerLock.lock();
    try {
      if (isPassThrough()) {
        getUnderlying().processCompleted();
      } else {
        _processCompleted = true;
      }
    } finally {
      _mergerLock.unlock();
    }
  }
  
  @Override
  public void shutdown() {
    _mergerLock.lock();
    try {
      if (isPassThrough()) {
        getUnderlying().shutdown();
      } else {
        _shutdown = true;
      }
      getCycleRetainer().replaceRetainedCycle(null);
    } finally {
      _mergerLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  public void drain() {
    _mergerLock.lock();
    try {
      if (_preResultCompilation != null) {
        getUnderlying().compiled(_preResultCompilation);
        _preResultCompilation = null;
      }
      if (_latestFullResult != null) {
        getUnderlying().result(_latestFullResult, _deltaResultMerger.consume());
        _latestFullResult = null;
      }
      if (_postResultCompilation != null) {
        getUnderlying().compiled(_postResultCompilation);
        _postResultCompilation = null;
      }
      if (_processCompleted) {
        getUnderlying().processCompleted();
        _processCompleted = false;
      }
      if (_shutdown) {
        getUnderlying().shutdown();
        _shutdown = false;
      }
    } finally {
      _mergerLock.unlock();
    }
  }
  
  /**
   * Consumes any updates waiting in the merger without notifying the underlying listener.
   */
  public void reset() {
    _mergerLock.lock();
    try {
      _preResultCompilation = null;
      _postResultCompilation = null;
      _latestFullResult = null;
      _deltaResultMerger.consume();
      getCycleRetainer().replaceRetainedCycle(null);
    } finally {
      _mergerLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  private ViewProcessListener getUnderlying() {
    return _underlying;
  }
  
  private ViewCycleRetainer getCycleRetainer() {
    return _cycleRetainer;
  }
  
}
