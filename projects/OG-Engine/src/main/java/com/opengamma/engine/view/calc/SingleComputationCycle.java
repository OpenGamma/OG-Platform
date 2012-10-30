/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static com.opengamma.util.functional.Functional.submapByKeySet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.time.Duration;
import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessContext;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.cache.MissingMarketDataSentinel;
import com.opengamma.engine.view.cache.NotCalculatedSentinel;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ComputationResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Holds all data and actions for a single computation pass. The view cycle may be executed at most once.
 * <p>
 * The cycle is thread-safe for readers, for example obtaining the current state or the result, but is only designed for a single executor.
 */
public class SingleComputationCycle implements ViewCycle, EngineResource {

  private static final Logger s_logger = LoggerFactory.getLogger(SingleComputationCycle.class);

  /**
   * The default property used to manipulate all root market data prior to cycle execution.
   */
  public static final String MARKET_DATA_SHIFT_PROPERTY = "MARKET_DATA_SHIFT";

  private enum NodeStateFlag {
    /**
     * Node was executed successfully.
     */
    EXECUTED,
    /**
     * Node was executed but failed.
     */
    FAILED,
    /**
     * Node was not executed because of function blacklist suppression.
     */
    SUPPRESSED;
  };

  // Injected inputs
  private final UniqueId _cycleId;
  private final UniqueId _viewProcessId;
  private final ViewProcessContext _viewProcessContext;
  private final CompiledViewDefinitionWithGraphsImpl _compiledViewDefinition;
  private final ViewCycleExecutionOptions _executionOptions;
  private final VersionCorrection _versionCorrection;

  private final ComputationResultListener _cycleFragmentResultListener;
  private final DependencyGraphExecutor<?> _dependencyGraphExecutor;
  private final GraphExecutorStatisticsGatherer _statisticsGatherer;

  private volatile ViewCycleState _state = ViewCycleState.AWAITING_EXECUTION;

  private volatile Instant _startTime;
  private volatile Instant _endTime;

  private final Map<DependencyNode, NodeStateFlag> _nodeStates = new ConcurrentHashMap<DependencyNode, NodeStateFlag>();
  private final Map<String, ViewComputationCache> _cachesByCalculationConfiguration = new HashMap<String, ViewComputationCache>();

  // Output
  private final InMemoryViewComputationResultModel _resultModel;

  public SingleComputationCycle(UniqueId cycleId, UniqueId viewProcessId,
      ComputationResultListener cycleFragmentResultListener, ViewProcessContext viewProcessContext,
      CompiledViewDefinitionWithGraphsImpl compiledViewDefinition, ViewCycleExecutionOptions executionOptions,
      VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(cycleId, "cycleId");
    ArgumentChecker.notNull(viewProcessId, "viewProcessId");
    ArgumentChecker.notNull(cycleFragmentResultListener, "cycleFragmentResultListener");
    ArgumentChecker.notNull(viewProcessContext, "viewProcessContext");
    ArgumentChecker.notNull(compiledViewDefinition, "compiledViewDefinition");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.isFalse(versionCorrection.containsLatest(), "versionCorrection must be fully-resolved");
    _cycleId = cycleId;
    _viewProcessId = viewProcessId;
    _viewProcessContext = viewProcessContext;
    _compiledViewDefinition = compiledViewDefinition;
    _cycleFragmentResultListener = cycleFragmentResultListener;
    _executionOptions = executionOptions;
    _versionCorrection = versionCorrection;
    _resultModel = constructTemplateResultModel();
    _dependencyGraphExecutor = getViewProcessContext().getDependencyGraphExecutorFactory().createExecutor(this);
    _statisticsGatherer = getViewProcessContext().getGraphExecutorStatisticsGathererProvider().getStatisticsGatherer(getViewProcessId());
  }

  private InMemoryViewComputationResultModel constructTemplateResultModel() {
    InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.setViewCycleId(getCycleId());
    result.setViewProcessId(getViewProcessId());
    result.setValuationTime(getExecutionOptions().getValuationTime());
    result.setVersionCorrection(getVersionCorrection());
    return result;
  }

  //-------------------------------------------------------------------------
  public Instant getValuationTime() {
    return getExecutionOptions().getValuationTime();
  }

  public ViewCycleExecutionOptions getViewCycleExecutionOptions() {
    return _executionOptions;
  }

  public long getFunctionInitId() {
    return getCompiledViewDefinition().getFunctionInitId();
  }

  /**
   * Gets the start time
   * 
   * @return the start time
   */
  public Instant getStartTime() {
    return _startTime;
  }

  /**
   * Gets the end time.
   * 
   * @return the end time
   */
  public Instant getEndTime() {
    return _endTime;
  }

  /**
   * @return the viewDefinition
   */
  public ViewDefinition getViewDefinition() {
    return getCompiledViewDefinition().getViewDefinition();
  }

  public DependencyGraphExecutor<?> getDependencyGraphExecutor() {
    return _dependencyGraphExecutor;
  }

  public GraphExecutorStatisticsGatherer getStatisticsGatherer() {
    return _statisticsGatherer;
  }

  public Map<String, ViewComputationCache> getCachesByCalculationConfiguration() {
    return Collections.unmodifiableMap(_cachesByCalculationConfiguration);
  }

  public ViewProcessContext getViewProcessContext() {
    return _viewProcessContext;
  }

  public Set<String> getAllCalculationConfigurationNames() {
    return new HashSet<String>(getCompiledViewDefinition().getViewDefinition().getAllCalculationConfigurationNames());
  }

  //-------------------------------------------------------------------------
  private UniqueId getCycleId() {
    return _cycleId;
  }

  private VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  private ViewCycleExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId getUniqueId() {
    return _cycleId;
  }

  @Override
  public UniqueId getViewProcessId() {
    return _viewProcessId;
  }

  @Override
  public ViewCycleState getState() {
    return _state;
  }

  @Override
  public Duration getDuration() {
    ViewCycleState state = getState();
    if (state == ViewCycleState.AWAITING_EXECUTION || state == ViewCycleState.EXECUTION_INTERRUPTED) {
      return null;
    }
    return Duration.between(getStartTime(), getEndTime() == null ? Instant.now() : getEndTime());
  }

  @Override
  public CompiledViewDefinitionWithGraphsImpl getCompiledViewDefinition() {
    return _compiledViewDefinition;
  }

  @Override
  public InMemoryViewComputationResultModel getResultModel() {
    return _resultModel;
  }

  @Override
  public ComputationCacheResponse queryComputationCaches(ComputationCacheQuery query) {
    ArgumentChecker.notNull(query, "query");
    ArgumentChecker.notNull(query.getCalculationConfigurationName(), "calculationConfigurationName");
    ArgumentChecker.notNull(query.getValueSpecifications(), "valueSpecifications");
    ViewComputationCache cache = getComputationCache(query.getCalculationConfigurationName());
    if (cache == null) {
      throw new DataNotFoundException("No computation cache for calculation configuration '" + query.getCalculationConfigurationName()
          + "' was found.");
    }

    Collection<Pair<ValueSpecification, Object>> result = cache.getValues(query.getValueSpecifications());
    ComputationCacheResponse response = new ComputationCacheResponse();
    response.setResults(result);
    return response;
  }

  //--------------------------------------------------------------------------  
  // REVIEW jonathan 2011-03-18 -- The following comment should be given some sort of 'listed' status for preservation :-)
  // REVIEW kirk 2009-11-03 -- This is a database kernel. Act accordingly.

  /**
   * Synchronously runs the cycle.
   * 
   * @param previousCycle the previous cycle from which a delta cycle should be performed, or null to perform a full cycle
   * @param marketDataSnapshot the market data snapshot with which to execute the cycle, not null
   * @param calcJobResultExecutorService the executor to use for streaming calculation job result consumption, not null
   * @throws InterruptedException if the thread is interrupted while waiting for the computation cycle to complete. Execution of any outstanding jobs will be cancelled, but {@link #release()} still
   *           must be called.
   */
  public void execute(SingleComputationCycle previousCycle, MarketDataSnapshot marketDataSnapshot, ExecutorService calcJobResultExecutorService) throws InterruptedException {
    if (_state != ViewCycleState.AWAITING_EXECUTION) {
      throw new IllegalStateException("State must be " + ViewCycleState.AWAITING_EXECUTION);
    }
    _startTime = Instant.now();
    _state = ViewCycleState.EXECUTING;

    createAllCaches();
    prepareInputs(marketDataSnapshot);

    if (previousCycle != null) {
      computeDelta(previousCycle);
    }

    final BlockingQueue<ExecutionResult> calcJobResultQueue = new LinkedBlockingQueue<ExecutionResult>();
    CalculationJobResultStreamConsumer calculationJobResultStreamConsumer = new CalculationJobResultStreamConsumer(calcJobResultQueue, this);
    Future<?> resultStreamConsumerJobInProgress;
    try {
      resultStreamConsumerJobInProgress = calcJobResultExecutorService.submit(calculationJobResultStreamConsumer);

      LinkedList<Future<?>> futures = new LinkedList<Future<?>>();

      for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
        s_logger.info("Executing plans for calculation configuration {}", calcConfigurationName);
        final DependencyGraph depGraph = createExecutableDependencyGraph(calcConfigurationName);
        s_logger.info("Submitting {} for execution by {}", depGraph, getDependencyGraphExecutor());
        final Future<?> future = getDependencyGraphExecutor().execute(depGraph, calcJobResultQueue, _statisticsGatherer);
        futures.add(future);
      }

      while (!futures.isEmpty()) {
        Future<?> future = futures.poll();
        try {
          future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
          s_logger.info("Waiting for " + future);
          futures.add(future);
        } catch (InterruptedException e) {
          Thread.interrupted();
          // Cancel all outstanding jobs to free up resources
          future.cancel(true);
          for (Future<?> incompleteFuture : futures) {
            incompleteFuture.cancel(true);
          }
          _state = ViewCycleState.EXECUTION_INTERRUPTED;
          s_logger.info("Execution interrupted before completion.");
          throw e;
        } catch (ExecutionException e) {
          s_logger.error("Unable to execute dependency graph", e);
          // Should we be swallowing this or not?
          throw new OpenGammaRuntimeException("Unable to execute dependency graph", e);
        }
      }

      _endTime = Instant.now();
    } finally {
      calculationJobResultStreamConsumer.terminate();
    }

    //wait for StreamCalculationJobResultConsumer to finish
    try {
      if (resultStreamConsumerJobInProgress != null) {
        resultStreamConsumerJobInProgress.get();
      }
    } catch (ExecutionException e) {
      Thread.currentThread().interrupt();
    }

    populateResultModel();
    _state = ViewCycleState.EXECUTED;
  }

  /**
   * Creates a map containing the "shift" operations to apply to market data or each calculation configuration. If there is no operation to apply, the map contains null for that configuration.
   * 
   * @return the map of computation cache to shift operations
   */
  private Map<ViewComputationCache, OverrideOperation> getCacheMarketDataOperation() {
    final Map<ViewComputationCache, OverrideOperation> shifts = new HashMap<ViewComputationCache, OverrideOperation>();
    for (ViewCalculationConfiguration calcConfig : getCompiledViewDefinition().getViewDefinition().getAllCalculationConfigurations()) {
      final Set<String> marketDataShift = calcConfig.getDefaultProperties().getValues(MARKET_DATA_SHIFT_PROPERTY);
      OverrideOperation operation = null;
      if (marketDataShift != null) {
        if (marketDataShift.size() != 1) {
          // This doesn't really mean much
          s_logger.error("Market data shift for {} not valid - {}", calcConfig.getName(), marketDataShift);
        } else {
          final String shiftExpr = marketDataShift.iterator().next();
          try {
            operation = getViewProcessContext().getOverrideOperationCompiler().compile(shiftExpr);
          } catch (IllegalArgumentException e) {
            s_logger.error("Market data shift for  {} not valid - {}", calcConfig.getName(), shiftExpr);
            s_logger.info("Invalid market data shift", e);
          }
        }
      }
      shifts.put(getComputationCache(calcConfig.getName()), operation);
    }
    return shifts;
  }

  private void prepareInputs(MarketDataSnapshot snapshot) {
    Set<ValueSpecification> missingMarketData = new HashSet<ValueSpecification>();
    Map<ValueRequirement, ValueSpecification> marketDataRequirements = getCompiledViewDefinition().getMarketDataRequirements();
    s_logger.debug("Populating {} market data items using snapshot {}", marketDataRequirements.size(), snapshot);
    Map<ViewComputationCache, OverrideOperation> cacheMarketDataOperation = getCacheMarketDataOperation();
    InMemoryViewComputationResultModel marketDataResultFragment = constructTemplateResultModel();
    final Map<ValueRequirement, ComputedValue> marketDataValues = snapshot.query(marketDataRequirements.keySet());
    for (Map.Entry<ValueRequirement, ValueSpecification> marketDataRequirement : marketDataRequirements.entrySet()) {
      ComputedValue data = marketDataValues.get(marketDataRequirement.getKey());
      if (data == null) {
        s_logger.debug("Unable to load market data value for {} from snapshot {}", marketDataRequirement, getValuationTime());
        missingMarketData.add(marketDataRequirement.getValue());
        data = new ComputedValue(marketDataRequirement.getValue(), MissingMarketDataSentinel.getInstance());
      } else {
        // REVIEW jonathan 2011-11-17 -- do we really need to include all market data in the result by default?
        getResultModel().addMarketData(data);
        addMarketDataToResultFragment(marketDataResultFragment, marketDataRequirement.getValue(), data);
      }
      addToAllCaches(marketDataRequirement.getKey(), data, cacheMarketDataOperation);
    }
    if (!missingMarketData.isEmpty()) {
      s_logger.info("Missing {} market data elements: {}", missingMarketData.size(), formatMissingLiveData(missingMarketData));
    }
    notifyFragmentCompleted(marketDataResultFragment);
  }

  private static String formatMissingLiveData(Set<ValueSpecification> missingLiveData) {
    StringBuilder sb = new StringBuilder();
    for (ValueSpecification spec : missingLiveData) {
      sb.append("[").append(spec.getValueName()).append(" on ");
      sb.append(spec.getTargetSpecification().getType());
      if (spec.getTargetSpecification().getType() == ComputationTargetType.PRIMITIVE) {
        sb.append("-").append(spec.getTargetSpecification().getIdentifier().getScheme().getName());
      }
      sb.append(":").append(spec.getTargetSpecification().getIdentifier().getValue()).append("] ");
    }
    return sb.toString();
  }

  /**
   *
   */
  private void createAllCaches() {
    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      ViewComputationCache cache = getViewProcessContext().getComputationCacheSource()
          .getCache(getUniqueId(), calcConfigurationName);
      _cachesByCalculationConfiguration.put(calcConfigurationName, cache);
    }
  }

  private void addToAllCaches(final ValueRequirement valueRequirement, final ComputedValue dataAsValue, final Map<ViewComputationCache, OverrideOperation> cacheMarketDataInfo) {
    for (Map.Entry<ViewComputationCache, OverrideOperation> cacheMarketData : cacheMarketDataInfo.entrySet()) {
      final ViewComputationCache cache = cacheMarketData.getKey();
      final ComputedValue cacheValue;
      if ((dataAsValue.getValue() instanceof MissingInput) || (cacheMarketData.getValue() == null)) {
        cacheValue = dataAsValue;
      } else {
        final Object newValue = cacheMarketData.getValue().apply(valueRequirement, dataAsValue.getValue());
        if (newValue != dataAsValue.getValue()) {
          cacheValue = new ComputedValue(dataAsValue.getSpecification(), newValue);
        } else {
          cacheValue = dataAsValue;
        }
      }
      cache.putSharedValue(cacheValue);
    }
  }

  private ViewComputationCache getComputationCache(String calcConfigName) {
    return _cachesByCalculationConfiguration.get(calcConfigName);
  }

  /**
   * Determine which live data inputs have changed between iterations, and:
   * <ul>
   * <li>Copy over all values that can be demonstrated to be the same from the previous iteration (because no input has changed)
   * <li>Only recompute the values that could have changed based on live data inputs
   * </ul>
   * 
   * @param previousCycle Previous iteration. It must not have been cleaned yet ({@link #releaseResources()}).
   */
  private void computeDelta(SingleComputationCycle previousCycle) {
    if (previousCycle.getState() != ViewCycleState.EXECUTED) {
      throw new IllegalArgumentException("State of previous cycle must be " + ViewCycleState.EXECUTED);
    }
    InMemoryViewComputationResultModel deltaResultFragment = constructTemplateResultModel();
    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      final DependencyGraph depGraph = getCompiledViewDefinition().getDependencyGraph(calcConfigurationName);
      ViewComputationCache cache = getComputationCache(calcConfigurationName);
      ViewComputationCache previousCache = previousCycle.getComputationCache(calcConfigurationName);
      LiveDataDeltaCalculator deltaCalculator = new LiveDataDeltaCalculator(depGraph, cache, previousCache);
      deltaCalculator.computeDelta();
      s_logger.info("Computed delta for calculation configuration '{}'. {} nodes out of {} require recomputation.",
          new Object[] {calcConfigurationName, deltaCalculator.getChangedNodes().size(), depGraph.getSize() });
      Collection<ValueSpecification> specsToCopy = new LinkedList<ValueSpecification>();
      Collection<ComputedValue> errors = new LinkedList<ComputedValue>();
      for (DependencyNode unchangedNode : deltaCalculator.getUnchangedNodes()) {
        final NodeStateFlag nodeState = previousCycle.getNodeState(unchangedNode);
        if (nodeState != null) {
          setNodeState(unchangedNode, nodeState);
          if (nodeState == NodeStateFlag.EXECUTED) {
            specsToCopy.addAll(unchangedNode.getOutputValues());
          } else {
            for (ValueSpecification outputValue : unchangedNode.getOutputValues()) {
              errors.add(new ComputedValue(outputValue, NotCalculatedSentinel.SUPPRESSED));
            }
          }
        }
      }
      if (!specsToCopy.isEmpty()) {
        for (ComputedValue value : copyValues(cache, previousCache, specsToCopy)) {
          if (depGraph.getTerminalOutputSpecifications().contains(value.getSpecification())) {
            deltaResultFragment.addValue(calcConfigurationName, value);
          }
        }
      }
      if (!errors.isEmpty()) {
        cache.putSharedValues(errors);
      }
    }
    if (!deltaResultFragment.getAllResults().isEmpty()) {
      notifyFragmentCompleted(deltaResultFragment);
    }
  }

  private Collection<ComputedValue> copyValues(ViewComputationCache cache, ViewComputationCache previousCache, Collection<ValueSpecification> specsToCopy) {
    Collection<Pair<ValueSpecification, Object>> valuesToCopy = previousCache.getValues(specsToCopy);
    Collection<ComputedValue> newValues = new ArrayList<ComputedValue>(valuesToCopy.size());
    for (Pair<ValueSpecification, Object> pair : valuesToCopy) {
      Object previousValue = pair.getSecond();
      if (previousValue != null) {
        newValues.add(new ComputedValue(pair.getFirst(), previousValue));
      } else {
        newValues.add(new ComputedValue(pair.getFirst(), NotCalculatedSentinel.EVALUATION_ERROR));
      }
    }
    cache.putSharedValues(newValues);
    return newValues;
  }

  private void populateResultModel() {
    getResultModel().setCalculationTime(Instant.now());
    getResultModel().setCalculationDuration(getDuration());
    for (DependencyGraph depGraph : getCompiledViewDefinition().getAllDependencyGraphs()) {
      populateResultModel(depGraph);
    }
  }

  private void populateResultModel(DependencyGraph depGraph) {
    ViewComputationCache computationCache = getComputationCache(depGraph.getCalculationConfigurationName());
    for (Pair<ValueSpecification, Object> value : computationCache.getValues(getOutputSpecificationsForResultModel(depGraph), CacheSelectHint.allShared())) {
      if (value.getValue() == null) {
        continue;
      }
      if (value.getValue() instanceof MissingInput) {
        continue;
      }
      getResultModel().addValue(depGraph.getCalculationConfigurationName(), new ComputedValue(value.getFirst(), value.getSecond()));
    }
  }

  /*package*/void calculationJobsCompleted(List<ExecutionResult> results) {
    try {
      ViewComputationResultModel fragmentResult = generateCycleFragmentResult(results);
      if (fragmentResult != null) {
        notifyFragmentCompleted(fragmentResult);
      }
    } catch (Exception e) {
      s_logger.warn("Error forming cycle fragment result after calculation jobs completed: " + results, e);
    }
  }

  private void notifyFragmentCompleted(ViewComputationResultModel fragmentResult) {
    try {
      _cycleFragmentResultListener.resultAvailable(fragmentResult);
    } catch (Exception e) {
      s_logger.warn("Error notifying listener of cycle fragment completion", e);
    }
  }

  private ViewComputationResultModel generateCycleFragmentResult(List<ExecutionResult> calculationJobResults) {
    InMemoryViewComputationResultModel resultModel = constructTemplateResultModel();
    for (ExecutionResult calculationJobResult : calculationJobResults) {
      String calcConfigurationName = calculationJobResult.getResult().getSpecification().getCalcConfigName();
      DependencyGraph depGraph = getCompiledViewDefinition().getDependencyGraph(calcConfigurationName);
      ViewComputationCache computationCache = getComputationCache(calcConfigurationName);
      final Iterator<CalculationJobResultItem> itrResultItem = calculationJobResult.getResult().getResultItems().iterator();
      final Iterator<DependencyNode> itrNode = calculationJobResult.getNodes().iterator();
      while (itrResultItem.hasNext()) {
        assert itrNode.hasNext();
        final CalculationJobResultItem resultItem = itrResultItem.next();
        final DependencyNode node = itrNode.next();
        Set<ValueSpecification> specifications = node.getOutputValues();
        Map<ValueSpecification, Set<ValueRequirement>> requirements = submapByKeySet(depGraph.getTerminalOutputs(), specifications);
        resultModel.addRequirements(requirements);
        for (Pair<ValueSpecification, Object> value : computationCache.getValues(specifications, CacheSelectHint.allShared())) {
          final ValueSpecification valueSpec = value.getFirst();
          final Object calculatedValue = value.getSecond();
          if ((calculatedValue == null) || !requirements.containsKey(valueSpec)) {
            // Not in cache or not a terminal output
            continue;
          }
          if (calculatedValue instanceof MissingMarketDataSentinel) {
            continue;
          }
          ComputedValue computedValue = new ComputedValue(valueSpec, calculatedValue);
          computedValue.setInvocationResult(resultItem.getResult());
          computedValue.setMissingInputs(resultItem.getMissingInputs());
          computedValue.setExceptionClass(resultItem.getExceptionClass());
          computedValue.setExceptionMsg(resultItem.getExceptionMsg());
          computedValue.setStackTrace(resultItem.getStackTrace());
          computedValue.setRequirements(depGraph.getTerminalOutputs().get(valueSpec));
          computedValue.setComputeNodeId(calculationJobResult.getResult().getComputeNodeId());
          resultModel.addValue(calcConfigurationName, computedValue);
        }
      }
    }
    return !resultModel.getAllResults().isEmpty() ? resultModel : null;
  }

  private void addMarketDataToResultFragment(InMemoryViewComputationResultModel result, ValueSpecification marketDataSpecification, ComputedValue marketData) {
    result.addMarketData(marketData);
    for (DependencyGraph depGraph : getCompiledViewDefinition().getAllDependencyGraphs()) {
      if (depGraph.getTerminalOutputSpecifications().contains(marketDataSpecification)) {
        result.addValue(depGraph.getCalculationConfigurationName(), marketData);
      }
    }
  }

  private Set<ValueSpecification> getOutputSpecificationsForResultModel(DependencyGraph depGraph) {
    Set<ValueSpecification> outputSpecifications = new HashSet<ValueSpecification>();
    for (ValueSpecification valueSpecification : depGraph.getOutputSpecifications()) {
      if (getViewDefinition().getResultModelDefinition().shouldOutputResult(valueSpecification, depGraph)) {
        outputSpecifications.add(valueSpecification);
      }
    }
    return outputSpecifications;
  }

  /**
   * Returns the dependency graph used by this cycle for the given calculation configuration.
   * 
   * @param calcConfName calculation configuration name
   * @return the dependency graph
   */
  protected DependencyGraph getDependencyGraph(String calcConfName) {
    return getCompiledViewDefinition().getDependencyGraph(calcConfName);
  }

  /**
   * Creates a subset of the dependency graph for execution. This will only include nodes that do are not dummy ones to source market data, have been considered executed by a delta from the previous
   * cycle, or are being suppressed by the execution blacklist. Note that this will update the cache with synthetic output values from suppressed nodes and alter the execution state of any nodes not
   * in the resultant subgraph.
   * 
   * @param calcConfName calculation configuration name
   * @return a dependency graph with any nodes which have already been satisfied filtered out, not null See {@link #computeDelta} and how it calls {@link #markExecuted}.
   */
  private DependencyGraph createExecutableDependencyGraph(final String calcConfName) {
    final FunctionBlacklistQuery blacklist = getViewProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getGraphExecutionBlacklist();
    return getDependencyGraph(calcConfName).subGraph(new DependencyNodeFilter() {
      public boolean accept(final DependencyNode node) {
        // Market data functions must not be executed
        if (node.getFunction().getFunction() instanceof MarketDataSourcingFunction) {
          markExecuted(node);
          return false;
        }
        // Everything else should be executed unless it was copied from a previous cycle or matched by the blacklist
        final NodeStateFlag state = getNodeState(node);
        if (state != null) {
          return false;
        }
        if (blacklist.isBlacklisted(node)) {
          markSuppressed(node);
          // If the node is suppressed, put values into the cache to indicate this
          final Set<ValueSpecification> outputs = node.getOutputValues();
          final ViewComputationCache cache = getComputationCache(calcConfName);
          if (outputs.size() == 1) {
            cache.putSharedValue(new ComputedValue(outputs.iterator().next(), NotCalculatedSentinel.SUPPRESSED));
          } else {
            final Collection<ComputedValue> errors = new ArrayList<ComputedValue>(outputs.size());
            for (ValueSpecification output : outputs) {
              errors.add(new ComputedValue(output, NotCalculatedSentinel.SUPPRESSED));
            }
            cache.putSharedValues(errors);
          }
          return false;
        }
        return true;
      }
    });
  }

  //--------------------------------------------------------------------------
  @Override
  public void release() {
    if (getState() == ViewCycleState.DESTROYED) {
      throw new IllegalStateException("View cycle " + getUniqueId() + " has already been released");
    }
    if (getViewDefinition().isDumpComputationCacheToDisk()) {
      dumpComputationCachesToDisk();
    }
    getViewProcessContext().getComputationCacheSource().releaseCaches(getUniqueId());
    _state = ViewCycleState.DESTROYED;
  }

  public void dumpComputationCachesToDisk() {
    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      DependencyGraph depGraph = getDependencyGraph(calcConfigurationName);
      ViewComputationCache computationCache = getComputationCache(calcConfigurationName);

      TreeMap<String, Object> key2Value = new TreeMap<String, Object>();
      for (ValueSpecification outputSpec : depGraph.getOutputSpecifications()) {
        Object value = computationCache.getValue(outputSpec);
        key2Value.put(outputSpec.toString(), value);
      }

      try {
        File file = File.createTempFile("computation-cache-" + calcConfigurationName + "-", ".txt");
        s_logger.info("Dumping cache for calc conf " + calcConfigurationName + " to " + file.getAbsolutePath());
        FileWriter writer = new FileWriter(file);
        writer.write(key2Value.toString());
        writer.close();
      } catch (IOException e) {
        throw new RuntimeException("Writing cache to file failed", e);
      }
    }
  }

  private NodeStateFlag getNodeState(final DependencyNode node) {
    return _nodeStates.get(node);
  }

  private void setNodeState(final DependencyNode node, final NodeStateFlag state) {
    _nodeStates.put(node, state);
  }

  public void markExecuted(final DependencyNode node) {
    setNodeState(node, NodeStateFlag.EXECUTED);
  }

  public void markFailed(final DependencyNode node) {
    setNodeState(node, NodeStateFlag.FAILED);
  }

  private void markSuppressed(final DependencyNode node) {
    setNodeState(node, NodeStateFlag.SUPPRESSED);
  }

}
