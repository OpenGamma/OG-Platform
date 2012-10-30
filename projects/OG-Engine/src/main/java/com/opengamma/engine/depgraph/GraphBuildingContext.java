/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetResolverUtils;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * Algorithm state. A context object is used by a single job thread. The root context is not used by any builder thread. The synchronization on the collation methods only is therefore sufficient.
 */
/* package */final class GraphBuildingContext {

  private static final Logger s_logger = LoggerFactory.getLogger(GraphBuildingContext.class);

  private static final int MAX_CALLBACK_DEPTH = 16;

  private final DependencyGraphBuilder _builder;
  private Map<ExceptionWrapper, ExceptionWrapper> _exceptions;
  private int _stackDepth;

  public GraphBuildingContext(final DependencyGraphBuilder builder) {
    _builder = builder;
  }

  private DependencyGraphBuilder getBuilder() {
    return _builder;
  }

  // Configuration & resources

  public MarketDataAvailabilityProvider getMarketDataAvailabilityProvider() {
    return getBuilder().getMarketDataAvailabilityProvider();
  }

  public CompiledFunctionResolver getFunctionResolver() {
    return getBuilder().getFunctionResolver();
  }

  public FunctionCompilationContext getCompilationContext() {
    return getBuilder().getCompilationContext();
  }

  public FunctionExclusionGroups getFunctionExclusionGroups() {
    return getBuilder().getFunctionExclusionGroups();
  }

  // Operations

  /**
   * Schedule the task for execution.
   * 
   * @param runnable task to execute, not null
   */
  public void run(final ResolveTask runnable) {
    runnable.addRef();
    getBuilder().addToRunQueue(runnable);
  }

  /**
   * Trigger an underlying pump operation. This may happen before returning or be deferred if the stack is past a depth threshold.
   * 
   * @param pump underlying operation
   */
  public void pump(final ResolutionPump pump) {
    s_logger.debug("Pumping {}", pump);
    if (++_stackDepth > MAX_CALLBACK_DEPTH) {
      getBuilder().addToRunQueue(new ResolutionPump.Pump(pump));
    } else {
      pump.pump(this);
    }
    _stackDepth--;
  }

  /**
   * Trigger an underlying close operation. This may happen before returning or be deferred if the stack is past a depth threshold.
   * 
   * @param pump underlying operation
   */
  public void close(final ResolutionPump pump) {
    s_logger.debug("Closing {}", pump);
    if (++_stackDepth > MAX_CALLBACK_DEPTH) {
      getBuilder().addToRunQueue(new ResolutionPump.Close(pump));
    } else {
      pump.close(this);
    }
    _stackDepth--;
  }

  /**
   * Trigger a resolved callback.
   * 
   * @param callback callback object
   * @param valueRequirement requirement resolved
   * @param resolvedValue value resolved to
   * @param pump source of the next value
   */
  public void resolved(final ResolvedValueCallback callback, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    s_logger.debug("Resolved {} to {}", valueRequirement, resolvedValue);
    _stackDepth++;
    // Scheduling failure and resolved callbacks from the run queue is a real headache to debug, so always call them inline 
    callback.resolved(this, valueRequirement, resolvedValue, pump);
    _stackDepth--;
  }

  /**
   * Trigger a resolution failure.
   * 
   * @param callback callback object
   * @param valueRequirement requirement that failed to resolve or for which there are no further resolutions
   * @param failure description of the failure
   */
  public void failed(final ResolvedValueCallback callback, final ValueRequirement valueRequirement, final ResolutionFailure failure) {
    s_logger.debug("Couldn't resolve {}", valueRequirement);
    _stackDepth++;
    // Scheduling failure and resolved callbacks from the run queue is a real headache to debug, so always call them inline 
    callback.failed(this, valueRequirement, failure);
    _stackDepth--;
  }

  /**
   * Stores an exception that should be reported to the user. Only store the first copy of an exception; after that increment the count of times that it occurred.
   * 
   * @param t exception to store, not null
   */
  public void exception(final Throwable t) {
    s_logger.debug("Caught exception", t);
    if (_exceptions == null) {
      _exceptions = new HashMap<ExceptionWrapper, ExceptionWrapper>();
    }
    ExceptionWrapper.createAndPut(t, _exceptions);
  }

  public ResolvedValueProducer resolveRequirement(final ValueRequirement rawRequirement, final ResolveTask dependent, final Set<FunctionExclusionGroup> functionExclusion) {
    final ValueRequirement requirement = simplifyType(rawRequirement);
    s_logger.debug("Resolve requirement {}", requirement);
    if ((dependent != null) && dependent.hasParent(requirement)) {
      dependent.setRecursionDetected();
      s_logger.debug("Can't introduce a ValueRequirement loop");
      return new NullResolvedValueProducer(requirement, recursiveRequirement(requirement));
    }
    RequirementResolver resolver = null;
    final ResolveTask[] tasks = getTasksResolving(requirement);
    if (tasks != null) {
      for (ResolveTask task : tasks) {
        if ((dependent == null) || !dependent.hasParent(task)) {
          if (resolver == null) {
            resolver = new RequirementResolver(requirement, dependent, functionExclusion);
          }
          resolver.addTask(this, task);
        }
        task.release(this);
      }
    }
    if (resolver != null) {
      resolver.start(this);
      return resolver;
    } else {
      s_logger.debug("Using direct resolution {}/{}", requirement, dependent);
      return getOrCreateTaskResolving(requirement, dependent, functionExclusion);
    }
  }

  public ResolveTask getOrCreateTaskResolving(final ValueRequirement valueRequirement, final ResolveTask parentTask, final Set<FunctionExclusionGroup> functionExclusion) {
    final ResolveTask newTask = new ResolveTask(valueRequirement, parentTask, functionExclusion);
    do {
      ResolveTask task;
      final Map<ResolveTask, ResolveTask> tasks = getBuilder().getOrCreateTasks(valueRequirement);
      synchronized (tasks) {
        if (tasks.containsKey(null)) {
          // The cache has been flushed
          continue;
        }
        task = tasks.get(newTask);
        if (task == null) {
          newTask.addRef();
          tasks.put(newTask, newTask);
        } else {
          task.addRef();
        }
      }
      if (task != null) {
        s_logger.debug("Using existing task {}", task);
        newTask.release(this);
        return task;
      } else {
        run(newTask);
        getBuilder().incrementActiveResolveTasks();
        return newTask;
      }
    } while (true);
  }

  private ResolveTask[] getTasksResolving(final ValueRequirement valueRequirement) {
    do {
      final ResolveTask[] result;
      final Map<ResolveTask, ResolveTask> tasks = getBuilder().getTasks(valueRequirement);
      if (tasks == null) {
        return null;
      }
      synchronized (tasks) {
        if (tasks.containsKey(null)) {
          // The cache has been flushed
          continue;
        }
        result = new ResolveTask[tasks.size()];
        int i = 0;
        for (ResolveTask task : tasks.keySet()) {
          result[i++] = task;
          task.addRef();
        }
      }
      return result;
    } while (true);
  }

  @SuppressWarnings("unchecked")
  public Pair<ResolveTask[], ResolvedValueProducer[]> getTasksProducing(final ValueSpecification valueSpecification) {
    do {
      final MapEx<ResolveTask, ResolvedValueProducer> tasks = getBuilder().getTasks(valueSpecification);
      if (tasks != null) {
        final ResolveTask[] resultTasks;
        final ResolvedValueProducer[] resultProducers;
        synchronized (tasks) {
          if (tasks.containsKey(null)) {
            continue;
          }
          if (tasks.isEmpty()) {
            return null;
          }
          resultTasks = new ResolveTask[tasks.size()];
          resultProducers = new ResolvedValueProducer[tasks.size()];
          int i = 0;
          for (Map.Entry<ResolveTask, ResolvedValueProducer> task : (Set<Map.Entry<ResolveTask, ResolvedValueProducer>>) tasks.entrySet()) {
            // Don't ref-count the tasks; they're just used for parent comparisons
            resultTasks[i] = task.getKey();
            resultProducers[i++] = task.getValue();
            task.getValue().addRef();
          }
        }
        return Pair.of(resultTasks, resultProducers);
      } else {
        return null;
      }
    } while (true);
  }

  public ResolvedValue getProduction(final ValueSpecification valueSpecification) {
    return getBuilder().getResolvedValue(valueSpecification);
  }

  public void discardTask(final ResolveTask task) {
    do {
      final Map<ResolveTask, ResolveTask> tasks = getBuilder().getTasks(task.getValueRequirement());
      if (tasks == null) {
        return;
      }
      synchronized (tasks) {
        if (tasks.containsKey(null)) {
          continue;
        }
        final ResolveTask removed = tasks.remove(task);
        if (removed == null) {
          // Task has already been discarded
          return;
        }
        if (removed != task) {
          // Task has already been discarded and replaced by an equivalent; don't discard that
          tasks.put(removed, removed);
          return;
        }
      }
      task.release(this);
      getBuilder().decrementActiveResolveTasks();
    } while (true);
  }

  public ResolvedValueProducer declareTaskProducing(final ValueSpecification valueSpecification, final ResolveTask task, final ResolvedValueProducer producer) {
    do {
      final MapEx<ResolveTask, ResolvedValueProducer> tasks = getBuilder().getOrCreateTasks(valueSpecification);
      ResolvedValueProducer result = null;
      if (tasks != null) {
        ResolvedValueProducer discard = null;
        synchronized (tasks) {
          if (!tasks.isEmpty()) {
            if (tasks.containsKey(null)) {
              continue;
            }
            final Map.Entry<ResolveTask, ResolvedValueProducer> resolveTask = tasks.getHashEntry(task);
            if (resolveTask != null) {
              if (resolveTask.getKey() == task) {
                // Replace an earlier attempt from this task with the new producer
                discard = resolveTask.getValue();
                producer.addRef();
                resolveTask.setValue(producer);
                result = producer;
              } else {
                // An equivalent task is doing the work
                result = resolveTask.getValue();
              }
              result.addRef();
            }
          }
          if (result == null) {
            final ResolvedValue value = getBuilder().getResolvedValue(valueSpecification);
            if (value != null) {
              result = new SingleResolvedValueProducer(task.getValueRequirement(), value);
            }
          }
          if (result == null) {
            // No matching tasks
            producer.addRef();
            tasks.put(task, producer);
            result = producer;
            result.addRef();
          }
        }
        if (discard != null) {
          discard.release(this);
        }
      } else {
        final ResolvedValue value = getBuilder().getResolvedValue(valueSpecification);
        if (value != null) {
          result = new SingleResolvedValueProducer(task.getValueRequirement(), value);
        }
      }
      return result;
    } while (true);
  }

  public void discardTaskProducing(final ValueSpecification valueSpecification, final ResolveTask task) {
    do {
      final MapEx<ResolveTask, ResolvedValueProducer> tasks = getBuilder().getTasks(valueSpecification);
      if (tasks != null) {
        final ResolvedValueProducer producer;
        synchronized (tasks) {
          if (tasks.containsKey(null)) {
            continue;
          }
          producer = (ResolvedValueProducer) tasks.remove(task);
          if (producer == null) {
            // Wasn't in the set
            return;
          }
        }
        producer.release(this);
      }
      return;
    } while (true);
  }

  public void declareProduction(final ResolvedValue resolvedValue) {
    getBuilder().addResolvedValue(resolvedValue);
  }

  public ComputationTargetSpecification resolveTargetReference(final ComputationTargetReference reference) {
    return getBuilder().resolveTargetReference(reference);
  }

  /**
   * Simplifies the type based on the associated {@link ComputationTargetResolver}.
   * 
   * @param valueSpec the specification to process, not null
   * @return the possibly simplified specification, not null
   */
  public ValueSpecification simplifyType(final ValueSpecification valueSpec) {
    final ComputationTargetSpecification oldTargetSpec = valueSpec.getTargetSpecification();
    final ComputationTargetSpecification newTargetSpec = ComputationTargetResolverUtils.simplifyType(oldTargetSpec, getCompilationContext().getComputationTargetResolver());
    if (newTargetSpec == oldTargetSpec) {
      return valueSpec;
    } else {
      return MemoryUtils.instance(new ValueSpecification(valueSpec.getValueName(), newTargetSpec, valueSpec.getProperties()));
    }
  }

  /**
   * Simplifies the type based on the associated {@link ComputationTargetResolver}.
   * 
   * @param valueReq the requirement to process, not null
   * @return the possibly simplified requirement, not null
   */
  public ValueRequirement simplifyType(final ValueRequirement valueReq) {
    final ComputationTargetReference oldTargetRef = valueReq.getTargetReference();
    final ComputationTargetReference newTargetRef = ComputationTargetResolverUtils.simplifyType(oldTargetRef, getCompilationContext().getComputationTargetResolver());
    if (newTargetRef == oldTargetRef) {
      return valueReq;
    } else {
      return MemoryUtils.instance(new ValueRequirement(valueReq.getValueName(), newTargetRef, valueReq.getConstraints()));
    }
  }

  // Failure reporting

  public ResolutionFailure recursiveRequirement(final ValueRequirement valueRequirement) {
    if (getBuilder().isDisableFailureReporting()) {
      return NullResolutionFailure.INSTANCE;
    } else {
      return ResolutionFailureImpl.recursiveRequirement(valueRequirement);
    }
  }

  public ResolutionFailure functionApplication(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification outputSpecification) {
    if (getBuilder().isDisableFailureReporting()) {
      return NullResolutionFailure.INSTANCE;
    } else {
      return ResolutionFailureImpl.functionApplication(valueRequirement, function, outputSpecification);
    }
  }

  public ResolutionFailure noFunctions(final ValueRequirement valueRequirement) {
    if (getBuilder().isDisableFailureReporting()) {
      return NullResolutionFailure.INSTANCE;
    } else {
      return ResolutionFailureImpl.noFunctions(valueRequirement);
    }
  }

  public ResolutionFailure couldNotResolve(final ValueRequirement valueRequirement) {
    if (getBuilder().isDisableFailureReporting()) {
      return NullResolutionFailure.INSTANCE;
    } else {
      return ResolutionFailureImpl.couldNotResolve(valueRequirement);
    }
  }

  public ResolutionFailure unsatisfied(final ValueRequirement valueRequirement) {
    if (getBuilder().isDisableFailureReporting()) {
      return NullResolutionFailure.INSTANCE;
    } else {
      return ResolutionFailureImpl.unsatisfied(valueRequirement);
    }
  }

  public ResolutionFailure marketDataMissing(final ValueRequirement valueRequirement) {
    if (getBuilder().isDisableFailureReporting()) {
      return NullResolutionFailure.INSTANCE;
    } else {
      return ResolutionFailureImpl.marketDataMissing(valueRequirement);
    }
  }

  // Collation

  /**
   * Merge information from the other context into this (a root context). The caller must be the thread that was working with the other context.
   * 
   * @param context the other context
   */
  public synchronized void mergeThreadContext(final GraphBuildingContext context) {
    if (_exceptions == null) {
      _exceptions = new HashMap<ExceptionWrapper, ExceptionWrapper>();
    }
    if (context._exceptions != null) {
      for (ExceptionWrapper exception : context._exceptions.keySet()) {
        final ExceptionWrapper existing = _exceptions.get(exception);
        if (existing != null) {
          existing.incrementCount(exception.getCount());
        } else {
          _exceptions.put(exception, exception);
        }
      }
    }
  }

  public synchronized Map<Throwable, Integer> getExceptions() {
    if (_exceptions == null) {
      return Collections.emptyMap();
    }
    final Map<Throwable, Integer> result = new HashMap<Throwable, Integer>();
    for (ExceptionWrapper exception : _exceptions.keySet()) {
      result.put(exception.getException(), exception.getCount());
    }
    return result;
  }

}
