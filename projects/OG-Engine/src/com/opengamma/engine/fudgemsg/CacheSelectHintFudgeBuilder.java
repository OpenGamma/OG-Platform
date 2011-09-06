/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.cache.CacheSelectHint;

/**
 * Fudge message builder for {@code CacheSelectHint}.
 */
@FudgeBuilderFor(CacheSelectHint.class)
public class CacheSelectHintFudgeBuilder implements FudgeBuilder<CacheSelectHint> {

  private static final String VALUES_FIELD_NAME = "cacheValues";
  private static final String PRIVATE_FIELD_NAME = "cacheValuesPrivate";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CacheSelectHint object) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(VALUES_FIELD_NAME, object.getValueIdentifiers());
    msg.add(PRIVATE_FIELD_NAME, object.isPrivate());
    return msg;
  }

  @Override
  public CacheSelectHint buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final long[] valueIdentifiers = (long[]) msg.getByName(VALUES_FIELD_NAME).getValue();
    final boolean isPrivate = msg.getBoolean(PRIVATE_FIELD_NAME);
    return CacheSelectHint.create(valueIdentifiers, isPrivate);
  }

}
