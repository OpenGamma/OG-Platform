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
import com.opengamma.core.security.SecurityLink;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
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
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 * Builds and saves trades, securities and underlying securities for OTC securities.
 * TODO the use of VersionCorrection.LATEST in this class is incorrect
 * the weak link between trades, positions and securities is a problem for OTCs because in reality they're a single
 * atomic object. the problem at the moment is there's no way to know which version of the security is being modified
 * given the unique ID of the trade. therefore it's not possible to detect any concurrent modification of securities,
 * the last update wins. there are various potential fixes but it might not be worth doing before the imminent refactor
 * of trades, positions and securities.
 */
/* package */ class OtcTradeBuilder extends AbstractTradeBuilder {

  /** Type name for OTC trades used in the data sent to the client. */
  /* package */ static final String TRADE_TYPE_NAME = "OtcTrade";

  /* package */ OtcTradeBuilder(PositionMaster positionMaster,
                                PortfolioMaster portfoioMaster,
                                SecurityMaster securityMaster,
                                Set<MetaBean> metaBeans,
                                StringConvert stringConvert) {
    super(positionMaster, portfoioMaster, securityMaster, metaBeans, stringConvert);
  }

  @SuppressWarnings("deprecation")
  UniqueId addTrade(BeanDataSource tradeData,
                                  BeanDataSource securityData,
                                  BeanDataSource underlyingData,
                                  UniqueId nodeId) {
    /*
    validate:
      underlying is present
      underlying type is correct
    */
    ManageableSecurity underlying = buildUnderlying(underlyingData);
    ManageableSecurity security;
    if (underlying == null) {
      security = BlotterUtils.buildSecurity(securityData);
    } else {
      ManageableSecurity savedUnderlying = getSecurityMaster().add(new SecurityDocument(underlying)).getSecurity();
      security = buildSecurity(securityData, savedUnderlying);
    }
    ManageableSecurity savedSecurity = getSecurityMaster().add(new SecurityDocument(security)).getSecurity();
    ManageableTrade trade = buildTrade(tradeData);
    trade.setSecurityLink(new ManageableSecurityLink(savedSecurity.getUniqueId()));
    ManageablePosition position = new ManageablePosition();
    position.setQuantity(BigDecimal.ONE);
    position.setSecurityLink(new ManageableSecurityLink(trade.getSecurityLink()));
    position.setTrades(Lists.newArrayList(trade));
    ManageablePosition savedPosition = getPositionMaster().add(new PositionDocument(position)).getPosition();
    ManageableTrade savedTrade = savedPosition.getTrades().get(0);

    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.addNodeObjectId(nodeId.getObjectId());
    PortfolioSearchResult searchResult = getPortfolioMaster().search(searchRequest);
    ManageablePortfolio portfolio = searchResult.getSinglePortfolio();
    ManageablePortfolioNode node = findNode(portfolio, nodeId);
    node.addPosition(savedPosition.getUniqueId());
    getPortfolioMaster().update(new PortfolioDocument(portfolio));
    return savedTrade.getUniqueId();
  }

  /* package */ UniqueId updatePosition(UniqueId positionId,
                                        BeanDataSource tradeData,
                                        BeanDataSource securityData,
                                        BeanDataSource underlyingData) {
    ManageableTrade trade = buildTrade(tradeData);
    ManageablePosition position = getPositionMaster().get(positionId).getPosition();
    ManageableSecurity previousSecurity = loadSecurity(position.getSecurityLink());
    return updateSecuritiesAndPosition(securityData, underlyingData, trade, previousSecurity, positionId);
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
      security type hasn't changed
      trade ID is versioned
    */
    ManageableTrade trade = buildTrade(tradeData);
    ManageableTrade previousTrade = getPositionMaster().getTrade(trade.getUniqueId());
    ManageableSecurity previousSecurity = loadSecurity(previousTrade.getSecurityLink());
    UniqueId previousPositionId = previousTrade.getParentPositionId();
    return updateSecuritiesAndPosition(securityData, underlyingData, trade, previousSecurity, previousPositionId);
  }

  private ManageableSecurity loadSecurity(SecurityLink securityLink) {
    if (securityLink.getObjectId() != null) {
      return getSecurityMaster().get(securityLink.getObjectId(), VersionCorrection.LATEST).getSecurity();
    } else if (securityLink.getExternalId() != null) {
      ExternalIdBundle idBundle = securityLink.getExternalId();
      SecuritySearchResult searchResult = getSecurityMaster().search(new SecuritySearchRequest(idBundle));
      if (searchResult.getSecurities().isEmpty()) {
        throw new IllegalArgumentException("No security found for ID bundle " + idBundle);
      }
      return searchResult.getFirstSecurity();
    } else {
      throw new IllegalArgumentException("No IDs in security link " + securityLink);
    }
  }

  @SuppressWarnings("deprecation")
  private UniqueId updateSecuritiesAndPosition(BeanDataSource securityData,
                                               BeanDataSource underlyingData,
                                               ManageableTrade trade,
                                               ManageableSecurity previousSecurity,
                                               UniqueId positionId) {
    // need the previous underlying so we don't lose the ID bundle, the data doesn't contain it
    ExternalIdBundle previousUnderlyingIdBundle;
    if (previousSecurity instanceof FinancialSecurity) {
      UnderlyingSecurityVisitor visitor = new UnderlyingSecurityVisitor(VersionCorrection.LATEST, getSecurityMaster());
      ManageableSecurity previousUnderlying = ((FinancialSecurity) previousSecurity).accept(visitor);
      if (previousUnderlying != null) {
        previousUnderlyingIdBundle = previousUnderlying.getExternalIdBundle();
      } else {
        previousUnderlyingIdBundle = ExternalIdBundle.EMPTY;
      }
    } else {
      previousUnderlyingIdBundle = ExternalIdBundle.EMPTY;
    }
    ManageableSecurity underlying = buildUnderlying(underlyingData, previousUnderlyingIdBundle);
    ManageableSecurity security;
    if (underlying == null) {
      security = BlotterUtils.buildSecurity(securityData, previousSecurity.getExternalIdBundle());
    } else {
      // need to set the unique ID to the ID from the previous version, securities aren't allowed to change
      // any changes in the security data are interpreted as edits to the security
      ManageableSecurity previousUnderlying = getUnderlyingSecurity(previousSecurity, VersionCorrection.LATEST);
      validateSecurity(underlying, previousUnderlying);
      underlying.setUniqueId(previousUnderlying.getUniqueId());
      ManageableSecurity savedUnderlying = getSecurityMaster().update(new SecurityDocument(underlying)).getSecurity();
      security = buildSecurity(securityData, savedUnderlying, previousSecurity.getExternalIdBundle());
    }
    // need to set the unique ID to the ID from the previous version, securities aren't allowed to change
    // any changes in the security data are interpreted as edits to the security
    validateSecurity(security, previousSecurity);
    security.setUniqueId(previousSecurity.getUniqueId());
    ManageableSecurity savedSecurity = getSecurityMaster().update(new SecurityDocument(security)).getSecurity();
    trade.setSecurityLink(new ManageableSecurityLink(savedSecurity.getUniqueId()));
    ManageablePosition position = getPositionMaster().get(positionId).getPosition();
    position.setTrades(Lists.newArrayList(trade));
    ManageablePosition savedPosition = getPositionMaster().update(new PositionDocument(position)).getPosition();
    ManageableTrade savedTrade = savedPosition.getTrades().get(0);
    return savedTrade.getUniqueId();
  }

  private ManageableSecurity getUnderlyingSecurity(ManageableSecurity security, VersionCorrection versionCorrection) {
    if (security instanceof FinancialSecurity) {
      UnderlyingSecurityVisitor visitor = new UnderlyingSecurityVisitor(versionCorrection, getSecurityMaster());
      return ((FinancialSecurity) security).accept(visitor);
    } else {
      return null;
    }
  }

  /**
   * Checks that the new and old versions of a security have the same type and if the new version specifies an ID
   * it is the same as the old ID.
   * @param newVersion The new version of the security
   * @param previousVersion The previous version of the security
   */
  private static void validateSecurity(ManageableSecurity newVersion, ManageableSecurity previousVersion) {
    if (!newVersion.getClass().equals(previousVersion.getClass())) {
      throw new IllegalArgumentException("Security type cannot change, new version " + newVersion + ", " +
                                             "previousVersion: " + previousVersion);
    }
    // TODO this should check for equality between the IDs but that's not working ATM
    // needs to be part of the bigger fix for the problem caused by the weak links between the different parts
    // of OTC trades
    if (newVersion.getUniqueId() != null && !newVersion.getUniqueId().equalObjectId(previousVersion.getUniqueId())) {
      throw new IllegalArgumentException("Cannot update a security with a different ID, " +
                                             "new ID: " + newVersion.getUniqueId() + ", " +
                                             "previous ID: " + previousVersion.getUniqueId());
    }
  }

  private ManageableTrade buildTrade(BeanDataSource tradeData) {
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
    // the link needs to be non-null but the real ID can't be set until the security has been created later
    tradeBuilder.set(meta.securityLink(), new ManageableSecurityLink());
    String counterparty = (String) tradeData.getValue(COUNTERPARTY);
    if (StringUtils.isEmpty(counterparty)) {
      counterparty = DEFAULT_COUNTERPARTY;
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
  /* package */ void extractTradeData(ManageableTrade trade, BeanDataSink<?> sink) {
    sink.setValue("type", TRADE_TYPE_NAME);
    extractPropertyData(trade.uniqueId(), sink);
    extractPropertyData(trade.tradeDate(), sink);
    extractPropertyData(trade.tradeTime(), sink);
    extractPropertyData(trade.premium(), sink);
    extractPropertyData(trade.premiumCurrency(), sink);
    extractPropertyData(trade.premiumDate(), sink);
    extractPropertyData(trade.premiumTime(), sink);
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
  }

  private void extractPropertyData(Property<?> property, BeanDataSink<?> sink) {
    sink.setValue(property.name(), getStringConvert().convertToString(property.metaProperty().get(property.bean())));
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
      builder.set(property, getStringConvert().convertFromString(property.propertyType(),
                                                                 (String) tradeData.getValue(property.name())));
    }
    return builder;
  }

  private FinancialSecurity buildSecurity(BeanDataSource securityData, Security underlying) {
    return buildSecurity(securityData, underlying, ExternalIdBundle.EMPTY);
  }

  private FinancialSecurity buildSecurity(BeanDataSource securityData, Security underlying, ExternalIdBundle idBundle) {
    ArgumentChecker.notNull(underlying, "underlying");
    BeanDataSource dataSource;
    ExternalId underlyingId = getUnderlyingId(underlying);
    // TODO would it be better to just return the bean builder from the visitor and handle this property manually?
    // TODO would have to use a different property for every security with underlyingId, there's no common supertype with it
    if (underlyingId == null) {
      throw new IllegalArgumentException("Unable to get underlying ID for security " + underlying);
    }
    // TODO I'm not keen on this, it doesn't smell great
    dataSource = new PropertyReplacingDataSource(securityData, "underlyingId", underlyingId.toString());
    return BlotterUtils.buildSecurity(dataSource, idBundle);
  }

  private FinancialSecurity buildUnderlying(BeanDataSource underlyingData) {
    return buildUnderlying(underlyingData, ExternalIdBundle.EMPTY);
  }

  private FinancialSecurity buildUnderlying(BeanDataSource underlyingData, ExternalIdBundle idBundle) {
    if (underlyingData == null) {
      return null;
    }
    return BlotterUtils.buildSecurity(underlyingData, idBundle);
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
