/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.math.BigDecimal;

import org.threeten.bp.LocalDate;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.target.lazy.LazyResolveContext;
import com.opengamma.engine.target.lazy.LazyResolvedPosition;
import com.opengamma.engine.target.lazy.LazyResolvedTrade;
import com.opengamma.engine.target.lazy.TargetResolverPortfolio;
import com.opengamma.engine.target.lazy.TargetResolverPortfolioNode;
import com.opengamma.engine.target.lazy.TargetResolverPosition;
import com.opengamma.engine.target.lazy.TargetResolverTrade;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * 
 */
public class MockComputationTargetResolver extends MapComputationTargetResolver {

  /**
   * 
   */
  public static final LocalDate TODAY = LocalDate.now();

  private final InMemorySecuritySource _securitySource = new InMemorySecuritySource();
  private final MockPositionSource _positionSource = new MockPositionSource();

  private int _portfolioId;
  private int _nodeId;
  private int _positionId;
  private int _tradeId;
  private int _securityId;

  private int _resolveCalls;

  protected MockComputationTargetResolver() {
    createPortfolio();
  }

  public static MockComputationTargetResolver resolved() {
    return new MockComputationTargetResolver();
  }

  public static MockComputationTargetResolver unresolved() {
    return new MockComputationTargetResolver() {

      @Override
      protected Portfolio portfolio(final Portfolio portfolio) {
        return new TargetResolverPortfolio(atVersionCorrection(VersionCorrection.LATEST), portfolio);
      }

      @Override
      protected PortfolioNode portfolioNode(final PortfolioNode node) {
        return new TargetResolverPortfolioNode(atVersionCorrection(VersionCorrection.LATEST), node);
      }

      @Override
      protected Position position(final Position position) {
        return new TargetResolverPosition(atVersionCorrection(VersionCorrection.LATEST), new LazyResolvedPosition(
            new LazyResolveContext(getSecuritySource(), null).atVersionCorrection(VersionCorrection.LATEST), new SimplePosition(position)));
      }

      @Override
      protected Trade trade(final Trade trade) {
        return new TargetResolverTrade(atVersionCorrection(VersionCorrection.LATEST), new LazyResolvedTrade(
            new LazyResolveContext(getSecuritySource(), null).atVersionCorrection(VersionCorrection.LATEST), new SimpleTrade(trade)));
      }

      @Override
      protected SecurityLink security(final Security security) {
        return new SimpleSecurityLink(security.getExternalIdBundle());
      }

    };
  }

  protected Portfolio portfolio(final Portfolio portfolio) {
    return portfolio;
  }

  private void createPortfolio() {
    final int id = _portfolioId++;
    final SimplePortfolio portfolio = new SimplePortfolio(UniqueId.of("Portfolio", Integer.toString(id)), "Portfolio " + id);
    portfolio.setRootNode(createNode(null, 2));
    _positionSource.addPortfolio(portfolio);
  }

  protected PortfolioNode portfolioNode(final PortfolioNode node) {
    return node;
  }

  private SimplePortfolioNode createNode(final UniqueId parentNodeId, int depth) {
    final int id = _nodeId++;
    final SimplePortfolioNode node = new SimplePortfolioNode(UniqueId.of("Node", Integer.toString(id)), "Node " + id) {

      private static final long serialVersionUID = 1L;

      @Override
      public void setUniqueId(final UniqueId uid) {
        // No-op
      }

    };
    if (parentNodeId != null) {
      node.setParentNodeId(parentNodeId);
    }
    if (depth > 0) {
      node.addChildNode(createNode(node.getUniqueId(), depth - 1));
      node.addChildNode(createNode(node.getUniqueId(), depth - 1));
    }
    node.addPosition(createPosition());
    node.addPosition(createPosition());
    addTarget(new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, portfolioNode(node)));
    return node;
  }

  protected Trade trade(final Trade trade) {
    return trade;
  }

  protected Position position(final Position position) {
    return position;
  }

  protected SecurityLink security(final Security security) {
    return SimpleSecurityLink.of(security);
  }

  private SimplePosition createPosition() {
    final Security security = createSecurity();
    final SimplePosition position = new SimplePosition(UniqueId.of("Position", Integer.toString(_positionId++)), BigDecimal.ONE, security) {

      private static final long serialVersionUID = 1L;

      @Override
      public void setUniqueId(final UniqueId uid) {
        // No-op
      }

    };
    position.setSecurityLink(security(security));
    final SimpleTrade trade = new SimpleTrade(security, BigDecimal.ONE, new SimpleCounterparty(ExternalId.of("Counterparty", "Mock")), TODAY, null) {

      private static final long serialVersionUID = 1L;

      @Override
      public void setUniqueId(final UniqueId uid) {
        if (getUniqueId() == null) {
          super.setUniqueId(uid);
        }
      }

    };
    trade.setUniqueId(UniqueId.of("Trade", Integer.toString(_tradeId++)));
    trade.setSecurityLink(security(security));
    addTarget(new ComputationTarget(ComputationTargetType.TRADE, trade(trade)));
    position.addTrade(trade);
    addTarget(new ComputationTarget(ComputationTargetType.POSITION, position(position)));
    return position;
  }

  private Security createSecurity() {
    final Security security = new MockSecurity(_securityId++);
    _securitySource.addSecurity(security);
    addTarget(new ComputationTarget(ComputationTargetType.SECURITY, security));
    return security;
  }

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification, final VersionCorrection versionCorrection) {
    _resolveCalls++;
    return super.resolve(specification, versionCorrection);
  }

  public int getResolveCalls() {
    return _resolveCalls;
  }

  @Override
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public PositionSource getPositionSource() {
    return _positionSource;
  }

}
