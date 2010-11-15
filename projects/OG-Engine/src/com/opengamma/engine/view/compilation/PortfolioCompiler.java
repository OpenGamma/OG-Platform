/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.PortfolioNodeTraverser;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Compiles portfolio requirements into the dependency graphs.
 */
/* package */final class PortfolioCompiler {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioCompiler.class);

  private PortfolioCompiler() {
  }

  // --------------------------------------------------------------------------
  /**
   * Adds portfolio targets to the dependency graphs as required, and fully resolves the portfolio structure.
   * 
   * @param compilationContext  the context of the view definition compilation
   * @return the fully-resolved portfolio structure if any portfolio targets were required, <code>null</code>
   *         otherwise.
   */
  public static Portfolio execute(ViewCompilationContext compilationContext) {
    // Everything we do here is geared towards the avoidance of resolution (of portfolios, positions, securities)
    // wherever possible, to prevent needless dependencies (on a position master, security master) when a view never
    // really has them.

    if (!hasPortfolioOutput(compilationContext.getViewDefinition())) {
      // Doesn't even matter if the portfolio can't be resolved - we're not outputting anything at the portfolio level
      // (which might be because the user knows the portfolio can't be resolved right now) so there are no portfolio
      // targets to add to the dependency graph.
      return null;
    }

    Portfolio portfolio = null;

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

      // Add portfolio requirements to the dependency graph
      PortfolioCompilerTraversalCallback traversalCallback = new PortfolioCompilerTraversalCallback(builder, calcConfig);
      new PortfolioNodeTraverser(traversalCallback).traverse(portfolio.getRootNode());
    }

    return portfolio;
  }

  // --------------------------------------------------------------------------
  private static boolean hasPortfolioOutput(ViewDefinition viewDefinition) {
    ResultModelDefinition resultModelDefinition = viewDefinition.getResultModelDefinition();
    return resultModelDefinition.getPositionOutputMode() != ResultOutputMode.NONE || resultModelDefinition.getAggregatePositionOutputMode() != ResultOutputMode.NONE;
  }

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

  private static Map<IdentifierBundle, Security> resolveSecurities(Portfolio portfolio, ViewCompilationContext viewCompilationContext) {
    OperationTimer timer = new OperationTimer(s_logger, "Resolving all securities for {}", portfolio.getName());
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

  private static Set<IdentifierBundle> getSecurityKeysForResolution(PortfolioNode node) {
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

  private static Portfolio createFullyResolvedPortfolio(Portfolio portfolio, Map<IdentifierBundle, Security> securitiesByKey) {
    return new PortfolioImpl(portfolio.getUniqueIdentifier(), portfolio.getName(), createFullyResolvedPortfolioHierarchy(portfolio.getRootNode(), securitiesByKey));
  }

  private static PortfolioNodeImpl createFullyResolvedPortfolioHierarchy(PortfolioNode rootNode, Map<IdentifierBundle, Security> securitiesByKey) {
    if (rootNode == null) {
      return null;
    }
    PortfolioNodeImpl populatedNode = new PortfolioNodeImpl(rootNode.getUniqueIdentifier(), rootNode.getName());
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
      populatedPosition.setPortfolioNode(populatedNode.getUniqueIdentifier());
      populatedNode.addPosition(populatedPosition);
    }
    for (PortfolioNode child : rootNode.getChildNodes()) {
      final PortfolioNodeImpl childNode = createFullyResolvedPortfolioHierarchy(child, securitiesByKey);
      childNode.setParentNode(populatedNode.getUniqueIdentifier());
      populatedNode.addChildNode(childNode);
    }
    return populatedNode;
  }

}
