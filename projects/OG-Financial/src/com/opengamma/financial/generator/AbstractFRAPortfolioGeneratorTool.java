/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.fra.FRASecurity;

/**
 * Utility for constructing a random FRA portfolio.
 */
public abstract class AbstractFRAPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected abstract AbstractFRASecurityGenerator createFRASecurityGenerator();

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final AbstractFRASecurityGenerator securities = createFRASecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<FRASecurity>(securities, getSecurityPersister());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("FRAs"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

}
