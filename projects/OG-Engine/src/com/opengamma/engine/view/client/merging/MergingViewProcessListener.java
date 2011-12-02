/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.time.Instant;

import com.google.common.base.Function;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.calc.EngineResourceManagerInternal;
import com.opengamma.engine.view.calc.EngineResourceRetainer;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleExecutionFailedCall;
import com.opengamma.engine.view.listener.CycleFragmentCompletedCall;
import com.opengamma.engine.view.listener.ProcessCompletedCall;
import com.opengamma.engine.view.listener.ProcessTerminatedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompilationFailedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Collects and merges view process updates, releasing them only when {@code drain()} is called.
 * Also ensures that different update types are passed to the underlying listener
 * in the correct order when drained.
 */
public class MergingViewProcessListener implements ViewResultListener {
  
  private final ReentrantLock _mergerLock = new ReentrantLock();
  private final ViewResultListener _underlying;
  
  private boolean _isPassThrough = true;
  private boolean _isLatestResultCycleRetained;
  private EngineResourceRetainer _cycleRetainer;
  
  /**
   * The time at which an update was last received.
   */
  private final AtomicLong _lastUpdateMillis = new AtomicLong(0);
  
  private final List<Function<ViewResultListener, ?>> _callQueue = new LinkedList<Function<ViewResultListener, ?>>();
  
  private int _cycleCompletedIndex = -1;
  private int _cycleFragmentCompletedIndex = -1;
  
  public MergingViewProcessListener(ViewResultListener underlying, EngineResourceManagerInternal<?> cycleManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
    _cycleRetainer = new EngineResourceRetainer(cycleManager);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets whether incoming updates should be allowed to pass straight through without merging.
   * If this is false then updates will not be released unless an update is triggered.
   *  
   * @return true if updates should be passed straight to listeners without merging
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
   * @param passThrough  true if incoming updates should be allowed to pass straight through without
   *                     merging, or false to merge updates until an update is triggered
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
  public UserPrincipal getUser() {
    return getUnderlying().getUser();
  }
  
  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    _mergerLock.lock();
    try {
      if (isPassThrough()) {
        getUnderlying().viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
      } else {
        _callQueue.add(new ViewDefinitionCompiledCall(compiledViewDefinition, hasMarketDataPermissions));
      }
      _lastUpdateMillis.set(System.currentTimeMillis());
    } finally {
      _mergerLock.unlock();
    }
  }
  
  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
    _mergerLock.lock();
    try {
      if (isPassThrough()) {
        getUnderlying().viewDefinitionCompilationFailed(valuationTime, exception);
      } else {
        _callQueue.add(new ViewDefinitionCompilationFailedCall(valuationTime, exception));
      }
    } finally {
      _mergerLock.unlock();
    }
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _mergerLock.lock();
    try {
      if (isLatestResultCycleRetained() && fullResult != null) {
        getCycleRetainer().replaceRetainedCycle(fullResult.getViewCycleId());
      }
      if (isPassThrough()) {
        getUnderlying().cycleCompleted(fullResult, deltaResult);
      } else {
        
        // Result merging is the most complicated. It is based on the following rules:
        //  - only one result call in the queue, kept up-to-date by merging new result calls into it 
        //  - the updated result call is repositioned to the end of the queue
        
        // Result collapsing
        if (_cycleCompletedIndex != -1) {
          // There's an old cycle completed call in the queue - find it and move to end
          CycleCompletedCall cycleCompletedCall = pullCallToEnd(_cycleCompletedIndex);
          // Merge new cycle completed call into old one
          cycleCompletedCall.update(fullResult, deltaResult);
        } else {
          // No existing cycle completed call - add new one
          CycleCompletedCall cycleCompletedCall = new CycleCompletedCall(fullResult, deltaResult);
          _cycleCompletedIndex = _callQueue.size();
          _callQueue.add(cycleCompletedCall);
        }
      }
      _lastUpdateMillis.set(System.currentTimeMillis());
    } finally {
      _mergerLock.unlock();
    }
  }
  
  @Override
  public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
    _mergerLock.lock();
    try {
      if (isPassThrough()) {
        getUnderlying().cycleFragmentCompleted(fullFragment, deltaFragment);
      } else {
        if (_cycleFragmentCompletedIndex != -1) {
          // There's an old fragment completed call in the queue - find it and move to end
          CycleFragmentCompletedCall cycleFragmentCompletedCall = pullCallToEnd(_cycleFragmentCompletedIndex);
          // Merge new fragment completed call into old one
          cycleFragmentCompletedCall.update(fullFragment, deltaFragment);
        } else {
          // No existing fragment completed call - add new one
          CycleFragmentCompletedCall cycleFragmentCompletedCall = new CycleFragmentCompletedCall(fullFragment, deltaFragment);
          _cycleFragmentCompletedIndex = _callQueue.size();
          _callQueue.add(cycleFragmentCompletedCall);
        }
      }
      _lastUpdateMillis.set(System.currentTimeMillis());
    } finally {
      _mergerLock.unlock();
    }
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    _mergerLock.lock();
    try {
      if (isPassThrough()) {
        getUnderlying().cycleExecutionFailed(executionOptions, exception);
      } else {
        _callQueue.add(new CycleExecutionFailedCall(executionOptions, exception));
      }
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
        _callQueue.add(new ProcessCompletedCall());
      }
    } finally {
      _mergerLock.unlock();
    }
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    _mergerLock.lock();
    try {
      if (isPassThrough()) {
        getUnderlying().processTerminated(executionInterrupted);
      } else {
        _callQueue.add(new ProcessTerminatedCall(executionInterrupted));
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
      for (Function<ViewResultListener, ?> call : _callQueue) {
        call.apply(getUnderlying());
      }
      _callQueue.clear();
      _cycleCompletedIndex = -1;
      _cycleFragmentCompletedIndex = -1;
      
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
      _callQueue.clear();
      _cycleCompletedIndex = -1;
      _cycleFragmentCompletedIndex = -1;
      getCycleRetainer().replaceRetainedCycle(null);
    } finally {
      _mergerLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  private <T extends Function<ViewResultListener, ?>> T pullCallToEnd(int fromIndex) {
    int lastIndex = _callQueue.size() - 1;
    if (fromIndex == lastIndex) {
      // Call is already at end of queue
      return (T) _callQueue.get(lastIndex);
    }
    // Call is elsewhere in queue - pull to end and update indices
    T call = (T) _callQueue.remove(fromIndex);
    _callQueue.add(call);
    if (_cycleFragmentCompletedIndex > fromIndex) {
      _cycleFragmentCompletedIndex--;
    } else if (_cycleFragmentCompletedIndex == fromIndex) {
      _cycleFragmentCompletedIndex = lastIndex;
    }
    if (_cycleCompletedIndex > fromIndex) {
      _cycleCompletedIndex--;
    } else if (_cycleCompletedIndex == fromIndex) {
      _cycleCompletedIndex = lastIndex;
    }
    return call;
  }
  
  private ViewResultListener getUnderlying() {
    return _underlying;
  }
  
  private EngineResourceRetainer getCycleRetainer() {
    return _cycleRetainer;
  }
  
}
