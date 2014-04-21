/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.codahale.metrics.Timer;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioNodeEquivalenceMapper;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.depgraph.impl.RootDiscardingSubgrapher;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelectionGraphManipulator;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.NoOpMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinitionFactory;
import com.opengamma.engine.marketdata.manipulator.ScenarioParameters;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.compilation.IllegalCompilationStateException;
import com.opengamma.engine.view.compilation.InvalidTargetDependencyNodeFilter;
import com.opengamma.engine.view.compilation.PartiallyCompiledGraph;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.cycle.DefaultViewCycleMetadata;
import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.cycle.ViewCycleState;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessContext;
import com.opengamma.engine.view.listener.ComputationResultListener;
import com.opengamma.engine.view.worker.cache.ViewExecutionCacheKey;
import com.opengamma.engine.view.worker.trigger.CombinedViewCycleTrigger;
import com.opengamma.engine.view.worker.trigger.FixedTimeTrigger;
import com.opengamma.engine.view.worker.trigger.RecomputationPeriodTrigger;
import com.opengamma.engine.view.worker.trigger.RunAsFastAsPossibleTrigger;
import com.opengamma.engine.view.worker.trigger.SuccessiveDeltaLimitTrigger;
import com.opengamma.engine.view.worker.trigger.ViewCycleEligibility;
import com.opengamma.engine.view.worker.trigger.ViewCycleTrigger;
import com.opengamma.engine.view.worker.trigger.ViewCycleTriggerResult;
import com.opengamma.engine.view.worker.trigger.ViewCycleType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.id.VersionCorrectionUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.metric.OpenGammaMetricRegistry;
import com.opengamma.util.tuple.Pair;

/**
 * The job which schedules and executes computation cycles for a view process. See {@link SingleThreadViewProcessWorkerFactory} for a more detailed description.
 */
public class SingleThreadViewProcessWorker implements ViewProcessWorker, MarketDataChangeListener {

  /**
   * Default to waiting 5 minutes when {link {@link ViewExecutionFlags#AWAIT_MARKET_DATA} is in use to avoid unintentionally causing the view process to hang indefinitely. Market data should normally
   * be available in seconds.
   */
  private static final long DEFAULT_MARKET_DATA_TIMEOUT_MILLIS = 300000;

  private static final Logger s_logger = LoggerFactory.getLogger(SingleThreadViewProcessWorker.class);

  private static final ExecutorService s_executor = NamedThreadPoolFactory.newCachedThreadPool("Worker");

  /**
   * Wrapper that allows a thread to be "borrowed" from an executor service.
   */
  /* package*/static final class BorrowedThread implements Runnable {

    private final String _name;

    private final Runnable _job;
    private final CountDownLatch _join = new CountDownLatch(1);
    private Thread _thread;
    private String _originalName;

    public BorrowedThread(final String name, final Runnable job) {
      _name = name;
      _job = job;
    }

    public synchronized Thread.State getState() {
      if (_thread != null) {
        return _thread.getState();
      } else {
        return (_originalName != null) ? Thread.State.TERMINATED : Thread.State.NEW;
      }
    }

    public void join() throws InterruptedException {
      _join.await();
    }

    public void join(long timeout) throws InterruptedException {
      _join.await(timeout, TimeUnit.MILLISECONDS);
    }

    public synchronized void interrupt() {
      if (_thread != null) {
        _thread.interrupt();
      }
    }

    public synchronized boolean isAlive() {
      return _thread != null;
    }

    // Runnable

    @Override
    public void run() {
      synchronized (this) {
        _thread = Thread.currentThread();
        _originalName = _thread.getName();
      }
      try {
        _thread.setName(_originalName + "-" + _name);
        _job.run();
      } finally {
        _thread.setName(_originalName);
        synchronized (this) {
          _thread = null;
        }
        _join.countDown();
      }
    }

  }

  private static final long NANOS_PER_MILLISECOND = 1000000;

  private final ViewProcessWorkerContext _context;

  private final ViewExecutionOptions _executionOptions;
  private final CombinedViewCycleTrigger _masterCycleTrigger = new CombinedViewCycleTrigger();
  private final FixedTimeTrigger _compilationExpiryCycleTrigger;
  private final boolean _executeCycles;
  private final boolean _executeGraphs;
  private final boolean _ignoreCompilationValidity;
  private final boolean _suppressExecutionOnNoMarketData;
  /**
   * The changes to the master trigger that must be made during the next cycle.
   * <p>
   * This has been added as an immediate fix for [PLAT-3291] but could be extended to represent an arbitrary change to add/remove triggers if we wish to support the execution options changing for a
   * running worker.
   */
  private ViewCycleTrigger _masterCycleTriggerChanges;

  private int _cycleCount;

  private EngineResourceReference<SingleComputationCycle> _previousCycleReference;
  /**
   * The current view definition the worker must calculate on.
   */
  private ViewDefinition _viewDefinition;

  /**
   * The most recently compiled form of the view definition. This may have been compiled by this worker, or retrieved from the cache and is being reused.
   */
  private CompiledViewDefinitionWithGraphs _latestCompiledViewDefinition;

  /**
   * The key to use for storing the compiled view definition, or querying it, from the cache shared with other workers. Whenever the market data provider or view definition changes, this must be
   * updated.
   */
  private ViewExecutionCacheKey _executionCacheKey;

  private TargetResolverChangeListener _targetResolverChanges;

  private volatile boolean _wakeOnCycleRequest;

  private volatile boolean _cycleRequested;
  private volatile boolean _forceTriggerCycle;

  /**
   * An updated view definition pushed in by the execution coordinator. When the next cycle runs, this should be used instead of the previous one.
   */
  private final AtomicReference<ViewDefinition> _newViewDefinition = new AtomicReference<>();

  private volatile Future<CompiledViewDefinitionWithGraphsImpl> _compilationTask;

  /**
   * Total time the job has spent "working". This does not include time spent waiting for a trigger. It is a real time spent on all I/O involved in a cycle (e.g. database accesses), graph compilation,
   * market data subscription, graph execution, result dispatch, etc.
   */
  private double _totalTimeNanos;

  /**
   * The terminatable job wrapper.
   */
  private final TerminatableJob _job;

  /**
   * The thread running this job.
   */
  private final BorrowedThread _thread;

  /**
   * The manipulator for structured market data.
   */
  private MarketDataSelectionGraphManipulator _marketDataSelectionGraphManipulator;

  /**
   * The market data selectors and function parameters which have been passed in via the ViewDefinition, which are applicable to a specific dependency graph. There will be an entry for each graph in
   * the view, even if the only contents are an empty map.
   */
  private Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> _specificMarketDataSelectors;

  private final MarketDataManager _marketDataManager;

  /**
   * Keep track of the number of market data managers created as we need to ensure they each have a unique name (for JMX registration).
   */
  private static final ConcurrentMap<String, AtomicInteger> s_mdmCount = new ConcurrentHashMap<String, AtomicInteger>();

  /**
   * Timer to track delta cycle execution time.
   */
  private Timer _deltaCycleTimer;
  /**
   * Timer to track full cycle execution time.
   */
  private Timer _fullCycleTimer;

  /**
   * An invalidation call is made by the market data layer to request that a full graph rebuild take place on the next cycle. This is to allow for resolutions that might differ because data
   * availability has changed.
   */
  private final AtomicBoolean _forceGraphRebuild = new AtomicBoolean();

  public SingleThreadViewProcessWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    _context = context;
    _executionOptions = executionOptions;
    _cycleRequested = !executionOptions.getFlags().contains(ViewExecutionFlags.WAIT_FOR_INITIAL_TRIGGER);
    _compilationExpiryCycleTrigger = new FixedTimeTrigger();
    addMasterCycleTrigger(_compilationExpiryCycleTrigger);
    if (executionOptions.getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_TIME_ELAPSED)) {
      addMasterCycleTrigger(new RecomputationPeriodTrigger(new Supplier<ViewDefinition>() {
        @Override
        public ViewDefinition get() {
          return getViewDefinition();
        }
      }));
    }
    if (executionOptions.getMaxSuccessiveDeltaCycles() != null) {
      addMasterCycleTrigger(new SuccessiveDeltaLimitTrigger(executionOptions.getMaxSuccessiveDeltaCycles()));
    }
    if (executionOptions.getFlags().contains(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE)) {
      if (_cycleRequested) {
        addMasterCycleTrigger(new RunAsFastAsPossibleTrigger());
      } else {
        // Defer the trigger until an initial one has happened
        _masterCycleTriggerChanges = new RunAsFastAsPossibleTrigger();
      }
    }
    _executeCycles = !executionOptions.getFlags().contains(ViewExecutionFlags.COMPILE_ONLY);
    _executeGraphs = !executionOptions.getFlags().contains(ViewExecutionFlags.FETCH_MARKET_DATA_ONLY);
    _suppressExecutionOnNoMarketData = executionOptions.getFlags().contains(ViewExecutionFlags.SKIP_CYCLE_ON_NO_MARKET_DATA);
    _ignoreCompilationValidity = executionOptions.getFlags().contains(ViewExecutionFlags.IGNORE_COMPILATION_VALIDITY);
    _viewDefinition = viewDefinition;
    _specificMarketDataSelectors = extractSpecificSelectors(viewDefinition);
    _marketDataManager = createMarketDataManager(context);
    _marketDataSelectionGraphManipulator = createMarketDataManipulator(_executionOptions.getDefaultExecutionOptions(), _specificMarketDataSelectors);
    _job = new Job();
    _thread = new BorrowedThread(context.toString(), _job);
    _deltaCycleTimer = OpenGammaMetricRegistry.getSummaryInstance().timer("SingleThreadViewProcessWorker.cycle.delta");
    _fullCycleTimer = OpenGammaMetricRegistry.getSummaryInstance().timer("SingleThreadViewProcessWorker.cycle.full");
    s_executor.submit(_thread);
  }

  private MarketDataManager createMarketDataManager(ViewProcessWorkerContext context) {
    String processId = context.getProcessContext().getProcessId().getValue();
    AtomicInteger currentEntry = s_mdmCount.putIfAbsent(processId, new AtomicInteger());
    if (currentEntry == null) {
      currentEntry = s_mdmCount.get(processId);
    }
    int newCount = currentEntry.incrementAndGet();
    // TODO - the hardcoded main should really be derived from a view process name if one were available
    return new MarketDataManager(this, getProcessContext().getMarketDataProviderResolver(), "main", processId + "-" + newCount);
  }

  /**
   * We can pickup market data manipulators from either the default execution context or from the view definition. Those from the execution context will have their function parameters specified within
   * the execution options as well (either per cycle or default). Manipulators from the view def will have function params specified alongside them.
   * 
   * @param executionOptions the execution options to get the selectors from
   * @param specificSelectors the graph-specific selectors
   * @return a market data manipulator combined those found in the execution context and the view defintion
   */
  private MarketDataSelectionGraphManipulator createMarketDataManipulator(ViewCycleExecutionOptions executionOptions,
      Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> specificSelectors) {

    MarketDataSelector executionOptionsMarketDataSelector = executionOptions != null ? executionOptions.getMarketDataSelector() : NoOpMarketDataSelector.getInstance();

    return new MarketDataSelectionGraphManipulator(executionOptionsMarketDataSelector, specificSelectors);
  }

  private Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> extractSpecificSelectors(ViewDefinition viewDefinition) {

    ConfigSource configSource = getProcessContext().getConfigSource();
    Collection<ViewCalculationConfiguration> calculationConfigurations = viewDefinition.getAllCalculationConfigurations();

    Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> specificSelectors = new HashMap<>();

    for (ViewCalculationConfiguration calcConfig : calculationConfigurations) {

      UniqueId scenarioId = calcConfig.getScenarioId();
      UniqueId scenarioParametersId = calcConfig.getScenarioParametersId();
      if (scenarioId != null) {
        ScenarioDefinitionFactory scenarioDefinitionFactory = configSource.getConfig(ScenarioDefinitionFactory.class, scenarioId);
        Map<String, Object> parameters;
        if (scenarioParametersId != null) {
          ScenarioParameters scenarioParameters = configSource.getConfig(ScenarioParameters.class, scenarioParametersId);
          parameters = scenarioParameters.getParameters();
        } else {
          parameters = null;
        }
        ScenarioDefinition scenarioDefinition = scenarioDefinitionFactory.create(parameters);
        specificSelectors.put(calcConfig.getName(), new HashMap<>(scenarioDefinition.getDefinitionMap()));
      } else {
        // Ensure we have an entry for each graph, even if selectors are empty
        specificSelectors.put(calcConfig.getName(), ImmutableMap.<DistinctMarketDataSelector, FunctionParameters>of());
      }
    }
    return specificSelectors;
  }

  private ViewProcessWorkerContext getWorkerContext() {
    return _context;
  }

  private ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  private ViewProcessContext getProcessContext() {
    return getWorkerContext().getProcessContext();
  }

  private ViewCycleTrigger getMasterCycleTrigger() {
    return _masterCycleTrigger;
  }

  private void addMasterCycleTrigger(final ViewCycleTrigger trigger) {
    _masterCycleTrigger.addTrigger(trigger);
  }

  public FixedTimeTrigger getCompilationExpiryCycleTrigger() {
    return _compilationExpiryCycleTrigger;
  }

  protected BorrowedThread getThread() {
    return _thread;
  }

  protected TerminatableJob getJob() {
    return _job;
  }

  private final class Job extends TerminatableJob {

    @Override
    protected void preStart() {
      _marketDataManager.start();
    }

    /**
     * Determines whether to run, and runs if required, a single computation cycle using the following rules:
     * <ul>
     * <li>A computation cycle can only be triggered if the relevant minimum computation period has passed since the start of the previous cycle.
     * <li>A computation cycle will be forced if the relevant maximum computation period has passed since the start of the previous cycle.
     * <li>A full computation is preferred over a delta computation if both are possible.
     * <li>Performing a full computation also updates the times to the next delta computation; i.e. a full computation is considered to be as good as a delta.
     * </ul>
     */
    @Override
    protected void runOneCycle() {
      // Exception handling is important here to ensure that computation jobs do not just die quietly while consumers are
      // potentially blocked, waiting for results.
      ViewCycleType cycleType;
      try {
        cycleType = waitForNextCycle();
      } catch (final InterruptedException e) {
        s_logger.debug("Interrupted during wait");
        return;
      }
      ViewCycleExecutionOptions executionOptions = null;
      try {
        if (!getExecutionOptions().getExecutionSequence().isEmpty()) {
          executionOptions = getExecutionOptions().getExecutionSequence().poll(getExecutionOptions().getDefaultExecutionOptions());
          s_logger.debug("Next cycle execution options: {}", executionOptions);
        }
        if (executionOptions == null) {
          s_logger.info("No more view cycle execution options");
          jobCompleted();
          return;
        }
      } catch (final Exception e) {
        s_logger.error("Error obtaining next view cycle execution options from sequence for " + getWorkerContext(), e);
        return;
      }

      if (executionOptions.getMarketDataSpecifications().isEmpty()) {
        s_logger.error("No market data specifications for cycle");
        cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("No market data specifications for cycle"));
        return;
      }

      SnapshotManager snapshotManager;

      try {
        snapshotManager = _marketDataManager.createSnapshotManagerForCycle(getViewDefinition().getMarketDataUser(), executionOptions.getMarketDataSpecifications());
        _executionCacheKey = ViewExecutionCacheKey.of(getViewDefinition(), _marketDataManager.getAvailabilityProvider(), _marketDataSelectionGraphManipulator);
      } catch (final Exception e) {
        s_logger.error("Error with market data provider", e);
        cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("Error with market data provider", e));
        return;
      }

      Instant compilationValuationTime;
      try {
        if (executionOptions.getValuationTime() != null) {
          compilationValuationTime = executionOptions.getValuationTime();
        } else {
          // Neither the cycle-specific options nor the defaults have overridden the valuation time so use the time
          // associated with the market data snapshot. To avoid initialising the snapshot perhaps before the required
          // inputs are known or even subscribed to, only ask for an indication at the moment.
          compilationValuationTime = snapshotManager.getSnapshotTimeIndication();
          if (compilationValuationTime == null) {
            throw new OpenGammaRuntimeException("Market data snapshot " + snapshotManager + " produced a null indication of snapshot time");
          }
        }
      } catch (final Exception e) {
        s_logger.error("Error obtaining compilation valuation time", e);
        cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("Error obtaining compilation valuation time", e));
        return;
      }

      final VersionCorrection versionCorrection = getResolverVersionCorrection(executionOptions);
      VersionCorrectionUtils.lock(versionCorrection);
      try {
        final CompiledViewDefinitionWithGraphs compiledViewDefinition;
        try {
          // Don't query the cache so that the process gets a "compiled" message even if a cached compilation is used
          final CompiledViewDefinitionWithGraphs previous = _latestCompiledViewDefinition;
          if (_ignoreCompilationValidity && (previous != null) && CompiledViewDefinitionWithGraphsImpl.isValidFor(previous, compilationValuationTime)) {
            compiledViewDefinition = previous;
          } else {
            compiledViewDefinition = getCompiledViewDefinition(compilationValuationTime, versionCorrection);
            if (compiledViewDefinition == null) {
              s_logger.info("Job terminated during view compilation");
              return;
            }
            if ((previous == null) || !previous.getCompilationIdentifier().equals(compiledViewDefinition.getCompilationIdentifier())) {
              if (_targetResolverChanges != null) {
                // We'll try to register for changes that will wake us up for a cycle if market data is not ticking
                if (previous != null) {
                  final Set<UniqueId> subscribedIds = new HashSet<>(previous.getResolvedIdentifiers().values());
                  for (UniqueId uid : compiledViewDefinition.getResolvedIdentifiers().values()) {
                    if (!subscribedIds.contains(uid)) {
                      _targetResolverChanges.watch(uid.getObjectId());
                    }
                  }
                } else {
                  for (UniqueId uid : compiledViewDefinition.getResolvedIdentifiers().values()) {
                    _targetResolverChanges.watch(uid.getObjectId());
                  }
                }
              }
              viewDefinitionCompiled(compiledViewDefinition);
              // [PLAT-3244] If the definition has been compiled and the graph changed then don't attempt a delta cycle. The delta
              // calculator is flawed. If that gets fixed instead (see Jira comments) then don't reset the cycleType here.
              cycleType = ViewCycleType.FULL;
            }
          }
        } catch (final Exception e) {
          final String message = MessageFormat.format("Error obtaining compiled view definition {0} for time {1} at version-correction {2}", getViewDefinition().getUniqueId(),
              compilationValuationTime, versionCorrection);
          s_logger.error(message);
          cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException(message, e));
          return;
        }
        // [PLAT-1174] This is necessary to support global injections by ValueRequirement. The use of a process-context level variable will be bad
        // if there are multiple worker threads that initialise snapshots concurrently.
        getProcessContext().getLiveDataOverrideInjector().setComputationTargetResolver(
            getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver().atVersionCorrection(versionCorrection));

        try {
          snapshotManager.addMarketDataRequirements(compiledViewDefinition.getMarketDataRequirements());
          if (getExecutionOptions().getFlags().contains(ViewExecutionFlags.AWAIT_MARKET_DATA)) {
            long timeoutMillis = getExecutionOptions().getMarketDataTimeoutMillis() != null ? getExecutionOptions().getMarketDataTimeoutMillis() : DEFAULT_MARKET_DATA_TIMEOUT_MILLIS;
            snapshotManager.initialiseSnapshotWithSubscriptionResults(timeoutMillis);
          } else {
            snapshotManager.initialiseSnapshot();
          }
          if (executionOptions.getValuationTime() == null) {
            executionOptions = executionOptions.copy().setValuationTime(snapshotManager.getSnapshotTime()).create();
          }
        } catch (final Exception e) {
          s_logger.error("Error initializing snapshot {}", snapshotManager);
          cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("Error initializing snapshot " + snapshotManager, e));
        }

        if (_executeCycles) {
          EngineResourceReference<SingleComputationCycle> cycleReference;
          try {
            cycleReference = createCycle(executionOptions, compiledViewDefinition, versionCorrection);
          } catch (final Exception e) {
            s_logger.error("Error creating next view cycle for " + getWorkerContext(), e);
            return;
          }
          try {
            try {
              final SingleComputationCycle singleComputationCycle = cycleReference.get();
              final Map<String, Collection<ComputationTargetSpecification>> configToComputationTargets = new HashMap<>();
              final Map<String, Map<ValueSpecification, Set<ValueRequirement>>> configToTerminalOutputs = new HashMap<>();
              final MarketDataSnapshot marketDataSnapshot = snapshotManager.getSnapshot();

              for (DependencyGraphExplorer graphExp : compiledViewDefinition.getDependencyGraphExplorers()) {
                configToComputationTargets.put(graphExp.getCalculationConfigurationName(), graphExp.getComputationTargets());
                configToTerminalOutputs.put(graphExp.getCalculationConfigurationName(), graphExp.getTerminalOutputs());
              }
              if (isTerminated()) {
                return;
              }
              cycleStarted(new DefaultViewCycleMetadata(cycleReference.get().getUniqueId(), marketDataSnapshot.getUniqueId(), compiledViewDefinition.getViewDefinition().getUniqueId(),
                  versionCorrection, executionOptions.getValuationTime(), singleComputationCycle.getAllCalculationConfigurationNames(), configToComputationTargets, configToTerminalOutputs,
                  executionOptions.getName()));
              if (isTerminated()) {
                return;
              }
              // We may have started the cycle without setting up market data subscriptions, so we
              // now need to set them up so that the data will start to be populated in future cycles
              snapshotManager.requestSubscriptions();
              executeViewCycle(cycleType, cycleReference, marketDataSnapshot);
            } catch (final InterruptedException e) {
              // Execution interrupted - don't propagate as failure
              s_logger.info("View cycle execution interrupted for {}", getWorkerContext());
              return;
            } catch (final Exception e) {
              // Execution failed; might be a result of shutdown
              s_logger.error("View cycle execution failed for " + getWorkerContext(), e);
              cycleExecutionFailed(executionOptions, e);
              return;
            }
            // Don't push the results through if we've been terminated, since another computation job could be running already
            // and the fact that we've been terminated means the view is no longer interested in the result. Just die quietly.
            if (isTerminated()) {
              return;
            }
            cycleCompleted(cycleReference.get());
            // Any clients only expecting a single result may have disconnected, implicitly terminating us, or we may have
            // been explicitly terminated as a result of completing the cycle. Terminate gracefully.
            if (isTerminated()) {
              return;
            }
            if (_previousCycleReference != null) {
              _previousCycleReference.release();
            }
            _previousCycleReference = cycleReference;
            cycleReference = null;
          } finally {
            if (cycleReference != null) {
              cycleReference.release();
            }
          }
        }
        if (getExecutionOptions().getExecutionSequence().isEmpty()) {
          jobCompleted();
        }
      } finally {
        VersionCorrectionUtils.unlock(versionCorrection);
      }
    }

    @Override
    protected void postRunCycle() {
      if (_previousCycleReference != null) {
        _previousCycleReference.release();
      }
      unsubscribeFromTargetResolverChanges();
      _marketDataManager.stop();
      _executionCacheKey = null;
      cacheCompiledViewDefinition(null);
    }

    @Override
    public void terminate() {
      super.terminate();
      final Future<CompiledViewDefinitionWithGraphsImpl> task = _compilationTask;
      if (task != null) {
        task.cancel(true);
      }
    }

  }

  private void cycleCompleted(final ViewCycle cycle) {
    try {
      getWorkerContext().cycleCompleted(cycle);
    } catch (final Exception e) {
      s_logger.error("Error notifying " + getWorkerContext() + " of view cycle completion", e);
    }
  }

  private void cycleStarted(final ViewCycleMetadata cycleMetadata) {
    try {
      getWorkerContext().cycleStarted(cycleMetadata);
    } catch (final Exception e) {
      s_logger.error("Error notifying " + getWorkerContext() + " of view cycle starting", e);
    }
  }

  private void cycleFragmentCompleted(final ViewComputationResultModel result) {
    try {
      getWorkerContext().cycleFragmentCompleted(result, getViewDefinition());
    } catch (final Exception e) {
      s_logger.error("Error notifying " + getWorkerContext() + " of cycle fragment completion", e);
    }
  }

  private void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
    try {
      getWorkerContext().cycleExecutionFailed(executionOptions, exception);
    } catch (final Exception vpe) {
      s_logger.error("Error notifying " + getWorkerContext() + " of the cycle execution error", vpe);
    }
  }

  private void viewDefinitionCompiled(final CompiledViewDefinitionWithGraphs compiledViewDefinition) {
    try {
      getWorkerContext().viewDefinitionCompiled(_marketDataManager.getMarketDataProvider(), compiledViewDefinition);
    } catch (final Exception vpe) {
      s_logger.error("Error notifying " + getWorkerContext() + " of view definition compilation", vpe);
    }
  }

  private void viewDefinitionCompilationFailed(final Instant compilationTime, final Exception e) {
    try {
      getWorkerContext().viewDefinitionCompilationFailed(compilationTime, e);
    } catch (final Exception vpe) {
      s_logger.error("Error notifying " + getWorkerContext() + " of the view definition compilation failure", vpe);
    }
  }

  private ViewCycleType waitForNextCycle() throws InterruptedException {
    while (true) {
      synchronized (this) {
        final long currentTimeNanos = System.nanoTime();
        final ViewCycleTriggerResult triggerResult = getMasterCycleTrigger().query(currentTimeNanos);
        ViewCycleEligibility cycleEligibility = triggerResult.getCycleEligibility();
        if (_forceTriggerCycle) {
          cycleEligibility = ViewCycleEligibility.FORCE;
          _forceTriggerCycle = false;
        }
        if (cycleEligibility == ViewCycleEligibility.FORCE || (cycleEligibility == ViewCycleEligibility.ELIGIBLE && _cycleRequested)) {
          _cycleRequested = false;
          ViewCycleType cycleType = triggerResult.getCycleType();
          if (_previousCycleReference == null) {
            // Cannot do a delta if we have no previous cycle
            cycleType = ViewCycleType.FULL;
          }
          try {
            getMasterCycleTrigger().cycleTriggered(currentTimeNanos, cycleType);
          } catch (final Exception e) {
            s_logger.error("Error notifying trigger of intention to execute cycle", e);
          }
          s_logger.debug("Eligible for {} cycle", cycleType);
          if (_masterCycleTriggerChanges != null) {
            // TODO: If we wish to support execution option changes mid-execution, we will need to add/remove any relevant triggers here
            // Currently only the run-as-fast-as-possible trigger becomes valid for the second cycle if we've also got wait-for-initial-trigger
            addMasterCycleTrigger(_masterCycleTriggerChanges);
            _masterCycleTriggerChanges = null;
          }
          return cycleType;
        }
        // Going to sleep (or doing some useful work)
        final long wakeUpTime = triggerResult.getNextStateChangeNanos();
        if (_cycleRequested) {
          s_logger.debug("Waiting to become eligible to perform the next computation cycle");
          // No amount of market data can make us eligible for a computation cycle any sooner.
          _wakeOnCycleRequest = false;
        } else {
          s_logger.debug("Waiting until forced to perform the next computation cycle");
          _wakeOnCycleRequest = cycleEligibility == ViewCycleEligibility.ELIGIBLE;
        }
        if ((_targetResolverChanges == null) || (_latestCompiledViewDefinition == null) || !_targetResolverChanges.hasChecksPending()) {
          long sleepTime = wakeUpTime - currentTimeNanos;
          sleepTime = Math.max(0, sleepTime);
          sleepTime /= NANOS_PER_MILLISECOND;
          sleepTime += 1; // Could have been rounded down during division so ensure only woken after state change
          s_logger.debug("Sleeping for {} ms", sleepTime);
          try {
            // This could wait until end of time. In this case, only marketDataChanged() or triggerCycle() will wake it up
            wait(sleepTime);
          } catch (final InterruptedException e) {
            // We support interruption as a signal that we have been terminated. If we're interrupted without having been
            // terminated, we'll just return to this method and go back to sleep.
            Thread.interrupted();
            s_logger.info("Interrupted while delaying. Continuing operation.");
            throw e;
          }
          continue;
        }
      }
      // There are checks pending on the target resolver; do these instead of sleeping
      s_logger.debug("Checking resolutions while waiting for next cycle");
      CompiledViewDefinitionWithGraphs viewDefinition = _latestCompiledViewDefinition;
      int max = 64; // arbitrary choice - bigger means more efficient if master is remote, but might miss the expected wake up time
      final Map<ComputationTargetReference, UniqueId> checks = Maps.newHashMapWithExpectedSize(max);
      for (Map.Entry<ComputationTargetReference, UniqueId> resolved : viewDefinition.getResolvedIdentifiers().entrySet()) {
        if (_targetResolverChanges.isChanged(resolved.getValue().getObjectId())) {
          checks.put(resolved.getKey(), resolved.getValue());
          max--;
          if (max == 0) {
            break;
          }
        }
      }
      if (checks.isEmpty()) {
        s_logger.debug("No resolutions to check");
        _targetResolverChanges.clearChecksPending();
      } else {
        final Instant now = now();
        long t = -System.nanoTime();
        final PoolExecutor previousInstance = PoolExecutor.setInstance(getProcessContext().getFunctionCompilationService().getExecutorService());
        final Map<ComputationTargetReference, ComputationTargetSpecification> resolved = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext()
            .getRawComputationTargetResolver().atVersionCorrection(VersionCorrection.of(now, now)).getSpecificationResolver().getTargetSpecifications(checks.keySet());
        PoolExecutor.setInstance(previousInstance);
        t += System.nanoTime();
        for (Map.Entry<ComputationTargetReference, UniqueId> check : checks.entrySet()) {
          final ComputationTargetSpecification resolution = resolved.get(check.getKey());
          if (resolution != null) {
            final UniqueId oldId = check.getValue();
            if (oldId.equals(resolution.getUniqueId())) {
              // Target resolves the same
              s_logger.trace("No change resolving {}", check.getKey());
              continue;
            }
          }
          // Target has a new resolution, or no longer resolves - mark it and request a new cycle
          s_logger.debug("New resolution of {} to {}", check.getKey(), resolution);
          _targetResolverChanges.setChanged(check.getValue().getObjectId());
          _forceTriggerCycle = true;
        }
        s_logger.info("{} resolutions checked in {}ms during cycle wait state", checks.size(), (double) t / 1e6);
      }
    }
  }

  private void executeViewCycle(final ViewCycleType cycleType, final EngineResourceReference<SingleComputationCycle> cycleReference, final MarketDataSnapshot marketDataSnapshot)
      throws Exception {
    SingleComputationCycle deltaCycle;
    if (cycleType == ViewCycleType.FULL) {
      s_logger.info("Performing full computation");
      deltaCycle = null;
    } else {
      deltaCycle = _previousCycleReference.get();
      if ((deltaCycle != null) && (deltaCycle.getState() != ViewCycleState.EXECUTED)) {
        // Can only do a delta cycle if the previous was valid
        s_logger.info("Performing full computation; no previous cycle");
        deltaCycle = null;
      } else {
        s_logger.info("Performing delta computation");
      }
    }
    boolean continueExecution = cycleReference.get().preExecute(deltaCycle, marketDataSnapshot, _suppressExecutionOnNoMarketData);
    if (_executeGraphs && continueExecution) {
      try {
        cycleReference.get().execute();
      } catch (final InterruptedException e) {
        Thread.interrupted();
        // In reality this means that the job has been terminated, and it will end as soon as we return from this method.
        // In case the thread has been interrupted without terminating the job, we tidy everything up as if the
        // interrupted cycle never happened so that deltas will be calculated from the previous cycle.
        s_logger.info("Interrupted while executing a computation cycle. No results will be output from this cycle.");
        throw e;
      } catch (final Exception e) {
        s_logger.error("Error while executing view cycle", e);
        throw e;
      }
    } else {
      s_logger.debug("Skipping graph execution");
    }
    cycleReference.get().postExecute();
    final long durationNanos = cycleReference.get().getDuration().toNanos();
    final Timer timer = deltaCycle != null ? _deltaCycleTimer : _fullCycleTimer;
    if (timer != null) {
      timer.update(durationNanos, TimeUnit.NANOSECONDS);
    }
    _totalTimeNanos += durationNanos;
    _cycleCount += 1;
    s_logger.info("Last latency was {} ms, Average latency is {} ms", durationNanos / NANOS_PER_MILLISECOND, (_totalTimeNanos / _cycleCount) / NANOS_PER_MILLISECOND);
  }

  private void jobCompleted() {
    s_logger.info("Computation job completed for {}", getWorkerContext());
    try {
      getWorkerContext().workerCompleted();
    } catch (final Exception e) {
      s_logger.error("Error notifying " + getWorkerContext() + " of computation job completion", e);
    }
    getJob().terminate();
  }

  private EngineResourceReference<SingleComputationCycle> createCycle(final ViewCycleExecutionOptions executionOptions, final CompiledViewDefinitionWithGraphs compiledViewDefinition,
      final VersionCorrection versionCorrection) {

    // [PLAT-3581] Is the check below still necessary? The logic to create the valuation time for compilation is the same as that for
    // populating the valuation time on the execution options that this detects.

    // View definition was compiled based on compilation options, which might have only included an indicative
    // valuation time. A further check ensures that the compiled view definition is still valid.
    if (!CompiledViewDefinitionWithGraphsImpl.isValidFor(compiledViewDefinition, executionOptions.getValuationTime())) {
      throw new OpenGammaRuntimeException("Compiled view definition " + compiledViewDefinition + " not valid for execution options " + executionOptions);
    }

    final UniqueId cycleId = getProcessContext().getCycleIdentifiers().get();
    final ComputationResultListener streamingResultListener = new ComputationResultListener() {
      @Override
      public void resultAvailable(final ViewComputationResultModel result) {
        cycleFragmentCompleted(result);
      }
    };
    final SingleComputationCycle cycle = new SingleComputationCycle(cycleId, executionOptions.getName(), streamingResultListener, getProcessContext(), compiledViewDefinition,
        executionOptions, versionCorrection);
    return getProcessContext().getCycleManager().manage(cycle);
  }

  private void subscribeToTargetResolverChanges() {
    if (_targetResolverChanges == null) {
      _targetResolverChanges = new TargetResolverChangeListener() {
        @Override
        protected void onChanged() {
          // Don't request a cycle, but wake up the main thread; it may then run a cycle, or start processing the change list
          synchronized (SingleThreadViewProcessWorker.this) {
            // Wake up to check things, and maybe then request a cycle
            SingleThreadViewProcessWorker.this.notifyAll();
          }
        }
      };
      getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver().changeManager().addChangeListener(_targetResolverChanges);
    }
  }

  private void unsubscribeFromTargetResolverChanges() {
    if (_targetResolverChanges != null) {
      getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver().changeManager().removeChangeListener(_targetResolverChanges);
      _targetResolverChanges = null;
    }
  }

  private static Instant now() {
    // TODO: The distributed caches use a message bus for eventual consistency. This should really be (NOW - maximum permitted clock drift - eventual consistency time limit)
    return Instant.now();
  }

  private VersionCorrection getResolverVersionCorrection(final ViewCycleExecutionOptions viewCycleOptions) {
    VersionCorrection vc;
    do {
      vc = viewCycleOptions.getResolverVersionCorrection();
      if (vc != null) {
        break;
      }
      final ViewCycleExecutionOptions options = getExecutionOptions().getDefaultExecutionOptions();
      if (options != null) {
        vc = options.getResolverVersionCorrection();
        if (vc != null) {
          break;
        }
      }
      vc = VersionCorrection.LATEST;
    } while (false);
    // Note: NOW means NOW as the caller has requested LATEST. We should not be using the valuation time.
    if (vc.getCorrectedTo() == null) {
      if (vc.getVersionAsOf() == null) {
        if (!_ignoreCompilationValidity) {
          subscribeToTargetResolverChanges();
        }
        return vc.withLatestFixed(now());
      } else {
        vc = vc.withLatestFixed(now());
      }
    } else if (vc.getVersionAsOf() == null) {
      vc = vc.withLatestFixed(now());
    }
    unsubscribeFromTargetResolverChanges();
    return vc;
  }

  private PortfolioNodeEquivalenceMapper getNodeEquivalenceMapper() {
    return new PortfolioNodeEquivalenceMapper();
  }

  private void markMappedPositions(final PortfolioNode node, final Map<UniqueId, Position> positions) {
    for (Position position : node.getPositions()) {
      positions.put(position.getUniqueId(), null);
    }
    for (PortfolioNode child : node.getChildNodes()) {
      markMappedPositions(child, positions);
    }
  }

  private void findUnmappedNodesAndPositions(final PortfolioNode node, final Map<UniqueId, UniqueId> mapped, final Set<UniqueId> unmapped, final Map<UniqueId, Position> positions) {
    if (mapped.containsKey(node.getUniqueId())) {
      // This node is mapped; as are the nodes underneath it, so just mark the child positions
      markMappedPositions(node, positions);
    } else {
      // This node is unmapped - mark it as such and check the nodes underneath it
      unmapped.add(node.getUniqueId());
      for (PortfolioNode child : node.getChildNodes()) {
        findUnmappedNodesAndPositions(child, mapped, unmapped, positions);
      }
      // Any child positions (and their trades) are unmapped if, and only if, they are not referenced by anything else
      for (Position position : node.getPositions()) {
        if (!positions.containsKey(position.getUniqueId())) {
          positions.put(position.getUniqueId(), position);
        }
      }
    }
  }

  private void findUnmappedPositions(final PortfolioNode node, final Set<UniqueId> unmapped, final Map<UniqueId, Position> positions) {
    for (PortfolioNode child : node.getChildNodes()) {
      findUnmappedPositions(child, unmapped, positions);
    }
    for (Position position : node.getPositions()) {
      if (!positions.containsKey(position.getUniqueId())) {
        if (unmapped.contains(position.getUniqueId())) {
          positions.put(position.getUniqueId(), position);
        } else {
          positions.put(position.getUniqueId(), null);
        }
      }
    }
  }

  private void findUnmappedNodesAndPositions(final PortfolioNode node, final Map<UniqueId, UniqueId> mapped, final Set<UniqueId> unmapped) {
    final Map<UniqueId, Position> positions = new HashMap<UniqueId, Position>();
    findUnmappedNodesAndPositions(node, mapped, unmapped, positions);
    for (Map.Entry<UniqueId, Position> position : positions.entrySet()) {
      if (position.getValue() == null) {
        if (!unmapped.contains(position.getKey())) {
          // "marked" during the "findUnmapped" operation and not explicitly unmapped
          continue;
        }
        // Not mapped, but already in the unmap set, so make sure trades are too
      } else {
        unmapped.add(position.getKey());
      }
      for (Trade trade : position.getValue().getTrades()) {
        unmapped.add(trade.getUniqueId());
      }
    }
  }

  private void findUnmappedTrades(final PortfolioNode node, final Set<UniqueId> unmapped) {
    final Map<UniqueId, Position> positions = new HashMap<UniqueId, Position>();
    findUnmappedPositions(node, unmapped, positions);
    for (Map.Entry<UniqueId, Position> position : positions.entrySet()) {
      if (position.getValue() != null) {
        for (Trade trade : position.getValue().getTrades()) {
          unmapped.add(trade.getUniqueId());
        }
      }
    }
  }

  private static DependencyNode remapNode(final DependencyNode node, final Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs,
      final ComputationTargetIdentifierRemapVisitor remapper, final Map<DependencyNode, DependencyNode> remapped) {
    DependencyNode newNode = remapped.get(node);
    if (newNode != null) {
      return newNode;
    }
    final DependencyNode[] inputNodes = DependencyNodeImpl.getInputNodeArray(node);
    ValueSpecification[] inputValues = null;
    for (int i = 0; i < inputNodes.length; i++) {
      final DependencyNode newInput = remapNode(inputNodes[i], terminalOutputs, remapper, remapped);
      if (newInput != inputNodes[i]) {
        if (inputValues == null) {
          inputValues = DependencyNodeImpl.getInputValueArray(node);
        }
        inputNodes[i] = newInput;
        inputValues[i] = MemoryUtils.instance(new ValueSpecification(inputValues[i].getValueName(), newInput.getTarget(), inputValues[i].getProperties()));
      }
    }
    ComputationTargetSpecification newTarget = remapper.remap(node.getTarget());
    final ValueSpecification[] outputValues;
    if (newTarget != null) {
      outputValues = new ValueSpecification[node.getOutputCount()];
      for (int i = 0; i < outputValues.length; i++) {
        final ValueSpecification output = node.getOutputValue(i);
        final ValueSpecification newOutput = MemoryUtils.instance(new ValueSpecification(output.getValueName(), newTarget, output.getProperties()));
        outputValues[i] = newOutput;
        final Set<ValueRequirement> oldReqs = terminalOutputs.remove(output);
        if (oldReqs != null) {
          Set<ValueRequirement> newReqs = Sets.newHashSetWithExpectedSize(oldReqs.size());
          for (ValueRequirement req : oldReqs) {
            final ComputationTargetReference newRequirementTarget = req.getTargetReference().accept(remapper);
            if (newRequirementTarget != null) {
              newReqs.add(MemoryUtils.instance(new ValueRequirement(req.getValueName(), newRequirementTarget, req.getConstraints())));
            } else {
              newReqs.add(req);
            }
          }
          terminalOutputs.put(newOutput, newReqs);
        }
      }
    } else {
      final int outputValueCount = node.getOutputCount();
      for (int i = 0; i < outputValueCount; i++) {
        final ValueSpecification output = node.getOutputValue(i);
        final Set<ValueRequirement> oldReqs = terminalOutputs.get(output);
        if (oldReqs != null) {
          Set<ValueRequirement> newReqs = null;
          for (ValueRequirement req : oldReqs) {
            final ComputationTargetReference newRequirementTarget = req.getTargetReference().accept(remapper);
            if (newRequirementTarget != null) {
              if (newReqs == null) {
                newReqs = Sets.newHashSetWithExpectedSize(oldReqs.size());
                for (ValueRequirement req2 : oldReqs) {
                  if (req2 == req) {
                    break;
                  }
                  newReqs.add(req2);
                }
              }
              newReqs.add(MemoryUtils.instance(new ValueRequirement(req.getValueName(), newRequirementTarget, req.getConstraints())));
            } else {
              if (newReqs != null) {
                newReqs.add(req);
              }
            }
          }
          if (newReqs != null) {
            terminalOutputs.put(output, newReqs);
          }
        }
      }
      if (inputValues == null) {
        // No change to the node
        remapped.put(node, node);
        return node;
      }
      outputValues = DependencyNodeImpl.getOutputValueArray(node);
      newTarget = node.getTarget();
    }
    if (inputValues == null) {
      inputValues = DependencyNodeImpl.getInputValueArray(node);
    }
    newNode = DependencyNodeImpl.of(node.getFunction(), newTarget, outputValues, inputValues, inputNodes);
    remapped.put(node, newNode);
    return newNode;
  }

  /**
   * Modifies the set of previous graphs to update nodes that can be mapped by altering their unique identifiers and remove terminal outputs derived from the portfolio by anything that cannot be
   * immediately mapped.
   * <p>
   * The main use of this logic is for handling portfolio structures that are the same shape but have different unique identifiers on the portfolio nodes (the map operation), and to remove terminal
   * outputs from portfolio nodes, positions or trades that aren't known to be in the new structure.
   * 
   * @param previousGraphs the previous graphs to update, not null
   * @param compiledViewDefinition the previously compiled view definition, not null
   * @param map the mapping of old unique identifiers to new ones or null/empty if none
   * @param unmap the set of old unique identifiers that might not have portfolio derived terminal outputs, not null
   */
  private void mapAndUnmapNodes(final Map<String, PartiallyCompiledGraph> previousGraphs, final CompiledViewDefinitionWithGraphs compiledViewDefinition, final Map<UniqueId, UniqueId> map,
      final Set<UniqueId> unmap) {
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("Mapping {} portfolio nodes to new structure, unmapping {} targets", (map != null) ? map.size() : 0, unmap.size());
    }
    // For anything not mapped, remove the terminal outputs from the graph
    for (Map.Entry<String, PartiallyCompiledGraph> previousGraphEntry : previousGraphs.entrySet()) {
      final PartiallyCompiledGraph previousGraph = previousGraphEntry.getValue();
      final ViewCalculationConfiguration calcConfig = compiledViewDefinition.getViewDefinition().getCalculationConfiguration(previousGraphEntry.getKey());
      final Set<ValueRequirement> specificRequirements = calcConfig.getSpecificRequirements();
      final Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs = previousGraph.getTerminalOutputs();
      final Iterator<Map.Entry<ValueSpecification, Set<ValueRequirement>>> itrTerminalOutput = terminalOutputs.entrySet().iterator();
      while (itrTerminalOutput.hasNext()) {
        final Map.Entry<ValueSpecification, Set<ValueRequirement>> entry = itrTerminalOutput.next();
        if (unmap.contains(entry.getKey().getTargetSpecification().getUniqueId())) {
          List<ValueRequirement> removal = null;
          for (final ValueRequirement requirement : entry.getValue()) {
            if (!specificRequirements.contains(requirement)) {
              if (removal == null) {
                removal = new ArrayList<>(entry.getValue().size());
              }
              removal.add(requirement);
            }
            // Anything that was in the specific requirements will be captured by the standard invalid identifier tests
          }
          if (removal != null) {
            if (removal.size() == entry.getValue().size()) {
              // No longer a terminal output
              itrTerminalOutput.remove();
            } else {
              final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>(entry.getValue());
              requirements.removeAll(removal);
              entry.setValue(requirements);
            }
          }
        }
      }
      if ((map != null) && !map.isEmpty()) {
        final ComputationTargetIdentifierRemapVisitor remapper = new ComputationTargetIdentifierRemapVisitor(map);
        final Collection<DependencyNode> oldRoots = previousGraph.getRoots();
        final Set<DependencyNode> newRoots = Sets.newHashSetWithExpectedSize(oldRoots.size());
        final Map<DependencyNode, DependencyNode> remapped = new HashMap<DependencyNode, DependencyNode>();
        for (DependencyNode oldRoot : oldRoots) {
          newRoots.add(remapNode(oldRoot, terminalOutputs, remapper, remapped));
        }
        previousGraph.getRoots().clear();
        previousGraph.getRoots().addAll(newRoots);
      }
    }
  }

  /**
   * Returns the set of unique identifiers that were previously used as targets in the dependency graph for object identifiers (or external identifiers) that now resolve differently.
   * 
   * @param previousResolutions the previous cycle's resolution of identifiers, not null
   * @param versionCorrection the resolver version correction for this cycle, not null
   * @return the invalid identifier set, or null if none are invalid, this is a map from the old unique identifier to the new resolution
   */
  private Map<UniqueId, ComputationTargetSpecification> getInvalidIdentifiers(final Map<ComputationTargetReference, UniqueId> previousResolutions, final VersionCorrection versionCorrection) {
    long t = -System.nanoTime();
    final Set<ComputationTargetReference> toCheck;
    if (_targetResolverChanges == null) {
      // Change notifications aren't relevant for historical iteration; must recheck all of the resolutions
      toCheck = previousResolutions.keySet();
    } else {
      // Subscribed to LATEST/LATEST so change manager notifications can filter the set to be checked
      toCheck = Sets.newHashSetWithExpectedSize(previousResolutions.size());
      final Set<ObjectId> allObjectIds = Sets.newHashSetWithExpectedSize(previousResolutions.size());
      for (final Map.Entry<ComputationTargetReference, UniqueId> previousResolution : previousResolutions.entrySet()) {
        final ObjectId oid = previousResolution.getValue().getObjectId();
        if (_targetResolverChanges.isChanged(oid)) {
          // A change was seen on this target
          s_logger.debug("Change observed on {}", oid);
          toCheck.add(previousResolution.getKey());
        }
        allObjectIds.add(oid);
      }
      _targetResolverChanges.watchOnly(allObjectIds);
      if (toCheck.isEmpty()) {
        s_logger.debug("No resolutions (from {}) to check", previousResolutions.size());
        return null;
      } else {
        s_logger.debug("Checking {} of {} resolutions for changed objects", toCheck.size(), previousResolutions.size());
      }
    }
    PoolExecutor previousInstance = PoolExecutor.setInstance(getProcessContext().getFunctionCompilationService().getExecutorService());
    final Map<ComputationTargetReference, ComputationTargetSpecification> specifications = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext()
        .getRawComputationTargetResolver().getSpecificationResolver().getTargetSpecifications(toCheck, versionCorrection);
    PoolExecutor.setInstance(previousInstance);
    t += System.nanoTime();
    Map<UniqueId, ComputationTargetSpecification> invalidIdentifiers = null;
    for (final Map.Entry<ComputationTargetReference, UniqueId> target : previousResolutions.entrySet()) {
      final ComputationTargetSpecification resolved = specifications.get(target.getKey());
      if ((resolved != null) && target.getValue().equals(resolved.getUniqueId())) {
        // No change
        s_logger.trace("No change resolving {}", target);
      } else if (toCheck.contains(target.getKey())) {
        // Identifier no longer resolved, or resolved differently
        s_logger.info("New resolution of {} to {}", target, resolved);
        if (invalidIdentifiers == null) {
          invalidIdentifiers = new HashMap<>();
        }
        invalidIdentifiers.put(target.getValue(), resolved);
      }
    }
    s_logger.info("{} resolutions checked in {}ms", toCheck.size(), t / 1e6);
    return invalidIdentifiers;
  }

  /**
   * Creates a filter that removes nodes from the graph based on invalid market data resolutions.
   * 
   * @param previousGraphs the previous graphs that have already been part processed
   * @return the filter if one is needed, null if no invalidation is required
   */
  private RootDiscardingSubgrapher getInvalidMarketData(final Map<String, PartiallyCompiledGraph> previousGraphs, final CompiledViewDefinitionWithGraphs viewDefinition,
      final VersionCorrection versionCorrection) {
    final InvalidMarketDataDependencyNodeFilter filter = new InvalidMarketDataDependencyNodeFilter(getProcessContext().getFunctionCompilationService().getFunctionCompilationContext()
        .getRawComputationTargetResolver().atVersionCorrection(versionCorrection), _marketDataManager.getAvailabilityProvider());
    final Set<DependencyNode> visited = new HashSet<DependencyNode>();
    if (previousGraphs != null) {
      for (Map.Entry<String, PartiallyCompiledGraph> previous : previousGraphs.entrySet()) {
        final PartiallyCompiledGraph graph = previous.getValue();
        for (DependencyNode root : graph.getRoots()) {
          filter.init(root, graph.getTerminalOutputs(), visited);
        }
      }
    } else {
      for (DependencyGraphExplorer explorer : viewDefinition.getDependencyGraphExplorers()) {
        final DependencyGraph graph = explorer.getWholeGraph();
        final int roots = graph.getRootCount();
        final Map<ValueSpecification, Set<ValueRequirement>> terminals = graph.getTerminalOutputs();
        for (int i = 0; i < roots; i++) {
          filter.init(graph.getRootNode(i), terminals, visited);
        }
      }
    }
    // 32 was chosen fairly arbitrarily prior to restructuring the graph structure
    if (filter.checkMarketData(getProcessContext().getFunctionCompilationService().getExecutorService(), 32)) {
      return filter;
    } else {
      return null;
    }
  }

  private Map<String, PartiallyCompiledGraph> invalidateMarketDataSourcingNodes(Map<String, PartiallyCompiledGraph> previousGraphs, final CompiledViewDefinitionWithGraphs viewDefinition,
      final VersionCorrection versionCorrection, final Set<UniqueId> unchangedNodes) {
    final RootDiscardingSubgrapher filter = getInvalidMarketData(previousGraphs, viewDefinition, versionCorrection);
    if (filter != null) {
      previousGraphs = getPreviousGraphs(previousGraphs, viewDefinition);
      filterPreviousGraphs(previousGraphs, filter, unchangedNodes);
    }
    return previousGraphs;
  }

  /**
   * Maintain the previously used dependency graphs by applying a node filter that identifies invalid nodes that must be recalculated (implying everything dependent on them must also be rebuilt).
   * 
   * @param previousGraphs the previously used graphs as a map from calculation configuration name to the data, not null
   * @param filter the filter to identify invalid nodes, not null
   * @param unchangedNodes optional identifiers of unchanged portfolio nodes; any nodes filtered out must be removed from this
   */
  private void filterPreviousGraphs(final Map<String, PartiallyCompiledGraph> previousGraphs, final RootDiscardingSubgrapher filter, final Set<UniqueId> unchangedNodes) {
    final Iterator<Map.Entry<String, PartiallyCompiledGraph>> itr = previousGraphs.entrySet().iterator();
    while (itr.hasNext()) {
      final Map.Entry<String, PartiallyCompiledGraph> entry = itr.next();
      final Collection<DependencyNode> oldRoots = entry.getValue().getRoots();
      final Set<DependencyNode> newRoots;
      if (unchangedNodes != null) {
        final Map<DependencyNode, RootDiscardingSubgrapher.NodeState> state = new HashMap<>();
        newRoots = filter.subGraph(oldRoots, entry.getValue().getTerminalOutputs(), entry.getValue().getMissingRequirements(), state);
        for (Map.Entry<DependencyNode, RootDiscardingSubgrapher.NodeState> node : state.entrySet()) {
          if (node.getValue() == RootDiscardingSubgrapher.NodeState.EXCLUDED) {
            unchangedNodes.remove(node.getKey().getTarget().getUniqueId());
          }
        }
      } else {
        newRoots = filter.subGraph(oldRoots, entry.getValue().getTerminalOutputs(), entry.getValue().getMissingRequirements());
      }
      if (newRoots != null) {
        oldRoots.clear();
        oldRoots.addAll(newRoots);
      } else {
        s_logger.info("Discarded total dependency graph for {}", entry.getKey());
        itr.remove();
      }
    }
  }

  private static Map<String, PartiallyCompiledGraph> getPreviousGraphs(Map<String, PartiallyCompiledGraph> previousGraphs, final CompiledViewDefinitionWithGraphs compiledViewDefinition) {
    if (previousGraphs == null) {
      final Collection<DependencyGraphExplorer> graphExps = compiledViewDefinition.getDependencyGraphExplorers();
      previousGraphs = Maps.newHashMapWithExpectedSize(graphExps.size());
      for (DependencyGraphExplorer graphExp : graphExps) {
        final DependencyGraph graph = graphExp.getWholeGraph();
        previousGraphs.put(graph.getCalculationConfigurationName(), new PartiallyCompiledGraph(graph));
      }
    }
    return previousGraphs;
  }

  private CompiledViewDefinitionWithGraphs getCompiledViewDefinition(final Instant valuationTime, final VersionCorrection versionCorrection) {
    final long functionInitId = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    updateViewDefinitionIfRequired();
    CompiledViewDefinitionWithGraphs compiledViewDefinition = null;
    final Pair<Lock, Lock> executionCacheLocks = getProcessContext().getExecutionCacheLock().get(_executionCacheKey, valuationTime, versionCorrection);
    executionCacheLocks.getSecond().lock();
    executionCacheLocks.getFirst().lock();
    boolean broadLock = true;
    try {
      do {
        Map<String, PartiallyCompiledGraph> previousGraphs = null;
        ConcurrentMap<ComputationTargetReference, UniqueId> previousResolutions = null;
        Set<UniqueId> changedPositions = null;
        Set<UniqueId> unchangedNodes = null;
        ViewCompilationServices compilationServices = null;
        if (!_forceGraphRebuild.getAndSet(false)) {
          compiledViewDefinition = getCachedCompiledViewDefinition(valuationTime, versionCorrection);
          boolean marketDataProviderDirty = _marketDataManager.isMarketDataProviderDirty();
          _marketDataManager.markMarketDataProviderClean();
          if (compiledViewDefinition != null) {
            executionCacheLocks.getFirst().unlock();
            broadLock = false;
            do {
              // The cast below is bad, but only temporary -- the function initialiser id needs to go
              if (functionInitId != ((CompiledViewDefinitionWithGraphsImpl) compiledViewDefinition).getFunctionInitId()) {
                // The function repository has been reinitialized which invalidates any previous graphs
                // TODO: [PLAT-2237, PLAT-1623, PLAT-2240] Get rid of this
                break;
              }
              final Map<ComputationTargetReference, UniqueId> resolvedIdentifiers = compiledViewDefinition.getResolvedIdentifiers();
              // TODO: The check below works well for the historical valuation case, but if the resolver v/c is different for two workers in the
              // group for an otherwise identical cache key then including it in the caching detail may become necessary to handle those cases.
              if (!versionCorrection.equals(compiledViewDefinition.getResolverVersionCorrection())) {
                final Map<UniqueId, ComputationTargetSpecification> invalidIdentifiers = getInvalidIdentifiers(resolvedIdentifiers, versionCorrection);
                if (invalidIdentifiers != null) {
                  previousGraphs = getPreviousGraphs(previousGraphs, compiledViewDefinition);
                  previousResolutions = new ConcurrentHashMap<>(resolvedIdentifiers.size());
                  final Map<UniqueId, UniqueId> mapped;
                  final Set<UniqueId> unmapped = new HashSet<>();
                  if (compiledViewDefinition.getPortfolio() != null) {
                    final ComputationTargetSpecification newPortfolioSpec = invalidIdentifiers.get(compiledViewDefinition.getPortfolio().getUniqueId());
                    if (newPortfolioSpec != null) {
                      // The portfolio resolution is different, invalidate or rewrite PORTFOLIO and PORTFOLIO_NODE nodes in the graph. Note that incremental
                      // compilation under this circumstance can be flawed if the functions have made notable use of the overall portfolio structure such that
                      // a full re-compilation will yield a different dependency graph to just rewriting the previous one.
                      final ComputationTargetResolver resolver = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver();
                      final ComputationTarget newPortfolio = resolver.resolve(newPortfolioSpec, versionCorrection);
                      // Map any nodes from the old portfolio structure to the new one
                      if (newPortfolio != null) {
                        mapped = getNodeEquivalenceMapper().getEquivalentNodes(compiledViewDefinition.getPortfolio().getRootNode(), ((Portfolio) newPortfolio.getValue()).getRootNode());
                        unchangedNodes = new HashSet<UniqueId>(mapped.values());
                      } else {
                        mapped = Collections.emptyMap();
                        unchangedNodes = new HashSet<UniqueId>();
                      }
                      // Build a set of previous resolutions that haven't changed and unmap any modified positions or trades
                      for (final Map.Entry<ComputationTargetReference, UniqueId> resolvedIdentifier : resolvedIdentifiers.entrySet()) {
                        if (invalidIdentifiers.containsKey(resolvedIdentifier.getValue())) {
                          if (resolvedIdentifier.getKey().getType().isTargetType(ComputationTargetType.POSITION_OR_TRADE)) {
                            unmapped.add(resolvedIdentifier.getValue());
                          }
                        } else {
                          previousResolutions.put(resolvedIdentifier.getKey(), resolvedIdentifier.getValue());
                        }
                      }
                      // Rewrite the graph for the new portfolio structure and identify any defunct positions/trades
                      findUnmappedNodesAndPositions(compiledViewDefinition.getPortfolio().getRootNode(), mapped, unmapped);
                    } else {
                      mapped = null;
                      // Build a set of previous resolutions and mark any changed positions or trades for unmapping
                      for (final Map.Entry<ComputationTargetReference, UniqueId> resolvedIdentifier : resolvedIdentifiers.entrySet()) {
                        if (invalidIdentifiers.containsKey(resolvedIdentifier.getValue())) {
                          if (resolvedIdentifier.getKey().getType().isTargetType(ComputationTargetType.POSITION)) {
                            ComputationTargetSpecification ctspec = invalidIdentifiers.get(resolvedIdentifier.getValue());
                            if (ctspec != null) {
                              if (changedPositions == null) {
                                changedPositions = new HashSet<>();
                              }
                              changedPositions.add(ctspec.getUniqueId());
                            }
                            unmapped.add(resolvedIdentifier.getValue());
                          } else if (resolvedIdentifier.getKey().getType().isTargetType(ComputationTargetType.TRADE)) {
                            unmapped.add(resolvedIdentifier.getValue());
                          }
                        } else {
                          previousResolutions.put(resolvedIdentifier.getKey(), resolvedIdentifier.getValue());
                        }
                      }
                      // Identify any defunct trades
                      findUnmappedTrades(compiledViewDefinition.getPortfolio().getRootNode(), unmapped);
                    }
                  } else {
                    mapped = null;
                  }
                  // Remove terminal outputs and rewrite nodes
                  mapAndUnmapNodes(previousGraphs, compiledViewDefinition, mapped, unmapped);
                  // Remove any PORTFOLIO nodes and any unmapped PORTFOLIO_NODE nodes with the filter
                  filterPreviousGraphs(previousGraphs, new InvalidPortfolioDependencyNodeFilter(unmapped), null);
                  // Invalidate any dependency graph nodes on the invalid targets
                  filterPreviousGraphs(previousGraphs, new InvalidTargetDependencyNodeFilter(invalidIdentifiers.keySet()), unchangedNodes);
                } else {
                  compiledViewDefinition = compiledViewDefinition.withResolverVersionCorrection(versionCorrection);
                  cacheCompiledViewDefinition(compiledViewDefinition);
                }
              }
              if (!CompiledViewDefinitionWithGraphsImpl.isValidFor(compiledViewDefinition, valuationTime)) {
                // Invalidate any dependency graph nodes that use functions that are no longer valid
                previousGraphs = getPreviousGraphs(previousGraphs, compiledViewDefinition);
                compilationServices = getProcessContext().asCompilationServices(_marketDataManager.getAvailabilityProvider());
                filterPreviousGraphs(previousGraphs, new InvalidFunctionDependencyNodeFilter(compilationServices.getFunctionResolver().compile(valuationTime), valuationTime), unchangedNodes);
              }
              if (marketDataProviderDirty) {
                // Invalidate any graph nodes that use market data which is no longer valid
                previousGraphs = invalidateMarketDataSourcingNodes(previousGraphs, compiledViewDefinition, versionCorrection, unchangedNodes);
              }
              if (previousGraphs == null) {
                // Existing cached model is valid (an optimization for the common case of similar, increasing valuation times)
                return compiledViewDefinition;
              }
              if (previousResolutions == null) {
                previousResolutions = new ConcurrentHashMap<>(resolvedIdentifiers);
              }
            } while (false);
            executionCacheLocks.getFirst().lock();
            broadLock = true;
          }
        } else {
          s_logger.debug("Full graph rebuild requested");
        }
        if (compilationServices == null) {
          // TODO: The relationship between ViewProcessContext, ViewCompilationContext, ViewCompilationServices and ViewDefinitionCompiler is starting to feel a bit cumbersome. It might
          // be neater to refactor so that we create a ViewDefinitionCompiler instance earlier on and query bits that we need. Otherwise we seem to repeat work such as obtaining a
          // compiled function resolver or versioned target resolver.
          compilationServices = getProcessContext().asCompilationServices(_marketDataManager.getAvailabilityProvider());
        }
        if (previousGraphs != null) {
          s_logger.info("Performing incremental graph compilation");
          _compilationTask = ViewDefinitionCompiler.incrementalCompileTask(getViewDefinition(), compilationServices, valuationTime, versionCorrection, previousGraphs, previousResolutions,
              changedPositions, unchangedNodes);
        } else {
          s_logger.info("Performing full graph compilation");
          _compilationTask = ViewDefinitionCompiler.fullCompileTask(getViewDefinition(), compilationServices, valuationTime, versionCorrection);
        }
        try {
          if (!getJob().isTerminated()) {
            compiledViewDefinition = _compilationTask.get();
            ComputationTargetResolver.AtVersionCorrection resolver = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver()
                .atVersionCorrection(versionCorrection);
            compiledViewDefinition = initialiseMarketDataManipulation(compiledViewDefinition, resolver);
            cacheCompiledViewDefinition(compiledViewDefinition);
          } else {
            return null;
          }
        } catch (IllegalCompilationStateException e) {
          s_logger.warn("Detected late change to compilation state; repeating compilation in {}", this);
          s_logger.debug("Caught exception", e);
          final ObjectId oid = e.getInvalidIdentifier();
          if ((oid != null) && (_targetResolverChanges != null)) {
            // Try again with this identifier invalidated
            s_logger.info("Invalidating {} and retrying", oid);
            _targetResolverChanges.setChanged(oid);
          } else {
            // Nothing to invalidate - force a full rebuild
            s_logger.error("Nothing to invalidate following illegal compilation state, forcing a full rebuild");
            _forceGraphRebuild.set(true);
          }
          continue;
        } finally {
          _compilationTask = null;
        }
        break;
      } while (true);
    } catch (final Exception e) {
      if (!getJob().isTerminated()) {
        final String message = MessageFormat.format("Error compiling view definition {0} for time {1}", getViewDefinition().getUniqueId(), valuationTime);
        viewDefinitionCompilationFailed(valuationTime, new OpenGammaRuntimeException(message, e));
        throw new OpenGammaRuntimeException(message, e);
      } else {
        s_logger.debug("Caught exception during termination", e);
        return null;
      }
    } finally {
      if (broadLock) {
        executionCacheLocks.getFirst().unlock();
      }
      executionCacheLocks.getSecond().unlock();
    }
    // [PLAT-984]
    // Assume that valuation times are increasing in real-time towards the expiry of the view definition, so that we
    // can predict the time to expiry. If this assumption is wrong then the worst we do is trigger an unnecessary
    // cycle. In the predicted case, we trigger a cycle on expiry so that any new market data subscriptions are made
    // straight away.
    if ((compiledViewDefinition.getValidTo() != null) && getExecutionOptions().getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED)) {
      final Duration durationToExpiry = _marketDataManager.getMarketDataProvider().getRealTimeDuration(valuationTime, compiledViewDefinition.getValidTo());
      final long expiryNanos = System.nanoTime() + durationToExpiry.toNanos();
      _compilationExpiryCycleTrigger.set(expiryNanos, ViewCycleTriggerResult.forceFull());
      // REVIEW Andrew 2012-11-02 -- If we are ticking live, then this is almost right (System.nanoTime will be close to valuationTime, depending on how
      // long the compilation took). If we are running through historical data then this is quite a meaningless trigger.
    } else {
      _compilationExpiryCycleTrigger.reset();
    }
    return compiledViewDefinition;
  }

  private CompiledViewDefinitionWithGraphs initialiseMarketDataManipulation(final CompiledViewDefinitionWithGraphs compiledViewDefinition,
      final ComputationTargetResolver.AtVersionCorrection resolver) {
    if (_marketDataSelectionGraphManipulator.hasManipulationsDefined()) {
      s_logger.info("Initialising market data manipulation");
      final Map<String, DependencyGraph> newGraphsByConfig = new HashMap<>();
      final Map<String, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByConfig = new HashMap<>();
      final Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> functionParamsByConfig = new HashMap<>();
      for (DependencyGraphExplorer graphExplorer : compiledViewDefinition.getDependencyGraphExplorers()) {
        DependencyGraph graph = graphExplorer.getWholeGraph();
        // REVIEW Chris 2014-01-14 - selectorMapping is stored in DependencyGraphStructureExtractor, mutated
        // by MarketDataSelectionGraphManipulator and used here. that's too obscure
        final Map<DistinctMarketDataSelector, Set<ValueSpecification>> selectorMapping = new HashMap<>();
        DependencyGraph modifiedGraph = _marketDataSelectionGraphManipulator.modifyDependencyGraph(graph, resolver, selectorMapping);
        if (!selectorMapping.isEmpty()) {
          newGraphsByConfig.put(modifiedGraph.getCalculationConfigurationName(), modifiedGraph);
          selectionsByConfig.put(modifiedGraph.getCalculationConfigurationName(), selectorMapping);
          final Map<DistinctMarketDataSelector, FunctionParameters> params = _specificMarketDataSelectors.get(modifiedGraph.getCalculationConfigurationName());
          // _specificMarketDataSelectors has an entry for each graph, so no null check required
          if (!params.isEmpty()) {
            // Filter the function params so that we only have entries for active selectors
            final Map<DistinctMarketDataSelector, FunctionParameters> filteredParams = Maps.filterKeys(params, new Predicate<DistinctMarketDataSelector>() {
              @Override
              public boolean apply(DistinctMarketDataSelector selector) {
                return selectorMapping.containsKey(selector);
              }
            });
            functionParamsByConfig.put(modifiedGraph.getCalculationConfigurationName(), filteredParams);
          }
        }
      }
      if (!selectionsByConfig.isEmpty()) {
        s_logger.info("Adding in market data manipulation selections: [{}] and preset function parameters: [{}]", selectionsByConfig, functionParamsByConfig);
        return compiledViewDefinition.withMarketDataManipulationSelections(newGraphsByConfig, selectionsByConfig, functionParamsByConfig);
      } else {
        s_logger.info("No market data manipulation selectors matched - no manipulation to be done");
      }
    }
    return compiledViewDefinition;
  }

  /**
   * Gets the cached compiled view definition which may be re-used in subsequent computation cycles.
   * <p>
   * External visibility for tests.
   * 
   * @param valuationTime the indicative valuation time, not null
   * @param resolverVersionCorrection the resolver version correction, not null
   * @return the cached compiled view definition, or null if nothing is currently cached
   */
  public CompiledViewDefinitionWithGraphs getCachedCompiledViewDefinition(final Instant valuationTime, final VersionCorrection resolverVersionCorrection) {
    CompiledViewDefinitionWithGraphs cached = _latestCompiledViewDefinition;
    if (cached != null) {
      final VersionCorrection lastResolution = cached.getResolverVersionCorrection();
      final boolean resolverMatch = resolverVersionCorrection.equals(lastResolution);
      final boolean valuationMatch = CompiledViewDefinitionWithGraphsImpl.isValidFor(cached, valuationTime);
      if (!resolverMatch || !valuationMatch) {
        // Query the cache in case there is a newer one
        cached = getProcessContext().getExecutionCache().getCompiledViewDefinitionWithGraphs(_executionCacheKey);
        if (cached != null) {
          // Only update ours if the one from the cache has a better validity
          if (resolverVersionCorrection.equals(cached.getResolverVersionCorrection())) {
            // View from the cache is for the correct V/C
            if (CompiledViewDefinitionWithGraphsImpl.isValidFor(cached, valuationTime)) {
              // View from the cache is for the correct valuation time
              _latestCompiledViewDefinition = cached;
            } else {
              // View from the cache is not the correct valuation time ...
              if (resolverMatch) {
                // ... caching is no better than the one we used last time
                cached = _latestCompiledViewDefinition;
              } else {
                // ... but is at least the correct resolution time
                _latestCompiledViewDefinition = cached;
              }
            }
          } else {
            if (!resolverMatch) {
              if (_targetResolverChanges != null) {
                if (!cached.getResolverVersionCorrection().getVersionAsOf().isBefore(lastResolution.getVersionAsOf()) &&
                    !cached.getResolverVersionCorrection().getCorrectedTo().isBefore(lastResolution.getCorrectedTo())) {
                  // Cached form was created while we were change subscribed so we can verify it ...
                  if (!valuationMatch && CompiledViewDefinitionWithGraphsImpl.isValidFor(cached, valuationTime)) {
                    // ... and then use it for the valuation time
                    _latestCompiledViewDefinition = cached;
                  } else {
                    // ... but it's no better than the one we used last time
                    cached = _latestCompiledViewDefinition;
                    //_latestCompiledViewDefinition = cached;
                  }
                } else {
                  // Cached form is outside of our change subscription window so verifying changes we've heard about won't help
                  cached = _latestCompiledViewDefinition;
                }
              } else {
                if (!valuationMatch && CompiledViewDefinitionWithGraphsImpl.isValidFor(cached, valuationTime)) {
                  // Cached form is at least valid for the valuation time
                  _latestCompiledViewDefinition = cached;
                } else {
                  // Cached one is no better than the one we used last time
                  cached = _latestCompiledViewDefinition;
                }
              }
            } else {
              // Cached form is no better than the one we used last time
              cached = _latestCompiledViewDefinition;
            }
          }
        } else {
          // Nothing in the cache; use the one from last time
          cached = _latestCompiledViewDefinition;
        }
      }
    } else {
      // Query the cache
      cached = getProcessContext().getExecutionCache().getCompiledViewDefinitionWithGraphs(_executionCacheKey);
      if (cached != null) {
        // This would have to be the "first" cycle, so ant target resolver listener will be empty
        _latestCompiledViewDefinition = cached;
      }
    }
    return cached;
  }

  /**
   * Replaces the cached compiled view definition.
   * <p>
   * External visibility for tests.
   * 
   * @param latestCompiledViewDefinition the compiled view definition, may be null
   */
  public void cacheCompiledViewDefinition(final CompiledViewDefinitionWithGraphs latestCompiledViewDefinition) {
    if (latestCompiledViewDefinition != null) {
      getProcessContext().getExecutionCache().setCompiledViewDefinitionWithGraphs(_executionCacheKey, latestCompiledViewDefinition);
    }
    _latestCompiledViewDefinition = latestCompiledViewDefinition;
  }

  /**
   * Gets the view definition currently in use by the computation job.
   * 
   * @return the view definition, not null
   */
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  private void updateViewDefinitionIfRequired() {
    final ViewDefinition newViewDefinition = _newViewDefinition.getAndSet(null);
    if (newViewDefinition != null) {
      _viewDefinition = newViewDefinition;
      _specificMarketDataSelectors = extractSpecificSelectors(newViewDefinition);
      _marketDataSelectionGraphManipulator = createMarketDataManipulator(_executionOptions.getDefaultExecutionOptions(), _specificMarketDataSelectors);

      // TODO [PLAT-3215] Might not need to discard the entire compilation at this point
      cacheCompiledViewDefinition(null);
      if (_targetResolverChanges != null) {
        _targetResolverChanges.watchNone();
      }

      // A change in view definition might mean a change in market data user which could invalidate the resolutions
      _marketDataManager.replaceMarketDataProviderIfRequired(newViewDefinition.getMarketDataUser());

      _executionCacheKey = ViewExecutionCacheKey.of(newViewDefinition, _marketDataManager.getAvailabilityProvider(), _marketDataSelectionGraphManipulator);
    }
  }

  @Override
  public void onMarketDataValuesChanged(final Collection<ValueSpecification> valueSpecifications) {
    if (!getExecutionOptions().getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED)) {
      return;
    }
    // Don't want to query the cache for this; always use the last one
    final CompiledViewDefinitionWithGraphs compiledView = _latestCompiledViewDefinition;
    if (compiledView == null) {
      return;
    }
    if (CollectionUtils.containsAny(compiledView.getMarketDataRequirements(), valueSpecifications)) {
      requestCycle();
    }
  }

  // ViewComputationJob

  @Override
  public synchronized boolean triggerCycle() {
    s_logger.debug("Cycle triggered manually");
    _forceTriggerCycle = true;
    notifyAll();
    return true;
  }

  @Override
  public synchronized boolean requestCycle() {
    // REVIEW jonathan 2010-10-04 -- this synchronisation is necessary, but it feels very heavyweight for
    // high-frequency market data. See how it goes, but we could take into account the recalc periods and apply a
    // heuristic (e.g. only wake up due to market data if max - min < e, for some e) which tries to see whether it's
    // worth doing all this.

    _cycleRequested = true;
    if (!_wakeOnCycleRequest) {
      return true;
    }
    notifyAll();
    return true;
  }

  @Override
  public void updateViewDefinition(final ViewDefinition viewDefinition) {
    s_logger.debug("Received new view definition {} for next cycle", viewDefinition.getUniqueId());
    _newViewDefinition.getAndSet(viewDefinition);
  }

  @Override
  public void terminate() {
    getJob().terminate();
    s_logger.debug("Interrupting calculation job thread");
    getThread().interrupt();
  }

  @Override
  public void join() throws InterruptedException {
    getThread().join();
  }

  @Override
  public boolean join(final long timeout) throws InterruptedException {
    getThread().join(timeout);
    return !getThread().isAlive();
  }

  @Override
  public boolean isTerminated() {
    return getJob().isTerminated() && !getThread().isAlive();
  }

  /**
   * @deprecated DON'T USE THIS, IT'S A TEMPORARY WORKAROUND
   */
  @Override
  @Deprecated
  public void forceGraphRebuild() {
    s_logger.debug("Requesting graph rebuild on next cycle");
    _forceGraphRebuild.set(true);
  }
}
