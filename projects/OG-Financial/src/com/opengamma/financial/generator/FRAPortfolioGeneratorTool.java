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
public class FRAPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected FRASecurityGenerator createFRASecurityGenerator() {
    return new FRASecurityGenerator();
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final FRASecurityGenerator securities = createFRASecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<FRASecurity>(securities, getSecurityPersister());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("FRAs"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

}
