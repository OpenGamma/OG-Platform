/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeMessageBuilder;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Partial implementation of {@link FudgeMessageBuilder}.
 */
/* package */abstract class AbstractFudgeMessageBuilder<T> implements FudgeMessageBuilder<T> {

  /**
   * Builds the message by serializing the specified object.
   * <p>
   * This method creates a new message and uses {@link #buildMessage(FudgeSerializer, MutableFudgeMsg, Object)}
   * to populate it.
   * @param serializer  the serializer, not null
   * @param object  the object being serialized
   * @return the created object, not null
   */
  @Override
  public final MutableFudgeMsg buildMessage(FudgeSerializer serializer, T object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    buildMessage(serializer, message, object);
    return message;
  }

  /**
   * Populates the message which is created by this base class.
   * @param serializer  the serializer, not null
   * @param message  the message to populate, not null
   * @param object  the object being serialized
   */
  protected abstract void buildMessage(FudgeSerializer serializer, MutableFudgeMsg message, T object);

  /**
   * The cache of previously resolved (and forced accessible) {@code writeReplace} methods.
   */
  private static final ConcurrentMap<Class<?>, Method> s_writeReplace = new ConcurrentHashMap<Class<?>, Method>();

  /**
   * Replaces an anonymous inner class with a serializable substitution based on its {@code writeReplace}
   * method.
   * 
   * @param object  the object to substitute, not null
   * @return the substitution object as returned by its {@code writeReplace} method
   * @throws OpenGammaRuntimeException if no suitable method is defined or an error occurs in its execution
   */
  protected static Object substituteObject(final Object object) {
    final Class<?> clazz = object.getClass();
    if (clazz.isAnonymousClass()) {
      Method method = s_writeReplace.get(clazz);
      if (method == null) {
        method = AccessController.doPrivileged(new PrivilegedAction<Method>() {

          @Override
          public Method run() {
            try {
              final Method mtd = clazz.getMethod("writeReplace");
              mtd.setAccessible(true);
              return mtd;
            } catch (NoSuchMethodException e) {
              // Ignore
            }
            return null;
          }

        });
        if (method == null) {
          throw new OpenGammaRuntimeException("No serialization substitution available for anonymous inner class object " + object);
        }
        s_writeReplace.putIfAbsent(clazz, method);
      }
      try {
        return method.invoke(object);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Couldn't call writeReplace on inner class", e);
      }
    }
    return object;
  }

}
