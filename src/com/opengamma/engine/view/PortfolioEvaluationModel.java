/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;


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
  private final PortfolioNode _rootNode;

  private FullyPopulatedPortfolioNode _populatedRootNode;
  private DependencyGraphModel _dependencyGraphModel;
  // REVIEW kirk 2009-09-14 -- HashSet is almost certainly the wrong set here.
  private final Set<FullyPopulatedPosition> _populatedPositions = new HashSet<FullyPopulatedPosition>();
  private final Set<Security> _securities = new HashSet<Security>();
  
  public PortfolioEvaluationModel(PortfolioNode rootNode) {
    assert rootNode != null;
    _rootNode = rootNode;
  }

  /**
   * @return the rootNode
   */
  public PortfolioNode getRootNode() {
    return _rootNode;
  }

  /**
   * @param populatedRootNode the populatedRootNode to set
   */
  public void setPopulatedRootNode(FullyPopulatedPortfolioNode populatedRootNode) {
    _populatedRootNode = populatedRootNode;
  }

  /**
   * @return the rootNode
   */
  public FullyPopulatedPortfolioNode getPopulatedRootNode() {
    return _populatedRootNode;
  }

  /**
   * @return the positions
   */
  public Set<FullyPopulatedPosition> getPopulatedPositions() {
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
      SecurityMaster secMaster,
      AnalyticFunctionRepository analyticFunctionRepository,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider,
      LiveDataSnapshotProvider liveDataSnapshotProvider,
      ViewDefinition viewDefinition) {
    assert secMaster != null;
    assert analyticFunctionRepository != null;
    assert liveDataAvailabilityProvider != null;
    assert liveDataSnapshotProvider != null;
    assert viewDefinition != null;
    
    FullyPopulatedPortfolioNode populatedRootNode = getPopulatedPortfolioNode(getRootNode(), secMaster);
    assert populatedRootNode != null;
    setPopulatedRootNode(populatedRootNode);
    
    loadPositions();
    loadSecurities();
    buildDependencyGraphs(analyticFunctionRepository, liveDataAvailabilityProvider, viewDefinition);
    addLiveDataSubscriptions(liveDataSnapshotProvider);
  }

  /**
   * @param node
   * @return
   */
  protected FullyPopulatedPortfolioNode getPopulatedPortfolioNode(
      PortfolioNode node, SecurityMaster secMaster) {
    if(node == null) {
      return null;
    }
    // REVIEW kirk 2009-09-16 -- This should actually be three passes:
    // - Gather up all the distinct SecurityKeys
    // - Scatter/gather to resolve all Securities
    // - Build the node tree from the resolved Securities
    FullyPopulatedPortfolioNode populatedNode = new FullyPopulatedPortfolioNode();
    for(Position position : node.getPositions()) {
      Security security = secMaster.getSecurity(position.getSecurityKey());
      if(security == null) {
        throw new OpenGammaRuntimeException("Unable to resolve security key " + position.getSecurityKey() + " for position " + position);
      }
      populatedNode.addPosition(position, security);
    }
    for(PortfolioNode subNode : node.getSubNodes()) {
      populatedNode.addSubNode(getPopulatedPortfolioNode(subNode, secMaster));
    }
    return populatedNode;
  }

  public void loadPositions() {
    FullyPopulatedPortfolioNode populatedRootNode = getPopulatedRootNode();
    loadPositions(populatedRootNode);
    setPopulatedRootNode(populatedRootNode);
    s_logger.debug("Operating on {} positions", getPopulatedPositions().size());
    // TODO kirk 2009-09-15 -- cache securities
  }
  
  protected void loadPositions(FullyPopulatedPortfolioNode node) {
    getPopulatedPositions().addAll(node.getPopulatedPositions());
    for(FullyPopulatedPortfolioNode child : node.getPopulatedSubNodes()) {
      loadPositions(child);
    }
  }
  
  public void loadSecurities() {
    for(FullyPopulatedPosition position : getPopulatedPositions()) {
      getSecurities().add(position.getSecurity());
    }
  }
  
  public void buildDependencyGraphs(AnalyticFunctionRepository analyticFunctionRepository, LiveDataAvailabilityProvider liveDataAvailabilityProvider, ViewDefinition viewDefinition) {
    DependencyGraphModel dependencyGraphModel = new DependencyGraphModel();
    dependencyGraphModel.setAnalyticFunctionRepository(analyticFunctionRepository);
    dependencyGraphModel.setLiveDataAvailabilityProvider(liveDataAvailabilityProvider);

    Map<String, Collection<AnalyticValueDefinition<?>>> outputsBySecurityType = viewDefinition.getValueDefinitionsBySecurityTypes();
    for(Security security : getSecurities()) {
      // REVIEW kirk 2009-09-04 -- This is potentially a VERY computationally expensive
      // operation. We could/should do them in parallel.
      Collection<AnalyticValueDefinition<?>> requiredOutputValues = outputsBySecurityType.get(security.getSecurityType());
      dependencyGraphModel.addSecurity(security, requiredOutputValues);
    }
    setDependencyGraphModel(dependencyGraphModel);
  }
  
  public void addLiveDataSubscriptions(LiveDataSnapshotProvider liveDataSnapshotProvider) {
    assert liveDataSnapshotProvider != null;
    Set<AnalyticValueDefinition<?>> requiredLiveData = getDependencyGraphModel().getAllRequiredLiveData();
    s_logger.info("Informing snapshot provider of {} subscriptions to input data", requiredLiveData.size());
    for(AnalyticValueDefinition<?> liveDataDefinition : requiredLiveData) {
      liveDataSnapshotProvider.addSubscription(liveDataDefinition);
    }
  }

}
