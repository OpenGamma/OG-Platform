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

import com.opengamma.engine.view.cache.MissingMarketDataSentinel;

/**
 * Fudge message builder for {@code MissingLiveDataSentinel}.
 */
@FudgeBuilderFor(MissingMarketDataSentinel.class)
public class MissingLiveDataSentinelFudgeBuilder implements FudgeBuilder<MissingMarketDataSentinel> {
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final MissingMarketDataSentinel sentinel) {
    final MutableFudgeMsg message = serializer.newMessage();
    return message;
  }

  @Override
  public MissingMarketDataSentinel buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return MissingMarketDataSentinel.getInstance();
  }

}
