/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Resolves the specified portfolio's securities and adds value requirements (targets) to the graph builder in the compilation context, thus triggering the compilation of the dependency graphs. The
 * identification of value requirements is done through a parallel traversal on the portfolio nodes using PortfolioCompilerTraversalCallback, which actually produces the value requirements and adds
 * them to the graph builder.
 */
public final class PortfolioCompiler {

  private PortfolioCompiler() {
  }

  /**
   * Resolves the securities in the portfolio at the latest version-correction.
   * 
   * @param portfolio the portfolio to resolve, not null
   * @param executorService the threading service, not null
   * @param securitySource the security source, not null
   * @return the resolved portfolio, not null
   */
  public static Portfolio resolvePortfolio(final Portfolio portfolio, final ExecutorService executorService,
      final SecuritySource securitySource) {
    return resolvePortfolio(portfolio, executorService, securitySource, VersionCorrection.LATEST);
  }

  /**
   * Resolves the securities in the portfolio at the given version-correction.
   * 
   * @param portfolio the portfolio to resolve, not null
   * @param executorService the threading service, not null
   * @param securitySource the security source, not null
   * @param versionCorrection the version-correction for security resolution, not null
   * @return the resolved portfolio, not null
   */
  public static Portfolio resolvePortfolio(final Portfolio portfolio, final ExecutorService executorService,
      final SecuritySource securitySource, final VersionCorrection versionCorrection) {
    final Portfolio cloned = new SimplePortfolio(portfolio);
    new SecurityLinkResolver(executorService, securitySource, versionCorrection).resolveSecurities(cloned.getRootNode());
    return cloned;
  }

  // --------------------------------------------------------------------------
  /**
   * Adds portfolio targets to the dependency graphs as required by a full compilation.
   * 
   * @param compilationContext the context of the view definition compilation
   * @param resolutions the resolutions within the portfolio structure (for example the position object identifiers and underlying security references)
   */
  protected static void executeFull(final ViewCompilationContext compilationContext, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions) {
    execute(compilationContext, resolutions, null);
  }

  private static void execute(final ViewCompilationContext compilationContext, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions, final Set<UniqueId> limitEvents) {
    for (final ViewCalculationConfiguration calcConfig : compilationContext.getViewDefinition().getAllCalculationConfigurations()) {
      if (calcConfig.getAllPortfolioRequirements().size() == 0) {
        // No portfolio requirements for this calculation configuration - avoid further processing.
        continue;
      }
      // Add portfolio requirements to the dependency graph
      final DependencyGraphBuilder builder = compilationContext.getBuilder(calcConfig.getName());
      final Portfolio portfolio = builder.getCompilationContext().getPortfolio();
      final PortfolioCompilerTraversalCallback traversalCallback = new PortfolioCompilerTraversalCallback(calcConfig, builder, resolutions, limitEvents);
      PortfolioNodeTraverser.parallel(traversalCallback, compilationContext.getServices().getExecutorService()).traverse(portfolio.getRootNode());
      // TODO: Use a heuristic to decide whether to let the graph builds run in parallel, or sequentially. We will force sequential builds for the time being.
      // Wait for the current config's dependency graph to be built before moving to the next view calc config
      try {
        builder.waitForDependencyGraphBuild();
      } catch (final InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
    }
  }

  /**
   * Adds portfolio targets to dependency graphs as required by an incremental compilation (nothing), and fully resolved the portfolio structure.
   * 
   * @param compilationContext the context of the view definition compiler
   * @param resolutions the resolutions within the portfolio structure (for example the position object identifiers and underlying security references)
   * @param changedPositions the identifiers of positions that have changed, or null if none
   */
  protected static void executeIncremental(final ViewCompilationContext compilationContext, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions,
      final Set<UniqueId> changedPositions) {
    if (changedPositions != null) {
      execute(compilationContext, resolutions, changedPositions);
    }
  }

}
