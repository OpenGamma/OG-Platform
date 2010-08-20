/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.DefaultViewComputationCache;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.cache.WriteBehindViewComputationCache;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.Pair;

/**
 * A calculation node implementation. The node can only be used by one thread - i.e. executeJob cannot be called concurrently to do
 * multiple jobs. To execute multiple jobs concurrently separate calculation nodes must be used.
 */
public abstract class AbstractCalculationNode implements CalculationNode {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractCalculationNode.class);
  private final ViewComputationCacheSource _cacheSource;
  private FunctionRepository _functionRepository;
  private final FunctionExecutionContext _functionExecutionContext;
  private final ComputationTargetResolver _targetResolver;
  private final ViewProcessorQuerySender _viewProcessorQuerySender;
  private final String _nodeId;
  private final ExecutorService _writeBehindExecutorService;

  private long _resolutionTime;
  private long _cacheGetTime;
  private long _invocationTime;
  private long _cachePutTime;

  protected AbstractCalculationNode(ViewComputationCacheSource cacheSource, FunctionExecutionContext functionExecutionContext, ComputationTargetResolver targetResolver,
      ViewProcessorQuerySender calcNodeQuerySender, String nodeId) {
    ArgumentChecker.notNull(cacheSource, "Cache Source");
    ArgumentChecker.notNull(functionExecutionContext, "Function Execution Context");
    ArgumentChecker.notNull(targetResolver, "Target Resolver");
    ArgumentChecker.notNull(calcNodeQuerySender, "Calc Node Query Sender");
    ArgumentChecker.notNull(nodeId, "Calculation node ID");

    _cacheSource = cacheSource;
    // Take a copy of the execution context as we will modify it during execution which isn't good if there are other CalcNodes in our JVM
    _functionExecutionContext = functionExecutionContext.clone();
    _targetResolver = targetResolver;
    _viewProcessorQuerySender = calcNodeQuerySender;
    _nodeId = nodeId;
    // TODO [ENG-183] pass the ExecutorService in as a parameter - it's used for the write-behind threads
    _writeBehindExecutorService = Executors.newCachedThreadPool();
  }

  public ViewComputationCacheSource getCacheSource() {
    return _cacheSource;
  }

  public void setFunctionRepository(final FunctionRepository functionRepository) {
    _functionRepository = functionRepository;
  }

  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }

  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }

  protected ViewProcessorQuerySender getViewProcessorQuerySender() {
    return _viewProcessorQuerySender;
  }

  protected ExecutorService getWriteBehindExecutorService() {
    return _writeBehindExecutorService;
  }

  @Override
  public String getNodeId() {
    return _nodeId;
  }

  public CalculationJobResult executeJob(CalculationJob job) {
    s_logger.info("Executing {} on {}", job, _nodeId);

    CalculationJobSpecification spec = job.getSpecification();

    getFunctionExecutionContext().setViewProcessorQuery(new ViewProcessorQuery(getViewProcessorQuerySender(), spec));
    getFunctionExecutionContext().setSnapshotEpochTime(spec.getIterationTimestamp());
    getFunctionExecutionContext().setSnapshotClock(DateUtil.epochFixedClockUTC(spec.getIterationTimestamp()));

    WriteBehindViewComputationCache cache = new WriteBehindViewComputationCache(getCache(spec), getWriteBehindExecutorService());

    long startNanos = System.nanoTime();

    List<CalculationJobResultItem> resultItems = new ArrayList<CalculationJobResultItem>();

    // Pre-fetch the identifiers to speed things up
    cacheAllValueSpecifications(cache, job.getJobItems());

    for (CalculationJobItem jobItem : job.getJobItems()) {

      CalculationJobResultItem resultItem;
      try {
        Set<ComputedValue> result = invoke(jobItem, cache);
        _cachePutTime -= System.nanoTime();
        cache.putValues(result);
        _cachePutTime += System.nanoTime();
        resultItem = new CalculationJobResultItem(jobItem);
      } catch (MissingInputException e) {
        // NOTE kirk 2009-10-20 -- We intentionally only do the message here so that we don't
        // litter the logs with stack traces.
        s_logger.info("Unable to invoke {} due to missing inputs: {}", jobItem, e.getMessage());
        resultItem = new CalculationJobResultItem(jobItem, e);
      } catch (Exception e) {
        s_logger.warn("Invoking " + jobItem.getFunctionUniqueIdentifier() + " threw exception.", e);
        resultItem = new CalculationJobResultItem(jobItem, e);
      }

      resultItems.add(resultItem);
      /*
       * ((DefaultViewComputationCache) cache.getUnderlying()).reportTimes();
       * System.err.println("resolution=" + (_resolutionTime / 1000000d) + "ms, cacheGet=" + (_cacheGetTime / 1000000d) + "ms, invoke=" + (_invocationTime / 1000000d) + "ms, cachePut="
       * + (_cachePutTime / 1000000d) + "ms");
       */
    }

    // TODO [ENG-183] the cast below is very nasty
    _cachePutTime -= System.nanoTime();
    ((WriteBehindViewComputationCache) cache).waitForPendingWrites();
    _cachePutTime += System.nanoTime();

    long endNanos = System.nanoTime();
    long durationNanos = endNanos - startNanos;
    CalculationJobResult jobResult = new CalculationJobResult(spec, durationNanos, resultItems, getNodeId());

    s_logger.info("Executed {}", job);
    /*
     * ((DefaultViewComputationCache) cache.getUnderlying()).reportTimes();
     * final double totalTime = (double) (_resolutionTime + _cacheGetTime + _invocationTime + _cachePutTime) / 100d;
     * if (totalTime > 0) {
     * System.err.println("Total = " + durationNanos + "ns - " + ((double) _resolutionTime / totalTime) + "% resolution, " + ((double) _cacheGetTime / totalTime) + "% cacheGet, "
     * + ((double) _invocationTime / totalTime) + "% invoke, " + ((double) _cachePutTime / totalTime) + "% cachePut");
     * }
     */
    ((DefaultViewComputationCache) cache.getUnderlying()).resetTimes();
    _resolutionTime = 0;
    _cacheGetTime = 0;
    _invocationTime = 0;
    _cachePutTime = 0;

    return jobResult;
  }

  // TODO Remove the time code that I've been using for debug and tuning

  private void cacheAllValueSpecifications(final ViewComputationCache cache, final Collection<CalculationJobItem> jobItems) {
    final Set<ValueSpecification> allValueSpecs = new HashSet<ValueSpecification>();
    for (CalculationJobItem jobItem : jobItems) {
      allValueSpecs.addAll(jobItem.getInputs());
    }
    s_logger.debug("Pre-fetching {} ValueIdentifiers", allValueSpecs.size());
    cache.cacheValueSpecifications(allValueSpecs);
  }

  @Override
  public ViewComputationCache getCache(CalculationJobSpecification spec) {
    ViewComputationCache cache = getCacheSource().getCache(spec.getViewName(), spec.getCalcConfigName(), spec.getIterationTimestamp());
    return cache;
  }

  private Set<ComputedValue> invoke(CalculationJobItem jobItem, ViewComputationCache cache) {

    String functionUniqueId = jobItem.getFunctionUniqueIdentifier();

    ComputationTarget target;
    _resolutionTime -= System.nanoTime();
    try {
      target = getTargetResolver().resolve(jobItem.getComputationTargetSpecification());
    } finally {
      _resolutionTime += System.nanoTime();
    }
    if (target == null) {
      throw new OpenGammaRuntimeException("Unable to resolve specification " + jobItem.getComputationTargetSpecification());
    }

    s_logger.debug("Invoking {} on target {}", functionUniqueId, target);

    FunctionInvoker invoker = getFunctionRepository().getInvoker(functionUniqueId);
    if (invoker == null) {
      throw new NullPointerException("Unable to locate " + functionUniqueId + " in function repository.");
    }

    // assemble inputs
    Collection<ComputedValue> inputs = new HashSet<ComputedValue>();
    Collection<ValueSpecification> missingInputs = new HashSet<ValueSpecification>();
    _cacheGetTime -= System.nanoTime();
    try {
      for (Pair<ValueSpecification, Object> input : cache.getValues(jobItem.getInputs())) {
        if ((input.getValue() == null) || (input.getValue() instanceof MissingInput)) {
          missingInputs.add(input.getKey());
        } else {
          inputs.add(new ComputedValue(input.getKey(), input.getValue()));
        }
      }
    } finally {
      _cacheGetTime += System.nanoTime();
    }

    if (!missingInputs.isEmpty()) {
      s_logger.info("Not able to execute as missing inputs {}", missingInputs);
      throw new MissingInputException(missingInputs, functionUniqueId);
    }

    FunctionInputs functionInputs = new FunctionInputsImpl(inputs);

    _invocationTime -= System.nanoTime();
    Set<ComputedValue> results;
    try {
      results = invoker.execute(getFunctionExecutionContext(), functionInputs, target, jobItem.getDesiredValues());
    } finally {
      _invocationTime += System.nanoTime();
    }
    if (results == null) {
      throw new NullPointerException("No results returned by invoker " + invoker);
    }
    return results;
  }

}
