/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.math.BigDecimal;
import java.util.Map.Entry;

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
   * Attributes fudge field name.
   */
  private static final String ATTRIBUTES = "attributes";
  /**
   * Premium time fudge field name.
   */
  private static final String PREMIUM_TIME = "premiumTime";
  /**
   * Premium date fudge field name.
   */
  private static final String PREMIUM_DATE = "premiumDate";
  /**
   * Premium currency fudge field name.
   */
  private static final String PREMIUM_CURRENCY = "premiumCurrency";
  /**
   * Premium value fudge field name.
   */
  private static final String PREMIUM = "premium";
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
  private static final String FIELD_COUNTERPARTY = "counterpartyKey";
  /**
   * Fudge field name.
   */
  private static final String FIELD_TRADE_DATE = "tradeDate";
  /**
   * Fudge field name.
   */
  private static final String FIELD_TRADE_TIME = "tradeTime";

  protected static MutableFudgeMsg buildMessageImpl(final FudgeSerializationContext context, final Trade trade) {
    final MutableFudgeMsg message = context.newMessage();
    if (trade.getUniqueId() != null) {
      context.addToMessage(message, FIELD_UNIQUE_ID, null, trade.getUniqueId());
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
      message.add(PREMIUM, null, trade.getPremium());
    }
    if (trade.getPremiumCurrency() != null) {
      message.add(PREMIUM_CURRENCY, null, trade.getPremiumCurrency().getCode());
    }
    if (trade.getPremiumDate() != null) {
      message.add(PREMIUM_DATE, null, trade.getPremiumDate());
    }
    if (trade.getPremiumTime() != null) {
      message.add(PREMIUM_TIME, null, trade.getPremiumTime());
    }
    if (haveAttributes(trade)) {
      final MutableFudgeMsg attributesMsg = context.newMessage();
      for (Entry<String, String> entry : trade.getAttributes().entrySet()) {
        attributesMsg.add(entry.getKey(), entry.getValue());
      }
      context.addToMessage(message, ATTRIBUTES, null, attributesMsg);
    }
    return message;
  }

  private static boolean haveAttributes(final Trade trade) {
    return trade.getAttributes() != null && !trade.getAttributes().isEmpty();
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final Trade trade) {
    final MutableFudgeMsg message = buildMessageImpl(context, trade);
    if (trade.getParentPositionId() != null) {
      context.addToMessage(message, FIELD_PARENT_POSITION_ID, null, trade.getParentPositionId());
    }
    return message;
  }

  protected static TradeImpl buildObjectImpl(final FudgeDeserializationContext context, final FudgeMsg message) {
    
    TradeImpl trade = new TradeImpl();
    if (message.hasField(FIELD_UNIQUE_ID)) {
      FudgeField uidField = message.getByName(FIELD_UNIQUE_ID);
      if (uidField != null) {
        trade.setUniqueId(context.fieldValueToObject(UniqueIdentifier.class, uidField));
      }      
    }
    if (message.hasField(FIELD_QUANTITY)) {
      FudgeField quantityField = message.getByName(FIELD_QUANTITY);
      if (quantityField != null) {
        trade.setQuantity(message.getFieldValue(BigDecimal.class, quantityField));
      }
    }
    if (message.hasField(FIELD_SECURITYKEY)) {
      FudgeField secKeyField = message.getByName(FIELD_SECURITYKEY);
      if (secKeyField != null) {
        trade.setSecurityKey(context.fieldValueToObject(IdentifierBundle.class, secKeyField));
      }
    }
    if (message.hasField(FIELD_COUNTERPARTY)) {
      FudgeField counterpartyField = message.getByName(FIELD_COUNTERPARTY);
      if (counterpartyField != null) {
        trade.setCounterparty(new CounterpartyImpl(context.fieldValueToObject(Identifier.class, counterpartyField)));
      }
    }
    if (message.hasField(FIELD_TRADE_DATE)) {
      FudgeField tradeDateField = message.getByName(FIELD_TRADE_DATE);
      if (tradeDateField != null) {
        trade.setTradeDate(message.getFieldValue(LocalDate.class, tradeDateField));
      }
    }
    if (message.hasField(FIELD_TRADE_TIME)) {
      FudgeField tradeTimeField = message.getByName(FIELD_TRADE_TIME);
      if (tradeTimeField != null) {
        trade.setTradeTime(message.getFieldValue(OffsetTime.class, tradeTimeField));
      }
    }
    if (message.hasField(PREMIUM)) {
      trade.setPremium(message.getDouble(PREMIUM));
    }
    if (message.hasField(PREMIUM_CURRENCY)) {
      String currencyCode = message.getString(PREMIUM_CURRENCY);
      if (currencyCode != null) {
        trade.setPremiumCurrency(Currency.of(currencyCode));
      }
    }
    if (message.hasField(PREMIUM_DATE)) {
      FudgeField premiumDate = message.getByName(PREMIUM_DATE);
      if (premiumDate != null) {
        trade.setPremiumDate(message.getFieldValue(LocalDate.class, premiumDate));
      }
    }
    if (message.hasField(PREMIUM_TIME)) {
      FudgeField premiumTime = message.getByName(PREMIUM_TIME);
      if (premiumTime != null) {
        trade.setPremiumTime(message.getFieldValue(OffsetTime.class, premiumTime));
      }
    }
    if (message.hasField(ATTRIBUTES)) {
      FudgeMsg attributesMsg = message.getMessage(ATTRIBUTES);
      for (FudgeField fudgeField : attributesMsg) {
        String key = fudgeField.getName();
        Object value = fudgeField.getValue();
        if (key != null && value != null) {
          trade.addAttribute(key, (String) value);
        }
      }
    }
    return trade;
  }

  @Override
  public Trade buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    final TradeImpl trade = buildObjectImpl(context, message);
    FudgeField positionField = message.getByName(FIELD_PARENT_POSITION_ID);
    if (positionField != null) {
      trade.setParentPositionId(context.fieldValueToObject(UniqueIdentifier.class, positionField));
    }
    return trade;
  }

}
