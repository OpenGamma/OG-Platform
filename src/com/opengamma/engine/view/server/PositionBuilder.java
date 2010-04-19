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
import com.opengamma.engine.position.PositionBean;
import com.opengamma.id.IdentifierBundle;

public class PositionBuilder implements FudgeBuilder<Position> {
  
  public static final String FIELD_QUANTITY = "quantity";
  public static final String FIELD_SECURITYKEY = "securityKey";
  public static final String FIELD_IDENTITYKEY = "identityKey";

  @Override
  public MutableFudgeFieldContainer buildMessage (FudgeSerializationContext context, Position position) {
    final MutableFudgeFieldContainer message = context.newMessage ();
    // Position
    message.add (FIELD_QUANTITY, null, position.getQuantity ());
    context.objectToFudgeMsg (message, FIELD_SECURITYKEY, null, position.getSecurityKey ());
    // Identifiable
    message.add (FIELD_IDENTITYKEY, null, position.getIdentityKey ().getValue ());
    return message;
  }
  @Override
  public Position buildObject (FudgeDeserializationContext context, FudgeFieldContainer message) {
    // Position
    final PositionBean position = new PositionBean (message.getFieldValue (BigDecimal.class, message.getByName (FIELD_QUANTITY)), context.fieldValueToObject (IdentifierBundle.class, message.getByName (FIELD_SECURITYKEY)));
    // Identifiable
    position.setIdentityKey (message.getFieldValue (String.class, message.getByName (FIELD_IDENTITYKEY)));
    return position;
  }
 
}