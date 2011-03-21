/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import java.util.Arrays;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.FunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Trivial function for debugging. Returns a Fudge message
 */
public class DebugFunctionMessage implements PublishedFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(DebugFunctionMessage.class);

  private Data execute(final Data foo, final Data bar) {
    s_logger.debug("Foo = {}, Bar = {}", foo, bar);
    final FudgeContext ctx = FudgeContext.GLOBAL_DEFAULT;
    final MutableFudgeFieldContainer msg = ctx.newMessage();
    final FudgeSerializationContext sctx = new FudgeSerializationContext(ctx);
    sctx.objectToFudgeMsgWithClassHeaders(msg, "foo", null, foo);
    sctx.objectToFudgeMsgWithClassHeaders(msg, "bar", null, bar);
    return DataUtil.of(msg);
  }

  @Override
  public MetaFunction getMetaFunction() {
    final List<MetaParameter> args = Arrays
        .asList(new MetaParameter("foo", JavaTypeInfo.builder(Data.class).defaultValue(null).get()), new MetaParameter(
            "bar", JavaTypeInfo.builder(Data.class).defaultValue(null).get()));
    final FunctionInvoker invoker = new AbstractFunctionInvoker() {
      @Override
      public Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
        return execute((Data) parameters[0], (Data) parameters[1]);
      }
    };
    return new MetaFunction("DebugFunctionMessage", args, invoker);
  }
}
