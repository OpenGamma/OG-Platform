/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.math.BigDecimal;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.id.IdentifierBundle;
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
  protected static final String FIELD_IDENTIFIER = "identifier";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_PARENT = "parent";

  protected static MutableFudgeMsg buildMessageImpl(final FudgeSerializationContext context, final Position position) {
    final MutableFudgeMsg message = context.newMessage();
    context.addToMessage(message, FIELD_IDENTIFIER, null, position.getUniqueId());
    message.add(FIELD_QUANTITY, null, position.getQuantity());
    context.addToMessage(message, FIELD_SECURITYKEY, null, position.getSecurityKey());
    return message;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final Position position) {
    final MutableFudgeMsg message = buildMessageImpl(context, position);
    context.addToMessage(message, FIELD_PARENT, null, position.getParentNodeId());
    return message;
  }

  protected static PositionImpl buildObjectImpl(final FudgeDeserializationContext context, final FudgeMsg message) {
    FudgeField idField = message.getByName(FIELD_IDENTIFIER);
    UniqueIdentifier id = idField != null ? context.fieldValueToObject(UniqueIdentifier.class, idField) : null;
    BigDecimal quantity = message.getFieldValue(BigDecimal.class, message.getByName(FIELD_QUANTITY));
    IdentifierBundle securityKey = context.fieldValueToObject(IdentifierBundle.class, message.getByName(FIELD_SECURITYKEY));
    PositionImpl position = new PositionImpl(quantity, securityKey);
    if (id != null) {
      position.setUniqueId(id);
    }
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
