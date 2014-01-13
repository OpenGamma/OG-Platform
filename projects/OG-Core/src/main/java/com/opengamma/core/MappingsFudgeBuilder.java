/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Maps;

/**
 * Fudge builder for {@link Mappings}.
 */
@FudgeBuilderFor(Mappings.class)
public class MappingsFudgeBuilder implements FudgeBuilder<Mappings> {

  private static final String ITEM = "item";
  private static final String VALUE = "value";
  private static final String MAPPING = "mapping";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Mappings mappings) {
    MutableFudgeMsg msg = serializer.newMessage();
    for (Map.Entry<String, String> entry : mappings.getMappings().entrySet()) {
      MutableFudgeMsg itemMsg = serializer.newMessage();
      itemMsg.add(VALUE, entry.getKey());
      itemMsg.add(MAPPING, entry.getValue());
      serializer.addToMessage(msg, ITEM, null, itemMsg);
    }
    return msg;
  }

  @Override
  public Mappings buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Map<String, String> mappings = Maps.newHashMap();
    for (FudgeField itemField : msg.getAllByName(ITEM)) {
      FudgeMsg itemMsg = (FudgeMsg) itemField.getValue();
      String value = deserializer.fieldValueToObject(String.class, itemMsg.getByName(VALUE));
      String mapping = deserializer.fieldValueToObject(String.class, itemMsg.getByName(MAPPING));
      mappings.put(value, mapping);
    }
    return new Mappings(mappings);
  }
}
