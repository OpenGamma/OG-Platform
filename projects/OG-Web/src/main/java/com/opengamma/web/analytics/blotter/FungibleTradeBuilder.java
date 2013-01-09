/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.convert.StringConverter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.OpenGammaClock;

/**
 * TODO factor out common code into an abstract superclass shared with OtcTradeBuilder?
 */
/* package */ abstract class FungibleTradeBuilder extends AbstractTradeBuilder {

  /* package */ static String TRADE_TYPE_NAME = "FungibleTrade";

  private static final String SECURITY_ID_BUNDLE = "securityIdBundle";

  /* package */ FungibleTradeBuilder(PositionMaster positionMaster,
                                     SecurityMaster securityMaster,
                                     Set<MetaBean> metaBeans) {
    super(positionMaster, securityMaster, metaBeans);
  }

  /**
   * Extracts trade data and populates a data sink.
   * @param trade The trade
   * @param sink The sink that should be populated with the trade data
   */
  /* package */ static void extractTradeData(ManageableTrade trade, BeanDataSink<?> sink) {
    // TODO this uses the SimpleName, need to override
    //sink.setBeanData(trade.metaBean(), trade);
    sink.setValue("type", TRADE_TYPE_NAME);
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
    sink.setValue(SECURITY_ID_BUNDLE, converter.convertToString(securityIdBundle));
  }

  private static void extractPropertyData(Property<?> property, BeanDataSink<?> sink) {
    sink.setValue(property.name(), property.metaProperty().getString(property.bean()));
  }
  /* package */ UniqueId buildAndSaveTrade(BeanDataSource tradeData) {
    if (!TRADE_TYPE_NAME.equals(tradeData.getBeanTypeName())) {
      throw new IllegalArgumentException("Can only build trades of type " + TRADE_TYPE_NAME +
                                             ", type name = " + tradeData.getBeanTypeName());
    }
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
    // TODO should there be checks to stop a trade's type changing by pointing to a different security type?
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
    position.setQuantity(trade.getQuantity().add(position.getQuantity()));
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
  // the horror... make this go away
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
    structure.put("type", TRADE_TYPE_NAME);
    structure.put("properties", properties);
    structure.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    return structure;
  }
}
