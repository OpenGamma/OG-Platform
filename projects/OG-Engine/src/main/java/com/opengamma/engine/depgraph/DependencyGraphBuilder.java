/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.ResolvedValueProducer.Chain;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.engine.target.digest.TargetDigests;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.Cancelable;
import com.opengamma.util.test.Profiler;
import com.opengamma.util.tuple.Pair;

/**
 * Builds a dependency graph that describes how to calculate values that will satisfy a given set of value requirements. Although a graph builder may itself use additional threads to complete the
 * graph it is only safe for a single calling thread to call any of the public methods at any one time. If multiple threads are to attempt to add targets to the graph concurrently, it is possible to
 * synchronize on the builder instance.
 */
public final class DependencyGraphBuilder implements Cancelable {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);

  /** The object id to be given to the next DependencyGraphBuilder to be created */
  private static final AtomicInteger s_nextObjectId = new AtomicInteger();

  /** The job id to be given to the next job to be created */
  private static final AtomicInteger s_nextJobId = new AtomicInteger();

  /**
   * Disables the multi-threaded graph building. If set, value requirements will be queued as they are added and the graph built by a single thread when {@link #getDependencyGraph} is called. This is
   * false by default but can be controlled by the {@code DependencyGraphBuilder.noBackgroundThreads} property. When set the value of {@link #_maxAdditionalThreads} is ignored.
   */
  private static final boolean NO_BACKGROUND_THREADS = System.getProperty("DependencyGraphBuilder.noBackgroundThreads", "FALSE").equalsIgnoreCase("TRUE");

  /**
   * Limits the maximum number of additional threads that the builder will spawn by default. This is used for the default value for {@link #_maxAdditionalThreads}. A value of {@code -1} will use the
   * number of processor cores as the default. The number of threads actually used by be different as the {@link DependencyGraphBuilderFactory} may only provide a limited pool to all active graph
   * builders. This is {@code -1} by default (use the number of processor cores) but can be controlled by the {@code DependencyGraphBuilder.maxAdditionalThreads} property.
   */
  private static final int MAX_ADDITIONAL_THREADS = Integer.parseInt(System.getProperty("DependencyGraphBuilder.maxAdditionalThreads", "-1"));

  /**
   * Writes the dependency graph structure (in ASCII) out after each graph build completes. Graphs are written to the user's temporary folder with the name {@code dependencyGraph} and a numeric suffix
   * from the builder's object ID. The default value is off but can be controlled by the {@code DependencyGraphBuilder.dumpDependencyGraph} property.
   */
  private static final boolean DEBUG_DUMP_DEPENDENCY_GRAPH = System.getProperty("DependencyGraphBuilder.dumpDependencyGraph", "FALSE").equalsIgnoreCase("TRUE");

  /**
   * Writes the value requirements that could not be resolved out. Failure information is written to the user's temporary folder with the name {@code resolutionFailure} and a sequential numeric suffix
   * from the builder's object ID. The verbosity of failure information will depend on the {@link #_disableFailureReporting} flag typically controlled by
   * {@link DependencyGraphBuilderFactory#setEnableFailureReporting}. The default value is off but can be controlled by the {@code DependencyGraphBuilder.dumpFailureInfo} property.
   */
  private static final boolean DEBUG_DUMP_FAILURE_INFO = System.getProperty("DependencyGraphBuilder.dumpFailureInfo", "FALSE").equalsIgnoreCase("TRUE");

  /**
   * Controls whether to GZIP the outputs created by {@link #DEBUG_DUMP_DEPENDENCY_GRAPH} and {@link #DEBUG_DUMP_FAILURE_INFO}. The default value is off but can be controlled by the
   * {@code DependencyGraphBuilder.dumpGZIP} property to save disk space and/or I/O overheads when a large volume of debugging data is being generated.
   */
  private static final boolean DEBUG_DUMP_GZIP = System.getProperty("DependencyGraphBuilder.dumpGZIP", "FALSE").equalsIgnoreCase("TRUE");

  /** Profiler for monitoring the {@link #abortLoops} operation. */
  private static final Profiler s_abortLoops = Profiler.create(DependencyGraphBuilder.class, "abortLoops");

  /** The object id of this DependencyGraphBuilder (used in logs) */
  private final int _objectId = s_nextObjectId.incrementAndGet();

  /** The number of active jobs in this instance of DependencyGraphBuilder */
  private final AtomicInteger _activeJobCount = new AtomicInteger();
  /** The set of active jobs in this instance of DependencyGraphBuilder */
  private final Set<Job> _activeJobs = new HashSet<Job>();
  /** The job run queue for this instance of DependencyGraphBuilder */
  private final RunQueue _runQueue;
  /** The deferred job queue for this instance of DependencyGraphBuilder */
  private final Queue<ContextRunnable> _deferredQueue = new ConcurrentLinkedQueue<ContextRunnable>();
  /** The context for building the dep graph in this instance of DependencyGraphBuilder */
  private final GraphBuildingContext _context = new GraphBuildingContext(this);
  /** The number of completed graph building steps in this instance of DependencyGraphBuilder */
  private final AtomicLong _completedSteps = new AtomicLong();
  /** The number of scheduled graph building steps in this instance of DependencyGraphBuilder */
  private final AtomicLong _scheduledSteps = new AtomicLong();
  /** The callback to use for terminal value resolution failure */
  private final GetTerminalValuesCallback _getTerminalValuesCallback = new GetTerminalValuesCallback(DEBUG_DUMP_FAILURE_INFO ? new ResolutionFailurePrinter(new OutputStreamWriter(
      openDebugStream("resolutionFailure"))) : ResolutionFailureVisitor.DEFAULT_INSTANCE);
  /** The job executor */
  private final Executor _executor;
  /**
   * Clears out resolvers from the resolution cache if memory starts getting low. Disable by setting the {@code DependencyGraphBuilder.disableResolutionCacheCleanup} property.
   */
  private final Housekeeper _contextCleaner = System.getProperty("DependencyGraphBuilder.disableResolutionCacheCleanup", "FALSE").equalsIgnoreCase("FALSE") ? Housekeeper.of(this,
      ResolutionCacheCleanup.INSTANCE) : null;
  /** The value requirements still pending in the run queue */
  private final PendingRequirements _pendingRequirements = new PendingRequirements(this);
  /** The name of the calculation configuration */
  private String _calculationConfigurationName;
  /** The market data availability provider for this instance of DependencyGraphBuilder */
  private MarketDataAvailabilityProvider _marketDataAvailabilityProvider;
  /** The function resolver for this instance of DependencyGraphBuilder */
  private CompiledFunctionResolver _functionResolver;
  /** The function compilation context for this instance of DependencyGraphBuilder */
  private FunctionCompilationContext _compilationContext;
  /** The function exclusion groups for this instance of DependencyGraphBuilder */
  private FunctionExclusionGroups _functionExclusionGroups;

  // The resolve task is ref-counted once for the map (it is being used as a set)
  private final ConcurrentMap<ValueRequirement, Map<ResolveTask, ResolveTask>> _requirements = new ConcurrentHashMap<ValueRequirement, Map<ResolveTask, ResolveTask>>();

  /** The number of active resolve tasks for this instance of DependencyGraphBuilder */
  private final AtomicInteger _activeResolveTasks = new AtomicInteger();

  // The resolve task is NOT ref-counted (it is only used for parent comparisons), but the value producer is
  private final ConcurrentMap<ValueSpecification, MapEx<ResolveTask, ResolvedValueProducer>> _specifications = new ConcurrentHashMap<ValueSpecification, MapEx<ResolveTask, ResolvedValueProducer>>();

  /**
   * Number of additional threads to launch while requirements are being added or the graph is being built. The total number of threads used for graph construction may be up to this value or may be
   * one higher as a thread blocked on graph construction in the call to {@link #getDependencyGraph} will join in with the remaining construction.
   */
  private volatile int _maxAdditionalThreads = getDefaultMaxAdditionalThreads();

  /** Flag to indicate when the build has been canceled */
  private boolean _cancelled;

  /**
   * Flag to indicate whether resolution failure information should be reported. Use in conjunction with the failure visitor registered with the terminal value callback to extract feedback. If there
   * is no visitor then suppressing failure reports will reduce the memory footprint of the algorithm as no additional state needs to be maintained.
   */
  private boolean _disableFailureReporting;

  // TODO: We should use an external execution framework rather than the one here; there are far better (and probably more accurate) implementations of
  // the algorithm in other projects I've worked on.

  public static int getDefaultMaxAdditionalThreads() {
    return NO_BACKGROUND_THREADS ? 0 : (MAX_ADDITIONAL_THREADS >= 0) ? MAX_ADDITIONAL_THREADS : Runtime.getRuntime().availableProcessors();
  }

  public static RunQueueFactory getDefaultRunQueueFactory() {
    return RunQueueFactory.getConcurrentLinkedQueue();
  }

  public DependencyGraphBuilder() {
    this(DependencyGraphBuilderFactory.getDefaultExecutor(), getDefaultRunQueueFactory());
  }

  protected DependencyGraphBuilder(final Executor executor, final RunQueueFactory runQueue) {
    _executor = executor;
    _runQueue = runQueue.createRunQueue();
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
  public void setMarketDataAvailabilityProvider(final MarketDataAvailabilityProvider marketDataAvailabilityProvider) {
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
  public void setFunctionResolver(final CompiledFunctionResolver functionResolver) {
    _functionResolver = functionResolver;
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
  public void setCompilationContext(final FunctionCompilationContext compilationContext) {
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

  /**
   * Sets the target digest rules
   * 
   * @param targetDigests the rules, or null to not use target digest rules
   */
  public void setTargetDigests(final TargetDigests targetDigests) {
    getTerminalValuesCallback().setTargetDigests(targetDigests);
  }

  public void setComputationTargetCollapser(final ComputationTargetCollapser computationTargetCollapser) {
    getTerminalValuesCallback().setComputationTargetCollapser(computationTargetCollapser);
  }

  /**
   * Sets whether to disable extended failure reporting when values can't be resolved.
   * 
   * @param disableFailureReporting false to propagate failure information to terminal outputs and report, true to suppress
   */
  public void setDisableFailureReporting(final boolean disableFailureReporting) {
    _disableFailureReporting = disableFailureReporting;
  }

  /**
   * Tests whether extended failure reporting is disabled.
   * 
   * @return true if there is no failure reporting, false otherwise
   */
  public boolean isDisableFailureReporting() {
    return _disableFailureReporting;
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

  protected int getActiveResolveTasks() {
    return _activeResolveTasks.get();
  }

  protected void incrementActiveResolveTasks() {
    _activeResolveTasks.incrementAndGet();
  }

  protected void decrementActiveResolveTasks() {
    _activeResolveTasks.decrementAndGet();
  }

  private static final ComputationTargetTypeVisitor<Void, Boolean> s_isUnionType = new ComputationTargetTypeVisitor<Void, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void unused) {
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void unused) {
      return types.get(types.size() - 1).accept(this, unused);
    }

    @Override
    public Boolean visitNullComputationTargetType(final Void unused) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void unused) {
      return Boolean.FALSE;
    }

  };

  protected ComputationTargetSpecification resolveTargetReference(final ComputationTargetReference reference) {
    ComputationTargetSpecification specification = getCompilationContext().getComputationTargetResolver().getSpecificationResolver().getTargetSpecification(reference);
    if (specification == null) {
      s_logger.warn("Couldn't resolve {}", reference);
      return null;
    }
    if (specification.getType().accept(s_isUnionType, null) == Boolean.TRUE) {
      final ComputationTarget target = getCompilationContext().getComputationTargetResolver().resolve(specification);
      if (target != null) {
        return target.toSpecification();
      } else {
        s_logger.warn("Resolved {} to {} but can't resolve target to eliminate union", reference, specification);
      }
    }
    return specification;
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
    Map<ResolveTask, ResolveTask> tasks = getTasks(valueRequirement);
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
   * Fetches an existing resolution of the given value specification.
   * <p>
   * The {@code valueSpecification} parameter must be normalized.
   * 
   * @param valueSpecification the specification to search for, not null
   * @return the resolved value, or null if not resolved
   */
  protected ResolvedValue getResolvedValue(final ValueSpecification valueSpecification) {
    return _getTerminalValuesCallback.getProduction(valueSpecification);
  }

  protected void addResolvedValue(final ResolvedValue value) {
    _getTerminalValuesCallback.declareProduction(value);
  }

  protected Pair<?, ?> getResolutions(final ComputationTargetSpecification targetSpec, final String valueName) {
    return _getTerminalValuesCallback.getResolutions(getCompilationContext(), targetSpec, valueName);
  }

  /**
   * Sets the listener to receive resolution failures. ResolutionFailureVisitors can also be registered here. If not set, a synthetic exception is created for each failure in the miscellaneous
   * exception set.
   * 
   * @param failureListener the listener to use, or null to create synthetic exceptions
   */
  public void setResolutionFailureListener(final ResolutionFailureListener failureListener) {
    getTerminalValuesCallback().setFailureListener(failureListener);
  }

  /**
   * Check that the market data availability provider, the function resolver and the calc config name are non-null
   */
  protected void checkInjectedInputs() {
    ArgumentChecker.notNullInjected(getMarketDataAvailabilityProvider(), "marketDataAvailabilityProvider");
    ArgumentChecker.notNullInjected(getFunctionResolver(), "functionResolver");
    ArgumentChecker.notNullInjected(getCalculationConfigurationName(), "calculationConfigurationName");
  }

  /**
   * Adds resolution of the given requirement to the run queue. Resolution will start as soon as possible and be available as pending for any tasks already running that require resolution of the
   * requirement.
   * <p>
   * This may not be called concurrently because of the way the root/global context gets used. Everything that calls it should hold the {@link #getContext} monitor so nothing else can use the context
   * - this monitor also serves as the build complete lock.
   * 
   * @param requirement the requirement to resolve
   */
  private void addTargetImpl(final ValueRequirement requirement) {
    final ResolvedValueProducer resolvedValue = getContext().resolveRequirement(requirement, null, null);
    resolvedValue.addCallback(getContext(), getTerminalValuesCallback());
    _pendingRequirements.add(getContext(), resolvedValue);
    resolvedValue.release(getContext());
  }

  // --------------------------------------------------------------------------

  /**
   * Passes a previously constructed dependency graph to the builder as part of an incremental build. The nodes from the graph will be adopted by the builder and may be returned in the graph that this
   * eventually produces. This should be called before adding any targets to the build.
   * 
   * @param graph the result of a previous graph build
   */
  public void setDependencyGraph(final DependencyGraph graph) {
    _getTerminalValuesCallback.populateState(graph, getCompilationContext());
  }

  /**
   * Adds a target requirement to the graph. The requirement is queued and the call returns; construction of the graph will happen on a background thread (if additional threads is non-zero), or when
   * the call to {@link #getDependencyGraph} is made. If it was not possible to satisfy the requirement that must be checked after graph construction is complete.
   * <p>
   * The caller must ensure that the same requirement is not passed multiple times to the builder. Depending on scheduling and memory availability, the cases may be identified and coalesced (by
   * {@link GraphBuildingContext#resolveRequirement}) into a single logical operation. Alternatively the resolutions may run to completion to include terminal outputs in the result. If the function
   * library contains an ambiguity or other aspect that means the resolved value specification could differ this will result in an invalid dependency graph.
   * 
   * @param requirement requirement to add, not null
   */
  public void addTarget(final ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");

    // Check that the market data availability provider, the function resolver and the calc config name are non-null
    checkInjectedInputs();

    // Use the context as a build complete lock so that housekeeping thread cannot observe a "built" state within this atomic block of work
    synchronized (getContext()) {
      // Add the value requirement to the graph (actually adds a suitable resolution task to the run queue)
      addTargetImpl(requirement);
    }
    // If the run-queue was empty, we won't have started a thread, so double check
    startBackgroundConstructionJob();
  }

  // TODO [PLAT-2286] When compiling a view, ask for the most complex form of all requirements (e.g. PORTFOLIO_NODE/PORTFOLIO_NODE/POSITION) and be prepared for the function resolution stage
  // to adjust this down to more specific forms. As part of this reduction, the requirement may end up as something we've already resolved, allowing values to be shared.

  /**
   * Adds target requirements to the graph. The requirements are queued and the call returns; construction of the graph will happen on a background thread (if additional threads is non-zero), or when
   * the call to {@link #getDependencyGraph} is made. If it was not possible to satisfy one or more requirements that must be checked after graph construction is complete.
   * <p>
   * The caller must ensure that the same requirement is not passed multiple times to the builder. Depending on scheduling and memory availability, the cases may be identified and coalesced (by
   * {@link GraphBuildingContext#resolveRequirement}) into a single logical operation. Alternatively the resolutions may run to completion to include terminal outputs in the result. If the function
   * library contains an ambiguity or other aspect that means the resolved value specification could differ this will result in an invalid dependency graph.
   * 
   * @param requirements requirements to add, not null and not containing nulls.
   */
  public void addTarget(final Collection<ValueRequirement> requirements) {
    ArgumentChecker.noNulls(requirements, "requirements");

    // Check that the market data availability provider, the function resolver and the calc config name are non-null
    checkInjectedInputs();

    // Use the context as a build complete lock so that housekeeping thread cannot observe a "built" state within this atomic block of work
    synchronized (getContext()) {
      for (final ValueRequirement requirement : requirements) {
        addTargetImpl(requirement);
      }
    }
    // If the run-queue was empty, we may not have started enough threads, so double check
    startBackgroundConstructionJob();
  }

  /**
   * Add a task to the run queue, increment the count of scheduled steps, and start/wake up a background thread if the run queue was empty, as this indicates that there are probably no active threads
   * at this precise moment.
   * 
   * @param runnable the task to add to the run queue
   */
  protected void addToRunQueue(final ContextRunnable runnable) {

    // Check if the run queue is empty
    final boolean dontSpawn = _runQueue.isEmpty();

    // Increment the number of scheduled steps
    _scheduledSteps.incrementAndGet();

    // Actually add the task to this DependencyGraphBuilder's run queue
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
    s_logger.debug("Checking for tasks to abort");
    s_abortLoops.begin();
    try {
      final Collection<ResolveTask> toCheck = new ArrayList<ResolveTask>();
      for (final MapEx<ResolveTask, ResolvedValueProducer> tasks : _specifications.values()) {
        synchronized (tasks) {
          if (!tasks.containsKey(null)) {
            toCheck.addAll((Collection<ResolveTask>) tasks.keySet());
          }
        }
      }
      for (final Map<ResolveTask, ResolveTask> tasks : _requirements.values()) {
        synchronized (tasks) {
          if (!tasks.containsKey(null)) {
            toCheck.addAll(tasks.keySet());
          }
        }
      }
      final GraphBuildingContext context = new GraphBuildingContext(this);
      int cancelled = 0;
      final Map<Chain, Chain.LoopState> checked = Maps.newHashMapWithExpectedSize(toCheck.size());
      for (ResolveTask task : toCheck) {
        cancelled += task.cancelLoopMembers(context, checked);
      }
      getContext().mergeThreadContext(context);
      if (s_logger.isInfoEnabled()) {
        if (cancelled > 0) {
          s_logger.info("Cancelled {} looped task(s)", cancelled);
        } else {
          s_logger.info("No looped tasks to cancel");
        }
      }
    } finally {
      s_abortLoops.end();
    }
  }

  // TODO: Might want to do the abort loops operation as part of a cache flush for long running views to detect the loops because
  // they are eating the memory before we run out of useful work to do.

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
      s_logger.debug("Building job {} started for {}", _objectId, DependencyGraphBuilder.this);
      if (_contextCleaner != null) {
        _contextCleaner.start();
      }
      boolean jobsLeftToRun;
      int completed = 0;

      // Repeat for as long as there are jobs left to run, and not poisoned
      do {
        // Create a new context for each logical block so that an exception from the build won't leave us with
        // an inconsistent context.
        final GraphBuildingContext context = new GraphBuildingContext(DependencyGraphBuilder.this);

        // Repeat for as long as there are jobs left to run, and not poisoned
        do {
          // Run a graph building job
          try {
            jobsLeftToRun = buildGraph(context);
            completed++;
          } catch (final Throwable t) {
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

      // Context is used as a build complete lock
      synchronized (getContext()) {
        final boolean abortLoops;
        synchronized (_activeJobs) {
          _activeJobs.remove(this);
          abortLoops = _activeJobs.isEmpty() && _runQueue.isEmpty() && _deferredQueue.isEmpty();
        }

        if (abortLoops) {
          // Any tasks that are still active have created a reciprocal loop disjoint from the runnable
          // graph of tasks. Aborting them at this point is easier and possibly more efficient than
          // the overhead of trying to stop the loops forming in the first place.
          abortLoops();
          // If any loops were aborted, new jobs will go onto the run queue, possibly a new active job
          // started. We are officially "dead"; another worker thread may become active
        }
      }
      s_logger.debug("Building job {} stopped after {} operations", _objectId, completed);
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
    ContextRunnable task = _runQueue.take();
    if (task == null) {
      task = _deferredQueue.poll();
      if (task == null) {
        // Nothing runnable and nothing deferred
        return false;
      }
    }
    if (!task.tryRun(context)) {
      // A concurrency limit was hit. Post the job into the contention buffer and try and take another from the run queue.
      do {
        _deferredQueue.add(task);
        task = _runQueue.take();
        if (task == null) {
          // Nothing runnable. Abort as we can't just add the deferred items or we might end up spinning
          return false;
        }
      } while (!task.tryRun(context));
    }
    // Reschedule a deferred item. There may be multiple deferred items, but we only release them one at a time as other jobs complete
    // which may resolve the contention. If the run queue becomes empty they will be taken directly by the code above.
    task = _deferredQueue.poll();
    if (task != null) {
      addToRunQueue(task);
    }
    _completedSteps.incrementAndGet();
    return true;
  }

  /**
   * Tests if the graph has been built or if work is still required. Graphs are only built in the background if additional threads is set to non-zero.
   * 
   * @return true if the graph has been built, false if it is outstanding
   * @throws CancellationException if the graph build has been canceled
   */
  public boolean isGraphBuilt() {
    // Context is used as the build complete lock
    synchronized (getContext()) {
      synchronized (_activeJobs) {
        if (_cancelled) {
          throw new CancellationException();
        }
        return _activeJobs.isEmpty() && _runQueue.isEmpty() && _deferredQueue.isEmpty();
      }
    }
  }

  /**
   * Returns the top-level value requirements currently being resolved.
   * 
   * @return the value requirements
   */
  public Collection<ValueRequirement> getOutstandingResolutions() {
    return new ArrayList<ValueRequirement>(_pendingRequirements.getValueRequirements());
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
      for (final Job job : _activeJobs) {
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

  protected boolean isGraphBuilt(final boolean allowBackgroundContinuation) throws InterruptedException {
    if (!isGraphBuilt()) {
      s_logger.info("Building dependency graph");
      do {
        final Job job = createConstructionJob();
        _activeJobCount.incrementAndGet();
        synchronized (_activeJobs) {
          if (!_cancelled) {
            _activeJobs.add(job);
          } else {
            throw new CancellationException();
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
          // Nothing in the queue for us so take a nap. There are background threads running and maybe items on the deferred queue.
          s_logger.info("Waiting for background threads");
          Thread.sleep(100);
        } else {
          return false;
        }
      } while (!isGraphBuilt());
    }
    return true;
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
    try {
      if (!isGraphBuilt(allowBackgroundContinuation)) {
        return null;
      }
    } catch (final InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    return createDependencyGraph();
  }

  /**
   * Blocks the caller until {@link #getDependencyGraph} is able to return without blocking. This can be used to build large graphs by submitting requirements in batches and waiting for each batch to
   * complete. This will reduce the amount of working memory required during the build if the fragments are sufficiently disjoint.
   */
  public void waitForDependencyGraphBuild() throws InterruptedException {
    isGraphBuilt(true);
  }

  /**
   * Flushes data that is unlikely to be needed again from the resolution caches. Anything discarded will either never be needed again for any pending resolutions, or is a cached production that can
   * be recalculated if necessary. Discards can be a multiple stage process - repeated calls all the while this function returns true must be used to flush all possible state and make as much memory
   * available as possible for the garbage collector.
   * 
   * @return true if one or more states were discarded, false if there was nothing that can be discarded
   */
  @SuppressWarnings("unchecked")
  protected boolean flushCachedStates() {
    // TODO: use heuristics to throw data away more sensibly (e.g. LRU)
    int removed = 0;
    final List<ResolvedValueProducer> discards = new ArrayList<ResolvedValueProducer>();
    GraphBuildingContext context = null;
    final Iterator<MapEx<ResolveTask, ResolvedValueProducer>> itrSpecifications = _specifications.values().iterator();
    while (itrSpecifications.hasNext()) {
      final MapEx<ResolveTask, ResolvedValueProducer> producers = itrSpecifications.next();
      synchronized (producers) {
        if (producers.containsKey(null)) {
          continue;
        }
        final Iterator<ResolvedValueProducer> itrProducer = producers.values().iterator();
        while (itrProducer.hasNext()) {
          final ResolvedValueProducer producer = itrProducer.next();
          if (producer.getRefCount() == 1) {
            discards.add(producer);
            itrProducer.remove();
            removed++;
          }
        }
        if (producers.isEmpty()) {
          itrSpecifications.remove();
          producers.put(null, null);
        }
      }
      if (!discards.isEmpty()) {
        if (context == null) {
          context = new GraphBuildingContext(this);
        }
        for (final ResolvedValueProducer discard : discards) {
          discard.release(context);
        }
        discards.clear();
      }
    }
    // Unfinished resolveTasks will be removed from the _requirements cache when their refCount hits 1 (the cache only). Finished
    // ones are kept, but should be released when we are low on memory.
    final Iterator<Map<ResolveTask, ResolveTask>> itrRequirements = _requirements.values().iterator();
    while (itrRequirements.hasNext()) {
      final Map<ResolveTask, ResolveTask> tasks = itrRequirements.next();
      synchronized (tasks) {
        if (tasks.containsKey(null)) {
          continue;
        }
        final Iterator<ResolveTask> itrTask = tasks.keySet().iterator();
        while (itrTask.hasNext()) {
          final ResolveTask task = itrTask.next();
          if (task.getRefCount() == 1) {
            discards.add(task);
            itrTask.remove();
            removed++;
          }
        }
        if (tasks.isEmpty()) {
          itrRequirements.remove();
          tasks.put(null, null);
        }
      }
      if (!discards.isEmpty()) {
        if (context == null) {
          context = new GraphBuildingContext(this);
        }
        for (final ResolvedValueProducer discard : discards) {
          discard.release(context);
        }
        discards.clear();
      }
    }
    if (context != null) {
      getContext().mergeThreadContext(context);
    }
    if (s_logger.isInfoEnabled()) {
      if (removed > 0) {
        s_logger.info("Discarded {} production task(s)", removed);
      } else {
        s_logger.info("No production tasks to discard");
      }
    }
    return removed > 0;
  }

  protected void reportStateSize() {
    _getTerminalValuesCallback.reportStateSize();
    if (!s_logger.isInfoEnabled()) {
      return;
    }
    int count = 0;
    for (final Map<ResolveTask, ResolveTask> entries : _requirements.values()) {
      synchronized (entries) {
        count += entries.size();
      }
    }
    s_logger.info("Requirements cache = {} tasks for {} requirements", count, _requirements.size());
    count = 0;
    for (final MapEx<ResolveTask, ResolvedValueProducer> entries : _specifications.values()) {
      synchronized (entries) {
        count += entries.size();
      }
    }
    s_logger.info("Specifications cache = {} tasks for {} specifications", count, _specifications.size());
    s_logger.info("Pending requirements = {}", _pendingRequirements.getValueRequirements().size());
    s_logger.info("Run queue length = {}, deferred queue length = {}", _runQueue.size(), _deferredQueue.size());
  }

  protected DependencyGraph createDependencyGraph() {
    final Pair<Collection<DependencyNode>, Integer> nodes = getTerminalValuesCallback().getGraphRootNodes();
    final DependencyGraphImpl graph = new DependencyGraphImpl(getCalculationConfigurationName(), nodes.getFirst(), nodes.getSecond(), getTerminalValuesCallback()
        .getTerminalValuesBySpecification());
    if (DEBUG_DUMP_DEPENDENCY_GRAPH) {
      final PrintStream ps = openDebugStream("dependencyGraph");
      ps.println("Configuration = " + getCalculationConfigurationName());
      DependencyGraphImpl.dumpStructureASCII(graph, ps);
      ps.close();
    }
    s_logger.info("{} built after {} steps", graph, _completedSteps);
    // Help out the GC
    _requirements.clear();
    _specifications.clear();
    return graph;
  }

  // --------------------------------------------------------------------------

  protected PrintStream openDebugStream(final String name) {
    try {
      String fileName = System.getProperty("java.io.tmpdir") + File.separatorChar + name + _objectId + ".txt";
      if (DEBUG_DUMP_GZIP) {
        fileName = fileName + ".gz";
      }
      OutputStream output = new FileOutputStream(fileName);
      if (DEBUG_DUMP_GZIP) {
        output = new GZIPOutputStream(output);
      }
      return new PrintStream(output);
    } catch (final IOException e) {
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
