/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

/**
 * Utility for constructing a random bond portfolio.
 */
public class BondPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final GovernmentBondSecurityGenerator securities = new GovernmentBondSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new BondPositionGenerator(securities, getSecurityPersister());
    // TODO: create other bond types
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Bonds"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

}
