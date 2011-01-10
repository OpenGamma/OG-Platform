/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
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

/**
 * Fudge message builder for {@code Trade}.
 */
@GenericFudgeBuilderFor(Trade.class)
public class TradeBuilder implements FudgeBuilder<Trade> {

  /**
   * Fudge field name.
   */
  protected static final String FIELD_UNIQUE_ID = "uniqueId";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_PARENT_POSITION_ID = "parentPositionId";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_QUANTITY = "quantity";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_SECURITYKEY = "securityKey";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_COUNTERPARTY = "counterparty";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_TRADE_DATE = "tradeDate";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_TRADE_TIME = "tradeTime";

  @Override
  public MutableFudgeFieldContainer buildMessage(final FudgeSerializationContext context, final Trade trade) {
    final MutableFudgeFieldContainer message = context.newMessage();
    if (trade.getUniqueId() != null) {
      context.objectToFudgeMsg(message, FIELD_UNIQUE_ID, null, trade.getUniqueId());
    }
    if (trade.getParentPositionId() != null) {
      context.objectToFudgeMsg(message, FIELD_PARENT_POSITION_ID, null, trade.getParentPositionId());
    }
    if (trade.getQuantity() != null) {
      message.add(FIELD_QUANTITY, null, trade.getQuantity());
    }
    if (trade.getSecurityKey() != null) {
      context.objectToFudgeMsg(message, FIELD_SECURITYKEY, null, trade.getSecurityKey());
    }
    if (trade.getCounterparty() != null) {
      context.objectToFudgeMsg(message, FIELD_COUNTERPARTY, null, trade.getCounterparty().getIdentifier());
    }
    if (trade.getTradeDate() != null) {
      message.add(FIELD_TRADE_DATE, null, trade.getTradeDate());
    }
    if (trade.getTradeTime() != null) {
      message.add(FIELD_TRADE_TIME, null, trade.getTradeTime());
    }
    return message;
  }

  @Override
  public Trade buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
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
    return trade;
  }

}
