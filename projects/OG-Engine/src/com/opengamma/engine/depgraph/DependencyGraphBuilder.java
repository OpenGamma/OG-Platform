/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.ResolveTask.TerminationCallback;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Cancellable;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Builds a dependency graph that describes how to calculate values that will satisfy a given
 * set of value requirements.
 */
public class DependencyGraphBuilder {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);
  private static final AtomicInteger s_nextObjectId = new AtomicInteger();

  // Injected Inputs:
  private String _calculationConfigurationName;
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private ComputationTargetResolver _targetResolver;
  private CompiledFunctionResolver _functionResolver;
  private FunctionCompilationContext _compilationContext;

  /**
   * Number of additional threads to launch while requirements are being added or the graph is being built.
   * The total number of threads used for graph construction may be up to this value or may be one higher
   * as a thread blocked on graph construction in the call to {@link #getDependencyGraph} will join in with
   * the remaining construction.
   */
  private volatile int _maxAdditionalThreads = Runtime.getRuntime().availableProcessors();

  // State:
  private final int _objectId = s_nextObjectId.incrementAndGet();
  private final ConcurrentMap<ValueRequirement, ConcurrentMap<ResolveTask, ResolveTask>> _requirements = new ConcurrentHashMap<ValueRequirement, ConcurrentMap<ResolveTask, ResolveTask>>();
  private final ConcurrentMap<ValueSpecification, ConcurrentMap<ResolveTask, ResolveTask>> _specifications = new ConcurrentHashMap<ValueSpecification, ConcurrentMap<ResolveTask, ResolveTask>>();
  private final AtomicInteger _activeJobCount = new AtomicInteger();
  private final Set<Job> _activeJobs = new HashSet<Job>();
  private final Queue<ResolveTask> _runQueue = new ConcurrentLinkedQueue<ResolveTask>();
  private final Collection<Throwable> _exceptions = new ConcurrentLinkedQueue<Throwable>();
  private final ConcurrentMap<ComputationTarget, ConcurrentMap<ParameterizedFunction, DependencyNode>> _graphTargets = new ConcurrentHashMap<ComputationTarget, ConcurrentMap<ParameterizedFunction, DependencyNode>>();
  private final Collection<ValueSpecification> _terminalOutputs = new ConcurrentLinkedQueue<ValueSpecification>();

  // TODO: we could have different run queues for the different states. When the PENDING one is considered, a bulk lookup operation can then be done

  // TODO: The number of active jobs for thread spawn decisions could come from a variable rather shared among a set of builders that themselves be
  // running concurrently (e.g. different configurations) so that the total background threads for the group is pegged, NOT the count per builder which
  // may be too much.

  // TODO: We should use an external execution framework rather than the one here; there are far better (and probably more accurate) implementations of
  // the algorithm in other projects I've worked on. 

  /**
   * Incrementing identifier for background thread identifiers. Users must hold the _activeJobs monitor.
   */
  private int _nextJobThreadId;

  private final TerminationCallback _addTargetCallback = new TerminationCallback() {

    private DependencyNode taskToNode(final ResolveTask task) {
      DependencyNode node = task.getDependencyNode();
      if (!node.getInputNodes().isEmpty() || !node.getDependentNodes().isEmpty()) {
        return node;
      }
      s_logger.debug("Converting {} to dependency node", task);
      final ComputationTarget target = task.getDependencyNode().getComputationTarget();
      ConcurrentMap<ParameterizedFunction, DependencyNode> functions = _graphTargets.get(target);
      if (functions == null) {
        functions = new ConcurrentHashMap<ParameterizedFunction, DependencyNode>();
        final ConcurrentMap<ParameterizedFunction, DependencyNode> existing = _graphTargets.putIfAbsent(target, functions);
        if (existing != null) {
          functions = existing;
        }
      }
      final ParameterizedFunction function = task.getDependencyNode().getFunction();
      node = functions.get(function);
      if (node == null) {
        node = task.getDependencyNode();
        final DependencyNode existing = functions.putIfAbsent(function, node);
        if (existing != null) {
          node = existing;
        }
      }

      // I think here is where we should add the output values as the resultant graph will require less pruning. It does
      // break the tests through which assume if a function was chosen then *all* if its outputs will be available.
      // Perhaps that's what we want though?
      // node.addOutputValue (task.getValueSpecification ());

      if (task.getInputTasks() != null) {
        for (ResolveTask input : task.getInputTasks()) {
          node.addInputValue(input.getValueSpecification());
          node.addInputNode(taskToNode(input));
        }
      }
      task.setDependencyNode(node);
      return node;
    }

    @Override
    public void complete(final ResolveTask task) {
      s_logger.info("Resolved {} to {}", task.getValueRequirement(), task.getValueSpecification());
      taskToNode(task);
      _terminalOutputs.add(task.getValueSpecification());
    }

    @Override
    public void failed(final ResolveTask task) {
      // TODO: extract some useful exception state from the task
      s_logger.error("Couldn't resolve {}", task.getValueRequirement());
    }

  };

  /**
   * @return the calculationConfigurationName
   */
  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  /**
   * @param calculationConfigurationName the calculationConfigurationName to set
   */
  public void setCalculationConfigurationName(String calculationConfigurationName) {
    _calculationConfigurationName = calculationConfigurationName;
  }

  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @param liveDataAvailabilityProvider the liveDataAvailabilityProvider to set
   */
  public void setLiveDataAvailabilityProvider(LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
  }

  /**
   * @return the functionResolver
   */
  public CompiledFunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  /**
   * @param functionResolver the functionResolver to set
   */
  public void setFunctionResolver(CompiledFunctionResolver functionResolver) {
    _functionResolver = functionResolver;
  }

  /**
   * @return the targetResolver
   */
  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }

  /**
   * @param targetResolver the targetResolver to set
   */
  public void setTargetResolver(ComputationTargetResolver targetResolver) {
    _targetResolver = targetResolver;
  }

  /**
   * @return the compilationContext
   */
  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }

  /**
   * @param compilationContext the compilationContext to set
   */
  public void setCompilationContext(FunctionCompilationContext compilationContext) {
    _compilationContext = compilationContext;
  }

  public int getMaxAdditionalThreads() {
    return _maxAdditionalThreads;
  }

  /**
   * Sets the maximum number of background threads to use for graph building. Set to zero to disable
   * background building. When set to a non-zero amount, if there is additional pending work jobs may
   * be started.
   * 
   * @param maxAdditionalThreads maximum number of background threads to use
   */
  public void setMaxAdditionalThreads(final int maxAdditionalThreads) {
    ArgumentChecker.isTrue(maxAdditionalThreads >= 0, "maxAdditionalThreads");
    _maxAdditionalThreads = maxAdditionalThreads;
    startBackgroundBuild();
  }

  protected void checkInjectedInputs() {
    ArgumentChecker.notNullInjected(getLiveDataAvailabilityProvider(), "liveDataAvailabilityProvider");
    ArgumentChecker.notNullInjected(getFunctionResolver(), "functionResolver");
    ArgumentChecker.notNullInjected(getTargetResolver(), "targetResolver");
    ArgumentChecker.notNullInjected(getCalculationConfigurationName(), "calculationConfigurationName");
  }

  /**
   * Adds a target requirement to the graph. The requirement is queued and the call returns; construction
   * of the graph will happen on a background thread (if additional threads is non-zero), or when the
   * call to {@link #getDependencyGraph} is made. If it was not possible to satisfy the requirement that
   * must be checked after graph construction is complete.
   * 
   * @param requirement requirement to add, not {@code null}
   */
  public void addTarget(ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    checkInjectedInputs();
    resolveRequirement(requirement, null).notifyOnTermination(_addTargetCallback);
    // If the run-queue was empty, we won't have started a thread, so double check 
    startBackgroundConstructionJob();
  }

  /**
   * Adds target requirements to the graph. The requirements are queued and the call returns; construction
   * of the graph will happen on a background thread (if additional threads is non-zero), or when the
   * call to {@link #getDependencyGraph} is made. If it was not possible to satisfy one or more requirements
   * that must be checked after graph construction is complete.
   * 
   * @param requirements requirements to add, not {@code null} and not containing {@code null}s.
   */
  public void addTarget(Set<ValueRequirement> requirements) {
    ArgumentChecker.noNulls(requirements, "requirements");
    checkInjectedInputs();
    for (ValueRequirement requirement : requirements) {
      resolveRequirement(requirement, null).notifyOnTermination(_addTargetCallback);
    }
    // If the run-queue was empty, we may not have started enough threads, so double check 
    startBackgroundConstructionJob();
  }

  protected ResolveTask resolveRequirement(final ValueRequirement requirement, final ResolveTask dependent) {
    s_logger.debug("addTargetImpl {}", requirement);
    final Set<ResolveTask> otherTasks = getOtherTasksResolving(requirement, dependent);
    if (otherTasks == null) {
      // There are no other tasks resolving the requirement
      final ResolveTask newTask = new ResolveTask(requirement);
      final ResolveTask task = declareTaskResolving(requirement, newTask);
      if (task == newTask) {
        s_logger.debug("Created resolver task for {}", requirement);
        addToRunQueue(task);
      }
      return task;
    } else {
      for (ResolveTask otherTask : otherTasks) {
        if (otherTask.getState() == ResolveTask.State.COMPLETE) {
          s_logger.debug("Found completed resolve task for {}", requirement);
          return otherTask;
        }
      }
      // There are other tasks resolving the requirement, but they may be different to us
      final ResolveTask newTask = new ResolveTask(requirement);
      final ResolveTask task = declareTaskResolving(requirement, newTask);
      if (task == newTask) {
        s_logger.debug("Created deferred resolver task for {}", requirement);
        final AtomicInteger blocked = new AtomicInteger(1);
        final TerminationCallback callback = new TerminationCallback() {

          private volatile ResolveTask _completed;

          @Override
          public void complete(final ResolveTask otherTask) {
            s_logger.debug("Resolve task completed for {}", requirement);
            _completed = otherTask;
            blocked.decrementAndGet();
          }

          @Override
          public void failed(final ResolveTask otherTask) {
            if (blocked.decrementAndGet() == 0) {
              if (_completed == null) {
                s_logger.debug("Created new resolver task for {}", requirement);
                addToRunQueue(task);
              }
            }
          }

        };
        for (ResolveTask otherTask : otherTasks) {
          blocked.incrementAndGet();
          otherTask.notifyOnTermination(callback);
        }
        callback.failed(null);
      }
      return task;
    }
  }

  protected void addToRunQueue(final ResolveTask runnable) {
    s_logger.debug("addToRunQueue {}", runnable);
    final boolean dontSpawn = _runQueue.isEmpty();
    _runQueue.add(runnable);
    if (!dontSpawn) {
      startBackgroundConstructionJob();
    }
  }

  private boolean startBackgroundConstructionJob() {
    int activeJobs = _activeJobCount.get();
    while (activeJobs < getMaxAdditionalThreads()) {
      if (_activeJobCount.compareAndSet(activeJobs, activeJobs + 1)) {
        synchronized (_activeJobs) {
          final Job job = createConstructionJob();
          _activeJobs.add(job);
          final Thread t = new Thread(job);
          t.setDaemon(true);
          t.setName(toString() + "-" + (_nextJobThreadId++));
          t.start();
          s_logger.info("Started background thread {}", t);
        }
        return true;
      }
      activeJobs = _activeJobCount.get();
    }
    return false;
  }

  /**
   * Job running thread.
   */
  protected final class Job implements Runnable, Cancellable {

    private volatile boolean _poison;

    private Job() {
    }

    @Override
    public void run() {
      s_logger.info("Building job started");
      boolean jobsLeftToRun;
      do {
        do {
          try {
            jobsLeftToRun = buildGraph();
          } catch (Throwable t) {
            s_logger.warn("Graph builder exception", t);
            postException(t);
            jobsLeftToRun = false;
          }
        } while (!_poison && jobsLeftToRun);
        s_logger.debug("Building job stopping");
        int activeJobs = _activeJobCount.decrementAndGet();
        // Watch for late arrivals in the run queue; they might have seen the old value
        // of activeJobs and not started anything.
        while (!_runQueue.isEmpty() && (activeJobs < getMaxAdditionalThreads()) && !_poison) {
          if (_activeJobCount.compareAndSet(activeJobs, activeJobs + 1)) {
            s_logger.debug("Building job resuming");
            // Note the log messages may go from "resuming" to stopped if the poison arrives between
            // the check above and the check below. This might look odd, but what the hey - they're
            // only DEBUG level messages.
            jobsLeftToRun = true;
            break;
          }
          activeJobs = _activeJobCount.get();
        }
      } while (!_poison && jobsLeftToRun);
      synchronized (_activeJobs) {
        _activeJobs.remove(this);
      }
      s_logger.info("Building job stopped");
    }

    @Override
    public boolean cancel(final boolean mayInterrupt) {
      _poison = true;
      return true;
    }

  }

  protected Job createConstructionJob() {
    return new Job();
  }

  /**
   * Main process loop, takes a runnable task and executes it. If the graph has not been built when
   * getDependencyGraph is called, the calling thread will also join this. There are additional
   * threads that also run in a pool to complete the work of the graph building.
   * 
   * @return true if there is more work still to do, false if all the work is done
   */
  protected boolean buildGraph() {
    final ResolveTask task = _runQueue.poll();
    if (task == null) {
      return false;
    }
    task.run(this);
    return true;
  }

  /**
   * Tests if the graph has been built or if work is still required. Graphs are only built in the
   * background if additional threads is set to non-zero.
   * 
   * @return {@code true} if the graph has been built, {@code false} if it is outstanding.
   */
  public boolean isGraphBuilt() {
    synchronized (_activeJobs) {
      if (!_activeJobs.isEmpty()) {
        // One or more active jobs, so can't be built yet
        return false;
      }
    }
    // no active jobs, so built if there is nothing in the run queue
    return _runQueue.isEmpty();
  }

  /**
   * Returns the dependency graph if it has been completed by background threads. If the graph has
   * not been completed it will return {@code null}. If the number of additional threads is set to
   * zero then the graph will not be built until {@link #getDependencyGraph} is called.
   * 
   * @return the graph if built or {@code null} otherwise
   */
  public DependencyGraph pollDependencyGraph() {
    if (isGraphBuilt()) {
      return createDependencyGraph();
    }
    return null;
  }

  /**
   * Cancels any construction threads. If background threads had been started for graph construction, they
   * will be stopped and the construction abandoned. Note that this will also reset the number of
   * additional threads to zero to prevent further threads from being started by the existing ones before
   * they terminate. If a thread is already blocked in a call to {@link getDependencyGraph} it will receive
   * a {@link CancellationException} unless the graph construction completes before the cancellation is
   * noted by that or other background threads. The cancellation is temporary, the additional threads
   * can be reset afterwards for continued background building or a subsequent call to getDependencyGraph
   * can finish the work.
   */
  public void cancelActiveBuild() {
    setMaxAdditionalThreads(0);
    synchronized (_activeJobs) {
      for (Job job : _activeJobs) {
        job.cancel(true);
      }
      _activeJobs.clear();
    }
  }

  /**
   * If there are runnable tasks but not as many active jobs as the requested number then additional threads
   * will be started. This is called when the number of background threads is changed.
   */
  protected void startBackgroundBuild() {
    if (_runQueue.isEmpty()) {
      s_logger.info("No pending runnable tasks for background building");
    } else {
      final Iterator<ResolveTask> itr = _runQueue.iterator();
      while (itr.hasNext() && startBackgroundConstructionJob()) {
        itr.next();
      }
    }
  }

  /**
   * Returns the constructed dependency graph able to compute as may of the requirements requested as
   * possible. If graph construction has not completed, will block the caller until it has and the
   * calling thread will be used for the remaining graph construction work (which will be the full
   * graph construction if additional threads is set to zero). For a non-blocking form see
   * {@link #pollDependencyGraph}.
   * 
   * @return the graph, not {@code null}
   */
  public DependencyGraph getDependencyGraph() {
    if (!isGraphBuilt()) {
      s_logger.info("Building dependency graph");
      do {
        final Job job = createConstructionJob();
        synchronized (_activeJobs) {
          _activeJobs.add(job);
        }
        job.run();
        synchronized (_activeJobs) {
          if (_activeJobs.isEmpty()) {
            // We're done and there are no other jobs
            break;
          } else {
            // There are other jobs still running ...
            if (!_runQueue.isEmpty()) {
              // ... and stuff in the queue
              continue;
            }
          }
        }
        // ... but nothing in the queue for us so take a nap
        s_logger.info("Waiting for background threads");
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new OpenGammaRuntimeException("Interrupted during graph building", e);
        }
      } while (true);
      if (!isGraphBuilt()) {
        throw new CancellationException("Dependency graph building incomplete");
      }
    }
    return createDependencyGraph();
  }

  // DON'T CHECK IN WITH =true
  private static final boolean DEBUG_DUMP_DEPENDENCY_GRAPH = false;

  protected DependencyGraph createDependencyGraph() {
    final DependencyGraph graph = new DependencyGraph(getCalculationConfigurationName());
    s_logger.debug("Converting internal representation to dependency graph");
    for (ConcurrentMap<ParameterizedFunction, DependencyNode> functions : _graphTargets.values()) {
      for (DependencyNode node : functions.values()) {
        graph.addDependencyNode(node);
      }
    }
    for (ValueSpecification terminalOutput : _terminalOutputs) {
      graph.addTerminalOutputValue(terminalOutput);
    }
    graph.dumpStructureASCII(System.out);
    if (DEBUG_DUMP_DEPENDENCY_GRAPH) {
      try {
        final PrintStream ps = new PrintStream(new FileOutputStream("/tmp/dependencyGraph.txt"));
        graph.dumpStructureASCII(ps);
        ps.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return graph;
  }

  /**
   * Stores an exception that should be reported to the user.
   * 
   * @param t exception to store, not {@code null}
   */
  protected void postException(final Throwable t) {
    _exceptions.add(t);
  }

  /**
   * Returns the set of exceptions that may have prevented graph construction.
   * 
   * @return the set of exceptions that were thrown by the building process, or {@code null} for none
   */
  @SuppressWarnings("unchecked")
  public Collection<Throwable> getExceptions() {
    if (_exceptions.isEmpty()) {
      return null;
    } else {
      return Collections.unmodifiableCollection(_exceptions);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "-" + _objectId;
  }

  private static <T> ResolveTask declareImpl(final T key, final ResolveTask task, final ConcurrentMap<T, ConcurrentMap<ResolveTask, ResolveTask>> taskMap) {
    ConcurrentMap<ResolveTask, ResolveTask> tasks = taskMap.get(key);
    if (tasks == null) {
      tasks = new ConcurrentHashMap<ResolveTask, ResolveTask>();
      tasks.put(task, task);
      tasks = taskMap.putIfAbsent(key, tasks);
      if (tasks == null) {
        return task;
      } else {
        final ResolveTask existingTask = tasks.putIfAbsent(task, task);
        if (existingTask == null) {
          return task;
        } else {
          return existingTask;
        }
      }
    } else {
      final ResolveTask existingTask = tasks.putIfAbsent(task, task);
      if (existingTask == null) {
        return task;
      } else {
        return existingTask;
      }
    }
  }

  protected ResolveTask declareTaskProducing(final ValueSpecification valueSpecification, final ResolveTask task) {
    return declareImpl(valueSpecification, task, _specifications);
  }

  protected Set<ResolveTask> getOtherTasksProducing(final ValueSpecification valueSpecification, final ResolveTask task) {
    ConcurrentMap<ResolveTask, ResolveTask> tasks = _specifications.get(valueSpecification);
    if (tasks == null) {
      return null;
    } else {
      // TODO: need to return a set with any tasks that ARE NOT dependent on the supplied task, this doesn't do that
      return tasks.keySet();
    }
  }

  protected ResolveTask declareTaskResolving(final ValueRequirement valueRequirement, final ResolveTask task) {
    return declareImpl(valueRequirement, task, _requirements);
  }

  protected Set<ResolveTask> getOtherTasksResolving(final ValueRequirement valueRequirement, final ResolveTask task) {
    final ConcurrentMap<ResolveTask, ResolveTask> tasks = _requirements.get(valueRequirement);
    if (tasks == null) {
      return null;
    } else {
      // TODO: need to return a set with any tasks that ARE NOT dependent on the supplied task, this doesn't do that
      return tasks.keySet();
    }
  }

}
