/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.fudgemsg;

import java.math.BigDecimal;
import java.util.Collection;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Fudge message builder for {@code Position}.
 */
@GenericFudgeBuilderFor(Position.class)
public class PositionBuilder implements FudgeBuilder<Position> {

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
  protected static final String FIELD_SECURITYID = "securityId";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_UNIQUE_ID = "uniqueId";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_PARENT = "parent";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_TRADES = "trades";

  private static void encodeTrades(final MutableFudgeMsg message, final FudgeSerializationContext context, final Collection<Trade> trades) {
    if (!trades.isEmpty()) {
      final MutableFudgeMsg msg = context.newMessage();
      for (Trade trade : trades) {
        msg.add(null, null, TradeBuilder.buildMessageImpl(context, trade));
      }
      message.add(FIELD_TRADES, msg);
    }
  }

  protected static MutableFudgeMsg buildMessageImpl(final FudgeSerializationContext context, final Position position) {
    final MutableFudgeMsg message = context.newMessage();
    if (position.getUniqueId() != null) {
      context.addToMessage(message, FIELD_UNIQUE_ID, null, position.getUniqueId());
    }
    if (position.getQuantity() != null) {
      message.add(FIELD_QUANTITY, null, position.getQuantity());
    }
    if (position.getSecurityLink().getIdBundle().size() > 0) {
      context.addToMessage(message, FIELD_SECURITYKEY, null, position.getSecurityLink().getIdBundle());
    }
    if (position.getSecurityLink().getObjectId() != null) {
      context.addToMessage(message, FIELD_SECURITYID, null, position.getSecurityLink().getObjectId());
    }
    encodeTrades(message, context, position.getTrades());
    return message;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final Position position) {
    final MutableFudgeMsg message = buildMessageImpl(context, position);
    context.addToMessage(message, FIELD_PARENT, null, position.getParentNodeId());
    return message;
  }

  private static void readTrades(final FudgeDeserializationContext context, final FudgeMsg message, final PositionImpl position) {
    if (message != null) {
      for (FudgeField field : message) {
        if (field.getValue() instanceof FudgeMsg) {
          final TradeImpl trade = TradeBuilder.buildObjectImpl(context, (FudgeMsg) field.getValue());
          trade.setParentPositionId(position.getUniqueId());
          position.addTrade(trade);
        }
      }
    }
  }

  protected static PositionImpl buildObjectImpl(final FudgeDeserializationContext context, final FudgeMsg message) {
    PositionImpl position = new PositionImpl();
    if (message.hasField(FIELD_UNIQUE_ID)) {
      FudgeField uidField = message.getByName(FIELD_UNIQUE_ID);
      if (uidField != null) {
        position.setUniqueId(context.fieldValueToObject(UniqueIdentifier.class, uidField));
      }      
    }
    if (message.hasField(FIELD_QUANTITY)) {
      FudgeField quantityField = message.getByName(FIELD_QUANTITY);
      if (quantityField != null) {
        position.setQuantity(message.getFieldValue(BigDecimal.class, quantityField));
      }
    }
    if (message.hasField(FIELD_SECURITYKEY)) {
      FudgeField secKeyField = message.getByName(FIELD_SECURITYKEY);
      if (secKeyField != null) {
        position.getSecurityLink().setIdBundle(context.fieldValueToObject(IdentifierBundle.class, secKeyField));
      }
    }
    if (message.hasField(FIELD_SECURITYID)) {
      FudgeField secIdField = message.getByName(FIELD_SECURITYID);
      if (secIdField != null) {
        position.getSecurityLink().setObjectId(context.fieldValueToObject(ObjectIdentifier.class, secIdField));
      }
    }
    readTrades(context, message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_TRADES)), position);
    return position;
  }

  @Override
  public Position buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    final PositionImpl position = buildObjectImpl(context, message);
    final FudgeField parentField = message.getByName(FIELD_PARENT);
    final UniqueIdentifier parentId = (parentField != null) ? context.fieldValueToObject(UniqueIdentifier.class, parentField) : null;
    if (parentId != null) {
      position.setParentNodeId(parentId);
    }
    return position;
  }

}
