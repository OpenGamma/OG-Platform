/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.CounterpartyImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.money.Currency;

/**
 * Fudge message builder for {@code Trade}.
 */
@GenericFudgeBuilderFor(Trade.class)
public class TradeBuilder implements FudgeBuilder<Trade> {

  /**
   * Fudge field name.
   */
  private static final String FIELD_UNIQUE_ID = "uniqueId";
  /**
   * Fudge field name.
   */
  private static final String FIELD_PARENT_POSITION_ID = "parentPositionId";
  /**
   * Fudge field name.
   */
  private static final String FIELD_QUANTITY = "quantity";
  /**
   * Fudge field name.
   */
  private static final String FIELD_SECURITYKEY = "securityKey";
  /**
   * Fudge field name.
   */
  private static final String FIELD_COUNTERPARTY = "counterparty";
  /**
   * Fudge field name.
   */
  private static final String FIELD_TRADE_DATE = "tradeDate";
  /**
   * Fudge field name.
   */
  private static final String FIELD_TRADE_TIME = "tradeTime";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final Trade trade) {
    final MutableFudgeMsg message = context.newMessage();
    if (trade.getUniqueId() != null) {
      context.addToMessage(message, FIELD_UNIQUE_ID, null, trade.getUniqueId());
    }
    if (trade.getParentPositionId() != null) {
      context.addToMessage(message, FIELD_PARENT_POSITION_ID, null, trade.getParentPositionId());
    }
    if (trade.getQuantity() != null) {
      message.add(FIELD_QUANTITY, null, trade.getQuantity());
    }
    if (trade.getSecurityKey() != null) {
      context.addToMessage(message, FIELD_SECURITYKEY, null, trade.getSecurityKey());
    }
    if (trade.getCounterparty() != null) {
      context.addToMessage(message, FIELD_COUNTERPARTY, null, trade.getCounterparty().getIdentifier());
    }
    if (trade.getTradeDate() != null) {
      message.add(FIELD_TRADE_DATE, null, trade.getTradeDate());
    }
    if (trade.getTradeTime() != null) {
      message.add(FIELD_TRADE_TIME, null, trade.getTradeTime());
    }
    if (trade.getPremium() != null) {
      message.add("premium", null, trade.getPremium());
    }
    if (trade.getPremiumCurrency() != null) {
      message.add("premiumCurrency", null, trade.getPremiumCurrency().getCode());
    }
    if (trade.getPremiumDate() != null) {
      message.add("premiumDate", null, trade.getPremiumDate());
    }
    if (trade.getPremiumTime() != null) {
      message.add("premiumTime", null, trade.getPremiumTime());
    }
    return message;
  }

  @Override
  public Trade buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    FudgeField uidField = message.getByName(FIELD_UNIQUE_ID);
    UniqueIdentifier tradeId = uidField != null ? context.fieldValueToObject(UniqueIdentifier.class, uidField) : null;
    FudgeField positionField = message.getByName(FIELD_PARENT_POSITION_ID);
    UniqueIdentifier positionId = positionField != null ? context.fieldValueToObject(UniqueIdentifier.class, positionField) : null;
    FudgeField quantityField = message.getByName(FIELD_QUANTITY);
    FudgeField secKeyField = message.getByName(FIELD_SECURITYKEY);
    FudgeField counterpartyField = message.getByName(FIELD_COUNTERPARTY);
    FudgeField tradeDateField = message.getByName(FIELD_TRADE_DATE);
    FudgeField tradeTimeField = message.getByName(FIELD_TRADE_TIME);
    TradeImpl trade = new TradeImpl();
    if (tradeId != null) {
      trade.setUniqueId(tradeId);
    }
    if (positionId != null) {
      trade.setParentPositionId(positionId);
    }
    if (quantityField != null) {
      trade.setQuantity(message.getFieldValue(BigDecimal.class, quantityField));
    }
    if (secKeyField != null) {
      trade.setSecurityKey(context.fieldValueToObject(IdentifierBundle.class, secKeyField));
    }
    if (counterpartyField != null) {
      trade.setCounterparty(new CounterpartyImpl(context.fieldValueToObject(Identifier.class, counterpartyField)));
    }
    if (tradeDateField != null) {
      trade.setTradeDate(message.getFieldValue(LocalDate.class, tradeDateField));
    }
    if (tradeTimeField != null) {
      trade.setTradeTime(message.getFieldValue(OffsetTime.class, tradeTimeField));
    }
    trade.setPremium(message.getDouble("premium"));
    String currencyCode = message.getString("premiumCurrency");
    if (currencyCode != null) {
      trade.setPremiumCurrency(Currency.of(currencyCode));
    }
    FudgeField premiumDate = message.getByName("premiumDate");
    if (premiumDate != null) {
      trade.setPremiumDate(message.getFieldValue(LocalDate.class, premiumDate));
    }
    FudgeField premiumTime = message.getByName("premiumTime");
    if (premiumTime != null) {
      trade.setPremiumTime(message.getFieldValue(OffsetTime.class, premiumTime));
    }
    return trade;
  }

}
