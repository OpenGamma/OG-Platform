/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.net.URI;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code ComponentServer}.
 */
@FudgeBuilderFor(ComponentServer.class)
public class ComponentServerFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ComponentServer> {

  /** Field name. */
  public static final String URI_FIELD_NAME = "uri";
  /** Field name. */
  public static final String INFOS_FIELD_NAME = "infos";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ComponentServer object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ComponentServerFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, ComponentServer object, final MutableFudgeMsg msg) {
    MutableFudgeMsg infos = serializer.newMessage();
    if (object.getUri() != null) {
      addToMessage(msg, URI_FIELD_NAME, object.getUri().toString());
    }
    for (ComponentInfo info : object.getComponentInfos()) {
      infos.add(null, null, ComponentInfoFudgeBuilder.toFudgeMsg(serializer, info));
    }
    addToMessage(msg, INFOS_FIELD_NAME, infos);
  }

  @Override
  public ComponentServer buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ComponentServer object = new ComponentServer();
    ComponentServerFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, ComponentServer object) {
    String uriStr = msg.getString(URI_FIELD_NAME);
    if (uriStr != null) {
      object.setUri(URI.create(uriStr));
    }
    FudgeMsg infos = msg.getMessage(INFOS_FIELD_NAME);
    for (FudgeField field : infos) {
      if (field.getValue() instanceof FudgeMsg) {
        ComponentInfo info = ComponentInfoFudgeBuilder.fromFudgeMsg(deserializer, (FudgeMsg) field.getValue());
        if (info.getType() != ClassNotFoundException.class) {
          object.getComponentInfos().add(info);
        }
      }
    }
  }

}
