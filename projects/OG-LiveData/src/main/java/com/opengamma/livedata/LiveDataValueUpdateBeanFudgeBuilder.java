/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Fudge message builder for {@code LiveDataValueUpdate}.
 */
@FudgeBuilderFor(LiveDataValueUpdateBean.class)
public class LiveDataValueUpdateBeanFudgeBuilder implements FudgeBuilder<LiveDataValueUpdateBean> {

  /** Field name. */
  public static final String SEQUENCE_NUMBER_FIELD_NAME = "sequenceNumber";
  /** Field name. */
  public static final String SPECIFICATION_FIELD_NAME = "specification";
  /** Field name. */
  public static final String FIELDS_FIELD_NAME = "fields";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, LiveDataValueUpdateBean object) {
    return LiveDataValueUpdateBeanFudgeBuilder.toFudgeMsg(serializer, object);
  }

  public static MutableFudgeMsg toFudgeMsg(FudgeSerializer serializer, LiveDataValueUpdateBean object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    LiveDataValueUpdateBeanFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, LiveDataValueUpdateBean object, final MutableFudgeMsg msg) {
    msg.add(SEQUENCE_NUMBER_FIELD_NAME, object.getSequenceNumber());
    if (object.getSpecification() != null) {
      msg.add(SPECIFICATION_FIELD_NAME, LiveDataSpecificationFudgeBuilder.toFudgeMsg(serializer, object.getSpecification()));
    }
    if (object.getFields() != null) {
      msg.add(FIELDS_FIELD_NAME, object.getFields());
    }
//    FudgeSerializer.addClassHeader(msg, LiveDataValueUpdateBean.class, LiveDataValueUpdate.class);
  }

  @Override
  public LiveDataValueUpdateBean buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    return LiveDataValueUpdateBeanFudgeBuilder.fromFudgeMsg(deserializer, msg);
  }

  public static LiveDataValueUpdateBean fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    Long sequenceNumber = msg.getLong(SEQUENCE_NUMBER_FIELD_NAME);
    FudgeMsg specificationFields = msg.getMessage(SPECIFICATION_FIELD_NAME);
    FudgeMsg fields = msg.getMessage(FIELDS_FIELD_NAME);
    // REVIEW kirk 2009-10-28 -- Right thing to do here?
    if (sequenceNumber == null) {
      return null;
    }
    if (specificationFields == null) {
      return null;
    }
    if (fields == null) {
      return null;
    }
    LiveDataSpecification spec = LiveDataSpecificationFudgeBuilder.fromFudgeMsg(deserializer, specificationFields);
    return new LiveDataValueUpdateBean(sequenceNumber, spec, fields);
  }

}
