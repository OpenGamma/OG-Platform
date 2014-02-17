/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Listens to the engine for results for a set of scenarios and combines them into a single object.
 */
public class ScenarioListener extends AbstractViewResultListener {

  private static final Logger s_logger = LoggerFactory.getLogger(ScenarioListener.class);

  /**
   * Single element blocking queue to hold the complete set of results. This allows the {@link #getResults()} method
   * to block until the results are available.
   */
  private final BlockingQueue<List<SimpleResultModel>> _resultsQueue = new ArrayBlockingQueue<>(1);

  /** Tables of results, one for each scenario, keyed by scenario name. */
  private final Map<String, SimpleResultModel> _results = Maps.newHashMap();

  /** The scenario names in the order they were defined. */
  private final List<String> _scenarioNames;

  private SimpleResultBuilder _resultsBuilder;

  public ScenarioListener(List<String> scenarioNames) {
    _scenarioNames = ArgumentChecker.notEmpty(scenarioNames, "scenarioNames");
  }

  /**
   * @return {@link UserPrincipal#getLocalUser()}
   */
  @Override
  public UserPrincipal getUser() {
    return UserPrincipal.getLocalUser();
  }

  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    _resultsBuilder = new SimpleResultBuilder(compiledViewDefinition);
  }

  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
    s_logger.warn("View compilation failed", exception);
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    String scenarioName = fullResult.getViewCycleExecutionOptions().getName();
    s_logger.info("cycle completed for scenario {}", scenarioName);
    SimpleResultModel resultModel = _resultsBuilder.build(fullResult);
    _results.put(resultModel.getCycleName(), resultModel);
  }

  @Override
  public void processCompleted() {
    s_logger.info("process completed");
    List<SimpleResultModel> resultModels = Lists.newArrayListWithCapacity(_results.size());
    for (String scenarioName : _scenarioNames) {
      SimpleResultModel resultModel = _results.get(scenarioName);
      if (resultModel == null) {
        s_logger.warn("No results found for scenario '{}'", scenarioName);
      } else {
        resultModels.add(resultModel);
      }
    }
    try {
      _resultsQueue.put(resultModels);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Unexpected exception", e);
    }
  }

  /**
   * @return The results for all scenarios, blocks until they are available.
   */
  /* package */ List<SimpleResultModel> getResults() {
    try {
      return _resultsQueue.take();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Unexpected exception", e);
    }
  }
}
