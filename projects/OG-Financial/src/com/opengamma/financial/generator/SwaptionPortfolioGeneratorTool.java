/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.option.SwaptionSecurity;

/**
 * Utility for constructing a random swaption portfolio.
 */
public class SwaptionPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected SwapSecurityGenerator createSwapSecurityGenerator() {
    return new SwapSecurityGenerator();
  }

  protected SwaptionSecurityGenerator createSwaptionSecurityGenerator() {
    return new SwaptionSecurityGenerator(createSwapSecurityGenerator(), getSecurityPersister());
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SwaptionSecurityGenerator securities = createSwaptionSecurityGenerator();
    configure(securities);
    configure(securities.getUnderlyingGenerator());
    final PositionGenerator positions = new SimplePositionGenerator<SwaptionSecurity>(securities, getSecurityPersister());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaptions"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

}
