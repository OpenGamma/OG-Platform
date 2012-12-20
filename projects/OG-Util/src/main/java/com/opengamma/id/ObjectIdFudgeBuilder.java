/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code ObjectId}.
 */
@FudgeBuilderFor(ObjectId.class)
public final class ObjectIdFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ObjectId> {

  /** Field name. */
  public static final String SCHEME_FIELD_NAME = "Scheme";
  /** Field name. */
  public static final String VALUE_FIELD_NAME = "Value";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ObjectId object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final ObjectId object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final ObjectId object, final MutableFudgeMsg msg) {
    addToMessage(msg, SCHEME_FIELD_NAME, object.getScheme());
    addToMessage(msg, VALUE_FIELD_NAME, object.getValue());
  }

  //-------------------------------------------------------------------------
  @Override
  public ObjectId buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(msg);
  }

  public static ObjectId fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    return fromFudgeMsg(msg);
  }

  public static ObjectId fromFudgeMsg(final FudgeMsg msg) {
    String scheme = msg.getString(SCHEME_FIELD_NAME);
    String value = msg.getString(VALUE_FIELD_NAME);
    return ObjectId.of(scheme, value);
  }

}
