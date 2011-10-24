/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import java.util.Arrays;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.FunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * A function which takes one of all types and returns an arbitrary type. This is for testing the type converters.
 */
public class DebugFunctionMultipleTypes implements PublishedFunction {

  private FudgeMsg execute(final byte byteValue, final boolean booleanValue, final char charValue, final double doubleValue, final float floatValue, final int intValue,
      final long longValue,
      final short shortValue, final String stringValue, final FudgeMsg messageValue) {
    final MutableFudgeMsg message = FudgeContext.GLOBAL_DEFAULT.newMessage();
    message.add("byte", byteValue);
    message.add("boolean", booleanValue);
    message.add("char", Character.toString(charValue));
    message.add("double", doubleValue);
    message.add("float", floatValue);
    message.add("int", intValue);
    message.add("long", longValue);
    message.add("short", shortValue);
    if (stringValue != null) {
      message.add("string", stringValue);
    }
    if (messageValue != null) {
      message.add("message", messageValue);
    }
    return message;
  }

  @Override
  public MetaFunction getMetaFunction() {
    final List<MetaParameter> args = Arrays.asList(
        new MetaParameter("byte", JavaTypeInfo.builder(Byte.TYPE).get()),
        new MetaParameter("boolean", JavaTypeInfo.builder(Boolean.TYPE).get()),
        new MetaParameter("char", JavaTypeInfo.builder(Character.TYPE).get()),
        new MetaParameter("double", JavaTypeInfo.builder(Double.TYPE).get()),
        new MetaParameter("float", JavaTypeInfo.builder(Float.TYPE).get()),
        new MetaParameter("int", JavaTypeInfo.builder(Integer.TYPE).get()),
        new MetaParameter("long", JavaTypeInfo.builder(Long.TYPE).get()),
        new MetaParameter("short", JavaTypeInfo.builder(Short.TYPE).get()),
        new MetaParameter("string", JavaTypeInfo.builder(String.class).allowNull().get()),
        new MetaParameter("message", JavaTypeInfo.builder(FudgeMsg.class).allowNull().get()));
    final FunctionInvoker invoker = new AbstractFunctionInvoker(args) {
      @Override
      public Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
        if (parameters.length != 10) {
          throw new IllegalArgumentException("Wrong number of parameters");
        }
        return execute((Byte) parameters[0], (Boolean) parameters[1], (Character) parameters[2], (Double) parameters[3], (Float) parameters[4], (Integer) parameters[5], (Long) parameters[6],
            (Short) parameters[7], (String) parameters[8], (FudgeMsg) parameters[9]);
      }
    };
    return new MetaFunction(Categories.DEBUG, "DebugFunctionMultipleTypes", args, invoker);
  }

}
