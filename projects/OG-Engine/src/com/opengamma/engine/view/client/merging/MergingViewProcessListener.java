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
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calc.EngineResourceManagerInternal;
import com.opengamma.engine.view.calc.EngineResourceRetainer;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleExecutionFailedCall;
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
  
  private int _currentResultCompilationIndex = -1;
  private int _resultIndex = -1;
  private int _futureResultCompilationIndex = -1;
  
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
        if (_futureResultCompilationIndex != -1) {
          // Another compilation without a result in the meantime. Perhaps errors are occurring.  
          _callQueue.remove(_futureResultCompilationIndex);
        }
        _futureResultCompilationIndex = _callQueue.size();
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
        //  - a compiled view definition is only retained in the queue while it corresponds to the current result or a
        //    future result; updating the result call could make an old compilation redundant
        
        // Result collapsing
        if (_resultIndex != -1) {
          // There's an old result call in the queue - find it and move to end
          CycleCompletedCall resultCall;
          int lastIndex = _callQueue.size() - 1;
          if (_resultIndex == lastIndex) {
            // Old result is already at end of queue
            resultCall = (CycleCompletedCall) _callQueue.get(lastIndex);
          } else {
            // Old result is elsewhere in queue - pull to end and update indices
            resultCall = (CycleCompletedCall) _callQueue.remove(_resultIndex);
            _callQueue.add(resultCall);
            if (_futureResultCompilationIndex > _resultIndex) {
              _futureResultCompilationIndex--;
            }
            _resultIndex = lastIndex;
          }
          
          // Merge new result into old one
          resultCall.update(fullResult, deltaResult);
        } else {
          // No existing result call - add new one
          CycleCompletedCall resultCall = new CycleCompletedCall(fullResult, deltaResult);
          _resultIndex = _callQueue.size();
          _callQueue.add(resultCall);
        }

        // Compilation collapsing
        if (_futureResultCompilationIndex != -1) {
          if (_currentResultCompilationIndex != -1) {
            // No longer interested in the compilation which applied to the result before it was updated
            _callQueue.remove(_currentResultCompilationIndex);
            _resultIndex--;
            _futureResultCompilationIndex--;
          }
          
          // The last compilation now applies to the current result
          _currentResultCompilationIndex = _futureResultCompilationIndex;
          _futureResultCompilationIndex = -1;
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
  public void jobResultReceived(ViewResultModel result, ViewDeltaResultModel delta) {
    _mergerLock.lock();
    try {
      if (isPassThrough()) {
        getUnderlying().jobResultReceived(result, delta);
      } else {
        _callQueue.add(new ProcessCompletedCall());
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
      _currentResultCompilationIndex = -1;
      _resultIndex = -1;
      _futureResultCompilationIndex = -1;
      
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
      _currentResultCompilationIndex = -1;
      _resultIndex = -1;
      _futureResultCompilationIndex = -1;
      getCycleRetainer().replaceRetainedCycle(null);
    } finally {
      _mergerLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  private ViewResultListener getUnderlying() {
    return _underlying;
  }
  
  private EngineResourceRetainer getCycleRetainer() {
    return _cycleRetainer;
  }
  
}
