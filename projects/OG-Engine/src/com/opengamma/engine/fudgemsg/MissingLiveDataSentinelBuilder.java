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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.cache.MissingLiveDataSentinel;

/**
 * Fudge message builder for {@code MissingLiveDataSentinel}.
 */
@FudgeBuilderFor(MissingLiveDataSentinel.class)
public class MissingLiveDataSentinelBuilder implements FudgeBuilder<MissingLiveDataSentinel> {
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final MissingLiveDataSentinel sentinel) {
    final MutableFudgeMsg message = context.newMessage();
    return message;
  }

  @Override
  public MissingLiveDataSentinel buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    return MissingLiveDataSentinel.getInstance();
  }

}
