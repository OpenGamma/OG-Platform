/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.convert.StringConverter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.OpenGammaClock;

/**
 * TODO factor out common code into an abstract superclass shared with OtcTradeBuilder?
 */
/* package */ abstract class FungibleTradeBuilder {

  // TODO where should these live? currently they're duplicated in OtcTradeBuilder
  private static final ExternalScheme CPTY_SCHEME = ExternalScheme.of("Cpty");
  private static final String COUNTERPARTY = "counterparty";
  private static final String SECURITY_ID_BUNDLE = "securityIdBundle";

  /**
   * Extracts trade data and populates a data sink.
   * @param trade The trade
   * @param sink The sink that should be populated with the trade data
   */
  /* package */ static void extractTradeData(ManageableTrade trade, BeanDataSink<?> sink) {
    sink.setBeanData(trade.metaBean(), trade);
    // TODO extract fields into list, use when building trade
    extractPropertyData(trade.uniqueId(), sink);
    extractPropertyData(trade.tradeDate(), sink);
    extractPropertyData(trade.tradeTime(), sink);
    extractPropertyData(trade.premium(), sink);
    extractPropertyData(trade.premiumCurrency(), sink);
    extractPropertyData(trade.premiumDate(), sink);
    extractPropertyData(trade.premiumTime(), sink);
    extractPropertyData(trade.quantity(), sink);
    sink.setMapValues(trade.attributes().name(), trade.getAttributes());
    sink.setValue(COUNTERPARTY, trade.getCounterpartyExternalId().getValue());
    ExternalIdBundle securityIdBundle = trade.getSecurityLink().getExternalId();
    StringConverter<ExternalIdBundle> converter = JodaBeanUtils.stringConverter().findConverter(ExternalIdBundle.class);
    // TODO this needs to be in the green screen showing the trade fields
    sink.setValue(SECURITY_ID_BUNDLE, converter.convertToString(securityIdBundle));
  }

  private static void extractPropertyData(Property<?> property, BeanDataSink<?> sink) {
    sink.setValue(property.name(), property.metaProperty().getString(property.bean()));
  }
  /* package */ UniqueId buildAndSaveTrade(BeanDataSource tradeData) {
    ManageableTrade.Meta meta = ManageableTrade.meta();
    BeanBuilder<? extends ManageableTrade> tradeBuilder =
        tradeBuilder(tradeData,
                     meta.uniqueId(), // TODO handle uniqueId differently for new trades - shouldn't be specified
                     meta.tradeDate(),
                     meta.tradeTime(),
                     meta.premium(),
                     meta.premiumCurrency(),
                     meta.premiumDate(),
                     meta.quantity(),
                     meta.premiumTime());
    tradeBuilder.set(meta.attributes(), tradeData.getMapValues(meta.attributes().name()));
    StringConverter<ExternalIdBundle> idBundleConverter =
        JodaBeanUtils.stringConverter().findConverter(ExternalIdBundle.class);
    String idBundleStr = tradeData.getValue(SECURITY_ID_BUNDLE);
    ExternalIdBundle securityIdBundle = idBundleConverter.convertFromString(ExternalIdBundle.class,idBundleStr);
    tradeBuilder.set(meta.securityLink(), new ManageableSecurityLink(securityIdBundle));
    String counterparty = tradeData.getValue(COUNTERPARTY);
    if (StringUtils.isEmpty(counterparty)) {
      throw new IllegalArgumentException("Trade counterparty is required");
    }
    tradeBuilder.set(meta.counterpartyExternalId(), ExternalId.of(CPTY_SCHEME, counterparty));
    ManageableTrade trade = tradeBuilder.build();
    // TODO need the node ID so we can add the position to the portfolio node
    ManageablePosition position = getPosition(trade);
    // TODO check the security exists and load it if not?
    position.setSecurityLink(new ManageableSecurityLink(securityIdBundle));
    position.addTrade(trade);
    position.setQuantity(trade.getQuantity());
    ManageablePosition savedPosition = savePosition(position);
    List<ManageableTrade> trades = savedPosition.getTrades();
    ManageableTrade savedTrade = trades.get(0);
    return savedTrade.getUniqueId();
  }

  /**
   * Saves a position to the position master.
   * @param position The position
   * @return The saved position
   */
  /* package */ abstract ManageablePosition savePosition(ManageablePosition position);

  // for existing trades need to remove from the position and adjust the quantity
  // need the node ID? if there's an existing position in the security need to add to that, otherwise create new
  /* package */ abstract ManageablePosition getPosition(ManageableTrade trade);

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

  // TODO different versions for OTC / non OTC
  // the horror... make this go away TODO move to the TradeBuilers? they create the trades
  static Map<String, Object> tradeStructure() {
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
    structure.put("type", "FungibleTrade");
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
