/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import com.opengamma.engine.ForwardingComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFormatter;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionResolver;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.position.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.PortfolioNodeTraverser;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
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
 */
public class PortfolioEvaluationModel {
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioEvaluationModel.class);
  private static final boolean OUTPUT_DEPENDENCY_GRAPHS = false;
  private static final boolean OUTPUT_LIVE_DATA_REQUIREMENTS = false;

  private Portfolio _portfolio;
  
  // REVIEW kirk 2010-03-29 -- Use a sorted map here?
  private final Map<String, DependencyGraph> _graphsByConfiguration =
    new ConcurrentHashMap<String, DependencyGraph>();
  private final Map<IdentifierBundle, Security> _securitiesByKey = new ConcurrentHashMap<IdentifierBundle, Security>();
  // REVIEW kirk 2009-09-14 -- HashSet is almost certainly the wrong set here.
  private final Set<Position> _populatedPositions = new HashSet<Position>();
  private final Set<Security> _securities = new HashSet<Security>();
  private final Set<ValueRequirement> _liveDataRequirements = new HashSet<ValueRequirement>();
  
  public PortfolioEvaluationModel(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "Portfolio");
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
  public void setPopulatedRootNode(PortfolioNodeImpl populatedRootNode) {
    _portfolio = new PortfolioImpl(_portfolio.getUniqueIdentifier(), _portfolio.getName(), populatedRootNode);
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
    return new TreeSet<String>(_graphsByConfiguration.keySet());
  }
  
  public Set<String> getAllOutputValueNames() {
    Set<String> valueNames = new HashSet<String>(); 
    for (DependencyGraph graph : getAllDependencyGraphs()) {
      for (ValueSpecification spec : graph.getOutputValues()) {
        valueNames.add(spec.getRequirementSpecification().getValueName());        
      }
    }
    return valueNames;
  }
  
  public Collection<DependencyGraph> getAllDependencyGraphs() {
    return new ArrayList<DependencyGraph>(_graphsByConfiguration.values());
  }
  
  public DependencyGraph getDependencyGraph(String calcConfigName) {
    return _graphsByConfiguration.get(calcConfigName);
  }
  
  private class ResolvedSecurityComputationTargetResolver extends ForwardingComputationTargetResolver {
    
    private final Map<UniqueIdentifier, Position> _positionsByUID = new HashMap<UniqueIdentifier, Position>();
    private final Map<UniqueIdentifier, Security> _securitiesByUID = new HashMap<UniqueIdentifier, Security>();
    private final Map<UniqueIdentifier, PortfolioNode> _portfolioNodeByUID = new HashMap<UniqueIdentifier, PortfolioNode>();
    
    public ResolvedSecurityComputationTargetResolver(final ComputationTargetResolver defaultResolver) {
      super(defaultResolver);
      for (final Position position : getPopulatedPositions()) {
        _positionsByUID.put(position.getUniqueIdentifier(), position);
      }
      for (final Security security : getSecurities()) {
        _securitiesByUID.put(security.getUniqueIdentifier(), security);
      }
      populatePortfolioNodeByUID(getPortfolio().getRootNode());
    }
    
    private void populatePortfolioNodeByUID(final PortfolioNode portfolioNode) {
      _portfolioNodeByUID.put(portfolioNode.getUniqueIdentifier(), portfolioNode);
      for (final PortfolioNode child : portfolioNode.getChildNodes()) {
        populatePortfolioNodeByUID(child);
      }
    }

    @Override
    public ComputationTarget resolve(ComputationTargetSpecification specification) {
      UniqueIdentifier uid = specification.getUniqueIdentifier();
      switch (specification.getType()) {
        case SECURITY: {
          Security security = _securitiesByUID.get(uid);
          s_logger.debug("Security ID {} requested, pre-resolved to {}", uid, security);
          if (security == null) {
            break;
          }
          return new ComputationTarget(ComputationTargetType.SECURITY, security);
        }
        case POSITION: {
          Position position = _positionsByUID.get(uid);
          s_logger.debug("Position ID {} requested, pre-resolved to {}", uid, position);
          if (position == null) {
            break;
          }
          return new ComputationTarget(ComputationTargetType.POSITION, position);
        }
        case PORTFOLIO_NODE : {
          PortfolioNode portfolioNode = _portfolioNodeByUID.get(uid);
          s_logger.debug("PortfolioNode ID {} requested, pre-resolved to {}", uid, portfolioNode);
          if (portfolioNode == null) {
            break;
          }
          return new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, portfolioNode);
        }
      }
      return super.resolve(specification);
    }
    
  }

  public void init(
      ViewCompilationServices viewCompilationServices,
      ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(viewCompilationServices, "View Compilation Services");
    ArgumentChecker.notNull(viewDefinition, "View Definition");
    
    // Resolve all of the securities
    resolveSecurities(viewCompilationServices);
    
    PortfolioNodeImpl populatedRootNode = getPopulatedPortfolioNode(getPortfolio().getRootNode());
    assert populatedRootNode != null;
    setPopulatedRootNode(populatedRootNode);
    
    loadPositions();
    loadSecurities();
    buildDependencyGraphs(
        viewCompilationServices.getFunctionResolver(),
        viewCompilationServices.getCompilationContext(),
        viewCompilationServices.getLiveDataAvailabilityProvider(),
        new ResolvedSecurityComputationTargetResolver(viewCompilationServices.getComputationTargetResolver()),
        viewDefinition);
    if (OUTPUT_DEPENDENCY_GRAPHS) {
      outputDependencyGraphs();
    }
    if (OUTPUT_LIVE_DATA_REQUIREMENTS) {
      outputLiveDataRequirements(viewCompilationServices.getSecuritySource());
    }
    refreshLiveDataRequirements();
  }
  
  private void outputDependencyGraphs() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, DependencyGraph> entry : _graphsByConfiguration.entrySet()) {
      String configName = entry.getKey();
      sb.append("DepGraph for ").append(configName);
      
      DependencyGraph depGraph = entry.getValue();
      sb.append("\tProducing values ").append(depGraph.getOutputValues());
      for (DependencyNode depNode : depGraph.getDependencyNodes()) {
        sb.append("\t\tNode:\n").append(DependencyNodeFormatter.toString(depNode));
      }
    }
    s_logger.warn("Dependency Graphs -- \n{}", sb);
  }
  
  private void outputLiveDataRequirements(SecuritySource secMaster) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, DependencyGraph> entry : _graphsByConfiguration.entrySet()) {
      String configName = entry.getKey();
      Collection<ValueRequirement> requiredLiveData = entry.getValue().getAllRequiredLiveData();
      if (requiredLiveData.isEmpty()) {
        sb.append(configName).append(" requires no live data.\n");
      } else {
        sb.append("Live data for ").append(configName).append("\n");
        for (ValueRequirement liveRequirement : requiredLiveData) {
          sb.append("\t").append(liveRequirement.getTargetSpecification().getRequiredLiveData(secMaster)).append("\n");
        }
      }
    }
    s_logger.warn("Live data requirements -- \n{}", sb);
  }
  
  protected void resolveSecurities(final ViewCompilationServices viewCompilationServices) {
    // TODO kirk 2010-03-07 -- Need to switch to OperationTimer for this.
    OperationTimer timer = new OperationTimer(s_logger, "Resolving all securities for {}", getPortfolio().getName());
    Set<IdentifierBundle> securityKeys = getSecurityKeysForResolution(getPortfolio().getRootNode());
    ExecutorCompletionService<IdentifierBundle> completionService = new ExecutorCompletionService<IdentifierBundle>(viewCompilationServices.getExecutorService());
    
    boolean failed = false;
    for (IdentifierBundle secKey : securityKeys) {
      if (secKey == null) {
        failed = true;
        s_logger.warn("Had null security key in at least one position");
      } else {
        completionService.submit(new SecurityResolutionJob(viewCompilationServices.getSecuritySource(), secKey), secKey);
      }
    }
    for (int i = 0; i < securityKeys.size(); i++) {
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
  
  /**
   * A small job that can be run in an executor to resolve a security against
   * a {@link SecuritySource}.
   */
  protected class SecurityResolutionJob implements Runnable {
    private final SecuritySource _securitySource;
    private final IdentifierBundle _securityKey;
    
    public SecurityResolutionJob(SecuritySource securitySource, IdentifierBundle securityKey) {
      _securitySource = securitySource;
      _securityKey = securityKey;
    }
    
    @Override
    public void run() {
      Security security = null;
      try {
        security = _securitySource.getSecurity(_securityKey);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Exception while resolving SecurityKey " + _securityKey, e);
      }
      if (security == null) {
        throw new OpenGammaRuntimeException("Unable to resolve security key " + _securityKey);
      } else {
        _securitiesByKey.put(_securityKey, security);
      }
    }
  }

  protected Set<IdentifierBundle> getSecurityKeysForResolution(PortfolioNode node) {
    Set<IdentifierBundle> result = new TreeSet<IdentifierBundle>();
    
    for (Position position : node.getPositions()) {
      if (position.getSecurity() != null) {
        // Nothing to do here; they pre-resolved the security.
        s_logger.debug("Security pre-resolved by PositionSource for {}", position.getUniqueIdentifier());
      } else if (position.getSecurityKey() != null) {
        result.add(position.getSecurityKey());
      } else {
        throw new IllegalArgumentException("Security or security key must be provided: " + position.getUniqueIdentifier());
      }
    }
    
    for (PortfolioNode subNode : node.getChildNodes()) {
      result.addAll(getSecurityKeysForResolution(subNode));
    }
    
    return result;
  }

  protected PortfolioNodeImpl getPopulatedPortfolioNode(
      PortfolioNode node) {
    if (node == null) {
      return null;
    }
    PortfolioNodeImpl populatedNode = new PortfolioNodeImpl(node.getUniqueIdentifier(), node.getName());
    for (Position position : node.getPositions()) {
      Security security = position.getSecurity();
      if (position.getSecurity() == null) {
        security = _securitiesByKey.get(position.getSecurityKey());
      }
      if (security == null) {
        throw new OpenGammaRuntimeException("Unable to resolve security key " + position.getSecurityKey() + " for position " + position);
      }
      PositionImpl populatedPosition = new PositionImpl(position.getUniqueIdentifier(), position.getQuantity(), position.getSecurityKey(), security);
      populatedNode.addPosition(populatedPosition);
    }
    for (PortfolioNode child : node.getChildNodes()) {
      populatedNode.addChildNode(getPopulatedPortfolioNode(child));
    }
    return populatedNode;
  }
  
  public void loadPositions() {
    OperationTimer timer = new OperationTimer(s_logger, "Loading positions on {}", getPortfolio().getName());
    PortfolioNode populatedRootNode = getPortfolio().getRootNode();
    loadPositions(populatedRootNode);
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
    for (Position position : getPopulatedPositions()) {
      getSecurities().add(position.getSecurity());
    }
  }
  
  public void buildDependencyGraphs(
      FunctionResolver functionResolver,
      FunctionCompilationContext compilationContext,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider,
      ComputationTargetResolver computationTargetResolver,
      ViewDefinition viewDefinition) {
    OperationTimer timer = new OperationTimer(s_logger, "Building dependency graphs {}", getPortfolio().getName());
    // REVIEW kirk 2010-03-29 -- Much like the inner loop, the outer loop is chock-full
    // of potentially expensive operations for parallelism. In fact, perhaps more so
    // than the inner loop.
    for (Map.Entry<String, ViewCalculationConfiguration> entry : viewDefinition.getAllCalculationConfigurationsByName().entrySet()) {
      String configName = entry.getKey();
      ViewCalculationConfiguration calcConfig = entry.getValue();
      
      DependencyGraphBuilder dependencyGraphBuilder = new DependencyGraphBuilder();
      dependencyGraphBuilder.setLiveDataAvailabilityProvider(liveDataAvailabilityProvider);
      dependencyGraphBuilder.setTargetResolver(computationTargetResolver);
      dependencyGraphBuilder.setFunctionResolver(functionResolver);
      dependencyGraphBuilder.setCompilationContext(compilationContext);
      dependencyGraphBuilder.setCalculationConfigurationName(configName);

      PortfolioNodeCompiler compiler = new PortfolioNodeCompiler(dependencyGraphBuilder, calcConfig);
      new PortfolioNodeTraverser(compiler).traverse(getPortfolio().getRootNode());
      
      DependencyGraph depGraph = dependencyGraphBuilder.getDependencyGraph();
      depGraph.removeUnnecessaryValues();
      
      _graphsByConfiguration.put(configName, depGraph);
    }
    timer.finished();
  }
  
  public Set<ValueRequirement> getAllLiveDataRequirements() {
    return Collections.unmodifiableSet(_liveDataRequirements);    
  }
  
  public void refreshLiveDataRequirements() {
    for (DependencyGraph dependencyGraph : _graphsByConfiguration.values()) {
      Set<ValueRequirement> requiredLiveData = dependencyGraph.getAllRequiredLiveData();
      _liveDataRequirements.addAll(requiredLiveData);
    }
  }
  
  public Set<ComputationTargetSpecification> getAllComputationTargets() {
    Set<ComputationTargetSpecification> targets = new HashSet<ComputationTargetSpecification>();
    for (DependencyGraph dependencyGraph : _graphsByConfiguration.values()) {
      Set<ComputationTargetSpecification> requiredLiveData = dependencyGraph.getAllComputationTargets();
      targets.addAll(requiredLiveData);
    }
    return targets;
  }
  
  /**
   * Gathers all security types. 
   */
  protected static class SubNodeSecurityTypeAccumulator extends AbstractPortfolioNodeTraversalCallback {
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
  
  /**
   * Compiles dependency graphs for each stage in a portfolio tree.
   */
  protected static class PortfolioNodeCompiler extends AbstractPortfolioNodeTraversalCallback {
    private final DependencyGraphBuilder _dependencyGraphBuilder;
    private final ViewCalculationConfiguration _calculationConfiguration;
    
    public PortfolioNodeCompiler(
        DependencyGraphBuilder dependencyGraphBuilder,
        ViewCalculationConfiguration calculationConfiguration) {
      _dependencyGraphBuilder = dependencyGraphBuilder;
      _calculationConfiguration = calculationConfiguration;
    }

    @Override
    public void preOrderOperation(PortfolioNode portfolioNode) {
      // Yes, we could in theory do this outside the loop by implementing more
      // callbacks, but it might have gotten hairy, so for the first pass I just
      // did it this way.
      Set<String> subNodeSecurityTypes = getSubNodeSecurityTypes(portfolioNode);
      Map<String, Set<String>> outputsBySecurityType = _calculationConfiguration.getValueRequirementsBySecurityTypes();
      for (String secType : subNodeSecurityTypes) {
        Set<String> requiredOutputs = outputsBySecurityType.get(secType);
        if ((requiredOutputs == null) || requiredOutputs.isEmpty()) {
          continue;
        }
        Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
        ResultModelDefinition resultModelDefinition = _calculationConfiguration.getDefinition().getResultModelDefinition();
        // first do the portfolio node targets (aggregated, multiple-position nodes), if they're needed
        if (resultModelDefinition.isComputePortfolioNodeCalculations()) {
          for (String requiredOutput : requiredOutputs) {
            requirements.add(new ValueRequirement(requiredOutput, portfolioNode));
          }
          _dependencyGraphBuilder.addTarget(new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, portfolioNode), requirements);
        }
        // now do the position nodes targets, if they're needed
        if (resultModelDefinition.isComputePositionNodeCalculations()) {
          for (Position position : portfolioNode.getPositions()) {
            requirements.clear();
            for (String requiredOutput : requiredOutputs) {
              requirements.add(new ValueRequirement(requiredOutput, position));
            }
            _dependencyGraphBuilder.addTarget(new ComputationTarget(ComputationTargetType.POSITION, position), requirements);
          }
        }
        // now do the per-security targets, if they're needed
        if (resultModelDefinition.isComputeSecurityNodeCalculations()) {
          for (Position position : portfolioNode.getPositions()) {
            requirements.clear();
            for (String requiredOutput : requiredOutputs) {
              requirements.add(new ValueRequirement(requiredOutput, position.getSecurity()));
            }
            _dependencyGraphBuilder.addTarget(new ComputationTarget(ComputationTargetType.SECURITY, position.getSecurity()), requirements);
          }
        }
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
