/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.joda.beans.MetaBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.impl.InMemoryPortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FungibleTradeBuilderTest {


  private static final ExternalIdBundle APPLE_BUNDLE;
  private static final ExternalIdBundle INTEL_BUNDLE;
  private static final EquitySecurity APPLE_SECURITY;
  private static final EquitySecurity INTEL_SECURITY;

  static {
    APPLE_BUNDLE = ExternalIdBundle.of(ExternalSchemes.BLOOMBERG_TICKER, "AAPL US Equity");
    INTEL_BUNDLE = ExternalIdBundle.of(ExternalSchemes.BLOOMBERG_TICKER, "INTC US Equity");
    APPLE_SECURITY = new EquitySecurity("exch", "exchCode", "Apple", Currency.USD);
    INTEL_SECURITY = new EquitySecurity("exch", "exchCode", "Intel", Currency.USD);
    APPLE_SECURITY.setExternalIdBundle(APPLE_BUNDLE);
    INTEL_SECURITY.setExternalIdBundle(INTEL_BUNDLE);
  }

  private InMemoryPortfolioMaster _portfolioMaster;
  private InMemoryPositionMaster _positionMaster;
  private FungibleTradeBuilder _tradeBuilder;
  private ManageablePortfolioNode _savedNode;
  private ManageablePosition _savedPosition;
  private ManageablePortfolio _savedPortfolio;

  @BeforeMethod
  public void setUp() throws Exception {
    _portfolioMaster = new InMemoryPortfolioMaster();
    _positionMaster = new InMemoryPositionMaster();
    InMemorySecurityMaster securityMaster = new InMemorySecurityMaster();
    securityMaster.add(new SecurityDocument(APPLE_SECURITY));
    securityMaster.add(new SecurityDocument(INTEL_SECURITY));
    BigDecimal quantity = BigDecimal.valueOf(20);
    ManageablePosition position = new ManageablePosition(quantity, APPLE_SECURITY.getExternalIdBundle());
    position.addTrade(new ManageableTrade(quantity,
                                          APPLE_BUNDLE,
                                          LocalDate.of(2012, 12, 1),
                                          OffsetTime.of(LocalTime.of(9, 30), ZoneOffset.UTC),
                                          ExternalId.of(AbstractTradeBuilder.CPTY_SCHEME, "existingCpty")));
    _savedPosition = _positionMaster.add(new PositionDocument(position)).getPosition();
    ManageablePortfolioNode root = new ManageablePortfolioNode("root");
    ManageablePortfolioNode node = new ManageablePortfolioNode("node");
    node.addPosition(_savedPosition.getUniqueId());
    root.addChildNode(node);
    ManageablePortfolio portfolio = new ManageablePortfolio("portfolio", root);
    _savedPortfolio = _portfolioMaster.add(new PortfolioDocument(portfolio)).getPortfolio();
    Set<MetaBean> metaBeans = Sets.<MetaBean>newHashSet(ManageableTrade.meta());
    _tradeBuilder = new FungibleTradeBuilder(_positionMaster,
                                             _portfolioMaster,
                                             securityMaster,
                                             metaBeans,
                                             BlotterUtils.getStringConvert());
    _savedNode = _savedPortfolio.getRootNode().getChildNodes().get(0);
  }

  private BeanDataSource createTradeData(String securityTicker, String id) {
    String securityId = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, securityTicker).toString();
    return BlotterTestUtils.beanData("uniqueId", id,
                                     "type", FungibleTradeBuilder.TRADE_TYPE_NAME,
                                     "tradeDate", "2012-12-21",
                                     "tradeTime", "14:25",
                                     "securityIdBundle", securityId,
                                     "premium", "1234",
                                     "premiumCurrency", "USD",
                                     "premiumDate", "2012-12-22",
                                     "premiumTime", "13:30",
                                     "quantity", "30",
                                     "counterparty", "cptyName",
                                     "attributes", Maps.newHashMap());
  }

  /**
   * add a trade to a portfolio node that doesn't have an existing position in the trade's security.
   * node has a position in a different security of the same type
   */
  @Test
  public void addTradeNoExistingPosition() {
    UniqueId tradeId = _tradeBuilder.addTrade(createTradeData("INTC US Equity", null), _savedNode.getUniqueId());
    ManageableTrade testTrade = _positionMaster.getTrade(tradeId);
    assertEquals(LocalDate.of(2012, 12, 21), testTrade.getTradeDate());
    assertEquals(OffsetTime.of(LocalTime.of(14, 25), ZoneOffset.UTC), testTrade.getTradeTime());
    assertEquals(INTEL_BUNDLE, testTrade.getSecurityLink().getExternalId());
    assertEquals(1234d, testTrade.getPremium());
    assertEquals(Currency.USD, testTrade.getPremiumCurrency());
    assertEquals(LocalDate.of(2012, 12, 22), testTrade.getPremiumDate());
    assertEquals(OffsetTime.of(LocalTime.of(13, 30), ZoneOffset.UTC), testTrade.getPremiumTime());
    assertEquals(BigDecimal.valueOf(30), testTrade.getQuantity());
    assertEquals(ExternalId.of(AbstractTradeBuilder.CPTY_SCHEME, "cptyName"), testTrade.getCounterpartyExternalId());
    assertTrue(testTrade.getAttributes().isEmpty());

    ManageablePosition testPosition = _positionMaster.get(testTrade.getParentPositionId()).getPosition();
    assertEquals(INTEL_BUNDLE, testPosition.getSecurityLink().getExternalId());
    assertEquals(testTrade.getQuantity(), testPosition.getQuantity());
    assertEquals(1, testPosition.getTrades().size());
    assertEquals(testTrade, testPosition.getTrades().get(0));

    ManageablePortfolio testPortfolio = _portfolioMaster.get(_savedNode.getPortfolioId()).getPortfolio();
    ManageablePortfolioNode testNode = testPortfolio.getRootNode().getChildNodes().get(0);
    assertEquals(2, testNode.getPositionIds().size());
    assertEquals(_savedPosition.getUniqueId().getObjectId(), testNode.getPositionIds().get(0));
    assertEquals(testPosition.getUniqueId().getObjectId(), testNode.getPositionIds().get(1));
  }

  /**
   * add a trade to a portfolio node that already has a position in the trade's security
   */
  @Test
  public void addTradeToExistingPosition() {
    UniqueId tradeId = _tradeBuilder.addTrade(createTradeData("AAPL US Equity", null), _savedNode.getUniqueId());
    ManageableTrade testTrade = _positionMaster.getTrade(tradeId);
    assertEquals(LocalDate.of(2012, 12, 21), testTrade.getTradeDate());
    assertEquals(OffsetTime.of(LocalTime.of(14, 25), ZoneOffset.UTC), testTrade.getTradeTime());
    assertEquals(APPLE_BUNDLE, testTrade.getSecurityLink().getExternalId());
    assertEquals(1234d, testTrade.getPremium());
    assertEquals(Currency.USD, testTrade.getPremiumCurrency());
    assertEquals(LocalDate.of(2012, 12, 22), testTrade.getPremiumDate());
    assertEquals(OffsetTime.of(LocalTime.of(13, 30), ZoneOffset.UTC), testTrade.getPremiumTime());
    assertEquals(BigDecimal.valueOf(30), testTrade.getQuantity());
    assertEquals(ExternalId.of(AbstractTradeBuilder.CPTY_SCHEME, "cptyName"), testTrade.getCounterpartyExternalId());
    assertTrue(testTrade.getAttributes().isEmpty());

    ManageablePosition testPosition = _positionMaster.get(testTrade.getParentPositionId()).getPosition();
    assertEquals(APPLE_BUNDLE, testPosition.getSecurityLink().getExternalId());
    assertEquals(BigDecimal.valueOf(50), testPosition.getQuantity());
    assertEquals(2, testPosition.getTrades().size());
    assertEquals(testTrade, testPosition.getTrades().get(1));

    ManageablePortfolio testPortfolio = _portfolioMaster.get(_savedNode.getPortfolioId()).getPortfolio();
    ManageablePortfolioNode testNode = testPortfolio.getRootNode().getChildNodes().get(0);
    assertEquals(1, testNode.getPositionIds().size());
    assertEquals(_savedPosition.getUniqueId().getObjectId(), testNode.getPositionIds().get(0));
  }

  /**
   * update a trade - the position's quantity should also be adjusted
   */
  @Test
  public void updateTrade() {
    ManageableTrade previousTrade = _savedPosition.getTrades().get(0);
    BeanDataSource tradeData = createTradeData("AAPL US Equity", previousTrade.getUniqueId().toString());
    UniqueId updatedId = _tradeBuilder.updateTrade(tradeData);

    ManageableTrade updatedTrade = _positionMaster.getTrade(updatedId);
    assertEquals(LocalDate.of(2012, 12, 21), updatedTrade.getTradeDate());
    assertEquals(OffsetTime.of(LocalTime.of(14, 25), ZoneOffset.UTC), updatedTrade.getTradeTime());
    assertEquals(APPLE_BUNDLE, updatedTrade.getSecurityLink().getExternalId());
    assertEquals(1234d, updatedTrade.getPremium());
    assertEquals(Currency.USD, updatedTrade.getPremiumCurrency());
    assertEquals(LocalDate.of(2012, 12, 22), updatedTrade.getPremiumDate());
    assertEquals(OffsetTime.of(LocalTime.of(13, 30), ZoneOffset.UTC), updatedTrade.getPremiumTime());
    assertEquals(BigDecimal.valueOf(30), updatedTrade.getQuantity());
    assertEquals(ExternalId.of(AbstractTradeBuilder.CPTY_SCHEME, "cptyName"), updatedTrade.getCounterpartyExternalId());
    assertTrue(updatedTrade.getAttributes().isEmpty());
  }

  /**
   * update a position that doesn't have any trades. position's quantity should match trade and trade should be added
   */
  @Test
  public void updatePositionWithNoTrades() {
    ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(42), APPLE_SECURITY.getExternalIdBundle());
    ManageablePosition savedPosition = _positionMaster.add(new PositionDocument(position)).getPosition();
    assertEquals(BigDecimal.valueOf(42), savedPosition.getQuantity());

    _tradeBuilder.updatePosition(createTradeData("AAPL US Equity", null), savedPosition.getUniqueId());
    ManageablePosition updatedPosition = _positionMaster.get(savedPosition.getUniqueId().getObjectId(),
                                                             VersionCorrection.LATEST).getPosition();
    assertEquals(BigDecimal.valueOf(30), updatedPosition.getQuantity());
    assertEquals(1, updatedPosition.getTrades().size());
    ManageableTrade trade = updatedPosition.getTrades().get(0);
    assertEquals(LocalDate.of(2012, 12, 21), trade.getTradeDate());
    assertEquals(OffsetTime.of(LocalTime.of(14, 25), ZoneOffset.UTC), trade.getTradeTime());
    assertEquals(APPLE_BUNDLE, trade.getSecurityLink().getExternalId());
    assertEquals(1234d, trade.getPremium());
    assertEquals(Currency.USD, trade.getPremiumCurrency());
    assertEquals(LocalDate.of(2012, 12, 22), trade.getPremiumDate());
    assertEquals(OffsetTime.of(LocalTime.of(13, 30), ZoneOffset.UTC), trade.getPremiumTime());
    assertEquals(BigDecimal.valueOf(30), trade.getQuantity());
    assertEquals(ExternalId.of(AbstractTradeBuilder.CPTY_SCHEME, "cptyName"), trade.getCounterpartyExternalId());
    assertTrue(trade.getAttributes().isEmpty());
  }

  /**
   * update the trade and change the security - this should fail
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void updateTradeChangeSecurity() {
    ManageableTrade previousTrade = _savedPosition.getTrades().get(0);
    BeanDataSource tradeData = createTradeData("INTC US Equity", previousTrade.getUniqueId().toString());
    _tradeBuilder.updateTrade(tradeData);
  }
}
