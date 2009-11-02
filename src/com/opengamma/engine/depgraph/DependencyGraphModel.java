/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.analytics.AnalyticFunctionDefinition;
import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.analytics.AnalyticFunctionResolver;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.DefaultAnalyticFunctionResolver;
import com.opengamma.engine.analytics.LiveDataSourcingFunction;
import com.opengamma.engine.analytics.PositionAnalyticFunctionDefinition;
import com.opengamma.engine.analytics.PrimitiveAnalyticFunctionDefinition;
import com.opengamma.engine.analytics.SecurityAnalyticFunctionDefinition;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.util.ArgumentChecker;

/**
 * A full model representing all operations that must be performed to
 * satisfy a particular logical computation model.
 *
 * @author kirk
 */
public class DependencyGraphModel {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphModel.class);
  // Injected Inputs:
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private AnalyticFunctionRepository _analyticFunctionRepository;
  // Result Holders:
  private final RevisedDependencyGraph _primitiveGraph = new RevisedDependencyGraph(ComputationTargetType.PRIMITIVE, null);
  private final Map<Security, RevisedDependencyGraph> _graphForSecurity =
    new HashMap<Security, RevisedDependencyGraph>();
  private final Map<Position, RevisedDependencyGraph> _graphForPosition = 
    new HashMap<Position, RevisedDependencyGraph>();
  private final Map<PortfolioNode, RevisedDependencyGraph> _graphForAggregatePosition =
    new HashMap<PortfolioNode, RevisedDependencyGraph>();
  private final Set<AnalyticValueDefinition<?>> _requiredLiveData =
    new HashSet<AnalyticValueDefinition<?>>();
  
  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @param liveDataAvailabilityProvider the liveDataAvailabilityProvider to set
   */
  public void setLiveDataAvailabilityProvider(
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
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
  public void setAnalyticFunctionRepository(
      AnalyticFunctionRepository analyticFunctionRepository) {
    _analyticFunctionRepository = analyticFunctionRepository;
  }
  
  /**
   * @return the requiredLiveData
   */
  public Set<AnalyticValueDefinition<?>> getRequiredLiveData() {
    return Collections.unmodifiableSet(_requiredLiveData);
  }

  public DependencyNode satisfyDependency(
      AnalyticValueDefinition<?> outputValue,
      AnalyticFunctionResolver functionResolver,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider,
      Object computationTarget
      ) {
    // TODO kirk 2009-11-02 -- Check inputs.
    ComputationTargetType computationTargetType = ComputationTargetType.determineFromTarget(computationTarget);
    assert computationTargetType != null;
    
    DependencyNode node = null;
    node = resolveNode(outputValue, computationTarget, computationTargetType);
    if(node != null) {
      s_logger.debug("Node for {} on {} already resolved for function {}",
          new Object[] {outputValue, computationTarget, node.getFunction().getShortName()});
      return node;
    }
    if(liveDataAvailabilityProvider.isAvailable(outputValue)) {
      assert !_requiredLiveData.contains(outputValue) : "Should have found existing dependency graph node.";
      _requiredLiveData.add(outputValue);
      node = new DependencyNode(new LiveDataSourcingFunction(outputValue), null);
      _primitiveGraph.addNode(node);
      _primitiveGraph.addRequiredLiveData(outputValue);
      return node;
    }

    AnalyticFunctionDefinition function =
      resolveFunctionForTarget(
          outputValue,
          functionResolver,
          computationTarget,
          computationTargetType);
    // Now that we have the function, we can process the node.
    // Functions have different target types from nodes, as you can satisfy a dependency
    // with a less-specific underlying (for example, positions can be satisfied by securities,
    // and everything can be satisfied by a primitive).
    RevisedDependencyGraph depGraph = null;
    Collection<AnalyticValueDefinition<?>> nodeInputs = null;
    switch(function.getTargetType()) {
    case PRIMITIVE:
    {
      assert function instanceof PrimitiveAnalyticFunctionDefinition;
      PrimitiveAnalyticFunctionDefinition primitiveFunction = (PrimitiveAnalyticFunctionDefinition) function;
      node = new DependencyNode(primitiveFunction, null);
      nodeInputs = primitiveFunction.getInputs();
      depGraph = _primitiveGraph;
      break;
    }
    case SECURITY:
    {
      assert function instanceof SecurityAnalyticFunctionDefinition;
      Security securityTarget = null;
      switch(computationTargetType) {
      case SECURITY: securityTarget = (Security) computationTarget; break;
      case POSITION: securityTarget = ((Position) computationTarget).getSecurity(); break;
      default: assert false : "Should not have been able to get a SecurityAnalyticFunctionDefinition on input type " + computationTargetType;
      }
      SecurityAnalyticFunctionDefinition securityFunction = (SecurityAnalyticFunctionDefinition) function;
      node = new DependencyNode(securityFunction, securityTarget);
      nodeInputs = securityFunction.getInputs(securityTarget);
      depGraph = _graphForSecurity.get(securityTarget);
      if(depGraph == null) {
        depGraph = new RevisedDependencyGraph(ComputationTargetType.SECURITY, securityTarget);
        _graphForSecurity.put(securityTarget, depGraph);
      }
      break;
    }
    case POSITION:
    {
      assert function instanceof PositionAnalyticFunctionDefinition;
      Position positionTarget = null;
      switch(computationTargetType) {
      case POSITION: positionTarget = (Position) computationTarget; break;
      default: assert false : "Should not have been able to get a PositionAnalyticFunctionDefinition on input type " + computationTargetType;
      }
      PositionAnalyticFunctionDefinition positionFunction = (PositionAnalyticFunctionDefinition) function;
      node = new DependencyNode(positionFunction, positionTarget);
      nodeInputs = positionFunction.getInputs(positionTarget);
      depGraph = _graphForPosition.get(positionTarget);
      if(depGraph == null) {
        depGraph = new RevisedDependencyGraph(ComputationTargetType.POSITION, positionTarget);
        _graphForPosition.put(positionTarget, depGraph); 
      }
      break;
    }
    case MULTIPLE_POSITIONS:
      throw new OpenGammaRuntimeException("Not yet implemented.");
    }
    assert node != null;
    depGraph.addNode(node);
    // TODO kirk 2009-11-02 -- Handle "buildsOwnSubGraph"
    assert nodeInputs != null;
    for(AnalyticValueDefinition<?> nodeInput: nodeInputs) {
      DependencyNode inputNode = satisfyDependency(nodeInput, functionResolver, liveDataAvailabilityProvider, computationTarget);
      node.addInputNode(nodeInput, inputNode);
    }
    return node;
  }

  /**
   * @param outputValue
   * @param functionResolver
   * @param computationTarget
   * @param computationTargetType
   * @return
   */
  @SuppressWarnings("unchecked")
  protected AnalyticFunctionDefinition resolveFunctionForTarget(
      AnalyticValueDefinition<?> outputValue,
      AnalyticFunctionResolver functionResolver, Object computationTarget,
      ComputationTargetType computationTargetType) {
    AnalyticFunctionDefinition function = null;
    switch(computationTargetType) {
    case PRIMITIVE:
      function = functionResolver.resolve(outputValue);
      break;
    case SECURITY:
      Security securityTarget = (Security) computationTarget;
      function = functionResolver.resolve(outputValue, securityTarget);
      break;
    case POSITION:
      function = functionResolver.resolve(outputValue, (Position) computationTarget);
      break;
    case MULTIPLE_POSITIONS:
      function = functionResolver.resolve(outputValue, (Collection<Position>) computationTarget);
      break;
    }
    if(function == null) {
      throw new UnsatisfiableDependencyGraphException("Could not resolve " + outputValue);
    }
    return function;
  }

  /**
   * @param outputValue
   * @param computationTarget
   * @param computationTargetType
   */
  @SuppressWarnings("unchecked")
  protected DependencyNode resolveNode(AnalyticValueDefinition<?> outputValue,
      Object computationTarget, ComputationTargetType computationTargetType) {
    // TODO kirk 2009-11-02 -- Change DependencyNodeResolver to support this.
    DependencyNode node;
    switch(computationTargetType) {
    case PRIMITIVE:
      node = resolvePrimitive(outputValue);
      break;
    case SECURITY:
      node = resolveSecurity(outputValue, (Security)computationTarget);
      break;
    case POSITION:
      node = resolvePosition(outputValue, (Position)computationTarget);
      break;
    case MULTIPLE_POSITIONS:
      node = resolveMultiplePosition(outputValue, (Collection<Position>) computationTarget);
      break;
    default: throw new OpenGammaRuntimeException("Unhandled computation target type.");
    }
    return node;
  }

  /**
   * @param outputValue
   * @param computationTarget
   * @return
   */
  public DependencyNode resolveMultiplePosition(
      AnalyticValueDefinition<?> outputValue,
      Collection<Position> computationTarget) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param outputValue
   * @param computationTarget
   * @return
   */
  public DependencyNode resolvePosition(
      AnalyticValueDefinition<?> outputValue, Position computationTarget) {
    DependencyNode resolved = null;
    resolved = resolveWithinGraph(outputValue, _primitiveGraph);
    if(resolved != null) {
      return resolved;
    }
    resolved = resolveWithinGraph(outputValue, _graphForSecurity.get(computationTarget.getSecurity()));
    if(resolved != null) {
      return resolved;
    }
    resolved = resolveWithinGraph(outputValue, _graphForPosition.get(computationTarget));
    return resolved;
  }

  /**
   * @param outputValue
   * @param computationTarget
   * @return
   */
  public DependencyNode resolveSecurity(
      AnalyticValueDefinition<?> outputValue, Security computationTarget) {
    DependencyNode resolved = null;
    resolved = resolveWithinGraph(outputValue, _primitiveGraph);
    if(resolved != null) {
      return resolved;
    }
    resolved = resolveWithinGraph(outputValue, _graphForSecurity.get(computationTarget));
    return resolved;
  }

  /**
   * @param outputValue
   * @return
   */
  public DependencyNode resolvePrimitive(AnalyticValueDefinition<?> outputValue) {
    return resolveWithinGraph(outputValue, _primitiveGraph);
  }
  
  private DependencyNode resolveWithinGraph(AnalyticValueDefinition<?> outputValue, RevisedDependencyGraph depGraph) {
    if(depGraph == null) {
      return null;
    }
    return depGraph.getNodeWhichProduces(outputValue);
  }

  @SuppressWarnings("unchecked")
  public void addPosition(Position position, Collection<AnalyticValueDefinition<?>> requiredOutputValues) {
    ArgumentChecker.checkNotNull(position, "Position to add");
    if((getLiveDataAvailabilityProvider() == null)
        || (getAnalyticFunctionRepository() == null)) {
      throw new IllegalStateException("Must have provided a data availability provider and analytic function repository.");
    }
    AnalyticFunctionResolver functionResolver = new DefaultAnalyticFunctionResolver(getAnalyticFunctionRepository());
    RevisedDependencyGraph positionDepGraph = new RevisedDependencyGraph(ComputationTargetType.POSITION, position);
    _graphForPosition.put(position, positionDepGraph);
    for(AnalyticValueDefinition requiredOutputValue : requiredOutputValues) {
      DependencyNode depNode = satisfyDependency(requiredOutputValue, functionResolver, getLiveDataAvailabilityProvider(), position);
      assert depNode != null;
      AnalyticValueDefinition satisfying = depNode.getBestOutput(requiredOutputValue);
      positionDepGraph.setResolvedRequirement(requiredOutputValue, satisfying);
    }
  }
  
  @SuppressWarnings("unchecked")
  public void addAggregatePosition(PortfolioNode node, Collection<AnalyticValueDefinition<?>> requiredOutputValues) {
    if(node == null) {
      throw new NullPointerException("Must provide a valid portfolio node.");
    }
    if((getLiveDataAvailabilityProvider() == null)
        || (getAnalyticFunctionRepository() == null)) {
      throw new IllegalStateException("Must have provided a data availability provider and analytic function repository.");
    }
    if(_graphForAggregatePosition.containsKey(node)) {
      s_logger.debug("Already added portfolio node {}", node);
      return;
    }
    Collection<Position> positions = flattenPortfolio(node);
    AnalyticFunctionResolver functionResolver = new DefaultAnalyticFunctionResolver(getAnalyticFunctionRepository());
    for(AnalyticValueDefinition requiredOutputValue : requiredOutputValues) {
      satisfyDependency(requiredOutputValue, functionResolver, getLiveDataAvailabilityProvider(), positions);
    }
  }
  
  protected Collection<Position> flattenPortfolio(PortfolioNode node) {
    if (node.getSubNodes().size() == 0) { // just a little early optimization.
      return node.getPositions();
    } else {
      Collection<Position> positions = new ArrayList<Position>(node.getPositions());
      for (PortfolioNode subNode : node.getSubNodes()) {
        positions.addAll(flattenPortfolio(subNode));
      }
      return positions;
    }
  }
  
  public Set<AnalyticValueDefinition<?>> getAllRequiredLiveData() {
    return Collections.unmodifiableSet(_requiredLiveData);
  }
  
  /**
   * @return the primitiveGraph
   */
  public RevisedDependencyGraph getPrimitiveGraph() {
    return _primitiveGraph;
  }
  
  public Set<Security> getSecuritiesWithDependencyGraphs() {
    return _graphForSecurity.keySet();
  }

  public RevisedDependencyGraph getDependencyGraph(Security security) {
    return _graphForSecurity.get(security);
  }
  
  public RevisedDependencyGraph getDependencyGraph(PortfolioNode node) {
    return _graphForAggregatePosition.get(node);
  }
  
  public Set<Position> getPositionsWithDependencyGraphs() {
    return _graphForPosition.keySet();
  }

  public RevisedDependencyGraph getDependencyGraph(Position position) {
    return _graphForPosition.get(position);
  }
  
}
