/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.BondPositionGenerator;
import com.opengamma.financial.generator.GovernmentBondSecurityGenerator;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;

/**
 * Utility for constructing a random bond portfolio.
 */
public class BondPortfolioGeneratorTool extends PortfolioGeneratorTool {

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
