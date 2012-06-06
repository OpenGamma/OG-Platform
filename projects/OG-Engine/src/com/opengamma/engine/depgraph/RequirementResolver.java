/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Resolves an individual requirement by aggregating the results of any existing tasks already resolving that requirement. If these missed an exploration because of a recursion constraint introduced
 * by their parent tasks, a "fallback" task is created to finish the job for the caller's parent.
 */
/* package */final class RequirementResolver extends AggregateResolvedValueProducer {

  private static final Logger s_logger = LoggerFactory.getLogger(RequirementResolver.class);

  private final ResolveTask _parentTask;
  private final Set<ResolveTask> _tasks = new HashSet<ResolveTask>();
  private ResolveTask _fallback;
  private ResolvedValue[] _coreResults;

  public RequirementResolver(final ValueRequirement valueRequirement, final ResolveTask parentTask) {
    super(valueRequirement);
    s_logger.debug("Created requirement resolver {}/{}", valueRequirement, parentTask);
    _parentTask = parentTask;
  }

  protected void addTask(final GraphBuildingContext context, final ResolveTask task) {
    if (_tasks.add(task)) {
      task.addRef();
      addProducer(context, task);
    }
  }

  private synchronized List<ResolveTask> takeTasks() {
    final List<ResolveTask> tasks = new ArrayList<ResolveTask>(_tasks);
    _tasks.clear();
    return tasks;
  }

  @Override
  protected void finished(final GraphBuildingContext context) {
    assert getPendingTasks() <= 1; // either the final pending task running with the "lastValue" flag, or all tasks have finished
    boolean useFallback = false;
    ResolveTask fallback;
    synchronized (this) {
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
      } else {
        _fallback = null;
      }
    }
    if ((fallback == null) && useFallback) {
      fallback = context.getOrCreateTaskResolving(getValueRequirement(), _parentTask, _parentTask.getFunctionExclusion());
      synchronized (this) {
        useFallback = _tasks.add(fallback);
      }
      if (useFallback) {
        fallback.addRef();
        s_logger.debug("Creating fallback task {}", fallback);
        synchronized (this) {
          assert _fallback == null;
          _fallback = fallback;
          _coreResults = getResults();
        }
        addProducer(context, fallback);
        return;
      } else {
        fallback.release(context);
        fallback = null;
      }
    }
    super.finished(context);
    if (fallback != null) {
      // Keep any fallback tasks that are recursion free - to prevent future fallbacks for this requirement
      if (fallback.wasRecursionDetected()) {
        final ResolvedValue[] fallbackResults = getResults();
        if (fallbackResults.length == 0) {
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
    // Release any other tasks
    for (ResolveTask task : takeTasks()) {
      task.release(context);
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
      }
      if (fallback != null) {
        // Discard the fallback task
        s_logger.debug("Discarding unfinished fallback task {} by {}", fallback, this);
        context.discardTask(fallback);
        fallback.release(context);
      }
      // Release any other tasks
      for (ResolveTask task : takeTasks()) {
        task.release(context);
      }
    }
    return count;
  }

}
