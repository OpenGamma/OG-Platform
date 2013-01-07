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

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 *
 */
/* package */ abstract class OtcTradeBuilder {

  // TODO where should these live?
  private static final ExternalScheme CPTY_SCHEME = ExternalScheme.of("Cpty");
  private static final String COUNTERPARTY = "counterparty";

  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final MetaBeanFactory _metaBeanFactory;

  /* package */ OtcTradeBuilder(SecurityMaster securityMaster, PositionMaster positionMaster, Set<MetaBean> metaBeans) {
    ArgumentChecker.notNull(securityMaster, "securityManager");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notEmpty(metaBeans, "metaBeans");
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    _metaBeanFactory = new MapMetaBeanFactory(metaBeans);
  }

  /* package */ UniqueId buildAndSaveTrade(BeanDataSource tradeData,
                                           BeanDataSource securityData,
                                           BeanDataSource underlyingData) {
    // TODO check the type name is OtcTrade
    ObjectId securityId = buildSecurity(securityData, underlyingData).getObjectId();
    ManageableTrade.Meta meta = ManageableTrade.meta();
    BeanBuilder<? extends ManageableTrade> tradeBuilder =
        tradeBuilder(tradeData,
                     meta.uniqueId(), // TODO handle uniqueId differently for new trades - shouldn't be specified
                     meta.tradeDate(),
                     meta.tradeTime(),
                     meta.premium(),
                     meta.premiumCurrency(),
                     meta.premiumDate(),
                     meta.premiumTime());
    tradeBuilder.set(meta.attributes(), tradeData.getMapValues(meta.attributes().name()));
    tradeBuilder.set(meta.quantity(), BigDecimal.ONE);
    tradeBuilder.set(meta.securityLink(), new ManageableSecurityLink(securityId));
    String counterparty = tradeData.getValue(COUNTERPARTY);
    if (StringUtils.isEmpty(counterparty)) {
      throw new IllegalArgumentException("Trade counterparty is required");
    }
    tradeBuilder.set(meta.counterpartyExternalId(), ExternalId.of(CPTY_SCHEME, counterparty));
    ManageableTrade trade = tradeBuilder.build();
    // TODO need the node ID so we can add the position to the portfolio node
    ManageablePosition position = getPosition(trade);
    position.setSecurityLink(new ManageableSecurityLink(securityId));
    position.addTrade(trade);
    position.setQuantity(trade.getQuantity());
    ManageablePosition savedPosition = savePosition(position);
    List<ManageableTrade> trades = savedPosition.getTrades();
    ManageableTrade savedTrade = trades.get(0);
    return savedTrade.getUniqueId();
  }

  // TODO move these to a separate class that only extracts data, also handles securities and underlyings
  /**
   * Extracts trade data and populates a data sink.
   * @param trade The trade
   * @param sink The sink that should be populated with the trade data
   */
  /* package */ static void extractTradeData(ManageableTrade trade, BeanDataSink<?> sink) {
    sink.setBeanData(trade.metaBean(), trade);
    extractPropertyData(trade.uniqueId(), sink);
    extractPropertyData(trade.tradeDate(), sink);
    extractPropertyData(trade.tradeTime(), sink);
    extractPropertyData(trade.premium(), sink);
    extractPropertyData(trade.premiumCurrency(), sink);
    extractPropertyData(trade.premiumDate(), sink);
    extractPropertyData(trade.premiumTime(), sink);
    sink.setMapValues(trade.attributes().name(), trade.getAttributes());
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
      // TODO custom converters needed for some properties? OffsetDate?
      builder.setString(property, tradeData.getValue(property.name()));
    }
    return builder;
  }

  /**
   * Saves a security to the security master.
   * @param security The security
   * @return The saved security
   */
  /* package */ abstract ManageableSecurity saveSecurity(ManageableSecurity security);

  /**
   * Saves a position to the position master.
   * @param position The position
   * @return The saved position
   */
  /* package */ abstract ManageablePosition savePosition(ManageablePosition position);

  /* package */ abstract ManageablePosition getPosition(ManageableTrade trade);

  private UniqueId buildSecurity(BeanDataSource securityData, BeanDataSource underlyingData) {
    ExternalId underlyingId = buildUnderlying(underlyingData);
    BeanDataSource dataSource;
    // TODO would it be better to just return the bean builder from the visitor and handle this property manually?
    if (underlyingId != null) {
      dataSource = new PropertyReplacingDataSource(securityData, "underlyingId", underlyingId.toString());
    } else {
      dataSource = securityData;
    }
    FinancialSecurity security = build(dataSource, FinancialSecurity.class);
    ManageableSecurity savedSecurity = saveSecurity(security);
    return savedSecurity.getUniqueId();
  }

  private ExternalId buildUnderlying(BeanDataSource underlyingData) {
    if (underlyingData == null) {
      return null;
    }
    FinancialSecurity underlying = build(underlyingData, FinancialSecurity.class);
    saveSecurity(underlying);
    ExternalId underlyingId = underlying.accept(new ExternalIdVisitor(_securityMaster));
    if (underlyingId == null) {
      throw new IllegalArgumentException("Unable to get external ID of underlying security " + underlying);
    }
    return underlyingId;
  }

  @SuppressWarnings("unchecked")
  private <T extends Bean> T build(BeanDataSource data, Class<T> expectedType) {
    // TODO custom converters for dates
    // default timezone based on OpenGammaClock
    // default times for effectiveDate and maturityDate properties on SwapSecurity, probably others
    // TODO decorator to filter out trade properties
    BeanVisitor<Bean> visitor = new BeanBuildingVisitor<Bean>(data, _metaBeanFactory);
    MetaBean metaBean = _metaBeanFactory.beanFor(data);
    // TODO externalIdBundle needs to be specified or building fails because it's null
    // we never want to set it. BeanBuildingVisitor should return the bean builder so we can set an empty ID bundle
    // and then call build(). we don't need to support externalIdBundle so can always set it to be EMPTY
    Object bean = new BeanTraverser().traverse(metaBean, visitor);
    if (!expectedType.isAssignableFrom(bean.getClass())) {
      throw new IllegalArgumentException("object type " + bean.getClass().getName() + " doesn't conform to " +
                                             "the expected type " + expectedType.getName());
    }
    return expectedType.cast(bean);
  }

  /* package */  SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /* package */ PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  // TODO different versions for OTC / non OTC
  // the horror... make this go away TODO move to the TradeBuilers? they create the trades
  static Map<String, Object> tradeStructure() {
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
    structure.put("type", "OtcTrade"); // TODO different type name for OTC and fungible
    structure.put("properties", properties);
    structure.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    return structure;
  }

  private static Map<String, Object> property(String name,
                                              boolean optional,
                                              boolean readOnly,
                                              Map<String, Object> typeInfo) {
    return ImmutableMap.<String, Object>of("name", name,
                                           "type", "single",
                                           "optional", optional,
                                           "readOnly", readOnly,
                                           "types", ImmutableList.of(typeInfo));
  }

  private static Map<String, Object> attributesProperty() {
    Map<String, Object> map = Maps.newHashMap();
    map.put("name", "attributes");
    map.put("type", "map");
    map.put("optional", true); // can't be null but have a default value so client doesn't need to specify
    map.put("readOnly", false);
    map.put("types", ImmutableList.of(typeInfo("string", "")));
    map.put("valueTypes", ImmutableList.of(typeInfo("string", "")));
    return map;
  }

  private static Map<String, Object> typeInfo(String expectedType, String actualType) {
    return ImmutableMap.<String, Object>of("beanType", false, "expectedType", expectedType, "actualType", actualType);
  }
}
