/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.LiveDataAvailabilityProvider;
import com.opengamma.engine.LiveDataSnapshotProvider;
import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.util.ThreadUtil;

/**
 * The base implementation of the {@link View} interface.
 *
 * @author kirk
 */
public class ViewImpl implements View, Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewImpl.class);
  // Injected dependencies:
  private final ViewDefinition _definition;
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private LiveDataSnapshotProvider _liveDataSnapshotProvider;
  private AnalyticFunctionRepository _analyticFunctionRepository;
  private PositionMaster _positionMaster;
  private SecurityMaster _securityMaster;
  private ViewComputationCacheFactory _computationCacheFactory;
  // Internal State:
  private PortfolioNode _positionRoot;
  private FullyPopulatedPortfolioNode _populatedPositionRoot;
  private Thread _recalculationThread;
  private ViewCalculationState _calculationState = ViewCalculationState.NOT_INITIALIZED;
  private ViewRecalculationJob _recalcJob; 
  
  public ViewImpl(ViewDefinition definition) {
    if(definition == null) {
      throw new NullPointerException("Must provide a definition.");
    }
    _definition = definition;
  }
  
  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @param liveDataAvailabilityProvider the liveDataAvailabilityProvider to set
   */
  @Required
  public void setLiveDataAvailabilityProvider(
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
  }

  /**
   * @return the liveDataSnapshotProvider
   */
  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return _liveDataSnapshotProvider;
  }

  /**
   * @param liveDataSnapshotProvider the liveDataSnapshotProvider to set
   */
  @Required
  public void setLiveDataSnapshotProvider(
      LiveDataSnapshotProvider liveDataSnapshotProvider) {
    _liveDataSnapshotProvider = liveDataSnapshotProvider;
  }

  /**
   * @return the analyticFunctionRepository
   */
  public AnalyticFunctionRepository getAnalyticFunctionRepository() {
    return _analyticFunctionRepository;
  }

  /**
   * @param analyticFunctionRepository the analyticFunctionRepository to set
   */
  @Required
  public void setAnalyticFunctionRepository(
      AnalyticFunctionRepository analyticFunctionRepository) {
    _analyticFunctionRepository = analyticFunctionRepository;
  }

  /**
   * @return the positionMaster
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * @param positionMaster the positionMaster to set
   */
  @Required
  public void setPositionMaster(PositionMaster positionMaster) {
    _positionMaster = positionMaster;
  }

  /**
   * @return the definition
   */
  public ViewDefinition getDefinition() {
    return _definition;
  }

  /**
   * @return the computationCacheFactory
   */
  public ViewComputationCacheFactory getComputationCacheFactory() {
    return _computationCacheFactory;
  }

  /**
   * @param computationCacheFactory the computationCacheFactory to set
   */
  @Required
  public void setComputationCacheFactory(
      ViewComputationCacheFactory computationCacheFactory) {
    _computationCacheFactory = computationCacheFactory;
  }

  /**
   * @return the securityMaster
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * @param securityMaster the securityMaster to set
   */
  public void setSecurityMaster(SecurityMaster securityMaster) {
    _securityMaster = securityMaster;
  }

  /**
   * @param positionRoot the positionRoot to set
   */
  public void setPositionRoot(PortfolioNode positionRoot) {
    _positionRoot = positionRoot;
  }

  /**
   * @param populatedPositionRoot the populatedPositionRoot to set
   */
  public void setPopulatedPositionRoot(
      FullyPopulatedPortfolioNode populatedPositionRoot) {
    _populatedPositionRoot = populatedPositionRoot;
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

  // TODO kirk 2009-09-03 -- Flesh out a bootstrap system:
  // - Load up the contents of the portfolio from a PortfolioMaster
  // - Load up the securities of the portfolio from a SecurityMaster
  // - Gather up all problems if anything isn't available
  
  public void init() {
    checkInjectedDependencies();
    setCalculationState(ViewCalculationState.INITIALIZING);

    PortfolioNode positionRoot = getPositionMaster().getRootPortfolio(getDefinition().getName());
    if(positionRoot == null) {
      throw new OpenGammaRuntimeException("Unable to resolve portfolio named " + getDefinition().getName());
    }
    setPositionRoot(positionRoot);
    
    FullyPopulatedPortfolioNode populatedPositionRoot = getPopulatedPortfolioNode(positionRoot);
    assert populatedPositionRoot != null;
    setPopulatedPositionRoot(populatedPositionRoot);
    
    setCalculationState(ViewCalculationState.NOT_STARTED);
  }
  
  /**
   * @param node
   * @return
   */
  protected FullyPopulatedPortfolioNode getPopulatedPortfolioNode(
      PortfolioNode node) {
    if(node == null) {
      return null;
    }
    FullyPopulatedPortfolioNode populatedNode = new FullyPopulatedPortfolioNode();
    for(Position position : node.getPositions()) {
      Security security = getSecurityMaster().getSecurity(position.getSecurityKey());
      if(security == null) {
        throw new OpenGammaRuntimeException("Unable to resolve security key " + position.getSecurityKey() + " for position " + position);
      }
      populatedNode.addPosition(position, security);
    }
    for(PortfolioNode subNode : node.getSubNodes()) {
      populatedNode.addSubNode(getPopulatedPortfolioNode(subNode));
    }
    return populatedNode;
  }

  /**
   * 
   */
  private void checkInjectedDependencies() {
    if(getAnalyticFunctionRepository() == null) {
      throw new IllegalStateException("Must have an Analytic Function Repository");
    }
    if(getLiveDataAvailabilityProvider() == null) {
      throw new IllegalStateException("Must have a Live Data Availability Provider");
    }
    if(getLiveDataSnapshotProvider() == null) {
      throw new IllegalStateException("Must have a Live Data Snapshot Provider");
    }
    if(getPositionMaster() == null) {
      throw new IllegalStateException("Must have a Position Master");
    }
    if(getComputationCacheFactory() == null) {
      throw new IllegalStateException("Must have a View Computation Cache Factory");
    }
    if(getSecurityMaster() == null) {
      throw new IllegalStateException("Must have a Security Master");
    }
  }

  @Override
  public ViewComputationResultModel getMostRecentResult() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PortfolioNode getPositionRoot() {
    return _positionRoot;
  }
  
  public FullyPopulatedPortfolioNode getPopulatedPositionRoot() {
    return _populatedPositionRoot;
  }
  
  public void recalculationPerformed(ViewComputationResultModel result) {
    s_logger.info("Recalculation Performed called.");
  }
  
  // REVIEW kirk 2009-09-11 -- Need to resolve the synchronization on the lifecycle
  // methods.

  @Override
  public synchronized boolean isRunning() {
    return getCalculationState() == ViewCalculationState.RUNNING;
  }

  @Override
  public synchronized void start() {
    s_logger.info("Starting...");
    // TODO kirk 2009-09-11 -- Check state.
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
  public synchronized void stop() {
    s_logger.info("Stopping.....");
    // TODO kirk 2009-09-11 -- Check state.
    
    assert getRecalcJob() != null;
    assert getRecalculationThread() != null;
    
    setCalculationState(ViewCalculationState.TERMINATING);
    
    getRecalcJob().terminate();
    // TODO kirk 2009-09-11 -- Have a heuristic on when to set the timeout based on
    // how long the job is currently taking to cycle.
    long timeout = 100 * 1000l;
    boolean successful = ThreadUtil.safeJoin(getRecalculationThread(), timeout);
    if(!successful) {
      s_logger.warn("Unable to shut down recalc thread in {}ms", timeout);
    }
    
    setCalculationState(ViewCalculationState.TERMINATED);
    
    setRecalcJob(null);
    setRecalculationThread(null);
    
    s_logger.info("Stopped.");
  }

}
