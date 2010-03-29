/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ThreadUtil;

/**
 * The base implementation of the {@link View} interface.
 *
 * @author kirk
 */
public class View implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(View.class);
  // Injected dependencies:
  private final ViewDefinition _definition;
  private final ViewProcessingContext _processingContext;
  // Internal State:
  private PortfolioEvaluationModel _portfolioEvaluationModel;
  private Thread _recalculationThread;
  private ViewCalculationState _calculationState = ViewCalculationState.NOT_INITIALIZED;
  private ViewRecalculationJob _recalcJob;
  private ViewComputationResultModelImpl _mostRecentResult;
  private final Set<ComputationResultListener> _resultListeners = new HashSet<ComputationResultListener>();
  private final Set<DeltaComputationResultListener> _deltaListeners = new HashSet<DeltaComputationResultListener>();

  public View(ViewDefinition definition, ViewProcessingContext processingContext) {
    if(definition == null) {
      throw new NullPointerException("Must provide a definition.");
    }
    if(processingContext == null) {
      throw new NullPointerException("Must provide a processing context.");
    }
    _definition = definition;
    _processingContext = processingContext;
  }
  
  /**
   * @return the definition
   */
  public ViewDefinition getDefinition() {
    return _definition;
  }

  /**
   * @return the processingContext
   */
  public ViewProcessingContext getProcessingContext() {
    return _processingContext;
  }

  /**
   * @return the recalculationThread
   */
  public Thread getRecalculationThread() {
    return _recalculationThread;
  }

  /**
   * @param recalculationThread the recalculationThread to set
   */
  protected void setRecalculationThread(Thread recalculationThread) {
    _recalculationThread = recalculationThread;
  }

  /**
   * @return the calculationState
   */
  public ViewCalculationState getCalculationState() {
    return _calculationState;
  }

  /**
   * @param calculationState the calculationState to set
   */
  protected void setCalculationState(ViewCalculationState calculationState) {
    _calculationState = calculationState;
  }

  /**
   * @return the recalcJob
   */
  public ViewRecalculationJob getRecalcJob() {
    return _recalcJob;
  }

  /**
   * @param recalcJob the recalcJob to set
   */
  protected void setRecalcJob(ViewRecalculationJob recalcJob) {
    _recalcJob = recalcJob;
  }

  /**
   * @return the portfolioEvaluationModel
   */
  public PortfolioEvaluationModel getPortfolioEvaluationModel() {
    return _portfolioEvaluationModel;
  }

  /**
   * @param portfolioEvaluationModel the portfolioEvaluationModel to set
   */
  public void setPortfolioEvaluationModel(
      PortfolioEvaluationModel portfolioEvaluationModel) {
    _portfolioEvaluationModel = portfolioEvaluationModel;
  }
  
  public void addResultListener(ComputationResultListener resultListener) {
    _resultListeners.add(resultListener);
  }
  
  public void removeResultListener(ComputationResultListener resultListener) {
    _resultListeners.remove(resultListener);
  }
  
  public void addDeltaResultListener(DeltaComputationResultListener deltaListener) {
    _deltaListeners.add(deltaListener);
  }
  
  public void removeDeltaResultLister(DeltaComputationResultListener deltaListener) {
    _deltaListeners.remove(deltaListener);
  }

  public synchronized void init() {
    s_logger.info("Initializing view {}", getDefinition().getName());
    checkInjectedDependencies();
    setCalculationState(ViewCalculationState.INITIALIZING);

    reloadPortfolio();
    
    setCalculationState(ViewCalculationState.NOT_STARTED);
  }
  
  public void reloadPortfolio() {
    s_logger.info("Reloading portfolio named {}", getDefinition().getRootPortfolioName());
    Portfolio portfolio = getProcessingContext().getPositionMaster().getRootPortfolio(getDefinition().getRootPortfolioName());
    if(portfolio == null) {
      throw new OpenGammaRuntimeException("Unable to resolve portfolio named " + getDefinition().getRootPortfolioName());
    }
    PortfolioEvaluationModel portfolioEvaluationModel = new PortfolioEvaluationModel(portfolio);
    portfolioEvaluationModel.init(
        getProcessingContext(),
        getDefinition());
    setPortfolioEvaluationModel(portfolioEvaluationModel);
  }
  
  /**
   * 
   */
  private void checkInjectedDependencies() {
  }

  public synchronized ViewComputationResultModel getMostRecentResult() {
    return _mostRecentResult;
  }

  public PortfolioNode getPositionRoot() {
    if(getPortfolioEvaluationModel() == null) {
      return null;
    }
    return getPortfolioEvaluationModel().getPortfolio();
  }
  
  public synchronized void recalculationPerformed(ViewComputationResultModelImpl result) {
    // REVIEW kirk 2009-09-24 -- We need to consider this method for background execution
    // of some kind. It's synchronized and blocks the recalc thread, so a slow
    // callback implementation (or just the cost of computing the delta model) will
    // be an unnecessary burden. Have to factor in some type of win there.
    s_logger.info("Recalculation Performed called.");
    // We swap these first so that in the callback the view is consistent.
    ViewComputationResultModelImpl previousResult = _mostRecentResult;
    _mostRecentResult = result;
    for(ComputationResultListener resultListener : _resultListeners) {
      resultListener.computationResultAvailable(result);
    }
    if(!_deltaListeners.isEmpty() && (previousResult != null)) {
      ViewDeltaResultModel deltaModel = computeDeltaModel(previousResult, result);
      for(DeltaComputationResultListener deltaListener : _deltaListeners) {
        deltaListener.deltaResultAvailable(deltaModel);
      }
    }
  }
  
  /**
   * @param previousResult
   * @param result
   * @return
   */
  private ViewDeltaResultModel computeDeltaModel(
      ViewComputationResultModelImpl previousResult,
      ViewComputationResultModelImpl result) {
    ViewDeltaResultModelImpl deltaModel = new ViewDeltaResultModelImpl();
    deltaModel.setInputDataTimestamp(result.getInputDataTimestamp());
    deltaModel.setResultTimestamp(result.getResultTimestamp());
    deltaModel.setPreviousResultTimestamp(previousResult.getResultTimestamp());

    // TODO kirk 2010-03-29 -- Fix this and re-implement as part of ENG-25.
    /*
    for(ComputationTargetSpecification targetSpec : result.getAllTargets()) {
      deltaModel.addTarget(targetSpec);
      
      Map<String, ComputedValue> resultValues = result.getValues(targetSpec);
      Map<String, ComputedValue> previousValues = previousResult.getValues(targetSpec);
      Map<ValueSpecification, ComputedValue> previousValueMap = new HashMap<ValueSpecification, ComputedValue>();
      for(ComputedValue previousValue : previousValues.values()) {
        previousValueMap.put(previousValue.getSpecification(), previousValue);
      }
      for(ComputedValue resultValue : resultValues.values()) {
        ComputedValue previousValue = previousValueMap.get(resultValue.getSpecification());
        if(previousValue == null) {
          deltaModel.addValue(resultValue);
        } else if(!ObjectUtils.equals(previousValue.getValue(), resultValue.getValue())) {
          deltaModel.addValue(resultValue);
        }
      }
    }
    */
    return deltaModel;
  }

  // REVIEW kirk 2009-09-11 -- Need to resolve the synchronization on the lifecycle
  // methods.

  @Override
  public synchronized boolean isRunning() {
    return getCalculationState() == ViewCalculationState.RUNNING;
  }
  
  public synchronized void runOneCycle() {
    ViewRecalculationJob recalcJob = new ViewRecalculationJob(this);
    recalcJob.runOneCycle();
  }

  @Override
  public synchronized void start() {
    s_logger.info("Starting...");
    switch(getCalculationState()) {
    case NOT_STARTED:
    case TERMINATED:
      // Normal state of play. Continue as normal.
      break;
    case TERMINATING:
      // In the middle of termination. This is really bad, as we're now holding the lock
      // that will allow termination to complete successfully. Therefore, we have to throw
      // an exception rather than just waiting or something.
      throw new IllegalStateException("Instructed to start while still terminating.");
    case INITIALIZING:
      // Must have thrown an exception in initialization. Can't start.
      throw new IllegalStateException("Initialization didn't completely successfully. Can't start.");
    case NOT_INITIALIZED:
      throw new IllegalStateException("Must call init() before starting.");
    case STARTING:
      // Must have thrown an exception when start() called previously.
      throw new IllegalStateException("start() already called, but failed to start. Cannot start again.");
    case RUNNING:
      throw new IllegalStateException("Already running.");
    }
    
    setCalculationState(ViewCalculationState.STARTING);
    ViewRecalculationJob recalcJob = new ViewRecalculationJob(this);
    Thread recalcThread = new Thread(recalcJob, "Recalc Thread for " + getDefinition().getName());
    
    setRecalcJob(recalcJob);
    setRecalculationThread(recalcThread);
    setCalculationState(ViewCalculationState.RUNNING);
    recalcThread.start();
    s_logger.info("Started.");
  }

  @Override
  public void stop() {
    s_logger.info("Stopping.....");
    synchronized(this) {
      switch(getCalculationState()) {
      case STARTING:
        // Something went horribly wrong during start, and it must have thrown an exception.
        s_logger.warn("Instructed to stop the ViewImpl, but still starting. Starting must have failed. Doing nothing.");
        break;
      case RUNNING:
        // This is the normal state of play. Do nothing.
        break;
      default:
        throw new IllegalStateException("Cannot stop a ViewImpl that isn't running. State: " + getCalculationState());
      }
    }
    
    assert getRecalcJob() != null;
    assert getRecalculationThread() != null;
    
    synchronized(this) {
      if((getCalculationState() == ViewCalculationState.TERMINATED)
          || (getCalculationState() == ViewCalculationState.TERMINATING)) {
        s_logger.info("Multiple requests to stop() made, this invocation will do nothing.");
        return;
      }
      setCalculationState(ViewCalculationState.TERMINATING);
    }
    
    getRecalcJob().terminate();
    // TODO kirk 2009-09-11 -- Have a heuristic on when to set the timeout based on
    // how long the job is currently taking to cycle.
    long timeout = 100 * 1000l;
    boolean successful = ThreadUtil.safeJoin(getRecalculationThread(), timeout);
    if(!successful) {
      s_logger.warn("Unable to shut down recalc thread in {}ms", timeout);
    }
    
    synchronized(this) {
      setCalculationState(ViewCalculationState.TERMINATED);
      
      setRecalcJob(null);
      setRecalculationThread(null);
    }
    s_logger.info("Stopped.");
  }

}
