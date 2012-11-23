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
 * Resolves the specified portfolio's securities and adds value requirements (targets) to the graph builder in the
 * compilation context, thus triggering the compilation of the dependency graphs. The identification of value
 * requirements is done through a parallel traversal on the portfolio nodes using PortfolioCompilerTraversalCallback,
 * which actually produces the value requirements and adds them to the graph builder.
 */
public final class PortfolioCompiler {

  private PortfolioCompiler() {
  }

  /**
   * Resolves the securities in the portfolio at the latest version-correction.
   *
   * @param portfolio  the portfolio to resolve, not null
   * @param executorService  the threading service, not null
   * @param securitySource  the security source, not null
   * @return the resolved portfolio, not null
   */
  public static Portfolio resolvePortfolio(final Portfolio portfolio, final ExecutorService executorService,
                                           final SecuritySource securitySource) {
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
  public static Portfolio resolvePortfolio(final Portfolio portfolio, final ExecutorService executorService,
                                           final SecuritySource securitySource, final VersionCorrection versionCorrection) {
    Portfolio cloned = new SimplePortfolio(portfolio);
    new SecurityLinkResolver(executorService, securitySource, versionCorrection).resolveSecurities(cloned.getRootNode());
    return cloned;
  }

  // --------------------------------------------------------------------------
  /**
   * Adds portfolio targets to the dependency graphs as required, and fully resolves the portfolio structure.
   * 
   * @param compilationContext  the context of the view definition compilation
   * @param versionCorrection  the version-correction at which to operate, not null
   * @param forcePortfolioResolution  true if there are external portfolio targets, false otherwise
   * @return the fully-resolved portfolio structure if any portfolio targets were required, null otherwise.
   */
  protected static Portfolio execute(ViewCompilationContext compilationContext, VersionCorrection versionCorrection,
                                     boolean forcePortfolioResolution) {
    // Everything we do here is geared towards the avoidance of resolution (of portfolios, positions, securities)
    // wherever possible, to prevent needless dependencies (on a position master, security master) when a view never
    // really has them.

    if (!isPortfolioOutputEnabled(compilationContext.getViewDefinition())) {
      // Doesn't even matter if the portfolio can't be resolved - we're not outputting anything at the portfolio level
      // (which might be because the user knows the portfolio can't be resolved right now) so there are no portfolio
      // targets to add to the dependency graph.
      return null;
    }

    Portfolio portfolio = forcePortfolioResolution ? getPortfolio(compilationContext, versionCorrection) : null;

    // For each configuration in the view def, add portfolio requirements to dep graph, resolve the portfolio and
    // start the graph building job
    for (ViewCalculationConfiguration calcConfig : compilationContext.getViewDefinition().getAllCalculationConfigurations()) {

      if (calcConfig.getAllPortfolioRequirements().size() == 0) {
        // No portfolio requirements for this calculation configuration - avoid further processing.
        continue;
      }

      // Actually need the portfolio now
      if (portfolio == null) {
        portfolio = getPortfolio(compilationContext, versionCorrection);
      }
      
      // Add portfolio requirements to the dependency graph:
      // Use PortfolioNodeTraverser to traverse the portfolio tree looking for value requirements.
      // PortfolioCompilerTraversalCallback passes any found value requirements to the dep graph builder,
      // and any related graph building may immediately proceed in the background
      final DependencyGraphBuilder builder = compilationContext.getBuilder(calcConfig.getName());
      final PortfolioCompilerTraversalCallback traversalCallback = new PortfolioCompilerTraversalCallback(calcConfig, builder);
      PortfolioNodeTraverser.parallel(traversalCallback,
          compilationContext.getServices().getExecutorService()).traverse(portfolio.getRootNode());

      // TODO: Use a heuristic to decide whether to let the graph builds run in parallel, or sequentially. We will force sequential builds for the time being.
      // Wait for the current config's dependency graph to be built before moving to the next view calc config
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
    return resultModelDefinition.getPositionOutputMode() != ResultOutputMode.NONE
        || resultModelDefinition.getAggregatePositionOutputMode() != ResultOutputMode.NONE;
  }

  /**
   * Fully resolves the portfolio structure for a view. A fully resolved structure has resolved
   * {@link Security} objects for each {@link Position} within the portfolio. Note however that
   * any underlying or related data referenced by a security will not be resolved at this stage. 
   * 
   * @param compilationContext  the compilation context containing the view being compiled, not null
   * @param versionCorrection  the version-correction at which the portfolio is required, not null
   */
  private static Portfolio getPortfolio(ViewCompilationContext compilationContext, VersionCorrection versionCorrection) {

    // Get the portfolio ID from the view definition
    UniqueId portfolioId = compilationContext.getViewDefinition().getPortfolioId();
    if (portfolioId == null) {
      throw new OpenGammaRuntimeException("The view definition '" + compilationContext.getViewDefinition().getName()
          + "' contains required portfolio outputs, but it does not reference a portfolio.");
    }

    // Get the position source from the compilation context
    PositionSource positionSource = compilationContext.getServices().getComputationTargetResolver().getPositionSource();
    if (positionSource == null) {
      throw new OpenGammaRuntimeException("The view definition '" + compilationContext.getViewDefinition().getName()
          + "' contains required portfolio outputs, but the compiler does not have access to a position source.");
    }

    // Resolve the portfolio
    // NOTE jonathan 2011-11-11 -- not sure what the right thing to do is here. Reasonable compromise seems to be to
    // follow the cycle VersionCorrection if no specific portfolio version has been specified, otherwise to use the
    // exact portfolio version requested (which is an important requirement for e.g. PnL Explain). Perhaps the
    // portfolio should be loaded independently of the cycle version correction, so latest always means latest?
    Portfolio portfolio;
    try {
      if (portfolioId.isVersioned()) {
        portfolio = positionSource.getPortfolio(portfolioId);
      } else {
        portfolio = positionSource.getPortfolio(portfolioId.getObjectId(), versionCorrection);
      }
    } catch (DataNotFoundException ex) {
      throw new OpenGammaRuntimeException("Unable to resolve portfolio '" + portfolioId + "' in position source '"
          + positionSource + "' used by view definition '" + compilationContext.getViewDefinition().getName() + "'", ex);
    }
    return portfolio;
  }

}
