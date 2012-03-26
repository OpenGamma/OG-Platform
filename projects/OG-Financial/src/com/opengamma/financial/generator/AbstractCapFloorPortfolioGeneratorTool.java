/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.capfloor.CapFloorSecurity;

/**
 * Utility for constructing a random Cap/Floor portfolio.
 */
public abstract class AbstractCapFloorPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected abstract AbstractCapFloorSecurityGenerator createCapFloorSecurityGenerator();

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final AbstractCapFloorSecurityGenerator securities = createCapFloorSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<CapFloorSecurity>(securities, getSecurityPersister());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Cap/Floor"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

}
