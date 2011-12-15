/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code ComponentInfosMsg}.
 */
@FudgeBuilderFor(ComponentInfosMsg.class)
public class ComponentInfosMsgFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ComponentInfosMsg> {

  /** Field name. */
  public static final String INFOS_FIELD_NAME = "infos";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ComponentInfosMsg object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ComponentInfosMsgFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, ComponentInfosMsg object, final MutableFudgeMsg msg) {
    MutableFudgeMsg infos = serializer.newMessage();
    for (ComponentInfo info : object.getInfos()) {
      infos.add(null, null, ComponentInfoFudgeBuilder.toFudgeMsg(serializer, info));
    }
    addToMessage(msg, INFOS_FIELD_NAME, infos);
  }

  @Override
  public ComponentInfosMsg buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ComponentInfosMsg object = new ComponentInfosMsg();
    ComponentInfosMsgFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, ComponentInfosMsg object) {
    FudgeMsg infos = msg.getMessage(INFOS_FIELD_NAME);
    for (FudgeField field : infos) {
      if (field instanceof FudgeMsg) {
        object.getInfos().add(ComponentInfoFudgeBuilder.fromFudgeMsg(deserializer, (FudgeMsg) field));
      }
    }
  }

}
