/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutput;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Portfolio tree traversal callback methods that construct value requirements for the specified portfolio's nodes, positions and trades (as per options specified in the result model definition). The
 * value requirements are added to the specified dependency graph builder, possibly triggering the background compilation of dependency graphs for each stage in a portfolio tree while this traversal
 * is still ongoing. The pre-order method for a portfolio node sets up an empty requirements container for that node, which is filled up as its children are traversed (if aggregation is specified in
 * the result model definition), and then added to the dependency graph's list of targets in the post-order method for that portfolio node.
 */
/* package */final class PortfolioCompilerTraversalCallback extends AbstractPortfolioNodeTraversalCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioCompilerTraversalCallback.class);

  private static final class NodeData {

    private final ComputationTargetSpecification _targetSpec;
    private final Set<Pair<String, ValueProperties>> _requirements = Sets.newHashSet();
    private final boolean _excluded;

    public NodeData(final PortfolioNode node, final boolean excluded) {
      _targetSpec = ComputationTargetSpecification.of(node);
      _excluded = excluded;
    }

    public synchronized void addRequirements(final Set<Pair<String, ValueProperties>> requirements) {
      _requirements.addAll(requirements);
    }

    public Set<Pair<String, ValueProperties>> getRequirements() {
      return _requirements;
    }

    public ComputationTargetSpecification getTargetSpecification() {
      return _targetSpec;
    }

    public boolean isExcluded() {
      return _excluded;
    }

  }

  private final Set<UniqueId> _includeEvents;
  private final Set<UniqueId> _excludeEvents;
  private Map<String, Set<Pair<String, ValueProperties>>> _portfolioRequirementsBySecurityType;
  private final List<MergedOutput> _mergedOutputs;
  private final Set<ValueRequirement> _alreadyAdded;
  private final DependencyGraphBuilder _builder;
  private final ConcurrentMap<ComputationTargetReference, UniqueId> _resolutions;
  private final boolean _outputAggregates;
  private final boolean _outputPositions;
  private final boolean _outputTrades;

  /**
   * This map persists gathered information for each portfolio node and position across multiple traversal steps, thus allowing child nodes/positions to insert aggregate requirements into their parent
   * node.
   */
  private final ConcurrentMap<UniqueId, NodeData> _nodeData = new ConcurrentHashMap<UniqueId, NodeData>();

  public PortfolioCompilerTraversalCallback(final ViewCalculationConfiguration calculationConfiguration, final DependencyGraphBuilder builder,
      final Set<ValueRequirement> alreadyAdded, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions, final Set<UniqueId> includeEvents,
      final Set<UniqueId> excludeEvents) {
    _portfolioRequirementsBySecurityType = calculationConfiguration.getPortfolioRequirementsBySecurityType();
    _mergedOutputs = calculationConfiguration.getMergedOutputs();
    final ResultModelDefinition resultModelDefinition = calculationConfiguration.getViewDefinition().getResultModelDefinition();
    _outputAggregates = resultModelDefinition.getAggregatePositionOutputMode() != ResultOutputMode.NONE;
    _outputPositions = resultModelDefinition.getPositionOutputMode() != ResultOutputMode.NONE;
    _outputTrades = resultModelDefinition.getTradeOutputMode() != ResultOutputMode.NONE;
    _builder = builder;
    _alreadyAdded = alreadyAdded;
    _resolutions = resolutions;
    _includeEvents = includeEvents;
    _excludeEvents = excludeEvents;
  }

  public Map<String, Set<Pair<String, ValueProperties>>> getPortfolioRequirementsBySecurityType() {
    return _portfolioRequirementsBySecurityType;
  }

  public void setPortfolioRequirementsBySecurityType(Map<String, Set<Pair<String, ValueProperties>>> portfolioRequirementsBySecurityType) {
    _portfolioRequirementsBySecurityType = portfolioRequirementsBySecurityType;
  }

  public void reset() {
    _nodeData.clear();
  }

  /**
   * Add the specified value requirement to the dep graph builder, triggering graph building by background threads.
   * <p>
   * If supplied, the {@link #_alreadyAdded} set member is used to identify anything that has already been added from the specific requirements of a view or as part of invalidating a previous graph.
   * See the notes in {@link DependencyGraphBuilder} for the hazards of requesting the same value requirement multiple times.
   * 
   * @param valueRequirement the value requirement to add
   */
  protected void addValueRequirement(final ValueRequirement valueRequirement) {
    if ((_alreadyAdded == null) || !_alreadyAdded.contains(valueRequirement)) {
      _builder.addTarget(valueRequirement);
    } else {
      s_logger.debug("Suppressing {} from the incremental requirement set", valueRequirement);
    }
  }

  /**
   * Store details of the security link in the resolution cache. The link is assumed to be a record of the link to the object, for example is it held by strong (object id) or weak (external id)
   * reference.
   * <p>
   * Securities are already resolved when the functions see the positions, so the logging target resolver will not capture any uses of the security.
   * 
   * @param link the link to store - the identifier is taken from this along with the resolved unique identifier
   */
  private void store(final SecurityLink link) {
    final ComputationTargetReference key;
    final UniqueId uid;
    if (link.getTarget() != null) {
      uid = link.getTarget().getUniqueId();
      if (link.getObjectId() != null) {
        key = new ComputationTargetSpecification(ComputationTargetType.SECURITY, uid.toLatest());
      } else if (!link.getExternalId().isEmpty()) {
        key = new ComputationTargetRequirement(ComputationTargetType.SECURITY, link.getExternalId());
      } else {
        return;
      }
      if (uid == null) {
        throw new IllegalArgumentException("Provided a SecurityLink " + link + " where the UniqueId could not be identified. Error in underlying Source/Master.");
      }
      final UniqueId existing = _resolutions.putIfAbsent(MemoryUtils.instance(key), uid);
      assert (existing == null) || existing.equals(uid);
    }
  }

  /**
   * Store details of the position lookup in the resolution cache. Positions are referenced from portfolio nodes by object identifier.
   * 
   * @param position the position to store
   */
  private void store(final Position position) {
    _resolutions.putIfAbsent(MemoryUtils.instance(new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getUniqueId().toLatest())), position.getUniqueId());
  }

  /**
   * The pre-order operation for a portfolio node, which adds the aggregate value requirements for the current portfolio node to the graph builder's set of value requirements.
   * 
   * @param node the portfolio node being traversed
   */
  @Override
  public void preOrderOperation(final PortfolioNode node) {
    // If a sub-set of nodes is to be considered, fail/return quickly
    boolean nodeExcluded = false;
    if (_excludeEvents != null) {
      if (_excludeEvents.contains(node.getUniqueId())) {
        if ((node.getParentNodeId() != null) && (_nodeData.get(node.getParentNodeId()) != null)) {
          nodeExcluded = true;
        } else {
          return;
        }
      }
    }
    // Initialise an empty set of requirements for the current portfolio node
    // This will be filled in as the traversal of this portfolio node's children proceeds, and retrieved during
    // this portfolio node's post-order traversal.
    final NodeData nodeData = new NodeData(node, nodeExcluded);
    _nodeData.put(node.getUniqueId(), nodeData);
    if (_outputAggregates && !nodeExcluded) {
      // Retrieve the required aggregate outputs (by 'aggregate' sec type) for the current calc configuration
      final Set<Pair<String, ValueProperties>> requiredOutputs =
          _portfolioRequirementsBySecurityType.get(ViewCalculationConfiguration.SECURITY_TYPE_AGGREGATE_ONLY);
      if ((requiredOutputs != null) && !requiredOutputs.isEmpty()) {
        // Add the aggregate value requirements for the current portfolio node to the graph builder's set of value requirements,
        // building them using the retrieved required aggregate outputs and the newly created computation target spec
        // for this portfolio node.
        final ComputationTargetSpecification targetSpec = nodeData.getTargetSpecification();
        for (final Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
          addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), targetSpec, requiredOutput.getSecond()));
        }
      }
    }
  }

  /**
   * The pre-order operation for a position in a portfolio. which adds the value requirements for the current position and/or its trades to the graph builder's set of value requirements (if the result
   * model specifies it), and also adds aggregate value requirements to the parent's requirements (again, if the result model specifies it) to be reaped post-order.
   * 
   * @param position the position being traversed
   */
  @Override
  public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
    // If a sub-set of positions is to be considered, fail/return quickly
    boolean positionExcluded = false;
    NodeData nodeData = null;
    if (_includeEvents != null) {
      if (!_includeEvents.contains(position.getUniqueId())) {
        return;
      }
    } else if (_excludeEvents != null) {
      nodeData = _nodeData.get(parentNode.getUniqueId());
      if (nodeData == null) {
        // Node is wholly excluded if it has no entry in the map
        return;
      }
      positionExcluded = nodeData.isExcluded();
    }
    // Get this position's security or return immediately if not available
    final Security security = position.getSecurity();
    if (security == null) {
      return;
    }
    if (!positionExcluded) {
      store(position);
      store(position.getSecurityLink());
    }
    // Identify this position's security type
    final String securityType = security.getSecurityType();
    Set<Pair<String, ValueProperties>> requiredOutputs;
    // Are we interested in producing results for positions?
    if (_outputPositions || _outputAggregates) {
      // Get all known required outputs for this security type in the current calculation configuration
      requiredOutputs = _portfolioRequirementsBySecurityType.get(securityType);
      // Check that there's at least one required output to deal with
      if ((requiredOutputs != null) && !requiredOutputs.isEmpty()) {
        if (nodeData == null) {
          nodeData = _nodeData.get(parentNode.getUniqueId());
        }
        // Are we interested in aggregate results for the parent? If so, pass on requirements to parent portfolio node
        if (_outputAggregates) {
          nodeData.addRequirements(requiredOutputs);
        }
        // Are we interested in any results at all for this position?
        if (_outputPositions && !positionExcluded) {
          final ComputationTargetSpecification positionSpec = nodeData.getTargetSpecification().containing(ComputationTargetType.POSITION, position.getUniqueId().toLatest());
          // Add the value requirements for the current position to the graph builder's set of value requirements,
          // building them using the retrieved required outputs for this security type and the newly created computation
          // target spec for this position.
          for (final Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
            addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), positionSpec, requiredOutput.getSecond()));
          }
        }
      }
      for (MergedOutput mergedOutput : _mergedOutputs) {
        if (nodeData == null) {
          nodeData = _nodeData.get(parentNode.getUniqueId());
        }
        final ValueProperties constraints = ValueProperties.with(ValuePropertyNames.NAME, mergedOutput.getMergedOutputName()).get();
        if (_outputAggregates) {
          nodeData.addRequirements(ImmutableSet.of(Pairs.of(ValueRequirementNames.MERGED_OUTPUT, constraints)));
        }
        if (_outputPositions && !positionExcluded) {
          final ComputationTargetSpecification positionSpec = nodeData.getTargetSpecification().containing(ComputationTargetType.POSITION, position.getUniqueId().toLatest());
          addValueRequirement(new ValueRequirement(ValueRequirementNames.MERGED_OUTPUT, positionSpec, constraints));
        }
      }
    }
    if (_outputTrades && !positionExcluded) {
      final Collection<Trade> trades = position.getTrades();
      if (!trades.isEmpty()) {
        requiredOutputs = _portfolioRequirementsBySecurityType.get(securityType);
        // Check that there's at least one required output to deal with
        if ((requiredOutputs != null) && !requiredOutputs.isEmpty()) {
          // Add value requirements for each trade
          for (final Trade trade : trades) {
            // TODO: [PLAT-2286] Scope the trade underneath it's parent portfolio node and position
            final ComputationTargetSpecification tradeSpec = ComputationTargetSpecification.of(trade);
            // Add the value requirements for the current trade to the graph builder's set of value requirements,
            // building them using the retrieved required outputs icw trades for this security type and the newly
            // created computation target spec for this trade.
            for (final Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
              addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), tradeSpec, requiredOutput.getSecond()));
            }
          }
        }
        for (MergedOutput mergedOutput : _mergedOutputs) {
          for (final Trade trade : trades) {
            final ValueProperties constraints = ValueProperties.with(ValuePropertyNames.NAME, mergedOutput.getMergedOutputName()).get();
            final ComputationTargetSpecification tradeSpec = ComputationTargetSpecification.of(trade);
            addValueRequirement(new ValueRequirement(ValueRequirementNames.MERGED_OUTPUT, tradeSpec, constraints));
          }
        }
        for (final Trade trade : trades) {
          store(trade.getSecurityLink());
        }
      }
    }
  }

  /**
   * The post-order operation for a portfolio node, which adds the value requirements gathered while traversing this portfolio node's children to the graph builder's set of value requirements. This
   * portfolio node's requirements are also passed up into its own parent node's requirments.
   * 
   * @param node the portfolio node being traversed
   */
  @Override
  public void postOrderOperation(final PortfolioNode node) {
    // Retrieve this portfolio node's value requirements (gathered during traversal of this portfolio node's children)
    final NodeData nodeData = _nodeData.remove(node.getUniqueId());
    if (nodeData == null) {
      // Totally excluded
      return;
    }
    final Set<Pair<String, ValueProperties>> nodeRequirements = nodeData.getRequirements();
    if (node.getParentNodeId() != null) {
      // Retrieve the parent portfolio node's requirements
      final NodeData parentNodeData = _nodeData.get(node.getParentNodeId());
      parentNodeData.addRequirements(nodeRequirements);
    }
    if (!nodeData.isExcluded()) {
      final ComputationTargetSpecification targetSpec = nodeData.getTargetSpecification();
      // Add the value requirements for the current portfolio node to the graph builder's set of value requirements,
      // building them using the requirements gathered during its children's traversal and the newly created computation
      // target spec for this portfolio node.
      for (final Pair<String, ValueProperties> requiredOutput : nodeRequirements) {
        addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), targetSpec, requiredOutput.getSecond()));
      }
    }
  }

}
