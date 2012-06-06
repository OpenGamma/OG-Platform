/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.engine.view.cache.NotCalculatedSentinel;

/**
 * Fudge builder for {@code NotCalculatedSentinel}.
 */
@FudgeBuilderFor(NotCalculatedSentinel.class)
public class NotCalculatedSentinelFudgeBuilder implements FudgeBuilder<NotCalculatedSentinel> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final NotCalculatedSentinel sentinel) {
    final MutableFudgeMsg message = serializer.newMessage();
    return message;
  }

  @Override
  public NotCalculatedSentinel buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return NotCalculatedSentinel.getInstance();
  }

}
