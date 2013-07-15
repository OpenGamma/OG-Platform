/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 *
 */
/* package */ class FungibleTradeBuilder extends AbstractTradeBuilder {

  /** Value for the type name in the JSON for fungible trades */
  /* package */ static final String TRADE_TYPE_NAME = "FungibleTrade";

  /** Key used in the JSON for the security ID bundle. */
  private static final String SECURITY_ID_BUNDLE = "securityIdBundle";

  /** For loading and saving portfolios and nodes. */
  private final PortfolioMaster _portfolioMaster;
  /** For loading and saving securities. */
  private final MasterSecuritySource _securitySource;

  /* package */ FungibleTradeBuilder(PositionMaster positionMaster,
                                     PortfolioMaster portfolioMaster,
                                     SecurityMaster securityMaster,
                                     Set<MetaBean> metaBeans,
                                     StringConvert stringConvert) {
    super(positionMaster, portfolioMaster, securityMaster, metaBeans, stringConvert);
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    _portfolioMaster = portfolioMaster;
    _securitySource = new MasterSecuritySource(getSecurityMaster());
  }

  /**
   * TODO make this non-static and make stringConvert a field
   * Extracts trade data and populates a data sink.
   * @param trade The trade
   * @param sink The sink that should be populated with the trade data
   */
  /* package */ void extractTradeData(ManageableTrade trade, BeanDataSink<?> sink, StringConvert stringConvert) {
    sink.setValue("type", TRADE_TYPE_NAME);
    extractPropertyData(trade.uniqueId(), sink);
    extractPropertyData(trade.tradeDate(), sink);
    extractPropertyData(trade.tradeTime(), sink);
    extractPropertyData(trade.premium(), sink);
    extractPropertyData(trade.premiumCurrency(), sink);
    extractPropertyData(trade.premiumDate(), sink);
    extractPropertyData(trade.premiumTime(), sink);
    extractPropertyData(trade.quantity(), sink);
    sink.setMap(trade.attributes().name(), trade.getAttributes());
    // this shouldn't be necessary as counterparty ID isn't nullable but there's a bug in the implementation of
    // ManageableTrade which allows null values
    ExternalId counterpartyId = trade.getCounterpartyExternalId();
    String counterpartyValue;
    if (counterpartyId != null) {
      counterpartyValue = counterpartyId.getValue();
    } else {
      counterpartyValue = null;
    }
    sink.setValue(COUNTERPARTY, counterpartyValue);
    ExternalIdBundle securityIdBundle = trade.getSecurityLink().getExternalId();
    StringConverter<ExternalIdBundle> converter = stringConvert.findConverter(ExternalIdBundle.class);
    sink.setValue(SECURITY_ID_BUNDLE, converter.convertToString(securityIdBundle));
  }

  private void extractPropertyData(Property<?> property, BeanDataSink<?> sink) {
    sink.setValue(property.name(), getStringConvert().convertToString(property.metaProperty().get(property.bean())));
  }

  /* package */ UniqueId addTrade(BeanDataSource tradeData, UniqueId nodeId) {
    ManageableTrade trade = buildTrade(tradeData);
    Security security = trade.getSecurityLink().resolve(_securitySource);
    // this slightly awkward approach is due to the portfolio master API. you can look up a node directly but in order
    // to save it you have to save the whole portfolio. this means you need to look up the node to find the portfolio
    // ID, look up the portfolio, find the node in the portfolio, modify that copy of the node and save the portfolio
    // TODO can use a portfolio search request and only hit the master once
    ManageablePortfolioNode node = _portfolioMaster.getNode(nodeId);
    ManageablePortfolio portfolio = _portfolioMaster.get(node.getPortfolioId()).getPortfolio();
    ManageablePortfolioNode portfolioNode = findNode(portfolio, nodeId);
    ManageablePosition position = findPosition(portfolioNode, security);
    if (position == null) {
      // no position in this security on the node, create a new position just for this trade
      ManageablePosition newPosition = new ManageablePosition(trade.getQuantity(), security.getExternalIdBundle());
      newPosition.addTrade(trade);
      ManageablePosition savedPosition = getPositionMaster().add(new PositionDocument(newPosition)).getPosition();
      portfolioNode.addPosition(savedPosition.getUniqueId());
      _portfolioMaster.update(new PortfolioDocument(portfolio));
      return savedPosition.getTrades().get(0).getUniqueId();
    } else {
      position.addTrade(trade);
      position.setQuantity(position.getQuantity().add(trade.getQuantity()));
      ManageablePosition savedPosition = getPositionMaster().update(new PositionDocument(position)).getPosition();
      List<ManageableTrade> savedTrades = savedPosition.getTrades();
      return savedTrades.get(savedTrades.size() - 1).getUniqueId();
    }
  }

  /**
   * Updates a position directly. This is only allowed for positions with no trades. The position's size is changed
   * to match the quantity in the trade details and a single trade is created for the position.
   * @param tradeData Trade data for the position
   * @param positionId Unique ID of the position
   */
  /* package */ void updatePosition(BeanDataSource tradeData, UniqueId positionId) {
    ManageableTrade trade = buildTrade(tradeData);
    // TODO check if the ID is versioned?
    ManageablePosition position = getPositionMaster().get(positionId).getPosition();
    // TODO this is a temporary workaround for a client bug. not sure what the correct behaviour is yet
    trade.setUniqueId(null);
    /*if (!trade.getSecurityLink().equals(position.getSecurityLink())) {
      throw new IllegalArgumentException("Cannot update a position's security. new version " + trade.getSecurityLink() +
                                             ", previous version: " + position.getSecurityLink());
    }*/
    if (position.getTrades().size() != 0) {
      throw new IllegalArgumentException("Cannot directly update a position that contains trade. Update the trades");
    }
    position.setTrades(Lists.newArrayList(trade));
    position.setQuantity(trade.getQuantity());
    getPositionMaster().update(new PositionDocument(position)).getPosition();
  }

  // TODO would it make more sense to have a void return type? does the client use the returned ID?
  /* package */ UniqueId updateTrade(BeanDataSource tradeData) {
    ManageableTrade trade = buildTrade(tradeData);
    ManageableTrade previousTrade = getPositionMaster().getTrade(trade.getUniqueId());
    ManageablePosition position = getPositionMaster().get(previousTrade.getParentPositionId()).getPosition();
    if (!trade.getSecurityLink().equals(previousTrade.getSecurityLink())) {
      throw new IllegalArgumentException("Cannot update a trade's security. new version " + trade +
                                             ", previous version: " + previousTrade);
    }
    List<ManageableTrade> trades = Lists.newArrayList();
    for (ManageableTrade existingTrade : position.getTrades()) {
      if (existingTrade.getUniqueId().equals(trade.getUniqueId())) {
        trades.add(trade);
        position.setQuantity(position.getQuantity().subtract(existingTrade.getQuantity()).add(trade.getQuantity()));
      } else {
        trades.add(existingTrade);
      }
    }
    position.setTrades(trades);
    ManageablePosition savedPosition = getPositionMaster().update(new PositionDocument(position)).getPosition();
    ManageableTrade savedTrade = savedPosition.getTrade(trade.getUniqueId().getObjectId());
    if (savedTrade == null) {
      // shouldn't ever happen
      throw new DataNotFoundException("Failed to save trade " + trade + " to position " + savedPosition);
    } else {
      return savedTrade.getUniqueId();
    }
  }

  /**
   * Returns a position from a node in a security or null if there isn't one.
   * @param node A portfolio node
   * @param security The security
   * @return A position from the node in the security or null if there isn't one
   */
  private ManageablePosition findPosition(ManageablePortfolioNode node, Security security) {
    for (ObjectId positionId : node.getPositionIds()) {
      // TODO which version do I want? will LATEST do?
      PositionDocument document = getPositionMaster().get(positionId, VersionCorrection.LATEST);
      ManageablePosition position = document.getPosition();
      Security positionSecurity = position.getSecurityLink().resolve(_securitySource);
      if (positionSecurity.getExternalIdBundle().containsAny(security.getExternalIdBundle())) {
        return position;
      }
    }
    return null;
  }

  private ManageableTrade buildTrade(BeanDataSource tradeData) {
    if (!TRADE_TYPE_NAME.equals(tradeData.getBeanTypeName())) {
      throw new IllegalArgumentException("Can only build trades of type " + TRADE_TYPE_NAME +
                                             ", type name = " + tradeData.getBeanTypeName());
    }
    ManageableTrade.Meta meta = ManageableTrade.meta();
    BeanBuilder<? extends ManageableTrade> tradeBuilder =
        tradeBuilder(tradeData,
                     meta.uniqueId(),
                     meta.tradeDate(),
                     meta.tradeTime(),
                     meta.premium(),
                     meta.premiumCurrency(),
                     meta.premiumDate(),
                     meta.quantity(),
                     meta.premiumTime());
    tradeBuilder.set(meta.attributes(), tradeData.getMapValues(meta.attributes().name()));
    String idBundleStr = (String) tradeData.getValue(SECURITY_ID_BUNDLE);
    // TODO check the security exists and load it if not? and the underlying?
    ExternalIdBundle securityIdBundle = getStringConvert().convertFromString(ExternalIdBundle.class, idBundleStr);
    tradeBuilder.set(meta.securityLink(), new ManageableSecurityLink(securityIdBundle));
    // this property is done manually so the client can just provide the counterparty name but the counterparty
    // on the trade is an external ID with the standard counterparty scheme
    String counterparty = (String) tradeData.getValue(COUNTERPARTY);
    if (StringUtils.isEmpty(counterparty)) {
      counterparty = DEFAULT_COUNTERPARTY;
    }
    tradeBuilder.set(meta.counterpartyExternalId(), ExternalId.of(CPTY_SCHEME, counterparty));
    return tradeBuilder.build();
  }
  /**
   * Creates a builder for a {@link ManageableTrade} and sets the simple properties from the data source.
   * @param tradeData The trade data
   * @param properties The trade properties to set
   * @return A builder with property values set from the trade data
   */
  private BeanBuilder<? extends ManageableTrade> tradeBuilder(BeanDataSource tradeData, MetaProperty<?>... properties) {
    BeanBuilder<? extends ManageableTrade> builder = ManageableTrade.meta().builder();
    for (MetaProperty<?> property : properties) {
      builder.set(property, getStringConvert().convertFromString(property.propertyType(), (String) tradeData.getValue(property.name())));
    }
    return builder;
  }

  // TODO different versions for OTC / non OTC
  // the horror... make this go away
  /* package */ static Map<String, Object> tradeStructure() {
    Map<String, Object> structure = Maps.newHashMap();
    List<Map<String, Object>> properties = Lists.newArrayList();
    properties.add(property("uniqueId", true, true, typeInfo("string", "UniqueId")));
    properties.add(property("quantity", true, false, typeInfo("number", "")));
    properties.add(property("counterparty", false, false, typeInfo("string", "")));
    properties.add(property("tradeDate", true, false, typeInfo("string", "LocalDate")));
    properties.add(property("tradeTime", true, false, typeInfo("string", "OffsetTime")));
    properties.add(property("premium", true, false, typeInfo("number", "")));
    properties.add(property("premiumCurrency", true, false, typeInfo("string", "Currency")));
    properties.add(property("premiumDate", true, false, typeInfo("string", "LocalDate")));
    properties.add(property("premiumTime", true, false, typeInfo("string", "OffsetTime")));
    properties.add(property("securityIdBundle", true, false, typeInfo("string", "ExternalIdBundle")));
    properties.add(attributesProperty());
    structure.put("type", TRADE_TYPE_NAME);
    structure.put("properties", properties);
    structure.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    return structure;
  }
}
