/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.base.Function;
import com.opengamma.engine.resource.EngineResourceManagerInternal;
import com.opengamma.engine.resource.EngineResourceRetainer;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleExecutionFailedCall;
import com.opengamma.engine.view.listener.CycleFragmentCompletedCall;
import com.opengamma.engine.view.listener.CycleStartedCall;
import com.opengamma.engine.view.listener.ProcessCompletedCall;
import com.opengamma.engine.view.listener.ProcessTerminatedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompilationFailedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Collects and merges view process updates, releasing them only when {@code drain()} is called. Also ensures that different update types are passed to the underlying listener in the correct order
 * when drained.
 */
public class MergingViewProcessListener implements ViewResultListener {

  private static final Logger s_logger = LoggerFactory.getLogger(MergingViewProcessListener.class);

  /**
   * Node in a doubly linked list of calls. This structure allows merged calls to be plucked easily from the list and moved to the end.
   */
  protected static final class Call<T extends Function<ViewResultListener, ?>> {

    private final T _function;
    private Call<?> _prev;
    private Call<?> _next;

    private Call(final T function, final Call<?> last) {
      _function = function;
      _prev = last;
      if (last != null) {
        assert last._next == null;
        last._next = this;
      }
    }

    private T getFunction() {
      return _function;
    }

    private void remove() {
      if (_prev != null) {
        _prev._next = _next;
      }
      if (_next != null) {
        _next._prev = _prev;
      }
    }

    private void moveToEnd(final Call<?> last) {
      assert (last != null) && (last != this) && (last._next == null);
      final Call<?> prev = _prev;
      final Call<?> next = _next;
      _prev = last;
      _next = null;
      last._next = this;
      if (prev != null) {
        prev._next = next;
      }
      if (next != null) {
        next._prev = prev;
      }
    }

  }

  private final ReentrantLock _mergerLock = new ReentrantLock();
  private final ViewResultListener _underlying;

  private boolean _isPassThrough = true;
  private boolean _isLatestResultCycleRetained;
  private EngineResourceRetainer _cycleRetainer;

  /**
   * The time at which an update was last received.
   */
  private final AtomicLong _lastUpdateMillis = new AtomicLong(0);

  private Call<?> _firstCall;
  private Call<?> _lastCall;
  private Call<CycleStartedCall> _previousCycleStarted;
  private Call<CycleStartedCall> _latestCycleStarted;
  private Call<CycleCompletedCall> _cycleCompleted;
  private Call<CycleFragmentCompletedCall> _cycleFragmentCompleted;

  public MergingViewProcessListener(ViewResultListener underlying, EngineResourceManagerInternal<?> cycleManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
    _cycleRetainer = new EngineResourceRetainer(cycleManager);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets whether incoming updates should be allowed to pass straight through without merging. If this is false then updates will not be released unless an update is triggered.
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
   * Sets whether incoming updates should be allowed to pass straight through without merging. If this is changed to <code>true</code> then an update is first triggered to clear any existing merged
   * updates. Subsequent updates will pass straight through until this is set to <code>false</code>.
   * 
   * @param passThrough true if incoming updates should be allowed to pass straight through without merging, or false to merge updates until an update is triggered
   * @return previously batched invocations on the underlying listener as a result of enabling pass-through. It is up to the caller to invoke them.
   */
  protected Call<?> setPassThrough(boolean passThrough) {
    _mergerLock.lock();
    try {
      _isPassThrough = passThrough;
      if (passThrough) {
        // Release anything that's been merged while it hasn't been passing updates straight through
        return takeCallQueue();
      } else {
        return null;
      }
    } finally {
      _mergerLock.unlock();
    }
  }

  /**
   * Gets the time at which the last update was received.
   * 
   * @return the time at which the last udpate was received, in milliseconds
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
      _lastUpdateMillis.set(System.currentTimeMillis());
      if (!isPassThrough()) {
        addCall(new ViewDefinitionCompiledCall(compiledViewDefinition, hasMarketDataPermissions));
        return;
      }
    } finally {
      _mergerLock.unlock();
    }
    getUnderlying().viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
  }

  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
    _mergerLock.lock();
    try {
      if (!isPassThrough()) {
        addCall(new ViewDefinitionCompilationFailedCall(valuationTime, exception));
        return;
      }
    } finally {
      _mergerLock.unlock();
    }
    getUnderlying().viewDefinitionCompilationFailed(valuationTime, exception);
  }

  @Override
  public void cycleStarted(ViewCycleMetadata cycleMetadata) {
    _mergerLock.lock();
    try {
      if (!isPassThrough()) {
        if (_previousCycleStarted != null) {
          // This shouldn't happen if notifications appear as expected
          removeCall(_previousCycleStarted);
        }
        _previousCycleStarted = _latestCycleStarted;
        _latestCycleStarted = addCall(new CycleStartedCall(cycleMetadata));
        return;
      }
    } finally {
      _mergerLock.unlock();
    }
    getUnderlying().cycleStarted(cycleMetadata);
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _mergerLock.lock();
    try {
      if (isLatestResultCycleRetained() && fullResult != null) {
        getCycleRetainer().replaceRetainedCycle(fullResult.getViewCycleId());
      }
      _lastUpdateMillis.set(System.currentTimeMillis());
      if (!isPassThrough()) {

        // Result merging is the most complicated. It is based on the following rules:
        //  - only one result call in the queue, kept up-to-date by merging new result calls into it 
        //  - the updated result call is repositioned to the end of the queue

        // Result collapsing
        if (_cycleCompleted != null) {
          // There's an old cycle completed call in the queue - move to end
          putCallToEnd(_cycleCompleted);
          // Merge new cycle completed call into old one
          _cycleCompleted.getFunction().update(fullResult, deltaResult);
        } else {
          // No existing cycle completed call - add new one
          _cycleCompleted = addCall(new CycleCompletedCall(fullResult, deltaResult));
        }

        // Only keep the cycle started call for the latest complete result
        if (_previousCycleStarted != null) {
          removeCall(_previousCycleStarted);
          _previousCycleStarted = null;
        }
        return;
      }
    } finally {
      _mergerLock.unlock();
    }
    getUnderlying().cycleCompleted(fullResult, deltaResult);
  }

  @Override
  public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
    _mergerLock.lock();
    try {
      _lastUpdateMillis.set(System.currentTimeMillis());
      if (!isPassThrough()) {
        if (_cycleFragmentCompleted != null) {
          // There's an old fragment completed call in the queue - move to end
          putCallToEnd(_cycleFragmentCompleted);
          // Merge new fragment completed call into old one
          _cycleFragmentCompleted.getFunction().update(fullFragment, deltaFragment);
        } else {
          // No existing fragment completed call - add new one
          _cycleFragmentCompleted = addCall(new CycleFragmentCompletedCall(fullFragment, deltaFragment));
        }
        return;
      }
    } finally {
      _mergerLock.unlock();
    }
    getUnderlying().cycleFragmentCompleted(fullFragment, deltaFragment);
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    _mergerLock.lock();
    try {
      if (!isPassThrough()) {
        addCall(new CycleExecutionFailedCall(executionOptions, exception));
        return;
      }
    } finally {
      _mergerLock.unlock();
    }
    getUnderlying().cycleExecutionFailed(executionOptions, exception);
  }

  @Override
  public void processCompleted() {
    _mergerLock.lock();
    try {
      if (!isPassThrough()) {
        addCall(new ProcessCompletedCall());
        return;
      }
    } finally {
      _mergerLock.unlock();
    }
    getUnderlying().processCompleted();
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    _mergerLock.lock();
    try {
      getCycleRetainer().replaceRetainedCycle(null);
      if (!isPassThrough()) {
        addCall(new ProcessTerminatedCall(executionInterrupted));
        return;
      }
    } finally {
      _mergerLock.unlock();
    }
    getUnderlying().processTerminated(executionInterrupted);
  }

  @Override
  public void clientShutdown(Exception e) {
    // Client shutdowns are not queued
  }

  /**
   * Clears the internal call queue state. The caller must hold the {@link #_mergerLock}.
   */
  private void clearCallQueue() {
    _lastCall = null;
    _previousCycleStarted = null;
    _latestCycleStarted = null;
    _cycleCompleted = null;
    _cycleFragmentCompleted = null;
  }

  /**
   * Returns the head of the queue, clearing it. The caller must hold the {@link #_mergerLock} lock.
   * 
   * @return the first element in the taken queue, or null if there was none
   */
  private Call<?> takeCallQueue() {
    final Call<?> result;
    result = _firstCall;
    clearCallQueue();
    return result;
  }

  /**
   * Makes queued calls to the underlying. The caller must not hold the {@link #_mergerLock} lock.
   * 
   * @param call the node head of the list, null for an empty list
   */
  protected void invoke(Call<?> call) {
    while (call != null) {
      try {
        call.getFunction().apply(getUnderlying());
      } catch (RuntimeException e) {
        s_logger.error("Error notifying underlying of {}: {}", call.getFunction(), e.getMessage());
        s_logger.warn("Caught exception", e);
      }
      call = call._next;
    }
  }

  /**
   * Invokes all of the calls from the queue, clearing it. The caller must not hold the {@link #_mergerLock} lock.
   */
  protected void drain() {
    final Call<?> calls;
    _mergerLock.lock();
    try {
      calls = takeCallQueue();
    } finally {
      _mergerLock.unlock();
    }
    invoke(calls);
  }

  /**
   * Consumes any updates waiting in the merger without notifying the underlying listener.
   */
  public void reset() {
    _mergerLock.lock();
    try {
      clearCallQueue();
      getCycleRetainer().replaceRetainedCycle(null);
    } finally {
      _mergerLock.unlock();
    }
  }

  /**
   * Moves the given call to the end of the linked list. The caller must hold the {@link #_mergerLock} lock.
   */
  private void putCallToEnd(final Call<?> call) {
    if (call == _lastCall) {
      // Already at the end
      return;
    }
    if (call == _firstCall) {
      // At the head of the queue, and there is more than one element
      _firstCall = call._next;
    }
    call.moveToEnd(_lastCall);
    _lastCall = call;
  }

  /**
   * Removes the given call from the linked list. The caller must hold the {@link #_mergerLock} lock.
   */
  private void removeCall(final Call<?> call) {
    if (call == _firstCall) {
      // At the head of the queue
      _firstCall = call._next;
    }
    if (call == _lastCall) {
      // At the tail of the queue
      _lastCall = call._prev;
    }
    call.remove();
  }

  /**
   * Adds a new call to the end of the linked list. The caller must hold the {@link #_mergerLock} lock.
   */
  private <T extends Function<ViewResultListener, ?>> Call<T> addCall(final T function) {
    final Call<T> call = new Call<T>(function, _lastCall);
    if (_firstCall == null) {
      // First element into the queue
      _firstCall = call;
    }
    _lastCall = call;
    return call;
  }

  private ViewResultListener getUnderlying() {
    return _underlying;
  }

  private EngineResourceRetainer getCycleRetainer() {
    return _cycleRetainer;
  }

}
