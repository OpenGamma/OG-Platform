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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;

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

  private static void encodeTrades(final MutableFudgeMsg message, final FudgeSerializer serializer, final Collection<Trade> trades) {
    if (!trades.isEmpty()) {
      final MutableFudgeMsg msg = serializer.newMessage();
      for (Trade trade : trades) {
        msg.add(null, null, TradeBuilder.buildMessageImpl(serializer, trade));
      }
      message.add(FIELD_TRADES, msg);
    }
  }

  protected static MutableFudgeMsg buildMessageImpl(final FudgeSerializer serializer, final Position position) {
    final MutableFudgeMsg message = serializer.newMessage();
    if (position.getUniqueId() != null) {
      serializer.addToMessage(message, FIELD_UNIQUE_ID, null, position.getUniqueId());
    }
    if (position.getQuantity() != null) {
      message.add(FIELD_QUANTITY, null, position.getQuantity());
    }
    if (position.getSecurityLink().getExternalId().size() > 0) {
      serializer.addToMessage(message, FIELD_SECURITYKEY, null, position.getSecurityLink().getExternalId());
    }
    if (position.getSecurityLink().getObjectId() != null) {
      serializer.addToMessage(message, FIELD_SECURITYID, null, position.getSecurityLink().getObjectId());
    }
    encodeTrades(message, serializer, position.getTrades());
    return message;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Position position) {
    final MutableFudgeMsg message = buildMessageImpl(serializer, position);
    serializer.addToMessage(message, FIELD_PARENT, null, position.getParentNodeId());
    return message;
  }

  private static void readTrades(final FudgeDeserializer deserializer, final FudgeMsg message, final PositionImpl position) {
    if (message != null) {
      for (FudgeField field : message) {
        if (field.getValue() instanceof FudgeMsg) {
          final TradeImpl trade = TradeBuilder.buildObjectImpl(deserializer, (FudgeMsg) field.getValue());
          trade.setParentPositionId(position.getUniqueId());
          position.addTrade(trade);
        }
      }
    }
  }

  protected static PositionImpl buildObjectImpl(final FudgeDeserializer deserializer, final FudgeMsg message) {
    SimpleSecurityLink secLink = new SimpleSecurityLink();
    if (message.hasField(FIELD_SECURITYKEY)) {
      FudgeField secKeyField = message.getByName(FIELD_SECURITYKEY);
      if (secKeyField != null) {
        secLink.setExternalId(deserializer.fieldValueToObject(ExternalIdBundle.class, secKeyField));
      }
    }
    if (message.hasField(FIELD_SECURITYID)) {
      FudgeField secIdField = message.getByName(FIELD_SECURITYID);
      if (secIdField != null) {
        secLink.setObjectId(deserializer.fieldValueToObject(ObjectId.class, secIdField));
      }
    }
    
    PositionImpl position = new PositionImpl();
    position.setSecurityLink(secLink);
    if (message.hasField(FIELD_UNIQUE_ID)) {
      FudgeField uniqueIdField = message.getByName(FIELD_UNIQUE_ID);
      if (uniqueIdField != null) {
        position.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdField));
      }      
    }
    if (message.hasField(FIELD_QUANTITY)) {
      FudgeField quantityField = message.getByName(FIELD_QUANTITY);
      if (quantityField != null) {
        position.setQuantity(message.getFieldValue(BigDecimal.class, quantityField));
      }
    }
    readTrades(deserializer, message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_TRADES)), position);
    return position;
  }

  @Override
  public Position buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final PositionImpl position = buildObjectImpl(deserializer, message);
    final FudgeField parentField = message.getByName(FIELD_PARENT);
    final UniqueId parentId = (parentField != null) ? deserializer.fieldValueToObject(UniqueId.class, parentField) : null;
    if (parentId != null) {
      position.setParentNodeId(parentId);
    }
    return position;
  }

}
