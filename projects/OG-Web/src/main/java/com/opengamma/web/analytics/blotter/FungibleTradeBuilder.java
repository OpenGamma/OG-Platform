/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.Property;
import org.joda.convert.StringConverter;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.position.ManageableTrade;

/**
 *
 */
/* package */ class FungibleTradeBuilder {

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
}
