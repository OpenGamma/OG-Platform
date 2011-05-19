/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import javax.time.Instant;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Provides standard view process execution options.
 */
@PublicAPI
public class ExecutionOptions implements ViewExecutionOptions {

  private ViewCycleExecutionSequence _executionSequence;
  private final boolean _runAsFastAsPossible;
  private final boolean _liveDataTriggerEnabled;
  private final Integer _maxSuccessiveDeltaCycles;
  private final boolean _compileOnly;
  private final UniqueIdentifier _marketDataSnapshotIdentifier;
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, boolean liveDataTriggerEnabled) {
    this(executionSequence, liveDataTriggerEnabled, null);
  }
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, boolean liveDataTriggerEnabled, Integer maxSuccessiveDeltaCycles) {
    this(executionSequence, false, liveDataTriggerEnabled, null, false);
  }
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, boolean runAsFastAsPossible,
      boolean liveDataTriggerEnabled, Integer maxSuccessiveDeltaCycles) {
    this(executionSequence, runAsFastAsPossible, liveDataTriggerEnabled, maxSuccessiveDeltaCycles, false);
  }
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, boolean runAsFastAsPossible,
      boolean liveDataTriggerEnabled, Integer maxSuccessiveDeltaCycles, boolean compileOnly) {
    this(executionSequence, runAsFastAsPossible, liveDataTriggerEnabled, maxSuccessiveDeltaCycles, compileOnly, null);
  }
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, boolean runAsFastAsPossible,
      boolean liveDataTriggerEnabled, Integer maxSuccessiveDeltaCycles, boolean compileOnly, UniqueIdentifier marketDataSnapshotIdentifier) {
    ArgumentChecker.notNull(executionSequence, "executionSequence");
    
    _executionSequence = executionSequence;
    _runAsFastAsPossible = runAsFastAsPossible;
    _liveDataTriggerEnabled = liveDataTriggerEnabled;
    _maxSuccessiveDeltaCycles = maxSuccessiveDeltaCycles;
    _compileOnly = compileOnly;
    _marketDataSnapshotIdentifier = marketDataSnapshotIdentifier;
  }
  
  public static ViewExecutionOptions realTime() {
    return new ExecutionOptions(new RealTimeViewCycleExecutionSequence(), true);
  }
  
  public static ViewExecutionOptions likeRealTime(ViewCycleExecutionSequence cycleExecutionSequence) {
    return new ExecutionOptions(cycleExecutionSequence, true);
  }
  
  public static ViewExecutionOptions batch(ViewCycleExecutionSequence cycleExecutionSequence) {
    return new ExecutionOptions(
        cycleExecutionSequence,
        true,
        false,
        null,
        false);
  }
  
  public static ViewExecutionOptions singleCycle() {
    return snapshot(null);
  }

  public static ViewExecutionOptions singleCycle(long valuationTimeMillis) {
    return snapshot(null, valuationTimeMillis);
  }

  public static ViewExecutionOptions singleCycle(Instant valuationTime) {
    return snapshot(null, valuationTime);
  }
  
  public static ViewExecutionOptions snapshot(UniqueIdentifier snapshotIdentifier) {
    return snapshot(snapshotIdentifier, Instant.now());
  }
  
  public static ViewExecutionOptions snapshot(UniqueIdentifier snapshotIdentifier, long valuationTimeMillis) {
    return snapshot(snapshotIdentifier, Instant.ofEpochMillis(valuationTimeMillis));
  }
  
  public static ViewExecutionOptions snapshot(UniqueIdentifier snapshotIdentifier, Instant valuationTime) {
    return new ExecutionOptions(
        ArbitraryViewCycleExecutionSequence.of(valuationTime),
        true,
        false,
        null,
        false, snapshotIdentifier);
  }
  
  public static ViewExecutionOptions compileOnly() {
    return compileOnly(ArbitraryViewCycleExecutionSequence.of(Instant.now()));
  }
  
  public static ViewExecutionOptions compileOnly(ViewCycleExecutionSequence cycleExecutionSequence) {
    return new ExecutionOptions(
        cycleExecutionSequence,
        true,
        false,
        null,
        true);
  }
  
  @Override
  public ViewCycleExecutionSequence getExecutionSequence() {
    return _executionSequence;
  }

  @Override
  public boolean isRunAsFastAsPossible() {
    return _runAsFastAsPossible;
  }

  @Override
  public boolean isLiveDataTriggerEnabled() {
    return _liveDataTriggerEnabled;
  }

  @Override
  public Integer getMaxSuccessiveDeltaCycles() {
    return _maxSuccessiveDeltaCycles;
  }
  
  @Override
  public boolean isCompileOnly() {
    return _compileOnly;
  }
  

  @Override
  public UniqueIdentifier getMarketDataSnapshotIdentifier() {
    return _marketDataSnapshotIdentifier;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _executionSequence.hashCode();
    result = prime * result + (_liveDataTriggerEnabled ? 1231 : 1237);
    result = prime * result + ((_maxSuccessiveDeltaCycles == null) ? 0 : _maxSuccessiveDeltaCycles.hashCode());
    result = prime * result + (_runAsFastAsPossible ? 1231 : 1237);
    result = prime * result + (_compileOnly ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ExecutionOptions)) {
      return false;
    }
    ExecutionOptions other = (ExecutionOptions) obj;
    if (!_executionSequence.equals(other._executionSequence)) {
      return false;
    }
    if (_liveDataTriggerEnabled != other._liveDataTriggerEnabled) {
      return false;
    }
    if (_maxSuccessiveDeltaCycles == null) {
      if (other._maxSuccessiveDeltaCycles != null) {
        return false;
      }
    } else if (!_maxSuccessiveDeltaCycles.equals(other._maxSuccessiveDeltaCycles)) {
      return false;
    }
    if (_runAsFastAsPossible != other._runAsFastAsPossible) {
      return false;
    }
    if (_compileOnly != other._compileOnly) {
      return false;
    }
    if (_marketDataSnapshotIdentifier == null) {
      if (other._marketDataSnapshotIdentifier != null) {
        return false;
      }
    } else if (!_marketDataSnapshotIdentifier.equals(other._marketDataSnapshotIdentifier)) {
      return false;
    }
    return true;
  }


}
