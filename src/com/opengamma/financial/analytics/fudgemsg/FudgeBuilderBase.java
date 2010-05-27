/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * Base class for the builder pattern.
 */
/* package */ abstract class FudgeBuilderBase<T> implements FudgeBuilder<T> {
  
  protected abstract void buildMessage(FudgeSerializationContext context, MutableFudgeFieldContainer message, T object);

  @Override
  public final MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, T object) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(null, 0, object.getClass().getName());
    buildMessage(context, message, object);
    return message;
  }

}
