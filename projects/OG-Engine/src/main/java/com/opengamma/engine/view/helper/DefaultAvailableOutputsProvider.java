/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import java.util.List;

import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.OptimisticMarketDataAvailabilityFilter;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@code AvailableOutputsProvider} against a local function repository.
 */
public class DefaultAvailableOutputsProvider implements AvailableOutputsProvider {

  private final CompiledFunctionService _compiledFunctions;
  private final FunctionExclusionGroups _functionExclusionGroups;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final MarketDataAvailabilityFilter _marketDataAvailability;
  private final String _wildcardIndicator;

  public DefaultAvailableOutputsProvider(final CompiledFunctionService compiledFunctionService, final FunctionExclusionGroups functionExclusionGroups, final PositionSource positionSource,
      final SecuritySource securitySource, final String wildcardIndicator) {
    this(compiledFunctionService, functionExclusionGroups, new OptimisticMarketDataAvailabilityFilter(), positionSource, securitySource, wildcardIndicator);
  }

  public DefaultAvailableOutputsProvider(final CompiledFunctionService compiledFunctionService, final FunctionExclusionGroups functionExclusionGroups,
      final MarketDataAvailabilityFilter marketDataAvailability, final PositionSource positionSource, final SecuritySource securitySource, final String wildcardIndicator) {
    ArgumentChecker.notNull(compiledFunctionService, "compiledFunctionService");
    ArgumentChecker.notNull(marketDataAvailability, "marketDataAvailability");
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");

    _compiledFunctions = compiledFunctionService;
    _functionExclusionGroups = functionExclusionGroups;
    _marketDataAvailability = marketDataAvailability;
    _positionSource = positionSource;
    _securitySource = securitySource;
    _wildcardIndicator = wildcardIndicator;
  }

  //------------------------------------------------------------------------
  @Override
  public AvailableOutputs getPortfolioOutputs(final Portfolio portfolio, final Instant instant) {
    return getPortfolioOutputs(portfolio, instant, null, null);
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(Portfolio portfolio, final Instant instant, final Integer maxNodes, final Integer maxPositions) {
    portfolio = preparePortfolio(portfolio, maxNodes, maxPositions);
    final Instant compileInstant = (instant != null ? instant : Instant.now());
    final CompiledFunctionRepository functionRepository = getCompiledFunctionService().compileFunctionRepository(compileInstant);
    return new AvailablePortfolioOutputs(portfolio, functionRepository, getFunctionExclusionGroups(), getMarketDataAvailability(), getWildcardIndicator());
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(final UniqueId portfolioId, final Instant instant) {
    return getPortfolioOutputs(portfolioId, instant, null, null);
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(final UniqueId portfolioId, final Instant instant, final Integer maxNodes, final Integer maxPositions) {
    Portfolio portfolio = getPortfolio(portfolioId);
    portfolio = preparePortfolio(portfolio, maxNodes, maxPositions);
    final Instant compileInstant = (instant != null ? instant : Instant.now());
    final CompiledFunctionRepository functionRepository = getCompiledFunctionService().compileFunctionRepository(compileInstant);
    return new AvailablePortfolioOutputs(portfolio, functionRepository, getFunctionExclusionGroups(), getMarketDataAvailability(), getWildcardIndicator());
  }

  //------------------------------------------------------------------------
  private CompiledFunctionService getCompiledFunctionService() {
    return _compiledFunctions;
  }

  private FunctionExclusionGroups getFunctionExclusionGroups() {
    return _functionExclusionGroups;
  }

  private PositionSource getPositionSource() {
    return _positionSource;
  }

  private SecuritySource getSecuritySource() {
    return _securitySource;
  }

  private MarketDataAvailabilityFilter getMarketDataAvailability() {
    return _marketDataAvailability;
  }

  private String getWildcardIndicator() {
    return _wildcardIndicator;
  }

  //------------------------------------------------------------------------
  private static SimplePortfolioNode copyNode(final PortfolioNode node, final Integer maxNodes, final Integer maxPositions) {
    final SimplePortfolioNode copy = new SimplePortfolioNode(node.getUniqueId(), node.getName());
    if (maxNodes != null && maxNodes > 0) {
      final List<PortfolioNode> childNodes = node.getChildNodes();
      int size = childNodes.size();
      if (size > 0) {
        if (size > maxNodes) {
          size = maxNodes;
        }
        for (int i = 0; i < size; i++) {
          copy.addChildNode(copyNode(childNodes.get(i), maxNodes, maxPositions));
        }
      }
    } else if (maxNodes == null) {
      for (final PortfolioNode child : node.getChildNodes()) {
        copy.addChildNode(copyNode(child, maxNodes, maxPositions));
      }
    }
    if (maxPositions != null && maxPositions > 0) {
      final List<Position> positions = node.getPositions();
      int size = positions.size();
      if (size > 0) {
        if (size > maxPositions) {
          size = maxPositions;
        }
        for (int i = 0; i < size; i++) {
          copy.addPosition(positions.get(i));
        }
      }
    } else if (maxPositions == null) {
      copy.addPositions(node.getPositions());
    }
    return copy;
  }

  /**
   * Fetches a portfolio by its unique identifier.
   * 
   * @param portfolioId the unique identifier of the portfolio, not null
   * @return the portfolio, not null
   * @throws DataNotFoundException if the portfolio identifier is invalid or cannot be resolved to a portfolio
   */
  protected Portfolio getPortfolio(final UniqueId portfolioId) {
    ArgumentChecker.notNull(portfolioId, "portfolioId");
    return getPositionSource().getPortfolio(portfolioId, VersionCorrection.LATEST);
  }

  /**
   * Prepares the portfolio, truncating the number of sub-nodes and positions if required and resolving its securities.
   * 
   * @param portfolio the portfolio, not null
   * @param maxNodes the maximum number of child nodes under each node, null for unlimited
   * @param maxPositions the maximum number of positions, null for unlimited
   * @return the resolved portfolio, truncated as requested
   */
  protected Portfolio preparePortfolio(Portfolio portfolio, final Integer maxNodes, final Integer maxPositions) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    if (maxNodes != null) {
      ArgumentChecker.notNegative(maxNodes, "maxNodes");
    }
    if (maxPositions != null) {
      ArgumentChecker.notNegative(maxPositions, "maxPositions");
    }
    if ((maxNodes != null) || (maxPositions != null)) {
      final SimplePortfolio copy = new SimplePortfolio(portfolio.getName());
      copy.setRootNode(copyNode(portfolio.getRootNode(), maxNodes, maxPositions));
      portfolio = copy;
    }
    return PortfolioCompiler.resolvePortfolio(portfolio, getCompiledFunctionService().getExecutorService().asService(), getSecuritySource());
  }

}
