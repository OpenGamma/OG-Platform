/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveDefinition;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
@FudgeBuilderFor(FuturePriceCurveDefinition.class)
public class FuturePriceCurveDefinitionBuilder implements FudgeBuilder<FuturePriceCurveDefinition<?>> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final FuturePriceCurveDefinition<?> object) {
    final MutableFudgeMsg message = context.newMessage();
    message.add("target", FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    message.add("name", object.getName());
    for (final Object x : object.getXs()) {
      if (x instanceof Number) {
        context.addToMessageWithClassHeaders(message, "xs", null, x);
      } else {
        message.add("xs", null, FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(x), x.getClass()));
      }
    }
    return message;
  }

  @Override
  public FuturePriceCurveDefinition<?> buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    final UniqueIdentifiable target = context.fieldValueToObject(UniqueIdentifiable.class, message.getByName("target"));
    final String name = message.getString("name");
    final List<FudgeField> xsFields = message.getAllByName("xs");
    final List<Object> xs = new ArrayList<Object>();
    for (final FudgeField xField : xsFields) {
      final Object x = context.fieldValueToObject(xField);
      xs.add(x);
    }
    return new FuturePriceCurveDefinition<Object>(name, target, xs.toArray());
  }
}
