/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.List;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * REST interface to the AvailableOutputs helper
 */
@Path("availableOutputs")
public class AvailableOutputsService {

  private final CompiledFunctionService _compiledFunctions;
  private final FudgeContext _fudgeContext;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private String _anyValue;

  public AvailableOutputsService(final FudgeContext fudgeContext, final CompiledFunctionService compiledFunctionService, final PositionSource positionSource, final SecuritySource securitySource) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(compiledFunctionService, "compiledFunctionService");
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _fudgeContext = fudgeContext;
    _compiledFunctions = compiledFunctionService;
    _positionSource = positionSource;
    _securitySource = securitySource;
  }

  public CompiledFunctionService getCompiledFunctions() {
    return _compiledFunctions;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public PositionSource getPositionSource() {
    return _positionSource;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public String getWildcardIndicator() {
    return _anyValue;
  }

  public void setWildcardIndicator(final String anyValue) {
    _anyValue = anyValue;
  }

  private static SimplePortfolioNode copyNode(final PortfolioNode node, final int maxNodes, final int maxPositions) {
    final SimplePortfolioNode copy = new SimplePortfolioNode(node.getUniqueId(), node.getName());
    if (maxNodes > 0) {
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
    } else if (maxNodes < 0) {
      for (PortfolioNode child : node.getChildNodes()) {
        copy.addChildNode(copyNode(child, maxNodes, maxPositions));
      }
    }
    if (maxPositions > 0) {
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
    } else if (maxPositions < 0) {
      copy.addPositions(node.getPositions());
    }
    return copy;
  }

  /**
   * Fetch and resolve the portfolio, truncating the number of sub-nodes and positions if necessary.
   * 
   * @param uid identifier of the portfolio
   * @param maxNodes maximum child nodes under each node, {@code -1} for no limit
   * @param maxPositions maximum number of positions, {@code -1} for no limit
   * @return the resolved (possibly truncated) portfolio
   */
  protected Portfolio getPortfolio(final String uid, final int maxNodes, final int maxPositions) {
    Portfolio rawPortfolio = getPositionSource().getPortfolio(UniqueId.parse(uid));
    if (rawPortfolio == null) {
      return null;
    }
    if ((maxNodes > -1) || (maxPositions > -1)) {
      final SimplePortfolio copy = new SimplePortfolio(rawPortfolio.getName());
      copy.setRootNode(copyNode(rawPortfolio.getRootNode(), maxNodes, maxPositions));
      rawPortfolio = copy;
    }
    final Portfolio resolvedPortfolio = PortfolioCompiler.resolvePortfolio(rawPortfolio, getCompiledFunctions().getExecutorService(), getSecuritySource());
    return resolvedPortfolio;
  }

  @Path("portfolio")
  public AvailablePortfolioOutputsResource portfolio() {
    return new AvailablePortfolioOutputsResource(this);
  }

}
