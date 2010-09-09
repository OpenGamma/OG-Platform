/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time.fudgemsg;

import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Builder to convert Expiry to and from Fudge.
 */
@FudgeBuilderFor(Expiry.class)
public class ExpiryBuilder implements FudgeBuilder<Expiry> {
  private static final String EXPIRY_KEY = "expiry";
  private static final String ACCURACY_KEY = "accuracy";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Expiry object) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    ZonedDateTime expiry = object.getExpiry();
    MutableFudgeFieldContainer expiryMsg = context.objectToFudgeMsg(expiry);
    msg.add(EXPIRY_KEY, expiryMsg);
    ExpiryAccuracy accuracy = object.getAccuracy();
    msg.add(ACCURACY_KEY, accuracy.name());
    return msg;
  }

  @Override
  public Expiry buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    FudgeField expiryField = message.getByName(EXPIRY_KEY);
    if (expiryField == null) {
      throw new IllegalArgumentException("Fudge message is not a Expiry - field 'expiry' is not present");
    }
    ZonedDateTime expiry = context.fieldValueToObject(ZonedDateTime.class, expiryField);
    String accuracyName = message.getString(ACCURACY_KEY);
    if (accuracyName == null) {
      throw new IllegalArgumentException("Fudge message is not a Expiry - field 'accuracy' is not present");
    }
    ExpiryAccuracy expiryAccuracy = ExpiryAccuracy.valueOf(accuracyName);
    return new Expiry(expiry, expiryAccuracy);
  }

}
