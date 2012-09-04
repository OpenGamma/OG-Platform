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
public class CapFloorPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected CapFloorSecurityGenerator createCapFloorSecurityGenerator() {
    return new CapFloorSecurityGenerator();
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final CapFloorSecurityGenerator securities = createCapFloorSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<CapFloorSecurity>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Cap/Floor"), positions, size);
  }

}
