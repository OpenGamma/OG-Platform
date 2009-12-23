/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AggregatePositionFunctionDefinition;
import com.opengamma.engine.analytics.FunctionDefinition;
import com.opengamma.engine.analytics.FunctionRepository;
import com.opengamma.engine.analytics.AnalyticFunctionResolver;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.DefaultAnalyticFunctionResolver;
import com.opengamma.engine.analytics.LiveDataSourcingFunction;
import com.opengamma.engine.analytics.PositionFunctionDefinition;
import com.opengamma.engine.analytics.PrimitiveFunctionDefinition;
import com.opengamma.engine.analytics.SecurityFunctionDefinition;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.util.ArgumentChecker;

/**
 * A logical dependency graph capable of satisfying computations
 * for a particular security type.
 *
 * @author kirk
 */
public class DependencyGraph {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraph.class);
  private Security _security;
  private Position _position;
  private Collection<Position> _positions;
  private final Set<AnalyticValueDefinition<?>> _requiredOutputValues =
    new HashSet<AnalyticValueDefinition<?>>();
  private final Set<AnalyticValueDefinition<?>> _requiredLiveData =
    new HashSet<AnalyticValueDefinition<?>>();
  private final Set<DependencyNode> _topLevelNodes =
    new HashSet<DependencyNode>();
  private final Map<AnalyticValueDefinition<?>, AnalyticValueDefinition<?>> _resolvedOutputs =
    new HashMap<AnalyticValueDefinition<?>, AnalyticValueDefinition<?>>();
  private int _nodeCount = 0;
  
  // At this point we don't know if the function required is primitive, security specific or position specific
  // although we do no whether or not it's an aggregate position, so we provide position and security info that
  // might or might not prove necessary at this point
  public DependencyGraph(Collection<AnalyticValueDefinition<?>> requiredOutputValues, Security security, Position position) {
    _security = security;
    _position = position;
    _positions = null;
    if(requiredOutputValues != null) {
      _requiredOutputValues.addAll(requiredOutputValues);
    }
  }
  // Aggregate position specific function constructor - in this case we already know what the function's going to need.
  public DependencyGraph(Collection<AnalyticValueDefinition<?>> requiredOutputValues, Collection<Position> positions) {
    if(positions == null) {
      throw new NullPointerException("Must specify a valid security.");
    }
    _security = null;
    _position = null;
    _positions = positions;
    if(requiredOutputValues != null) {
      _requiredOutputValues.addAll(requiredOutputValues);
    }
  }

  private void checkState() {
    if (_position != null && _security != null) {
      throw new OpenGammaRuntimeException("Internal state is indeterminate until dep graph is built");
    }
  }
  /**
   * This should only be called if getComputationTargetType() returns SECURITY_KEY
   * @return the securityKey
   */
  public Security getSecurity() {
    if (_security == null) {
      s_logger.warn("getSecurityKey() called when job is "+toString());
    }
    checkState();
    return _security;
  }
  
  /**
   * This should only be called if getComputationTargetType() returns POSITION
   * @return the position
   */
  public Position getPosition() {
    if (_position == null) {
      s_logger.warn("getPosition() called when job is "+toString());
    }
    checkState();
    return _position;
  }
  
  /**
   * This should only be called if getPositions() returns AGGREGATE_POSITION
   * @return the positions
   */
  public Collection<Position> getPositions() {
    if (_positions == null) {
      s_logger.warn("getPositions() called when job is "+toString());
    }
    return _positions;
  }
  
  public ComputationTarget getComputationTargetType() {
    checkState();
    if (_security != null) {
      assert _position == null;
      assert _positions == null;
      return ComputationTarget.SECURITY;
    } else if (_position != null) {
      assert _positions == null; // already checked _securityKey
      return ComputationTarget.POSITION;
    } else if (_positions != null) { // already checked the others.
      return ComputationTarget.MULTIPLE_POSITIONS;
    } else {
      return ComputationTarget.PRIMITIVE;
    }
  }
  public enum ComputationTarget {
    PRIMITIVE, SECURITY, POSITION, MULTIPLE_POSITIONS
  }
    
  /**
   * @return the requiredOutputValues
   */
  public Set<AnalyticValueDefinition<?>> getRequiredOutputValues() {
    return _requiredOutputValues;
  }

  /**
   * @return the requiredLiveData
   */
  public Set<AnalyticValueDefinition<?>> getRequiredLiveData() {
    return _requiredLiveData;
  }

  /**
   * @return the topLevelNodes
   */
  public Set<DependencyNode> getTopLevelNodes() {
    return _topLevelNodes;
  }

  /**
   * @return the nodeCount
   */
  public int getNodeCount() {
    return _nodeCount;
  }

  /**
   * @param nodeCount the nodeCount to set
   */
  public void setNodeCount(int nodeCount) {
    _nodeCount = nodeCount;
  }

  /**
   * @return the resolvedOutputs
   */
  public Map<AnalyticValueDefinition<?>, AnalyticValueDefinition<?>> getResolvedOutputs() {
    return _resolvedOutputs;
  }

  public void buildDependencyGraph(
      FunctionRepository functionRepository,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    ArgumentChecker.checkNotNull(functionRepository, "Analytic Function Repository");
    ArgumentChecker.checkNotNull(liveDataAvailabilityProvider, "Live Data Availability Provider");
    buildDependencyGraphImpl(functionRepository, liveDataAvailabilityProvider);
  }
  
  // TODO kirk 2009-09-04 -- Determine how to specify all the various failed dependencies.
  protected void buildDependencyGraphImpl(
      FunctionRepository functionRepository,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    assert functionRepository != null;
    assert liveDataAvailabilityProvider != null;
    
    DefaultAnalyticFunctionResolver functionResolver = new DefaultAnalyticFunctionResolver(functionRepository);

    DefaultDependencyNodeResolver nodeResolver = new DefaultDependencyNodeResolver();
    Set<DependencyNode> topLevelNodes = new HashSet<DependencyNode>();
    Set<AnalyticValueDefinition<?>> requiredLiveData = new HashSet<AnalyticValueDefinition<?>>();
    for(AnalyticValueDefinition<?> outputValue : getRequiredOutputValues()) {
      DependencyNode topLevelNode = satisfyDependency(outputValue, nodeResolver, requiredLiveData, functionResolver, liveDataAvailabilityProvider);
      assert topLevelNode != null;
      topLevelNodes.add(topLevelNode);
      _resolvedOutputs.put(outputValue, topLevelNode.getBestOutput(outputValue));
    }
    getRequiredLiveData().clear();
    getRequiredLiveData().addAll(requiredLiveData);
    s_logger.info("{} built graph with {} nodes", getPosition(), nodeResolver.size());
    getTopLevelNodes().addAll(topLevelNodes);
    setNodeCount(nodeResolver.size());
  }
  
  protected DependencyNode satisfyDependency(
      AnalyticValueDefinition<?> outputValue,
      DefaultDependencyNodeResolver nodeResolver,
      Set<AnalyticValueDefinition<?>> requiredLiveData,
      AnalyticFunctionResolver functionResolver,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    DependencyNode node = null;
    node = nodeResolver.resolve(outputValue);
    if(node != null) {
      s_logger.debug("Satisfied output value {} with existing dependency", outputValue);
      return node;
    }
    
    // Can we satisfy this item using live data?
    if(liveDataAvailabilityProvider.isAvailable(outputValue)) {
      assert !requiredLiveData.contains(outputValue) : "Should have found existing dependency graph node.";
      requiredLiveData.add(outputValue);
      node = new DependencyNode(new LiveDataSourcingFunction(outputValue), null);
      nodeResolver.addSubGraph(node);
      return node;
    }
    // note we access the fields here directly because the get methods contains checks that mean they 
    // won't work until we've established the function type.
    FunctionDefinition function;
    if(_security != null) {
      function = functionResolver.resolve(outputValue, _security);
      if(function == null) {
        if(_position != null) {
          function = functionResolver.resolve(outputValue, _position);
        } else {
          s_logger.error("Cannot resolve function - trying to find match for "+outputValue+" for "+getPosition());
          throw new NullPointerException();
        }
      }
    } else {
      if(_positions != null) {
        function = functionResolver.resolve(outputValue, _positions);
        if(function == null) {
          s_logger.error("Cannot resolve function - trying to find match for "+outputValue+" for "+getPositions());
          throw new NullPointerException();          
        }
      } else {
        s_logger.error("Cannot resolve function - no security or positions data at all while trying to find match for "+outputValue);
        throw new NullPointerException();
      }
    }

    if(function.buildsOwnSubGraph()) {
      if(function instanceof PrimitiveFunctionDefinition) {
        assert getComputationTargetType() == ComputationTarget.PRIMITIVE;
        node = ((PrimitiveFunctionDefinition) function).buildSubGraph(functionResolver, nodeResolver);
      } else if (function instanceof SecurityFunctionDefinition) {
        _position = null;
        assert getComputationTargetType() == ComputationTarget.SECURITY;
        node = ((SecurityFunctionDefinition) function).buildSubGraph(getSecurity(), functionResolver, nodeResolver);
      } else if (function instanceof PositionFunctionDefinition) {
        _security = null;
        assert getComputationTargetType() == ComputationTarget.POSITION;
        node = ((PositionFunctionDefinition) function).buildSubGraph(getPosition(), functionResolver, nodeResolver);
      } else if (function instanceof AggregatePositionFunctionDefinition) {
        assert getComputationTargetType() == ComputationTarget.MULTIPLE_POSITIONS;
        node = ((AggregatePositionFunctionDefinition) function).buildSubGraph(getPositions(), functionResolver, nodeResolver);
      } else {
        throw new IllegalStateException("Cannot handle anything other than Primitive, Security, Position or Aggregate Position function");
      }
      nodeResolver.addSubGraph(node);
    } else {
      if(function instanceof PrimitiveFunctionDefinition) {
        assert getComputationTargetType() == ComputationTarget.PRIMITIVE;
        node = new DependencyNode((PrimitiveFunctionDefinition) function, null);
      } else if(function instanceof SecurityFunctionDefinition) {
        _position = null;
        assert getComputationTargetType() == ComputationTarget.SECURITY;
        node = new DependencyNode((SecurityFunctionDefinition) function, getSecurity());        
      } else if(function instanceof PositionFunctionDefinition) {
        _security = null;
        assert getComputationTargetType() == ComputationTarget.POSITION;
        node = new DependencyNode((PositionFunctionDefinition) function, getPosition());
      } else if(function instanceof AggregatePositionFunctionDefinition) {
        assert getComputationTargetType() == ComputationTarget.MULTIPLE_POSITIONS;
        node = new DependencyNode((AggregatePositionFunctionDefinition) function, getPositions());
      } else {
        throw new IllegalStateException("Cannot handle anything other than Primitive, Security, Position or Aggregate Position functions.");
      }
      nodeResolver.addSubGraph(node);
      for(AnalyticValueDefinition<?> inputValue : node.getInputValues()) {
        DependencyNode inputNode = satisfyDependency(inputValue, nodeResolver, requiredLiveData, functionResolver, liveDataAvailabilityProvider);
        assert inputNode != null : "This is a bad assertion. Do something better.";
        node.addInputNode(inputValue, inputNode);
      }
    }
    assert node != null;
    
    return node;
  }
}
