/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.swap.SwapSecurity;


/**
 * Utility for constructing a random swap portfolio.
 */
public class SwapPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected SwapSecurityGenerator createSwapSecurityGenerator() {
    SwapSecurityGenerator securities = new SwapSecurityGenerator();
    configure(securities);
    return securities;
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SwapSecurityGenerator securities = createSwapSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<SwapSecurity>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaps"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SwapSecurityGenerator securities = createSwapSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<SwapSecurity>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaps"), positions, portfolioSize);
  }
  
}
