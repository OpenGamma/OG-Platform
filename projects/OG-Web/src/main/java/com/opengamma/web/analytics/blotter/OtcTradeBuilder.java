/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.convert.StringConvert;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.OpenGammaClock;

/**
 * Builds and saves trades, securities and underlying securities for OTC securities.
 */
/* package */ class OtcTradeBuilder extends AbstractTradeBuilder {

  /** Type name for OTC trades used in the data sent to the client. */
  /* package */ static final String TRADE_TYPE_NAME = "OtcTrade";

  /**
   * For traversing trade and security {@link MetaBean}s and building instances from the data sent from the blotter.
   * The security type name is filtered out because it is a read-only property. The external ID bundle is filtered
   * out because it is always empty for trades and securities entered via the blotter but isn't nullable. Therefore
   * it has to be explicitly set to an empty bundle after the client data is processed but before the object is built.
   */
  private static final BeanTraverser s_beanTraverser = new BeanTraverser(
      new PropertyFilter(FinancialSecurity.meta().externalIdBundle()),
      new PropertyFilter(ManageableSecurity.meta().securityType()));

  /* package */ OtcTradeBuilder(PositionMaster positionMaster,
                                PortfolioMaster portfoioMaster,
                                SecurityMaster securityMaster,
                                Set<MetaBean> metaBeans,
                                StringConvert stringConvert) {
    super(positionMaster, portfoioMaster, securityMaster, metaBeans, stringConvert);
  }

  /* package */ UniqueId addTrade(BeanDataSource tradeData,
                                  BeanDataSource securityData,
                                  BeanDataSource underlyingData,
                                  UniqueId nodeId) {
    /*
    validate:
      underlying is present
      underlying type is correct
    */
    ManageableSecurity underlying = buildUnderlying(underlyingData);
    ManageableSecurity savedUnderlying;
    if (underlying == null) {
      savedUnderlying = null;
    } else {
      savedUnderlying = getSecurityMaster().add(new SecurityDocument(underlying)).getSecurity();
    }
    ManageableSecurity security = buildSecurity(securityData, savedUnderlying);
    ManageableSecurity savedSecurity = getSecurityMaster().add(new SecurityDocument(security)).getSecurity();
    ManageableTrade trade = buildTrade(tradeData, savedSecurity);
    ManageablePosition position = new ManageablePosition(BigDecimal.ONE, trade.getSecurityLink().getExternalId());
    position.setTrades(Lists.newArrayList(trade));
    ManageablePosition savedPosition = getPositionMaster().add(new PositionDocument(position)).getPosition();
    ManageableTrade savedTrade = savedPosition.getTrades().get(0);

    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.addNodeObjectId(nodeId.getObjectId());
    PortfolioSearchResult searchResult = getPortfolioMaster().search(searchRequest);
    ManageablePortfolio portfolio = searchResult.getSinglePortfolio();
    ManageablePortfolioNode node = findNode(portfolio.getRootNode(), nodeId);
    node.addPosition(savedPosition.getUniqueId());
    getPortfolioMaster().update(new PortfolioDocument(portfolio));
    return savedTrade.getUniqueId();
  }

  /* package */ UniqueId updateTrade(BeanDataSource tradeData,
                                     BeanDataSource securityData,
                                     BeanDataSource underlyingData) {
    if (!TRADE_TYPE_NAME.equals(tradeData.getBeanTypeName())) {
      throw new IllegalArgumentException("Can only build trades of type " + TRADE_TYPE_NAME +
                                             ", type name = " + tradeData.getBeanTypeName());
    }
    /*
    validate:
      underlying is present
      underlying type is correct
      trade's security type hasn't changed
    */
    ManageableSecurity underlying = buildUnderlying(underlyingData);
    ManageableSecurity savedUnderlying;
    if (underlying == null) {
      savedUnderlying = null;
    } else {
      savedUnderlying = getSecurityMaster().update(new SecurityDocument(underlying)).getSecurity();
    }
    ManageableSecurity security = buildSecurity(securityData, savedUnderlying);
    ManageableSecurity savedSecurity = getSecurityMaster().update(new SecurityDocument(security)).getSecurity();
    ManageableTrade trade = buildTrade(tradeData, savedSecurity);
    PositionSearchRequest searchRequest = new PositionSearchRequest();
    searchRequest.addTradeObjectId(trade.getUniqueId());
    PositionSearchResult searchResult = getPositionMaster().search(searchRequest);
    ManageablePosition position = searchResult.getSinglePosition();
    // TODO validation
    position.setTrades(Lists.newArrayList(trade));
    ManageablePosition savedPosition = getPositionMaster().update(new PositionDocument(position)).getPosition();
    ManageableTrade savedTrade = savedPosition.getTrades().get(0);
    return savedTrade.getUniqueId();
  }

  private ManageableTrade buildTrade(BeanDataSource tradeData, Security security) {
    ManageableTrade.Meta meta = ManageableTrade.meta();
    BeanBuilder<? extends ManageableTrade> tradeBuilder =
        tradeBuilder(tradeData,
                     meta.uniqueId(),
                     meta.tradeDate(),
                     meta.tradeTime(),
                     meta.premium(),
                     meta.premiumCurrency(),
                     meta.premiumDate(),
                     meta.premiumTime());
    tradeBuilder.set(meta.attributes(), tradeData.getMapValues(meta.attributes().name()));
    tradeBuilder.set(meta.quantity(), BigDecimal.ONE);
    tradeBuilder.set(meta.securityLink(), new ManageableSecurityLink(security.getUniqueId().getObjectId()));
    String counterparty = (String) tradeData.getValue(COUNTERPARTY);
    if (StringUtils.isEmpty(counterparty)) {
      throw new IllegalArgumentException("Trade counterparty is required");
    }
    tradeBuilder.set(meta.counterpartyExternalId(), ExternalId.of(CPTY_SCHEME, counterparty));
    return tradeBuilder.build();
  }

  // TODO move these to a separate class that only extracts data, also handles securities and underlyings
  /**
   * Extracts trade data and populates a data sink.
   * @param trade The trade
   * @param sink The sink that should be populated with the trade data
   */
  /* package */ static void extractTradeData(ManageableTrade trade, BeanDataSink<?> sink) {
    sink.setValue("type", TRADE_TYPE_NAME);
    extractPropertyData(trade.uniqueId(), sink);
    extractPropertyData(trade.tradeDate(), sink);
    extractPropertyData(trade.tradeTime(), sink);
    extractPropertyData(trade.premium(), sink);
    extractPropertyData(trade.premiumCurrency(), sink);
    extractPropertyData(trade.premiumDate(), sink);
    extractPropertyData(trade.premiumTime(), sink);
    sink.setMap(trade.attributes().name(), trade.getAttributes());
    sink.setValue(COUNTERPARTY, trade.getCounterpartyExternalId().getValue());
  }

  private static void extractPropertyData(Property<?> property, BeanDataSink<?> sink) {
    sink.setValue(property.name(), property.metaProperty().getString(property.bean()));
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
      builder.setString(property, (String) tradeData.getValue(property.name()));
    }
    return builder;
  }

  private FinancialSecurity buildSecurity(BeanDataSource securityData, Security underlying) {
    BeanDataSource dataSource;
    ExternalId underlyingId = getUnderlyingId(underlying);
    // TODO would it be better to just return the bean builder from the visitor and handle this property manually?
    // TODO would have to use a different property for every security with underlyingId, there's no common supertype with it
    if (underlyingId != null) {
      dataSource = new PropertyReplacingDataSource(securityData, "underlyingId", underlyingId.toString());
    } else {
      dataSource = securityData;
    }
    FinancialSecurity security = build(dataSource);
    // TODO check underlying is of the correct type
    return security;
  }

  private FinancialSecurity buildUnderlying(BeanDataSource underlyingData) {
    if (underlyingData == null) {
      return null;
    }
    return build(underlyingData);
  }

  private ExternalId getUnderlyingId(Security underlying) {
    ExternalId underlyingId;
    if (underlying instanceof FinancialSecurity) {
      underlyingId = ((FinancialSecurity) underlying).accept(new ExternalIdVisitor(getSecurityMaster()));
    } else {
      underlyingId = null;
    }
    return underlyingId;
  }

  @SuppressWarnings("unchecked")
  private FinancialSecurity build(BeanDataSource data) {
    // TODO custom converters for dates
    // default timezone based on OpenGammaClock
    // default times for effectiveDate and maturityDate properties on SwapSecurity, probably others
    BeanVisitor<BeanBuilder<FinancialSecurity>> visitor =
        new BeanBuildingVisitor<>(data, getMetaBeanFactory(), getStringConvert());
    MetaBean metaBean = getMetaBeanFactory().beanFor(data);
    // TODO check it's a FinancialSecurity metaBean
    if (!(metaBean instanceof FinancialSecurity.Meta)) {
      throw new IllegalArgumentException("MetaBean " + metaBean + " isn't for a FinancialSecurity");
    }
    BeanBuilder<FinancialSecurity> builder = (BeanBuilder<FinancialSecurity>) s_beanTraverser.traverse(metaBean, visitor);
    // externalIdBundle needs to be specified or building fails because it's not nullable
    // TODO need to preserve the bundle when editing existing trades. pass to client or use previous version?
    // do in Existing* subclass, that looks up previous version, other subclass doesn't care, no bundle for new trades
    builder.set(FinancialSecurity.meta().externalIdBundle(), ExternalIdBundle.EMPTY);
    Object bean = builder.build();
    if (bean instanceof FinancialSecurity) {
      return (FinancialSecurity) bean;
    } else {
      throw new IllegalArgumentException("object type " + bean.getClass().getName() + " isn't a Financial Security");
    }
  }

  // TODO different versions for OTC / non OTC
  // the horror... make this go away TODO move to the TradeBuilers? they create the trades
  /* package */ static Map<String, Object> tradeStructure() {
    Map<String, Object> structure = Maps.newHashMap();
    List<Map<String, Object>> properties = Lists.newArrayList();
    properties.add(property("uniqueId", true, true, typeInfo("string", "UniqueId")));
    properties.add(property("counterparty", false, false, typeInfo("string", "")));
    properties.add(property("tradeDate", true, false, typeInfo("string", "LocalDate")));
    properties.add(property("tradeTime", true, false, typeInfo("string", "OffsetTime")));
    properties.add(property("premium", true, false, typeInfo("number", "")));
    properties.add(property("premiumCurrency", true, false, typeInfo("string", "Currency")));
    properties.add(property("premiumDate", true, false, typeInfo("string", "LocalDate")));
    properties.add(property("premiumTime", true, false, typeInfo("string", "OffsetTime")));
    properties.add(attributesProperty());
    structure.put("type", TRADE_TYPE_NAME);
    structure.put("properties", properties);
    structure.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    return structure;
  }
}
