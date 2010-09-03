/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.livedata.LiveDataSnapshotListener;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.calc.ViewRecalculationJob;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.engine.view.permission.ViewPermission;
import com.opengamma.engine.view.permission.ViewPermissionException;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ThreadUtil;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class ViewImpl implements View, Lifecycle, LiveDataSnapshotListener {
  private static final Logger s_logger = LoggerFactory.getLogger(View.class);
  // Injected dependencies:
  private final ViewDefinition _definition;
  private final ViewProcessingContext _processingContext;
  private final LiveDataInjector _liveDataInjector;
  // Internal State:
  private ViewEvaluationModel _viewEvaluationModel;
  private Thread _recalculationThread;
  private ViewCalculationState _calculationState = ViewCalculationState.NOT_INITIALIZED;
  private ViewRecalculationJob _recalcJob;
  private ViewComputationResultModel _latestResult;
  private final Set<ComputationResultListener> _resultListeners = new CopyOnWriteArraySet<ComputationResultListener>();
  private final Set<DeltaComputationResultListener> _deltaListeners = new CopyOnWriteArraySet<DeltaComputationResultListener>();
  private volatile boolean _populateResultModel = true;
 
  /**
   * Constructs an instance. 
   * 
   * @param definition  the view definition, not null
   * @param processingContext  the context from the view processor, not null 
   */
  public ViewImpl(ViewDefinition definition, ViewProcessingContext processingContext) {
    this(definition, processingContext, null);
  }
  
  /**
   * Constructs an instance.
   * 
   * @param definition  the view definition, not null
   * @param processingContext  the context from the view processor, not null
   * @param liveDataInjector  an optional live data injector to be used for inserting user-provided live data for this
   *                          view. For this to have any effect, the values injected should be included by the snapshot
   *                          provider that is part of the processing context.
   */
  public ViewImpl(ViewDefinition definition, ViewProcessingContext processingContext, LiveDataInjector liveDataInjector) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(processingContext, "processingContext");
    
    _definition = definition;
    _processingContext = processingContext;
    _liveDataInjector = liveDataInjector;
  }
  
  /**
   * @return the definition
   */
  @Override
  public ViewDefinition getDefinition() {
    return _definition;
  }

  /**
   * @return the processingContext
   */
  @Override
  public ViewProcessingContext getProcessingContext() {
    return _processingContext;
  }
  
  @Override
  public LiveDataInjector getLiveDataInjector() {
    return _liveDataInjector;
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
   * @return the latest view evaluation model
   */
  @Override
  public ViewEvaluationModel getViewEvaluationModel() {
    return _viewEvaluationModel;
  }
  
  public ViewPermissionProvider getPermissionProvider() {
    return getProcessingContext().getPermissionProvider();
  }
    
  @Override
  public boolean addResultListener(ComputationResultListener resultListener) {
    ArgumentChecker.notNull(resultListener, "Result listener");
    
    getPermissionProvider().assertPermission(ViewPermission.READ_RESULTS, resultListener.getUser(), this);
    return _resultListeners.add(resultListener);
  }
  
  @Override
  public boolean removeResultListener(ComputationResultListener resultListener) {
    ArgumentChecker.notNull(resultListener, "Result listener");
    return _resultListeners.remove(resultListener);
  }
  
  @Override
  public boolean addDeltaResultListener(DeltaComputationResultListener deltaListener) {
    ArgumentChecker.notNull(deltaListener, "Delta listener");
    
    getPermissionProvider().assertPermission(ViewPermission.READ_RESULTS, deltaListener.getUser(), this);
    return _deltaListeners.add(deltaListener);
  }
  
  @Override
  public boolean removeDeltaResultLister(DeltaComputationResultListener deltaListener) {
    ArgumentChecker.notNull(deltaListener, "Delta listener");
    return _deltaListeners.remove(deltaListener);
  }
  
  @Override
  public String getName() {
    return getDefinition().getName();
  }
  
  public Set<ComputationTargetSpecification> getAllComputationTargets() {
    return getViewEvaluationModel().getAllComputationTargets();
  }
  
  public synchronized void init() {
    OperationTimer timer = new OperationTimer(s_logger, "Initializing view {}", getDefinition().getName());
    setCalculationState(ViewCalculationState.INITIALIZING);

    _viewEvaluationModel = ViewDefinitionCompiler.compile(getDefinition(), getProcessingContext().asCompilationServices());
    addLiveDataSubscriptions();
    
    setCalculationState(ViewCalculationState.NOT_STARTED);
    timer.finished();
  }

  /**
   * Adds live data subscriptions to the view.
   */
  private void addLiveDataSubscriptions() {
    Set<ValueRequirement> liveDataRequirements = getRequiredLiveData();
    
    OperationTimer timer = new OperationTimer(s_logger, "Adding {} live data subscriptions for portfolio {}", liveDataRequirements.size(), getDefinition().getPortfolioId());
    
    LiveDataSnapshotProvider snapshotProvider = getProcessingContext().getLiveDataSnapshotProvider();
    snapshotProvider.addListener(this);
    snapshotProvider.addSubscription(getDefinition().getLiveDataUser(), liveDataRequirements);
    timer.finished();
  }
  
  @Override
  public void subscriptionFailed(ValueRequirement requirement, String msg) {
  }

  @Override
  public void subscriptionStopped(ValueRequirement requirement) {
  }

  @Override
  public void subscriptionSucceeded(ValueRequirement requirement) {
  }

  @Override
  public void valueChanged(ValueRequirement value) {
    ValueSpecification valueSpecification = new ValueSpecification(value, LiveDataSourcingFunction.UNIQUE_ID);
    Set<ValueSpecification> liveDataRequirements = getViewEvaluationModel().getAllLiveDataRequirements();
    ViewRecalculationJob recalcJob = getRecalcJob();
    if (recalcJob != null && liveDataRequirements.contains(valueSpecification)) {
      recalcJob.liveDataChanged();  
    }
  }

  @Override
  public synchronized ViewComputationResultModel getLatestResult() {
    return _latestResult;
  }

  @Override
  public Portfolio getPortfolio() {
    if (getViewEvaluationModel() == null) {
      return null;
    }
    return getViewEvaluationModel().getPortfolio();
  }

  public PortfolioNode getPositionRoot() {
    if (getViewEvaluationModel() == null) {
      return null;
    }
    return getViewEvaluationModel().getPortfolio().getRootNode();
  }

  @Override
  public synchronized void recalculationPerformed(ViewComputationResultModel result) {
    // REVIEW kirk 2009-09-24 -- We need to consider this method for background execution
    // of some kind. It's synchronized and blocks the recalc thread, so a slow
    // callback implementation (or just the cost of computing the delta model) will
    // be an unnecessary burden. Have to factor in some type of win there.
    s_logger.debug("Recalculation Performed called.");
    // We swap these first so that in the callback the view is consistent.
    ViewResultModel previousResult = _latestResult;
    _latestResult = result;
    for (ComputationResultListener resultListener : _resultListeners) {
      resultListener.computationResultAvailable(result);
    }
    if (!_deltaListeners.isEmpty() && (previousResult != null)) {
      ViewDeltaResultModel deltaModel = computeDeltaModel(previousResult, result);
      for (DeltaComputationResultListener deltaListener : _deltaListeners) {
        deltaListener.deltaResultAvailable(deltaModel);
      }
    }
  }
  
  /**
   * @param previousResult
   * @param result
   * @return
   */
  private ViewDeltaResultModel computeDeltaModel(ViewResultModel previousResult, ViewResultModel result) {
    ViewDeltaResultModelImpl deltaModel = new ViewDeltaResultModelImpl();
    deltaModel.setValuationTime(result.getValuationTime());
    deltaModel.setResultTimestamp(result.getResultTimestamp());
    deltaModel.setPreviousResultTimestamp(previousResult.getResultTimestamp());
    deltaModel.setCalculationConfigurationNames(result.getCalculationConfigurationNames());
    for (ComputationTargetSpecification targetSpec : result.getAllTargets()) {
      computeDeltaModel(deltaModel, targetSpec, previousResult, result);
    }
    
    return deltaModel;
  }
  
  private void computeDeltaModel(ViewDeltaResultModelImpl deltaModel, ComputationTargetSpecification targetSpec,
      ViewResultModel previousResult, ViewResultModel result) {
    for (String calcConfigName : result.getCalculationConfigurationNames()) {
      ViewCalculationResultModel resultCalcModel = result.getCalculationResult(calcConfigName);
      ViewCalculationResultModel previousCalcModel = previousResult.getCalculationResult(calcConfigName);      
      computeDeltaModel(deltaModel, targetSpec, calcConfigName, previousCalcModel, resultCalcModel);
    }
  }

  private void computeDeltaModel(ViewDeltaResultModelImpl deltaModel, ComputationTargetSpecification targetSpec,
      String calcConfigName, ViewCalculationResultModel previousCalcModel, ViewCalculationResultModel resultCalcModel) {
    if (previousCalcModel == null) {
      // Everything is new/delta because this is a new calculation context.
      Map<String, ComputedValue> resultValues = resultCalcModel.getValues(targetSpec);
      for (Map.Entry<String, ComputedValue> resultEntry : resultValues.entrySet()) {
        deltaModel.addValue(calcConfigName, resultEntry.getValue());
      }
    } else {
      Map<String, ComputedValue> resultValues = resultCalcModel.getValues(targetSpec);
      Map<String, ComputedValue> previousValues = previousCalcModel.getValues(targetSpec);
      
      if (previousValues == null) {
        // Everything is new/delta because this is a new target.
        for (Map.Entry<String, ComputedValue> resultEntry : resultValues.entrySet()) {
          deltaModel.addValue(calcConfigName, resultEntry.getValue());
        }
      } else {
        // Have to individual delta.
        DeltaDefinition deltaDefinition = getDefinition().getCalculationConfiguration(calcConfigName).getDeltaDefinition();
        for (Map.Entry<String, ComputedValue> resultEntry : resultValues.entrySet()) {
          ComputedValue resultValue = resultEntry.getValue();
          ComputedValue previousValue = previousValues.get(resultEntry.getKey());
          // REVIEW jonathan 2010-05-07 -- The previous value that we're comparing with is the value from the last
          // computation cycle, not the value that we last emitted as a delta. It is therefore important that the
          // DeltaComparers take this into account in their implementation of isDelta. E.g. they should compare the
          // values after truncation to the required decimal place, rather than testing whether the difference of the
          // full values is greater than some threshold; this way, there will always be a point beyond which a change
          // is detected, even in the event of gradual creep.
          if (deltaDefinition.isDelta(previousValue, resultValue)) {
            deltaModel.addValue(calcConfigName, resultEntry.getValue());
          }
        }
      }
    }
  }

  // REVIEW kirk 2009-09-11 -- Need to resolve the synchronization on the lifecycle
  // methods.

  @Override
  public synchronized boolean isRunning() {
    return getCalculationState() == ViewCalculationState.RUNNING;
  }
  
  public boolean hasListeners() {
    return !_resultListeners.isEmpty() || !_deltaListeners.isEmpty();
  }
  
  public boolean isPopulateResultModel() {
    return _populateResultModel;
  }

  public void setPopulateResultModel(boolean populateResultModel) {
    _populateResultModel = populateResultModel;
  }

  @Override
  public synchronized void runOneCycle() {
    long snapshotTime = getProcessingContext().getLiveDataSnapshotProvider().snapshot();
    runOneCycle(snapshotTime);
  }
  
  @Override
  public synchronized void runOneCycle(long valuationTime) {
    SingleComputationCycle cycle = createCycle(valuationTime);
    cycle.prepareInputs();
    cycle.executePlans();
    
    if (isPopulateResultModel()) {
      cycle.populateResultModel();
      recalculationPerformed(cycle.getResultModel());
    }
    
    cycle.releaseResources();
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
    synchronized (this) {
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
    
    synchronized (this) {
      if ((getCalculationState() == ViewCalculationState.TERMINATED)
          || (getCalculationState() == ViewCalculationState.TERMINATING)) {
        s_logger.info("Multiple requests to stop() made, this invocation will do nothing.");
        return;
      }
      setCalculationState(ViewCalculationState.TERMINATING);
    }
    
    getRecalcJob().terminate();
    if (getRecalculationThread().getState() == Thread.State.TIMED_WAITING) {
      // In this case it might be waiting on a recalculation pass. Interrupt it.
      getRecalculationThread().interrupt();
    }
    
    // TODO kirk 2009-09-11 -- Have a heuristic on when to set the timeout based on
    // how long the job is currently taking to cycle.
    long timeout = 100 * 1000L;
    boolean successful = ThreadUtil.safeJoin(getRecalculationThread(), timeout);
    if (!successful) {
      s_logger.warn("Unable to shut down recalc thread in {}ms", timeout);
    }
    
    synchronized (this) {
      setCalculationState(ViewCalculationState.TERMINATED);
      
      setRecalcJob(null);
      setRecalculationThread(null);
    }
    s_logger.info("Stopped.");
  }
  
  /**
   * Checks that the given user has access to every market data line required to compute the results of the view, and
   * throws an exception if this is not the case.
   * 
   * @param user  the user
   * @throws ViewPermissionException  if any entitlement problems are found
   */
  @Override
  public void assertAccessToLiveDataRequirements(UserPrincipal user) {
    s_logger.info("Checking that {} is entitled to the results of {}", user, this);

    Collection<LiveDataSpecification> requiredLiveData = getRequiredLiveDataSpecifications();
    
    Map<LiveDataSpecification, Boolean> entitlements = getProcessingContext().getLiveDataEntitlementChecker().isEntitled(user, requiredLiveData);
    
    ArrayList<LiveDataSpecification> failures = new ArrayList<LiveDataSpecification>();
    for (Map.Entry<LiveDataSpecification, Boolean> entry : entitlements.entrySet()) {
      if (entry.getValue().booleanValue() == false) {
        failures.add(entry.getKey());
      }
    }
    
    if (!failures.isEmpty()) {
      throw new ViewPermissionException(user + " is not entitled to the output of " + this + 
          " because they do not have permissions to " + failures.get(0));
    }
  }
  
  @Override
  public Set<ValueRequirement> getRequiredLiveData() {
    ViewEvaluationModel viewEvaluationModel = getViewEvaluationModel();
    if (viewEvaluationModel == null) {
      return null;
    }
    Set<ValueSpecification> requiredSpecs = viewEvaluationModel.getAllLiveDataRequirements();
    
    Set<ValueRequirement> returnValue = new HashSet<ValueRequirement>();
    for (ValueSpecification requiredSpec : requiredSpecs) {
      returnValue.add(requiredSpec.getRequirementSpecification());      
    }
    return returnValue;
  }
  
  private Collection<LiveDataSpecification> getRequiredLiveDataSpecifications() {
    Set<LiveDataSpecification> returnValue = new HashSet<LiveDataSpecification>();
    for (ValueRequirement requirement : getRequiredLiveData()) {
      LiveDataSpecification liveDataSpec = requirement.getRequiredLiveData(getProcessingContext().getSecuritySource());
      returnValue.add(liveDataSpec);      
    }
    return returnValue;
  }

  @Override
  public String toString() {
    return "View[" + getDefinition().getName() + "]";
  }
  
  @Override
  public SingleComputationCycle createCycle(long valuationTime) {
    SingleComputationCycle cycle = new SingleComputationCycle(this, valuationTime);
    return cycle;
  }

}
