/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.math.BigDecimal;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Fudge message builder for {@code Position}.
 */
@GenericFudgeBuilderFor(Position.class)
public class PositionBuilder implements FudgeBuilder<Position> {

  private static final String FIELD_QUANTITY = "quantity";
  private static final String FIELD_SECURITYKEY = "securityKey";
  private static final String FIELD_IDENTIFIER = "identifier";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Position position) {
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, FIELD_IDENTIFIER, null, position.getUniqueIdentifier());
    message.add(FIELD_QUANTITY, null, position.getQuantity());
    context.objectToFudgeMsg(message, FIELD_SECURITYKEY, null, position.getSecurityKey());
    return message;
  }

  @Override
  public Position buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    FudgeField idField = message.getByName(FIELD_IDENTIFIER);
    UniqueIdentifier id = idField != null ? context.fieldValueToObject(UniqueIdentifier.class, idField) : null;
    BigDecimal quantity = message.getFieldValue(BigDecimal.class, message.getByName(FIELD_QUANTITY));
    IdentifierBundle securityKey = context.fieldValueToObject(IdentifierBundle.class, message.getByName(FIELD_SECURITYKEY));
    
    PositionImpl position = new PositionImpl(quantity, securityKey);
    if (id != null) {
      position.setUniqueIdentifier(id);
    }
    return position;
  }

}
