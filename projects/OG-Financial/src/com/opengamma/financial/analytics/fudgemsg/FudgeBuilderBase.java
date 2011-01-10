/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

  /**
   * Builds the message by serializing the specified object.
   * <p>
   * This method creates a new message and uses {@link #buildMessage(FudgeSerializationContext, MutableFudgeFieldContainer, Object)}
   * to populate it.
   * @param context  the Fudge context, not null
   * @param object  the object being serialized
   * @return the created object, not null
   */
  @Override
  public final MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, T object) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(null, 0, object.getClass().getName());
    buildMessage(context, message, object);
    return message;
  }

  /**
   * Populates the message which is created by this base class.
   * @param context  the Fudge context, not null
   * @param message  the message to populate, not null
   * @param object  the object being serialized
   */
  protected abstract void buildMessage(FudgeSerializationContext context, MutableFudgeFieldContainer message, T object);

}
