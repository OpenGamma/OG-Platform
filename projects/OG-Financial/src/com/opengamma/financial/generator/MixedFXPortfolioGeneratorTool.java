/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.math.BigDecimal;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Utility for constructing a random FX portfolio.
 */
public class MixedFXPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  /**
   * 
   */
  protected class MixedFXSecurityGenerator<T extends ManageableSecurity> extends AbstractFXSecurityGenerator<T> implements PortfolioNodeGenerator {

    private int _count = 1;

    @Override
    public T createSecurity() {
      return null;
    }

    private void add(final SimplePortfolioNode node, final ManageableTrade trade) {
      final Position position = SimplePositionGenerator.createPositionFromTrade(trade);
      if (position != null) {
        node.addPosition(position);
      }
    }

    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode("Strategy " + _count);
      final Bundle bundle = createBundle();
      add(node, createFXBarrierOptionSecurityTrade(bundle, BigDecimal.ONE, getSecurityPersister(), getCounterPartyGenerator()));
      add(node, createFXDigitalOptionSecurityTrade(bundle, BigDecimal.ONE, getSecurityPersister(), getCounterPartyGenerator()));
      add(node, createFXForwardSecurityTrade(bundle, BigDecimal.ONE, getSecurityPersister(), getCounterPartyGenerator()));
      add(node, createFXOptionSecurityTrade(bundle, BigDecimal.ONE, getSecurityPersister(), getCounterPartyGenerator()));
      if (node.getPositions().isEmpty()) {
        return null;
      }
      _count++;
      return node;
    }

  }

  protected <T extends ManageableSecurity> MixedFXSecurityGenerator<T> createMixedFXSecurityGenerator() {
    return new MixedFXSecurityGenerator<T>();
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final MixedFXSecurityGenerator<?> securities = createMixedFXSecurityGenerator();
    configure(securities);
    final TreePortfolioNodeGenerator rootNode = new TreePortfolioNodeGenerator(new StaticNameGenerator("Mixed FX"));
    for (int i = 0; i < size / 4; i++) {
      rootNode.addChildNode(securities);
    }
    return rootNode;
  }

}
