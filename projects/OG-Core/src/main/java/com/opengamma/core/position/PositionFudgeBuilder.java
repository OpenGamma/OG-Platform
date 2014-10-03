/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;

/**
 * Fudge message builder for {@code Position}.
 */
@GenericFudgeBuilderFor(Position.class)
public class PositionFudgeBuilder implements FudgeBuilder<Position> {

  /** Field name. */
  public static final String ATTRIBUTES_FIELD_NAME = "attributes";
  /** Field name. */
  public static final String QUANTITY_FIELD_NAME = "quantity";
  /** Field name. */
  public static final String SECURITY_KEY_FIELD_NAME = "securityKey";
  /** Field name. */
  public static final String SECURITY_ID_FIELD_NAME = "securityId";
  /** Field name. */
  public static final String UNIQUE_ID_FIELD_NAME = "uniqueId";
  /** Field name. */
  public static final String TRADES_FIELD_NAME = "trades";

  private static void encodeTrades(final MutableFudgeMsg message, final FudgeSerializer serializer, final Collection<Trade> trades) {
    if (!trades.isEmpty()) {
      final MutableFudgeMsg msg = serializer.newMessage();
      for (Trade trade : trades) {
        msg.add(null, null, TradeFudgeBuilder.buildMessageImpl(serializer, trade));
      }
      message.add(TRADES_FIELD_NAME, msg);
    }
  }

  protected static MutableFudgeMsg buildMessageImpl(final FudgeSerializer serializer, final Position position) {
    final MutableFudgeMsg message = serializer.newMessage();
    if (position.getUniqueId() != null) {
      serializer.addToMessage(message, UNIQUE_ID_FIELD_NAME, null, position.getUniqueId());
    }
    if (position.getQuantity() != null) {
      message.add(QUANTITY_FIELD_NAME, null, position.getQuantity());
    }
    if (position.getSecurityLink().getExternalId().size() > 0) {
      serializer.addToMessage(message, SECURITY_KEY_FIELD_NAME, null, position.getSecurityLink().getExternalId());
    }
    if (position.getSecurityLink().getObjectId() != null) {
      serializer.addToMessage(message, SECURITY_ID_FIELD_NAME, null, position.getSecurityLink().getObjectId());
    }
    if (haveAttributes(position)) {
      final MutableFudgeMsg attributesMsg = serializer.newMessage();
      for (Entry<String, String> entry : position.getAttributes().entrySet()) {
        attributesMsg.add(entry.getKey(), entry.getValue());
      }
      serializer.addToMessage(message, ATTRIBUTES_FIELD_NAME, null, attributesMsg);
    }
    encodeTrades(message, serializer, position.getTrades());
    return message;
  }
  
  private static boolean haveAttributes(final Position position) {
    return position.getAttributes() != null && !position.getAttributes().isEmpty();
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Position position) {
    final MutableFudgeMsg message = buildMessageImpl(serializer, position);
    message.add(null, FudgeSerializer.TYPES_HEADER_ORDINAL, FudgeWireType.STRING, Position.class.getName());
    return message;
  }

  private static void readTrades(final FudgeDeserializer deserializer, final FudgeMsg message, final SimplePosition position) {
    if (message != null) {
      for (FudgeField field : message) {
        if (field.getValue() instanceof FudgeMsg) {
          final SimpleTrade trade = TradeFudgeBuilder.buildObjectImpl(deserializer, (FudgeMsg) field.getValue());
          position.addTrade(trade);
        }
      }
    }
  }

  protected static SimplePosition buildObjectImpl(final FudgeDeserializer deserializer, final FudgeMsg message) {
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
    
    SimplePosition position = new SimplePosition();
    position.setSecurityLink(secLink);
    if (message.hasField(UNIQUE_ID_FIELD_NAME)) {
      FudgeField uniqueIdField = message.getByName(UNIQUE_ID_FIELD_NAME);
      if (uniqueIdField != null) {
        position.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdField));
      }
    }
    if (message.hasField(QUANTITY_FIELD_NAME)) {
      FudgeField quantityField = message.getByName(QUANTITY_FIELD_NAME);
      if (quantityField != null) {
        position.setQuantity(message.getFieldValue(BigDecimal.class, quantityField));
      }
    }
    if (message.hasField(ATTRIBUTES_FIELD_NAME)) {
      FudgeMsg attributesMsg = message.getMessage(ATTRIBUTES_FIELD_NAME);
      for (FudgeField fudgeField : attributesMsg) {
        String key = fudgeField.getName();
        Object value = fudgeField.getValue();
        if (key != null && value != null) {
          position.addAttribute(key, (String) value);
        }
      }
    }
    readTrades(deserializer, message.getFieldValue(FudgeMsg.class, message.getByName(TRADES_FIELD_NAME)), position);
    return position;
  }

  @Override
  public Position buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectImpl(deserializer, message);
  }

}
