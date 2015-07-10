/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetResolverUtils;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

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

  public CompiledFunctionDefinition getFunctionDefinition(final String functionId) {
    return getBuilder().getFunctionResolver().getFunction(functionId);
  }

  // Operations

  public void submit(final ContextRunnable runnable) {
    getBuilder().addToRunQueue(runnable);
  }

  /**
   * Schedule the task for execution.
   * 
   * @param runnable task to execute, not null
   */
  public void run(final ResolveTask runnable) {
    // Run inline unless the stack is full, or the task attempts to defer execution. Only run if the task hasn't been discarded (ie
    // no-one is going to consume its results)
    if (runnable.addRef()) {
      // Added a reference for the run-queue (which will be removed by tryRun)
      if ((++_stackDepth > MAX_CALLBACK_DEPTH) || !runnable.tryRun(this)) {
        submit(runnable);
      }
      _stackDepth--;
    }
  }

  /**
   * Trigger an underlying pump operation. This may happen before returning or be deferred if the stack is past a depth threshold.
   * 
   * @param pump underlying operation
   */
  public void pump(final ResolutionPump pump) {
    s_logger.debug("Pumping {}", pump);
    if (++_stackDepth > MAX_CALLBACK_DEPTH) {
      submit(new ResolutionPump.Pump(pump));
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
      submit(new ResolutionPump.Close(pump));
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

  public ResolvedValueProducer resolveRequirement(final ValueRequirement rawRequirement, final ResolveTask dependent, final Collection<FunctionExclusionGroup> functionExclusion) {
    final ValueRequirement requirement = simplifyType(rawRequirement);
    s_logger.debug("Resolve requirement {}", requirement);
    if ((dependent != null) && dependent.hasParent(requirement)) {
      s_logger.debug("Can't introduce a ValueRequirement loop");
      return null;
    }
    RequirementResolver resolver = null;
    final ResolveTask[] tasks = getTasksResolving(requirement);
    if (tasks != null) {
      int i = 0;
      int l = tasks.length;
      while (i < l) {
        final ResolveTask task = tasks[i];
        if ((dependent == null) || !dependent.hasParent(task)) {
          if ((task.isFinished() && !task.wasRecursionDetected()) || (ObjectUtils.equals(functionExclusion, task.getFunctionExclusion()) && task.hasParentValueRequirements(dependent))) {
            // The task we've found has either already completed, without hitting a recursion constraint. Or
            // the task is identical to the fallback task we'd create naturally. In either case, release everything
            // else and use it.
            for (int j = 0; j < i; j++) {
              tasks[j].release(this);
            }
            for (int j = i + 1; j < l; j++) {
              tasks[j].release(this);
            }
            return task;
          }
          i++;
        } else {
          task.release(this);
          tasks[i] = tasks[--l];
        }
      }
      // Anything left in the array is suitable for use in a RequirementResolver
      if (l > 0) {
        resolver = new RequirementResolver(requirement, dependent, functionExclusion);
        if (l != tasks.length) {
          resolver.setTasks(this, Arrays.copyOf(tasks, l));
        } else {
          resolver.setTasks(this, tasks);
        }
        for (i = 0; i < l; i++) {
          tasks[i].release(this);
        }
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

  public ResolveTask getOrCreateTaskResolving(final ValueRequirement valueRequirement, final ResolveTask parentTask, final Collection<FunctionExclusionGroup> functionExclusion) {
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
          newTask.addRef(); // Already got a reference, increment for the collection
          tasks.put(newTask, newTask);
        } else {
          task.addRef(); // Got the task lock, increment so we can return it
        }
      }
      if (task != null) {
        s_logger.debug("Using existing task {}", task);
        newTask.release(this); // Discard local allocation
        return task;
      } else {
        getBuilder().incrementActiveResolveTasks();
        // Don't call run; we want to fork this out to a new worker thread, never call inline
        newTask.addRef(); // Reference held by the run queue
        submit(newTask);
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
        for (final ResolveTask task : tasks.keySet()) {
          result[i++] = task;
          task.addRef(); // Got the task lock
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
          for (final Map.Entry<ResolveTask, ResolvedValueProducer> task : (Set<Map.Entry<ResolveTask, ResolvedValueProducer>>) tasks.entrySet()) {
            // Don't ref-count the tasks; they're just used for parent comparisons
            resultTasks[i] = task.getKey();
            resultProducers[i++] = task.getValue();
            task.getValue().addRef(); // We're holding the task lock
          }
        }
        return Pairs.of(resultTasks, resultProducers);
      } else {
        return null;
      }
    } while (true);
  }

  /**
   * Fetches an existing resolution of the given value specification.
   * <p>
   * The {@code valueSpecification} parameter must be normalized.
   * 
   * @param valueSpecification the specification to search for, not null
   * @return the resolved value, or null if not resolved
   */
  public ResolvedValue getProduction(final ValueSpecification valueSpecification) {
    return getBuilder().getResolvedValue(valueSpecification);
  }

  public static final class ResolutionIterator {

    private final Object _properties;
    private final Object _functions;
    private final int _length;
    private int _index;

    private ResolutionIterator(final Pair<?, ?> values) {
      _properties = values.getFirst();
      _functions = values.getSecond();
      if (_properties instanceof ValueProperties) {
        _length = 1;
      } else {
        _length = ((ValueProperties[]) _properties).length;
      }
      _index = 0;
    }

    public boolean hasNext() {
      return ++_index < _length;
    }

    public ValueProperties getValueProperties() {
      if (_length == 1) {
        return (ValueProperties) _properties;
      } else {
        return ((ValueProperties[]) _properties)[_index];
      }
    }

    public ParameterizedFunction getFunction() {
      if (_length == 1) {
        return (ParameterizedFunction) _functions;
      } else {
        return ((ParameterizedFunction[]) _functions)[_index];
      }
    }

  }

  /**
   * Returns an iterator over previous resolutions (that are present in the dependency graph) on the same target digest for the same value name.
   * 
   * @param targetDigest the target's digest, not null
   * @param desiredValue the value requirement name, not null
   * @return any existing resolutions, null if there are none
   */
  public ResolutionIterator getResolutions(final ComputationTargetSpecification targetSpec, final String desiredValue) {
    Pair<?, ?> properties = getBuilder().getResolutions(targetSpec, desiredValue);
    if (properties == null) {
      return null;
    }
    return new ResolutionIterator(properties);
  }

  public void discardTask(final ResolveTask task) {
    // TODO: Could we post "discardTask" tasks to a queue and have them done in batches by a ContextRunnable?
    do {
      final Map<ResolveTask, ResolveTask> tasks = getBuilder().getTasks(task.getValueRequirement());
      if (tasks == null) {
        return;
      }
      synchronized (tasks) {
        if (tasks.containsKey(null)) {
          continue;
        }
        final int rc = task.getRefCount();
        if (rc == 0) {
          // Not referenced by us by definition
          return;
        }
        if (rc != 1) {
          if (!task.isFinished()) {
            // Can't discard this -- something might be waiting on a result from it??
            return;
          }
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

  /**
   * Declares a task that has been created to deliver a potential resolution.
   * <p>
   * The {@code valueSpecification} parameter must be normalized
   * 
   * @param valueSpecification the tentative resolution the value producer will attempt to deliver, not null
   * @param task the task to perform the resolution, not null
   * @param producer the value producer managed by the task which will deliver the value specification, not null
   * @return an existing producer, otherwise null if the new task is now declared for the work
   */
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
                producer.addRef(); // The caller already holds an open reference
                resolveTask.setValue(producer);
                result = producer;
              } else {
                // An equivalent task is doing the work
                result = resolveTask.getValue();
              }
              result.addRef(); // Either the caller holds an open reference on the producer, or we've got the task lock
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
            producer.addRef(); // Caller already holds open reference
            tasks.put(task, producer);
            result = producer;
            result.addRef(); // Caller already holds open reference (this is the producer)
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
   * <p>
   * This returns a normalized form of the value specification.
   * 
   * @param valueSpec the specification to process, not null
   * @return the possibly simplified specification, not null
   */
  public ValueSpecification simplifyType(final ValueSpecification valueSpec) {
    final ComputationTargetSpecification oldTargetSpec = valueSpec.getTargetSpecification();
    final ComputationTargetSpecification newTargetSpec = ComputationTargetResolverUtils.simplifyType(oldTargetSpec, getCompilationContext().getComputationTargetResolver());
    if (newTargetSpec == oldTargetSpec) {
      return MemoryUtils.instance(valueSpec);
    } else {
      return MemoryUtils.instance(new ValueSpecification(valueSpec.getValueName(), newTargetSpec, valueSpec.getProperties()));
    }
  }

  /**
   * Bulk form of {@link #simplifyType(ValueSpecification)}. If the values are already in their simplified form then the original collection is returned.
   * 
   * @param specifications the specifications to process, not null
   * @return the possibly simplified specifications, not null
   */
  public Collection<ValueSpecification> simplifyTypes(final Collection<ValueSpecification> specifications) {
    if (specifications.size() == 1) {
      final ValueSpecification specification = specifications.iterator().next();
      final ValueSpecification reducedSpecification = simplifyType(specification);
      if (specification == reducedSpecification) {
        return specifications;
      } else {
        return Collections.singleton(reducedSpecification);
      }
    } else {
      final Collection<ValueSpecification> result = new ArrayList<ValueSpecification>(specifications.size());
      for (ValueSpecification specification : specifications) {
        result.add(simplifyType(specification));
      }
      return result;
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
      for (final ExceptionWrapper exception : context._exceptions.keySet()) {
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
    for (final ExceptionWrapper exception : _exceptions.keySet()) {
      result.put(exception.getException(), exception.getCount());
    }
    return result;
  }

}
