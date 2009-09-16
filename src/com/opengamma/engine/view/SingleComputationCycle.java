/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.security.Security;

// TODO kirk 2009-09-14 -- Do we need some type of progress monitor?

/**
 * Holds all data and actions for a single pass through a computation cycle.
 * In general, each invocation of {@link ViewRecalculationJob#runOneCycle()}
 * will create an instance of this class.
 * <p/>
 * At the moment, the concurrency metaphor is:
 * <ul>
 *   <li>Each distinct security has its own execution plan</li>
 *   <li>The cycle will schedule each node in the execution plan sequentially</li>
 *   <li>If there are shared sub-graphs that aren't security specific, they'll be captured at execution time.</li>
 * </ul>
 * This is, of course, not optimal, and later on we can fix that.
 *
 * @author kirk
 */
public class SingleComputationCycle {
  private static final Logger s_logger = LoggerFactory.getLogger(SingleComputationCycle.class);
  // Injected Inputs:
  private final ViewComputationCache _computationCache;
  private final FullyPopulatedPortfolioNode _rootNode;
  private final LiveDataSnapshotProvider _snapshotProvider;
  private final ViewDefinition _viewDefinition;
  private final AnalyticFunctionRepository _analyticFunctionRepository;
  private final LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  
  // State:
  private final long _startTime;
  private DependencyGraphModel _dependencyGraphModel;
  // REVIEW kirk 2009-09-14 -- HashSet is almost certainly the wrong set here.
  private final Set<FullyPopulatedPosition> _populatedPositions = new HashSet<FullyPopulatedPosition>();
  private final Map<Security, PerSecurityExecutionPlan> _plansBySecurity = new HashMap<Security, PerSecurityExecutionPlan>();
  
  // Outputs:
  private long _snapshotTime;
  private final ViewComputationResultModelImpl _resultModel;
  
  public SingleComputationCycle(
      ViewComputationCache cache,
      FullyPopulatedPortfolioNode rootNode,
      LiveDataSnapshotProvider snapshotProvider,
      ViewComputationResultModelImpl resultModel,
      ViewDefinition viewDefinition,
      AnalyticFunctionRepository analyticFunctionRepository,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    assert cache != null;
    assert rootNode != null;
    assert snapshotProvider != null;
    assert resultModel != null;
    assert viewDefinition != null;
    assert analyticFunctionRepository != null;
    assert liveDataAvailabilityProvider != null;
    _computationCache = cache;
    _rootNode = rootNode;
    _snapshotProvider = snapshotProvider;
    _resultModel = resultModel;
    _viewDefinition = viewDefinition;
    _analyticFunctionRepository = analyticFunctionRepository;
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
    _startTime = System.currentTimeMillis();
  }
  
  /**
   * @return the snapshotTime
   */
  public long getSnapshotTime() {
    return _snapshotTime;
  }

  /**
   * @param snapshotTime the snapshotTime to set
   */
  public void setSnapshotTime(long snapshotTime) {
    _snapshotTime = snapshotTime;
  }

  /**
   * @return the computationCache
   */
  public ViewComputationCache getComputationCache() {
    return _computationCache;
  }

  /**
   * @return the rootNode
   */
  public FullyPopulatedPortfolioNode getRootNode() {
    return _rootNode;
  }

  /**
   * @return the snapshotProvider
   */
  public LiveDataSnapshotProvider getSnapshotProvider() {
    return _snapshotProvider;
  }

  /**
   * @return the analyticFunctionRepository
   */
  public AnalyticFunctionRepository getAnalyticFunctionRepository() {
    return _analyticFunctionRepository;
  }

  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @return the startTime
   */
  public long getStartTime() {
    return _startTime;
  }

  /**
   * @return the logicalDependencyGraph
   */
  public DependencyGraphModel getDependencyGraphModel() {
    return _dependencyGraphModel;
  }

  /**
   * @param dependencyGraphModel the dependencyGraphModel to set
   */
  public void setDependencyGraphModel(DependencyGraphModel dependencyGraphModel) {
    _dependencyGraphModel = dependencyGraphModel;
  }

  /**
   * @return the resultModel
   */
  public ViewComputationResultModelImpl getResultModel() {
    return _resultModel;
  }

  /**
   * @return the positions
   */
  public Set<FullyPopulatedPosition> getPopulatedPositions() {
    return _populatedPositions;
  }

  /**
   * @return the viewDefinition
   */
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  public void prepareInputs() {
    setSnapshotTime(getSnapshotProvider().snapshot());
    getResultModel().setInputDataTimestamp(getSnapshotTime());
    
    Set<AnalyticValueDefinition> requiredLiveData = getDependencyGraphModel().getAllRequiredLiveData();
    s_logger.debug("Populating {} market data items for snapshot {}", requiredLiveData.size(), getSnapshotTime());
    
    for(AnalyticValueDefinition requiredDataDefinition : requiredLiveData) {
      AnalyticValue value = getSnapshotProvider().querySnapshot(getSnapshotTime(), requiredDataDefinition);
      if(value == null) {
        s_logger.warn("Unable to load live data value for {} at snapshot {}", requiredDataDefinition, getSnapshotTime());
      } else {
        getComputationCache().putValue(value);
      }
    }
  }
  
  public void loadPositions() {
    loadPositions(getRootNode());
    s_logger.debug("Operating on {} positions this cycle", getPopulatedPositions().size());
    // TODO kirk 2009-09-15 -- cache securities
  }
  
  protected void loadPositions(FullyPopulatedPortfolioNode node) {
    for(FullyPopulatedPortfolioNode child : node.getPopulatedSubNodes()) {
      loadPositions(child);
    }
  }
  
  public void buildDependencyGraphs() {
    Set<Security> securities = new HashSet<Security>();
    for(FullyPopulatedPosition position : getPopulatedPositions()) {
      securities.add(position.getSecurity());
    }
    
    DependencyGraphModel dependencyGraphModel = new DependencyGraphModel();
    dependencyGraphModel.setAnalyticFunctionRepository(getAnalyticFunctionRepository());
    dependencyGraphModel.setLiveDataAvailabilityProvider(getLiveDataAvailabilityProvider());

    Map<String, Collection<AnalyticValueDefinition>> outputsBySecurityType = getViewDefinition().getValueDefinitionsBySecurityTypes();
    for(Security security : securities) {
      // REVIEW kirk 2009-09-04 -- This is potentially a VERY computationally expensive
      // operation. We could/should do them in parallel.
      Collection<AnalyticValueDefinition> requiredOutputValues = outputsBySecurityType.get(security.getSecurityType());
      dependencyGraphModel.addSecurity(security, requiredOutputValues);
    }
    setDependencyGraphModel(dependencyGraphModel);
  }
  
  public void buildExecutionPlans() {
    assert getDependencyGraphModel() != null;
    
    Set<Security> securities = new HashSet<Security>();
    for(FullyPopulatedPosition position : getPopulatedPositions()) {
      securities.add(position.getSecurity());
    }
    s_logger.debug("Building execution plans for {} distinct securities", securities.size());
    for(Security security : securities) {
      PerSecurityExecutionPlan executionPlan = new PerSecurityExecutionPlan(security, getDependencyGraphModel().getDependencyGraph(security));
      _plansBySecurity.put(security, executionPlan);
    }
    for(PerSecurityExecutionPlan executionPlan : _plansBySecurity.values()) {
      // TODO kirk 2009-09-14 -- This might be expensive in the future, so should be done
      // in a parallel form.
      executionPlan.buildExecutionPlan();
    }
  }
  
  public void executePlans() {
    for(PerSecurityExecutionPlan executionPlan : _plansBySecurity.values()) {
      // TODO kirk 2009-09-14 -- Yep, need some concurrency here as well.
      executePlan(executionPlan);
    }
  }

  /**
   * @param executionPlan
   */
  private void executePlan(PerSecurityExecutionPlan executionPlan) {
    // TODO kirk 2009-09-14 -- Yep, this sucks. Totally first-gen code.
    for(DependencyNode node : executionPlan.getOrderedNodes()) {
      // First of all, check that we don't have the outputs already ready.
      boolean allFound = true;
      for(AnalyticValueDefinition outputDefinition : node.getOutputValues()) {
        if(getComputationCache().getValue(outputDefinition) == null) {
          allFound = false;
          break;
        }
      }
      
      if(allFound) {
        s_logger.debug("Able to skip a node because it was already computed.");
        continue;
      }
      
      Collection<AnalyticValue> inputs = new HashSet<AnalyticValue>();
      for(AnalyticValueDefinition inputDefinition : node.getInputValues()) {
        inputs.add(getComputationCache().getValue(inputDefinition));
      }
      Collection<AnalyticValue> outputs = node.getFunction().execute(inputs, executionPlan.getSecurity());
      for(AnalyticValue outputValue : outputs) {
        getComputationCache().putValue(outputValue);
      }
    }
  }
  
  public void populateResultModel() {
    Map<String, Collection<AnalyticValueDefinition>> valueDefsBySecTypes = getViewDefinition().getValueDefinitionsBySecurityTypes(); 
    for(FullyPopulatedPosition position : getPopulatedPositions()) {
      // REVIEW kirk 2009-09-14 -- Could be parallelized if we need to.
      Security security = position.getSecurity();
      String securityType = security.getSecurityType();
      Collection<AnalyticValueDefinition> secTypeValueDefs = valueDefsBySecTypes.get(securityType);
      
      for(AnalyticValueDefinition analyticValueDefinition : secTypeValueDefs) {
        AnalyticValue unscaledValue = getComputationCache().getValue(analyticValueDefinition);
        if(unscaledValue != null) {
          AnalyticValue scaledValue = unscaledValue.scaleForPosition(position.getPosition().getQuantity());
          getResultModel().addValue(position.getPosition(), scaledValue);
        }
      }
    }
  }

  /**
   * 
   */
  public void addLiveDataSubscriptions() {
    Set<AnalyticValueDefinition> requiredLiveData = getDependencyGraphModel().getAllRequiredLiveData();
    s_logger.info("Informing snapshot provider of {} subscriptions to input data", requiredLiveData.size());
    for(AnalyticValueDefinition liveDataDefinition : requiredLiveData) {
      getSnapshotProvider().addSubscription(liveDataDefinition);
    }
  }

}
