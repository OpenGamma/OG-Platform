/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Resolves an individual requirement by aggregating the results of any existing tasks already resolving that requirement. If these missed an exploration because of a recursion constraint introduced
 * by their parent tasks, a "fallback" task is created to finish the job for the caller's parent.
 */
/* package */final class RequirementResolver extends AggregateResolvedValueProducer {

  private static final Logger s_logger = LoggerFactory.getLogger(RequirementResolver.class);

  private final ResolveTask _parentTask;
  private final Collection<FunctionExclusionGroup> _functionExclusion;
  // _tasks is just used to spot the recursionDetected flag - not ref-Counted
  private ResolveTask[] _tasks;
  private ResolveTask _fallback;
  private ResolvedValue[] _coreResults;

  public RequirementResolver(final ValueRequirement valueRequirement, final ResolveTask parentTask, final Collection<FunctionExclusionGroup> functionExclusion) {
    super(valueRequirement);
    s_logger.debug("Created requirement resolver {}/{}", valueRequirement, parentTask);
    _parentTask = parentTask;
    _functionExclusion = functionExclusion;
  }

  public void setTasks(final GraphBuildingContext context, final ResolveTask[] tasks) {
    _tasks = tasks;
    for (ResolveTask task : tasks) {
      addProducer(context, task);
    }
  }

  @Override
  protected boolean isLastResult() {
    // Caller already holds monitor - see javadoc
    if (_fallback == null) {
      for (ResolveTask task : _tasks) {
        if (!task.wasRecursionDetected()) {
          // One ran to completion without hitting a recursion constraint so no fallback task will be created - this is the last result
          return true;
        }
      }
      // A fallback task may be created during the finished call, so suppress the last result indicator for now
      return false;
    } else {
      // The fallback task is active so let its last result carry through
      return true;
    }
  }

  @Override
  protected void finished(final GraphBuildingContext context) {
    boolean useFallback = false;
    ResolveTask fallback;
    synchronized (this) {
      final int pendingTasks = getPendingTasks();
      if (pendingTasks == Integer.MIN_VALUE) {
        // We've already been discarded (everything was released when we went to rc=0)
        s_logger.debug("Ignoring finish on discarded {}", this);
        return;
      }
      assert (pendingTasks == 0) || (pendingTasks == 1); // Either the final pending task running with the "lastValue" flag, or all tasks have finished
      fallback = _fallback;
      if (fallback == null) {
        // Only create a fallback if none of the others ran to completion without hitting a recursion constraint.
        useFallback = true;
        for (ResolveTask task : _tasks) {
          if (!task.wasRecursionDetected()) {
            useFallback = false;
            break;
          }
        }
        _tasks = null;
      } else {
        // local variable takes open reference from _fallback
        _fallback = null;
      }
    }
    if ((fallback == null) && useFallback) {
      fallback = context.getOrCreateTaskResolving(getValueRequirement(), _parentTask, _functionExclusion);
      s_logger.debug("Creating fallback task {}", fallback);
      synchronized (this) {
        assert _fallback == null;
        // _fallback takes the open reference from the local variable
        _fallback = fallback;
        _coreResults = getResults();
      }
      addProducer(context, fallback);
      return;
    }
    super.finished(context);
    if (fallback != null) {
      // Keep any fallback tasks that are recursion free - to prevent future fallbacks for this requirement
      if (fallback.wasRecursionDetected()) {
        final ResolvedValue[] fallbackResults = getResults();
        // If this resolver was ref-counted to zero (nothing subscribed to it) then the results can be null at this point
        if ((fallbackResults == null) || (fallbackResults.length == 0)) {
          // Task produced no new results - discard
          s_logger.debug("Discarding fallback task {} by {}", fallback, this);
          context.discardTask(fallback);
        } else {
          boolean matched = true;
          synchronized (this) {
            for (int i = 0; i < fallbackResults.length; i++) {
              boolean found = false;
              for (int j = 0; j < _coreResults.length; j++) {
                if (fallbackResults[i].equals(_coreResults[j])) {
                  found = true;
                  break;
                }
              }
              if (!found) {
                matched = false;
                break;
              }
            }
          }
          if (matched) {
            // Task didn't produce any new results - discard
            context.discardTask(fallback);
          }
        }
      } else {
        s_logger.debug("Keeping fallback task {} by {}", fallback, this);
      }
      fallback.release(context);
    }
  }

  @Override
  public String toString() {
    return "Resolve" + getObjectId() + "[" + getValueRequirement() + ", " + _parentTask + "]";
  }

  @Override
  public int release(final GraphBuildingContext context) {
    final int count = super.release(context);
    if (count == 0) {
      final ResolveTask fallback;
      synchronized (this) {
        fallback = _fallback;
        _fallback = null;
        _tasks = null;
      }
      if (fallback != null) {
        // Release/discard the fallback task
        fallback.release(context);
      }
    }
    return count;
  }

}
