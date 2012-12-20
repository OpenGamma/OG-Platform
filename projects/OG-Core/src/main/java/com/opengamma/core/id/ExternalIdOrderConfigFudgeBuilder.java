/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for ExternalIdOrderConfig
 */
@FudgeBuilderFor(ExternalIdOrderConfig.class)
public class ExternalIdOrderConfigFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ExternalIdOrderConfig> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ExternalIdOrderConfig object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    for (Map.Entry<ExternalScheme, Integer> entry : object.getRateMap().entrySet()) {
      final MutableFudgeMsg entryMsg = serializer.newMessage();
      serializer.addToMessage(entryMsg, "scheme", null, entry.getKey().getName());
      serializer.addToMessage(entryMsg, "rating", null, entry.getValue());
      serializer.addToMessage(msg, "entry", null, entryMsg);
    }
    return msg;
  }

  @Override
  public ExternalIdOrderConfig buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    Map<ExternalScheme, Integer> properMap = Maps.newHashMap();
    for (FudgeField field : message.getAllByName("entry")) {
      FudgeMsg entryMsg = deserializer.fieldValueToObject(FudgeMsg.class, field);
      ExternalScheme scheme = ExternalScheme.of(entryMsg.getString("scheme"));
      int rating = entryMsg.getInt("rating");
      properMap.put(scheme, rating);
    }
    ExternalIdOrderConfig config = new ExternalIdOrderConfig();
    config.setRateMap(properMap);
    return config;
  }

}
