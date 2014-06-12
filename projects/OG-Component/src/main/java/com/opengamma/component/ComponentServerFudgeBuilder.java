/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.beans.BeanBuilder;
import org.threeten.bp.Instant;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code ComponentServer}.
 * <p>
 * This converts the data to and from the Fudge message protocol format.
 */
@FudgeBuilderFor(ComponentServer.class)
public class ComponentServerFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ComponentServer> {

  /** Field name. */
  public static final String URI_FIELD_NAME = "uri";
  /** Field name. */
  public static final String VERSION_FIELD_NAME = "version";
  /** Field name. */
  public static final String BUILD_FIELD_NAME = "build";
  /** Field name. */
  public static final String CURRENT_INSTANT_FIELD_NAME = "currentInstant";
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
    if (object.getUri() != null) {
      addToMessage(msg, VERSION_FIELD_NAME, object.getVersion());
    }
    if (object.getUri() != null) {
      addToMessage(msg, BUILD_FIELD_NAME, object.getBuild());
    }
    if (object.getUri() != null) {
      addToMessage(msg, CURRENT_INSTANT_FIELD_NAME, object.getCurrentInstant());
    }
    for (ComponentInfo info : object.getComponentInfos()) {
      infos.add(null, null, ComponentInfoFudgeBuilder.toFudgeMsg(serializer, info));
    }
    addToMessage(msg, INFOS_FIELD_NAME, infos);
  }

  @Override
  public ComponentServer buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    BeanBuilder<? extends ComponentServer> builder = ComponentServer.meta().builder();
    ComponentServerFudgeBuilder.fromFudgeMsg(deserializer, msg, builder);
    return builder.build();
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, BeanBuilder<? extends ComponentServer> builder) {
    String uriStr = msg.getString(URI_FIELD_NAME);
    if (uriStr != null) {
      builder.set(ComponentServer.meta().uri(), URI.create(uriStr));
    }
    String versionStr = msg.getString(VERSION_FIELD_NAME);
    if (versionStr != null) {
      builder.set(ComponentServer.meta().version(), versionStr);
    }
    String buildStr = msg.getString(BUILD_FIELD_NAME);
    if (buildStr != null) {
      builder.set(ComponentServer.meta().build(), buildStr);
    }
    Instant instant = msg.getValue(Instant.class, CURRENT_INSTANT_FIELD_NAME);
    if (instant != null) {
      builder.set(ComponentServer.meta().currentInstant(), instant);
    }
    FudgeMsg infoMsgs = msg.getMessage(INFOS_FIELD_NAME);
    List<ComponentInfo> infos = new ArrayList<>();
    for (FudgeField field : infoMsgs) {
      if (field.getValue() instanceof FudgeMsg) {
        ComponentInfo info = ComponentInfoFudgeBuilder.fromFudgeMsg(deserializer, (FudgeMsg) field.getValue());
        if (info.getType() != ClassNotFoundException.class) {
          infos.add(info);
        }
      }
    }
    builder.set(ComponentServer.meta().componentInfos(), infos);
  }

}
