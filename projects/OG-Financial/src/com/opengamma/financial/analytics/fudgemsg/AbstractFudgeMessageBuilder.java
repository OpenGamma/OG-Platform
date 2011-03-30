/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.lang.reflect.InvocationTargetException;

import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeMessageBuilder;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Partial implementation of {@link FudgeMessageBuilder}.
 */
/* package */abstract class AbstractFudgeMessageBuilder<T> implements FudgeMessageBuilder<T> {

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

  /**
   * Replaces an anonymous inner class with a serializable substitution.
   */
  protected static Object substituteObject(final Object object) {
    Class<?> clazz = object.getClass();
    if (clazz.isAnonymousClass()) {
      clazz = clazz.getEnclosingClass();
      while (clazz != null) {
        try {
          return clazz.getDeclaredMethod("innerClassSubstitute", Object.class).invoke(null, object);
        } catch (IllegalAccessException e) {
          throw new OpenGammaRuntimeException("No serialization substitution available for anonymous inner class object " + object, e);
        } catch (InvocationTargetException e) {
          if (e.getTargetException() instanceof RuntimeException) {
            throw (RuntimeException) e.getTargetException();
          }
        } catch (NoSuchMethodException e) {
          // Ignore
          //e.printStackTrace();
        }
        clazz = clazz.getEnclosingClass();
      }
      throw new OpenGammaRuntimeException("No serialization substitution available for anonymous inner class object " + object);
    }
    return object;
  }

}
