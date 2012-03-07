/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.net.URI;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code ComponentInfo}.
 * <p>
 * This converts the data to and from the Fudge message protocol format.
 */
@FudgeBuilderFor(ComponentInfo.class)
public class ComponentInfoFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ComponentInfo> {

  /** Field name. */
  public static final String TYPE_FIELD_NAME = "type";
  /** Field name. */
  public static final String CLASSIFIER_FIELD_NAME = "classifier";
  /** Field name. */
  public static final String URI_FIELD_NAME = "uri";
  /** Field name. */
  public static final String ATTRIBUTES_FIELD_NAME = "attributes";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ComponentInfo object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ComponentInfoFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(FudgeSerializer serializer, ComponentInfo object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ComponentInfoFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, ComponentInfo object, final MutableFudgeMsg msg) {
    addToMessage(msg, TYPE_FIELD_NAME, object.getType().getName());
    addToMessage(msg, CLASSIFIER_FIELD_NAME, object.getClassifier());
    if (object.getUri() != null) {
      addToMessage(msg, URI_FIELD_NAME, object.getUri().toString());
    }
    MutableFudgeMsg attributesMsg = serializer.newMessage();
    for (Entry<String, String> entry : object.getAttributes().entrySet()) {
      attributesMsg.add(entry.getKey(), entry.getValue());
    }
    addToMessage(msg, ATTRIBUTES_FIELD_NAME, attributesMsg);
  }

  @Override
  public ComponentInfo buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ComponentInfo object = new ComponentInfo();
    ComponentInfoFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static ComponentInfo fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    ComponentInfo object = new ComponentInfo();
    ComponentInfoFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, ComponentInfo object) {
    String typeStr = msg.getString(TYPE_FIELD_NAME);
    try {
      object.setType(ComponentInfoFudgeBuilder.class.getClassLoader().loadClass(typeStr));
    } catch (ClassNotFoundException ex) {
      // ignore, as server may have classes that are not in the client classpath
      object.setType(ClassNotFoundException.class);
      object.addAttribute(ClassNotFoundException.class.getName(), typeStr);
    }
    object.setClassifier(msg.getString(CLASSIFIER_FIELD_NAME));
    String uriStr = msg.getString(URI_FIELD_NAME);
    if (uriStr != null) {
      object.setUri(URI.create(uriStr));
    }
    FudgeMsg attributes = msg.getMessage(ATTRIBUTES_FIELD_NAME);
    for (FudgeField field : attributes) {
      object.addAttribute(field.getName(), field.getValue().toString());
    }
  }

}
