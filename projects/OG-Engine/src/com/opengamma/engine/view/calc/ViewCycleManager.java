/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.engine.view.ViewProcessContext;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A resource manager for computation cycles.
 */
public class ViewCycleManager {
  
  private final ConcurrentMap<UniqueIdentifier, ReferenceCountedComputationCycle> _cycleMap = new ConcurrentHashMap<UniqueIdentifier, ReferenceCountedComputationCycle>();
  
  /**
   * Creates a new view cycle, initially with a single reference to it which is returned.
   * 
   * @param cycleId  the unique identifier of the new cycle, not null
   * @param processId  the unique identifier of the view process, not null
   * @param processContext  the view process context, not null
   * @param evaluationModel  the view evaluation model, not null
   * @param executionOptions  the view cycle execution options, not null
   * @return a reference wrapper to the computation cycle, not null
   * @throws IllegalArgumentException  if a cycle with the same ID is already under management
   */
  public ViewCycleReferenceImpl createViewCycle(UniqueIdentifier cycleId, UniqueIdentifier processId,
      ViewProcessContext processContext, ViewEvaluationModel evaluationModel, ViewCycleExecutionOptions executionOptions) {
    ArgumentChecker.notNull(cycleId, "cycleId");
    ArgumentChecker.notNull(processId, "processId");
    ArgumentChecker.notNull(processContext, "processContext");
    ArgumentChecker.notNull(evaluationModel, "evaluationModel");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    SingleComputationCycle cycle = new SingleComputationCycle(cycleId, processId, processContext, evaluationModel, executionOptions);
    ReferenceCountedComputationCycle refCountedCycle = new ReferenceCountedComputationCycle(cycle);
    if (_cycleMap.put(cycleId, refCountedCycle) != null) {
      throw new IllegalArgumentException("A cycle with ID " + cycleId + " is already being managed");
    }
    return new ViewCycleReferenceImpl(this, cycle);
  }
  
  /**
   * Increments the reference count for a computation cycle which ensures it is retained. A call to this method must be
   * paired with a subsequent call to {@link #decrementCycleReferenceCount(UniqueIdentifier)} to avoid resource leaks. 
   * 
   * @param cycleId  the unique identifier of the cycle to retain, not null
   * @return {@code true} if the operation was successful, {@code false} if the cycle was not found
   */
  public boolean incrementCycleReferenceCount(UniqueIdentifier cycleId) {
    return incrementCycleReferenceCountCore(cycleId) != null;
  }
  
  private SingleComputationCycle incrementCycleReferenceCountCore(UniqueIdentifier cycleId) {
    ArgumentChecker.notNull(cycleId, "cycleId");
    ReferenceCountedComputationCycle refCountedCycle = _cycleMap.get(cycleId);
     
    if (refCountedCycle == null) {
      return null;
    }
    
    synchronized (refCountedCycle) {
      if (refCountedCycle.getReferenceCount() > 0) {
        refCountedCycle.incrementReferenceCount();
      } else {
        // Concurrently being released, so to the external user it's already been deleted
        throw new IllegalArgumentException("No cycle with ID " + cycleId + " could be found");
      }
    }
    
    return refCountedCycle.getCycle();
  }
  
  /**
   * Decrements the reference count for a computation cycle which may allow it to be released. A call to this method
   * must only follow a previous call to {@link #incrementCycleReferenceCount(UniqueIdentifier)}.
   * 
   * @param cycleId  the uniqueIdentifier of the cycle to release, not null
   * @return {@code true} if the operation was successful, {@code false} if the cycle was not found
   */
  public boolean decrementCycleReferenceCount(UniqueIdentifier cycleId) {
    ArgumentChecker.notNull(cycleId, "cycleId");
    ReferenceCountedComputationCycle refCountedCycle = _cycleMap.get(cycleId);
    
    if (refCountedCycle == null) {
      return false;
    }
    
    synchronized (refCountedCycle) {
      if (refCountedCycle.decrementReferenceCount() == 0) {
        _cycleMap.remove(cycleId);
        refCountedCycle.getCycle().releaseResources();
      }
    }
    return true;
  }
  
  /**
   * Constructs a reference wrapper to a view cycle, incrementing the reference count for that cycle. The reference
   * wrapper facilitates decrementing the reference count through the method {@link ViewCycleReference#release()}.
   * 
   * @param cycleId  the unique identifier of the view cycle for which a reference is required, not null
   * @return a reference wrapper to the view cycle, or {@code null} if the cycle was not found
   */
  public ViewCycleReference createReference(UniqueIdentifier cycleId) {
    ArgumentChecker.notNull(cycleId, "cycleId");
    SingleComputationCycle cycle = incrementCycleReferenceCountCore(cycleId);
    return cycle == null ? null : new ViewCycleReferenceImpl(this, cycle);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Holds reference counting state for a computation cycle. Intentionally not thread-safe, so requires external
   * synchronisation.
   */
  private static class ReferenceCountedComputationCycle {
    
    private final SingleComputationCycle _cycle;
    private long _refCount = 1;
    
    public ReferenceCountedComputationCycle(SingleComputationCycle cycle) {
      _cycle = cycle;
    }
    
    public SingleComputationCycle getCycle() {
      return _cycle;
    }
    
    public long getReferenceCount() {
      return _refCount;
    }
    
    public long incrementReferenceCount() {
      return ++_refCount;
    }
    
    public long decrementReferenceCount() {
      return --_refCount;
    }
    
  }
  
}
