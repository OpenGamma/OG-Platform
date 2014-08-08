/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import java.math.BigDecimal;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.wire.types.FudgeWireType;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Fudge message builder for {@code Trade}.
 */
@GenericFudgeBuilderFor(Trade.class)
public class TradeFudgeBuilder implements FudgeBuilder<Trade> {

  /** Field name. */
  public static final String ATTRIBUTES_FIELD_NAME = "attributes";
  /** Field name. */
  public static final String PREMIUM_TIME_FIELD_NAME = "premiumTime";
  /** Field name. */
  public static final String PREMIUM_DATE_FIELD_NAME = "premiumDate";
  /** Field name. */
  public static final String PREMIUM_CURRENCY_FIELD_NAME = "premiumCurrency";
  /** Field name. */
  public static final String PREMIUM_FIELD_NAME = "premium";
  /** Field name. */
  public static final String UNIQUE_ID_FIELD_NAME = "uniqueId";
  /** Field name. */
  public static final String QUANTITY_FIELD_NAME = "quantity";
  /** Field name. */
  protected static final String SECURITY_KEY_FIELD_NAME = "securityKey";
  /** Field name. */
  protected static final String SECURITY_ID_FIELD_NAME = "securityId";
  /** Field name. */
  public static final String COUNTERPARTY_FIELD_NAME = "counterpartyKey";
  /** Field name. */
  public static final String TRADE_DATE_FIELD_NAME = "tradeDate";
  /** Field name. */
  public static final String TRADE_TIME_FIELD_NAME = "tradeTime";

  protected static MutableFudgeMsg buildMessageImpl(final FudgeSerializer serializer, final Trade trade) {
    final MutableFudgeMsg message = serializer.newMessage();
    if (trade.getUniqueId() != null) {
      serializer.addToMessage(message, UNIQUE_ID_FIELD_NAME, null, trade.getUniqueId());
    }
    if (trade.getQuantity() != null) {
      message.add(QUANTITY_FIELD_NAME, null, trade.getQuantity());
    }
    if (trade.getSecurityLink().getExternalId().size() > 0) {
      serializer.addToMessage(message, SECURITY_KEY_FIELD_NAME, null, trade.getSecurityLink().getExternalId());
    }
    if (trade.getSecurityLink().getObjectId() != null) {
      serializer.addToMessage(message, SECURITY_ID_FIELD_NAME, null, trade.getSecurityLink().getObjectId());
    }
    if (trade.getCounterparty() != null) {
      serializer.addToMessage(message, COUNTERPARTY_FIELD_NAME, null, trade.getCounterparty().getExternalId());
    }
    if (trade.getTradeDate() != null) {
      message.add(TRADE_DATE_FIELD_NAME, null, trade.getTradeDate());
    }
    if (trade.getTradeTime() != null) {
      message.add(TRADE_TIME_FIELD_NAME, null, trade.getTradeTime());
    }
    if (trade.getPremium() != null) {
      message.add(PREMIUM_FIELD_NAME, null, trade.getPremium());
    }
    if (trade.getPremiumCurrency() != null) {
      message.add(PREMIUM_CURRENCY_FIELD_NAME, null, trade.getPremiumCurrency().getCode());
    }
    if (trade.getPremiumDate() != null) {
      message.add(PREMIUM_DATE_FIELD_NAME, null, trade.getPremiumDate());
    }
    if (trade.getPremiumTime() != null) {
      message.add(PREMIUM_TIME_FIELD_NAME, null, trade.getPremiumTime());
    }
    if (haveAttributes(trade)) {
      final MutableFudgeMsg attributesMsg = serializer.newMessage();
      for (Entry<String, String> entry : trade.getAttributes().entrySet()) {
        attributesMsg.add(entry.getKey(), entry.getValue());
      }
      serializer.addToMessage(message, ATTRIBUTES_FIELD_NAME, null, attributesMsg);
    }
    return message;
  }

  private static boolean haveAttributes(final Trade trade) {
    return trade.getAttributes() != null && !trade.getAttributes().isEmpty();
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Trade trade) {
    final MutableFudgeMsg message = buildMessageImpl(serializer, trade);
    message.add(null, FudgeSerializer.TYPES_HEADER_ORDINAL, FudgeWireType.STRING, Trade.class.getName());
    return message;
  }

  protected static SimpleTrade buildObjectImpl(final FudgeDeserializer deserializer, final FudgeMsg message) {
    SimpleSecurityLink secLink = new SimpleSecurityLink();
    if (message.hasField(SECURITY_KEY_FIELD_NAME)) {
      FudgeField secKeyField = message.getByName(SECURITY_KEY_FIELD_NAME);
      if (secKeyField != null) {
        secLink.setExternalId(deserializer.fieldValueToObject(ExternalIdBundle.class, secKeyField));
      }
    }
    if (message.hasField(SECURITY_ID_FIELD_NAME)) {
      FudgeField secIdField = message.getByName(SECURITY_ID_FIELD_NAME);
      if (secIdField != null) {
        secLink.setObjectId(deserializer.fieldValueToObject(ObjectId.class, secIdField));
      }
    }
    
    SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(secLink);
    if (message.hasField(UNIQUE_ID_FIELD_NAME)) {
      FudgeField uniqueIdField = message.getByName(UNIQUE_ID_FIELD_NAME);
      if (uniqueIdField != null) {
        trade.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdField));
      }
    }
    if (message.hasField(QUANTITY_FIELD_NAME)) {
      FudgeField quantityField = message.getByName(QUANTITY_FIELD_NAME);
      if (quantityField != null) {
        trade.setQuantity(message.getFieldValue(BigDecimal.class, quantityField));
      }
    }
    if (message.hasField(COUNTERPARTY_FIELD_NAME)) {
      FudgeField counterpartyField = message.getByName(COUNTERPARTY_FIELD_NAME);
      if (counterpartyField != null) {
        trade.setCounterparty(new SimpleCounterparty(deserializer.fieldValueToObject(ExternalId.class, counterpartyField)));
      }
    }
    if (message.hasField(TRADE_DATE_FIELD_NAME)) {
      FudgeField tradeDateField = message.getByName(TRADE_DATE_FIELD_NAME);
      if (tradeDateField != null) {
        trade.setTradeDate(message.getFieldValue(LocalDate.class, tradeDateField));
      }
    }
    if (message.hasField(TRADE_TIME_FIELD_NAME)) {
      FudgeField tradeTimeField = message.getByName(TRADE_TIME_FIELD_NAME);
      if (tradeTimeField != null) {
        trade.setTradeTime(message.getFieldValue(OffsetTime.class, tradeTimeField));
      }
    }
    if (message.hasField(PREMIUM_FIELD_NAME)) {
      trade.setPremium(message.getDouble(PREMIUM_FIELD_NAME));
    }
    if (message.hasField(PREMIUM_CURRENCY_FIELD_NAME)) {
      String currencyCode = message.getString(PREMIUM_CURRENCY_FIELD_NAME);
      if (currencyCode != null) {
        trade.setPremiumCurrency(Currency.of(currencyCode));
      }
    }
    if (message.hasField(PREMIUM_DATE_FIELD_NAME)) {
      FudgeField premiumDate = message.getByName(PREMIUM_DATE_FIELD_NAME);
      if (premiumDate != null) {
        trade.setPremiumDate(message.getFieldValue(LocalDate.class, premiumDate));
      }
    }
    if (message.hasField(PREMIUM_TIME_FIELD_NAME)) {
      FudgeField premiumTime = message.getByName(PREMIUM_TIME_FIELD_NAME);
      if (premiumTime != null) {
        trade.setPremiumTime(message.getFieldValue(OffsetTime.class, premiumTime));
      }
    }
    if (message.hasField(ATTRIBUTES_FIELD_NAME)) {
      FudgeMsg attributesMsg = message.getMessage(ATTRIBUTES_FIELD_NAME);
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
  public Trade buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectImpl(deserializer, message);
  }

}
