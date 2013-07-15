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

import com.opengamma.engine.cache.CacheSelectHint;

/**
 * Fudge message builder for {@code CacheSelectHint}.
 * 
 * <pre>
 * message CacheSelectHint {
 *   required long[] cacheValues;         // values that are explicitly located
 *   required boolean cacheValuesPrivate; // true if the explicit values are private, false if shared
 * }
 * </pre>
 */
@FudgeBuilderFor(CacheSelectHint.class)
public class CacheSelectHintFudgeBuilder implements FudgeBuilder<CacheSelectHint> {

  private static final String VALUES_FIELD_NAME = "cacheValues";
  private static final String PRIVATE_FIELD_NAME = "cacheValuesPrivate";
  
  public static void buildMessageImpl(final MutableFudgeMsg msg, final CacheSelectHint object) {
    msg.add(VALUES_FIELD_NAME, object.getValueIdentifiers());
    msg.add(PRIVATE_FIELD_NAME, object.isPrivate());
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CacheSelectHint object) {
    MutableFudgeMsg msg = serializer.newMessage();
    buildMessageImpl(msg, object);
    return msg;
  }

  public static CacheSelectHint buildObjectImpl(final FudgeMsg msg) {
    final long[] valueIdentifiers = (long[]) msg.getByName(VALUES_FIELD_NAME).getValue();
    final boolean isPrivate = msg.getBoolean(PRIVATE_FIELD_NAME);
    return CacheSelectHint.create(valueIdentifiers, isPrivate);
  }

  @Override
  public CacheSelectHint buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return buildObjectImpl(msg);
  }

}
