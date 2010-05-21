/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.TerminatableJob;

/**
 * The primary job which will recalculate a {@link View}'s output data.
 */
public class ViewRecalculationJob extends TerminatableJob {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewRecalculationJob.class);
  private final View _view;
  private ViewComputationResultModelImpl _previousResult;
  private double _numExecutions;
  private double _totalTime;
  
  public ViewRecalculationJob(View view) {
    if (view == null) {
      throw new NullPointerException("Must provide a backing view.");
    }
    _view = view;
  }

  /**
   * @return the previousResult
   */
  public ViewComputationResultModelImpl getPreviousResult() {
    return _previousResult;
  }

  /**
   * @param previousResult the previousResult to set
   */
  public void setPreviousResult(ViewComputationResultModelImpl previousResult) {
    _previousResult = previousResult;
  }

  /**
   * @return the view
   */
  public View getView() {
    return _view;
  }
  
  @Override
  protected void runOneCycle() {
    long snapshotTime = getView().getProcessingContext().getLiveDataSnapshotProvider().snapshot();
    runOneCycle(snapshotTime);
  }

  protected void runOneCycle(long snapshotTime) {
    SingleComputationCycle cycle = createCycle(snapshotTime);
    runCycle(cycle);
  }
  
  private SingleComputationCycle createCycle(long snapshotTime) {
    PortfolioEvaluationModel portfolioEvaluationModel = getView().getPortfolioEvaluationModel();
    ViewComputationResultModelImpl result = new ViewComputationResultModelImpl();
    // REVIEW kirk 2010-03-29 -- Order here is important. This is lame and should be refactored into
    // the constructor.
    result.setCalculationConfigurationNames(portfolioEvaluationModel.getAllCalculationConfigurationNames());
    result.setPortfolio(portfolioEvaluationModel.getPortfolio());
    
    SingleComputationCycle cycle = new SingleComputationCycle(
        getView().getDefinition().getName(),
        getView().getProcessingContext(),
        portfolioEvaluationModel,
        result, 
        getView().getDefinition(),
        snapshotTime);
    return cycle;
  }
  
  private void runCycle(SingleComputationCycle cycle) {
    cycle.prepareInputs();
    cycle.executePlans();
    cycle.populateResultModel();
    cycle.releaseResources();
    
    long endTime = System.currentTimeMillis();
    cycle.getResultModel().setResultTimestamp(endTime);
    long delta = endTime - cycle.getStartTime();
    _totalTime += delta;
    _numExecutions += 1.0;
    s_logger.info("Last latency was {}, Average latency is {}ms", delta, (_totalTime / _numExecutions));
    getView().recalculationPerformed(cycle.getResultModel());
    // Do this intentionally AFTER alerting the view. Because of the listener system,
    // we have to recompute the delta, because we have to factor in the dispatch time
    // in recalculationPerformed().
    endTime = System.currentTimeMillis();
    delta = endTime - cycle.getStartTime();
    delayOnMinimumRecalculationPeriod(delta);
  }
  
  /**
   * Enforce the delay imposed by the minimum recalculation period for a view definition.
   * This is primarily a method so that it's clear in stack traces what's going on.
   * 
   * @param cycleComputationTime The time it took to actually calculate the view iteration 
   */
  protected void delayOnMinimumRecalculationPeriod(long cycleComputationTime) {
    if (getView().getDefinition().getMinimumRecalculationPeriod() == null) {
      return;
    }
    long minimumRecalculationPeriod = getView().getDefinition().getMinimumRecalculationPeriod();
    if (cycleComputationTime < minimumRecalculationPeriod) {
      long timeToWait = minimumRecalculationPeriod - cycleComputationTime;
      s_logger.debug("Waiting for {}ms as computed faster than minimum recalculation period", timeToWait);
      try {
        Thread.sleep(timeToWait);
      } catch (InterruptedException e) {
        Thread.interrupted();
        s_logger.info("Interrupted while delaying due to minimum recalculation period. Continuing operation.");
      }
    }
  }
}
