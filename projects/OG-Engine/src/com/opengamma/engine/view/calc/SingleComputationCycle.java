/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.time.Duration;
import javax.time.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.listener.ComputationCycleResultListener;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.functional.Function1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessContext;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.cache.MissingMarketDataSentinel;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

import static com.opengamma.util.functional.Functional.flatMap;
import static com.opengamma.util.functional.Functional.map;
import static com.opengamma.util.functional.Functional.submapByKeySet;

/**
 * Holds all data and actions for a single computation pass. The view cycle may be executed at most once.
 * <p>
 * The cycle is thread-safe for readers, for example obtaining the current state or the result, but is only designed
 * for a single executor.
 */
public class SingleComputationCycle implements ViewCycle, EngineResource {
  private static final Logger s_logger = LoggerFactory.getLogger(SingleComputationCycle.class);

  // Injected inputs
  private final UniqueId _cycleId;
  private final UniqueId _viewProcessId;
  private final ViewProcessContext _viewProcessContext;
  private final CompiledViewDefinitionWithGraphsImpl _compiledViewDefinition;
  private final ViewCycleExecutionOptions _executionOptions;
  
  private final ComputationCycleResultListener _computationCycleResultListener;
  private final DependencyGraphExecutor<?> _dependencyGraphExecutor;
  private final GraphExecutorStatisticsGatherer _statisticsGatherer;

  private volatile ViewCycleState _state = ViewCycleState.AWAITING_EXECUTION;

  private volatile Instant _startTime;
  private volatile Instant _endTime;

  private final Set<DependencyNode> _executedNodes = Collections.newSetFromMap(new ConcurrentHashMap<DependencyNode, Boolean>());
  private final Set<DependencyNode> _failedNodes = Collections.newSetFromMap(new ConcurrentHashMap<DependencyNode, Boolean>());
  private final Map<String, ViewComputationCache> _cachesByCalculationConfiguration = new HashMap<String, ViewComputationCache>();

  // Output
  private final InMemoryViewComputationResultModel _resultModel;

  public SingleComputationCycle(UniqueId cycleId, UniqueId viewProcessId, ComputationCycleResultListener computationCycleResultListener,

      ViewProcessContext viewProcessContext, CompiledViewDefinitionWithGraphsImpl compiledViewDefinition,
      ViewCycleExecutionOptions executionOptions, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(cycleId, "cycleId");
    ArgumentChecker.notNull(viewProcessId, "viewProcessId");
    ArgumentChecker.notNull(computationCycleResultListener, "computationCycleResultListener");
    ArgumentChecker.notNull(viewProcessContext, "viewProcessContext");
    ArgumentChecker.notNull(compiledViewDefinition, "compiledViewDefinition");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.isFalse(versionCorrection.containsLatest(), "versionCorrection must be fully-resolved");

    _cycleId = cycleId;
    _viewProcessId = viewProcessId;
    _viewProcessContext = viewProcessContext;
    _compiledViewDefinition = compiledViewDefinition;
    
    _computationCycleResultListener = computationCycleResultListener;

    _executionOptions = executionOptions;

    _resultModel = new InMemoryViewComputationResultModel();
    _resultModel.setCalculationConfigurationNames(getCompiledViewDefinition().getViewDefinition().getAllCalculationConfigurationNames());
    if (getCompiledViewDefinition().getPortfolio() != null) {
      _resultModel.setPortfolio(getCompiledViewDefinition().getPortfolio());
    }
    _resultModel.setViewCycleId(cycleId);
    _resultModel.setViewProcessId(getViewProcessId());
    _resultModel.setValuationTime(executionOptions.getValuationTime());
    _resultModel.setVersionCorrection(versionCorrection);

    _dependencyGraphExecutor = getViewProcessContext().getDependencyGraphExecutorFactory().createExecutor(this);
    _statisticsGatherer = getViewProcessContext().getGraphExecutorStatisticsGathererProvider().getStatisticsGatherer(getViewProcessId());
  }

  //-------------------------------------------------------------------------
  public Instant getValuationTime() {
    return _executionOptions.getValuationTime();
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
   * @param previousCycle  the previous cycle from which a delta cycle should be performed, or null to perform
   *                       a full cycle
   * @param marketDataSnapshot  the market data snapshot with which to execute the cycle, not null
   * @throws InterruptedException  if the thread is interrupted while waiting for the computation cycle to complete.
   *                               Execution of any outstanding jobs will be cancelled, but {@link #release()}
   *                               still must be called.
   */
  public void execute(SingleComputationCycle previousCycle, MarketDataSnapshot marketDataSnapshot) throws InterruptedException {
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

    // This job is consuming calculation jobs from the queue, which are enqueued by dependency graph executor
    // the job results are streamed to the ViewProcesor without waitout waiting for the current cycle to complete
    final BlockingQueue<CalculationJobResult> calcJobResultQueue = new LinkedBlockingQueue<CalculationJobResult>();
    class StreamCalculationJobResultConsumer extends TerminatableJob {
      boolean completeAndExit = false;
      @Override
      protected void runOneCycle() {
        try {
          if(completeAndExit){

          }
          CalculationJobResult jobResult = calcJobResultQueue.poll(50, TimeUnit.MILLISECONDS);
          if(jobResult != null){
            _computationCycleResultListener.jobResultReceived(populateResultModel(jobResult));
          }else if(completeAndExit){
            this.terminate();
          }
        } catch (InterruptedException e) {
          this.terminate();
        }
      }

      public void completeAndExit(){
        completeAndExit = true;
      }
    }
    StreamCalculationJobResultConsumer streamCalculationJobResultConsumer =  new StreamCalculationJobResultConsumer();
    Thread streamCalculationJobResultConsumerThread = new Thread(streamCalculationJobResultConsumer, "Computation job for " + this);
    streamCalculationJobResultConsumerThread.start();
    // ~

    LinkedList<Future<?>> futures = new LinkedList<Future<?>>();

    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      s_logger.info("Executing plans for calculation configuration {}", calcConfigurationName);
      DependencyGraph depGraph = getExecutableDependencyGraph(calcConfigurationName);

      s_logger.info("Submitting {} for execution by {}", depGraph, getDependencyGraphExecutor());

      Future<?> future = getDependencyGraphExecutor().execute(depGraph, calcJobResultQueue, _statisticsGatherer);
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

    streamCalculationJobResultConsumer.completeAndExit();
    streamCalculationJobResultConsumerThread.join();

    populateResultModel();
    _state = ViewCycleState.EXECUTED;
  }

  /**
   * Creates a map containing the "shift" operations to apply to market data or each
   * calculation configuration. If there is no operation to apply, the map contains
   * null for that configuration.
   * 
   * @return the map of computation cache to shift operations 
   */
  private Map<ViewComputationCache, OverrideOperation> getCacheMarketDataOperation() {
    final Map<ViewComputationCache, OverrideOperation> shifts = new HashMap<ViewComputationCache, OverrideOperation>();
    for (ViewCalculationConfiguration calcConfig : getCompiledViewDefinition().getViewDefinition().getAllCalculationConfigurations()) {
      final Set<String> marketDataShift = calcConfig.getDefaultProperties().getValues("MARKET_DATA_SHIFT");
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
    for (Map.Entry<ValueRequirement, ValueSpecification> marketDataRequirement : marketDataRequirements.entrySet()) {
      // REVIEW 2010-10-22 Andrew
      // If we're asking the snapshot for a "requirement" then it should give back a more detailed "specification" with the data (i.e. a
      // ComputedValue instance where the specification satisfies the requirement). Functions should then declare their requirements and
      // not the exact specification they want for market data. Alternatively, if the snapshot will give us the exact value we ask for then
      // we should be querying with a "specification" and not a requirement.
      final Object data = snapshot.query(marketDataRequirement.getKey());
      final ComputedValue dataAsValue;
      if (data == null) {
        s_logger.debug("Unable to load market data value for {} from snapshot {}", marketDataRequirement, getValuationTime());
        missingMarketData.add(marketDataRequirement.getValue());
        dataAsValue = new ComputedValue(marketDataRequirement.getValue(), MissingMarketDataSentinel.getInstance());
      } else {
        dataAsValue = new ComputedValue(marketDataRequirement.getValue(), data);
        getResultModel().addMarketData(dataAsValue);
      }
      addToAllCaches(marketDataRequirement.getKey(), dataAsValue, cacheMarketDataOperation);
    }
    if (!missingMarketData.isEmpty()) {
      s_logger.warn("Missing {} market data elements: {}", missingMarketData.size(), formatMissingLiveData(missingMarketData));
    }
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
      if (cacheMarketData.getValue() == null) {
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

    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      DependencyGraph depGraph = getCompiledViewDefinition().getDependencyGraph(calcConfigurationName);

      ViewComputationCache cache = getComputationCache(calcConfigurationName);
      ViewComputationCache previousCache = previousCycle.getComputationCache(calcConfigurationName);

      LiveDataDeltaCalculator deltaCalculator = new LiveDataDeltaCalculator(depGraph, cache, previousCache);
      deltaCalculator.computeDelta();

      s_logger.info("Computed delta for calculation configuration '{}'. {} nodes out of {} require recomputation.",
          new Object[] {calcConfigurationName, deltaCalculator.getChangedNodes().size(), depGraph.getSize() });

      Collection<ValueSpecification> specsToCopy = new HashSet<ValueSpecification>();

      for (DependencyNode unchangedNode : deltaCalculator.getUnchangedNodes()) {
        if (previousCycle.isExecuted(unchangedNode)) {
          markExecuted(unchangedNode);
          if (previousCycle.isFailed(unchangedNode)) {
            markFailed(unchangedNode);
          } else {
            specsToCopy.addAll(unchangedNode.getOutputValues());
          }
        }
      }
      if (!specsToCopy.isEmpty()) {
        copyValues(cache, previousCache, specsToCopy);
      }
    }
  }

  private void copyValues(ViewComputationCache cache, ViewComputationCache previousCache, Collection<ValueSpecification> specsToCopy) {
    Collection<Pair<ValueSpecification, Object>> valuesToCopy = previousCache.getValues(specsToCopy);

    Collection<ComputedValue> newValues = new HashSet<ComputedValue>();
    for (Pair<ValueSpecification, Object> pair : valuesToCopy) {
      Object previousValue = pair.getSecond();
      if (previousValue != null) {
        newValues.add(new ComputedValue(pair.getFirst(), previousValue));
      }
    }
    cache.putSharedValues(newValues);
  }

  private void populateResultModel() {
    getResultModel().setCalculationTime(Instant.now());
    getResultModel().setCalculationDuration(getDuration());
    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      DependencyGraph depGraph = getCompiledViewDefinition().getDependencyGraph(calcConfigurationName);
      populateResultModel(calcConfigurationName, depGraph);
    }
  }

  private void populateResultModel(String calcConfigurationName, DependencyGraph depGraph) {
    ViewComputationCache computationCache = getComputationCache(calcConfigurationName);
    
    for (Pair<ValueSpecification, Object> value : computationCache.getValues(getOutputSpecificationsForResultModel(depGraph), CacheSelectHint.allShared())) {
      if (value.getValue() == null) {
        continue;
      }
      if (value.getSecond() instanceof MissingMarketDataSentinel) {
        continue;
      }
      getResultModel().addValue(calcConfigurationName, new ComputedValue(value.getFirst(), value.getSecond()));
    }
    getResultModel().addRequirements(depGraph.getTerminalOutputs());
  }
  
  private ViewComputationResultModel populateResultModel(CalculationJobResult calculationJobResult) {
    InMemoryViewComputationResultModel resultModel = new InMemoryViewComputationResultModel();
    String calcConfigurationName = calculationJobResult.getSpecification().getCalcConfigName();
    DependencyGraph depGraph = getCompiledViewDefinition().getDependencyGraph(calcConfigurationName);

    ViewComputationCache computationCache = getComputationCache(calcConfigurationName);

    //extracts set of ValueSpecification out of calculation job result items.
    Set<ValueSpecification> specifications = flatMap(new HashSet<ValueSpecification>(), calculationJobResult.getResultItems(), new Function1<CalculationJobResultItem, Collection<ValueSpecification>>() {
      @Override
      public Set<ValueSpecification> execute(CalculationJobResultItem calculationJobResultItem) {
        calculationJobResultItem.getItem().getDesiredValues();
        calculationJobResultItem.getItem().getDesiredValues();
        return calculationJobResultItem.getOutputs();
      }
    });
    
    for (Pair<ValueSpecification, Object> value : computationCache.getValues(specifications, CacheSelectHint.allShared())) {
      if (value.getValue() == null) {
        continue;
      }
      if (value.getSecond() instanceof MissingMarketDataSentinel) {
        continue;
      }
      resultModel.addValue(calcConfigurationName, new ComputedValue(value.getFirst(), value.getSecond()));
    }

    Map<ValueSpecification, Set<ValueRequirement>> requirements = submapByKeySet(depGraph.getTerminalOutputs(), specifications);
    resultModel.addRequirements(requirements);
    return resultModel;
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

  private DependencyGraph getDependencyGraph(String calcConfName) {
    DependencyGraph depGraph = getCompiledViewDefinition().getDependencyGraph(calcConfName);
    return depGraph;
  }

  /**
   * @param calcConfName  calculation configuration name
   * @return a dependency graph with any nodes which have already been satisfied filtered out, not {@code null}
   * See {@link #computeDelta} and how it calls {@link #markExecuted}.
   */
  protected DependencyGraph getExecutableDependencyGraph(String calcConfName) {
    DependencyGraph originalDepGraph = getDependencyGraph(calcConfName);
    DependencyGraph dependencyGraph = originalDepGraph.subGraph(new DependencyNodeFilter() {
      public boolean accept(DependencyNode node) {
        // Market data functions must not be executed
        if (node.getFunction().getFunction() instanceof MarketDataSourcingFunction) {
          markExecuted(node);
        }

        return !isExecuted(node);
      }
    });
    return dependencyGraph;
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

  //--------------------------------------------------------------------------
  public boolean isExecuted(DependencyNode node) {
    return node == null || _executedNodes.contains(node);
  }

  public void markExecuted(DependencyNode node) {
    if (node != null) {
      _executedNodes.add(node);
    }
  }

  public boolean isFailed(DependencyNode node) {
    return node == null || _failedNodes.contains(node);
  }

  public void markFailed(DependencyNode node) {
    if (node == null) {
      return;
    }
    _failedNodes.add(node);
  }
}
