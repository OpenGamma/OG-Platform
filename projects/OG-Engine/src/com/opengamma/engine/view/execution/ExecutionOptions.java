/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.EnumSet;

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
  private final EnumSet<ViewExecutionFlags> _flags;
  private final Integer _maxSuccessiveDeltaCycles;
  private final UniqueIdentifier _marketDataSnapshotIdentifier;
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags) {
    this(executionSequence, flags, null, null);
  }
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags, Integer maxSuccessiveDeltaCycles) {
    this(executionSequence, flags, maxSuccessiveDeltaCycles, null);
  }
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags, UniqueIdentifier marketDataSnapshotId) {
    this(executionSequence, flags, null, marketDataSnapshotId);
  }
    
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags,
      Integer maxSuccessiveDeltaCycles, UniqueIdentifier marketDataSnapshotIdentifier) {
    ArgumentChecker.notNull(executionSequence, "executionSequence");
    ArgumentChecker.notNull(flags, "flags");
    
    _executionSequence = executionSequence;
    _flags = flags;
    _maxSuccessiveDeltaCycles = maxSuccessiveDeltaCycles;
    _marketDataSnapshotIdentifier = marketDataSnapshotIdentifier;
  }
  
  public static ViewExecutionOptions realTime() {
    return new ExecutionOptions(new RealTimeViewCycleExecutionSequence(), ExecutionFlags.triggersEnabled().get());
  }
  
  public static ViewExecutionOptions likeRealTime(ViewCycleExecutionSequence cycleExecutionSequence) {
    return new ExecutionOptions(cycleExecutionSequence, ExecutionFlags.triggersEnabled().get());
  }
  
  public static ViewExecutionOptions batch(ViewCycleExecutionSequence cycleExecutionSequence) {
    return new ExecutionOptions(cycleExecutionSequence, ExecutionFlags.none().runAsFastAsPossible().get());
  }

  public static ViewExecutionOptions singleCycle() {
    return singleCycle(Instant.now());
  }
  
  public static ViewExecutionOptions singleCycle(long valuationTimeMillis) {
    return singleCycle(Instant.ofEpochMillis(valuationTimeMillis));
  }
  
  public static ViewExecutionOptions singleCycle(Instant valuationTime) {
    return new ExecutionOptions(ArbitraryViewCycleExecutionSequence.of(valuationTime), ExecutionFlags.none().runAsFastAsPossible().get());
  }
  
  /**
   * Creates execution options for running using a snapshot against the current time. Execution will never complete,
   * allowing changes to the snapshot or changes due to time passing to trigger a further cycle.
   * 
   * @param snapshotIdentifier  the identifier of the snapshot, not {@code null}
   * @return the execution options, not {@code null}
   */
  public static ViewExecutionOptions snapshot(UniqueIdentifier snapshotIdentifier) {
    return new ExecutionOptions(new RealTimeViewCycleExecutionSequence(), ExecutionFlags.triggersEnabled().get(), snapshotIdentifier);
  }
  
  /**
   * Creates execution options for running using a snapshot, with a fixed valuation time. Execution will never
   * complete, allowing changes to the snapshot data to trigger a further cycle for this valuation time.
   * 
   * @param snapshotIdentifier  the identifier of the snapshot, not {@code null}
   * @param valuationTime  the fixed valuation time, not {@code null}
   * @return the execution options, not {@code null}
   */
  public static ViewExecutionOptions snapshot(UniqueIdentifier snapshotIdentifier, Instant valuationTime) {
    return new ExecutionOptions(ArbitraryViewCycleExecutionSequence.of(valuationTime), ExecutionFlags.none().triggerOnLiveData().get(), snapshotIdentifier);
  }
  
  public static ViewExecutionOptions compileOnly() {
    return compileOnly(ArbitraryViewCycleExecutionSequence.of(Instant.now()));
  }
  
  public static ViewExecutionOptions compileOnly(ViewCycleExecutionSequence cycleExecutionSequence) {
    return new ExecutionOptions(cycleExecutionSequence, ExecutionFlags.none().compileOnly().get());
  }
  
  @Override
  public ViewCycleExecutionSequence getExecutionSequence() {
    return _executionSequence;
  }

  @Override
  public Integer getMaxSuccessiveDeltaCycles() {
    return _maxSuccessiveDeltaCycles;
  }

  @Override
  public UniqueIdentifier getMarketDataSnapshotIdentifier() {
    return _marketDataSnapshotIdentifier;
  }
  
  @Override
  public EnumSet<ViewExecutionFlags> getFlags() {
    return _flags;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_executionSequence == null) ? 0 : _executionSequence.hashCode());
    result = prime * result + ((_flags == null) ? 0 : _flags.hashCode());
    result = prime * result + ((_marketDataSnapshotIdentifier == null) ? 0 : _marketDataSnapshotIdentifier.hashCode());
    result = prime * result + ((_maxSuccessiveDeltaCycles == null) ? 0 : _maxSuccessiveDeltaCycles.hashCode());
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
    if (!_flags.equals(other._flags)) {
      return false;
    }
    if (_marketDataSnapshotIdentifier == null) {
      if (other._marketDataSnapshotIdentifier != null) {
        return false;
      }
    } else if (!_marketDataSnapshotIdentifier.equals(other._marketDataSnapshotIdentifier)) {
      return false;
    }
    if (_maxSuccessiveDeltaCycles == null) {
      if (other._maxSuccessiveDeltaCycles != null) {
        return false;
      }
    } else if (!_maxSuccessiveDeltaCycles.equals(other._maxSuccessiveDeltaCycles)) {
      return false;
    }
    return true;
  }

}
