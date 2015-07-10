/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.google.common.base.Optional;

/**
 * Fudge builder for Guava optionals.
 */
@GenericFudgeBuilderFor(Optional.class)
public class OptionalFudgeBuilder implements FudgeBuilder<Optional<?>> {

  /** Field name. */
  public static final String OPTIONAL_FIELD_NAME = "guavaOptional";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Optional<?> object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageObject(msg, OPTIONAL_FIELD_NAME, null, object.orNull(), Object.class);
    return msg;
  }

  @Override
  public Optional<?> buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeField field = msg.getByName(OPTIONAL_FIELD_NAME);
    return field != null ? Optional.of(deserializer.fieldValueToObject(field)) : Optional.absent();
  }

}
