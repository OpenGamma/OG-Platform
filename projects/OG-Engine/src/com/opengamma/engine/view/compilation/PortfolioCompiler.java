/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Compiles Portfolio requirements into the dependency graphs.
 */
public final class PortfolioCompiler {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioCompiler.class);

  private PortfolioCompiler() {
  }

  // --------------------------------------------------------------------------
  /**
   * Adds portfolio targets to the dependency graphs as required, and fully resolves the portfolio structure.
   * 
   * @param compilationContext  the context of the view definition compilation
   * @param forcePortfolioResolution  {@code true} if there are external portfolio targets, {@code false} otherwise
   * @return the fully-resolved portfolio structure if any portfolio targets were required, {@code null}
   *         otherwise.
   */
  protected static Portfolio execute(ViewCompilationContext compilationContext, boolean forcePortfolioResolution) {
    // Everything we do here is geared towards the avoidance of resolution (of portfolios, positions, securities)
    // wherever possible, to prevent needless dependencies (on a position master, security master) when a view never
    // really has them.

    if (!isPortfolioOutputEnabled(compilationContext.getViewDefinition())) {
      // Doesn't even matter if the portfolio can't be resolved - we're not outputting anything at the portfolio level
      // (which might be because the user knows the portfolio can't be resolved right now) so there are no portfolio
      // targets to add to the dependency graph.
      return null;
    }
     
    Portfolio portfolio = forcePortfolioResolution ? getPortfolio(compilationContext) : null;

    for (ViewCalculationConfiguration calcConfig : compilationContext.getViewDefinition().getAllCalculationConfigurations()) {
      if (calcConfig.getAllPortfolioRequirements().size() == 0) {
        // No portfolio requirements for this calculation configuration - avoid further processing.
        continue;
      }

      // Actually need the portfolio now
      if (portfolio == null) {
        portfolio = getPortfolio(compilationContext);
      }
      
      DependencyGraphBuilder builder = compilationContext.getBuilders().get(calcConfig.getName());

      // Cache PortfolioNode, Trade and Position entities
      compilationContext.getServices().getComputationTargetResolver().cachePortfolioNodeHierarchy(portfolio.getRootNode());
      cacheTradesAndPositions(compilationContext.getServices().getComputationTargetResolver(), portfolio.getRootNode());

      // Add portfolio requirements to the dependency graph
      PortfolioCompilerTraversalCallback traversalCallback = new PortfolioCompilerTraversalCallback(builder, calcConfig);
      PortfolioNodeTraverser.depthFirst(traversalCallback).traverse(portfolio.getRootNode());
    }
    
    return portfolio;
  }

  private static void cacheTradesAndPositions(final CachingComputationTargetResolver resolver, final PortfolioNode node) {
    final Collection<Position> positions = node.getPositions();
    resolver.cachePositions(positions);
    for (Position position : positions) {
      resolver.cacheTrades(position.getTrades());
    }
    for (PortfolioNode child : node.getChildNodes()) {
      cacheTradesAndPositions(resolver, child);
    }
  }

  // --------------------------------------------------------------------------
  
  /**
   * Tests whether the view has portfolio outputs enabled.
   * 
   * @param viewDefinition the view definition
   * @return {@code true} if there is at least one portfolio target, {@code false} otherwise
   */
  private static boolean isPortfolioOutputEnabled(ViewDefinition viewDefinition) {
    ResultModelDefinition resultModelDefinition = viewDefinition.getResultModelDefinition();
    return resultModelDefinition.getPositionOutputMode() != ResultOutputMode.NONE || resultModelDefinition.getAggregatePositionOutputMode() != ResultOutputMode.NONE;
  }

  /**
   * Fully resolves the portfolio structure for a view. A fully resolved structure has resolved
   * {@link Security} objects for each {@link Position} within the portfolio. Note however that
   * any underlying or related data referenced by a security will not be resolved at this stage. 
   * 
   * @param compilationContext the compilation context containing the view being compiled
   */
  private static Portfolio getPortfolio(ViewCompilationContext compilationContext) {
    UniqueIdentifier portfolioId = compilationContext.getViewDefinition().getPortfolioId();
    if (portfolioId == null) {
      throw new OpenGammaRuntimeException("The view definition '" + compilationContext.getViewDefinition().getName() + "' contains required portfolio outputs, but it does not reference a portfolio.");
    }
    PositionSource positionSource = compilationContext.getServices().getPositionSource();
    if (positionSource == null) {
      throw new OpenGammaRuntimeException("The view definition '" + compilationContext.getViewDefinition().getName()
          + "' contains required portfolio outputs, but the compiler does not have access to a position source.");
    }
    Portfolio portfolio = positionSource.getPortfolio(portfolioId);
    if (portfolio == null) {
      throw new OpenGammaRuntimeException("Unable to resolve portfolio '" + portfolioId + "' in position source '" + positionSource + "' used by view definition '"
          + compilationContext.getViewDefinition().getName() + "'");
    }

    Map<IdentifierBundle, Security> securitiesByKey = resolveSecurities(portfolio, compilationContext);
    return createFullyResolvedPortfolio(portfolio, securitiesByKey);
  }

  public static Portfolio resolvePortfolio(final Portfolio portfolio, final ExecutorService executorService, final SecuritySource securitySource) {
    final Set<IdentifierBundle> securityKeys = getSecurityKeysForResolution(portfolio.getRootNode());
    final Map<IdentifierBundle, Security> securitiesByKey = SecurityResolver.resolveSecurities(securityKeys, executorService, securitySource);
    return createFullyResolvedPortfolio(portfolio, securitiesByKey);
  }

  /**
   * Resolves all of the securities for all positions within the portfolio.
   * 
   * @param portfolio the portfolio to resolve
   * @param viewCompilationContext the compilation context containing the view being compiled
   */
  private static Map<IdentifierBundle, Security> resolveSecurities(Portfolio portfolio, ViewCompilationContext viewCompilationContext) {
    OperationTimer timer = new OperationTimer(s_logger, "Resolving all securities for {}", portfolio.getName());
    
    // First retrieve all of the security keys referenced within the portfolio, then resolve them all as a single step
    Set<IdentifierBundle> securityKeys = getSecurityKeysForResolution(portfolio.getRootNode());
    Map<IdentifierBundle, Security> securitiesByKey;
    try {
      securitiesByKey = SecurityResolver.resolveSecurities(securityKeys, viewCompilationContext);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to resolve all securities for portfolio " + portfolio.getName());
    } finally {
      timer.finished();
    }

    // While we've got the resolved securities to hand, we might as well cache them since they are all computation
    // targets that will be needed later
    CachingComputationTargetResolver resolver = viewCompilationContext.getServices().getComputationTargetResolver();
    resolver.cacheSecurities(securitiesByKey.values());

    return securitiesByKey;
  }

  /**
   * Walks the portfolio structure collecting all of the security identifiers referenced by the position nodes.
   * 
   * @param node a portfolio node to process
   * @return the set of security identifiers for any positions under the given portfolio node 
   */
  private static Set<IdentifierBundle> getSecurityKeysForResolution(PortfolioNode node) {
    Set<IdentifierBundle> result = new TreeSet<IdentifierBundle>();

    for (Position position : node.getPositions()) {
      if (position.getSecurity() != null) {
        // Nothing to do here; they pre-resolved the security.
        s_logger.debug("Security pre-resolved by PositionSource for {}", position.getUniqueId());
      } else if (position.getSecurityKey() != null) {
        result.add(position.getSecurityKey());
      } else {
        throw new IllegalArgumentException("Security or security key must be provided: " + position.getUniqueId());
      }
      
      //get trades security identifiers as well
      for (Trade trade : position.getTrades()) {
        if (trade.getSecurity() != null) {
          // Nothing to do here; they pre-resolved the security.
          s_logger.debug("Security pre-resolved by PositionSource for {}", trade.getUniqueId());
        } else if (trade.getSecurityKey() != null) {
          result.add(trade.getSecurityKey());
        } else {
          throw new IllegalArgumentException("Security or security key must be provided: " + trade.getUniqueId());
        }
      }
    }

    for (PortfolioNode subNode : node.getChildNodes()) {
      result.addAll(getSecurityKeysForResolution(subNode));
    }

    return result;
  }

  /**
   * Constructs a new {@link Portfolio} instance containing resolved positions that reference {@link Security} instances.
   * 
   * @param portfolio the unresolved portfolio to copy
   * @param securitiesByKey the resolved securities to use
   */
  private static Portfolio createFullyResolvedPortfolio(Portfolio portfolio, Map<IdentifierBundle, Security> securitiesByKey) {
    return new PortfolioImpl(portfolio.getUniqueId(), portfolio.getName(), createFullyResolvedPortfolioHierarchy(portfolio.getRootNode(), securitiesByKey));
  }

  /**
   * Constructs a copy a {@link PortfolioNode}, and the hierarchy underneath it, that contains fully resolved positions.
   * 
   * @param rootNode the unresolved portfolio hierarchy node to copy
   * @param securitiesByKey the resolved securities to use
   */
  private static PortfolioNodeImpl createFullyResolvedPortfolioHierarchy(PortfolioNode rootNode, Map<IdentifierBundle, Security> securitiesByKey) {
    if (rootNode == null) {
      return null;
    }
    PortfolioNodeImpl populatedNode = new PortfolioNodeImpl(rootNode.getName());
    if (rootNode.getUniqueId() != null) {
      populatedNode.setUniqueId(rootNode.getUniqueId());
    }
    // Take copies of any positions directly under this node, adding the resolved security instances. 
    for (Position position : rootNode.getPositions()) {
      Security security = position.getSecurity();
      if (position.getSecurity() == null) {
        security = securitiesByKey.get(position.getSecurityKey());
      }
      if (security == null) {
        throw new OpenGammaRuntimeException("Unable to resolve security key " + position.getSecurityKey() + " for position " + position);
      }
      PositionImpl populatedPosition = new PositionImpl(position);
      populatedPosition.setSecurity(security);
      populatedPosition.setParentNodeId(populatedNode.getUniqueId());
      // set the children trade security as well
      final Set<Trade> origTrades = populatedPosition.getTrades();
      if (!origTrades.isEmpty()) {
        final Set<Trade> newTrades = Sets.newHashSetWithExpectedSize(origTrades.size());
        for (Trade trade : origTrades) {
          TradeImpl populatedTrade = new TradeImpl(trade);
          populatedTrade.setParentPositionId(populatedPosition.getUniqueId());
          populatedTrade.setSecurity(security);
          newTrades.add(populatedTrade);
        }
        populatedPosition.setTrades(newTrades);
      }
      populatedNode.addPosition(populatedPosition);
    }
    // Add resolved copies of any nodes directly underneath this node
    for (PortfolioNode child : rootNode.getChildNodes()) {
      final PortfolioNodeImpl childNode = createFullyResolvedPortfolioHierarchy(child, securitiesByKey);
      childNode.setParentNodeId(populatedNode.getUniqueId());
      populatedNode.addChildNode(childNode);
    }
    return populatedNode;
  }

}
