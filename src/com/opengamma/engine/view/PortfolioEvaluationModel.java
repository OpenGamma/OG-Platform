/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFormatter;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.FunctionResolver;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.position.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.PortfolioNodeTraverser;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionBean;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;


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
  private static final boolean OUTOUT_DEPENDENCY_GRAPHS = false;
  private final Portfolio _portfolio;

  private PortfolioNode _populatedRootNode;
  // REVIEW kirk 2010-03-29 -- Use a sorted map here?
  private final Map<String, DependencyGraphModel> _graphModelsByConfiguration =
    new ConcurrentHashMap<String, DependencyGraphModel>();
  private final Map<IdentifierBundle, Security> _securitiesByKey = new ConcurrentHashMap<IdentifierBundle, Security>();
  // REVIEW kirk 2009-09-14 -- HashSet is almost certainly the wrong set here.
  private final Set<Position> _populatedPositions = new HashSet<Position>();
  private final Set<Security> _securities = new HashSet<Security>();
  
  public PortfolioEvaluationModel(Portfolio portfolio) {
    assert portfolio != null;
    _portfolio = portfolio;
  }

  /**
   * @return the portfolio
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
  
  public Set<String> getAllCalculationConfigurationNames() {
    return new TreeSet<String>(_graphModelsByConfiguration.keySet());
  }
  
  public Collection<DependencyGraphModel> getAllDependencyGraphModels() {
    return new ArrayList<DependencyGraphModel>(_graphModelsByConfiguration.values());
  }
  
  public DependencyGraphModel getDependencyGraphModel(String calcConfigName) {
    return _graphModelsByConfiguration.get(calcConfigName);
  }

  public void init(
      ViewProcessingContext viewProcessingContext,
      ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(viewProcessingContext, "View Processing Context");
    ArgumentChecker.notNull(viewDefinition, "View Definition");
    
    resolveSecurities(viewProcessingContext);
    
    PortfolioNode populatedRootNode = getPopulatedPortfolioNode(getPortfolio().getRootNode(), viewProcessingContext.getSecurityMaster());
    assert populatedRootNode != null;
    setPopulatedRootNode(populatedRootNode);
    
    loadPositions();
    loadSecurities();
    buildDependencyGraphs(
        viewProcessingContext.getFunctionRepository(),
        viewProcessingContext.getFunctionResolver(),
        viewProcessingContext.getCompilationContext(),
        viewProcessingContext.getLiveDataAvailabilityProvider(),
        viewProcessingContext.getComputationTargetResolver(),
        viewDefinition);
    if(OUTOUT_DEPENDENCY_GRAPHS) {
      outputDependencyGraphs();
    }
    addLiveDataSubscriptions(viewProcessingContext, viewDefinition);
  }
  
  private void outputDependencyGraphs() {
    StringBuilder sb = new StringBuilder();
    for(DependencyGraphModel depGraphModel : _graphModelsByConfiguration.values()) {
      sb.append("DepGraphModel for ").append(depGraphModel.getCalculationConfigurationName());
      for(DependencyGraph depGraph : depGraphModel.getAllDependencyGraphs()) {
        sb.append("\tGot dep graph for ").append(depGraph.getComputationTarget());
        sb.append("\tProducing values ").append(depGraph.getOutputValues());
        for(DependencyNode depNode : depGraph.getDependencyNodes()) {
          sb.append("\t\tNode:\n").append(DependencyNodeFormatter.toString(depNode));
        }
      }
    }
    s_logger.warn("Dependency Graph Models -- \n{}", sb);
  }
  
  protected void resolveSecurities(final ViewProcessingContext viewProcessingContext) {
    // TODO kirk 2010-03-07 -- Need to switch to OperationTimer for this.
    OperationTimer timer = new OperationTimer(s_logger, "Resolving all securities for {}", getPortfolio().getName());
    Set<IdentifierBundle> securityKeys = getSecurityKeysForResolution(getPortfolio().getRootNode());
    ExecutorCompletionService<IdentifierBundle> completionService = new ExecutorCompletionService<IdentifierBundle>(viewProcessingContext.getExecutorService());
    
    boolean failed = false;
    for(IdentifierBundle secKey : securityKeys) {
      if(secKey == null) {
        failed = true;
        s_logger.warn("Had null security key in at least one position");
      } else {
        completionService.submit(new SecurityResolutionJob(viewProcessingContext, secKey), secKey);
      }
    }
    for(int i = 0; i < securityKeys.size(); i++) {
      Future<IdentifierBundle> future = null;
      try {
        future = completionService.take();
      } catch (InterruptedException e1) {
        Thread.interrupted();
        s_logger.warn("Interrupted, so didn't finish resolution.");
        failed = true;
        break;
      }
      try {
        future.get();
      } catch (Exception e) {
        s_logger.warn("Got exception resolving securities", e);
        failed = true;
      }
    }
    if (failed) {
      throw new OpenGammaRuntimeException("Unable to resolve all securities for Portfolio " + getPortfolio().getName());
    }
    timer.finished();
  }
  
  protected class SecurityResolutionJob implements Runnable {
    private final ViewProcessingContext _viewProcessingContext;
    private final IdentifierBundle _securityKey;
    
    public SecurityResolutionJob(
        ViewProcessingContext viewProcessingContext,
        IdentifierBundle securityKey) {
      _viewProcessingContext = viewProcessingContext;
      _securityKey = securityKey;
    }
    
    @Override
    public void run() {
      Security security = null;
      try {
        security = _viewProcessingContext.getSecurityMaster().getSecurity(_securityKey);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Exception while resolving SecurityKey " + _securityKey, e);
      }
      if(security == null) {
        throw new OpenGammaRuntimeException("Unable to resolve security key " + _securityKey);
      } else {
        _securitiesByKey.put(_securityKey, security);
      }
    }
  }

  protected Set<IdentifierBundle> getSecurityKeysForResolution(PortfolioNode node) {
    Set<IdentifierBundle> result = new TreeSet<IdentifierBundle>();
    
    for(Position position : node.getPositions()) {
      if(position.getSecurity() != null) {
        // Nothing to do here; they pre-resolved the security.
      } else if(position.getSecurityKey() != null) {
        result.add(position.getSecurityKey());
      } else {
        throw new IllegalArgumentException("Security or security key must be provided: " + position.getUniqueIdentifier());
      }
    }
    
    for(PortfolioNode subNode : node.getChildNodes()) {
      result.addAll(getSecurityKeysForResolution(subNode));
    }
    
    return result;
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
    PortfolioNodeImpl populatedNode = new PortfolioNodeImpl();
    populatedNode.setUniqueIdentifier(node.getUniqueIdentifier());
    for(Position position : node.getPositions()) {
      Security security = position.getSecurity();
      if(position.getSecurity() == null) {
        security = _securitiesByKey.get(position.getSecurityKey());
      }
      if(security == null) {
        throw new OpenGammaRuntimeException("Unable to resolve security key " + position.getSecurityKey() + " for position " + position);
      }
      PositionBean populatedPosition = new PositionBean(
          position.getUniqueIdentifier(), position.getQuantity(), position.getSecurityKey(), security);  // we could just reuse the existing object?
      populatedNode.addPosition(populatedPosition);
    }
    for (PortfolioNode child : node.getChildNodes()) {
      populatedNode.addChildNode(getPopulatedPortfolioNode(child, secMaster));
    }
    return populatedNode;
  }
  
  public void loadPositions() {
    OperationTimer timer = new OperationTimer(s_logger, "Loading positions on {}", getPortfolio().getName());
    PortfolioNode populatedRootNode = getPopulatedRootNode();
    loadPositions(populatedRootNode);
    setPopulatedRootNode(populatedRootNode);
    timer.finished();
    s_logger.debug("Operating on {} positions", getPopulatedPositions().size());
  }
  
  protected void loadPositions(PortfolioNode node) {
    getPopulatedPositions().addAll(node.getPositions());
    for (PortfolioNode child : node.getChildNodes()) {
      loadPositions(child);
    }
  }
  
  public void loadSecurities() {
    // REVIEW kirk 2010-03-07 -- This is necessary because securities might have
    // been pre-resolved, so we can't just rely on the map from SecurityKey to Security
    // that we build up during resolution.
    for(Position position : getPopulatedPositions()) {
      getSecurities().add(position.getSecurity());
    }
  }
  
  public void buildDependencyGraphs(
      FunctionRepository functionRepository,
      FunctionResolver functionResolver,
      FunctionCompilationContext compilationContext,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider,
      ComputationTargetResolver computationTargetResolver,
      ViewDefinition viewDefinition) {
    OperationTimer timer = new OperationTimer(s_logger, "Building dependency graphs {}", getPortfolio().getName());
    // REVIEW kirk 2010-03-29 -- Much like the inner loop, the outer loop is chock-full
    // of potentially expensive operations for parallelism. In fact, perhaps more so
    // than the inner loop.
    for(Map.Entry<String, ViewCalculationConfiguration> entry : viewDefinition.getAllCalculationConfigurationsByName().entrySet()) {
      String configName = entry.getKey();
      ViewCalculationConfiguration calcConfig = entry.getValue();
      
      DependencyGraphModel dependencyGraphModel = new DependencyGraphModel();
      dependencyGraphModel.setFunctionRepository(functionRepository);
      dependencyGraphModel.setLiveDataAvailabilityProvider(liveDataAvailabilityProvider);
      dependencyGraphModel.setTargetResolver(computationTargetResolver);
      dependencyGraphModel.setFunctionResolver(functionResolver);
      dependencyGraphModel.setCompilationContext(compilationContext);
      dependencyGraphModel.setCalculationConfigurationName(configName);

      Map<String, Set<String>> outputsBySecurityType = calcConfig.getValueRequirementsBySecurityTypes();
      for(Position position : getPopulatedPositions()) {
        // REVIEW kirk 2009-09-04 -- This is potentially a VERY computationally expensive
        // operation. We could/should do them in parallel.
        Set<String> requiredOutputValues = outputsBySecurityType.get(position.getSecurity().getSecurityType());
        Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
        for(String requirementName : requiredOutputValues) {
          ValueRequirement requirement = new ValueRequirement(requirementName, position);
          requirements.add(requirement);
        }
        dependencyGraphModel.addTarget(new ComputationTarget(ComputationTargetType.POSITION, position), requirements);
      }
      PortfolioNodeCompiler compiler = new PortfolioNodeCompiler(dependencyGraphModel, calcConfig);
      new PortfolioNodeTraverser(compiler).traverse(getPopulatedRootNode());
      
      dependencyGraphModel.removeUnnecessaryOutputs();
      
      _graphModelsByConfiguration.put(configName, dependencyGraphModel);
    }
    timer.finished();
  }
  
  public void addLiveDataSubscriptions(
      final ViewProcessingContext viewProcessingContext,
      final ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(viewProcessingContext, "view processing context");
    
    Set<ValueRequirement> liveDataRequirements = getAllLiveDataRequirements();
    
    OperationTimer timer = new OperationTimer(s_logger, "Adding {} live data subscriptions", liveDataRequirements.size());
    viewProcessingContext.getLiveDataSnapshotProvider().addSubscription(viewDefinition.getUserName(), liveDataRequirements);
    timer.finished();
  }
  
  public Set<ValueRequirement> getAllLiveDataRequirements() {
    Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for(DependencyGraphModel dependencyGraphModel : _graphModelsByConfiguration.values()) {
      Set<ValueRequirement> requiredLiveData = dependencyGraphModel.getAllRequiredLiveData();
      result.addAll(requiredLiveData);
    }
    return result;
  }
  
  protected static class SubNodeSecurityTypeAccumulator
  extends AbstractPortfolioNodeTraversalCallback {
    private final Set<String> _subNodeSecurityTypes = new TreeSet<String>();

    /**
     * @return the subNodeSecurityTypes
     */
    public Set<String> getSubNodeSecurityTypes() {
      return _subNodeSecurityTypes;
    }

    @Override
    public void preOrderOperation(Position position) {
      _subNodeSecurityTypes.add(position.getSecurity().getSecurityType());
    }
  }
  
  protected static class PortfolioNodeCompiler
  extends AbstractPortfolioNodeTraversalCallback {
    private final DependencyGraphModel _dependencyGraphModel;
    private final ViewCalculationConfiguration _calculationConfiguration;
    
    public PortfolioNodeCompiler(
        DependencyGraphModel dependencyGraphModel,
        ViewCalculationConfiguration calculationConfiguration) {
      _dependencyGraphModel = dependencyGraphModel;
      _calculationConfiguration = calculationConfiguration;
    }

    @Override
    public void postOrderOperation(PortfolioNode portfolioNode) {
      // Yes, we could in theory do this outside the loop by implementing more
      // callbacks, but it might have gotten hairy, so for the first pass I just
      // did it this way.
      Set<String> subNodeSecurityTypes = getSubNodeSecurityTypes(portfolioNode);
      Map<String, Set<String>> outputsBySecurityType = _calculationConfiguration.getValueRequirementsBySecurityTypes();
      for(String secType : subNodeSecurityTypes) {
        Set<String> requiredOutputs = outputsBySecurityType.get(secType);
        if((requiredOutputs == null) || requiredOutputs.isEmpty()) {
          continue;
        }
        Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
        for(String requiredOutput : requiredOutputs) {
          requirements.add(new ValueRequirement(requiredOutput, portfolioNode));
        }
        _dependencyGraphModel.addTarget(new ComputationTarget(ComputationTargetType.MULTIPLE_POSITIONS, portfolioNode), requirements);
      }
    }
        
  }

  /**
   * @param portfolioNode
   * @return
   */
  private static Set<String> getSubNodeSecurityTypes(PortfolioNode portfolioNode) {
    SubNodeSecurityTypeAccumulator accumulator = new SubNodeSecurityTypeAccumulator();
    new PortfolioNodeTraverser(accumulator).traverse(portfolioNode);
    return accumulator.getSubNodeSecurityTypes();
  }
  
}
