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
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.convert.StringConvert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.OpenGammaClock;

/**
 *
 */
/* package */ abstract class OtcTradeBuilder extends AbstractTradeBuilder {

  public static final String TRADE_TYPE_NAME = "OtcTrade";

  /* package */ OtcTradeBuilder(SecurityMaster securityMaster,
                                PositionMaster positionMaster,
                                Set<MetaBean> metaBeans,
                                StringConvert stringConvert) {
    super(positionMaster, securityMaster, metaBeans, stringConvert);
  }

  /* package */ UniqueId buildAndSaveTrade(BeanDataSource tradeData,
                                           BeanDataSource securityData,
                                           BeanDataSource underlyingData) {
    if (!TRADE_TYPE_NAME.equals(tradeData.getBeanTypeName())) {
      throw new IllegalArgumentException("Can only build trades of type " + TRADE_TYPE_NAME +
                                             ", type name = " + tradeData.getBeanTypeName());
    }
    ObjectId securityId = buildSecurity(securityData, underlyingData).getObjectId();
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
    tradeBuilder.set(meta.securityLink(), new ManageableSecurityLink(securityId));
    String counterparty = tradeData.getValue(COUNTERPARTY);
    if (StringUtils.isEmpty(counterparty)) {
      throw new IllegalArgumentException("Trade counterparty is required");
    }
    tradeBuilder.set(meta.counterpartyExternalId(), ExternalId.of(CPTY_SCHEME, counterparty));
    ManageableTrade trade = tradeBuilder.build();
    // TODO need the node ID so we can add the position to the portfolio node
    ManageablePosition position = getPosition(trade);
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
    sink.setValue("type", TRADE_TYPE_NAME);
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

  private UniqueId buildSecurity(BeanDataSource securityData, BeanDataSource underlyingData) {
    ExternalId underlyingId = buildUnderlying(underlyingData);
    BeanDataSource dataSource;
    // TODO would it be better to just return the bean builder from the visitor and handle this property manually?
    // TODO would have to use a different property for every security with underlyingId, there's no common supertype with it
    if (underlyingId != null) {
      dataSource = new PropertyReplacingDataSource(securityData, "underlyingId", underlyingId.toString());
    } else {
      dataSource = securityData;
    }
    FinancialSecurity security = build(dataSource);
    ManageableSecurity savedSecurity = saveSecurity(security);
    return savedSecurity.getUniqueId();
  }

  private ExternalId buildUnderlying(BeanDataSource underlyingData) {
    if (underlyingData == null) {
      return null;
    }
    FinancialSecurity underlying = build(underlyingData);
    saveSecurity(underlying);
    ExternalId underlyingId = underlying.accept(new ExternalIdVisitor(getSecurityMaster()));
    if (underlyingId == null) {
      throw new IllegalArgumentException("Unable to get external ID of underlying security " + underlying);
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
    // filter out the externalIdBundle property so the traverser doesn't set it to null (which will fail)
    PropertyFilter externalIdFilter = new PropertyFilter(FinancialSecurity.meta().externalIdBundle());
    BeanBuilder<FinancialSecurity> builder =
        (BeanBuilder<FinancialSecurity>) new BeanTraverser(externalIdFilter).traverse(metaBean, visitor);
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
