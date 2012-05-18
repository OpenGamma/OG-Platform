/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.swap.ForwardSwapSecurity;

/**
 * Utility for constructing a random forward swap portfolio.
 */
public class ForwardSwapPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected ForwardSwapSecurityGenerator createForwardSwapSecurityGenerator() {
    return new ForwardSwapSecurityGenerator();
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final ForwardSwapSecurityGenerator securities = createForwardSwapSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<ForwardSwapSecurity>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaps"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

}
