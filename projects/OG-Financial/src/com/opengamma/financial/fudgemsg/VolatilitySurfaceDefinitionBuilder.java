/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting VolatilitySurfaceDefinition instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilitySurfaceDefinition.class)
public class VolatilitySurfaceDefinitionBuilder implements FudgeBuilder<VolatilitySurfaceDefinition<?, ?>> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final VolatilitySurfaceDefinition<?, ?> object) {
    final MutableFudgeMsg message = context.newMessage();
    context.addToMessage(message, "currency", null, object.getCurrency());
    message.add("name", object.getName());
    for (final Object x : object.getXs()) {
      if (x instanceof Number) {
        context.addToMessageWithClassHeaders(message, "xs", null, x);
      } else {
        message.add("xs", null, FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(x), x.getClass()));
      }
    }
    for (final Object y : object.getYs()) {
      if (y instanceof Number) {
        context.addToMessageWithClassHeaders(message, "ys", null, y);
      } else {
        message.add("ys", null, FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(y), y.getClass()));
      }
    }
    return message;
  }

  @Override
  public VolatilitySurfaceDefinition<?, ?> buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    final Currency currency = context.fieldValueToObject(Currency.class, message.getByName("currency"));
    final String name = message.getString("name");
    final List<FudgeField> xsFields = message.getAllByName("xs");
    final List<Object> xs = new ArrayList<Object>();
    for (final FudgeField xField : xsFields) {
      final Object x = context.fieldValueToObject(xField);
      xs.add(x);
    }
    final List<FudgeField> ysFields = message.getAllByName("ys");
    final List<Object> ys = new ArrayList<Object>();
    for (final FudgeField yField : ysFields) {
      final Object y = context.fieldValueToObject(yField);
      ys.add(y);
    }
    return new VolatilitySurfaceDefinition<Object, Object>(name, currency, xs.toArray(), ys.toArray());
  }

}
