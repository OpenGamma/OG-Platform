/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionBean;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;


// REVIEW kirk 2009-09-16 -- The design goal here is that the portfolio
// evaluation model will be capable of incrementally maintaining its
// state based on certain changes (new position, position quantity changed)
// in the underlying portfolio, but for the time being, I just needed to
// move everything that's static for the portfolio out of the
// eval loop for performance.

/**
 * Holds all data that is specific to a particular version of a {@link Portfolio},
 * and must be re-evaluated when the portfolio changes.
 *
 * @author kirk
 */
public class PortfolioEvaluationModel {
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioEvaluationModel.class);
  private final Portfolio _portfolio;

  private PortfolioNode _populatedRootNode;
  private DependencyGraphModel _dependencyGraphModel;
  // REVIEW kirk 2009-09-14 -- HashSet is almost certainly the wrong set here.
  private final Set<Position> _populatedPositions = new HashSet<Position>();
  private final Set<Security> _securities = new HashSet<Security>();
  
  public PortfolioEvaluationModel(Portfolio portfolio) {
    assert portfolio != null;
    _portfolio = portfolio;
  }

  /**
   * @return the rootNode
   */
  public Portfolio getPortfolio() {
    return _portfolio;
  }

  /**
   * @param populatedRootNode the populatedRootNode to set
   */
  public void setPopulatedRootNode(PortfolioNode populatedRootNode) {
    _populatedRootNode = populatedRootNode;
  }

  /**
   * @return the rootNode
   */
  public PortfolioNode getPopulatedRootNode() {
    return _populatedRootNode;
  }

  /**
   * @return the positions
   */
  public Set<Position> getPopulatedPositions() {
    return _populatedPositions;
  }
  
  /**
   * @return the securities
   */
  public Set<Security> getSecurities() {
    return _securities;
  }

  /**
   * @return the dependencyGraphModel
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

  public void init(
      ViewProcessingContext viewProcessingContext,
      ViewDefinition viewDefinition) {
    ArgumentChecker.checkNotNull(viewProcessingContext, "View Processing Context");
    ArgumentChecker.checkNotNull(viewDefinition, "View Definition");
    
    PortfolioNode populatedRootNode = getPopulatedPortfolioNode(getPortfolio(), viewProcessingContext.getSecurityMaster());
    assert populatedRootNode != null;
    setPopulatedRootNode(populatedRootNode);
    
    loadPositions();
    loadSecurities();
    buildDependencyGraphs(
        viewProcessingContext.getAnalyticFunctionRepository(),
        viewProcessingContext.getLiveDataAvailabilityProvider(),
        viewProcessingContext.getComputationTargetResolver(),
        viewDefinition);
    addLiveDataSubscriptions(viewProcessingContext.getLiveDataSnapshotProvider());
  }

  /**
   * @param node
   * @return
   */
  protected PortfolioNode getPopulatedPortfolioNode(
      PortfolioNode node, SecurityMaster secMaster) {
    if(node == null) {
      return null;
    }
    // REVIEW kirk 2009-09-16 -- This should actually be three passes:
    // - Gather up all the distinct SecurityKeys
    // - Scatter/gather to resolve all Securities
    // - Build the node tree from the resolved Securities
    PortfolioNodeImpl populatedNode = new PortfolioNodeImpl();
    populatedNode.setIdentityKey(node.getIdentityKey());
    for(Position position : node.getPositions()) {
      Security security = position.getSecurity();
      if(position.getSecurity() == null) {
        security = secMaster.getSecurity(position.getSecurityKey());
      }
      if(security == null) {
        throw new OpenGammaRuntimeException("Unable to resolve security key " + position.getSecurityKey() + " for position " + position);
      }
      PositionBean populatedPosition = new PositionBean(position.getQuantity(), position.getSecurityKey(), security);  // we could just reuse the existing object?
      populatedPosition.setIdentityKey(position.getIdentityKey());
      populatedNode.addPosition(populatedPosition);
    }
    for(PortfolioNode subNode : node.getSubNodes()) {
      populatedNode.addSubNode(getPopulatedPortfolioNode(subNode, secMaster));
    }
    return populatedNode;
  }

  public void loadPositions() {
    PortfolioNode populatedRootNode = getPopulatedRootNode();
    loadPositions(populatedRootNode);
    setPopulatedRootNode(populatedRootNode);
    s_logger.debug("Operating on {} positions", getPopulatedPositions().size());
    // TODO kirk 2009-09-15 -- cache securities
  }
  
  protected void loadPositions(PortfolioNode node) {
    getPopulatedPositions().addAll(node.getPositions());
    for(PortfolioNode child : node.getSubNodes()) {
      loadPositions(child);
    }
  }
  
  public void loadSecurities() {
    for(Position position : getPopulatedPositions()) {
      getSecurities().add(position.getSecurity());
    }
  }
  
  public void buildDependencyGraphs(
      FunctionRepository analyticFunctionRepository,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider,
      ComputationTargetResolver computationTargetResolver,
      ViewDefinition viewDefinition) {
    DependencyGraphModel dependencyGraphModel = new DependencyGraphModel();
    dependencyGraphModel.setFunctionRepository(analyticFunctionRepository);
    dependencyGraphModel.setLiveDataAvailabilityProvider(liveDataAvailabilityProvider);
    dependencyGraphModel.setTargetResolver(computationTargetResolver);

    Map<String, Set<String>> outputsBySecurityType = viewDefinition.getValueDefinitionsBySecurityTypes();
    for(Position position : getPopulatedPositions()) {
      // REVIEW kirk 2009-09-04 -- This is potentially a VERY computationally expensive
      // operation. We could/should do them in parallel.
      Set<String> requiredOutputValues = outputsBySecurityType.get(position.getSecurity().getSecurityType());
      Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      for(String requirementName : requiredOutputValues) {
        ValueRequirement requirement = new ValueRequirement(requirementName, ComputationTargetType.POSITION, position.getIdentityKey());
        requirements.add(requirement);
      }
      dependencyGraphModel.addTarget(new ComputationTarget(ComputationTargetType.POSITION, position), requirements);
    }
    //buildDependencyGraphs(getPopulatedRootNode(), dependencyGraphModel, analyticFunctionRepository, liveDataAvailabilityProvider, viewDefinition);
    setDependencyGraphModel(dependencyGraphModel);
  }
  
  /*
  public Set<String> buildDependencyGraphs(PortfolioNode node,
      DependencyGraphModel dependencyGraphModel,
      FunctionRepository analyticFunctionRepository, 
      LiveDataAvailabilityProvider liveDataAvailabilityProvider, 
      ViewDefinition viewDefinition) {
      Set<String> securityTypesUnder = new TreeSet<String>();
      // find out the security types for all the positions we have right here.
      for(Position position : node.getPositions()) {
        securityTypesUnder.add(position.getSecurity().getSecurityType());
      }
      // recursively find the security types for the positions under this node.
      for (PortfolioNode subNode : node.getSubNodes()) {
        securityTypesUnder.addAll(buildDependencyGraphs(subNode, dependencyGraphModel, analyticFunctionRepository, liveDataAvailabilityProvider, viewDefinition));
      }
      // now we work out the intersection of all the value definitions for those securities.
      Map<String, Collection<AnalyticValueDefinition<?>>> outputsBySecurityType = viewDefinition.getValueDefinitionsBySecurityTypes();
      Set<AnalyticValueDefinition<?>> requiredOutputs = new HashSet<AnalyticValueDefinition<?>>();
      // calculate the UNION
      for(String type : securityTypesUnder) {
        requiredOutputs.addAll(outputsBySecurityType.get(type));
      }
      // add this node to the dependency model.
      dependencyGraphModel.addAggregatePosition(node, requiredOutputs);
      return securityTypesUnder;
  }
  */
  
  public void addLiveDataSubscriptions(LiveDataSnapshotProvider liveDataSnapshotProvider) {
    assert liveDataSnapshotProvider != null;
    Set<ValueRequirement> requiredLiveData = getDependencyGraphModel().getAllRequiredLiveData();
    s_logger.info("Informing snapshot provider of {} subscriptions to input data", requiredLiveData.size());
    for(ValueRequirement liveDataRequirement : requiredLiveData) {
      liveDataSnapshotProvider.addSubscription(liveDataRequirement);
    }
  }

}
