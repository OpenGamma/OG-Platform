/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;


/**
 * Utility for constructing a random swap portfolio.
 */
public abstract class AbstractSwapPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected abstract AbstractSwapSecurityGenerator createSwapSecurityGenerator();

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final AbstractSwapSecurityGenerator securities = createSwapSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SwapPositionGenerator(securities, getSecurityPersister());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaps"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

}
