/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Cancelable;

/**
 * Builds a dependency graph that describes how to calculate values that will satisfy a given set of value requirements. Although a graph builder may itself use additional threads to complete the
 * graph it is only safe for a single calling thread to call any of the public methods at any one time. If multiple threads are to attempt to add targets to the graph concurrently, it is possible to
 * synchronize on the builder instance.
 */
public final class DependencyGraphBuilder implements Cancelable {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);

  private static final AtomicInteger s_nextObjectId = new AtomicInteger();
  private static final AtomicInteger s_nextJobId = new AtomicInteger();

  private static final boolean NO_BACKGROUND_THREADS = false; // DON'T CHECK IN WITH =true
  private static final int MAX_ADDITIONAL_THREADS = -1; // DON'T CHECK IN WITH !=-1
  private static final boolean DEBUG_DUMP_DEPENDENCY_GRAPH = false; // DON'T CHECK IN WITH =true
  private static final boolean DEBUG_DUMP_FAILURE_INFO = false; // DON'T CHECK IN WITH =true

  private final int _objectId = s_nextObjectId.incrementAndGet();
  private final AtomicInteger _activeJobCount = new AtomicInteger();
  private final Set<Job> _activeJobs = new HashSet<Job>();
  private final Queue<ContextRunnable> _runQueue = new ConcurrentLinkedQueue<ContextRunnable>();
  private final Queue<ValueRequirement> _workQueue = new ConcurrentLinkedQueue<ValueRequirement>();
  private final Object _buildCompleteLock = new Object();
  private final GraphBuildingContext _context = new GraphBuildingContext(this);
  private final AtomicLong _completedSteps = new AtomicLong();
  private final AtomicLong _scheduledSteps = new AtomicLong();
  private final GetTerminalValuesCallback _getTerminalValuesCallback = new GetTerminalValuesCallback(DEBUG_DUMP_FAILURE_INFO ? new ResolutionFailurePrinter(openDebugStream("resolutionFailure"))
      : ResolutionFailureVisitor.DEFAULT_INSTANCE);
  private final Executor _executor;
  private final Housekeeper _contextCleaner = Housekeeper.of(this, ResolutionCacheCleanup.INSTANCE);
  private final PendingRequirements _pendingRequirements = new PendingRequirements(this);
  private String _calculationConfigurationName;
  private MarketDataAvailabilityProvider _marketDataAvailabilityProvider;
  private ComputationTargetResolver _targetResolver;
  private CompiledFunctionResolver _functionResolver;
  private FunctionCompilationContext _compilationContext;
  private FunctionExclusionGroups _functionExclusionGroups;

  // The resolve task is ref-counted once for the map (it is being used as a set)
  private final ConcurrentMap<ValueRequirement, Map<ResolveTask, ResolveTask>> _requirements = new ConcurrentHashMap<ValueRequirement, Map<ResolveTask, ResolveTask>>();
  private final AtomicInteger _activeResolveTasks = new AtomicInteger();

  // The resolve task is NOT ref-counted (it is only used for parent comparisons), but the value producer is
  private final ConcurrentMap<ValueSpecification, MapEx<ResolveTask, ResolvedValueProducer>> _specifications = new ConcurrentHashMap<ValueSpecification, MapEx<ResolveTask, ResolvedValueProducer>>();

  /**
   * Number of additional threads to launch while requirements are being added or the graph is being built. The total number of threads used for graph construction may be up to this value or may be
   * one higher as a thread blocked on graph construction in the call to {@link #getDependencyGraph} will join in with the remaining construction.
   */
  private volatile int _maxAdditionalThreads = getDefaultMaxAdditionalThreads();

  /**
   * Number of resolution tasks to put into the run queue at any one time. The run queue will be drained of all tasks before the next batch is applied so that loop detection can be performed. This
   * size can be used in conjunction with the cache flushing period to reduce the memory requirements when building large graphs.
   */
  private volatile int _maxRunQueue = getDefaultMaxRunQueue();

  /**
   * Flush the value resolver cache when there are more than a certain number of elements in it.
   */
  private volatile int _resolutionCacheSize = getDefaultResolutionCacheSize();

  /**
   * Flag to indicate when the build has been canceled.
   */
  private boolean _cancelled;

  // TODO: we could have different run queues for the different states. When the PENDING one is considered, a bulk lookup operation can then be done

  // TODO: We should use an external execution framework rather than the one here; there are far better (and probably more accurate) implementations of
  // the algorithm in other projects I've worked on.

  @SuppressWarnings("unused")
  public static int getDefaultMaxAdditionalThreads() {
    return NO_BACKGROUND_THREADS ? 0 : (MAX_ADDITIONAL_THREADS >= 0) ? MAX_ADDITIONAL_THREADS : Runtime.getRuntime().availableProcessors();
  }

  public static int getDefaultResolutionCacheSize() {
    // TODO: want to flush the cache when memory is low, rather than at count thresholds
    // 10K objects arbitrary choice from running Demo Equity Option Test View
    return 10000;
  }

  public static int getDefaultMaxRunQueue() {
    return (getDefaultMaxAdditionalThreads() + 1) * 8;
  }

  public DependencyGraphBuilder() {
    this(DependencyGraphBuilderFactory.getDefaultExecutor());
  }

  protected DependencyGraphBuilder(final Executor executor) {
    _executor = executor;
  }

  protected GraphBuildingContext getContext() {
    return _context;
  }

  protected GetTerminalValuesCallback getTerminalValuesCallback() {
    return _getTerminalValuesCallback;
  }

  /**
   * @return the calculationConfigurationName
   */
  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  /**
   * @param calculationConfigurationName the calculationConfigurationName to set
   */
  public void setCalculationConfigurationName(final String calculationConfigurationName) {
    _calculationConfigurationName = calculationConfigurationName;
  }

  /**
   * @return the market data availability provider
   */
  public MarketDataAvailabilityProvider getMarketDataAvailabilityProvider() {
    return _marketDataAvailabilityProvider;
  }

  /**
   * @param marketDataAvailabilityProvider the market data availability provider to set
   */
  public void setMarketDataAvailabilityProvider(MarketDataAvailabilityProvider marketDataAvailabilityProvider) {
    _marketDataAvailabilityProvider = marketDataAvailabilityProvider;
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

  /**
   * Sets the function exclusion group rules to use.
   * 
   * @param exclusionGroups the source of groups, or null to not use function exclusion groups
   */
  public void setFunctionExclusionGroups(final FunctionExclusionGroups exclusionGroups) {
    _functionExclusionGroups = exclusionGroups;
  }

  /**
   * Returns the current function exclusion group rules.
   * 
   * @return the function exclusion groups or null if none are being used
   */
  public FunctionExclusionGroups getFunctionExclusionGroups() {
    return _functionExclusionGroups;
  }

  public int getMaxAdditionalThreads() {
    return _maxAdditionalThreads;
  }

  /**
   * Sets the maximum number of background threads to use for graph building. Set to zero to disable background building. When set to a non-zero amount, if there is additional pending work jobs may be
   * started.
   * 
   * @param maxAdditionalThreads maximum number of background threads to use
   */
  public void setMaxAdditionalThreads(final int maxAdditionalThreads) {
    ArgumentChecker.isTrue(maxAdditionalThreads >= 0, "maxAdditionalThreads");
    _maxAdditionalThreads = maxAdditionalThreads;
    startBackgroundBuild();
  }

  public int getResolutionCacheSize() {
    return _resolutionCacheSize;
  }

  public void setResolutionCacheSize(final int resolutionCacheSize) {
    ArgumentChecker.isTrue(resolutionCacheSize > 0, "resolutionCacheSize");
    s_logger.info("Resolution cache size set to {}", resolutionCacheSize);
    _resolutionCacheSize = resolutionCacheSize;
  }

  public int getMaxRunQueue() {
    return _maxRunQueue;
  }

  public void setMaxRunQueue(final int maxRunQueue) {
    _maxRunQueue = maxRunQueue;
  }

  protected int getActiveResolveTasks() {
    return _activeResolveTasks.get();
  }

  protected void incrementActiveResolveTasks() {
    _activeResolveTasks.incrementAndGet();
  }

  protected void decrementActiveResolveTasks() {
    _activeResolveTasks.decrementAndGet();
  }

  protected MapEx<ResolveTask, ResolvedValueProducer> getTasks(final ValueSpecification valueSpecification) {
    return _specifications.get(valueSpecification);
  }

  protected MapEx<ResolveTask, ResolvedValueProducer> getOrCreateTasks(final ValueSpecification valueSpecification) {
    MapEx<ResolveTask, ResolvedValueProducer> tasks = getTasks(valueSpecification);
    if (tasks == null) {
      tasks = new MapEx<ResolveTask, ResolvedValueProducer>();
      final MapEx<ResolveTask, ResolvedValueProducer> existing = _specifications.putIfAbsent(valueSpecification, tasks);
      if (existing != null) {
        return existing;
      }
    }
    return tasks;
  }

  protected Map<ResolveTask, ResolveTask> getTasks(final ValueRequirement valueRequirement) {
    return _requirements.get(valueRequirement);
  }

  protected Map<ResolveTask, ResolveTask> getOrCreateTasks(final ValueRequirement valueRequirement) {
    Map<ResolveTask, ResolveTask> tasks = _requirements.get(valueRequirement);
    if (tasks == null) {
      tasks = new HashMap<ResolveTask, ResolveTask>();
      final Map<ResolveTask, ResolveTask> existing = _requirements.putIfAbsent(valueRequirement, tasks);
      if (existing != null) {
        return existing;
      }
    }
    return tasks;
  }

  /**
   * Sets the visitor to receive resolution failures. If not set, a synthetic exception is created for each failure in the miscellaneous exception set.
   * 
   * @param failureVisitor the visitor to use, or null to create synthetic exceptions
   */
  public void setResolutionFailureVisitor(final ResolutionFailureVisitor<?> failureVisitor) {
    getTerminalValuesCallback().setResolutionFailureVisitor(failureVisitor);
  }

  protected void checkInjectedInputs() {
    ArgumentChecker.notNullInjected(getMarketDataAvailabilityProvider(), "marketDataAvailabilityProvider");
    ArgumentChecker.notNullInjected(getFunctionResolver(), "functionResolver");
    ArgumentChecker.notNullInjected(getTargetResolver(), "targetResolver");
    ArgumentChecker.notNullInjected(getCalculationConfigurationName(), "calculationConfigurationName");
  }

  /**
   * Adds resolution of the given requirement to the run queue. Resolution will start as soon as possible and be available as pending for any tasks already running that require resolution of the
   * requirement.
   * 
   * @param requirement the requirement to resolve
   */
  protected void addRunTargetImpl(final ValueRequirement requirement) {
    final ResolvedValueProducer resolvedValue = getContext().resolveRequirement(requirement, null, null);
    resolvedValue.addCallback(getContext(), getTerminalValuesCallback());
    _pendingRequirements.add(getContext(), resolvedValue);
    resolvedValue.release(getContext());
  }

  /**
   * Adds resolution of the given requirement to the work queue. If the run queue is below the maximum size the requirement is added directly to the run queue; otherwise it will become runnable when
   * the run queue is empty.
   * 
   * @param requirement the requirement to resolve
   */
  protected void addWorkTargetImpl(final ValueRequirement requirement) {
    if (_runQueue.size() < getMaxRunQueue()) {
      addRunTargetImpl(requirement);
    } else {
      // Already over capacity on the run queue; add requirement to work queue instead 
      _workQueue.add(requirement);
      // Increment the scheduled steps so that progress estimates aren't complete bunk
      _scheduledSteps.incrementAndGet();
    }
  }

  /**
   * Adds a target requirement to the graph. The requirement is queued and the call returns; construction of the graph will happen on a background thread (if additional threads is non-zero), or when
   * the call to {@link #getDependencyGraph} is made. If it was not possible to satisfy the requirement that must be checked after graph construction is complete.
   * 
   * @param requirement requirement to add, not null
   */
  public void addTarget(final ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    checkInjectedInputs();
    // Hold the build complete lock so that housekeeping thread cannot observe a "built" state within this atomic block of work
    synchronized (_buildCompleteLock) {
      addWorkTargetImpl(requirement);
    }
    // If the run-queue was empty, we won't have started a thread, so double check 
    startBackgroundConstructionJob();
  }

  /**
   * Adds target requirements to the graph. The requirements are queued and the call returns; construction of the graph will happen on a background thread (if additional threads is non-zero), or when
   * the call to {@link #getDependencyGraph} is made. If it was not possible to satisfy one or more requirements that must be checked after graph construction is complete.
   * 
   * @param requirements requirements to add, not null and not containing nulls.
   */
  public void addTarget(Set<ValueRequirement> requirements) {
    ArgumentChecker.noNulls(requirements, "requirements");
    checkInjectedInputs();
    // Hold the build complete lock so that housekeeping thread cannot observe a "built" state within this atomic block of work
    synchronized (_buildCompleteLock) {
      for (ValueRequirement requirement : requirements) {
        addWorkTargetImpl(requirement);
      }
    }
    // If the run-queue was empty, we may not have started enough threads, so double check 
    startBackgroundConstructionJob();
  }

  protected void addToRunQueue(final ContextRunnable runnable) {
    s_logger.debug("Queuing {}", runnable);
    final boolean dontSpawn = _runQueue.isEmpty() && _workQueue.isEmpty();
    _scheduledSteps.incrementAndGet();
    _runQueue.add(runnable);
    // Don't start construction jobs if the queue is empty or a sequential piece of work bounces between two threads (i.e. there
    // is already a background thread that is running the caller which can then execute the task it has just put into the run
    // queue). The moment the queue is non-empty, start a job if possible.
    if (!dontSpawn) {
      startBackgroundConstructionJob();
    }
  }

  protected boolean startBackgroundConstructionJob() {
    int activeJobs = _activeJobCount.get();
    while (activeJobs < getMaxAdditionalThreads()) {
      if (_activeJobCount.compareAndSet(activeJobs, activeJobs + 1)) {
        synchronized (_activeJobs) {
          final Job job = createConstructionJob();
          _activeJobs.add(job);
          _executor.execute(job);
        }
        return true;
      }
      activeJobs = _activeJobCount.get();
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  protected void abortLoops() {
    s_logger.debug("Checking for active tasks to abort");
    List<ResolveTask> activeTasks = null;
    for (MapEx<ResolveTask, ResolvedValueProducer> tasks : _specifications.values()) {
      synchronized (tasks) {
        for (ResolveTask task : (Set<ResolveTask>) tasks.keySet()) {
          if (task.isActive()) {
            if (activeTasks == null) {
              activeTasks = new LinkedList<ResolveTask>();
            }
            activeTasks.add(task);
          }
        }
      }
    }
    if (activeTasks != null) {
      final Set<Object> visited = new HashSet<Object>();
      int cancelled = 0;
      final GraphBuildingContext context = new GraphBuildingContext(this);
      for (ResolveTask task : activeTasks) {
        cancelled += task.cancelLoopMembers(getContext(), visited);
      }
      getContext().mergeThreadContext(context);
      s_logger.info("Cancelled {} looped tasks", cancelled);
    } else {
      s_logger.debug("No looped tasks");
    }
  }

  /**
   * Job running thread.
   */
  protected final class Job implements Runnable, Cancelable {

    private final int _objectId = s_nextJobId.incrementAndGet();
    private volatile boolean _poison;

    private Job() {
    }

    @Override
    public void run() {
      s_logger.info("Building job {} started for {}", _objectId, DependencyGraphBuilder.this);
      _contextCleaner.start();
      boolean jobsLeftToRun;
      int completed = 0;
      do {
        // Create a new context for each logical block so that an exception from the build won't leave us with
        // an inconsistent context.
        final GraphBuildingContext context = new GraphBuildingContext(DependencyGraphBuilder.this);
        do {
          try {
            jobsLeftToRun = buildGraph(context);
            completed++;
          } catch (Throwable t) {
            s_logger.warn("Graph builder exception", t);
            _context.exception(t);
            jobsLeftToRun = false;
          }
        } while (!_poison && jobsLeftToRun);
        s_logger.debug("Merging thread context");
        getContext().mergeThreadContext(context);
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
      synchronized (_buildCompleteLock) {
        final boolean abortLoops;
        synchronized (_activeJobs) {
          _activeJobs.remove(this);
          abortLoops = _activeJobs.isEmpty() && _runQueue.isEmpty();
        }
        if (abortLoops) {
          // Any tasks that are still active have created a reciprocal loop disjoint from the runnable
          // graph of tasks. Aborting them at this point is easier and possibly more efficient than
          // the overhead of trying to stop the loops forming in the first place.
          abortLoops();
          // If any loops were aborted, new jobs will go onto the run queue, possibly a new active job
          // started. We are officially dead but could be restarted using logic similar to above to
          // update the activeJobCount and activeJobs set.
          int newWork = 0;
          while (_runQueue.size() < getMaxRunQueue()) {
            final ValueRequirement requirement = _workQueue.poll();
            if (requirement != null) {
              s_logger.debug("Starting work on {}", requirement);
              addRunTargetImpl(requirement);
              newWork++;
            } else {
              break;
            }
          }
          if (newWork > 0) {
            s_logger.info("Added {} items from work queue to run queue, {} remaining", newWork, _workQueue.size());
            // Remove the synthetic scheduled steps that were added when the work was first registered
            _scheduledSteps.addAndGet(-newWork);
          }
        }
      }
      _contextCleaner.stop();
      s_logger.info("Building job {} stopped after {} operations", _objectId, completed);
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
   * Main process loop, takes a runnable task and executes it. If the graph has not been built when getDependencyGraph is called, the calling thread will also join this. There are additional threads
   * that also run in a pool to complete the work of the graph building.
   * 
   * @param context the calling thread's building context
   * @return true if there is more work still to do, false if all the work is done
   */
  protected boolean buildGraph(final GraphBuildingContext context) {
    final ContextRunnable task = _runQueue.poll();
    if (task == null) {
      return false;
    }
    task.run(context);
    _completedSteps.incrementAndGet();
    return true;
  }

  /**
   * Tests if the graph has been built or if work is still required. Graphs are only built in the background if additional threads is set to non-zero.
   * 
   * @return true if the graph has been built, false if it is outstanding
   */
  public boolean isGraphBuilt() {
    synchronized (_buildCompleteLock) {
      synchronized (_activeJobs) {
        if (_cancelled) {
          throw new CancellationException();
        }
        return _activeJobs.isEmpty() && _runQueue.isEmpty() && _workQueue.isEmpty();
      }
    }
  }

  /**
   * Returns the top-level value requirements currently being resolved. This does not include those that have not been started yet.
   * 
   * @return the value requirements
   */
  public Collection<ValueRequirement> getActiveOutstandingResolutions() {
    return new ArrayList<ValueRequirement>(_pendingRequirements.getValueRequirements());
  }

  /**
   * Returns the top-level value requirements currently being resolved. This includes those that have not been started yet.
   * 
   * @return the value requirements
   */
  public Collection<ValueRequirement> getAllOutstandingResolutions() {
    synchronized (_buildCompleteLock) {
      final Collection<ValueRequirement> pending = _pendingRequirements.getValueRequirements();
      final Collection<ValueRequirement> result = new ArrayList<ValueRequirement>(_workQueue.size() + pending.size());
      result.addAll(_workQueue);
      result.addAll(pending);
      return result;
    }
  }

  /**
   * Returns the dependency graph if it has been completed by background threads. If the graph has not been completed it will return null. If the number of additional threads is set to zero then the
   * graph will not be built until {@link #getDependencyGraph} is called.
   * 
   * @return the graph if built, null otherwise
   */
  public DependencyGraph pollDependencyGraph() {
    if (isGraphBuilt()) {
      return createDependencyGraph();
    }
    return null;
  }

  /**
   * Cancels any construction threads. If background threads had been started for graph construction, they will be stopped and the construction abandoned. Note that this will also reset the number of
   * additional threads to zero to prevent further threads from being started by the existing ones before they terminate. If a thread is already blocked in a call to {@link #getDependencyGraph} it
   * will receive a {@link CancellationException} unless the graph construction completes before the cancellation is noted by that or other background threads. The cancellation is temporary, the
   * additional threads can be reset afterwards for continued background building or a subsequent call to getDependencyGraph can finish the work.
   * 
   * @param mayInterruptIfRunning ignored
   * @return true if the build was cancelled
   */
  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    setMaxAdditionalThreads(0);
    synchronized (_activeJobs) {
      _cancelled = true;
      for (Job job : _activeJobs) {
        job.cancel(true);
      }
      _activeJobs.clear();
    }
    return true;
  }

  public boolean isCancelled() {
    return _cancelled;
  }

  /**
   * If there are runnable tasks but not as many active jobs as the requested number then additional threads will be started. This is called when the number of background threads is changed.
   */
  protected void startBackgroundBuild() {
    if (_runQueue.isEmpty()) {
      s_logger.info("No pending runnable tasks for background building");
    } else {
      final Iterator<ContextRunnable> itr = _runQueue.iterator();
      while (itr.hasNext() && startBackgroundConstructionJob()) {
        itr.next();
      }
    }
  }

  /**
   * Estimate the completion of the build, from 0 (nothing completed) to 1 (all done). The completion is based on the number of completed steps versus the currently known number of steps.
   * 
   * @return the completion estimate
   */
  public Supplier<Double> buildFractionEstimate() {
    return new BuildFractionEstimate(this);
  }

  protected long getScheduledSteps() {
    return _scheduledSteps.get();
  }

  protected long getCompletedSteps() {
    return _completedSteps.get();
  }

  /**
   * Returns the constructed dependency graph able to compute as many of the requirements requested as possible. If graph construction has not completed, will block the caller until it has and the
   * calling thread will be used for the remaining graph construction work (which will be the full graph construction if additional threads is set to zero). For a non-blocking form see
   * {@link #pollDependencyGraph} or {@link #getDependencyGraph(boolean)}.
   * 
   * @return the graph, not null
   */
  public DependencyGraph getDependencyGraph() {
    return getDependencyGraph(true);
  }

  /**
   * Returns the constructed dependency graph able to compute as many of the requirements requested as possible. If graph construction has not completed, the calling thread will participate in graph
   * construction (which will be the full graph construction if additional threads is set to zero). When background threads are being used, the caller may optionally be blocked until all have
   * completed. For a completely non-blocking form see {@link #pollDependencyGraph}.
   * 
   * @param allowBackgroundContinuation whether to block the caller until graph construction is complete. If set to false the function may return null if background threads are still completing but
   *          there was no work for the calling thread to do.
   * @return the graph if built, null if still being built in the background
   */
  public DependencyGraph getDependencyGraph(final boolean allowBackgroundContinuation) {
    if (!isGraphBuilt()) {
      s_logger.info("Building dependency graph");
      do {
        final Job job = createConstructionJob();
        synchronized (_activeJobs) {
          if (!_cancelled) {
            _activeJobs.add(job);
          }
        }
        job.run();
        synchronized (_activeJobs) {
          if (!_runQueue.isEmpty()) {
            // more jobs in the queue so keep going
            continue;
          }
        }
        if (allowBackgroundContinuation) {
          // ... but nothing in the queue for us so take a nap
          s_logger.info("Waiting for background threads");
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            throw new OpenGammaRuntimeException("Interrupted during graph building", e);
          }
        } else {
          return null;
        }
      } while (!isGraphBuilt());
    }
    return createDependencyGraph();
  }

  protected void discardIntermediateState() {
    s_logger.debug("Discarding intermediate state {} requirements", _requirements.size());
    _requirements.clear();
    s_logger.debug("Discarding intermediate state {} specifications", _specifications.size());
    _specifications.clear();
  }

  protected void flushCachedStates() {
    // TODO: use heuristics to throw data away more sensibly
    int resolversRemoved = 0;
    final List<ResolveTask> resolverDiscard = new ArrayList<ResolveTask>();
    GraphBuildingContext context = null;
    for (Map<ResolveTask, ResolveTask> entries : _requirements.values()) {
      synchronized (entries) {
        final Iterator<ResolveTask> itr = entries.keySet().iterator();
        while (itr.hasNext()) {
          final ResolveTask task = itr.next();
          if (task.isFinished()) {
            itr.remove();
            resolverDiscard.add(task);
          }
        }
      }
      if (!resolverDiscard.isEmpty()) {
        if (context == null) {
          context = new GraphBuildingContext(this);
        }
        resolversRemoved += resolverDiscard.size();
        for (ResolveTask task : resolverDiscard) {
          task.release(context);
        }
        resolverDiscard.clear();
      }
    }
    if (resolversRemoved > 0) {
      _activeResolveTasks.addAndGet(-resolversRemoved);
      s_logger.info("Discarded {} resolve task(s) - {} still active", resolversRemoved, _activeResolveTasks);
      getContext().mergeThreadContext(context);
    }
  }

  protected void reportStateSize() {
    if (!s_logger.isInfoEnabled()) {
      return;
    }
    int count = 0;
    for (Map<ResolveTask, ResolveTask> entries : _requirements.values()) {
      synchronized (entries) {
        count += entries.size();
      }
    }
    s_logger.info("Requirements cache = {} tasks for {} requirements", count, _requirements.size());
    count = 0;
    for (MapEx<ResolveTask, ResolvedValueProducer> entries : _specifications.values()) {
      synchronized (entries) {
        count += entries.size();
      }
    }
    s_logger.info("Specifications cache = {} tasks for {} specifications", count, _specifications.size());
  }

  protected DependencyGraph createDependencyGraph() {
    final DependencyGraph graph = new DependencyGraph(getCalculationConfigurationName());
    s_logger.debug("Converting internal representation to dependency graph");
    for (DependencyNode node : getTerminalValuesCallback().getGraphNodes()) {
      graph.addDependencyNode(node);
    }
    for (Map.Entry<ValueRequirement, ValueSpecification> terminalOutput : getTerminalValuesCallback().getTerminalValues().entrySet()) {
      graph.addTerminalOutput(terminalOutput.getKey(), terminalOutput.getValue());
    }
    //graph.dumpStructureASCII(System.out);
    if (DEBUG_DUMP_DEPENDENCY_GRAPH) {
      final PrintStream ps = openDebugStream("dependencyGraph");
      ps.println("Configuration = " + getCalculationConfigurationName());
      graph.dumpStructureASCII(ps);
      ps.close();
    }
    // Clear out the build caches
    discardIntermediateState();
    s_logger.info("Graph built after {} steps", _completedSteps);
    return graph;
  }

  protected PrintStream openDebugStream(final String name) {
    try {
      final String fileName = System.getProperty("java.io.tmpdir") + File.separatorChar + name + _objectId + ".txt";
      return new PrintStream(new FileOutputStream(fileName));
    } catch (IOException e) {
      s_logger.error("Can't open debug file", e);
      return System.out;
    }
  }

  /**
   * Returns a map of the originally requested value requirements to the value specifications that were put into the graph as terminal outputs. Any unsatisfied requirements will be absent from the
   * map.
   * 
   * @return the map of requirements to value specifications, not null
   */
  public Map<ValueRequirement, ValueSpecification> getValueRequirementMapping() {
    return getTerminalValuesCallback().getTerminalValues();
  }

  /**
   * Returns the set of exceptions that may have prevented graph construction.
   * 
   * @return the set of exceptions that were thrown by the building process, null for none
   */
  public Map<Throwable, Integer> getExceptions() {
    return getContext().getExceptions();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "-" + _objectId;
  }

}
