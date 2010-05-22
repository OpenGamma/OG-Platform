/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.server;

import java.math.BigDecimal;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Fudge message builder for {@code Position}.
 */
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
    return new PositionImpl(
        context.fieldValueToObject(UniqueIdentifier.class, message.getByName(FIELD_IDENTIFIER)),
        message.getFieldValue(BigDecimal.class, message.getByName(FIELD_QUANTITY)),
        context.fieldValueToObject(IdentifierBundle.class, message.getByName(FIELD_SECURITYKEY)));
  }

}
