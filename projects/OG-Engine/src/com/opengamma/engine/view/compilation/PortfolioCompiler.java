/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.concurrent.ExecutorService;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Compiles Portfolio requirements into the dependency graphs.
 */
public final class PortfolioCompiler {

  private PortfolioCompiler() {
  }

  // --------------------------------------------------------------------------
  /**
   * Adds portfolio targets to the dependency graphs as required, and fully resolves the portfolio structure.
   * 
   * @param compilationContext  the context of the view definition compilation
   * @return the fully-resolved portfolio structure if any portfolio targets were required, null otherwise.
   */
  protected static Portfolio execute(ViewCompilationContext compilationContext) {
    // Everything we do here is geared towards the avoidance of resolution (of portfolios, positions, securities)
    // wherever possible, to prevent needless dependencies (on a position master, security master) when a view never
    // really has them.

    if (!isPortfolioOutputEnabled(compilationContext.getViewDefinition())) {
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
      
      // Add portfolio requirements to the dependency graph
      final DependencyGraphBuilder builder = compilationContext.getBuilder(calcConfig.getName());
      final PortfolioCompilerTraversalCallback traversalCallback = new PortfolioCompilerTraversalCallback(calcConfig, builder);
      PortfolioNodeTraverser.parallel(traversalCallback, compilationContext.getServices().getExecutorService()).traverse(portfolio.getRootNode());

      // TODO: Use a heuristic to decide whether to let the graph builds run in parallel, or sequentially. We will force sequential builds for the time being.
      try {
        builder.waitForDependencyGraphBuild();
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }

    }
    
    return portfolio;
  }

  /**
   * Tests whether the view has portfolio outputs enabled.
   * 
   * @param viewDefinition  the view definition
   * @return true if there is at least one portfolio target
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
   * @param compilationContext  the compilation context containing the view being compiled, not null
   */
  private static Portfolio getPortfolio(ViewCompilationContext compilationContext) {
    UniqueId portfolioId = compilationContext.getViewDefinition().getPortfolioId();
    if (portfolioId == null) {
      throw new OpenGammaRuntimeException("The view definition '" + compilationContext.getViewDefinition().getName() + "' contains required portfolio outputs, but it does not reference a portfolio.");
    }
    PositionSource positionSource = compilationContext.getServices().getFunctionCompilationContext().getComputationTargetResolver().getPositionSource();
    if (positionSource == null) {
      throw new OpenGammaRuntimeException("The view definition '" + compilationContext.getViewDefinition().getName()
          + "' contains required portfolio outputs, but the compiler does not have access to a position source.");
    }
    // If the portfolio identifier is versioned then that must be used, otherwise it is the requested resolution version.
    Portfolio portfolio;
    try {
      if (portfolioId.isVersioned()) {
        portfolio = positionSource.getPortfolio(portfolioId);
      } else {
        portfolio = positionSource.getPortfolio(portfolioId.getObjectId(), compilationContext.getResolverVersionCorrection());
      }
    } catch (DataNotFoundException ex) {
      throw new OpenGammaRuntimeException("Unable to resolve portfolio '" + portfolioId + "' in position source '" + positionSource +
          "' used by view definition '" + compilationContext.getViewDefinition().getName() + "'", ex);
    }
    return portfolio;
  }

  /**
   * Resolves the securities in the portfolio at the latest version-correction.
   * 
   * @param portfolio  the portfolio to resolve, not null
   * @param executorService  the threading service, not null
   * @param securitySource  the security source, not null
   * @return the resolved portfolio, not null
   */
  public static Portfolio resolvePortfolio(final Portfolio portfolio, final ExecutorService executorService, final SecuritySource securitySource) {
    return resolvePortfolio(portfolio, executorService, securitySource, VersionCorrection.LATEST);
  }
  
  /**
   * Resolves the securities in the portfolio at the given version-correction.
   * 
   * @param portfolio  the portfolio to resolve, not null
   * @param executorService  the threading service, not null
   * @param securitySource  the security source, not null
   * @param versionCorrection  the version-correction for security resolution, not null
   * @return the resolved portfolio, not null
   */
  public static Portfolio resolvePortfolio(final Portfolio portfolio, final ExecutorService executorService, final SecuritySource securitySource, final VersionCorrection versionCorrection) {
    Portfolio cloned = new SimplePortfolio(portfolio);
    new SecurityLinkResolver(executorService, securitySource, versionCorrection).resolveSecurities(cloned.getRootNode());
    return cloned;
  }

}
