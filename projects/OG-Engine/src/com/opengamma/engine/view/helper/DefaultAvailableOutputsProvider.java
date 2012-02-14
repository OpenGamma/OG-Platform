/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import java.util.List;

import javax.time.Instant;
import javax.time.InstantProvider;

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
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.test.OptimisticMarketDataAvailabilityProvider;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@code AvailableOutputsProvider} against a local function repository.
 */
public class DefaultAvailableOutputsProvider implements AvailableOutputsProvider {

  private final CompiledFunctionService _compiledFunctions;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final MarketDataAvailabilityProvider _marketDataAvailabilityProvider;
  private final String _wildcardIndicator;
  
  public DefaultAvailableOutputsProvider(CompiledFunctionService compiledFunctionService,
      PositionSource positionSource, SecuritySource securitySource, String wildcardIndicator) {
    this(compiledFunctionService, new OptimisticMarketDataAvailabilityProvider(), positionSource, securitySource, wildcardIndicator);
  }
  
  public DefaultAvailableOutputsProvider(CompiledFunctionService compiledFunctionService,
      MarketDataAvailabilityProvider marketDataAvailabilityProvider, 
      PositionSource positionSource, SecuritySource securitySource, String wildcardIndicator) {
    ArgumentChecker.notNull(compiledFunctionService, "compiledFunctionService");
    ArgumentChecker.notNull(marketDataAvailabilityProvider, "marketDataAvailabilityProvider");
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");

    _compiledFunctions = compiledFunctionService;
    _marketDataAvailabilityProvider = marketDataAvailabilityProvider;
    _positionSource = positionSource;
    _securitySource = securitySource;
    _wildcardIndicator = wildcardIndicator;
  }

  //------------------------------------------------------------------------
  @Override
  public AvailableOutputs getPortfolioOutputs(Portfolio portfolio, InstantProvider instantProvider) {
    return getPortfolioOutputs(portfolio, instantProvider, null, null);
  }
  
  @Override
  public AvailableOutputs getPortfolioOutputs(Portfolio portfolio, InstantProvider instantProvider, Integer maxNodes, Integer maxPositions) {
    portfolio = preparePortfolio(portfolio, maxNodes, maxPositions);
    InstantProvider compileInstantProvider = instantProvider != null ? instantProvider : Instant.now();
    CompiledFunctionRepository functionRepository = getCompiledFunctionService().compileFunctionRepository(compileInstantProvider);
    return new AvailablePortfolioOutputs(portfolio, functionRepository, getMarketDataAvailabilityProvider(), getWildcardIndicator());
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(UniqueId portfolioId, InstantProvider instantProvider) {
    return getPortfolioOutputs(portfolioId, instantProvider, null, null);
  }
  
  @Override
  public AvailableOutputs getPortfolioOutputs(UniqueId portfolioId, InstantProvider instantProvider, Integer maxNodes, Integer maxPositions) {
    Portfolio portfolio = getPortfolio(portfolioId);
    portfolio = preparePortfolio(portfolio, maxNodes, maxPositions);
    InstantProvider compileInstantProvider = instantProvider != null ? instantProvider : Instant.now();
    CompiledFunctionRepository functionRepository = getCompiledFunctionService().compileFunctionRepository(compileInstantProvider);
    return new AvailablePortfolioOutputs(portfolio, functionRepository, getMarketDataAvailabilityProvider(), getWildcardIndicator());
  }
  
  //------------------------------------------------------------------------
  private CompiledFunctionService getCompiledFunctionService() {
    return _compiledFunctions;
  }
  
  private PositionSource getPositionSource() {
    return _positionSource;
  }
  
  private SecuritySource getSecuritySource() {
    return _securitySource;
  }
  
  private MarketDataAvailabilityProvider getMarketDataAvailabilityProvider() {
    return _marketDataAvailabilityProvider;
  }
  
  private String getWildcardIndicator() {
    return _wildcardIndicator;
  }
  
  //------------------------------------------------------------------------
  private static SimplePortfolioNode copyNode(PortfolioNode node, Integer maxNodes, Integer maxPositions) {
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
      for (PortfolioNode child : node.getChildNodes()) {
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
   * @param portfolioId  the unique identifier of the portfolio, not null
   * @return the portfolio, not null
   * @throws DataNotFoundException  if the portfolio identifier is invalid or cannot be resolved to a portfolio
   */
  protected Portfolio getPortfolio(UniqueId portfolioId) {
    ArgumentChecker.notNull(portfolioId, "portfolioId");
    return getPositionSource().getPortfolio(portfolioId);
  }

  /**
   * Prepares the portfolio, truncating the number of sub-nodes and positions if required and resolving its securities.
   * 
   * @param portfolio  the portfolio, not null
   * @param maxNodes  the maximum number of child nodes under each node, null for unlimited
   * @param maxPositions  the maximum number of positions, null for unlimited
   * @return the resolved portfolio, truncated as requested
   */
  protected Portfolio preparePortfolio(Portfolio portfolio, Integer maxNodes, Integer maxPositions) {
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
    return PortfolioCompiler.resolvePortfolio(portfolio, getCompiledFunctionService().getExecutorService(), getSecuritySource());
  }

}
