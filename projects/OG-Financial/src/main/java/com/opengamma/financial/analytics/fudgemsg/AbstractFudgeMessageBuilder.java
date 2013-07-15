/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeMessageBuilder;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.fudgemsg.WriteReplaceHelper;

/**
 * Partial implementation of {@link FudgeMessageBuilder}.
 */
/* package */abstract class AbstractFudgeMessageBuilder<T> implements FudgeMessageBuilder<T> {

  /**
   * Builds the message by serializing the specified object.
   * <p>
   * This method creates a new message and uses {@link #buildMessage(FudgeSerializer, MutableFudgeMsg, Object)} to populate it.
   * 
   * @param serializer the serializer, not null
   * @param object the object being serialized
   * @return the created object, not null
   */
  @Override
  public final MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final T object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    buildMessage(serializer, message, object);
    return message;
  }

  /**
   * Populates the message which is created by this base class.
   * 
   * @param serializer the serializer, not null
   * @param message the message to populate, not null
   * @param object the object being serialized
   */
  protected abstract void buildMessage(FudgeSerializer serializer, MutableFudgeMsg message, T object);

  /**
   * Replaces an anonymous inner class with a serializable substitution based on its {@code writeReplace} method.
   * 
   * @param object the object to substitute, not null
   * @return the substitution object as returned by its {@code writeReplace} method
   * @throws OpenGammaRuntimeException if no suitable method is defined or an error occurs in its execution
   */
  protected static Object substituteObject(final Object object) {
    return WriteReplaceHelper.writeReplace(object);
  }

}
