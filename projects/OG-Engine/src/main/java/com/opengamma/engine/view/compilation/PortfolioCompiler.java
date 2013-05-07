/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.concurrent.ExecutorService;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.security.SecuritySource;
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

}
