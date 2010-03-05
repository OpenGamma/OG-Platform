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
 *
 * @author kirk
 */
public class ViewRecalculationJob extends TerminatableJob {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewRecalculationJob.class);
  private final View _view;
  private ViewComputationResultModelImpl _previousResult;
  
  public ViewRecalculationJob(View view) {
    if(view == null) {
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
    PortfolioEvaluationModel portfolioEvaluationModel = getView().getPortfolioEvaluationModel();
    ViewComputationResultModelImpl result = new ViewComputationResultModelImpl();
    result.setPortfolio(portfolioEvaluationModel.getPortfolio(), portfolioEvaluationModel.getPopulatedRootNode());
    
    SingleComputationCycle cycle = new SingleComputationCycle(
        getView().getDefinition().getName(),
        getView().getProcessingContext(),
        portfolioEvaluationModel,
        result, getView().getDefinition());
    if(!cycle.prepareInputs()) {
      s_logger.info("Not executing as couldn't snapshot market data. Probably waiting for source data to finish populating.");
      try {
        Thread.sleep(100l);
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
    } else {
      cycle.executePlans();
      cycle.populateResultModel();
      cycle.releaseResources();
    }
    
    long endTime = System.currentTimeMillis();
    result.setResultTimestamp(endTime);
    long delta = endTime - cycle.getStartTime();
    s_logger.info("Completed one recalculation pass in {}ms", delta);
    getView().recalculationPerformed(result);
  }
}
