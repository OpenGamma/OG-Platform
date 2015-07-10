/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.cache.MissingInput;
import com.opengamma.engine.cache.MissingOutput;
import com.opengamma.engine.cache.ViewComputationCache;
import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.calcnode.CalculationJobResultItem;
import com.opengamma.engine.calcnode.CalculationJobResultItemBuilder;
import com.opengamma.engine.calcnode.MutableExecutionLog;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.exec.DefaultAggregatedExecutionLog;
import com.opengamma.engine.exec.DependencyNodeJobExecutionResult;
import com.opengamma.engine.exec.DependencyNodeJobExecutionResultCache;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.resource.EngineResource;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.impl.ViewProcessContext;
import com.opengamma.engine.view.listener.ComputationResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.log.LogLevel;
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

  /**
   * Marker for nodes that have not been executed, for example because of blacklist suppression, calculation error or missing input data (perhaps caused by blacklist suppression or calculation
   * errors).
   */
  private static final DependencyNodeJobExecutionResult BLACKLISTED_NODE_JOB_RESULT = new DependencyNodeJobExecutionResult("", CalculationJobResultItemBuilder
      .of(new MutableExecutionLog(ExecutionLogMode.FULL)).withSuppression().toResultItem(), AggregatedExecutionLog.EMPTY);

  // Injected inputs
  private final UniqueId _cycleId;
  private final String _name;
  private final ViewProcessContext _viewProcessContext;
  private final CompiledViewDefinitionWithGraphs _compiledViewDefinition;
  private final ViewCycleExecutionOptions _executionOptions;
  private final VersionCorrection _versionCorrection;

  private final ComputationResultListener _cycleFragmentResultListener;

  private volatile ViewCycleState _state = ViewCycleState.AWAITING_EXECUTION;

  private volatile Instant _startTime;
  private volatile Instant _endTime;

  private final Map<String, DependencyNodeJobExecutionResultCache> _jobResultCachesByCalculationConfiguration = new ConcurrentHashMap<String, DependencyNodeJobExecutionResultCache>();
  private final Map<String, ViewComputationCache> _cachesByCalculationConfiguration = new HashMap<String, ViewComputationCache>();
  private volatile SingleComputationCycleExecutor _executor;

  // Output
  private final InMemoryViewComputationResultModel _resultModel;

  public SingleComputationCycle(final UniqueId cycleId, final String name, final ComputationResultListener cycleFragmentResultListener, final ViewProcessContext viewProcessContext,
      final CompiledViewDefinitionWithGraphs compiledViewDefinition, final ViewCycleExecutionOptions executionOptions, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(cycleId, "cycleId");
    ArgumentChecker.notNull(cycleFragmentResultListener, "cycleFragmentResultListener");
    ArgumentChecker.notNull(viewProcessContext, "viewProcessContext");
    ArgumentChecker.notNull(compiledViewDefinition, "compiledViewDefinition");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.isFalse(versionCorrection.containsLatest(), "versionCorrection must be fully-resolved");
    _cycleId = cycleId;
    _name = name;
    _viewProcessContext = viewProcessContext;
    _compiledViewDefinition = compiledViewDefinition;
    _cycleFragmentResultListener = cycleFragmentResultListener;
    _executionOptions = executionOptions;
    _versionCorrection = versionCorrection;
    _resultModel = constructTemplateResultModel();
  }

  protected InMemoryViewComputationResultModel constructTemplateResultModel() {
    final InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.setViewCycleId(getCycleId());
    result.setViewProcessId(getViewProcessId());
    result.setViewCycleExecutionOptions(getExecutionOptions());
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

  @Override
  public String getName() {
    return _name;
  }

  /**
   * @return the function initialization identifier
   * @deprecated this needs to go
   */
  @Deprecated
  public long getFunctionInitId() {
    // The cast is only temporary until we've got rid of the function initialisation id
    return ((CompiledViewDefinitionWithGraphsImpl) getCompiledViewDefinition()).getFunctionInitId();
  }

  /**
   * Gets the start time
   * 
   * @return the start timep
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

  public Map<String, ViewComputationCache> getCachesByCalculationConfiguration() {
    return Collections.unmodifiableMap(_cachesByCalculationConfiguration);
  }

  public ViewProcessContext getViewProcessContext() {
    return _viewProcessContext;
  }

  public Set<String> getAllCalculationConfigurationNames() {
    return new HashSet<>(getCompiledViewDefinition().getViewDefinition().getAllCalculationConfigurationNames());
  }

  //-------------------------------------------------------------------------
  private UniqueId getCycleId() {
    return _cycleId;
  }

  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  protected ExecutionLogModeSource getLogModeSource() {
    return _viewProcessContext.getExecutionLogModeSource();
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId getUniqueId() {
    return _cycleId;
  }

  @Override
  public UniqueId getViewProcessId() {
    return _viewProcessContext.getProcessId();
  }

  @Override
  public ViewCycleState getState() {
    return _state;
  }

  @Override
  public Duration getDuration() {
    final ViewCycleState state = getState();
    if (state == ViewCycleState.AWAITING_EXECUTION || state == ViewCycleState.EXECUTION_INTERRUPTED) {
      return null;
    }
    return Duration.between(getStartTime(), getEndTime() == null ? Instant.now() : getEndTime());
  }

  @Override
  public ViewCycleExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinition() {
    return _compiledViewDefinition;
  }

  @Override
  public InMemoryViewComputationResultModel getResultModel() {
    return _resultModel;
  }

  @Override
  public ComputationCacheResponse queryComputationCaches(final ComputationCycleQuery query) {
    ArgumentChecker.notNull(query, "query");
    ArgumentChecker.notNull(query.getCalculationConfigurationName(), "calculationConfigurationName");
    ArgumentChecker.notNull(query.getValueSpecifications(), "valueSpecifications");
    final ViewComputationCache cache = getComputationCache(query.getCalculationConfigurationName());
    if (cache == null) {
      throw new DataNotFoundException("No computation cache for calculation configuration '" + query.getCalculationConfigurationName() + "' was found.");
    }

    final Collection<Pair<ValueSpecification, Object>> result = cache.getValues(query.getValueSpecifications());
    final ComputationCacheResponse response = new ComputationCacheResponse();
    response.setResults(result);
    return response;
  }

  @Override
  public ComputationResultsResponse queryResults(final ComputationCycleQuery query) {
    final DependencyNodeJobExecutionResultCache jobExecutionResultCache = getJobExecutionResultCache(query.getCalculationConfigurationName());
    if (jobExecutionResultCache == null) {
      return null;
    }
    final ComputationCacheResponse cacheResponse = queryComputationCaches(query);
    final Map<ValueSpecification, ComputedValueResult> resultMap = new HashMap<>();
    for (final Pair<ValueSpecification, Object> cacheEntry : cacheResponse.getResults()) {
      final ValueSpecification valueSpec = cacheEntry.getFirst();
      final Object cachedValue = cacheEntry.getSecond();
      final Object value = cachedValue != null ? cachedValue : MissingOutput.EVALUATION_ERROR;
      resultMap.put(valueSpec, createComputedValueResult(valueSpec, value, jobExecutionResultCache.get(valueSpec)));
    }
    final ComputationResultsResponse response = new ComputationResultsResponse();
    response.setResults(resultMap);
    return response;
  }

  protected static ComputedValueResult createComputedValueResult(final ValueSpecification valueSpec, final Object calculatedValue, final DependencyNodeJobExecutionResult jobExecutionResult) {
    if (jobExecutionResult == null) {
      return new ComputedValueResult(valueSpec, calculatedValue, AggregatedExecutionLog.EMPTY, null, null, null);
    } else {
      final CalculationJobResultItem jobResultItem = jobExecutionResult.getJobResultItem();
      return new ComputedValueResult(valueSpec, calculatedValue, jobExecutionResult.getAggregatedExecutionLog(), jobExecutionResult.getComputeNodeId(), jobResultItem.getMissingInputs(),
          jobResultItem.getResult());
    }
  }

  /**
   * Prepares the cycle for execution, organising the caches and copying any values salvaged from a previous cycle.
   * 
   * @param previousCycle the previous cycle from which a delta cycle should be performed, or null to perform a full cycle
   * @param marketDataSnapshot the market data snapshot with which to execute the cycle, not null
   * @param suppressExecutionOnNoMarketData true if execution is to be suppressed when input data is entirely missing, false otherwise
   * @return true if execution should continue, false if execution should be suppressed
   */
  public boolean preExecute(final SingleComputationCycle previousCycle, final MarketDataSnapshot marketDataSnapshot, final boolean suppressExecutionOnNoMarketData) {
    if (_state != ViewCycleState.AWAITING_EXECUTION) {
      throw new IllegalStateException("State must be " + ViewCycleState.AWAITING_EXECUTION);
    }
    _startTime = Instant.now();
    _state = ViewCycleState.EXECUTING;
    createAllCaches();
    if (!prepareInputs(marketDataSnapshot, suppressExecutionOnNoMarketData)) {
      generateSuppressedOutputs();
      return false;
    }
    if (previousCycle != null) {
      computeDelta(previousCycle);
    }
    return true;
  }

  protected Map<ValueSpecification, FunctionParameters> createFunctionParameters(final String calcConfigurationName) {
    final CompiledViewCalculationConfiguration calculationConfiguration = _compiledViewDefinition.getCompiledCalculationConfiguration(calcConfigurationName);
    final Map<DistinctMarketDataSelector, Set<ValueSpecification>> marketDataSelections = calculationConfiguration.getMarketDataSelections();
    if (marketDataSelections.isEmpty()) {
      s_logger.info("No market data selections defined in graph [{}]", calcConfigurationName);
      return Collections.<ValueSpecification, FunctionParameters>emptyMap();
    }
    s_logger.info("Building function parameters for market data manipulation in graph [{}]", calcConfigurationName);
    final Map<ValueSpecification, FunctionParameters> parameters = new HashMap<ValueSpecification, FunctionParameters>();
    // Get function params configured through the view definition
    final Map<DistinctMarketDataSelector, FunctionParameters> functionParameters = Maps.newHashMap(calculationConfiguration.getMarketDataSelectionFunctionParameters());
    s_logger.info("Added in function parameters from view definition - now have {} entries", functionParameters.size());
    // Add the function params passed through the execution options which will
    // potentially override the same functions from the view definition
    // A future enhancement could look at merging/composing the functions if desired
    functionParameters.putAll(_executionOptions.getFunctionParameters());
    s_logger.info("Added in function parameters from execution options - now have {} entries", functionParameters.size());
    int nodeCount = 0;
    for (Map.Entry<DistinctMarketDataSelector, Set<ValueSpecification>> entry : marketDataSelections.entrySet()) {
      final DistinctMarketDataSelector selector = entry.getKey();
      final Set<ValueSpecification> matchingSpecifications = entry.getValue();
      final FunctionParameters nodeParameters;
      if (functionParameters.containsKey(selector)) {
        nodeParameters = functionParameters.get(selector);
      } else {
        nodeParameters = new EmptyFunctionParameters();
      }
      for (ValueSpecification valueSpecification : matchingSpecifications) {
        parameters.put(valueSpecification, nodeParameters);
        nodeCount++;
      }
    }
    s_logger.info("Inserted manipulation functions and parameters for {} nodes", nodeCount);
    return parameters;
  }

  /**
   * Completes the execution cycle.
   */
  public void postExecute() {
    completeResultModel();
    _state = ViewCycleState.EXECUTED;
    _endTime = Instant.now();
  }

  // REVIEW jonathan 2011-03-18 -- The following comment should be given some sort of 'listed' status for preservation :-)
  // REVIEW kirk 2009-11-03 -- This is a database kernel. Act accordingly.

  /**
   * Synchronously runs the cycle.
   * 
   * @throws InterruptedException if the thread is interrupted while waiting for the computation cycle to complete. Execution of any outstanding jobs will be cancelled, but {@link #release()} still
   *           must be called.
   */
  public void execute() throws InterruptedException {
    _executor = new SingleComputationCycleExecutor(this);
    try {
      _executor.execute();
    } catch (InterruptedException e) {
      Thread.interrupted();
      _state = ViewCycleState.EXECUTION_INTERRUPTED;
      s_logger.info("Execution interrupted before completion.");
    } finally {
      _executor = null;
    }
  }

  /**
   * Adds suppressed output markers to the result model for all terminal outputs.
   */
  private void generateSuppressedOutputs() {
    final InMemoryViewComputationResultModel fullResultModel = getResultModel();
    final InMemoryViewComputationResultModel fragmentResultModel = constructTemplateResultModel();
    for (CompiledViewCalculationConfiguration compiledCalcConfig : getCompiledViewDefinition().getCompiledCalculationConfigurations()) {
      String calcConfigName = compiledCalcConfig.getName();
      for (ValueSpecification valueSpec : compiledCalcConfig.getTerminalOutputSpecifications().keySet()) {
        ComputedValue value = new ComputedValue(valueSpec, MissingOutput.SUPPRESSED);
        ComputedValueResult valueResult = new ComputedValueResult(value, AggregatedExecutionLog.EMPTY);
        fragmentResultModel.addValue(calcConfigName, valueResult);
        fullResultModel.addValue(calcConfigName, valueResult);
      }
    }
    fragmentResultModel.setCalculationTime(Instant.now());
    notifyFragmentCompleted(fragmentResultModel);
  }

  /**
   * Fetches the override operation to apply to market data in the given configuration. If there is no operation to apply, returns null.
   * 
   * @return the shift operation, or null for non
   */
  private OverrideOperation getCacheMarketDataOperation(ViewCalculationConfiguration calcConfig) {
    final Set<String> marketDataShift = calcConfig.getDefaultProperties().getValues(MARKET_DATA_SHIFT_PROPERTY);
    OverrideOperation operation = null;
    if (marketDataShift != null) {
      if (marketDataShift.size() != 1) {
        // This doesn't really mean much
        s_logger.error("Market data shift for {} not valid - {}", calcConfig.getName(), marketDataShift);
      } else {
        final OverrideOperationCompiler compiler = getViewProcessContext().getOverrideOperationCompiler();
        final ComputationTargetResolver.AtVersionCorrection resolver = getViewProcessContext().getFunctionCompilationService().getFunctionCompilationContext()
            .getRawComputationTargetResolver().atVersionCorrection(getVersionCorrection());
        final String shiftExpr = marketDataShift.iterator().next();
        try {
          operation = compiler.compile(shiftExpr, resolver);
        } catch (final IllegalArgumentException e) {
          s_logger.error("Market data shift for  {} not valid - {}", calcConfig.getName(), shiftExpr);
          s_logger.info("Invalid market data shift", e);
        }
      }
    }
    return operation;
  }

  private static final DefaultAggregatedExecutionLog MARKET_DATA_LOG = DefaultAggregatedExecutionLog.indicatorLogMode(EnumSet.of(LogLevel.WARN));

  /**
   * Populates the value cache with the required input data.
   * 
   * @param snapshot the market data snapshot from which to source the input data, not null
   * @param suppressExecutionOnNoMarketData true if execution is to be suppressed when input data is entirely missing, false otherwise
   * @return true if execution should continue, false if execution should be suppressed
   */
  private boolean prepareInputs(final MarketDataSnapshot snapshot, boolean suppressExecutionOnNoMarketData) {
    int missingMarketData = 0;
    final Set<ValueSpecification> allRequiredMarketData = getCompiledViewDefinition().getMarketDataRequirements();
    s_logger.debug("Populating {} market data items using snapshot {}", allRequiredMarketData.size(), snapshot);
    final InMemoryViewComputationResultModel fragmentResultModel = constructTemplateResultModel();
    final InMemoryViewComputationResultModel fullResultModel = getResultModel();
    final Map<ValueSpecification, Object> marketDataValues = snapshot.query(allRequiredMarketData);
    if (suppressExecutionOnNoMarketData && allRequiredMarketData.size() > 0 && marketDataValues.size() == 0) {
      // Market data was expected but the snapshot was empty. Don't bother doing anything else, and indicate that
      // execution should not continue.
      return false;
    }
    final ResultModelDefinition resultModel = getViewDefinition().getResultModelDefinition();
    for (CompiledViewCalculationConfiguration calcConfig : getCompiledViewDefinition().getCompiledCalculationConfigurations()) {
      final OverrideOperation operation = getCacheMarketDataOperation(getViewDefinition().getCalculationConfiguration(calcConfig.getName()));
      final ViewComputationCache cache = getComputationCache(calcConfig.getName());
      final Collection<ValueSpecification> marketDataRequirements = calcConfig.getMarketDataRequirements();
      final Set<ValueSpecification> terminalOutputs = calcConfig.getTerminalOutputSpecifications().keySet();
      final Collection<ComputedValueResult> valuesToLoad = new ArrayList<>(marketDataRequirements.size());
      for (ValueSpecification marketDataSpec : marketDataRequirements) {
        Object marketDataValue = marketDataValues.get(marketDataSpec);
        ComputedValueResult computedValueResult;
        if (operation != null) {
          if (marketDataValue != null) {
            marketDataValue = operation.apply(marketDataSpec.toRequirementSpecification(), marketDataValue);
            if (marketDataValue == null) {
              s_logger.debug("Market data {} discarded by override operation", marketDataSpec);
            }
          }
        }
        if (marketDataValue == null) {
          s_logger.debug("Unable to load market data value for {} from snapshot {}", marketDataSpec, getValuationTime());
          missingMarketData++;
          // TODO provide elevated logs if requested from market data providers
          computedValueResult = new ComputedValueResult(marketDataSpec, MissingInput.MISSING_MARKET_DATA, MARKET_DATA_LOG);
          ComputedValueResult resultModelValueResult = new ComputedValueResult(marketDataSpec, null, AggregatedExecutionLog.EMPTY);
          fragmentResultModel.addMarketData(resultModelValueResult);
          fullResultModel.addMarketData(resultModelValueResult);
        } else {
          computedValueResult = new ComputedValueResult(marketDataSpec, marketDataValue, AggregatedExecutionLog.EMPTY);
          fragmentResultModel.addMarketData(computedValueResult);
          fullResultModel.addMarketData(computedValueResult);
        }
        if (terminalOutputs.contains(marketDataSpec) && (resultModel.getOutputMode(marketDataSpec.getTargetSpecification().getType()) != ResultOutputMode.NONE)) {
          fragmentResultModel.addValue(calcConfig.getName(), computedValueResult);
          fullResultModel.addValue(calcConfig.getName(), computedValueResult);
        }
        valuesToLoad.add(computedValueResult);
      }
      if (!valuesToLoad.isEmpty()) {
        cache.putSharedValues(valuesToLoad);
      }
    }
    if (missingMarketData > 0) {
      // REVIEW jonathan 2012-11-01 -- probably need a cycle-level execution log for things like this
      s_logger.info("Missing {} market data elements", missingMarketData);
    }
    fragmentResultModel.setCalculationTime(Instant.now());
    notifyFragmentCompleted(fragmentResultModel);
    return true;
  }

  /**
   * Ensures that a computation cache exists for for each calculation configuration.
   */
  private void createAllCaches() {
    for (final String calcConfigurationName : getAllCalculationConfigurationNames()) {
      final ViewComputationCache cache = getViewProcessContext().getComputationCacheSource().getCache(getUniqueId(), calcConfigurationName);
      _cachesByCalculationConfiguration.put(calcConfigurationName, cache);
      _jobResultCachesByCalculationConfiguration.put(calcConfigurationName, new DependencyNodeJobExecutionResultCache());
    }
  }

  protected ViewComputationCache getComputationCache(final String calcConfigName) {
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
  private void computeDelta(final SingleComputationCycle previousCycle) {
    if (previousCycle.getState() != ViewCycleState.EXECUTED) {
      throw new IllegalArgumentException("State of previous cycle must be " + ViewCycleState.EXECUTED);
    }
    final FunctionParametersDelta parameterDelta = FunctionParametersDelta.of(previousCycle.getExecutionOptions(), getExecutionOptions());
    final InMemoryViewComputationResultModel fragmentResultModel = constructTemplateResultModel();
    final InMemoryViewComputationResultModel fullResultModel = getResultModel();
    final CompiledViewDefinition previousViewDefinition = previousCycle.getCompiledViewDefinition();
    final CompiledViewDefinitionWithGraphs viewDefinition = getCompiledViewDefinition();
    for (final DependencyGraphExplorer depGraphExplorer : viewDefinition.getDependencyGraphExplorers()) {
      final DependencyGraph depGraph = depGraphExplorer.getWholeGraph();
      final String calcConfig = depGraph.getCalculationConfigurationName();
      final ViewComputationCache cache = getComputationCache(calcConfig);
      final ViewComputationCache previousCache = previousCycle.getComputationCache(calcConfig);
      final DependencyNodeJobExecutionResultCache jobExecutionResultCache = getJobExecutionResultCache(calcConfig);
      final DependencyNodeJobExecutionResultCache previousJobExecutionResultCache = previousCycle.getJobExecutionResultCache(calcConfig);
      final LiveDataDeltaCalculator deltaCalculator = new LiveDataDeltaCalculator(depGraph, cache, previousCache, parameterDelta.getValueSpecifications(calcConfig, previousViewDefinition,
          viewDefinition));
      deltaCalculator.computeDelta();
      s_logger.info("Computed delta for calculation configuration '{}'. {} nodes out of {} require recomputation.", calcConfig, deltaCalculator.getChangedNodes().size(), depGraph.getSize());
      final Collection<ValueSpecification> specsToCopy = new LinkedList<>();
      final Collection<ComputedValue> errors = new LinkedList<>();
      for (final DependencyNode unchangedNode : deltaCalculator.getUnchangedNodes()) {
        if (MarketDataSourcingFunction.UNIQUE_ID.equals(unchangedNode.getFunction().getFunctionId())) {
          // Market data is already in the cache, so don't need to copy it across again
          continue;
        }
        final DependencyNodeJobExecutionResult previousExecutionResult = previousJobExecutionResultCache.get(unchangedNode);
        if (previousExecutionResult == null) {
          // Nothing to reuse
          continue;
        }
        if (getLogModeSource().getLogMode(calcConfig, unchangedNode.getOutputValue(0)) == ExecutionLogMode.FULL &&
            previousExecutionResult.getJobResultItem().getExecutionLog().getEvents() == null) {
          // Need to rerun calculation to collect logs, so cannot reuse
          continue;
        }
        final int outputs = unchangedNode.getOutputCount();
        if (previousExecutionResult.getJobResultItem().isFailed()) {
          for (int i = 0; i < outputs; i++) {
            errors.add(new ComputedValue(unchangedNode.getOutputValue(i), MissingOutput.SUPPRESSED));
          }
        } else {
          for (int i = 0; i < outputs; i++) {
            specsToCopy.add(unchangedNode.getOutputValue(i));
          }
        }
        jobExecutionResultCache.put(unchangedNode, previousExecutionResult);
      }
      if (!specsToCopy.isEmpty()) {
        final ComputationCycleQuery reusableResultsQuery = new ComputationCycleQuery();
        reusableResultsQuery.setCalculationConfigurationName(depGraph.getCalculationConfigurationName());
        reusableResultsQuery.setValueSpecifications(specsToCopy);
        final ComputationResultsResponse reusableResultsQueryResponse = previousCycle.queryResults(reusableResultsQuery);
        final Map<ValueSpecification, ComputedValueResult> resultsToReuse = reusableResultsQueryResponse.getResults();
        final Collection<ComputedValue> newValues = new ArrayList<>(resultsToReuse.size());
        final Map<ValueSpecification, ?> terminalOutputs = depGraph.getTerminalOutputs();
        for (final ComputedValueResult computedValueResult : resultsToReuse.values()) {
          final ValueSpecification valueSpec = computedValueResult.getSpecification();
          if (terminalOutputs.containsKey(valueSpec) && getViewDefinition().getResultModelDefinition().shouldOutputResult(valueSpec, depGraph)) {
            fragmentResultModel.addValue(depGraph.getCalculationConfigurationName(), computedValueResult);
            fullResultModel.addValue(depGraph.getCalculationConfigurationName(), computedValueResult);
          }
          final Object previousValue = computedValueResult.getValue() != null ? computedValueResult.getValue() : MissingOutput.EVALUATION_ERROR;
          newValues.add(new ComputedValue(valueSpec, previousValue));
        }
        cache.putSharedValues(newValues);
      }
      if (!errors.isEmpty()) {
        cache.putSharedValues(errors);
      }
    }
    if (!fragmentResultModel.getAllResults().isEmpty()) {
      fragmentResultModel.setCalculationTime(Instant.now());
      notifyFragmentCompleted(fragmentResultModel);
    }
  }

  private void completeResultModel() {
    getResultModel().setCalculationTime(Instant.now());
    getResultModel().setCalculationDuration(getDuration());
  }

  protected void notifyFragmentCompleted(final ViewComputationResultModel fragmentResult) {
    try {
      _cycleFragmentResultListener.resultAvailable(fragmentResult);
    } catch (final Exception e) {
      s_logger.warn("Error notifying listener of cycle fragment completion", e);
    }
  }

  /**
   * Returns the dependency graph used by this cycle for the given calculation configuration.
   * 
   * @param calcConfName calculation configuration name
   * @return the dependency graph
   */
  protected DependencyGraph getDependencyGraph(final String calcConfName) {
    return getCompiledViewDefinition().getDependencyGraphExplorer(calcConfName).getWholeGraph();
  }

  /**
   * Returns the set of values already calculated and in the shared cache at execution of the graph, or that have been blacklisted. This will control the subset of the graph that is used to create the
   * jobs.
   * 
   * @param calcConfName the calculation configuration name to query
   * @return the values in the shared cache, not null
   */
  protected Set<ValueSpecification> getSharedValues(final String calcConfName) {
    // Get the market data
    final Set<ValueSpecification> sharedValues = new HashSet<ValueSpecification>(getCompiledViewDefinition().getCompiledCalculationConfiguration(calcConfName).getMarketDataRequirements());
    // Add anything kept as part of a delta cycle
    final DependencyNodeJobExecutionResultCache jobCache = getJobExecutionResultCache(calcConfName);
    sharedValues.addAll(jobCache.getExecutedData());
    // Handle blacklisted nodes
    final FunctionBlacklistQuery blacklist = getViewProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getGraphExecutionBlacklist();
    if (!blacklist.isEmpty()) {
      final Iterator<DependencyNode> nodes = getDependencyGraph(calcConfName).nodeIterator();
      Collection<ComputedValue> errors = null;
      while (nodes.hasNext()) {
        final DependencyNode node = nodes.next();
        if (blacklist.isBlacklisted(node)) {
          final int outputs = node.getOutputCount();
          if (errors == null) {
            errors = new LinkedList<ComputedValue>();
          }
          for (int i = 0; i < outputs; i++) {
            final ValueSpecification output = node.getOutputValue(i);
            errors.add(new ComputedValue(node.getOutputValue(i), MissingOutput.SUPPRESSED));
            sharedValues.add(output);
          }
          jobCache.put(node, BLACKLISTED_NODE_JOB_RESULT);
        }
      }
      if (errors != null) {
        final ViewComputationCache cache = getComputationCache(calcConfName);
        cache.putSharedValues(errors);
      }
    }
    return sharedValues;
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
    for (final String calcConfigurationName : getAllCalculationConfigurationNames()) {
      final DependencyGraph depGraph = getDependencyGraph(calcConfigurationName);
      final ViewComputationCache computationCache = getComputationCache(calcConfigurationName);
      final TreeMap<String, Object> key2Value = new TreeMap<>();
      for (final ValueSpecification outputSpec : DependencyGraphImpl.getAllOutputSpecifications(depGraph)) {
        final Object value = computationCache.getValue(outputSpec);
        key2Value.put(outputSpec.toString(), value);
      }
      try {
        final File file = File.createTempFile("computation-cache-" + calcConfigurationName + "-", ".txt");
        s_logger.info("Dumping cache for calc conf " + calcConfigurationName + " to " + file.getAbsolutePath());
        final FileWriter writer = new FileWriter(file);
        writer.write(key2Value.toString());
        writer.close();
      } catch (final IOException e) {
        throw new RuntimeException("Writing cache to file failed", e);
      }
    }
  }

  /**
   * Receives a job result fragment. These will be streamed in by the execution framework. Only one notification per job will be received (for example the execution framework might have
   * repeated/duplicated jobs to handle node failures).
   * 
   * @param job the job that was executed, not null
   * @param jobResult the job result, not null
   */
  public void jobCompleted(final CalculationJob job, final CalculationJobResult jobResult) {
    final SingleComputationCycleExecutor executor = _executor;
    if (executor != null) {
      executor.jobCompleted(job, jobResult);
    }
  }

  protected DependencyNodeJobExecutionResultCache getJobExecutionResultCache(final String calcConfigName) {
    return _jobResultCachesByCalculationConfiguration.get(calcConfigName);
  }

  @Override
  public String toString() {
    return "ComputationCycle-" + _cycleId.toString();
  }

}
