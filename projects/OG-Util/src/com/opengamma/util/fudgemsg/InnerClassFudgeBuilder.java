/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFactory;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.types.FudgeWireType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Builder to convert inner classes to and from Fudge.
 *
 * @param <T> the bean type
 */
public final class InnerClassFudgeBuilder<T> implements FudgeBuilder<T> {

  private FudgeBuilderFactory _delegateFudgeBuilderFactory;

  /**
   * Creates a builder for inner class
   * @param delegate fudge builder factory used to obtain fudge builders for outer class 
   * and ctor consturctor parameters of the inner class
   */
  public InnerClassFudgeBuilder(FudgeBuilderFactory delegate) {
    _delegateFudgeBuilderFactory = delegate;
  }

  @SuppressWarnings("unchecked")
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, final T inner) {
    try {
      MutableFudgeMsg msg = serializer.newMessage();
      //save the internal class name
      msg.add(null, FudgeSerializer.TYPES_HEADER_ORDINAL, FudgeWireType.STRING, inner.getClass().getName());

      //save the ctor parameters
      List<Object> parameters = AccessController.doPrivileged(new PrivilegedAction<List<Object>>() {
        @Override
        public List<Object> run() {
          try {
            final Constructor[] ctors = inner.getClass().getDeclaredConstructors();
            //We require that inner classes got only one ctor (anonymous inner classes will do)
            //in order to avoid disambiguity
            if (ctors.length == 1) {
              final Constructor ctor = ctors[0];
              // types of parameters of ctor
              final Class[] parameterTypes = ctor.getParameterTypes();
              // all declared parameters of the inner class
              final Field[] fs = inner.getClass().getDeclaredFields();
              // extracting copiler synthetized fields of inner class
              // first are the not sinthetized fields (regular ones) we need to skip
              // then there are compiler synthetized fields with corresponds to ctor parameters
              // the last field is the reference to enclosing object so we need to skipp it as well
              final Field[] paramFields = Arrays.copyOfRange(fs, fs.length - parameterTypes.length, fs.length - 1);
              final List<Object> parameters = newArrayList();
              for (Field paramField : paramFields) {
                paramField.setAccessible(true);
                parameters.add(paramField.get(inner));
              }
              return parameters;
            }
          } catch (IllegalAccessException e) {
            // Ignore
          }
          return null;
        }
      });

      for (Object parameter : parameters) {
        //save the ctor parameter                  
        serializer.addToMessageWithClassHeaders(msg, null, 1, parameter);
      }

      return msg;
    } catch (RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to serialize: " + inner.getClass().getName(), ex);
    }
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public T buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {

    final FudgeField classNameField = msg.getByOrdinal(FudgeSerializer.TYPES_HEADER_ORDINAL);
    final String className = (String) classNameField.getValue();
    try {
      final List<Object> parameters = newArrayList();
      parameters.add(null);//the omitted enclosing object
      for (FudgeField parameterField : msg.getAllByOrdinal(1)) {
        parameters.add(deserializer.fieldValueToObject(parameterField));
      }      

      return (T) AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override
        public Object run() {
          try {
            final Class<?> innerClass = Class.forName(className);
            final Constructor[] ctors = innerClass.getDeclaredConstructors();
            //We require that inner classes got only one ctor (anonymous inner classes will do)
            //in order to avoid disambiguity
            if (ctors.length == 1) {
              final Constructor ctor = ctors[0];
              ctor.setAccessible(true);   // solution
              return ctor.newInstance(parameters.toArray());
            }
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          } catch (InstantiationException e) {
            throw new RuntimeException(e);
          } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
          return null;
        }
      });

    } catch (RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to deserialize: " + className, ex);
    } 
  }

}
